package com.virtualstudios.extensionfunctions.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*signingConfigs {
    // Important: change the keystore for a production deployment
    val userKeystore = File(System.getProperty("user.home"), ".android/debug.keystore")
    val localKeystore = rootProject.file("debug_2.keystore")
    val hasKeyInfo = userKeystore.exists()
    create("release") {
        storeFile = if (hasKeyInfo) userKeystore else localKeystore
        storePassword = if (hasKeyInfo) "android" else System.getenv("compose_store_password")
        keyAlias = if (hasKeyInfo) "androiddebugkey" else System.getenv("compose_key_alias")
        keyPassword = if (hasKeyInfo) "android" else System.getenv("compose_key_password")
    }
}*/


/**
 * Support wide screen by making the content width max 840dp, centered horizontally.
 */
fun Modifier.supportWideScreen() = this
    .fillMaxWidth()
    .wrapContentWidth(align = Alignment.CenterHorizontally)
    .widthIn(max = 840.dp)


@Composable
fun FunctionalityNotAvailablePopup(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Text(
                text = "Functionality not available \uD83D\uDE48",
                //style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CLOSE")
            }
        }
    )
}

