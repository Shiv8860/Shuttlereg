package com.example.shuttlereg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shuttlereg.domain.model.EventCategory
import com.example.shuttlereg.domain.model.Gender
import com.example.shuttlereg.presentation.viewmodel.AuthViewModel
import com.example.shuttlereg.presentation.viewmodel.AuthUiState
import com.example.shuttlereg.ui.theme.ShuttleregTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShuttleregTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainApp()
                }
            }
        }
    }
}

private enum class Screen { HOME, ELIGIBILITY, SIGN_IN, SIGN_UP, RESET_PASSWORD }

@Composable
fun MainApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isSignedIn by authViewModel.isSignedIn.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(if (isSignedIn) Screen.HOME else Screen.SIGN_IN) }

    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            currentScreen = Screen.HOME
        } else if (currentScreen == Screen.HOME || currentScreen == Screen.ELIGIBILITY) {
            currentScreen = Screen.SIGN_IN
        }
    }

    when {
        !isSignedIn -> {
            when (currentScreen) {
                Screen.SIGN_IN -> SignInScreen(
                    uiState = uiState,
                    onSignIn = { email, password -> authViewModel.signInWithEmail(email, password) },
                    onNavigateToSignUp = { currentScreen = Screen.SIGN_UP },
                    onNavigateToResetPassword = { currentScreen = Screen.RESET_PASSWORD },
                    onClearError = { authViewModel.clearError() }
                )
                Screen.SIGN_UP -> SignUpScreen(
                    uiState = uiState,
                    onSignUp = { fullName, email, password -> authViewModel.signUpWithEmail(email, password, fullName) },
                    onNavigateToSignIn = { currentScreen = Screen.SIGN_IN },
                    onClearError = { authViewModel.clearError() }
                )
                Screen.RESET_PASSWORD -> ResetPasswordScreen(
                    uiState = uiState,
                    onSendResetEmail = { email -> authViewModel.sendPasswordResetEmail(email) },
                    onNavigateToSignIn = { currentScreen = Screen.SIGN_IN },
                    onClearError = { authViewModel.clearError() },
                    onClearSuccess = { authViewModel.clearSuccess() }
                )
                else -> SignInScreen(
                    uiState = uiState,
                    onSignIn = { email, password -> authViewModel.signInWithEmail(email, password) },
                    onNavigateToSignUp = { currentScreen = Screen.SIGN_UP },
                    onNavigateToResetPassword = { currentScreen = Screen.RESET_PASSWORD },
                    onClearError = { authViewModel.clearError() }
                )
            }
        }
        else -> {
            when (currentScreen) {
                Screen.HOME -> MainScreen(
                    uiState = uiState,
                    onCheckEligibility = { currentScreen = Screen.ELIGIBILITY },
                    onSignOut = { authViewModel.signOut() },
                    onSendEmailVerification = { authViewModel.sendEmailVerification() },
                    onClearError = { authViewModel.clearError() },
                    onClearSuccess = { authViewModel.clearSuccess() }
                )
                Screen.ELIGIBILITY -> EligibilityScreen(
                    onBack = { currentScreen = Screen.HOME }
                )
                else -> MainScreen(
                    uiState = uiState,
                    onCheckEligibility = { currentScreen = Screen.ELIGIBILITY },
                    onSignOut = { authViewModel.signOut() },
                    onSendEmailVerification = { authViewModel.sendEmailVerification() },
                    onClearError = { authViewModel.clearError() },
                    onClearSuccess = { authViewModel.clearSuccess() }
                )
            }
        }
    }
}

@Composable
fun SignInScreen(
    uiState: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToResetPassword: () -> Unit,
    onClearError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                onClearError()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SnackbarHost(hostState = snackbarHostState)
        
        Text(
            text = "ShuttleReg",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign In",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSignIn(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text("Sign In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToSignUp,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading
            ) {
                Text("Create Account")
            }
            
            OutlinedButton(
                onClick = onNavigateToResetPassword,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading
            ) {
                Text("Forgot Password")
            }
        }
    }
}

@Composable
fun SignUpScreen(
    uiState: AuthUiState,
    onSignUp: (String, String, String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    onClearError: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                onClearError()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SnackbarHost(hostState = snackbarHostState)
        
        Text(
            text = "ShuttleReg",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            isError = password.isNotBlank() && confirmPassword.isNotBlank() && password != confirmPassword
        )

        if (password.isNotBlank() && confirmPassword.isNotBlank() && password != confirmPassword) {
            Text(
                text = "Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSignUp(fullName, email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && 
                     fullName.isNotBlank() && 
                     email.isNotBlank() && 
                     password.isNotBlank() && 
                     password == confirmPassword
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text("Create Account")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToSignIn,
            enabled = !uiState.isLoading
        ) {
            Text("Already have an account? Sign In")
        }
    }
}

@Composable
fun ResetPasswordScreen(
    uiState: AuthUiState,
    onSendResetEmail: (String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                onClearError()
            }
        }
        uiState.successMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                onClearSuccess()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SnackbarHost(hostState = snackbarHostState)
        
        Text(
            text = "ShuttleReg",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter your email address and we'll send you a link to reset your password.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSendResetEmail(email) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && email.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text("Send Reset Email")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToSignIn,
            enabled = !uiState.isLoading
        ) {
            Text("Back to Sign In")
        }
    }
}

@Composable
fun MainScreen(
    uiState: AuthUiState,
    onCheckEligibility: () -> Unit,
    onSignOut: () -> Unit,
    onSendEmailVerification: () -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                onClearError()
            }
        }
        uiState.successMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                onClearSuccess()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SnackbarHost(hostState = snackbarHostState)

        Text(
            text = "ShuttleReg",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        uiState.user?.let { user ->
            Text(
                text = "Welcome, ${user.fullName}!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (!user.isEmailVerified) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Email Not Verified",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please verify your email to access all features.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onSendEmailVerification,
                            enabled = !uiState.isLoading
                        ) {
                            Text("Send Verification Email")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Badminton Tournament Registration App",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🏸 Features Ready:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val features = listOf(
                    "✅ Modern UI with Jetpack Compose",
                    "✅ Age-based event selection",
                    "✅ Firebase Authentication",
                    "✅ Tournament registration flow",
                    "✅ Payment integration ready",
                    "✅ PDF generation utility",
                    "✅ Excel export functionality",
                    "✅ Multi-step registration form"
                )

                features.forEach { feature ->
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCheckEligibility,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Check Eligibility")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Sign Out")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EligibilityScreen(
    onBack: () -> Unit,
    calculateEligibleEvents: (LocalDate, Gender) -> List<EventCategory> = { dob, gender ->
        com.example.shuttlereg.domain.usecase.CalculateEligibleEventsUseCase().invoke(dob, gender)
    }
) {
    var fullName by remember { mutableStateOf("") }
    var year by remember { mutableStateOf(2012f) }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var eligible by remember { mutableStateOf<List<EventCategory>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Eligibility Checker",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Year of Birth: ${year.toInt()}")

        Slider(
            value = year,
            onValueChange = { year = it },
            valueRange = 2000f..2020f,
            steps = 20,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Gender")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Gender.values().forEach { g ->
                val selected = g == gender
                InputChip(
                    selected = selected,
                    onClick = { gender = g },
                    label = { Text(g.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val dob = LocalDate.of(year.toInt(), 1, 1)
                eligible = calculateEligibleEvents(dob, gender)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (eligible.isNotEmpty()) {
            Text(
                "Eligible Categories:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            eligible.forEach {
                Text("• ${it.displayName} (${it.ageLimit})")
            }
        } else {
            Text("No eligible categories yet. Adjust inputs and try again.")
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ShuttleregTheme {
        MainScreen(
            uiState = AuthUiState(),
            onCheckEligibility = {},
            onSignOut = {},
            onSendEmailVerification = {},
            onClearError = {},
            onClearSuccess = {}
        )
    }
}
