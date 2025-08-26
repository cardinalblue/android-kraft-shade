package com.cardinalblue.kraftshade.demo.ui.components

import android.content.Context
import android.graphics.fonts.FontStyle
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NetworkAwareScreen(
    additionalDescription: String? = null,
    contentWhenNetworkIsAvailable: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkAvailable = remember {
        mutableStateOf(isNetworkAvailable(connectivityManager))
    }

    // Observe network changes (optional improvement)
    DisposableEffect(Unit) {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                networkAvailable.value = true
            }

            override fun onLost(network: Network) {
                networkAvailable.value = false
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        // Cleanup
        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    if (networkAvailable.value) {
        contentWhenNetworkIsAvailable()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "No internet connection available",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            if (additionalDescription != null) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = additionalDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}