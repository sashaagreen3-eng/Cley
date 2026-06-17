#!/usr/bin/env bash
set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <target-dir>"
  exit 1
fi

TARGET_DIR="$1"
echo "Создаю проект в ./${TARGET_DIR} ..."
rm -rf "${TARGET_DIR}"
mkdir -p "${TARGET_DIR}"

# package.json
cat > "${TARGET_DIR}/package.json" <<'EOF'
{
  "name": "rn-e2ee-messenger",
  "version": "0.1.0",
  "private": true,
  "scripts": {
    "start": "react-native start",
    "android": "react-native run-android",
    "ios": "react-native run-ios",
    "lint": "eslint . --ext .ts,.tsx"
  },
  "dependencies": {
    "@react-native-firebase/app": "^17.0.0",
    "@react-native-firebase/auth": "^17.0.0",
    "@react-native-firebase/firestore": "^17.0.0",
    "@react-native-firebase/storage": "^17.0.0",
    "libsignal-protocol-typescript": "^0.0.0",
    "react": "18.2.0",
    "react-native": "0.71.10",
    "@react-navigation/native": "^6.1.6",
    "@react-navigation/native-stack": "^6.9.12",
    "react-native-gesture-handler": "^2.9.0",
    "react-native-reanimated": "^2.14.4",
    "@react-native-async-storage/async-storage": "^1.20.1",
    "react-native-get-random-values": "^1.8.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.14",
    "@types/react-native": "^0.71.7",
    "typescript": "^5.2.2",
    "eslint": "^8.50.0"
  }
}
EOF

# tsconfig.json
cat > "${TARGET_DIR}/tsconfig.json" <<'EOF'
{
  "compilerOptions": {
    "target": "ESNext",
    "module": "ESNext",
    "jsx": "react-jsx",
    "moduleResolution": "node",
    "allowJs": true,
    "noEmit": true,
    "strict": true,
    "skipLibCheck": true,
    "esModuleInterop": true,
    "resolveJsonModule": true,
    "types": ["react-native"]
  },
  "exclude": ["node_modules", "android", "ios"]
}
EOF

# .gitignore
cat > "${TARGET_DIR}/.gitignore" <<'EOF'
node_modules/
android/
ios/
build/
*.keystore
.env
rn-e2ee-messenger.zip
EOF

# LICENSE (MIT)
cat > "${TARGET_DIR}/LICENSE" <<'EOF'
MIT License

Copyright (c) 2026

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software...
(Full text omitted for brevity — replace with your preferred license text)
EOF

# README.md
cat > "${TARGET_DIR}/README.md" <<'EOF'
# RN E2EE Messenger (scaffold)

Этот репозиторий — стартовый шаблон мессенджера на React Native + Firebase + Signal E2EE.

Важные заметки
- Firebase credentials нужно добавить вручную в `src/firebase/config.ts`.
- Полная реализация Signal-протокола в этом шаблоне частично упрощена (placeholder) — я включил весь каркас и готов реализовать полный Signal X3DH + Double Ratchet по запросу.
- Для запуска в React Native нужны полифилы (react-native-get-random-values и прочие). См. раздел «Полифилы» ниже.

Быстрый старт
1. Убедитесь, что у вас настроено окружение React Native CLI (не Expo).
2. Скопируйте/распакуйте этот проект, затем:
   - yarn install
   - npx pod-install ios
3. Настройте Firebase: создайте проект, включите Auth (email/password), Firestore и Storage. Вставьте конфиг в `src/firebase/config.ts`.
4. Запустите Metro: `yarn start` и запустите приложение `yarn android` или `yarn ios`.

Файлы примера
- src/firebase/* — инициализация Firebase
- src/services/* — auth + firestore helper + admin utilities (block/unblock)
- src/crypto/signalService.ts — wrapper для libsignal (с упрощениями/placeholder)
- src/screens/* — Login, ChatList, Chat, Admin
- firestore.rules — пример правил для Firestore, включая запрет на отправку сообщений заблокированными пользователями

Дальше
- Я могу полностью реализовать Signal-протокол (X3DH, SessionBuilder, SessionCipher, удаление one-time prekey после использования) и добавить шифрование вложений. Напишите, если нужно, и я добавлю в следующий коммит/ZIP.
EOF

# firestore.rules
cat > "${TARGET_DIR}/firestore.rules" <<'EOF'
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow create: if request.auth != null && request.auth.uid == userId;
      allow update: if request.auth != null && (
        request.auth.uid == userId
        || get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin'
      );
      allow delete: if false;
    }

    match /conversations/{convId} {
      allow read: if request.auth != null && request.auth.uid in resource.data.members;
      allow create: if request.auth != null;
      allow update, delete: if false;

      match /messages/{msgId} {
        allow read: if request.auth != null && request.auth.uid in get(/databases/$(database)/documents/conversations/$(convId)).data.members;

        allow create: if request.auth != null
                      && request.resource.data.senderId == request.auth.uid
                      && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isBlocked != true;

        allow update, delete: if false;
      }
    }
  }
}
EOF

# create project structure
mkdir -p "${TARGET_DIR}/src/crypto"
mkdir -p "${TARGET_DIR}/src/firebase"
mkdir -p "${TARGET_DIR}/src/navigation"
mkdir -p "${TARGET_DIR}/src/screens"
mkdir -p "${TARGET_DIR}/src/services"

# App.tsx
cat > "${TARGET_DIR}/App.tsx" <<'EOF'
import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import RootStack from './src/navigation';
import { initFirebase } from './src/firebase/init';
import { GestureHandlerRootView } from 'react-native-gesture-handler';

export default function App() {
  useEffect(() => {
    initFirebase();
  }, []);

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <NavigationContainer>
        <RootStack />
      </NavigationContainer>
    </GestureHandlerRootView>
  );
}
EOF

# firebase config placeholder
cat > "${TARGET_DIR}/src/firebase/config.ts" <<'EOF'
export const firebaseConfig = {
  apiKey: "REPLACE_ME",
  authDomain: "REPLACE_ME.firebaseapp.com",
  projectId: "REPLACE_ME",
  storageBucket: "REPLACE_ME.appspot.com",
  messagingSenderId: "REPLACE_ME",
  appId: "REPLACE_ME"
};
EOF

# firebase init
cat > "${TARGET_DIR}/src/firebase/init.ts" <<'EOF'
import { firebaseConfig } from './config';
import { initializeApp } from '@react-native-firebase/app';
import firestore from '@react-native-firebase/firestore';

let initialized = false;

export function initFirebase() {
  if (initialized) return;
  initializeApp(firebaseConfig as any);
  initialized = true;
  firestore().settings({ experimentalForceLongPolling: true });
}
EOF

# services/auth.ts
cat > "${TARGET_DIR}/src/services/auth.ts" <<'EOF'
import auth from '@react-native-firebase/auth';
import firestore from '@react-native-firebase/firestore';

export async function signUpEmail(email: string, password: string, displayName?: string) {
  const cred = await auth().createUserWithEmailAndPassword(email, password);
  await cred.user.updateProfile({ displayName: displayName ?? '' });
  await firestore().collection('users').doc(cred.user.uid).set({
    uid: cred.user.uid,
    email,
    displayName: displayName ?? '',
    createdAt: firestore.FieldValue.serverTimestamp()
  }, { merge: true });
  return cred.user;
}

export async function signInEmail(email: string, password: string) {
  const cred = await auth().signInWithEmailAndPassword(email, password);
  return cred.user;
}

export function signOut() {
  return auth().signOut();
}

export function currentUser() {
  return auth().currentUser;
}
EOF

# services/firestore.ts (includes admin functions)
cat > "${TARGET_DIR}/src/services/firestore.ts" <<'EOF'
import firestore from '@react-native-firebase/firestore';
import { FirebaseFirestoreTypes } from '@react-native-firebase/firestore';

export type MessageDoc = {
  id?: string;
  conversationId: string;
  senderId: string;
  ciphertext: string;
  keyId?: string;
  createdAt?: FirebaseFirestoreTypes.Timestamp;
  attachmentPath?: string;
  iv?: string;
};

export function createConversationBetween(aUid: string, bUid: string) {
  const convId = [aUid, bUid].sort().join('_');
  const ref = firestore().collection('conversations').doc(convId);
  ref.set({ id: convId, members: [aUid, bUid], isGroup: false }, { merge: true });
  return convId;
}

export function sendMessage(message: MessageDoc) {
  const ref = firestore().collection('conversations').doc(message.conversationId).collection('messages').doc();
  message.createdAt = firestore.FieldValue.serverTimestamp() as any;
  return ref.set({ ...message, id: ref.id });
}

export function subscribeMessages(conversationId: string, onUpdate: (msgs: MessageDoc[]) => void) {
  return firestore()
    .collection('conversations')
    .doc(conversationId)
    .collection('messages')
    .orderBy('createdAt', 'asc')
    .onSnapshot(snapshot => {
      const msgs: MessageDoc[] = [];
      snapshot.forEach(doc => msgs.push(doc.data() as MessageDoc));
      onUpdate(msgs);
    });
}

// Prekey storage
export function uploadPrekeys(uid: string, prekeysPayload: any) {
  return firestore().collection('users').doc(uid).collection('prekeys').doc('meta').set(prekeysPayload);
}

export async function fetchRemotePrekeys(remoteUid: string) {
  const doc = await firestore().collection('users').doc(remoteUid).collection('prekeys').doc('meta').get();
  return doc.exists ? doc.data() : null;
}

// --- Admin / user utilities ---

export async function blockUser(targetUid: string, adminUid: string, reason?: string) {
  return firestore().collection('users').doc(targetUid).set({
    isBlocked: true,
    blockedBy: adminUid,
    blockedAt: firestore.FieldValue.serverTimestamp(),
    blockReason: reason || null
  }, { merge: true });
}

export async function unblockUser(targetUid: string, adminUid: string) {
  return firestore().collection('users').doc(targetUid).set({
    isBlocked: false,
    unblockedBy: adminUid,
    unblockedAt: firestore.FieldValue.serverTimestamp(),
    blockReason: firestore.FieldValue.delete()
  }, { merge: true });
}

export async function listUsersForAdmin() {
  const snap = await firestore().collection('users').orderBy('createdAt', 'desc').get();
  const users: any[] = [];
  snap.forEach(d => users.push(d.data()));
  return users;
}

export async function getUserDoc(uid: string) {
  const doc = await firestore().collection('users').doc(uid).get();
  return doc.exists ? (doc.data() as any) : null;
}
EOF

# crypto/signalService.ts (placeholder implementation)
cat > "${TARGET_DIR}/src/crypto/signalService.ts" <<'EOF'
/**
 * Высокоуровневый wrapper вокруг libsignal-protocol-typescript.
 * В шаблоне часть функций упрощены (placeholders).
 * Перед production необходимо реализовать полноценную логику:
 * - KeyHelper.generateIdentityKeyPair, SignedPreKey, PreKeys
 * - SessionBuilder (X3DH) и SessionCipher (Double Ratchet)
 * - Надёжное хранение приватных ключей (SecureStore/Keychain)
 */
import * as signal from 'libsignal-protocol-typescript';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { fetchRemotePrekeys } from '../services/firestore';

const STORAGE_KEYS = {
  IDENTITY_KEY: 'signal_identity_key',
  REGISTRATION_ID: 'signal_registration_id',
  PREKEYS: 'signal_prekeys',
  SESSION_STORE_PREFIX: 'signal_session_'
};

export async function generateAndStoreIdentity(uid: string) {
  const registrationId = signal.KeyHelper.generateRegistrationId();
  const identityKeyPair = await signal.KeyHelper.generateIdentityKeyPair();
  const signedPreKey = await signal.KeyHelper.generateSignedPreKey(identityKeyPair, 1);
  const preKeys = [];
  for (let i = 1; i <= 10; i++) {
    preKeys.push(await signal.KeyHelper.generatePreKey(i + 1));
  }
  const payload = {
    registrationId,
    identityKeyPair: {
      pubKey: arrayBufferToBase64(identityKeyPair.pubKey),
      privKey: arrayBufferToBase64(identityKeyPair.privKey)
    },
    signedPreKey: {
      keyId: signedPreKey.keyId,
      pubKey: arrayBufferToBase64(signedPreKey.keyPair.pubKey),
      privKey: arrayBufferToBase64(signedPreKey.keyPair.privKey),
      signature: arrayBufferToBase64(signedPreKey.signature)
    },
    preKeys: preKeys.map(pk => ({
      keyId: pk.keyId,
      pubKey: arrayBufferToBase64(pk.keyPair.pubKey),
      privKey: arrayBufferToBase64(pk.keyPair.privKey)
    }))
  };
  const publicBundle = {
    registrationId,
    identityKey: payload.identityKeyPair.pubKey,
    signedPreKey: {
      keyId: payload.signedPreKey.keyId,
      pubKey: payload.signedPreKey.pubKey,
      signature: payload.signedPreKey.signature
    },
    preKeys: preKeys.map(pk => ({ keyId: pk.keyId, pubKey: arrayBufferToBase64(pk.keyPair.pubKey) }))
  };
  await AsyncStorage.setItem(STORAGE_KEYS.IDENTITY_KEY + '_' + uid, JSON.stringify({
    registrationId,
    identityKeyPair: {
      pubKey: payload.identityKeyPair.pubKey,
      privKey: payload.identityKeyPair.privKey
    },
    signedPreKey: payload.signedPreKey,
    preKeys: payload.preKeys
  }));
  return publicBundle;
}

export async function initSessionWithRemote(remoteUid: string, myUid: string) {
  const remote = await fetchRemotePrekeys(remoteUid);
  if (!remote) throw new Error('Remote prekeys not found');
  // TODO: Build prekey bundle and run signal.SessionBuilder to create session
}

export async function encryptFor(remoteUid: string, myUid: string, plaintext: string) {
  // Временная заглушка: base64 encode (заменить на sessionCipher.encrypt)
  const bin = unescape(encodeURIComponent(plaintext));
  let str = '';
  for (let i = 0; i < bin.length; i++) {
    str += String.fromCharCode(bin.charCodeAt(i));
  }
  return Buffer.from(str, 'binary').toString('base64');
}

export async function decryptFrom(senderUid: string, myUid: string, ciphertext: string) {
  // Заглушка inverse of above
  const bin = Buffer.from(ciphertext, 'base64').toString('binary');
  let out = '';
  for (let i = 0; i < bin.length; i++) out += String.fromCharCode(bin.charCodeAt(i));
  return decodeURIComponent(escape(out));
}

function arrayBufferToBase64(buf: ArrayBuffer) {
  const bytes = new Uint8Array(buf);
  let binary = '';
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return Buffer.from(binary, 'binary').toString('base64');
}
EOF

# navigation
cat > "${TARGET_DIR}/src/navigation/index.tsx" <<'EOF'
import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import LoginScreen from '../screens/LoginScreen';
import ChatListScreen from '../screens/ChatListScreen';
import ChatScreen from '../screens/ChatScreen';
import AdminScreen from '../screens/AdminScreen';

const Stack = createNativeStackNavigator();

export default function RootStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: true }}>
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="Chats" component={ChatListScreen} />
      <Stack.Screen name="Admin" component={AdminScreen} />
      <Stack.Screen name="Chat" component={ChatScreen} />
    </Stack.Navigator>
  );
}
EOF

# screens/LoginScreen.tsx
cat > "${TARGET_DIR}/src/screens/LoginScreen.tsx" <<'EOF'
import React, { useState } from 'react';
import { View, TextInput, Button, Text, Alert } from 'react-native';
import { signInEmail, signUpEmail, signOut } from '../services/auth';
import { generateAndStoreIdentity } from '../crypto/signalService';
import { uploadPrekeys, getUserDoc } from '../services/firestore';

export default function LoginScreen({ navigation }: any) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  async function handleSignUp() {
    const user = await signUpEmail(email, password, email.split('@')[0]);
    const publicBundle = await generateAndStoreIdentity(user.uid);
    await uploadPrekeys(user.uid, publicBundle);
    navigation.replace('Chats');
  }

  async function handleSignIn() {
    const user = await signInEmail(email, password);
    const doc = await getUserDoc(user.uid);
    if (doc && doc.isBlocked) {
      await signOut();
      Alert.alert('Доступ запрещён', 'Ваш аккаунт заблокирован администратором.');
      return;
    }
    navigation.replace('Chats');
  }

  return (
    <View style={{ padding: 16 }}>
      <Text>Email</Text>
      <TextInput value={email} onChangeText={setEmail} style={{ borderWidth: 1, marginBottom: 8 }} />
      <Text>Password</Text>
      <TextInput value={password} onChangeText={setPassword} secureTextEntry style={{ borderWidth: 1, marginBottom: 12 }} />
      <Button title="Sign in" onPress={handleSignIn} />
      <View style={{ height: 12 }} />
      <Button title="Sign up (create keys & upload prekeys)" onPress={handleSignUp} />
    </View>
  );
}
EOF

# screens/ChatListScreen.tsx
cat > "${TARGET_DIR}/src/screens/ChatListScreen.tsx" <<'EOF'
import React, { useEffect, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, Button } from 'react-native';
import auth from '@react-native-firebase/auth';
import { createConversationBetween } from '../services/firestore';

export default function ChatListScreen({ navigation }: any) {
  const [peers, setPeers] = useState<string[]>(['demo_user@example.com']);

  useEffect(() => {
  }, []);

  function openChatWith(peerEmail: string) {
    const myUid = auth().currentUser!.uid;
    const peerUid = peerEmail;
    const convId = createConversationBetween(myUid, peerUid);
    navigation.navigate('Chat', { conversationId: convId, peerUid });
  }

  function openAdmin() {
    navigation.navigate('Admin');
  }

  return (
    <View style={{ flex: 1, padding: 16 }}>
      <Button title="Admin" onPress={openAdmin} />
      <FlatList
        data={peers}
        keyExtractor={item => item}
        renderItem={({ item }) => (
          <TouchableOpacity onPress={() => openChatWith(item)} style={{ padding: 12, borderBottomWidth: 1 }}>
            <Text>{item}</Text>
          </TouchableOpacity>
        )}
      />
    </View>
  );
}
EOF

# screens/ChatScreen.tsx
cat > "${TARGET_DIR}/src/screens/ChatScreen.tsx" <<'EOF'
import React, { useEffect, useState } from 'react';
import { View, TextInput, Button, FlatList, Text, Alert } from 'react-native';
import { subscribeMessages, sendMessage, getUserDoc } from '../services/firestore';
import { encryptFor, decryptFrom } from '../crypto/signalService';
import auth from '@react-native-firebase/auth';

export default function ChatScreen({ route }: any) {
  const { conversationId, peerUid } = route.params;
  const [text, setText] = useState('');
  const [messages, setMessages] = useState<any[]>([]);
  const myUid = auth().currentUser!.uid;
  const [isBlocked, setIsBlocked] = useState(false);

  useEffect(() => {
    let mounted = true;
    (async () => {
      const doc = await getUserDoc(myUid);
      if (!mounted) return;
      setIsBlocked(!!(doc && doc.isBlocked));
    })();

    const unsub = subscribeMessages(conversationId, async (msgs) => {
      const out: any[] = [];
      for (const m of msgs) {
        try {
          const plaintext = await decryptFrom(m.senderId, myUid, m.ciphertext);
          out.push({ ...m, plaintext });
        } catch (e) {
          out.push({ ...m, plaintext: '[cannot decrypt]' });
        }
      }
      setMessages(out);
    });
    return () => { mounted = false; unsub && unsub(); };
  }, [conversationId]);

  async function handleSend() {
    if (isBlocked) {
      Alert.alert('Заблокировано', 'Вы заблокированы и не можете отправлять сообщения.');
      return;
    }
    const ciphertext = await encryptFor(peerUid, myUid, text);
    await sendMessage({
      conversationId,
      senderId: myUid,
      ciphertext
    } as any);
    setText('');
  }

  return (
    <View style={{ flex: 1, padding: 12 }}>
      {isBlocked && (
        <View style={{ padding: 8, backgroundColor: '#ffe6e6', marginBottom: 8 }}>
          <Text style={{ color: '#b00020' }}>Ваш аккаунт заблокирован — отправка сообщений отключена.</Text>
        </View>
      )}
      <FlatList
        data={messages}
        keyExtractor={(item) => item.id || Math.random().toString()}
        renderItem={({ item }) => (
          <View style={{ padding: 8, borderBottomWidth: 1 }}>
            <Text style={{ fontWeight: item.senderId === myUid ? '700' : '400' }}>{item.senderId}</Text>
            <Text>{item.plaintext}</Text>
          </View>
        )}
      />
      <View style={{ flexDirection: 'row', alignItems: 'center' }}>
        <TextInput value={text} onChangeText={setText} style={{ flex: 1, borderWidth: 1, padding: 8 }} />
        <Button title="Send" onPress={handleSend} />
      </View>
    </View>
  );
}
EOF

# screens/AdminScreen.tsx
cat > "${TARGET_DIR}/src/screens/AdminScreen.tsx" <<'EOF'
import React, { useEffect, useState } from 'react';
import { View, Text, FlatList, Button, Alert } from 'react-native';
import { listUsersForAdmin, blockUser, unblockUser } from '../services/firestore';
import auth from '@react-native-firebase/auth';

export default function AdminScreen({ navigation }: any) {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const myUid = auth().currentUser!.uid;

  useEffect(() => {
    fetchUsers();
  }, []);

  async function fetchUsers() {
    setLoading(true);
    try {
      const u = await listUsersForAdmin();
      setUsers(u);
    } catch (e) {
      Alert.alert('Ошибка', String(e));
    } finally {
      setLoading(false);
    }
  }

  async function handleBlock(user: any) {
    Alert.alert('Подтвердите', `Заблокировать ${user.email || user.uid}?`, [
      { text: 'Отмена', style: 'cancel' },
      {
        text: 'Блокировать',
        style: 'destructive',
        onPress: async () => {
          await blockUser(user.uid, myUid, 'blocked by admin');
          fetchUsers();
        }
      }
    ]);
  }

  async function handleUnblock(user: any) {
    await unblockUser(user.uid, myUid);
    fetchUsers();
  }

  return (
    <View style={{ flex: 1, padding: 12 }}>
      <Text style={{ fontSize: 18, fontWeight: '700', marginBottom: 12 }}>Admin — Users</Text>
      <FlatList
        data={users}
        keyExtractor={(item) => item.uid || item.email || Math.random().toString()}
        renderItem={({ item }) => (
          <View style={{ padding: 8, borderBottomWidth: 1, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
            <View>
              <Text style={{ fontWeight: '600' }}>{item.displayName || item.email || item.uid}</Text>
              <Text style={{ color: '#666' }}>{item.isBlocked ? `Blocked — by ${item.blockedBy}` : 'Active'}</Text>
            </View>
            <View style={{ flexDirection: 'row' }}>
              {item.isBlocked ? (
                <Button title="Unblock" onPress={() => handleUnblock(item)} />
              ) : (
                <Button title="Block" color="red" onPress={() => handleBlock(item)} />
              )}
            </View>
          </View>
        )}
      />
      <View style={{ height: 12 }} />
      <Button title="Refresh" onPress={fetchUsers} disabled={loading} />
    </View>
  );
}
EOF

# create helper index files (optional)
cat > "${TARGET_DIR}/src/index.d.ts" <<'EOF'
declare module 'libsignal-protocol-typescript';
EOF

# Finalize
echo "Инициализирую git и создаю zip ..."
pushd "${TARGET_DIR}" > /dev/null
git init >/dev/null 2>&1 || true
git add . >/dev/null 2>&1 || true
git commit -m "Initial scaffold: RN E2EE Messenger" >/dev/null 2>&1 || true
popd > /dev/null

zip -r "${TARGET_DIR}.zip" "${TARGET_DIR}" >/dev/null
echo "Готово: ./${TARGET_DIR} и ./${TARGET_DIR}.zip"
echo "Откройте README.md внутри папки для дальнейших инструкций."
EOF