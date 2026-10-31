const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-arishmes-1d2fbd9c-3eca-4d8b-b954-89b274edd53f";

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("unauthenticated user cannot read profiles", async () => {
  const unauthContext = testEnv.unauthenticatedContext();
  const db = unauthContext.firestore(DATABASE_ID);
  await assertFails(db.collection("users").doc("user1").get());
});

test("authenticated user can read profile and write their own profile", async () => {
  const aliceContext = testEnv.authenticatedContext("alice");
  const db = aliceContext.firestore(DATABASE_ID);

  await assertSucceeds(
    db.collection("users").doc("alice").set({
      userId: "alice",
      displayName: "Alice",
      username: "alice_w",
      phoneNumber: "+1234567890",
      about: "Hey there! I am using Arish Messenger.",
      photoUrl: "",
      isOnline: true,
      lastSeen: new Date(),
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );

  // Bob can read Alice's profile
  const bobContext = testEnv.authenticatedContext("bob");
  const bobDb = bobContext.firestore(DATABASE_ID);
  await assertSucceeds(bobDb.collection("users").doc("alice").get());

  // But Bob cannot write Alice's profile
  await assertFails(
    bobDb.collection("users").doc("alice").update({
      displayName: "Hacked"
    })
  );
});

test("participant can create chat and post messages", async () => {
  const alice = testEnv.authenticatedContext("alice");
  const aliceDb = alice.firestore(DATABASE_ID);

  await assertSucceeds(
    aliceDb.collection("chats").doc("chat_1").set({
      id: "chat_1",
      isGroup: false,
      participantUids: ["alice", "bob"],
      lastMessageText: "Hello Bob",
      lastMessageSenderId: "alice",
      lastMessageTimestamp: new Date(),
      unreadCounts: { bob: 1 },
      typingUids: [],
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );

  // Alice posts message
  await assertSucceeds(
    aliceDb.collection("chats").doc("chat_1").collection("messages").doc("msg_1").set({
      id: "msg_1",
      chatId: "chat_1",
      senderId: "alice",
      senderName: "Alice",
      recipientId: "bob",
      participantUids: ["alice", "bob"],
      text: "Hello Bob",
      type: "text",
      status: "sent",
      reactions: {},
      isEdited: false,
      isDeleted: false,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );

  // Eve (not in participantUids) cannot read messages
  const eve = testEnv.authenticatedContext("eve");
  const eveDb = eve.firestore(DATABASE_ID);
  await assertFails(eveDb.collection("chats").doc("chat_1").get());
  await assertFails(eveDb.collection("chats").doc("chat_1").collection("messages").doc("msg_1").get());
});
