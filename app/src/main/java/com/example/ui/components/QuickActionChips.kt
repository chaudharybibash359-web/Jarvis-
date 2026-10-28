package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisTextPrimary

data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val command: String
)

@Composable
fun QuickActionChips(
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        QuickAction("Flashlight", Icons.Default.FlashlightOn, "turn on flashlight"),
        QuickAction("YouTube", Icons.Default.SmartDisplay, "open youtube"),
        QuickAction("Camera", Icons.Default.PhotoCamera, "open camera"),
        QuickAction("7 AM Alarm", Icons.Default.Alarm, "set alarm for 7:00 am"),
        QuickAction("Search Web", Icons.Default.Search, "search the web for artificial intelligence"),
        QuickAction("System Diagnostic", Icons.Default.Info, "run system diagnostic")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { action ->
            AssistChip(
                onClick = { onActionClick(action.command) },
                label = { Text(action.label, fontSize = 12.sp, color = JarvisTextPrimary) },
                leadingIcon = {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = JarvisCardBg.copy(alpha = 0.8f)
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = JarvisCyan.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
