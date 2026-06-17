package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ContactEntity
import com.example.data.MessageEntity
import com.example.data.UserSettingsEntity
import com.example.data.InterUserMessageEntity
import com.example.ui.ChatViewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .meshBackground()
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->
                        val chatViewModel: ChatViewModel = viewModel()
                        MessengerApp(
                            viewModel = chatViewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

// Glassmorphism and Mesh Background Modifiers
fun Modifier.meshBackground(): Modifier = this.drawBehind {
    val width = size.width
    val height = size.height
    // Base solid background
    drawRect(color = Color(0xFFF0F2F8))
    
    // Top-left radial gradient (rgba(186, 204, 255, 0.4) -> transparent)
    val topLeftRadius = kotlin.math.max(width, height) * 0.82f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x66BACCFF), Color.Transparent),
            center = androidx.compose.ui.geometry.Offset(0f, 0f),
            radius = topLeftRadius
        ),
        radius = topLeftRadius,
        center = androidx.compose.ui.geometry.Offset(0f, 0f)
    )

    // Bottom-right radial gradient (rgba(220, 186, 255, 0.3) -> transparent)
    val bottomRightRadius = kotlin.math.max(width, height) * 0.82f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x4DDCBAFF), Color.Transparent),
            center = androidx.compose.ui.geometry.Offset(width, height),
            radius = bottomRightRadius
        ),
        radius = bottomRightRadius,
        center = androidx.compose.ui.geometry.Offset(width, height)
    )
}

fun Modifier.glassPanel(shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp)): Modifier = this
    .background(Color(0xB3FFFFFF), shape)
    .border(width = 1.dp, color = Color(0x66FFFFFF), shape = shape)

// Global Sealed Class representing the sliding local screens
sealed class AppScreen {
    object List : AppScreen()
    data class Detail(val contactId: Long) : AppScreen()
}

@Composable
fun MessengerApp(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val selectedContactId by viewModel.selectedContactId.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val idleCountdown by viewModel.idleCountdown.collectAsStateWithLifecycle()
    val typingStatus by viewModel.typingStatus.collectAsStateWithLifecycle()

    var mobileScreenState by remember { mutableStateOf<AppScreen>(AppScreen.List) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showAdminPanel by remember { mutableStateOf(false) }
    val interUserMessages by viewModel.interUserMessages.collectAsStateWithLifecycle()

    // Synchronize selected screen with selection changes
    LaunchedEffect(selectedContactId) {
        mobileScreenState = if (selectedContactId != null) {
            AppScreen.Detail(selectedContactId!!)
        } else {
            AppScreen.List
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 720.dp

        if (isTablet) {
            // TABLET / LANDSCAPE LAYOUT: Side-by-Side Split Screen
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Column: Chat List (fixed width)
                Column(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                        .background(Color(0xD9F0F2F8)) // smooth translucent white frosted overlay
                        .border(
                            width = 1.dp,
                            color = Color(0x33FFFFFF)
                        )
                ) {
                    ChatListHeader(
                        settings = userSettings,
                        idleCountdown = idleCountdown,
                        onEditStatusClick = { showStatusDialog = true },
                        onEditNameClick = { showEditNameDialog = true },
                        onToggleAdmin = { viewModel.toggleAdminMode(!(userSettings?.isAdmin ?: false)) },
                        onOpenAdminPanel = { showAdminPanel = true }
                    )
                    
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    ChatContactsList(
                        contacts = contacts,
                        selectedId = selectedContactId,
                        typingStatus = typingStatus,
                        onContactSelect = { id -> viewModel.selectContact(id) }
                    )
                }

                // Right Column: Chat History Details
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (selectedContactId != null) {
                        val activeContact = contacts.find { it.id == selectedContactId }
                        ChatDetailView(
                            contact = activeContact,
                            messages = activeMessages,
                            typingText = typingStatus[selectedContactId],
                            onSendMessage = { text -> viewModel.sendMessage(text) },
                            onBackClick = { viewModel.selectContact(null) },
                            onClearHistory = { viewModel.clearChat(selectedContactId!!) },
                            onWakeDiana = { viewModel.wakeUpDiana() },
                            isTablet = true,
                            onActivityTrigger = { viewModel.resetIdleTimer() }
                        )
                    } else {
                        // Empty Chat detail state
                        EmptyChatDetailState(userSettings = userSettings)
                    }
                }
            }
        } else {
            // MOBILE / PORTRAIT LAYOUT: Navigation Screen Slider
            Box(modifier = Modifier.fillMaxSize()) {
                when (val screen = mobileScreenState) {
                    is AppScreen.List -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            ChatListHeader(
                                settings = userSettings,
                                idleCountdown = idleCountdown,
                                onEditStatusClick = { showStatusDialog = true },
                                onEditNameClick = { showEditNameDialog = true },
                                onToggleAdmin = { viewModel.toggleAdminMode(!(userSettings?.isAdmin ?: false)) },
                                onOpenAdminPanel = { showAdminPanel = true }
                            )

                            Divider(color = Color(0x1A000000))

                            ChatContactsList(
                                contacts = contacts,
                                selectedId = selectedContactId,
                                typingStatus = typingStatus,
                                onContactSelect = { id -> viewModel.selectContact(id) }
                            )
                        }
                    }
                    is AppScreen.Detail -> {
                        val activeContact = contacts.find { it.id == screen.contactId }
                        
                        BackHandler {
                            viewModel.selectContact(null)
                        }

                        ChatDetailView(
                            contact = activeContact,
                            messages = activeMessages,
                            typingText = typingStatus[screen.contactId],
                            onSendMessage = { text -> viewModel.sendMessage(text) },
                            onBackClick = { viewModel.selectContact(null) },
                            onClearHistory = { viewModel.clearChat(screen.contactId) },
                            onWakeDiana = { viewModel.wakeUpDiana() },
                            isTablet = false,
                            onActivityTrigger = { viewModel.resetIdleTimer() }
                        )
                    }
                }
            }
        }

        // Popup Dialog for manual Status Selection
        if (showStatusDialog && userSettings != null) {
            StatusSelectorDialog(
                currentSettings = userSettings!!,
                onDismiss = { showStatusDialog = false },
                onStatusSelected = { status, customText, isManual ->
                    viewModel.changeUserStatus(status, customText, isManual)
                    showStatusDialog = false
                }
            )
        }

        // Popup Dialog for Profile Name edit
        if (showEditNameDialog && userSettings != null) {
            EditNameDialog(
                currentName = userSettings!!.name,
                onDismiss = { showEditNameDialog = false },
                onSave = { newName ->
                    viewModel.updateUserName(newName)
                    showEditNameDialog = false
                }
            )
        }

        // Popup Dialog for Admin Panel
        if (showAdminPanel && userSettings != null) {
            AdminPanelDialog(
                settings = userSettings!!,
                interUserMessages = interUserMessages,
                onDismiss = { showAdminPanel = false },
                onToggleBlock = { viewModel.updateBlockStatus(it) },
                onToggleCanWriteFirst = { viewModel.updateCanWriteFirst(it) },
                onToggleMediaRestricted = { viewModel.updateMediaRestriction(it) },
                onToggleTextRestricted = { viewModel.updateTextRestriction(it) }
            )
        }
    }
}

// --- COMPOSE UI SUBCOMPONENTS ---

@Composable
fun ChatListHeader(
    settings: UserSettingsEntity?,
    idleCountdown: Int,
    onEditStatusClick: () -> Unit,
    onEditNameClick: () -> Unit,
    onToggleAdmin: () -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .glassPanel(RoundedCornerShape(24.dp))
            .testTag("app_header_surface")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Мессенджер",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                // Timer or status indication banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x26000000))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Таймер простоя",
                            tint = if (idleCountdown > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                idleCountdown > 0 -> "Авто-отход: ${idleCountdown}с"
                                idleCountdown == 0 -> "Режим: Отошел"
                                else -> "Ручной статус"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Self Profile Box
            if (settings != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassPanel(RoundedCornerShape(20.dp))
                        .background(Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar with dynamic status indicator ring
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clickable { onEditStatusClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            // Spinning/pulsating active ring if online
                            val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
                            val scaleFactor by infiniteTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.05f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .scale(if (settings.status == "ONLINE") scaleFactor else 1.0f)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                getStatusColor(settings.status).copy(alpha = 0.25f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = settings.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }

                            // Distinct Status cutout dot overlay
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(getStatusColor(settings.status))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // User profile information
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onEditStatusClick() }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = settings.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Изменить имя",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onEditNameClick() }
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = getStatusTextRu(settings.status),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = getStatusColor(settings.status)
                                )
                                if (!settings.customStatus.isNullOrEmpty()) {
                                    Text(
                                        text = " • ${settings.customStatus}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Options / edit buttons
                        IconButton(
                            onClick = { onEditStatusClick() },
                            modifier = Modifier.testTag("status_edit_trigger")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Настроить статус",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Режим администратора",
                            tint = if (settings.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Режим администратора",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = settings.isAdmin,
                        onCheckedChange = { onToggleAdmin() },
                        modifier = Modifier
                            .scale(0.85f)
                            .testTag("admin_mode_toggle")
                    )
                }

                if (settings.isAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onOpenAdminPanel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_admin_panel_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Открыть админ-панель",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatContactsList(
    contacts: List<ContactEntity>,
    selectedId: Long?,
    typingStatus: Map<Long, String>,
    onContactSelect: (Long) -> Unit
) {
    if (contacts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("contacts_list")
        ) {
            item {
                Text(
                    text = "ДИАЛОГИ",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            items(contacts, key = { it.id }) { contact ->
                val isSelected = contact.id == selectedId
                val typingText = typingStatus[contact.id]

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassPanel(RoundedCornerShape(20.dp))
                            .background(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                } else {
                                    Color(0x13FFFFFF)
                                },
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onContactSelect(contact.id) }
                            .padding(14.dp)
                            .testTag("contact_item_${contact.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    // Contact Avatar with Custom Presence Indicator
                    Box(modifier = Modifier.size(46.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(contact.avatarColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                        }

                        // Distinct Status Outlined Dot matching contact's current presence state
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(getStatusColor(contact.status))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Name, Details and Status Messages
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            // Formatted Last Active/Message time
                            Text(
                                text = formatTime(contact.lastMessageTimestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Typing text or custom status text
                        if (typingText != null) {
                            Text(
                                text = typingText,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Specific tag for DND
                                if (contact.status == "DND") {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsOff,
                                        contentDescription = "Не беспокоить",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .padding(end = 2.dp)
                                    )
                                }
                                
                                val statusBubbleText = if (!contact.customStatus.isNullOrEmpty()) {
                                    contact.customStatus
                                } else {
                                    getStatusTextRu(contact.status)
                                }
                                
                                Text(
                                    text = statusBubbleText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (contact.status == "ONLINE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Display snippet of the last message
                        if (!contact.lastMessage.isNullOrEmpty()) {
                            Text(
                                text = contact.lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailView(
    contact: ContactEntity?,
    messages: List<MessageEntity>,
    typingText: String?,
    onSendMessage: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearHistory: () -> Unit,
    onWakeDiana: () -> Unit,
    isTablet: Boolean,
    onActivityTrigger: () -> Unit
) {
    if (contact == null) return

    val focusManager = LocalFocusManager.current
    var inputMessageText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    // Slide down to the last message automatically on changes
    LaunchedEffect(messages.size, typingText) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onActivityTrigger() }
                    ) {
                        // Contact Image
                        Box(modifier = Modifier.size(36.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color(contact.avatarColor)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(1.5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(getStatusColor(contact.status))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val secondaryText = if (typingText != null) {
                                    typingText
                                } else if (!contact.customStatus.isNullOrEmpty()) {
                                    contact.customStatus
                                } else {
                                    getStatusTextRu(contact.status)
                                }

                                Text(
                                    text = secondaryText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (typingText != null) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        getStatusColor(contact.status)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (!isTablet) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад к контактам"
                            )
                        }
                    }
                },
                actions = {
                    // Manual trigger to wake up Diana if offline
                    if (contact.id == 4L && contact.status == "OFFLINE") {
                        OutlinedButton(
                            onClick = onWakeDiana,
                            contentPadding = ButtonDefaults.TextButtonContentPadding,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("wake_up_diana_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Разбудить",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Разбудить", fontSize = 11.sp)
                        }
                    }

                    // Reset Chat Messages helper
                    TextButton(onClick = onClearHistory) {
                        Text("Очистить", color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0x66FFFFFF)
                )
            )
        },
        bottomBar = {
            // Typing Input Row Bar
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassPanel(RoundedCornerShape(32.dp))
                        .background(Color(0x99FFFFFF), RoundedCornerShape(32.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .pointerInput(Unit) {
                            // Reset inactivity timer on any interaction
                            detectTapGestures(onTap = { onActivityTrigger() })
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputMessageText,
                        onValueChange = {
                            inputMessageText = it
                            onActivityTrigger()
                        },
                        placeholder = { Text("Введите сообщение...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = false,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputMessageText.trim().isNotEmpty()) {
                                    onSendMessage(inputMessageText)
                                    inputMessageText = ""
                                    onActivityTrigger()
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputMessageText.trim().isNotEmpty()) {
                                onSendMessage(inputMessageText)
                                inputMessageText = ""
                                onActivityTrigger()
                            }
                        },
                        enabled = inputMessageText.trim().isNotEmpty(),
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (inputMessageText.trim().isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color(0x13000000)
                                },
                                shape = CircleShape
                            )
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Отправить",
                            tint = if (inputMessageText.trim().isNotEmpty()) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Chat History List Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .meshBackground()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        onActivityTrigger()
                    })
                }
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Чат пуст",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Здесь пусто. Начните общение!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = WindowInsets.safeDrawing.asPaddingValues()
                ) {
                    items(messages) { message ->
                        val isCurrentUser = message.sender == "me"
                        
                        // Row aligning layout either Left (contact) or Right (me)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                        ) {
                             Surface(
                                 shape = if (isCurrentUser) {
                                     RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
                                 } else {
                                     RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
                                 },
                                 color = if (isCurrentUser) {
                                     MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                 } else {
                                     Color.Transparent
                                 },
                                 modifier = Modifier
                                     .widthIn(max = 280.dp)
                                     .then(
                                         if (isCurrentUser) {
                                             Modifier.border(
                                                 width = 1.dp,
                                                 color = Color(0x33FFFFFF),
                                                 shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
                                             )
                                         } else {
                                             Modifier.glassPanel(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp))
                                         }
                                     )
                             ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        text = message.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isCurrentUser) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = formatTime(message.timestamp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = if (isCurrentUser) {
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }

                    // Virtual item to buffer typing indicator spacing
                    if (typingText != null) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.FiberManualRecord,
                                            contentDescription = "печатает",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(10.dp)
                                                .scale(0.81f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = typingText,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatDetailState(
    userSettings: UserSettingsEntity?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubble,
                contentDescription = "Мессенджер",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Выберите диалог для общения",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "В приложении работает система авто-статусов простоя. Если не проявлять активность 15 секунд, ваш статус переключится в 'Отошел'.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (userSettings != null) {
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .glassPanel(RoundedCornerShape(16.dp))
                    .background(Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ваш текущий статус:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(getStatusColor(userSettings.status))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = getStatusTextRu(userSettings.status) + if (!userSettings.customStatus.isNullOrEmpty()) " • ${userSettings.customStatus}" else "",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = getStatusColor(userSettings.status)
                        )
                    }
                }
            }
        }
    }
}

// --- COMPLEX INTERACTIVE DIALOG COMPONENTS ---

@Composable
fun StatusSelectorDialog(
    currentSettings: UserSettingsEntity,
    onDismiss: () -> Unit,
    onStatusSelected: (String, String?, Boolean) -> Unit
) {
    var customText by remember { mutableStateOf(currentSettings.customStatus ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .width(320.dp)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Изменение присутствия",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Выберите желаемый статус:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // 4 Status Actions Option Items
                val statuses = listOf(
                    Triple("ONLINE", "В сети", "Авто-статус активен"),
                    Triple("DND", "Не беспокоить", "Присылает тихие автоответы"),
                    Triple("AWAY", "Отошел", "Предупреждает об отсутствии"),
                    Triple("OFFLINE", "Не в сети", "Инкогнито")
                )

                statuses.forEach { (statusKey, label, desc) ->
                    val isCurrent = currentSettings.status == statusKey
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color = if (isCurrent) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .clickable {
                                // Online resets manual flag to false, DND/AWAY/OFFLINE triggers manual override = true
                                val finalIsManual = statusKey != "ONLINE"
                                onStatusSelected(statusKey, customText.ifEmpty { null }, finalIsManual)
                            }
                            .padding(10.dp)
                            .testTag("status_select_$statusKey"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(getStatusColor(statusKey))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Выбран",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Пользовательский статус:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Custom Status Text input
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    placeholder = { Text("Например: Кодю на Compose 💻") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_status_text_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalIsManual = currentSettings.status != "ONLINE"
                            onStatusSelected(
                                currentSettings.status,
                                customText.ifEmpty { null },
                                finalIsManual
                            )
                        },
                        modifier = Modifier.testTag("status_dialog_apply_button")
                    ) {
                        Text("Применить")
                    }
                }
            }
        }
    }

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentName) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .width(300.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ваше имя в чате",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("name_edit_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (text.trim().isNotEmpty()) {
                                onSave(text.trim())
                            }
                        },
                        enabled = text.trim().isNotEmpty()
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

// --- UTILITY MAPPINGS & FORMATTERS ---

private fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "ONLINE" -> Color(0xFF4CAF50) // Emerald Green
        "DND" -> Color(0xFFF44336)    // Crimson Red
        "AWAY" -> Color(0xFFFFC107)   // Amber Yellow
        "OFFLINE" -> Color(0xFF9E9E9E) // Slated Gray
        else -> Color(0xFF9E9E9E)
    }
}

private fun getStatusTextRu(status: String): String {
    return when (status.uppercase()) {
        "ONLINE" -> "В сети"
        "DND" -> "Не беспокоить"
        "AWAY" -> "Отошел"
        "OFFLINE" -> "Не в сети"
        else -> "Не в сети"
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun AdminPanelDialog(
    settings: UserSettingsEntity,
    interUserMessages: List<InterUserMessageEntity>,
    onDismiss: () -> Unit,
    onToggleBlock: (Boolean) -> Unit,
    onToggleCanWriteFirst: (Boolean) -> Unit,
    onToggleMediaRestricted: (Boolean) -> Unit,
    onToggleTextRestricted: (Boolean) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .glassPanel(RoundedCornerShape(24.dp))
                .testTag("admin_panel_dialog_surface"),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Админ-панель",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Панель Администратора",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Policy Controls Group
                Text(
                    text = "Политики безопасности",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Policy 1: Block User
                        PolicyRow(
                            title = "Заблокировать чаты",
                            desc = "Полная блокировка отправки и приема",
                            checked = settings.isBlocked,
                            onCheckedChange = onToggleBlock
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        
                        // Policy 2: Can write first
                        PolicyRow(
                            title = "Разрешить писать первым",
                            desc = "Позволяет инициировать новые диалоги",
                            checked = settings.canWriteFirst,
                            onCheckedChange = onToggleCanWriteFirst
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Policy 3: Media sending restricted
                        PolicyRow(
                            title = "Ограничить отправку медиа",
                            desc = "Запрещает отправку фото, видео и документов",
                            checked = settings.mediaSendingRestricted,
                            onCheckedChange = onToggleMediaRestricted
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Policy 4: Text sending restricted
                        PolicyRow(
                            title = "Ограничить отправку текста",
                            desc = "Запрещает отправку обычных текстовых сообщений",
                            checked = settings.textSendingRestricted,
                            onCheckedChange = onToggleTextRestricted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Messages Auditing Section
                Text(
                    text = "Журнал аудита сообщений (${interUserMessages.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    if (interUserMessages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Журнал сообщений пуст",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(interUserMessages.size) { index ->
                                val msg = interUserMessages[index]
                                InterUserMessageRow(msg)
                                if (index < interUserMessages.size - 1) {
                                    Divider(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_panel_close_button")
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}

@Composable
fun PolicyRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.8f)
        )
    }
}

@Composable
fun InterUserMessageRow(msg: InterUserMessageEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = msg.senderName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " ➔ ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = msg.receiverName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(msg.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = msg.text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

