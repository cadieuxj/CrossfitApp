package com.apexai.crossfit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit 4 TestRule that installs a [StandardTestDispatcher] as Dispatchers.Main
 * for the duration of each test.
 *
 * Usage:
 *   @get:Rule val coroutineRule = TestCoroutineRule()
 *
 *   fun myTest() = coroutineRule.runTest {
 *       // coroutine code here — time is controlled
 *   }
 *
 * The [testScheduler] is shared so that advanceUntilIdle() / advanceTimeBy()
 * work across all coroutines launched by the ViewModel under test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineRule(
    val testScheduler: TestCoroutineScheduler = TestCoroutineScheduler(),
    val testDispatcher: TestDispatcher = StandardTestDispatcher(testScheduler)
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }

    /**
     * Convenience wrapper around [kotlinx.coroutines.test.runTest] that
     * automatically uses this rule's [testScheduler].
     */
    fun runTest(block: suspend kotlinx.coroutines.test.TestScope.() -> Unit) =
        kotlinx.coroutines.test.runTest(testScheduler) { block() }
}
