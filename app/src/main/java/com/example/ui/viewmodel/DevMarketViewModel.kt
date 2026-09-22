package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DisputeLog
import com.example.data.model.DisputeMessage
import com.example.data.model.EscrowTransaction
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.model.User
import com.example.data.repository.DevMarketRepository
import com.example.data.repository.DisputeResolutionType
import com.example.data.supabase.SupabaseDataRepository
import com.example.data.supabase.SupabasePlatformAnalytics
import com.example.ui.dialogs.DocumentType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UserRole(val label: String, val badge: String) {
    CLIENT("Client", "Hiring & Funding"),
    DEVELOPER("Developer", "Services & Payouts"),
    ADMIN("Admin", "Escrow & Mediation")
}

data class ClientMetrics(
    val totalEscrowFunded: Double = 0.0,
    val totalPendingReview: Double = 0.0,
    val totalReleased: Double = 0.0
)

data class DeveloperMetrics(
    val guaranteedEscrowBalance: Double = 0.0,
    val totalEarnedGross: Double = 0.0,
    val totalEarnedNet: Double = 0.0,
    val activeJobsCount: Int = 0
)

data class AdminMetrics(
    val totalVolumeInEscrow: Double = 0.0,
    val totalCommissionEarned: Double = 0.0,
    val activeDisputesCount: Int = 0,
    val totalProjectsCount: Int = 0
)

data class DevMarketUiState(
    val currentUser: User? = null,
    val isLoggedIn: Boolean = false,
    val isLoginLoading: Boolean = false,
    val loginError: String? = null,
    val activeRole: UserRole = UserRole.CLIENT,
    val selectedProjectId: String? = null,
    val selectedMilestoneId: String? = null,
    val isFundingDialogOpen: Boolean = false,
    val fundingMilestone: Milestone? = null,
    val isDeliverableDialogOpen: Boolean = false,
    val deliverableMilestone: Milestone? = null,
    val isRevisionDialogOpen: Boolean = false,
    val revisionMilestone: Milestone? = null,
    val isReleaseDialogOpen: Boolean = false,
    val releaseMilestone: Milestone? = null,
    val isDisputeDialogOpen: Boolean = false,
    val disputeMilestone: Milestone? = null,
    val isAdminResolutionDialogOpen: Boolean = false,
    val resolvingDisputeLog: DisputeLog? = null,
    val resolvingMilestone: Milestone? = null,
    val isNewProjectDialogOpen: Boolean = false,
    val isPayPalOnboardingDialogOpen: Boolean = false,
    val feeCalculationPopoverMilestone: Milestone? = null,
    val isSettingsDialogOpen: Boolean = false,
    val isLegalDialogOpen: Boolean = false,
    val activeLegalDocType: DocumentType = DocumentType.PRIVACY_POLICY
)

class DevMarketViewModel(
    private val repository: DevMarketRepository,
    private val supabaseRepository: SupabaseDataRepository = SupabaseDataRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevMarketUiState())
    val uiState: StateFlow<DevMarketUiState> = _uiState.asStateFlow()

    private val _supabaseAnalytics = MutableStateFlow(SupabasePlatformAnalytics())
    val supabaseAnalytics: StateFlow<SupabasePlatformAnalytics> = _supabaseAnalytics.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    val projects: StateFlow<List<Project>> = repository.allProjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val milestones: StateFlow<List<Milestone>> = repository.allMilestones.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions: StateFlow<List<EscrowTransaction>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val disputeLogs: StateFlow<List<DisputeLog>> = repository.allDisputeLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val users: StateFlow<List<User>> = repository.allUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val clientMetrics: StateFlow<ClientMetrics> = combine(milestones, transactions) { msList, txList ->
        val funded = msList.filter { it.status == "escrow_funded" || it.status == "under_review" || it.status == "disputed" }
            .sumOf { it.amount }
        val pending = msList.filter { it.status == "under_review" }
            .sumOf { it.amount }
        val released = msList.filter { it.status == "released" }
            .sumOf { it.amount }
        ClientMetrics(
            totalEscrowFunded = funded,
            totalPendingReview = pending,
            totalReleased = released
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClientMetrics())

    val developerMetrics: StateFlow<DeveloperMetrics> = combine(milestones, projects) { msList, projList ->
        val guaranteed = msList.filter { it.status == "escrow_funded" || it.status == "under_review" }
            .sumOf { it.developerAmount }
        val gross = msList.filter { it.status == "released" }
            .sumOf { it.amount }
        val net = msList.filter { it.status == "released" }
            .sumOf { it.developerAmount }
        DeveloperMetrics(
            guaranteedEscrowBalance = guaranteed,
            totalEarnedGross = gross,
            totalEarnedNet = net,
            activeJobsCount = projList.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DeveloperMetrics())

    val adminMetrics: StateFlow<AdminMetrics> = combine(milestones, disputeLogs, projects) { msList, dispList, projList ->
        val inEscrow = msList.filter { it.status == "escrow_funded" || it.status == "under_review" || it.status == "disputed" }
            .sumOf { it.amount }
        val commission = msList.filter { it.status == "released" }
            .sumOf { it.commissionAmount }
        val activeDisputes = dispList.count { it.adminResolution == null }
        AdminMetrics(
            totalVolumeInEscrow = inEscrow,
            totalCommissionEarned = commission,
            activeDisputesCount = activeDisputes,
            totalProjectsCount = projList.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminMetrics())

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(loginError = "Please enter both email and password")
            return
        }

        _uiState.value = _uiState.value.copy(isLoginLoading = true, loginError = null)
        viewModelScope.launch {
            // Dedicated Admin Authentication Guard
            val isAdminEmailCandidate = trimmedEmail.equals(ADMIN_PRIMARY_EMAIL, ignoreCase = true) ||
                trimmedEmail.equals(ADMIN_SECONDARY_EMAIL, ignoreCase = true)

            if (isAdminEmailCandidate) {
                // Strict credential check for Admin Access
                if (password != ADMIN_PASSKEY) {
                    _uiState.value = _uiState.value.copy(
                        isLoginLoading = false,
                        loginError = "Invalid Admin Credentials: Unauthorized access attempt to Admin Mediation Center."
                    )
                    return@launch
                }

                // Ensure admin user entity exists in database
                val userList = users.value
                var adminUser = userList.firstOrNull {
                    it.email.equals(ADMIN_PRIMARY_EMAIL, ignoreCase = true) ||
                        it.email.equals(ADMIN_SECONDARY_EMAIL, ignoreCase = true)
                }

                if (adminUser == null) {
                    adminUser = repository.registerUser(
                        fullName = "Samuel Gitau (Platform Admin)",
                        email = trimmedEmail.lowercase(),
                        role = "admin"
                    )
                }

                _uiState.value = _uiState.value.copy(
                    currentUser = adminUser,
                    isLoggedIn = true,
                    isLoginLoading = false,
                    loginError = null,
                    activeRole = UserRole.ADMIN
                )
                _toastMessage.emit("Admin Authentication Verified. Welcome, ${adminUser.fullName}")
                refreshSupabaseAnalytics()
                return@launch
            }

            // Standard Client / Developer Authentication
            val userList = users.value
            val matchedUser = userList.firstOrNull { it.email.equals(trimmedEmail, ignoreCase = true) }
            if (matchedUser != null) {
                if (matchedUser.role == "admin" && password != ADMIN_PASSKEY) {
                    _uiState.value = _uiState.value.copy(
                        isLoginLoading = false,
                        loginError = "Invalid Admin Credentials: Password verification failed."
                    )
                    return@launch
                }

                val assignedRole = when (matchedUser.role) {
                    "client" -> UserRole.CLIENT
                    "developer" -> UserRole.DEVELOPER
                    "admin" -> UserRole.ADMIN
                    else -> UserRole.CLIENT
                }
                _uiState.value = _uiState.value.copy(
                    currentUser = matchedUser,
                    isLoggedIn = true,
                    isLoginLoading = false,
                    loginError = null,
                    activeRole = assignedRole
                )
                _toastMessage.emit("Welcome back, ${matchedUser.fullName} (${assignedRole.label})")
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoginLoading = false,
                    loginError = "No account found for '$trimmedEmail'. Please check your email or create an account."
                )
            }
        }
    }

    fun register(fullName: String, email: String, password: String, role: String) {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()
        if (trimmedName.isBlank() || trimmedEmail.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(loginError = "Please fill in all required registration fields")
            return
        }
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            _uiState.value = _uiState.value.copy(loginError = "Please enter a valid email address")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(loginError = "Password must be at least 6 characters")
            return
        }

        _uiState.value = _uiState.value.copy(isLoginLoading = true, loginError = null)
        viewModelScope.launch {
            val userList = users.value
            if (userList.any { it.email.equals(trimmedEmail, ignoreCase = true) }) {
                _uiState.value = _uiState.value.copy(
                    isLoginLoading = false,
                    loginError = "An account with email '$trimmedEmail' already exists. Please sign in."
                )
                return@launch
            }

            // Dedicated Admin Registration Guard
            val requestedRole = if (role.equals("admin", ignoreCase = true)) {
                val isAuthorizedEmail = trimmedEmail.equals(ADMIN_PRIMARY_EMAIL, ignoreCase = true) ||
                    trimmedEmail.equals(ADMIN_SECONDARY_EMAIL, ignoreCase = true)

                if (!isAuthorizedEmail) {
                    _uiState.value = _uiState.value.copy(
                        isLoginLoading = false,
                        loginError = "Admin Guard: Admin account registration is restricted to authorized platform owner ($ADMIN_PRIMARY_EMAIL)."
                    )
                    return@launch
                }

                // Verify master admin passkey during admin signup
                if (password != ADMIN_PASSKEY) {
                    _uiState.value = _uiState.value.copy(
                        isLoginLoading = false,
                        loginError = "Admin Guard: Master admin secret passkey required for admin registration."
                    )
                    return@launch
                }
                "admin"
            } else {
                role
            }

            // Create new user in database
            val newUser = repository.registerUser(trimmedName, trimmedEmail, requestedRole)
            val assignedRole = when (newUser.role) {
                "client" -> UserRole.CLIENT
                "developer" -> UserRole.DEVELOPER
                "admin" -> UserRole.ADMIN
                else -> UserRole.CLIENT
            }
            _uiState.value = _uiState.value.copy(
                currentUser = newUser,
                isLoggedIn = true,
                isLoginLoading = false,
                loginError = null,
                activeRole = assignedRole
            )
            _toastMessage.emit("Account created! Welcome to DevMarket, ${newUser.fullName}")
            
            // Auto refresh Supabase stats if admin
            if (assignedRole == UserRole.ADMIN) {
                refreshSupabaseAnalytics()
            }
        }
    }

    fun clearLoginError() {
        _uiState.value = _uiState.value.copy(loginError = null)
    }

    fun quickLogin(user: User) {
        val assignedRole = when (user.role) {
            "client" -> UserRole.CLIENT
            "developer" -> UserRole.DEVELOPER
            "admin" -> UserRole.ADMIN
            else -> UserRole.CLIENT
        }
        _uiState.value = _uiState.value.copy(
            currentUser = user,
            isLoggedIn = true,
            isLoginLoading = false,
            loginError = null,
            activeRole = assignedRole
        )
        viewModelScope.launch {
            _toastMessage.emit("Signed in as ${user.fullName} (${assignedRole.label})")
        }
    }

    fun logout() {
        _uiState.value = _uiState.value.copy(
            currentUser = null,
            isLoggedIn = false,
            loginError = null,
            activeRole = UserRole.CLIENT
        )
        viewModelScope.launch {
            _toastMessage.emit("Signed out successfully.")
        }
    }

    fun selectRole(role: UserRole) {
        val currentRole = _uiState.value.currentUser?.role
        // RBAC Enforcement: Only admin users can switch to or access the Admin view!
        if (role == UserRole.ADMIN && currentRole != "admin") {
            viewModelScope.launch {
                _toastMessage.emit("Access Denied: Only administrators have access to the Admin Mediation Center.")
            }
            return
        }

        _uiState.value = _uiState.value.copy(activeRole = role)
        viewModelScope.launch {
            _toastMessage.emit("Switched to ${role.label} View (${role.badge})")
        }
    }

    fun selectProject(projectId: String?) {
        _uiState.value = _uiState.value.copy(selectedProjectId = projectId)
    }

    fun getProjectMessages(projectId: String): StateFlow<List<DisputeMessage>> {
        return repository.getMessagesForProject(projectId).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun showFundingDialog(milestone: Milestone) {
        _uiState.value = _uiState.value.copy(
            isFundingDialogOpen = true,
            fundingMilestone = milestone
        )
    }

    fun dismissFundingDialog() {
        _uiState.value = _uiState.value.copy(
            isFundingDialogOpen = false,
            fundingMilestone = null
        )
    }

    fun confirmFunding(milestone: Milestone) {
        viewModelScope.launch {
            repository.fundMilestoneEscrow(milestone)
            dismissFundingDialog()
            _toastMessage.emit("PayPal Escrow Funded: \$${milestone.amount.toInt()} held with delayed disbursement.")
        }
    }

    fun showDeliverableDialog(milestone: Milestone) {
        _uiState.value = _uiState.value.copy(
            isDeliverableDialogOpen = true,
            deliverableMilestone = milestone
        )
    }

    fun dismissDeliverableDialog() {
        _uiState.value = _uiState.value.copy(
            isDeliverableDialogOpen = false,
            deliverableMilestone = null
        )
    }

    fun submitDeliverable(milestoneId: String, note: String, url: String) {
        viewModelScope.launch {
            repository.submitDeliverables(milestoneId, note, url)
            dismissDeliverableDialog()
            _toastMessage.emit("Deliverables submitted! Milestone is now In Review.")
        }
    }

    fun showRevisionDialog(milestone: Milestone) {
        _uiState.value = _uiState.value.copy(
            isRevisionDialogOpen = true,
            revisionMilestone = milestone
        )
    }

    fun dismissRevisionDialog() {
        _uiState.value = _uiState.value.copy(
            isRevisionDialogOpen = false,
            revisionMilestone = null
        )
    }

    fun submitRevisionRequest(milestoneId: String, revisionNotes: String) {
        viewModelScope.launch {
            repository.requestRevision(milestoneId, revisionNotes)
            dismissRevisionDialog()
            _toastMessage.emit("Revision requested. Developer notified.")
        }
    }

    fun showReleaseDialog(milestone: Milestone) {
        _uiState.value = _uiState.value.copy(
            isReleaseDialogOpen = true,
            releaseMilestone = milestone
        )
    }

    fun dismissReleaseDialog() {
        _uiState.value = _uiState.value.copy(
            isReleaseDialogOpen = false,
            releaseMilestone = null
        )
    }

    fun confirmReleasePayment(milestone: Milestone) {
        viewModelScope.launch {
            repository.approveAndReleasePayment(milestone.id)
            dismissReleaseDialog()
            val net = milestone.developerAmount
            val fee = milestone.commissionAmount
            _toastMessage.emit("Payment Released! \$${net.toInt()} disbursed to Developer, \$${fee.toInt()} retained as Platform Commission.")
        }
    }

    fun showDisputeDialog(milestone: Milestone) {
        _uiState.value = _uiState.value.copy(
            isDisputeDialogOpen = true,
            disputeMilestone = milestone
        )
    }

    fun dismissDisputeDialog() {
        _uiState.value = _uiState.value.copy(
            isDisputeDialogOpen = false,
            disputeMilestone = null
        )
    }

    fun openDispute(milestone: Milestone, reason: String) {
        viewModelScope.launch {
            val user = _uiState.value.currentUser
            val role = _uiState.value.activeRole
            val raisedByName = user?.fullName ?: when (role) {
                UserRole.CLIENT -> "Sarah Jenkins (Client)"
                UserRole.DEVELOPER -> "Alex Rivera (Developer)"
                UserRole.ADMIN -> "Admin Mediator"
            }
            val raisedById = user?.id ?: when (role) {
                UserRole.CLIENT -> "usr_client_1"
                UserRole.DEVELOPER -> "usr_dev_1"
                UserRole.ADMIN -> "usr_admin_1"
            }
            repository.openDispute(milestone, reason, raisedByName, raisedById)
            dismissDisputeDialog()
            _toastMessage.emit("Dispute escalated to Admin Resolution Center.")
        }
    }

    fun showAdminResolutionDialog(log: DisputeLog, milestone: Milestone?) {
        _uiState.value = _uiState.value.copy(
            isAdminResolutionDialogOpen = true,
            resolvingDisputeLog = log,
            resolvingMilestone = milestone
        )
    }

    fun dismissAdminResolutionDialog() {
        _uiState.value = _uiState.value.copy(
            isAdminResolutionDialogOpen = false,
            resolvingDisputeLog = null,
            resolvingMilestone = null
        )
    }

    fun resolveDispute(
        log: DisputeLog,
        milestoneId: String,
        type: DisputeResolutionType,
        notes: String
    ) {
        viewModelScope.launch {
            repository.adminResolveDispute(log.id, milestoneId, type, notes)
            dismissAdminResolutionDialog()
            val action = if (type == DisputeResolutionType.FORCE_RELEASE) "Force Released to Developer" else "Force Refunded to Client"
            _toastMessage.emit("Dispute resolved: $action")
        }
    }

    fun sendDisputeMessage(projectId: String, messageText: String) {
        if (messageText.isBlank()) return
        viewModelScope.launch {
            val role = _uiState.value.activeRole
            val (senderId, senderName) = when (role) {
                UserRole.CLIENT -> "usr_client_1" to "Sarah Jenkins"
                UserRole.DEVELOPER -> "usr_dev_1" to "Alex Rivera"
                UserRole.ADMIN -> "usr_admin_1" to "DevMarket Trust & Safety"
            }
            repository.sendDisputeMessage(
                projectId = projectId,
                senderId = senderId,
                senderName = senderName,
                senderRole = role.name.lowercase(),
                message = messageText
            )
        }
    }

    fun showPayPalOnboardingDialog() {
        _uiState.value = _uiState.value.copy(isPayPalOnboardingDialogOpen = true)
    }

    fun dismissPayPalOnboardingDialog() {
        _uiState.value = _uiState.value.copy(isPayPalOnboardingDialogOpen = false)
    }

    fun togglePayPalConnection(connect: Boolean) {
        viewModelScope.launch {
            val merchantId = if (connect) "PMR-DEV-${System.currentTimeMillis().toString().takeLast(5)}" else null
            repository.updateDeveloperPayPalStatus("usr_dev_1", connect, merchantId)
            dismissPayPalOnboardingDialog()
            if (connect) {
                _toastMessage.emit("PayPal Partner Onboarding verified! Merchant ID: $merchantId (Delayed Disbursement Enabled)")
            } else {
                _toastMessage.emit("PayPal account disconnected.")
            }
        }
    }

    fun showNewProjectDialog() {
        _uiState.value = _uiState.value.copy(isNewProjectDialogOpen = true)
    }

    fun dismissNewProjectDialog() {
        _uiState.value = _uiState.value.copy(isNewProjectDialogOpen = false)
    }

    fun createProject(
        title: String,
        description: String,
        milestonesList: List<Pair<String, Double>>
    ) {
        viewModelScope.launch {
            repository.createNewProject(
                clientId = "usr_client_1",
                developerId = "usr_dev_1",
                title = title,
                description = description,
                milestones = milestonesList
            )
            dismissNewProjectDialog()
            _toastMessage.emit("New project created with ${milestonesList.size} milestones.")
        }
    }

    fun toggleFeeCalculationPopover(milestone: Milestone?) {
        _uiState.value = _uiState.value.copy(feeCalculationPopoverMilestone = milestone)
    }

    fun refreshSupabaseAnalytics() {
        viewModelScope.launch {
            val stats = supabaseRepository.fetchPlatformStatistics()
            _supabaseAnalytics.value = stats
            if (stats.isLiveFromSupabase) {
                _toastMessage.emit("Supabase PostgREST: Live platform analytics synchronized.")
            } else if (stats.fetchErrorMessage != null) {
                _toastMessage.emit("Supabase: ${stats.fetchErrorMessage}")
            }
        }
    }

    fun syncLocalDataToSupabase() {
        viewModelScope.launch {
            val success = supabaseRepository.syncLocalDataToSupabase(projects.value, milestones.value)
            if (success) {
                _toastMessage.emit("Local projects and escrows successfully synced to Supabase!")
                refreshSupabaseAnalytics()
            } else {
                _toastMessage.emit("Sync failed. Check Supabase connection in settings.")
            }
        }
    }

    fun showSettingsDialog() {
        _uiState.value = _uiState.value.copy(isSettingsDialogOpen = true)
    }

    fun dismissSettingsDialog() {
        _uiState.value = _uiState.value.copy(isSettingsDialogOpen = false)
    }

    fun showLegalDialog(docType: DocumentType) {
        _uiState.value = _uiState.value.copy(
            isLegalDialogOpen = true,
            activeLegalDocType = docType
        )
    }

    fun dismissLegalDialog() {
        _uiState.value = _uiState.value.copy(isLegalDialogOpen = false)
    }

    fun getSupabaseUrl(): String = supabaseRepository.getSupabaseUrl()

    companion object {
        const val ADMIN_PRIMARY_EMAIL = "samuelgitau76@gmail.com"
        const val ADMIN_SECONDARY_EMAIL = "admin@devmarket.io"
        const val ADMIN_PASSKEY = "a23a4bSAMUEL"
    }
}
