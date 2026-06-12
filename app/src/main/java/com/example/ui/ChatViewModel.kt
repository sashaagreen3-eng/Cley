package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ChatRepository
    
    val contacts: StateFlow<List<ContactEntity>>
    val userSettings: StateFlow<UserSettingsEntity?>
    val interUserMessages: StateFlow<List<InterUserMessageEntity>>
    
    private val _selectedContactId = MutableStateFlow<Long?>(null)
    val selectedContactId: StateFlow<Long?> = _selectedContactId

    private val _activeMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<MessageEntity>> = _activeMessages

    private val _idleCountdown = MutableStateFlow(15)
    val idleCountdown: StateFlow<Int> = _idleCountdown

    private val _typingStatus = MutableStateFlow<Map<Long, String>>(emptyMap())
    val typingStatus: StateFlow<Map<Long, String>> = _typingStatus

    private var idleJob: Job? = null
    private var messagesObservationJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChatRepository(database.chatDao())
        
        contacts = repository.contacts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userSettings = repository.userSettings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        interUserMessages = repository.interUserMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            val currentContacts = database.chatDao().getAllContactsDirect()
            if (currentContacts.isEmpty()) {
                val demoContacts = listOf(
                    ContactEntity(1L, "Алиса", "ONLINE", "Общаюсь на Compose 🚀", 0xFFE91E63.toInt(), "Привет! Как дела?", System.currentTimeMillis() - 600000),
                    ContactEntity(2L, "Боб", "AWAY", "Отошел попить кофе ☕", 0xFF2196F3.toInt(), "Я скоро вернусь", System.currentTimeMillis() - 300000),
                    ContactEntity(3L, "Чарли", "DND", "На важном созвоне 🤫", 0xFF4CAF50.toInt(), "Пожалуйста, не беспокойте", System.currentTimeMillis() - 1500000),
                    ContactEntity(4L, "Диана (ИИ)", "OFFLINE", "Спит 💤", 0xFF9C27B0.toInt(), "Для связи разбудите меня", System.currentTimeMillis() - 7200000)
                )
                repository.insertContacts(demoContacts)

                val defaultSettings = UserSettingsEntity(
                    id = 1,
                    name = "Разработчик",
                    status = "ONLINE",
                    customStatus = null,
                    isManual = false,
                    lastActiveTimestamp = System.currentTimeMillis(),
                    isAdmin = false,
                    isBlocked = false,
                    canWriteFirst = true,
                    mediaSendingRestricted = false,
                    textSendingRestricted = false
                )
                repository.insertUserSettings(defaultSettings)

                // Seed Inter-user messages between other contacts for administrator inspection
                val now = System.currentTimeMillis()
                repository.insertInterUserMessage(InterUserMessageEntity(senderName = "Алиса", receiverName = "Боб", text = "Привет Боб! Ты идешь сегодня на встречу по Compose?", timestamp = now - 7200000))
                repository.insertInterUserMessage(InterUserMessageEntity(senderName = "Боб", receiverName = "Алиса", text = "Привет Алиса! Да, буду через 15 минут. Подготовил демо-презентацию.", timestamp = now - 6900000))
                repository.insertInterUserMessage(InterUserMessageEntity(senderName = "Алиса", receiverName = "Боб", text = "Супер! Жду в переговорке.", timestamp = now - 6600000))
                
                repository.insertInterUserMessage(InterUserMessageEntity(senderName = "Боб", receiverName = "Чарли", text = "Чарли, привет! Ты получил техническое задание для мессенджера?", timestamp = now - 5400000))
                repository.insertInterUserMessage(InterUserMessageEntity(senderName = "Чарли", receiverName = "Боб", text = "Привет Боб! Да, изучаю. Архитектура на Room и StateFlow выглядит очень надежно.", timestamp = now - 5100000))
                
                repository.insertInterUserMessage(InterUserMessageEntity(senderName = "Чарли", receiverName = "Алиса", text = "Алиса, мы утвердили дизайн-систему апликации, выглядит круто!", timestamp = now - 3600000))
                repository.insertInterUserMessage(InterUserMessageEntity(senderName = "Алиса", receiverName = "Чарли", text = "Действительно превосходно! Frosted Glass стиль идеально подходит.", timestamp = now - 3300000))

                val lastTimestamp = System.currentTimeMillis() - 600000
                database.chatDao().insertMessage(MessageEntity(contactId = 1L, sender = "contact", text = "Привет! Как дела?", timestamp = lastTimestamp))
                database.chatDao().insertMessage(MessageEntity(contactId = 2L, sender = "contact", text = "Я скоро вернусь", timestamp = lastTimestamp + 300000))
                database.chatDao().insertMessage(MessageEntity(contactId = 3L, sender = "contact", text = "Пожалуйста, не беспокойте", timestamp = lastTimestamp - 900000))
            }
            
            startIdleTimerTracker()
        }
    }

    fun selectContact(contactId: Long?) {
        _selectedContactId.value = contactId
        messagesObservationJob?.cancel()
        
        if (contactId != null) {
            messagesObservationJob = viewModelScope.launch {
                repository.getMessages(contactId).collect { list ->
                    _activeMessages.value = list
                }
            }
        } else {
            _activeMessages.value = emptyList()
        }
    }

    fun sendMessage(text: String) {
        val currentContactId = _selectedContactId.value ?: return
        viewModelScope.launch {
            val appCtx = getApplication<Application>()
            val db = AppDatabase.getDatabase(appCtx)
            val settings = repository.getUserSettingsDirect()
            
            if (settings != null) {
                if (settings.isBlocked) {
                    return@launch
                }
                if (settings.textSendingRestricted) {
                    val systemWarning = MessageEntity(
                        contactId = currentContactId,
                        sender = "contact",
                        text = "⚠️ Ошибка: Отправка текстовых сообщений заблокирована администратором.",
                        timestamp = System.currentTimeMillis()
                    )
                    db.chatDao().insertMessage(systemWarning)
                    return@launch
                }
                if (!settings.canWriteFirst) {
                    val actualHistory = _activeMessages.value.any { !it.text.startsWith("⚠️ Ошибка") }
                    if (!actualHistory) {
                        val systemWarning = MessageEntity(
                            contactId = currentContactId,
                            sender = "contact",
                            text = "⚠️ Ошибка: Администратор ограничил вам возможность писать первым.",
                            timestamp = System.currentTimeMillis()
                        )
                        db.chatDao().insertMessage(systemWarning)
                        return@launch
                    }
                }
            }

            val userMsg = MessageEntity(
                contactId = currentContactId,
                sender = "me",
                text = text,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(userMsg)

            simulateContactResponse(currentContactId, text)
        }
    }

    private suspend fun simulateContactResponse(contactId: Long, userMessage: String) {
        val contactObj = repository.getContact(contactId) ?: return

        when (contactObj.status.uppercase()) {
            "DND" -> {
                delay(800)
                val autoReply = MessageEntity(
                    contactId = contactId,
                    sender = "contact",
                    text = "[Автоответ]: Извините, мой режим 'Не беспокоить' активен. Отвечу позже, когда освобожусь. Текст получен: \"$userMessage\"",
                    timestamp = System.currentTimeMillis()
                )
                repository.insertMessage(autoReply)
            }
            "AWAY" -> {
                delay(1200)
                setTyping(contactId, "${contactObj.name} печатает...")
                delay(3000)
                clearTyping(contactId)

                val replyText = "Привет! Я сейчас отошел («Отошел»), но увидел твое сообщение: \"$userMessage\". Как только вернусь к телефону, отвечу полностью!"
                val autoReply = MessageEntity(
                    contactId = contactId,
                    sender = "contact",
                    text = replyText,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertMessage(autoReply)
            }
            "ONLINE" -> {
                delay(500)
                setTyping(contactId, "${contactObj.name} печатает...")
                delay(2000)
                clearTyping(contactId)

                var textResponse: String
                var attachedFile: Triple<String, String, String>? = null // Type, Name, Size

                val queryLower = userMessage.lowercase()
                if (queryLower.contains("фото") || queryLower.contains("картинка")) {
                    textResponse = "Вот, держи классную фотографию с нашего недавнего Compose-митапа! 📸"
                    attachedFile = Triple("IMAGE", "compose_meetup_2026.png", "1.2 MB")
                } else if (queryLower.contains("аудио") || queryLower.contains("песн") || queryLower.contains("голосовой")) {
                    textResponse = "Записал голосовое сообщение или трек для тебя! Оцени качество звука. 🎵"
                    attachedFile = Triple("AUDIO", "VOICE_REPLY.ogg", "450 KB")
                } else if (queryLower.contains("видео") || queryLower.contains("клип")) {
                    textResponse = "Смотри видеоотчет по новой архитектуре Jetpack Compose! 🎥"
                    attachedFile = Triple("VIDEO", "compose_deep_dive.mp4", "12.8 MB")
                } else if (queryLower.contains("документ") || queryLower.contains("файл")) {
                    textResponse = "Привет! Высылаю тебе технический документ со спецификациями нашего проекта. 📄"
                    attachedFile = Triple("DOCUMENT", "TECHNICAL_SPEC.pdf", "850 KB")
                } else {
                    textResponse = if (contactId == 4L) {
                        val aiResponse = GeminiService.generateResponse(
                            "Ты - Диана, умный ИИ ассистент в мессенджере. Твой статус - В сети. Ответь кратко на сообщение пользователя на русском языке: \"$userMessage\""
                        )
                        aiResponse.ifEmpty {
                            "Привет! Я умная ИИ-собеседница Диана. Твой вопрос получен: \"$userMessage\". Gemini API сейчас работает в симулированном режиме, но я всегда рада поболтать!"
                        }
                    } else {
                        genericResponseFor(contactObj.name, userMessage)
                    }
                }

                val reply = MessageEntity(
                    contactId = contactId,
                    sender = "contact",
                    text = textResponse,
                    timestamp = System.currentTimeMillis(),
                    fileType = attachedFile?.first,
                    fileName = attachedFile?.second,
                    fileSize = attachedFile?.third,
                    downloadProgress = if (attachedFile != null) 0 else 100,
                    isDownloading = false
                )
                repository.insertMessage(reply)
            }
            "OFFLINE" -> {
            }
        }
    }

    private fun genericResponseFor(name: String, query: String): String {
        return when (name) {
            "Алиса" -> {
                val thoughts = listOf(
                    "Классная мысль! Рада, что мы обсуждаем это.",
                    "Да, полностью с тобой согласна! 😄",
                    "Хм, надо подумать. А что думаешь ты?",
                    "Интересно! Расскажи подробнее про свой проект на Jetpack Compose."
                )
                thoughts.random()
            }
            else -> "Сообщение доставлено. Спасибо статус-сервису!"
        }
    }

    private fun setTyping(contactId: Long, text: String) {
        val current = _typingStatus.value.toMutableMap()
        current[contactId] = text
        _typingStatus.value = current
    }

    private fun clearTyping(contactId: Long) {
        val current = _typingStatus.value.toMutableMap()
        current.remove(contactId)
        _typingStatus.value = current
    }

    fun changeUserStatus(status: String, customText: String?, isManual: Boolean) {
        viewModelScope.launch {
            val currentSettings = repository.getUserSettingsDirect() ?: return@launch
            val updated = currentSettings.copy(
                status = status,
                customStatus = customText,
                isManual = isManual,
                lastActiveTimestamp = System.currentTimeMillis()
            )
            repository.insertUserSettings(updated)

            if (isManual) {
                _idleCountdown.value = -1
            } else {
                _idleCountdown.value = 15
            }
        }
    }

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            val currentSettings = repository.getUserSettingsDirect() ?: return@launch
            val updated = currentSettings.copy(name = newName)
            repository.insertUserSettings(updated)
        }
    }

    fun wakeUpDiana() {
        viewModelScope.launch {
            repository.updateContactStatus(4L, "ONLINE", "Готова к общению ✨")
            val infoMsg = MessageEntity(
                contactId = 4L,
                sender = "contact",
                text = "Вы разбудили Диану! 🌟 Теперь она онлайн и готова отвечать с помощью Gemini AI.",
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(infoMsg)
        }
    }

    fun resetIdleTimer() {
        viewModelScope.launch {
            val currentSettings = repository.getUserSettingsDirect() ?: return@launch
            if (!currentSettings.isManual) {
                _idleCountdown.value = 15
                if (currentSettings.status == "AWAY") {
                    val updated = currentSettings.copy(
                        status = "ONLINE",
                        lastActiveTimestamp = System.currentTimeMillis()
                    )
                    repository.insertUserSettings(updated)
                }
            }
        }
    }

    private fun startIdleTimerTracker() {
        idleJob?.cancel()
        idleJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentSettings = repository.getUserSettingsDirect()
                if (currentSettings != null && !currentSettings.isManual) {
                    val currentTimer = _idleCountdown.value
                    if (currentTimer > 0) {
                        _idleCountdown.value = currentTimer - 1
                    } else if (currentTimer == 0) {
                        val updated = currentSettings.copy(
                            status = "AWAY",
                            customStatus = "Нет активности 😴"
                        )
                        repository.insertUserSettings(updated)
                        _idleCountdown.value = 0
                    }
                }
            }
        }
    }

    fun clearChat(contactId: Long) {
        viewModelScope.launch {
            repository.clearChat(contactId)
        }
    }

    // --- INTEGRATED FILE SHARING (IMAGES, VIDEO, AUDIO, DOCS) FLOW ---
    fun sendFileMessage(fileType: String, fileName: String, fileSize: String) {
        val currentContactId = _selectedContactId.value ?: return
        viewModelScope.launch {
            val appCtx = getApplication<Application>()
            val db = AppDatabase.getDatabase(appCtx)
            val settings = repository.getUserSettingsDirect()

            if (settings != null) {
                if (settings.isBlocked) return@launch
                if (settings.mediaSendingRestricted) {
                    val systemWarning = MessageEntity(
                        contactId = currentContactId,
                        sender = "contact",
                        text = "⚠️ Ошибка: Отправка медиафайлов ограничена администратором.",
                        timestamp = System.currentTimeMillis()
                    )
                    db.chatDao().insertMessage(systemWarning)
                    return@launch
                }
                if (!settings.canWriteFirst) {
                    val actualHistory = _activeMessages.value.any { !it.text.startsWith("⚠️ Ошибка") }
                    if (!actualHistory) {
                        val systemWarning = MessageEntity(
                            contactId = currentContactId,
                            sender = "contact",
                            text = "⚠️ Ошибка: Администратор ограничил вам возможность писать первым.",
                            timestamp = System.currentTimeMillis()
                        )
                        db.chatDao().insertMessage(systemWarning)
                        return@launch
                    }
                }
            }

            val timestamp = System.currentTimeMillis()
            var fileMsg = MessageEntity(
                contactId = currentContactId,
                sender = "me",
                text = "Поделился файлом: $fileName",
                timestamp = timestamp,
                fileUri = "simulated_uri",
                fileType = fileType,
                fileName = fileName,
                fileSize = fileSize,
                uploadProgress = 0,
                isUploading = true
            )

            // Save start record and initiate incremental simulated upload progress
            val insertedId = repository.insertMessage(fileMsg)

            for (p in 10..100 step 10) {
                delay(120)
                val updatedMsg = fileMsg.copy(
                    id = insertedId,
                    uploadProgress = p,
                    isUploading = p < 100
                )
                db.chatDao().insertMessage(updatedMsg)
            }

            // Trigger simulated contact reply based on this file upload!
            simulateContactResponse(currentContactId, "отправил файл $fileName")
        }
    }

    fun downloadFile(msgId: Long) {
        viewModelScope.launch {
            val appCtx = getApplication<Application>()
            val db = AppDatabase.getDatabase(appCtx)

            val msg = _activeMessages.value.find { it.id == msgId } ?: return@launch
            
            // Simulating incremental download states
            for (p in 0..100 step 10) {
                val updatedMsg = msg.copy(
                    isDownloading = p < 100,
                    downloadProgress = p
                )
                db.chatDao().insertMessage(updatedMsg)
                delay(100)
            }
        }
    }

    // --- INTEGRATED ADMINISTRATOR SYSTEM CONTROLS ---
    fun toggleAdminMode(enabled: Boolean) {
        viewModelScope.launch {
            val settings = repository.getUserSettingsDirect() ?: return@launch
            val updated = settings.copy(isAdmin = enabled)
            repository.insertUserSettings(updated)
        }
    }

    fun updateBlockStatus(blocked: Boolean) {
        viewModelScope.launch {
            val settings = repository.getUserSettingsDirect() ?: return@launch
            val updated = settings.copy(isBlocked = blocked)
            repository.insertUserSettings(updated)
        }
    }

    fun updateCanWriteFirst(canWriteFirst: Boolean) {
        viewModelScope.launch {
            val settings = repository.getUserSettingsDirect() ?: return@launch
            val updated = settings.copy(canWriteFirst = canWriteFirst)
            repository.insertUserSettings(updated)
        }
    }

    fun updateMediaRestriction(mediaRestricted: Boolean) {
        viewModelScope.launch {
            val settings = repository.getUserSettingsDirect() ?: return@launch
            val updated = settings.copy(mediaSendingRestricted = mediaRestricted)
            repository.insertUserSettings(updated)
        }
    }

    fun updateTextRestriction(textRestricted: Boolean) {
        viewModelScope.launch {
            val settings = repository.getUserSettingsDirect() ?: return@launch
            val updated = settings.copy(textSendingRestricted = textRestricted)
            repository.insertUserSettings(updated)
        }
    }

    override fun onCleared() {
        super.onCleared()
        idleJob?.cancel()
        messagesObservationJob?.cancel()
    }
}
