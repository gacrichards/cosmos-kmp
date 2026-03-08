package com.gacrichards.cosmos.data.remote.mapper

import com.gacrichards.cosmos.data.remote.dto.ApodDto
import com.gacrichards.cosmos.data.remote.dto.MediaTypeDto
import com.gacrichards.cosmos.domain.model.MediaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ApodMapperTest {

    private val imageDto = ApodDto(
        date = "2024-11-15",
        title = "NGC 1232: A Grand Design Spiral Galaxy",
        explanation = "One of the largest galaxies.",
        mediaType = MediaTypeDto.IMAGE,
        url = "https://apod.nasa.gov/apod/image/2411/ngc1232b_vlt_960.jpg",
        hdUrl = "https://apod.nasa.gov/apod/image/2411/ngc1232b_vlt_4000.jpg",
        copyright = "ESO",
    )

    @Test
    fun `IMAGE mediaType maps to MediaType Image`() {
        assertEquals(MediaType.Image(imageDto.url), imageDto.toDomain().mediaType)
    }

    @Test
    fun `VIDEO mediaType maps to MediaType Video`() {
        val dto = imageDto.copy(mediaType = MediaTypeDto.VIDEO, url = "https://www.youtube.com/embed/abc123")
        assertEquals(MediaType.Video(dto.url), dto.toDomain().mediaType)
    }

    @Test
    fun `UNKNOWN mediaType throws IllegalStateException`() {
        val dto = imageDto.copy(mediaType = MediaTypeDto.UNKNOWN)
        assertFailsWith<IllegalStateException> { dto.toDomain() }
    }

    @Test
    fun `scalar fields are mapped from DTO`() {
        val apod = imageDto.toDomain()
        assertEquals(imageDto.date, apod.date)
        assertEquals(imageDto.title, apod.title)
        assertEquals(imageDto.explanation, apod.explanation)
        assertEquals(imageDto.hdUrl, apod.hdUrl)
    }

    @Test
    fun `copyright whitespace is trimmed`() {
        val apod = imageDto.copy(copyright = "  NASA / ESA  ").toDomain()
        assertEquals("NASA / ESA", apod.copyright)
    }

    @Test
    fun `null copyright stays null`() {
        assertNull(imageDto.copy(copyright = null).toDomain().copyright)
    }

    @Test
    fun `null hdUrl stays null`() {
        assertNull(imageDto.copy(hdUrl = null).toDomain().hdUrl)
    }
}
