package com.azvar.puskar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azvar.puskar.core.designsystem.PuskarTheme
import com.azvar.puskar.domain.model.Job
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.domain.model.WorkMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun JobCard(
    job: Job,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = job.company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (job.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (job.isBookmarked) "Hapus dari simpanan" else "Simpan lowongan",
                        tint = if (job.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${job.location} · ${job.workMode.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(onClick = {}, label = { Text(job.type.label) })
                DeadlineLabel(deadline = job.deadline, today = today)
            }
        }
    }
}

@Composable
private fun DeadlineLabel(deadline: LocalDate, today: LocalDate) {
    val daysLeft = ChronoUnit.DAYS.between(today, deadline)
    val (text, color) = when {
        daysLeft < 0 -> "Ditutup" to MaterialTheme.colorScheme.onSurfaceVariant
        daysLeft == 0L -> "Tutup hari ini" to MaterialTheme.colorScheme.error
        daysLeft <= 7 -> "$daysLeft hari lagi" to MaterialTheme.colorScheme.error
        else -> "$daysLeft hari lagi" to Color.Unspecified
    }
    Text(text = text, style = MaterialTheme.typography.labelMedium, color = color)
}

@Preview(showBackground = true)
@Composable
private fun JobCardPreview() {
    PuskarTheme {
        JobCard(
            job = Job(
                id = "1",
                title = "Android Developer Intern",
                company = "PT Contoh Teknologi",
                location = "Jakarta Selatan",
                type = JobType.INTERNSHIP,
                workMode = WorkMode.HYBRID,
                description = "",
                requirements = emptyList(),
                deadline = LocalDate.now().plusDays(5),
                applyUrl = "",
                isBookmarked = true,
            ),
            onClick = {},
            onBookmarkClick = {},
        )
    }
}
