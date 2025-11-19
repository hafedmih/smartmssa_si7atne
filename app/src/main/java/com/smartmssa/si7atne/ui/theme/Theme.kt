// In ui/theme/Theme.kt

package com.smartmssa.si7atne.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Define the Light Color Scheme based on the logo ---
private val LightColorScheme = lightColorScheme(
    primary = Si7aOrange,              // Buttons, active indicators
    secondary = Si7aBlue,             // Floating action buttons, highlights
    tertiary = Si7aBlue,              // Accents
    background = Color.White,         // Screen background
    surface = Si7aLightGray,          // Card backgrounds, surfaces
    onPrimary = Color.White,          // Text on primary buttons
    onSecondary = Color.White,        // Text on secondary elements
    onTertiary = Color.White,
    onBackground = Si7aDarkText,      // Main body text color
    onSurface = Si7aDarkText,         // Text on cards
)

// --- Optional: Define a Dark Color Scheme ---
// You can customize this further if needed
private val DarkColorScheme = darkColorScheme(
    primary = Si7aOrange,
    secondary = Si7aBlue,
    tertiary = Si7aBlue,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2C2C2C),
    onPrimary = Si7aDarkText,
    onSecondary = Si7aDarkText,
    onTertiary = Si7aDarkText,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)

@Composable
fun Si7atneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,

    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // --- Use our custom schemes ---
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assumes Typography.kt is set up
        content = content
    )
}