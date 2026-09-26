package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.IconButton
import com.example.ui.components.RoleSwitcherBar
import com.example.ui.screens.LoginScreen
import com.example.ui.dialogs.AdminResolutionModal
import com.example.ui.dialogs.CommunicationPortalDialog
import com.example.ui.dialogs.CreateProjectModal
import com.example.ui.dialogs.DocumentType
import com.example.ui.dialogs.FundEscrowModal
import com.example.ui.dialogs.LegalDocumentModal
import com.example.ui.dialogs.OpenDisputeModal
import com.example.ui.dialogs.PayPalDetailsDialog
import com.example.ui.dialogs.PayPalOnboardingModal
import com.example.ui.dialogs.ReleasePaymentModal
import com.example.ui.dialogs.RequestRevisionModal
import com.example.ui.dialogs.SettingsPanelModal
import com.example.ui.dialogs.SubmitDeliverablesModal
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ClientDashboardScreen
import com.example.ui.screens.DeveloperDashboardScreen
import com.example.ui.screens.EscrowLedgerScreen
import com.example.ui.screens.ProjectDashboardScreen
import com.example.ui.theme.ButtonYellow
import com.example.ui.theme.ButtonYellowHover
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.OnButtonYellow
import com.example.ui.theme.PayPalBlue
import com.example.ui.theme.PayPalSky
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.DevMarketViewModel
import com.example.ui.viewmodel.UserRole
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevMarketApp(
    viewModel: DevMarketViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val milestones by viewModel.milestones.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val disputeLogs by viewModel.disputeLogs.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    val clientMetrics by viewModel.clientMetrics.collectAsStateWithLifecycle()
    val developerMetrics by viewModel.developerMetrics.collectAsStateWithLifecycle()
    val adminMetrics by viewModel.adminMetrics.collectAsStateWithLifecycle()
    val supabaseAnalytics by viewModel.supabaseAnalytics.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var lastRole by remember { mutableStateOf(uiState.activeRole) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Gate UI with LoginScreen if not authenticated
    if (!uiState.isLoggedIn || uiState.currentUser == null) {
        LoginScreen(
            errorMessage = uiState.loginError,
            isLoading = uiState.isLoginLoading,
            onLogin = { email, password ->
                viewModel.login(email, password)
            },
            onRegister = { fullName, email, password, role ->
                viewModel.register(fullName, email, password, role)
            },
            onClearError = {
                viewModel.clearLoginError()
            }
        )
        return
    }

    val currentUser = uiState.currentUser!!
    val isCurrentUserAdmin = currentUser.role == "admin"

    // Synchronize selected tab with current role and block unauthorized admin tab selection
    LaunchedEffect(uiState.activeRole, currentUser.role) {
        if (!isCurrentUserAdmin && uiState.activeRole == UserRole.ADMIN) {
            // Force non-admins out of admin role
            viewModel.selectRole(if (currentUser.role == "developer") UserRole.DEVELOPER else UserRole.CLIENT)
        } else if (lastRole != uiState.activeRole) {
            lastRole = uiState.activeRole
            selectedTab = when (uiState.activeRole) {
                UserRole.CLIENT -> 1
                UserRole.DEVELOPER -> 2
                UserRole.ADMIN -> if (isCurrentUserAdmin) 3 else 0
            }
        }
    }

    // Toast snackbar listener
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val activeUser = currentUser
    val developerUser = if (currentUser.role == "developer") currentUser else users.firstOrNull { it.role == "developer" }

    val activeDisputeProject = disputeLogs.firstOrNull()?.projectId ?: "proj_2"
    val disputeMessages by viewModel.getProjectMessages(activeDisputeProject).collectAsStateWithLifecycle(emptyList())

    val activePortalProjectId = uiState.communicationPortalProjectId ?: uiState.selectedProjectId ?: "proj_1"
    val communicationMessages by viewModel.getProjectMessages(activePortalProjectId).collectAsStateWithLifecycle(emptyList())

    var showAccountManagementDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = if (uiState.isDarkMode) Slate950 else Color(0xFFF8FAFC),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = if (uiState.isDarkMode) Slate900 else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    width = 0.5.dp,
                    color = if (uiState.isDarkMode) Slate800 else Slate200
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clean Brand Identity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedTab = 0 }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.isDarkMode) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                .border(1.dp, Emerald500, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "DevMarket Logo",
                                tint = Emerald500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "DevMarket",
                                    color = if (uiState.isDarkMode) Color.White else Slate900,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Emerald500)
                                )
                            }
                            Text(
                                text = "Delayed Disbursement Escrow",
                                color = if (uiState.isDarkMode) Slate400 else Slate500,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Direct Navigation Icons: Account Management, Theme Toggle, Settings, Logout
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Account Management Pill / Button
                        Surface(
                            color = if (uiState.isDarkMode) Slate800 else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.isDarkMode) Slate700 else Slate300),
                            modifier = Modifier
                                .clickable { showAccountManagementDialog = true }
                                .testTag("nav_account_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (currentUser.role) {
                                                "client" -> Color(0xFF0284C7)
                                                "developer" -> Emerald500
                                                "admin" -> Color(0xFF7C3AED)
                                                else -> Slate400
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser.fullName.first().toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = currentUser.fullName.split(" ").first(),
                                    color = if (uiState.isDarkMode) Slate200 else Slate800,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Account Options",
                                    tint = if (uiState.isDarkMode) Slate400 else Slate600,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // 1.5 Quick Theme Toggle Button (Light & Dark Mode)
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.isDarkMode) ButtonYellow.copy(alpha = 0.15f) else ButtonYellow)
                                .border(1.dp, if (uiState.isDarkMode) ButtonYellow else ButtonYellowHover, RoundedCornerShape(8.dp))
                                .testTag("nav_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (uiState.isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = if (uiState.isDarkMode) ButtonYellow else OnButtonYellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // 2. Settings Navigation Icon Button
                        IconButton(
                            onClick = { viewModel.showSettingsDialog() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.isDarkMode) Slate800 else Color(0xFFF1F5F9))
                                .border(1.dp, if (uiState.isDarkMode) Slate700 else Slate300, RoundedCornerShape(8.dp))
                                .testTag("nav_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Platform Settings",
                                tint = if (uiState.isDarkMode) Slate300 else Slate700,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // 3. Logout Navigation Icon Button
                        IconButton(
                            onClick = { showLogoutConfirmDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.isDarkMode) Slate800 else Color(0xFFF1F5F9))
                                .border(1.dp, if (uiState.isDarkMode) Slate700 else Slate300, RoundedCornerShape(8.dp))
                                .testTag("nav_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = Rose500,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (uiState.isDarkMode) Slate900 else Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .border(width = 0.5.dp, color = if (uiState.isDarkMode) Slate800 else Slate200)
                    .testTag("bottom_nav_bar")
            ) {
                // 1. Projects Dashboard tab (Always visible to all authenticated users)
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Projects") },
                    label = { Text("Projects", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Emerald500,
                        indicatorColor = Emerald600,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_item_projects")
                )

                // 2. Role-tailored Workspace / Hub Tab
                if (currentUser.role == "client") {
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            viewModel.selectRole(UserRole.CLIENT)
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Client Hub") },
                        label = { Text("Client Hub", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF0284C7),
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_client")
                    )
                } else if (currentUser.role == "developer") {
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            viewModel.selectRole(UserRole.DEVELOPER)
                        },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Dev Hub") },
                        label = { Text("Dev Hub", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Emerald500,
                            indicatorColor = Emerald600,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_developer")
                    )
                } else if (isCurrentUserAdmin) {
                    // For Admin: Client Hub, Developer Hub, Admin Desk
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            viewModel.selectRole(UserRole.CLIENT)
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Client") },
                        label = { Text("Client", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF38BDF8),
                            indicatorColor = Color(0xFF0284C7),
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_client")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            viewModel.selectRole(UserRole.DEVELOPER)
                        },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Developer") },
                        label = { Text("Dev", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Emerald500,
                            indicatorColor = Emerald600,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_developer")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = {
                            selectedTab = 3
                            viewModel.selectRole(UserRole.ADMIN)
                        },
                        icon = { Icon(Icons.Default.Gavel, contentDescription = "Admin") },
                        label = { Text("Admin", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFFA78BFA),
                            indicatorColor = Color(0xFF7C3AED),
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_admin")
                    )
                }

                // 3. Escrow Ledger tab (visible to all authenticated roles)
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Ledger") },
                    label = { Text("Ledger", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = PayPalBlue,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_item_ledger")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ProjectDashboardScreen(
                    projects = projects,
                    milestones = milestones,
                    users = users,
                    currentUser = currentUser,
                    activeRole = uiState.activeRole,
                    selectedProjectId = uiState.selectedProjectId,
                    feeCalculationMilestone = uiState.feeCalculationPopoverMilestone,
                    messages = communicationMessages,
                    onSelectProject = { viewModel.selectProject(it) },
                    onFundMilestone = { viewModel.showFundingDialog(it) },
                    onReleaseMilestone = { viewModel.showReleaseDialog(it) },
                    onRequestRevision = { viewModel.showRevisionDialog(it) },
                    onOpenDispute = { viewModel.showDisputeDialog(it) },
                    onSubmitDeliverable = { viewModel.showDeliverableDialog(it) },
                    onToggleFeeCalculation = { viewModel.toggleFeeCalculationPopover(it) },
                    onNewProject = { viewModel.showNewProjectDialog() },
                    onOpenCommunicationPortal = { viewModel.openCommunicationPortal(it) },
                    onSendMessage = { text -> viewModel.sendPublicCommunicationMessage(uiState.selectedProjectId ?: "proj_1", text) },
                    onOpenPayPalDetails = { viewModel.showPayPalDetailsDialog() },
                    onOpenSettings = { viewModel.showSettingsDialog() },
                    onOpenAccountManagement = { showAccountManagementDialog = true },
                    onLogout = { showLogoutConfirmDialog = true }
                )

                1 -> ClientDashboardScreen(
                    metrics = clientMetrics,
                    projects = projects,
                    milestones = milestones,
                    selectedProjectId = uiState.selectedProjectId,
                    feeCalculationMilestone = uiState.feeCalculationPopoverMilestone,
                    onSelectProject = { viewModel.selectProject(it) },
                    onFundMilestone = { viewModel.showFundingDialog(it) },
                    onReleaseMilestone = { viewModel.showReleaseDialog(it) },
                    onRequestRevision = { viewModel.showRevisionDialog(it) },
                    onOpenDispute = { viewModel.showDisputeDialog(it) },
                    onToggleFeeCalculation = { viewModel.toggleFeeCalculationPopover(it) },
                    onNewProject = { viewModel.showNewProjectDialog() }
                )

                2 -> DeveloperDashboardScreen(
                    metrics = developerMetrics,
                    developerUser = developerUser,
                    projects = projects,
                    milestones = milestones,
                    onSubmitDeliverable = { viewModel.showDeliverableDialog(it) },
                    onManagePayPal = { viewModel.showPayPalOnboardingDialog() }
                )

                3 -> {
                    // RBAC Guard: If not admin, do not render AdminDashboardScreen
                    if (isCurrentUserAdmin) {
                        AdminDashboardScreen(
                            metrics = adminMetrics,
                            disputeLogs = disputeLogs,
                            projects = projects,
                            milestones = milestones,
                            disputeMessages = disputeMessages,
                            supabaseAnalytics = supabaseAnalytics,
                            onRefreshSupabase = { viewModel.refreshSupabaseAnalytics() },
                            onSyncSupabase = { viewModel.syncLocalDataToSupabase() },
                            onOpenResolutionModal = { log, ms -> viewModel.showAdminResolutionDialog(log, ms) },
                            onSendMessage = { projId, text -> viewModel.sendDisputeMessage(projId, text) }
                        )
                    } else {
                        // Access Denied Screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Slate950)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Rose500.copy(alpha = 0.15f))
                                    .border(1.dp, Rose500, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Access Restricted",
                                    tint = Rose500,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Access Restricted",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Developers and Clients must not access the Admin Mediation Center. This section is restricted strictly to authorized platform administrators.",
                                color = Slate400,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            androidx.compose.material3.Button(
                                onClick = {
                                    selectedTab = 0
                                    viewModel.selectRole(if (currentUser.role == "developer") UserRole.DEVELOPER else UserRole.CLIENT)
                                },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Emerald600),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Return to Project Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                4 -> EscrowLedgerScreen(
                    transactions = transactions,
                    milestones = milestones
                )
            }

            // Dialogs
            if (uiState.isFundingDialogOpen && uiState.fundingMilestone != null) {
                FundEscrowModal(
                    milestone = uiState.fundingMilestone!!,
                    onConfirm = { viewModel.confirmFunding(uiState.fundingMilestone!!) },
                    onDismiss = { viewModel.dismissFundingDialog() }
                )
            }

            if (uiState.isDeliverableDialogOpen && uiState.deliverableMilestone != null) {
                SubmitDeliverablesModal(
                    milestone = uiState.deliverableMilestone!!,
                    onSubmit = { note, url ->
                        viewModel.submitDeliverable(uiState.deliverableMilestone!!.id, note, url)
                    },
                    onDismiss = { viewModel.dismissDeliverableDialog() }
                )
            }

            if (uiState.isRevisionDialogOpen && uiState.revisionMilestone != null) {
                RequestRevisionModal(
                    milestone = uiState.revisionMilestone!!,
                    onRequest = { feedback ->
                        viewModel.submitRevisionRequest(uiState.revisionMilestone!!.id, feedback)
                    },
                    onDismiss = { viewModel.dismissRevisionDialog() }
                )
            }

            if (uiState.isReleaseDialogOpen && uiState.releaseMilestone != null) {
                ReleasePaymentModal(
                    milestone = uiState.releaseMilestone!!,
                    onConfirm = { viewModel.confirmReleasePayment(uiState.releaseMilestone!!) },
                    onDismiss = { viewModel.dismissReleaseDialog() }
                )
            }

            if (uiState.isDisputeDialogOpen && uiState.disputeMilestone != null) {
                OpenDisputeModal(
                    milestone = uiState.disputeMilestone!!,
                    onConfirm = { reason ->
                        viewModel.openDispute(uiState.disputeMilestone!!, reason)
                    },
                    onDismiss = { viewModel.dismissDisputeDialog() }
                )
            }

            if (uiState.isAdminResolutionDialogOpen && uiState.resolvingDisputeLog != null) {
                AdminResolutionModal(
                    log = uiState.resolvingDisputeLog!!,
                    milestone = uiState.resolvingMilestone,
                    onResolve = { type, notes ->
                        viewModel.resolveDispute(
                            log = uiState.resolvingDisputeLog!!,
                            milestoneId = uiState.resolvingDisputeLog!!.milestoneId ?: "",
                            type = type,
                            notes = notes
                        )
                    },
                    onDismiss = { viewModel.dismissAdminResolutionDialog() }
                )
            }

            if (uiState.isPayPalOnboardingDialogOpen) {
                PayPalOnboardingModal(
                    isConnected = developerUser?.paypalConnected == true,
                    onToggle = { viewModel.togglePayPalConnection(it) },
                    onDismiss = { viewModel.dismissPayPalOnboardingDialog() }
                )
            }

            if (uiState.isNewProjectDialogOpen) {
                CreateProjectModal(
                    onCreate = { title, desc, msList ->
                        viewModel.createProject(title, desc, msList)
                    },
                    onDismiss = { viewModel.dismissNewProjectDialog() }
                )
            }

            // 1-on-1 Communication Portal Dialog
            if (uiState.isCommunicationPortalOpen) {
                val portalProject = projects.firstOrNull { it.id == activePortalProjectId }
                CommunicationPortalDialog(
                    project = portalProject,
                    messages = communicationMessages,
                    currentUser = currentUser,
                    onSendMessage = { text ->
                        viewModel.sendPublicCommunicationMessage(portalProject?.id ?: "proj_1", text)
                    },
                    onDismiss = { viewModel.closeCommunicationPortal() }
                )
            }

            // PayPal Details & Setup Dialog
            if (uiState.isPayPalDetailsDialogOpen) {
                PayPalDetailsDialog(
                    currentUser = currentUser,
                    onSaveDetails = { email, merchantId, isConnected ->
                        viewModel.savePayPalDetails(email, merchantId, isConnected)
                    },
                    onDismiss = { viewModel.dismissPayPalDetailsDialog() }
                )
            }

            // Platform Settings Modal
            if (uiState.isSettingsDialogOpen && currentUser != null) {
                SettingsPanelModal(
                    currentUser = currentUser,
                    supabaseUrl = viewModel.getSupabaseUrl(),
                    isDarkMode = uiState.isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onClose = { viewModel.dismissSettingsDialog() },
                    onOpenPrivacyPolicy = { viewModel.showLegalDialog(DocumentType.PRIVACY_POLICY) },
                    onOpenEscrowAgreement = { viewModel.showLegalDialog(DocumentType.ESCROW_AGREEMENT) },
                    onSyncSupabase = { viewModel.syncLocalDataToSupabase() }
                )
            }

            // Legal Documents Modal (Privacy Policy & Escrow Agreement)
            if (uiState.isLegalDialogOpen) {
                LegalDocumentModal(
                    documentType = uiState.activeLegalDocType,
                    onClose = { viewModel.dismissLegalDialog() }
                )
            }

            // Account Management & Perspective Switcher Modal
            if (showAccountManagementDialog && currentUser != null) {
                AccountManagementDialog(
                    currentUser = currentUser,
                    users = users,
                    activeRole = uiState.activeRole,
                    onRoleSelected = { role ->
                        viewModel.selectRole(role)
                        selectedTab = when (role) {
                            UserRole.CLIENT -> 1
                            UserRole.DEVELOPER -> 2
                            UserRole.ADMIN -> 3
                        }
                    },
                    onSwitchAccount = { user ->
                        viewModel.quickLogin(user)
                    },
                    onOpenSettings = { viewModel.showSettingsDialog() },
                    onLogout = {
                        showLogoutConfirmDialog = true
                    },
                    onDismiss = { showAccountManagementDialog = false }
                )
            }

            // Direct Logout Confirmation Dialog
            if (showLogoutConfirmDialog && currentUser != null) {
                LogoutConfirmationDialog(
                    userName = currentUser.fullName,
                    onConfirmLogout = {
                        showLogoutConfirmDialog = false
                        showAccountManagementDialog = false
                        viewModel.logout()
                    },
                    onDismiss = { showLogoutConfirmDialog = false }
                )
            }
        }
    }
}

@Composable
fun AccountManagementDialog(
    currentUser: com.example.data.model.User,
    users: List<com.example.data.model.User>,
    activeRole: com.example.ui.viewmodel.UserRole,
    onRoleSelected: (com.example.ui.viewmodel.UserRole) -> Unit,
    onSwitchAccount: (com.example.data.model.User) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Slate900,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Account Title and Close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Emerald500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Account Management",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // User Details Card
                Surface(
                    color = Slate800.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    when (currentUser.role) {
                                        "client" -> Color(0xFF0284C7)
                                        "developer" -> Emerald500
                                        "admin" -> Color(0xFF7C3AED)
                                        else -> Slate400
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.fullName.first().toString(),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.fullName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = currentUser.email,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Surface(
                                color = Emerald500.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PayPal Verified • ${currentUser.role.uppercase()} • Escrow Secured",
                                    color = Emerald500,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Active Perspective Switcher
                Column {
                    Text(
                        text = "ROLE PERSPECTIVE",
                        color = Slate400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val availableRoles = if (currentUser.role == "admin") {
                            listOf(
                                com.example.ui.viewmodel.UserRole.CLIENT,
                                com.example.ui.viewmodel.UserRole.DEVELOPER,
                                com.example.ui.viewmodel.UserRole.ADMIN
                            )
                        } else {
                            listOf(
                                if (currentUser.role == "developer")
                                    com.example.ui.viewmodel.UserRole.DEVELOPER
                                else
                                    com.example.ui.viewmodel.UserRole.CLIENT
                            )
                        }

                        availableRoles.forEach { role ->
                            val isSelected = activeRole == role
                            Surface(
                                color = if (isSelected) {
                                    when (role) {
                                        com.example.ui.viewmodel.UserRole.CLIENT -> Color(0xFF0284C7).copy(alpha = 0.25f)
                                        com.example.ui.viewmodel.UserRole.DEVELOPER -> Emerald500.copy(alpha = 0.25f)
                                        com.example.ui.viewmodel.UserRole.ADMIN -> Color(0xFF7C3AED).copy(alpha = 0.25f)
                                    }
                                } else Slate800,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) {
                                        when (role) {
                                            com.example.ui.viewmodel.UserRole.CLIENT -> Color(0xFF38BDF8)
                                            com.example.ui.viewmodel.UserRole.DEVELOPER -> Emerald500
                                            com.example.ui.viewmodel.UserRole.ADMIN -> Color(0xFFA78BFA)
                                        }
                                    } else Slate700
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onRoleSelected(role)
                                        onDismiss()
                                    }
                                    .testTag("role_button_${role.name.lowercase()}")
                            ) {
                                Text(
                                    text = role.label,
                                    color = if (isSelected) Color.White else Slate300,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Quick Demo Account Switcher
                if (users.size > 1) {
                    Column {
                        Text(
                            text = "SWITCH ACCOUNT (DEMO PROFILES)",
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            users.take(3).forEach { user ->
                                val isCurrent = user.id == currentUser.id
                                Surface(
                                    color = if (isCurrent) Emerald500.copy(alpha = 0.15f) else Slate800,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isCurrent) Emerald500 else Slate700
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            if (!isCurrent) {
                                                onSwitchAccount(user)
                                                onDismiss()
                                            }
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = user.fullName.split(" ").first(),
                                            color = if (isCurrent) Emerald500 else Slate300,
                                            fontSize = 10.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = user.role.capitalize(java.util.Locale.ROOT),
                                            color = Slate400,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Slate800)
                )

                // Actions: Settings & Logout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onDismiss()
                                onOpenSettings()
                            }
                            .testTag("settings_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = Slate300,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Settings", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Surface(
                        color = Rose500.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onDismiss()
                                onLogout()
                            }
                            .testTag("logout_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = Rose500,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out", color = Rose500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    userName: String,
    onConfirmLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Slate900,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Rose500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Rose500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sign Out of DevMarket?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = userName,
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = "Are you sure you want to end your current session? All funded milestones and escrow guarantees remain securely recorded.",
                    color = Slate300,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onConfirmLogout,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                    ) {
                        Text("Sign Out", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
