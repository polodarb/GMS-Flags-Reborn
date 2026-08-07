package ua.polodarb.gmsflags.data.repository.impl.report.datasource

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest
import ua.polodarb.gmsflags.data.repository.report.datasource.ReportDiagnosticsCollector
import ua.polodarb.gmsflags.domain.report.ReportDeviceInfo
import ua.polodarb.gmsflags.domain.report.ReportDiagnostics

internal class AndroidReportDiagnosticsCollector(
    context: Context,
) : ReportDiagnosticsCollector {
    private val applicationContext = context.applicationContext
    private val packageManager = applicationContext.packageManager
    private val packageName = applicationContext.packageName

    override fun collect(): ReportDiagnostics {
        val packageInfo = runCatching {
            packageManager.getPackageInfo(packageName, 0)
        }.getOrNull()
        return ReportDiagnostics(
            device = ReportDeviceInfo(
                manufacturer = Build.MANUFACTURER.orEmpty(),
                model = Build.MODEL.orEmpty(),
                androidRelease = Build.VERSION.RELEASE.orEmpty(),
                sdkInt = Build.VERSION.SDK_INT,
                abi = Build.SUPPORTED_ABIS?.firstOrNull().orEmpty(),
            ),
            appVersionName = packageInfo?.versionName.orEmpty(),
            appVersionCode = packageInfo?.longVersionCode ?: 0L,
            appSignatureSha256 = readSignatureSha256(),
        )
    }

    private fun readSignatureSha256(): String? {
        val signatures = runCatching { readSignatures() }.getOrNull()
        val firstSigner = signatures?.firstOrNull() ?: return null
        return runCatching {
            MessageDigest.getInstance("SHA-256")
                .digest(firstSigner.toByteArray())
                .joinToString(separator = ":") { byte -> "%02X".format(byte) }
        }.getOrNull()
    }

    @Suppress("DEPRECATION", "PackageManagerGetSignatures")
    private fun readSignatures(): Array<Signature>? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES,
            )
            val signingInfo = info.signingInfo
            when {
                signingInfo == null -> null
                signingInfo.hasMultipleSigners() -> signingInfo.apkContentsSigners
                else -> signingInfo.signingCertificateHistory
            }
        } else {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES,
            ).signatures
        }
}
