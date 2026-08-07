package ua.polodarb.gmsflags.data.repository.impl.report.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ua.polodarb.gmsflags.domain.report.ProblemReport
import ua.polodarb.gmsflags.domain.report.ProblemReportContext
import ua.polodarb.gmsflags.domain.report.ReportCategory
import ua.polodarb.gmsflags.domain.report.ReportDeviceInfo
import ua.polodarb.gmsflags.domain.report.ReportDiagnostics

private val diagnostics = ReportDiagnostics(
    device = ReportDeviceInfo(
        manufacturer = "Google",
        model = "Pixel 5",
        androidRelease = "16",
        sdkInt = 36,
        abi = "arm64-v8a",
    ),
    appVersionName = "1.0.1",
    appVersionCode = 101,
    appSignatureSha256 = "AB:CD",
)

class ReportsRepositoryImplMappingTest {

    @Test
    fun `toNetModel includes the wire value of the category`() {
        val report = ProblemReport(
            message = "found a bug",
            contact = null,
            diagnostics = diagnostics,
            context = ProblemReportContext(7, null, null, null, null, null),
            category = ReportCategory.Bug,
        )

        val net = report.toNetModel(xposedLogs = "")

        assertEquals("bug", net.category)
        assertEquals(7L, net.context.recommendationId)
    }

    @Test
    fun `toNetModel maps a null recommendationId through cleanly`() {
        val report = ProblemReport(
            message = "a suggestion",
            contact = null,
            diagnostics = diagnostics,
            context = ProblemReportContext.EMPTY,
            category = ReportCategory.Suggestion,
        )

        val net = report.toNetModel(xposedLogs = "")

        assertNull(net.context.recommendationId)
        assertEquals("suggestion", net.category)
    }
}
