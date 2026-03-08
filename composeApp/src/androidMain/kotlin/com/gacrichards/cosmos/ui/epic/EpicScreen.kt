package com.gacrichards.cosmos.ui.epic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gacrichards.cosmos.domain.UiState
import com.gacrichards.cosmos.domain.model.EpicImage
import com.gacrichards.cosmos.presentation.epic.EpicEvent
import com.gacrichards.cosmos.presentation.epic.EpicUiState
import com.gacrichards.cosmos.presentation.epic.EpicViewModel

@Composable
fun EpicScreen(viewModel: EpicViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    EpicContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
private fun EpicContent(
    uiState: EpicUiState,
    onEvent: (EpicEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Date navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { onEvent(EpicEvent.PreviousDate) },
                enabled = uiState.canGoPrevious,
            ) {
                Text("‹", fontSize = 24.sp)
            }

            Text(
                text = uiState.currentDate ?: "Loading…",
                style = MaterialTheme.typography.titleMedium,
            )

            TextButton(
                onClick = { onEvent(EpicEvent.NextDate) },
                enabled = uiState.canGoNext,
            ) {
                Text("›", fontSize = 24.sp)
            }
        }

        // Image grid
        when (val imagesState = uiState.images) {
            is UiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            is UiState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(
                        text = imagesState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Button(
                        onClick = { onEvent(EpicEvent.Retry) },
                        modifier = Modifier.padding(top = 16.dp),
                    ) {
                        Text("Retry")
                    }
                }
            }
            is UiState.Success -> {
                if (imagesState.data.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No images for ${uiState.currentDate}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(imagesState.data, key = { it.identifier }) { image ->
                            EpicImageItem(image = image)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EpicImageItem(image: EpicImage) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(image.thumbnailUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Earth from EPIC — ${image.date}",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Preview(showBackground = true, name = "EPIC — Loading")
@Composable
private fun EpicScreenLoadingPreview() {
    MaterialTheme {
        EpicContent(
            uiState = EpicUiState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "EPIC — Images")
@Composable
private fun EpicScreenImagesPreview() {
    MaterialTheme {
        EpicContent(
            uiState = EpicUiState(
                availableDates = listOf("2024-01-15", "2024-01-14", "2024-01-13"),
                currentDateIndex = 0,
                images = UiState.Success(previewImages),
            ),
            onEvent = {},
        )
    }
}

private val previewImages = List(6) { index ->
    EpicImage(
        identifier = "preview_$index",
        date = "2024-01-15",
        imageName = "epic_1b_20240115000000",
        caption = "Earth from DSCOVR EPIC",
        thumbnailUrl = "",
        fullUrl = "",
    )
}
