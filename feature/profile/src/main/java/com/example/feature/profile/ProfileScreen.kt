package com.example.feature.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(), onNavigateToOrders: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onShowSnackbar: (msg: String) -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(
        uiState.logoutSucceed,
        uiState.snackbarMessage
    ) {
        uiState.snackbarMessage?.let {
            onShowSnackbar(it)
            viewModel.resetSnackbarMessage()
        }

        if (uiState.logoutSucceed) {
            onNavigateToLogin()
            viewModel.resetLogoutSucceeded()
        }


    }

    val circleSize = (LocalConfiguration.current.screenHeightDp / 5).dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top

        ) {
            Spacer(Modifier.fillMaxHeight(0.1f))

            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(Color.LightGray), contentAlignment = Alignment.Center
            ) {
                val userNameToCircle = uiState.userName.take(2).uppercase(Locale.ROOT)
                Text(userNameToCircle, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onNavigateToOrders,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                val orderSize = uiState.orders.size.toString()
                Text(
                    "Orders : $orderSize order",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    color = Color.Black
                )
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.userName,
                onValueChange = {}, shape = RoundedCornerShape(16.dp),
                label = { Text("Username") }, readOnly = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = {}, shape = RoundedCornerShape(16.dp),
                label = { Text("Email") },
                readOnly = true
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = { viewModel.onEvent(ProfileScreenEvent.Logout) },
                modifier = Modifier.fillMaxWidth(0.5f),
            ) {
                Text("Logout")
            }


        }

    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenTopBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(title = { Text("Profile") })

}