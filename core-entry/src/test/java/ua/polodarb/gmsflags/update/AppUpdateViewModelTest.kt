package ua.polodarb.gmsflags.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import ua.polodarb.gmsflags.domain.update.AppUpdatePolicy
import ua.polodarb.gmsflags.domain.update.AppUpdateType
import ua.polodarb.gmsflags.domain.update.DismissSoftAppUpdate
import ua.polodarb.gmsflags.domain.update.GetAppUpdatePolicy

@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdateViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `soft update fills both the sheet and the notice`() = runTest(dispatcher) {
        val policy = policy(AppUpdateType.SOFT)
        val viewModel = viewModel(policy)
        advanceUntilIdle()

        assertEquals(policy, viewModel.sheetPolicy.value)
        assertEquals(policy, viewModel.noticePolicy.value)
    }

    @Test
    fun `dismissing the soft sheet keeps the notice and snoozes the policy`() = runTest(dispatcher) {
        val policy = policy(AppUpdateType.SOFT)
        var snoozed: Pair<String, Int>? = null
        val viewModel = viewModel(
            policy = policy,
            dismissSoftAppUpdate = { policyId, cooldownHours -> snoozed = policyId to cooldownHours },
        )
        advanceUntilIdle()

        viewModel.dismiss()
        advanceUntilIdle()

        assertNull(viewModel.sheetPolicy.value)
        assertEquals(policy, viewModel.noticePolicy.value)
        assertEquals(policy.policyId to policy.softCooldownHours, snoozed)
    }

    @Test
    fun `force update ignores dismiss`() = runTest(dispatcher) {
        val policy = policy(AppUpdateType.FORCE)
        var snoozeCalls = 0
        val viewModel = viewModel(
            policy = policy,
            dismissSoftAppUpdate = { _, _ -> snoozeCalls++ },
        )
        advanceUntilIdle()

        viewModel.dismiss()
        advanceUntilIdle()

        assertEquals(policy, viewModel.sheetPolicy.value)
        assertEquals(policy, viewModel.noticePolicy.value)
        assertEquals(0, snoozeCalls)
    }

    @Test
    fun `a failing policy lookup leaves both surfaces empty`() = runTest(dispatcher) {
        val viewModel = AppUpdateViewModel(
            getAppUpdatePolicy = { error("remote config unavailable") },
            dismissSoftAppUpdate = { _, _ -> },
        )
        advanceUntilIdle()

        assertNull(viewModel.sheetPolicy.value)
        assertNull(viewModel.noticePolicy.value)
    }

    @Test
    fun `a forced notice policy shows the notice without the sheet`() = runTest(dispatcher) {
        val forced = policy(AppUpdateType.SOFT)
        val viewModel = AppUpdateViewModel(
            getAppUpdatePolicy = { null },
            dismissSoftAppUpdate = { _, _ -> },
            forcedNoticePolicy = forced,
        )
        advanceUntilIdle()

        assertNull(viewModel.sheetPolicy.value)
        assertEquals(forced, viewModel.noticePolicy.value)
    }

    private fun viewModel(
        policy: AppUpdatePolicy?,
        dismissSoftAppUpdate: DismissSoftAppUpdate = DismissSoftAppUpdate { _, _ -> },
    ) = AppUpdateViewModel(
        getAppUpdatePolicy = GetAppUpdatePolicy { policy },
        dismissSoftAppUpdate = dismissSoftAppUpdate,
    )

    private fun policy(type: AppUpdateType) = AppUpdatePolicy(
        policyId = "policy-1",
        title = "Update available",
        description = "A newer version is available.",
        updateUrl = "https://example.com",
        type = type,
        softCooldownHours = 12,
        primaryButtonTitle = "Update",
        secondaryButtonTitle = "Later",
    )
}
