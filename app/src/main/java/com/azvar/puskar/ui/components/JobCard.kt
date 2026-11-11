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
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azvar.puskar.core.designsystem.PuskarTheme
import com.azvar.puskar.domain.model.Job
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.domain.model.WorkMode
import com.azvar.puskar.domain.model.isClosed
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
    val closed = job.isClosed(today)
    Card(onClick = onClick, modifier = modifier.fillMaxWidth().alpha(if (closed) 0.6f else 1f)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                CompanyLogo(job.company)
                Spacer(Modifier.width(12.dp))
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InfoRow(Icons.Outlined.LocationOn, "${job.location} · ${job.workMode.label}")
                job.salary?.let { InfoRow(Icons.Outlined.Payments, it) }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Tag(job.type.label)
                Text(
                    relativePostedLabel(job.postedAt, today),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                DeadlineLabel(deadline = job.deadline, today = today)
            }
        }
    }
}

/** Small non-interactive label. Uses Surface (not a chip) so screen readers don't announce it as a button. */
@Composable
fun Tag(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun DeadlineLabel(deadline: LocalDate, today: LocalDate) {
    val daysLeft = ChronoUnit.DAYS.between(today, deadline)
    val color = when {
        daysLeft < 0 -> MaterialTheme.colorScheme.onSurfaceVariant
        daysLeft <= 7 -> MaterialTheme.colorScheme.error
        else -> Color.Unspecified
    }
    Text(
        text = deadlineLabel(deadline, today),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = color,
    )
}

@Preview(showBackground = true)
@Composable
private fun JobCardPreview() {
    PuskarTheme {
        JobCard(
            job = Job(
                id = "1",
                title = "Android Developer Intern",
                company = "Kirana Teknologi",
                industry = "Teknologi",
                companyAbout = "",
                location = "Jakarta Selatan",
                type = JobType.INTERNSHIP,
                workMode = WorkMode.HYBRID,
                description = "",
                requirements = emptyList(),
                benefits = emptyList(),
                salary = "Uang saku Rp3,5 jt/bulan",
                postedAt = LocalDate.now().minusDays(3),
                deadline = LocalDate.now().plusDays(5),
                applyUrl = "",
                isBookmarked = true,
            ),
            onClick = {},
            onBookmarkClick = {},
        )
    }
}
