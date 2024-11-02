package com.example.feature.login


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun LoginScreen(
    onShowSnackbar: (msg: String) -> Unit, onNavigateToRegister: () -> Unit,
    onNavigateToProducts: () -> Unit, viewModel: LoginViewModel = hiltViewModel()
) {


    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current

    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val passwordFocusRequester = remember { FocusRequester() }



    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onShowSnackbar("login success")
            onNavigateToProducts()
            viewModel.resetLoginSuccess()
        }
    }

    LaunchedEffect(uiState.loginErrorMessage) {
        uiState.loginErrorMessage?.let { msg ->
            onShowSnackbar(msg)
            viewModel.resetLoginErrorMessage()
        }
    }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.2f))

            Text(
                "Login",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(LoginScreenEvent.EmailChanged(it)) },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        passwordFocusRequester.requestFocus()
                    }
                )
            )


            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onEvent(LoginScreenEvent.PasswordChanged(it)) },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                shape = RoundedCornerShape(8.dp),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Icon(
                        painter = if (isPasswordVisible) painterResource(R.drawable.icon_visible) else painterResource(
                            R.drawable.icon_invisible
                        ), contentDescription = "",
                        modifier = Modifier.clickable { isPasswordVisible = !isPasswordVisible }
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.clearFocus()
                    }
                )
            )

            Spacer(Modifier.height(16.dp))

            ElevatedButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onEvent(LoginScreenEvent.Login)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.email.isNotEmpty() && uiState.password.isNotEmpty() && !uiState.isLoading,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Cyan.copy(alpha = 0.7f)
                )

            ) {
                Text("Login")

            }

            Spacer(Modifier.height(20.dp))

            Text("Don't have Account ?")
            TextButton(onClick = onNavigateToRegister) {
                Text("Register")
            }

        }
    }


}