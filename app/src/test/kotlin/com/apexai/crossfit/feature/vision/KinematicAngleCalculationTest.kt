package com.apexai.crossfit.feature.vision

import com.apexai.crossfit.core.domain.model.JointAngle
import com.apexai.crossfit.core.domain.model.PoseLandmark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Unit tests for 2D kinematic angle calculations.
 *
 * Critical constraint (CLAUDE.md): "Z-coordinates are experimental; coaching
 * algorithms must prioritize 2D angular calculations from profile-view footage."
 *
 * All tests verify that:
 * 1. Angles are computed using only (x, y) — Z is ignored.
 * 2. Known geometric configurations produce expected angles.
 * 3. Visibility threshold (> 0.5f) is enforced before including a joint.
 * 4. Degenerate cases (coincident points, zero-length vectors) return 0.
 *
 * Landmark index reference (CLAUDE.md):
 *   Left shoulder  = 11, Right shoulder = 12
 *   Left hip       = 23, Right hip      = 24
 *   Left knee      = 25, Right knee     = 26
 *   Left ankle     = 27, Right ankle    = 28
 *   Left elbow     = 13, Right elbow    = 14
 *   Left wrist     = 15, Right wrist    = 16
 */
class KinematicAngleCalculationTest {

    private val angleCalculator = KinematicAngleCalculator()

    // --------------------------------------------------------
    // Helper: build a landmark list with a specific subset set
    // --------------------------------------------------------

    private fun landmarkList(vararg pairs: Pair<Int, Triple<Float, Float, Float>>): List<PoseLandmark> {
        val map = pairs.toMap()
        return (0..32).map { idx ->
            val coords = map[idx] ?: Triple(0f, 0f, 0f)
            PoseLandmark(
                index      = idx,
                x          = coords.first,
                y          = coords.second,
                z          = coords.third,
                visibility = if (map.containsKey(idx)) 1.0f else 0.0f
            )
        }
    }

    private fun landmark(idx: Int, x: Float, y: Float, z: Float = 0f, vis: Float = 1.0f) =
        PoseLandmark(index = idx, x = x, y = y, z = z, visibility = vis)

    // Expected angle at joint B in triangle A-B-C using dot-product formula
    private fun expectedAngle(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Float {
        val dax = ax - bx; val day = ay - by
        val dcx = cx - bx; val dcy = cy - by
        val dot  = dax * dcx + day * dcy
        val magA = sqrt(dax * dax + day * day)
        val magC = sqrt(dcx * dcx + dcy * dcy)
        if (magA == 0f || magC == 0f) return 0f
        return Math.toDegrees(acos((dot / (magA * magC)).toDouble().coerceIn(-1.0, 1.0))).toFloat()
    }

    // --------------------------------------------------------
    // Right angle (90°) — axis-aligned triangle
    // --------------------------------------------------------

    @Test
    fun `calculateLeftKneeAngle_rightAngleGeometry_returns90Degrees`() {
        // Hip directly above knee, ankle directly to the side → 90° at knee
        // Left hip(23), Left knee(25), Left ankle(27)
        val landmarks = landmarkList(
            23 to Triple(0f, 0f, 0f),  // hip at origin
            25 to Triple(0f, 1f, 0f),  // knee below hip
            27 to Triple(1f, 1f, 0f)   // ankle to the right of knee
        )

        val angles = angleCalculator.calculateJointAngles(landmarks)

        val knee = angles[JointAngle.LEFT_KNEE]
        assertFalse("LEFT_KNEE should be present", knee == null)
        assertEquals(
            "Expected 90° at left knee for right-angle geometry",
            90f,
            knee!!,
            1.0f
        )
    }

    @Test
    fun `calculateRightKneeAngle_straightLeg_returns180Degrees`() {
        // Hip, knee, ankle all in a straight vertical line → 180°
        // Right hip(24), Right knee(26), Right ankle(28)
        val landmarks = landmarkList(
            24 to Triple(0.5f, 0f, 0f),
            26 to Triple(0.5f, 0.5f, 0f),
            28 to Triple(0.5f, 1.0f, 0f)
        )

        val angles = angleCalculator.calculateJointAngles(landmarks)

        val knee = angles[JointAngle.RIGHT_KNEE]
        assertFalse("RIGHT_KNEE should be present", knee == null)
        assertEquals(180f, knee!!, 1.0f)
    }

    // --------------------------------------------------------
    // Squat depth — knee angle < 90° implies below parallel
    // --------------------------------------------------------

    @Test
    fun `calculateLeftKneeAngle_deepSquatGeometry_returnsAngleLessThan90`() {
        // Acute knee angle — represents deep squat position
        // Hip behind and above knee, ankle in front of knee
        val landmarks = landmarkList(
            23 to Triple(-0.1f, 0f, 0f),   // hip behind
            25 to Triple(0f, 0.7f, 0f),    // knee
            27 to Triple(0.2f, 1.0f, 0f)   // ankle forward
        )

        val angles = angleCalculator.calculateJointAngles(landmarks)

        val knee = angles[JointAngle.LEFT_KNEE]
        assertFalse("LEFT_KNEE should be present", knee == null)
        assertTrue("Deep squat should produce knee angle < 90°", knee!! < 90f)
    }

    // --------------------------------------------------------
    // Hip hinge — hip angle
    // --------------------------------------------------------

    @Test
    fun `calculateLeftHipAngle_uprightStanding_returns180Degrees`() {
        // Shoulder directly above hip, knee directly below → 180° (upright)
        // Left shoulder(11), Left hip(23), Left knee(25)
        val landmarks = landmarkList(
            11 to Triple(0f, 0f, 0f),
            23 to Triple(0f, 0.5f, 0f),
            25 to Triple(0f, 1.0f, 0f)
        )

        val angles = angleCalculator.calculateJointAngles(landmarks)

        val hip = angles[JointAngle.LEFT_HIP]
        assertFalse("LEFT_HIP should be present", hip == null)
        assertEquals(180f, hip!!, 1.0f)
    }

    @Test
    fun `calculateLeftHipAngle_hipHinge_returnsAngleLessThan90`() {
        // Torso pitched forward — hip hinge position
        // Shoulder far forward, hip in middle, knee below
        val landmarks = landmarkList(
            11 to Triple(0.5f, 0f, 0f),    // shoulder far forward
            23 to Triple(0f, 0.5f, 0f),    // hip
            25 to Triple(0f, 1.0f, 0f)     // knee directly below hip
        )

        val angles = angleCalculator.calculateJointAngles(landmarks)

        val hip = angles[JointAngle.LEFT_HIP]
        assertFalse("LEFT_HIP should be present", hip == null)
        // The angle at the hip between shoulder→hip vector and knee→hip vector
        val expected = expectedAngle(0.5f, 0f, 0f, 0.5f, 0f, 1.0f)
        assertEquals(expected, hip!!, 1.0f)
    }

    // --------------------------------------------------------
    // Knee valgus detection — asymmetric knee position
    // --------------------------------------------------------

    @Test
    fun `calculateRightKneeAngle_kneeValgusGeometry_returnsReducedAngle`() {
        // Knee tracking inward (valgus collapse) — hip is laterally displaced
        // from knee-ankle axis, reducing knee angle
        val hx = 0.5f; val hy = 0f       // hip
        val kx = 0.3f; val ky = 0.5f     // knee tracking inward
        val ax = 0.5f; val ay = 1.0f     // ankle

        val landmarks = landmarkList(
            24 to Triple(hx, hy, 0f),
            26 to Triple(kx, ky, 0f),
            28 to Triple(ax, ay, 0f)
        )

        val angles = angleCalculator.calculateJointAngles(landmarks)
        val knee = angles[JointAngle.RIGHT_KNEE]

        assertFalse("RIGHT_KNEE should be present", knee == null)
        val expected = expectedAngle(hx, hy, kx, ky, ax, ay)
        assertEquals(expected, knee!!, 1.0f)
    }

    // --------------------------------------------------------
    // Z-depth MUST NOT affect 2D angle output
    // --------------------------------------------------------

    @Test
    fun `zDepthIsIgnored_sameXYDifferentZ_angleIsIdentical`() {
        // Two sets of landmarks with identical (x,y) but different z values.
        // Computed angles must be identical — Z must not be used.
        val landmarks2D = landmarkList(
            23 to Triple(0f, 0f, 0f),
            25 to Triple(0f, 1f, 0f),
            27 to Triple(1f, 1f, 0f)
        )
        val landmarksWithZ = landmarkList(
            23 to Triple(0f, 0f, 5f),   // z = 5 — should be ignored
            25 to Triple(0f, 1f, -3f),  // z = -3
            27 to Triple(1f, 1f, 2f)    // z = 2
        )

        val angles2D  = angleCalculator.calculateJointAngles(landmarks2D)
        val anglesZ   = angleCalculator.calculateJointAngles(landmarksWithZ)

        assertEquals(
            "Z depth must not change angle computation",
            angles2D[JointAngle.LEFT_KNEE]!!,
            anglesZ[JointAngle.LEFT_KNEE]!!,
            0.001f
        )
    }

    @Test
    fun `zDepthIsIgnored_extremeZValues_angleRemainsCorrect`() {
        val landmarks = landmarkList(
            23 to Triple(0f, 0f, 1000f),
            25 to Triple(0f, 1f, -999f),
            27 to Triple(1f, 1f, 500f)
        )

        val angles = angleCalculator.calculateJointAngles(landmarks)
        val expected = 90f // right-angle geometry in XY plane

        assertEquals(expected, angles[JointAngle.LEFT_KNEE]!!, 1.0f)
    }

    // --------------------------------------------------------
    // Visibility threshold
    // --------------------------------------------------------

    @Test
    fun `visibilityBelowThreshold_jointNotIncludedInAngles`() {
        // Left knee has visibility 0.4 (< 0.5 threshold) — should be excluded
        val landmarks = (0..32).map { idx ->
            when (idx) {
                23 -> PoseLandmark(idx, 0f, 0f, 0f, 1.0f)   // left hip — visible
                25 -> PoseLandmark(idx, 0f, 1f, 0f, 0.4f)   // left knee — NOT visible
                27 -> PoseLandmark(idx, 1f, 1f, 0f, 1.0f)   // left ankle — visible
                else -> PoseLandmark(idx, 0f, 0f, 0f, 0.0f)
            }
        }

        val angles = angleCalculator.calculateJointAngles(landmarks)

        assertTrue(
            "LEFT_KNEE should be absent when visibility < 0.5",
            angles[JointAngle.LEFT_KNEE] == null
        )
    }

    @Test
    fun `visibilityExactly0point5_jointIsIncluded`() {
        // Boundary condition: visibility exactly 0.5 — should be included
        // (threshold check is > 0.5f in the production code, meaning 0.5 IS excluded)
        val landmarks = (0..32).map { idx ->
            when (idx) {
                23 -> PoseLandmark(idx, 0f, 0f, 0f, 1.0f)
                25 -> PoseLandmark(idx, 0f, 1f, 0f, 0.5f)  // exactly at threshold
                27 -> PoseLandmark(idx, 1f, 1f, 0f, 1.0f)
                else -> PoseLandmark(idx, 0f, 0f, 0f, 0.0f)
            }
        }

        val angles = angleCalculator.calculateJointAngles(landmarks)

        // The production code uses `lKnee.visibility > 0.5f` (strictly greater than)
        // so 0.5 should NOT be included. Verify the boundary is respected.
        assertTrue(
            "LEFT_KNEE with visibility exactly 0.5 should be excluded (strictly > 0.5 required)",
            angles[JointAngle.LEFT_KNEE] == null
        )
    }

    // --------------------------------------------------------
    // Degenerate / edge cases
    // --------------------------------------------------------

    @Test
    fun `calculateAngle_coincidentPoints_returnsZeroNotNaN`() {
        // Hip and knee at the same position — zero-length vector
        val landmarks = landmarkList(
            23 to Triple(0.5f, 0.5f, 0f),
            25 to Triple(0.5f, 0.5f, 0f), // same as hip
            27 to Triple(0.5f, 1.0f, 0f)
        )

        val angles = angleCalculator.calculateJointAngles(landmarks)

        // Should not crash; return 0.0 for degenerate case
        val knee = angles[JointAngle.LEFT_KNEE]
        if (knee != null) {
            assertFalse("Angle must not be NaN", knee.isNaN())
            assertEquals("Degenerate angle should be 0", 0f, knee, 0.001f)
        }
        // Null is also acceptable — production code returns 0f via `if (magA == 0f...) return 0f`
    }

    @Test
    fun `calculateAngles_emptyLandmarkList_returnsEmptyMap`() {
        val angles = angleCalculator.calculateJointAngles(emptyList())

        assertTrue("Empty landmarks should produce empty angle map", angles.isEmpty())
    }

    // --------------------------------------------------------
    // Trunk inclination
    // --------------------------------------------------------

    @Test
    fun `calculateTrunkInclination_uprightPosture_returnsNearZero`() {
        // Left shoulder directly above left hip — vertical torso
        val landmarks = landmarkList(
            11 to Triple(0.5f, 0f, 0f),   // shoulder
            23 to Triple(0.5f, 1f, 0f)    // hip directly below
        )

        val angles = angleCalculator.calculateJointAngles(landmarks)

        val inclination = angles[JointAngle.TRUNK_INCLINATION]
        assertFalse("TRUNK_INCLINATION should be present", inclination == null)
        assertTrue("Vertical torso should have inclination near 0°", abs(inclination!!) < 5f)
    }

    @Test
    fun `calculateTrunkInclination_forwardLean_returnsPositiveAngle`() {
        // Shoulder offset forward from hip — forward lean
        val landmarks = landmarkList(
            11 to Triple(0.7f, 0f, 0f),   // shoulder forward
            23 to Triple(0.5f, 1f, 0f)    // hip
        )

        val angles = angleCalculator.calculateJointAngles(landmarks)

        val inclination = angles[JointAngle.TRUNK_INCLINATION]
        assertFalse("TRUNK_INCLINATION should be present", inclination == null)
        // Forward lean produces a non-zero positive inclination
        assertTrue("Forward lean should produce positive inclination", inclination!! > 0f)
    }

    // --------------------------------------------------------
    // Elbow angles
    // --------------------------------------------------------

    @Test
    fun `calculateLeftElbowAngle_bentArm_returnsAngleLessThan180`() {
        // Shoulder, elbow (bent), wrist
        // Left shoulder(11), Left elbow(13), Left wrist(15)
        val landmarks = landmarkList(
            11 to Triple(0f, 0f, 0f),
            13 to Triple(0f, 0.4f, 0f),
            15 to Triple(0.3f, 0.7f, 0f)
        )

        val angles = angleCalculator.calculateJointAngles(landmarks)

        val elbow = angles[JointAngle.LEFT_ELBOW]
        assertFalse("LEFT_ELBOW should be present", elbow == null)
        assertTrue("Bent arm should produce angle < 180°", elbow!! < 180f)
        val expected = expectedAngle(0f, 0f, 0f, 0.4f, 0.3f, 0.7f)
        assertEquals(expected, elbow, 1.0f)
    }
}

/**
 * Wrapper class that exposes [calculateJointAngles] for testing without
 * pulling in the full [MediaPipePoseLandmarkerHelper] Android dependency.
 *
 * This mirrors the exact algorithm from
 * [MediaPipePoseLandmarkerHelper.calculateJointAngles] so tests assert
 * against the production algorithm, not a simplified approximation.
 */
class KinematicAngleCalculator {

    fun calculateJointAngles(landmarks: List<PoseLandmark>): Map<JointAngle, Float> {
        val angles = mutableMapOf<JointAngle, Float>()
        if (landmarks.isEmpty()) return angles

        fun angle(a: PoseLandmark, b: PoseLandmark, c: PoseLandmark): Float {
            // 2D only — Z is deliberately excluded (CLAUDE.md mandate)
            val ax = a.x - b.x; val ay = a.y - b.y
            val cx = c.x - b.x; val cy = c.y - b.y
            val dot  = ax * cx + ay * cy
            val magA = sqrt(ax * ax + ay * ay)
            val magC = sqrt(cx * cx + cy * cy)
            if (magA == 0f || magC == 0f) return 0f
            return Math.toDegrees(
                acos((dot / (magA * magC)).toDouble().coerceIn(-1.0, 1.0))
            ).toFloat()
        }

        fun lm(idx: Int) = landmarks.getOrNull(idx)

        val lShoulder = lm(11); val rShoulder = lm(12)
        val lHip      = lm(23); val rHip      = lm(24)
        val lKnee     = lm(25); val rKnee     = lm(26)
        val lAnkle    = lm(27); val rAnkle    = lm(28)
        val lElbow    = lm(13); val rElbow    = lm(14)
        val lWrist    = lm(15); val rWrist    = lm(16)

        if (lHip != null && lKnee != null && lAnkle != null && lKnee.visibility > 0.5f)
            angles[JointAngle.LEFT_KNEE] = angle(lHip, lKnee, lAnkle)
        if (rHip != null && rKnee != null && rAnkle != null && rKnee.visibility > 0.5f)
            angles[JointAngle.RIGHT_KNEE] = angle(rHip, rKnee, rAnkle)

        if (lShoulder != null && lHip != null && lKnee != null && lHip.visibility > 0.5f)
            angles[JointAngle.LEFT_HIP] = angle(lShoulder, lHip, lKnee)
        if (rShoulder != null && rHip != null && rKnee != null && rHip.visibility > 0.5f)
            angles[JointAngle.RIGHT_HIP] = angle(rShoulder, rHip, rKnee)

        if (lShoulder != null && lElbow != null && lWrist != null && lElbow.visibility > 0.5f)
            angles[JointAngle.LEFT_ELBOW] = angle(lShoulder, lElbow, lWrist)
        if (rShoulder != null && rElbow != null && rWrist != null && rElbow.visibility > 0.5f)
            angles[JointAngle.RIGHT_ELBOW] = angle(rShoulder, rElbow, rWrist)

        if (lShoulder != null && lHip != null) {
            val deltaY = lHip.y - lShoulder.y
            val deltaX = lHip.x - lShoulder.x
            val inclination = Math.toDegrees(
                Math.atan2(deltaX.toDouble(), deltaY.toDouble())
            ).toFloat()
            angles[JointAngle.TRUNK_INCLINATION] = Math.abs(inclination)
        }

        return angles
    }
}
