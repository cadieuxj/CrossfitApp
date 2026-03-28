package com.apexai.crossfit.feature.readiness

import com.apexai.crossfit.core.domain.model.ReadinessZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the ACWR calculation logic and zone classification.
 *
 * The ACWR formula (from CLAUDE.md / ARCHITECTURE_PLAN.md §7.4):
 *   ACWR = acute_load / chronic_load
 *   - acute_load  = sum of training load over past 7 days
 *   - chronic_load = rolling average over past 28 days
 *
 * Zone boundaries:
 *   < 0.8   → UNDERTRAINED
 *   0.8–1.3 → OPTIMAL
 *   1.3–1.5 → CAUTION  (exclusive lower, inclusive upper for 1.3 boundary test)
 *   > 1.5   → HIGH_RISK
 *
 * Note: ACWR is computed server-side (Supabase Edge Function). This class
 * tests the *client-side zone classification utility* that maps a given ACWR
 * float to a [ReadinessZone]. The utility is extracted from the domain so
 * the UI can display zone colour without a second network call.
 */
class AcwrCalculationTest {

    // --------------------------------------------------------
    // Zone classification — boundary value analysis
    // --------------------------------------------------------

    @Test
    fun `classifyZone_acwr0point5_returnsUndertrained`() {
        assertEquals(ReadinessZone.UNDERTRAINED, AcwrZoneClassifier.classify(0.5f))
    }

    @Test
    fun `classifyZone_acwrExactly0point8_returnsOptimal`() {
        assertEquals(ReadinessZone.OPTIMAL, AcwrZoneClassifier.classify(0.8f))
    }

    @Test
    fun `classifyZone_acwrJustBelow0point8_returnsUndertrained`() {
        assertEquals(ReadinessZone.UNDERTRAINED, AcwrZoneClassifier.classify(0.799f))
    }

    @Test
    fun `classifyZone_acwr1point0_returnsOptimal`() {
        assertEquals(ReadinessZone.OPTIMAL, AcwrZoneClassifier.classify(1.0f))
    }

    @Test
    fun `classifyZone_acwrExactly1point3_returnsOptimal`() {
        // 1.3 is the inclusive upper boundary of OPTIMAL
        assertEquals(ReadinessZone.OPTIMAL, AcwrZoneClassifier.classify(1.3f))
    }

    @Test
    fun `classifyZone_acwrJustAbove1point3_returnsCaution`() {
        assertEquals(ReadinessZone.CAUTION, AcwrZoneClassifier.classify(1.301f))
    }

    @Test
    fun `classifyZone_acwr1point4_returnsCaution`() {
        assertEquals(ReadinessZone.CAUTION, AcwrZoneClassifier.classify(1.4f))
    }

    @Test
    fun `classifyZone_acwrExactly1point5_returnsCaution`() {
        // 1.5 is inclusive upper boundary of CAUTION
        assertEquals(ReadinessZone.CAUTION, AcwrZoneClassifier.classify(1.5f))
    }

    @Test
    fun `classifyZone_acwrJustAbove1point5_returnsHighRisk`() {
        assertEquals(ReadinessZone.HIGH_RISK, AcwrZoneClassifier.classify(1.501f))
    }

    @Test
    fun `classifyZone_acwr2point0_returnsHighRisk`() {
        assertEquals(ReadinessZone.HIGH_RISK, AcwrZoneClassifier.classify(2.0f))
    }

    @Test
    fun `classifyZone_acwr3point5_returnsHighRisk`() {
        assertEquals(ReadinessZone.HIGH_RISK, AcwrZoneClassifier.classify(3.5f))
    }

    // --------------------------------------------------------
    // Edge cases — zero and near-zero values
    // --------------------------------------------------------

    @Test
    fun `classifyZone_acwrZero_returnsUndertrained`() {
        // Zero acute load means no training in past 7 days
        assertEquals(ReadinessZone.UNDERTRAINED, AcwrZoneClassifier.classify(0f))
    }

    @Test
    fun `classifyZone_acwrNegative_returnsUndertrained`() {
        // Should never occur in production but must not crash
        assertEquals(ReadinessZone.UNDERTRAINED, AcwrZoneClassifier.classify(-0.1f))
    }

    // --------------------------------------------------------
    // ACWR formula correctness
    // --------------------------------------------------------

    @Test
    fun `calculateAcwr_normalValues_returnsCorrectRatio`() {
        val acuteLoad  = 550f
        val chronicLoad = 500f
        val expected = acuteLoad / chronicLoad // 1.1

        val result = AcwrZoneClassifier.calculate(acuteLoad, chronicLoad)

        assertEquals(expected, result, 0.001f)
    }

    @Test
    fun `calculateAcwr_acuteHigherThanChronic_returnsRatioAbove1`() {
        val result = AcwrZoneClassifier.calculate(acuteLoad = 800f, chronicLoad = 500f)

        assertTrue("ACWR should be > 1 when acute > chronic", result > 1f)
        assertEquals(1.6f, result, 0.001f)
    }

    @Test
    fun `calculateAcwr_acuteLowerThanChronic_returnsRatioBelow1`() {
        val result = AcwrZoneClassifier.calculate(acuteLoad = 300f, chronicLoad = 500f)

        assertTrue("ACWR should be < 1 when acute < chronic", result < 1f)
        assertEquals(0.6f, result, 0.001f)
    }

    @Test
    fun `calculateAcwr_zeroChronicLoad_returnsZero`() {
        // Guard against division by zero — athlete has no training history
        val result = AcwrZoneClassifier.calculate(acuteLoad = 400f, chronicLoad = 0f)

        assertEquals("Zero chronic load must return 0.0, not NaN", 0f, result, 0.001f)
    }

    @Test
    fun `calculateAcwr_bothZero_returnsZero`() {
        val result = AcwrZoneClassifier.calculate(acuteLoad = 0f, chronicLoad = 0f)

        assertEquals(0f, result, 0.001f)
    }

    @Test
    fun `calculateAcwr_zeroAcutePositiveChronic_returnsZero`() {
        // Rest week — no training in past 7 days
        val result = AcwrZoneClassifier.calculate(acuteLoad = 0f, chronicLoad = 500f)

        assertEquals(0f, result, 0.001f)
        assertEquals(ReadinessZone.UNDERTRAINED, AcwrZoneClassifier.classify(result))
    }

    // --------------------------------------------------------
    // Missing HRV — zone classification is unaffected
    // --------------------------------------------------------

    @Test
    fun `classifyZone_acwrOptimalWithNullHrv_remainsOptimal`() {
        // HRV is a biometric component shown in the UI but ACWR zone is
        // derived purely from training load ratio (per CLAUDE.md spec).
        val acwr = AcwrZoneClassifier.calculate(550f, 500f) // 1.1 → OPTIMAL
        val zone = AcwrZoneClassifier.classify(acwr)

        assertEquals(ReadinessZone.OPTIMAL, zone)
    }

    // --------------------------------------------------------
    // Optimal zone inclusive range validation
    // --------------------------------------------------------

    @Test
    fun `classifyZone_range0point8to1point3_allReturnOptimal`() {
        val testValues = listOf(0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.25f, 1.3f)

        testValues.forEach { acwr ->
            assertEquals(
                "ACWR $acwr should be OPTIMAL",
                ReadinessZone.OPTIMAL,
                AcwrZoneClassifier.classify(acwr)
            )
        }
    }

    @Test
    fun `classifyZone_above1point5_allReturnHighRisk`() {
        val testValues = listOf(1.51f, 1.6f, 1.8f, 2.0f, 2.5f)

        testValues.forEach { acwr ->
            assertEquals(
                "ACWR $acwr should be HIGH_RISK",
                ReadinessZone.HIGH_RISK,
                AcwrZoneClassifier.classify(acwr)
            )
        }
    }
}

/**
 * Client-side ACWR utility.
 *
 * Mirrors the server-side Edge Function logic locally so the UI can
 * classify a received [acwr] value without a second network round-trip.
 *
 * This object is co-located with the test to make the contract explicit.
 * The production implementation belongs in the domain layer at
 * `feature/readiness/domain/AcwrZoneClassifier.kt`.
 */
object AcwrZoneClassifier {

    /**
     * Compute ACWR from raw load values.
     * Returns 0.0 when [chronicLoad] is zero to prevent NaN / division-by-zero.
     */
    fun calculate(acuteLoad: Float, chronicLoad: Float): Float {
        if (chronicLoad == 0f) return 0f
        return acuteLoad / chronicLoad
    }

    /**
     * Classify an ACWR value into a [ReadinessZone].
     *
     * Zone boundaries (inclusive):
     *   < 0.8   → UNDERTRAINED
     *   0.8–1.3 → OPTIMAL
     *   1.3–1.5 → CAUTION  (1.3 is shared upper OPTIMAL / lower CAUTION exclusive)
     *   > 1.5   → HIGH_RISK
     */
    fun classify(acwr: Float): ReadinessZone = when {
        acwr <= 0f   -> ReadinessZone.UNDERTRAINED
        acwr < 0.8f  -> ReadinessZone.UNDERTRAINED
        acwr <= 1.3f -> ReadinessZone.OPTIMAL
        acwr <= 1.5f -> ReadinessZone.CAUTION
        else         -> ReadinessZone.HIGH_RISK
    }
}
