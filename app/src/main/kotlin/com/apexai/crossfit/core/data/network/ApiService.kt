package com.apexai.crossfit.core.data.network

import com.apexai.crossfit.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AnalyzeVideoRequest(
    val videoId: String,
    val movementType: String,
    val athleteId: String
)

@Serializable
data class AnalyzeVideoResponse(
    val analysisId: String,
    val status: String
)

@Singleton
class FastApiService @Inject constructor(
    private val httpClient: HttpClient
) {
    private val baseUrl = BuildConfig.FASTAPI_BASE_URL

    /**
     * Kicks off Gemini video analysis on the FastAPI microservice.
     * The video must already be uploaded to Supabase Storage before calling this.
     */
    suspend fun analyzeVideo(
        videoId: String,
        movementType: String,
        athleteId: String,
        accessToken: String
    ): AnalyzeVideoResponse {
        return httpClient.post("$baseUrl/api/v1/coaching/analyze") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(AnalyzeVideoRequest(videoId, movementType, athleteId))
        }.body()
    }

    /**
     * Uploads a video file directly to FastAPI as multipart.
     * Used as an alternative path if bypassing Supabase Storage.
     */
    suspend fun uploadVideoMultipart(
        videoBytes: ByteArray,
        fileName: String,
        movementType: String,
        accessToken: String
    ): AnalyzeVideoResponse {
        return httpClient.post("$baseUrl/api/v1/coaching/upload") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("movement_type", movementType)
                        append(
                            key = "video",
                            value = videoBytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, "video/mp4")
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            }
                        )
                    }
                )
            )
        }.body()
    }
}
