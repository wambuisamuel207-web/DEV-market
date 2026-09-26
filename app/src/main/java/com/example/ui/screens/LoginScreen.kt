package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Amber500
import com.example.ui.theme.ButtonYellow
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

@Composable
fun LoginScreen(
    errorMessage: String?,
    isLoading: Boolean,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (fullName: String, email: String, password: String, role: String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = Sign In, 1 = Sign Up
    var selectedAuthMode by remember { mutableIntStateOf(0) }

    // Sign In form state
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Sign Up form state
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regRole by remember { mutableStateOf("client") } // "client" or "developer"

    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo & Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.5.dp, Emerald500, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "DevMarket Security Logo",
                    tint = Emerald500,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DevMarket",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Emerald500.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "ESCROW",
                        color = Emerald500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "PayPal Delayed Disbursement Marketplace",
                color = Slate400,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Sign In / Sign Up Tab Selector
                    TabRow(
                        selectedTabIndex = selectedAuthMode,
                        containerColor = Slate900,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedAuthMode]),
                                color = ButtonYellow,
                                height = 3.dp
                            )
                        },
                        divider = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Slate800)
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedAuthMode == 0,
                            onClick = {
                                selectedAuthMode = 0
                                onClearError()
                            },
                            text = {
                                Text(
                                    text = "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (selectedAuthMode == 0) Color.White else Slate400
                                )
                            },
                            modifier = Modifier.testTag("tab_sign_in")
                        )
                        Tab(
                            selected = selectedAuthMode == 1,
                            onClick = {
                                selectedAuthMode = 1
                                onClearError()
                            },
                            text = {
                                Text(
                                    text = "Create Account",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (selectedAuthMode == 1) Color.White else Slate400
                                )
                            },
                            modifier = Modifier.testTag("tab_sign_up")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (selectedAuthMode == 0) {
                        // =================== SIGN IN FORM ===================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sign in to your DevMarket account",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                            Surface(
                                color = Color(0xFF7C3AED).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .clickable {
                                        loginEmail = "samuelgitau76@gmail.com"
                                        loginPassword = "a23a4bSAMUEL"
                                    }
                                    .testTag("admin_quick_fill_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Admin Fill",
                                        color = Color(0xFFDDD6FE),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email Field
                        Text(
                            text = "EMAIL ADDRESS",
                            color = Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            placeholder = { Text("e.g. sarah@hypergrowth.io", color = Slate500, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email", tint = Slate400, modifier = Modifier.size(18.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald500,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Slate200,
                                cursorColor = Emerald500
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Field
                        Text(
                            text = "PASSWORD",
                            color = Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            placeholder = { Text("Enter your password", color = Slate500, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Password", tint = Slate400, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password Visibility",
                                        tint = Slate400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald500,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Slate200,
                                cursorColor = Emerald500
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    if (loginEmail.isNotBlank() && loginPassword.isNotBlank()) {
                                        onLogin(loginEmail, loginPassword)
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    } else {
                        // =================== SIGN UP FORM ===================
                        Text(
                            text = "Create a new developer or client account",
                            color = Slate400,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Role Selection: Client vs Developer vs Platform Admin (Owner Only)
                        Text(
                            text = "I WANT TO:",
                            color = Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Hire Developers (Client)
                            Surface(
                                color = if (regRole == "client") Color(0xFF0284C7).copy(alpha = 0.2f) else Slate800,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (regRole == "client") 1.5.dp else 1.dp,
                                    if (regRole == "client") Color(0xFF38BDF8) else Slate700
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { regRole = "client" }
                                    .testTag("signup_role_client")
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Client",
                                        tint = if (regRole == "client") Color(0xFF38BDF8) else Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Hire Talent",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Client",
                                            color = Slate400,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            // Offer Services (Developer)
                            Surface(
                                color = if (regRole == "developer") Emerald500.copy(alpha = 0.2f) else Slate800,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (regRole == "developer") 1.5.dp else 1.dp,
                                    if (regRole == "developer") Emerald500 else Slate700
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { regRole = "developer" }
                                    .testTag("signup_role_developer")
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Developer",
                                        tint = if (regRole == "developer") Emerald500 else Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Offer Services",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Developer",
                                            color = Slate400,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            // Platform Admin (Authorized Owner only)
                            Surface(
                                color = if (regRole == "admin") Color(0xFF7C3AED).copy(alpha = 0.25f) else Slate800,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (regRole == "admin") 1.5.dp else 1.dp,
                                    if (regRole == "admin") Color(0xFFA78BFA) else Slate700
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { regRole = "admin" }
                                    .testTag("signup_role_admin")
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Gavel,
                                        contentDescription = "Admin",
                                        tint = if (regRole == "admin") Color(0xFFA78BFA) else Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Admin",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Owner Only",
                                            color = if (regRole == "admin") Color(0xFFA78BFA) else Slate400,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (regRole == "admin") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0xFF7C3AED).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Admin registration is restricted to platform owner (samuelgitau76@gmail.com) with master admin key.",
                                        color = Color(0xFFDDD6FE),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full Name Field
                        Text(
                            text = "FULL NAME",
                            color = Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_name_input"),
                            placeholder = { Text("e.g. Maya Chen", color = Slate500, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Name", tint = Slate400, modifier = Modifier.size(18.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PayPalSky,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Slate200,
                                cursorColor = PayPalSky
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email Field
                        Text(
                            text = "WORK EMAIL",
                            color = Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_email_input"),
                            placeholder = { Text("e.g. maya@devstudio.com", color = Slate500, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email", tint = Slate400, modifier = Modifier.size(18.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PayPalSky,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Slate200,
                                cursorColor = PayPalSky
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Field
                        Text(
                            text = "CREATE PASSWORD",
                            color = Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_password_input"),
                            placeholder = { Text("At least 6 characters", color = Slate500, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Password", tint = Slate400, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password Visibility",
                                        tint = Slate400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PayPalSky,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Slate200,
                                cursorColor = PayPalSky
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    if (regFullName.isNotBlank() && regEmail.isNotBlank() && regPassword.isNotBlank()) {
                                        onRegister(regFullName, regEmail, regPassword, regRole)
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Error Message Banner
                    AnimatedVisibility(visible = errorMessage != null) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = Rose500.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Rose500.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_error_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = Rose500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = errorMessage ?: "",
                                        color = Color(0xFFFDA4AF),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Submit Action Button (Sign In or Create Account)
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            if (selectedAuthMode == 0) {
                                onLogin(loginEmail, loginPassword)
                            } else {
                                onRegister(regFullName, regEmail, regPassword, regRole)
                            }
                        },
                        enabled = if (selectedAuthMode == 0) {
                            !isLoading && loginEmail.isNotBlank() && loginPassword.isNotBlank()
                        } else {
                            !isLoading && regFullName.isNotBlank() && regEmail.isNotBlank() && regPassword.isNotBlank()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag(if (selectedAuthMode == 0) "login_submit_button" else "signup_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonYellow,
                            disabledContainerColor = Slate800
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = OnButtonYellow,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (selectedAuthMode == 0) "Sign In to Account" else "Create DevMarket Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = OnButtonYellow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode switch toggle link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedAuthMode == 0) "Don't have an account? " else "Already have an account? ",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (selectedAuthMode == 0) "Create one" else "Sign in",
                            color = if (selectedAuthMode == 0) Emerald500 else PayPalSky,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable {
                                    selectedAuthMode = if (selectedAuthMode == 0) 1 else 0
                                    onClearError()
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security Notice Card
            Surface(
                color = Slate900.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Access Control Security",
                        tint = Amber500,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Strict Role-Based Access Control (RBAC)",
                            color = Slate200,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Developers and clients are strictly isolated to their respective dashboards. The Admin Mediation & Escrow Center is restricted solely to authorized platform administrators.",
                            color = Slate400,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
