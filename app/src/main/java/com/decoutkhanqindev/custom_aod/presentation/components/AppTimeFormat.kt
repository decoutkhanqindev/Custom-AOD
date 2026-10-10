package com.decoutkhanqindev.custom_aod.presentation.components

import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.decoutkhanqindev.custom_aod.R
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun is24HourFormat(): Boolean = DateFormat.is24HourFormat(LocalContext.current)

@Composable
fun rememberTimeFormatter(): DateTimeFormatter = rememberFormatter(
    if (is24HourFormat()) R.string.aod_time_pattern_24h else R.string.aod_time_pattern_12h,
)

@Composable
fun rememberFormatter(@StringRes patternRes: Int): DateTimeFormatter {
    val locale = LocalConfiguration.current.locales[0]
    val pattern = stringResource(patternRes)
    return remember(pattern, locale) { DateTimeFormatter.ofPattern(pattern, locale) }
}

@Composable
fun rememberZonedDateTime(epochMillis: Long): ZonedDateTime = remember(epochMillis) {
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
}
