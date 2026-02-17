package com.example.splitify.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.splitify.presentation.components.SplitifyAppBar
import com.example.splitify.presentation.theme.NeutralColors
import com.example.splitify.presentation.theme.PrimaryColors

@Composable
fun AppSettingsScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        topBar = {
            SplitifyAppBar(
                title = "App Settings",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralColors.Neutral50)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notifications Section
            ProfileSection(title = "Notifications") {
                Column {
                    SettingSwitchItem(
                        title = "Push Notifications",
                        subtitle = "Receive notifications about expenses and settlements",
                        checked = appSettings.notificationsEnabled,
                        onCheckedChange = { viewModel.updateNotifications(it) }
                    )
                }
            }

            // Currency Section
            ProfileSection(title = "Currency") {
                Column {
                    val currencies = listOf(
                        "USD" to "$",
                        "EUR" to "€",
                        "GBP" to "£",
                        "INR" to "₹",
                        "JPY" to "¥",
                        "CAD" to "C$",
                        "AUD" to "A$"
                    )

                    currencies.forEachIndexed { index, (code, symbol) ->
                        SettingRadioItem(
                            title = code,
                            subtitle = symbol,
                            selected = appSettings.currency == code,
                            onClick = { viewModel.updateCurrency(code) }
                        )

                        if (index < currencies.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = NeutralColors.Neutral200
                            )
                        }
                    }
                }
            }

            // Theme Section
            ProfileSection(title = "Theme") {
                Column {
                    val themes = listOf(
                        "System" to "Follow system settings",
                        "Light" to "Always use light theme",
                        "Dark" to "Always use dark theme"
                    )

                    themes.forEachIndexed { index, (theme, description) ->
                        SettingRadioItem(
                            title = theme,
                            subtitle = description,
                            selected = appSettings.theme == theme,
                            onClick = { viewModel.updateTheme(theme) }
                        )

                        if (index < themes.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = NeutralColors.Neutral200
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = NeutralColors.Neutral900
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralColors.Neutral600
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryColors.Primary600,
                checkedTrackColor = PrimaryColors.Primary200
            )
        )
    }
}

@Composable
fun SettingRadioItem(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) PrimaryColors.Primary600 else NeutralColors.Neutral900
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralColors.Neutral600
            )
        }

        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = PrimaryColors.Primary600
            )
        )
    }
}