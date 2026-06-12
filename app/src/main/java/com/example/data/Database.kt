package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val status: String, // ONLINE, AWAY, DND, OFFLINE
    val customStatus: String?,
    val avatarColor: Int,
    val lastMessage: String?,
    val lastMessageTimestamp: Long
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long,
    val sender: String, // "me" or "contact"
    val text: String,
    val timestamp: Long,
    
    // File shares
    val fileUri: String? = null,
    val fileType: String? = null, // "IMAGE", "VIDEO", "AUDIO", "DOCUMENT"
    val fileName: String? = null,
    val fileSize: String? = null,
    val uploadProgress: Int = 100, // 0..100
    val downloadProgress: Int = 100, // 0..100
    val isDownloading: Boolean = false,
    val isUploading: Boolean = false
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val status: String, // ONLINE, AWAY, DND, OFFLINE
    val customStatus: String?,
    val isManual: Boolean, // manual override prevents auto away
    val lastActiveTimestamp: Long,
    
    // Administrator policy checks
    val isAdmin: Boolean = false,
    val isBlocked: Boolean = false,
    val canWriteFirst: Boolean = true,
    val mediaSendingRestricted: Boolean = false,
    val textSendingRestricted: Boolean = false
)

@Entity(tableName = "inter_user_messages")
data class InterUserMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val receiverName: String,
    val text: String,
    val timestamp: Long
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val email: String,
    val username: String,
    val passwordHash: String,
    val isLoggedIn: Boolean = false
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM contacts ORDER BY lastMessageTimestamp DESC")
    fun getAllContactsFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts")
    suspend fun getAllContactsDirect(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactDirect(id: Long): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("UPDATE contacts SET status = :status, customStatus = :customStatus WHERE id = :id")
    suspend fun updateContactStatus(id: Long, status: String, customStatus: String?)

    @Query("UPDATE contacts SET lastMessage = :message, lastMessageTimestamp = :timestamp WHERE id = :id")
    suspend fun updateContactLastMessage(id: Long, message: String, timestamp: Long)

    @Query("SELECT * FROM messages WHERE contactId = :contactId ORDER BY timestamp ASC")
    fun getMessagesFlow(contactId: Long): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("DELETE FROM messages WHERE contactId = :contactId")
    suspend fun deleteMessagesByContact(contactId: Long)

    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettingsFlow(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getUserSettingsDirect(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettingsEntity)

    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountDirect(email: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInAccountFlow(): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInAccountDirect(): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Query("UPDATE accounts SET isLoggedIn = 0")
    suspend fun logoutAllAccounts()

    // Inter-user message queries for administrator view
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterUserMessage(msg: InterUserMessageEntity)

    @Query("SELECT * FROM inter_user_messages ORDER BY timestamp ASC")
    fun getAllInterUserMessagesFlow(): Flow<List<InterUserMessageEntity>>
}

@Database(entities = [ContactEntity::class, MessageEntity::class, UserSettingsEntity::class, AccountEntity::class, InterUserMessageEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "presence_chat_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ChatRepository(private val chatDao: ChatDao) {
    val contacts = chatDao.getAllContactsFlow()
    val userSettings = chatDao.getUserSettingsFlow()

    fun getMessages(contactId: Long): Flow<List<MessageEntity>> = chatDao.getMessagesFlow(contactId)

    suspend fun insertMessage(message: MessageEntity): Long {
        val id = chatDao.insertMessage(message)
        chatDao.updateContactLastMessage(message.contactId, message.text, message.timestamp)
        return id
    }

    suspend fun updateContactStatus(id: Long, status: String, customStatus: String?) {
        chatDao.updateContactStatus(id, status, customStatus)
    }

    suspend fun insertContacts(contacts: List<ContactEntity>) {
        chatDao.insertContacts(contacts)
    }

    suspend fun getContact(id: Long) = chatDao.getContactDirect(id)

    suspend fun clearChat(contactId: Long) {
        chatDao.deleteMessagesByContact(contactId)
        chatDao.updateContactLastMessage(contactId, "", System.currentTimeMillis())
    }

    suspend fun getUserSettingsDirect() = chatDao.getUserSettingsDirect()

    suspend fun insertUserSettings(settings: UserSettingsEntity) {
        chatDao.insertUserSettings(settings)
    }

    val loggedInAccountFlow = chatDao.getLoggedInAccountFlow()

    suspend fun getAccountDirect(email: String) = chatDao.getAccountDirect(email)
    suspend fun getLoggedInAccountDirect() = chatDao.getLoggedInAccountDirect()
    suspend fun insertAccount(account: AccountEntity) {
        chatDao.insertAccount(account)
    }
    suspend fun logoutAllAccounts() {
        chatDao.logoutAllAccounts()
    }

    val interUserMessages = chatDao.getAllInterUserMessagesFlow()
    suspend fun insertInterUserMessage(msg: InterUserMessageEntity) {
        chatDao.insertInterUserMessage(msg)
    }
}
