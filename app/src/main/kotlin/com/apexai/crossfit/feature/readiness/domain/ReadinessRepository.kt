package com.apexai.crossfit.feature.readiness.domain

import com.apexai.crossfit.core.domain.model.HealthSnapshot
import com.apexai.crossfit.core.domain.model.ReadinessScore
import kotlinx.coroutines.flow.Flow

interface ReadinessRepository {
    fun getReadinessScore(userId: String): Flow<ReadinessScore>
    fun getReadinessHistory(userId: String, days: Int): Flow<List<ReadinessScore>>
    suspend fun syncHealthSnapshot(snapshot: HealthSnapshot): Result<Unit>
    suspend fun checkHealthConnectPermissions(): Boolean
}
