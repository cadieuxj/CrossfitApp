package com.apexai.crossfit.feature.pr

import app.cash.turbine.test
import com.apexai.crossfit.Fixtures
import com.apexai.crossfit.core.domain.model.PersonalRecord
import com.apexai.crossfit.core.domain.model.PrHistoryEntry
import com.apexai.crossfit.core.domain.model.PrUnit
import com.apexai.crossfit.feature.pr.domain.PrRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for the PR repository contract.
 *
 * These tests verify the expected data contract of [PrRepository]:
 * - PRs are grouped by movement category
 * - Within each category, records are sorted by date (descending)
 * - Empty user has empty PR map
 * - getPrHistory returns entries in ascending date order for trend charting
 *
 * The fake implementation used here mirrors how [PrRepositoryImpl] behaves
 * after mapping from Supabase rows, so these tests validate the contract
 * that the ViewModel depends on.
 *
 * Critical constraint: Android client never computes PRs — all [PersonalRecord]
 * data comes from the PostgreSQL trigger on `results` INSERT. These tests
 * only verify read/grouping behaviour.
 */
class PrRepositoryTest {

    private lateinit var repository: PrRepository
    private val testUserId = "user-test-001"

    private val now      = Instant.parse("2026-03-28T12:00:00Z")
    private val dayAgo   = Instant.parse("2026-03-27T12:00:00Z")
    private val weekAgo  = Instant.parse("2026-03-21T12:00:00Z")
    private val monthAgo = Instant.parse("2026-02-28T12:00:00Z")

    // Fixture PRs spread across multiple categories and dates
    private val prSnatch = Fixtures.personalRecord(
        id           = "pr-snatch",
        movementId   = "mov-snatch",
        movementName = "Snatch",
        category     = "Olympic Lifting",
        value        = 85.0,
        unit         = PrUnit.KG
    ).copy(achievedAt = now)

    private val prCleanJerk = Fixtures.personalRecord(
        id           = "pr-clean",
        movementId   = "mov-clean",
        movementName = "Clean & Jerk",
        category     = "Olympic Lifting",
        value        = 110.0,
        unit         = PrUnit.KG
    ).copy(achievedAt = weekAgo)

    private val prPullUp = Fixtures.personalRecord(
        id           = "pr-pullup",
        movementId   = "mov-pullup",
        movementName = "Pull-up",
        category     = "Gymnastics",
        value        = 25.0,
        unit         = PrUnit.REPS
    ).copy(achievedAt = dayAgo)

    private val prFran = Fixtures.personalRecord(
        id           = "pr-fran",
        movementId   = "mov-fran",
        movementName = "Fran",
        category     = "Benchmark WODs",
        value        = 180.0,
        unit         = PrUnit.SECONDS
    ).copy(achievedAt = monthAgo)

    @Before
    fun setUp() {
        repository = FakePrRepositoryForTest(
            prs = listOf(prSnatch, prCleanJerk, prPullUp, prFran)
        )
    }

    // --------------------------------------------------------
    // getAllPrs — grouping by category
    // --------------------------------------------------------

    @Test
    fun `getAllPrs_multiplePrs_groupedByCategory`() = runTest {
        repository.getAllPrs(testUserId).test {
            val map = awaitItem()
            awaitComplete()

            assertTrue("Olympic Lifting category should exist", map.containsKey("Olympic Lifting"))
            assertTrue("Gymnastics category should exist", map.containsKey("Gymnastics"))
            assertTrue("Benchmark WODs category should exist", map.containsKey("Benchmark WODs"))
        }
    }

    @Test
    fun `getAllPrs_olympicLifting_containsBothLiftingPrs`() = runTest {
        repository.getAllPrs(testUserId).test {
            val map = awaitItem()
            awaitComplete()

            val olympicPrs = map["Olympic Lifting"] ?: emptyList()
            assertEquals("Should have 2 Olympic Lifting PRs", 2, olympicPrs.size)

            val names = olympicPrs.map { it.movementName }
            assertTrue("Snatch should be in Olympic Lifting", "Snatch" in names)
            assertTrue("Clean & Jerk should be in Olympic Lifting", "Clean & Jerk" in names)
        }
    }

    @Test
    fun `getAllPrs_prsReturnedByDateDescending`() = runTest {
        repository.getAllPrs(testUserId).test {
            val map = awaitItem()
            awaitComplete()

            val olympicPrs = map["Olympic Lifting"] ?: emptyList()
            assertEquals("Most recent PR should be first (Snatch at 'now')",
                "Snatch", olympicPrs[0].movementName)
            assertEquals("Older PR should be second (Clean & Jerk at 'weekAgo')",
                "Clean & Jerk", olympicPrs[1].movementName)
        }
    }

    @Test
    fun `getAllPrs_emptyUser_returnsEmptyMap`() = runTest {
        val emptyRepo = FakePrRepositoryForTest(prs = emptyList())
        emptyRepo.getAllPrs(testUserId).test {
            val map = awaitItem()
            awaitComplete()

            assertTrue("No PRs should produce empty map", map.isEmpty())
        }
    }

    @Test
    fun `getAllPrs_singleCategory_mapHasOneKey`() = runTest {
        val singleCategoryRepo = FakePrRepositoryForTest(
            prs = listOf(prSnatch, prCleanJerk) // both Olympic Lifting
        )
        singleCategoryRepo.getAllPrs(testUserId).test {
            val map = awaitItem()
            awaitComplete()

            assertEquals("Should have exactly one category key", 1, map.size)
            assertEquals("Olympic Lifting", map.keys.first())
        }
    }

    @Test
    fun `getAllPrs_prValues_preservedCorrectly`() = runTest {
        repository.getAllPrs(testUserId).test {
            val map = awaitItem()
            awaitComplete()

            val snatch = map["Olympic Lifting"]?.find { it.movementName == "Snatch" }
            assertEquals(85.0, snatch?.value)
            assertEquals(PrUnit.KG, snatch?.unit)
        }
    }

    // --------------------------------------------------------
    // getPrHistory — sorting for trend chart
    // --------------------------------------------------------

    @Test
    fun `getPrHistory_multipleEntries_returnedAscendingByDate`() = runTest {
        val historyEntries = listOf(
            PrHistoryEntry(value = 70.0, unit = PrUnit.KG, achievedAt = weekAgo),
            PrHistoryEntry(value = 80.0, unit = PrUnit.KG, achievedAt = dayAgo),
            PrHistoryEntry(value = 85.0, unit = PrUnit.KG, achievedAt = now)
        )
        val repo = FakePrRepositoryForTest(history = historyEntries)

        repo.getPrHistory(testUserId, "mov-snatch").test {
            val entries = awaitItem()
            awaitComplete()

            assertEquals(3, entries.size)
            // Ascending order for trend chart — oldest first
            assertTrue("First entry should be oldest",
                entries[0].achievedAt < entries[1].achievedAt)
            assertTrue("Second entry should be before third",
                entries[1].achievedAt < entries[2].achievedAt)
        }
    }

    @Test
    fun `getPrHistory_emptyHistory_returnsEmptyList`() = runTest {
        val repo = FakePrRepositoryForTest(history = emptyList())

        repo.getPrHistory(testUserId, "mov-unknown").test {
            val entries = awaitItem()
            awaitComplete()

            assertTrue("Empty movement history should return empty list", entries.isEmpty())
        }
    }

    @Test
    fun `getPrHistory_singleEntry_returnedCorrectly`() = runTest {
        val entry = PrHistoryEntry(value = 85.0, unit = PrUnit.KG, achievedAt = now)
        val repo = FakePrRepositoryForTest(history = listOf(entry))

        repo.getPrHistory(testUserId, "mov-snatch").test {
            val entries = awaitItem()
            awaitComplete()

            assertEquals(1, entries.size)
            assertEquals(85.0, entries[0].value, 0.001)
            assertEquals(PrUnit.KG, entries[0].unit)
        }
    }

    @Test
    fun `getPrHistory_valuesIncrease_trendIsProgressive`() = runTest {
        val history = listOf(
            PrHistoryEntry(value = 60.0, unit = PrUnit.KG, achievedAt = monthAgo),
            PrHistoryEntry(value = 70.0, unit = PrUnit.KG, achievedAt = weekAgo),
            PrHistoryEntry(value = 80.0, unit = PrUnit.KG, achievedAt = now)
        )
        val repo = FakePrRepositoryForTest(history = history)

        repo.getPrHistory(testUserId, "mov-snatch").test {
            val entries = awaitItem()
            awaitComplete()

            val values = entries.map { it.value }
            assertEquals(listOf(60.0, 70.0, 80.0), values)
        }
    }
}

/**
 * In-test fake implementation of [PrRepository] that:
 * - Groups [prs] by category and sorts descending by achievedAt
 * - Returns [history] sorted ascending by achievedAt
 *
 * This simulates exactly what [PrRepositoryImpl] must do after mapping
 * from Supabase rows.
 */
private class FakePrRepositoryForTest(
    private val prs: List<PersonalRecord> = emptyList(),
    private val history: List<PrHistoryEntry> = emptyList()
) : PrRepository {

    override fun getAllPrs(userId: String) = flow {
        val grouped = prs
            .sortedByDescending { it.achievedAt }
            .groupBy { it.category }
        emit(grouped)
    }

    override fun getPrHistory(userId: String, movementId: String) = flow {
        emit(history.sortedBy { it.achievedAt })
    }
}
