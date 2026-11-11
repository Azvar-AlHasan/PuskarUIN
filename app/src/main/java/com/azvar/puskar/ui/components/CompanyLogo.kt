package com.azvar.puskar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private val LogoPalette = listOf(
    Color(0xFF1A73E8), Color(0xFF188038), Color(0xFFE37400), Color(0xFFD93025),
    Color(0xFF9334E6), Color(0xFF007B83), Color(0xFFC5221F), Color(0xFF185ABC),
)

/** Monogram logo with a stable color per company, like Gmail's sender avatars. */
@Composable
fun CompanyLogo(name: String, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val color = LogoPalette[abs(name.hashCode()) % LogoPalette.size]
    val initials = name.split(' ')
        .filter { it.firstOrNull()?.isLetter() == true }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.36f).sp,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
