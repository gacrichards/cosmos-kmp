package com.gacrichards.cosmos.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.model.MediaType
import com.gacrichards.cosmos.presentation.today.TodayEvent
import com.gacrichards.cosmos.presentation.today.TodayViewModel

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    contentPadding: PaddingValues = PaddingValues(),
    onImageClick: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> TodayScreenSkeleton(contentPadding = contentPadding)
        is UiState.Error -> ErrorContent(
            message = state.message,
            onRetry = { viewModel.onEvent(TodayEvent.Retry) },
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        )
        is UiState.Success -> TodayContent(
            apod = state.data,
            onImageClick = { onImageClick(state.data.date) },
            modifier = Modifier.padding(contentPadding),
        )
    }
}

@Composable
private fun TodayContent(
    apod: Apod,
    onImageClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        when (val media = apod.mediaType) {
            is MediaType.Image -> AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(media.url)
                    .crossfade(true)
                    .build(),
                contentDescription = apod.title,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 240.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onImageClick),
            )
            is MediaType.Video -> VideoPlaceholder(url = media.url)
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = apod.title,
                style = MaterialTheme.typography.headlineSmall,
            )
            apod.copyright?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "© $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = apod.explanation,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun VideoPlaceholder(url: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "Video content",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Preview(showBackground = true, name = "Today — Loading")
@Composable
private fun TodayScreenLoadingPreview() {
    MaterialTheme {
        TodayScreenSkeleton()
    }
}

@Preview(showBackground = true, name = "Today — Error")
@Composable
private fun TodayScreenErrorPreview() {
    MaterialTheme {
        ErrorContent(
            message = "Failed to load. Check your connection.",
            onRetry = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(showBackground = true, name = "Today — Image APOD")
@Composable
private fun TodayScreenImagePreview() {
    MaterialTheme {
        TodayContent(apod = previewApod)
    }
}

@Preview(showBackground = true, name = "Today — Video APOD")
@Composable
private fun TodayScreenVideoPreview() {
    MaterialTheme {
        TodayContent(
            apod = previewApod.copy(
                title = "A Perseid Meteor Shower Timelapse",
                mediaType = MediaType.Video("https://www.youtube.com/embed/example"),
                copyright = null,
            ),
        )
    }
}

private val previewApod = Apod(
    date = "2024-11-15",
    title = "NGC 1232: A Grand Design Spiral Galaxy",
    explanation = "One of the largest galaxies visible in the night sky, NGC 1232 displays " +
            "winding spiral arms dotted with young blue stars and a bright yellow central core " +
            "made up of older, cooler stars.",
    mediaType = MediaType.Image("https://apod.nasa.gov/apod/image/2411/ngc1232b_vlt_960.jpg"),
    hdUrl = "https://apod.nasa.gov/apod/image/2411/ngc1232b_vlt_4000.jpg",
    copyright = "FORS, 8.2-meter VLT Antu, ESO",
)
