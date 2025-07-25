package com.zen.zukodosample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import co.hyperverge.hyperkyc.HyperKyc
import co.hyperverge.hyperkyc.data.models.HyperKycConfig
import co.hyperverge.hyperkyc.data.models.result.HyperKycStatus
import com.zen.zukodosample.ui.theme.ZukodoSampleTheme
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZukodoSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Sample(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Sample(modifier: Modifier = Modifier) {
    val hyperKycConfig = remember {
        HyperKycConfig(
            "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6ImNjZXlleiIsImhhc2giOiI3NTg3NDYxZGJkMjY1OTZlZjNkYjNlYmVhYjZiNjlkZmViNjE3ZDBkZjRiNzQxY2RmNDFkYmYzNDYzYmNkOGVjIiwiaWF0IjoxNzUzNDM2MDM0LCJleHAiOjE3NTM0MzcwMzQsImp0aSI6IjQwNTBhNDdjLTQwNjItNDIyNi05MzM3LWVhYzcyODIwM2IxMyJ9.GQKtZxL1cK6VY9SYlGvFgh6w1LwcGcZ8nX6VJZYYWpU75NUAc89b1t8QMHTtMFPEPcF6Fi3QP04n28oqhLR0TRSNAaoRIM5e7o1XcZ8ubnZvxPLi5wR6X4rCGre5NqYtOxVWryxU1hR_lb0lkXR2JIWV6A0hH8xaOa6MAOy1lRk",
            "zokudo_kyc_revamp",
            UUID.randomUUID().toString()
        )
    }
    hyperKycConfig.setUseLocation(true)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(HyperKyc.Contract()) { result ->
        when (result.status) {
            HyperKycStatus.USER_CANCELLED -> {
                Log.d("Zaggle", "User cancelled the KYC process")
            }

            HyperKycStatus.ERROR -> {
                Log.e(
                    "Zaggle",
                    "Error occurred during KYC process: ${result.errorMessage} | ${result.errorCode}"
                )
            }

            HyperKycStatus.AUTO_APPROVED,
            HyperKycStatus.AUTO_DECLINED,
            HyperKycStatus.NEEDS_REVIEW,
                -> {
                Log.d("Zaggle", "KYC process completed with status: ${result.status}")
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Please enable location permission to continue")
            }
        } else {
            launcher.launch(hyperKycConfig)
        }
    }
    Button(
        modifier = modifier,
        onClick = {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                launcher.launch(hyperKycConfig)
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    ) { Text("Click MEEE!") }
}