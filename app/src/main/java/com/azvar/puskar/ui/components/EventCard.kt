package com.azvar.puskar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azvar.puskar.core.designsystem.PuskarTheme
import com.azvar.puskar.domain.model.CareerEvent
import com.azvar.puskar.domain.model.EventCategory
import java.time.LocalDateTime

@Composable
fun EventCard(
    event: CareerEvent,
    isPast: Boolean,
    onClick: () -> Unit,
    onReminderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth().alpha(if (isPast) 0.6f else 1f)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Tag(event.category.label)
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = event.organizer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!isPast) {
                    IconButton(onClick = onReminderClick) {
                        Icon(
                            imageVector = if (event.isReminderSet) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsNone,
                            contentDescription = if (event.isReminderSet) "Matikan pengingat" else "Ingatkan saya",
                            tint = if (event.isReminderSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            InfoRow(Icons.Outlined.Schedule, if (isPast) "Selesai · ${event.formattedShortSchedule()}" else event.formattedShortSchedule())
            InfoRow(
                if (event.isOnline) Icons.Outlined.Videocam else Icons.Outlined.LocationOn,
                event.location,
            )
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun EventCardPreview() {
    PuskarTheme {
        EventCard(
            event = CareerEvent(
                id = "e1",
                title = "Job Fair Kampus 2026",
                organizer = "Pusat Karier UIN Jakarta",
                category = EventCategory.JOB_FAIR,
                description = "",
                startAt = LocalDateTime.of(2026, 11, 20, 9, 0),
                endAt = LocalDateTime.of(2026, 11, 20, 15, 0),
                location = "Auditorium Harun Nasution",
                isOnline = false,
                registrationUrl = "",
                isReminderSet = true,
            ),
            isPast = false,
            onClick = {},
            onReminderClick = {},
        )
    }
}
