package ua.polodarb.gmsflags.domain.report

/**
 * Device / OS identity collected for a problem report. Purely descriptive - never contains anything
 * the user typed.
 */
data class ReportDeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidRelease: String,
    val sdkInt: Int,
    val abi: String,
)

/**
 * Everything about THIS build and device that a report attaches automatically.
 *
 * [appSignatureSha256] is this app's own signing certificate hash (hex, uppercase, colon-separated).
 * It lets the backend tell an original build from a repackaged one. Null when it could not be read.
 */
data class ReportDiagnostics(
    val device: ReportDeviceInfo,
    val appVersionName: String,
    val appVersionCode: Long,
    val appSignatureSha256: String?,
)

/**
 * The recommendation context a report is filed against. Every field is nullable: the header entry
 * point reports a recommendation generally (no hook), and a general report filed from Settings (see
 * presentation-feature-settings) carries no recommendation at all - use [EMPTY] for that case.
 */
data class ProblemReportContext(
    val recommendationId: Long?,
    val variantLabel: String?,
    val hookRecipeId: Long?,
    val hookTrustStatus: String?,
    val targetPackage: String?,
    val targetVersionName: String?,
) {
    companion object {
        val EMPTY = ProblemReportContext(null, null, null, null, null, null)
    }
}

/** What kind of report this is - drives categorization/triage in the CRM. */
enum class ReportCategory(val wireValue: String) {
    Bug("bug"),
    Suggestion("suggestion"),
    Other("other"),
}

/**
 * A complete, ready-to-send problem report: the user's [message], the auto-collected [diagnostics],
 * the [context] it was filed from, and its [category].
 */
data class ProblemReport(
    val message: String,
    val contact: String?,
    val diagnostics: ReportDiagnostics,
    val context: ProblemReportContext,
    val category: ReportCategory,
)
