package com.example.model

import androidx.compose.ui.graphics.Color

enum class ScrewColor(
    val displayName: String,
    val baseColor: Color,
    val lightColor: Color,
    val darkColor: Color,
    val specularColor: Color
) {
    RED(
        displayName = "Red",
        baseColor = Color(0xFFE11D48),
        lightColor = Color(0xFFFB7185),
        darkColor = Color(0xFF9F1239),
        specularColor = Color(0xFFFFD1DC)
    ),
    BLUE(
        displayName = "Blue",
        baseColor = Color(0xFF2563EB),
        lightColor = Color(0xFF60A5FA),
        darkColor = Color(0xFF1E40AF),
        specularColor = Color(0xFFDBEAFE)
    ),
    GREEN(
        displayName = "Green",
        baseColor = Color(0xFF16A34A),
        lightColor = Color(0xFF4ADE80),
        darkColor = Color(0xFF166534),
        specularColor = Color(0xFFDCFCE7)
    ),
    YELLOW(
        displayName = "Yellow",
        baseColor = Color(0xFFD97706),
        lightColor = Color(0xFFFBBF24),
        darkColor = Color(0xFF92400E),
        specularColor = Color(0xFFFEF3C7)
    ),
    PURPLE(
        displayName = "Purple",
        baseColor = Color(0xFF7C3AED),
        lightColor = Color(0xFFA78BFA),
        darkColor = Color(0xFF5B21B6),
        specularColor = Color(0xFFEDE9FE)
    ),
    ORANGE(
        displayName = "Orange",
        baseColor = Color(0xFFEA580C),
        lightColor = Color(0xFFFB923C),
        darkColor = Color(0xFF9A3412),
        specularColor = Color(0xFFFFEDD5)
    );

    companion object {
        fun fromIndex(index: Int): ScrewColor {
            val entries = entries
            return entries[index % entries.size]
        }
    }
}
