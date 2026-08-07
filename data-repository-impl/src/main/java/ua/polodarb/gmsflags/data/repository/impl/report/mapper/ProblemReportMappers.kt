package ua.polodarb.gmsflags.data.repository.impl.report.mapper

import ua.polodarb.gmsflags.data.network.publicapi.model.ReportContextNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.ReportDeviceNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.ReportRequestNetModel
import ua.polodarb.gmsflags.domain.report.ProblemReport

internal fun ProblemReport.toNetModel(xposedLogs: String): ReportRequestNetModel = ReportRequestNetModel(
    message = message,
    contact = contact,
    appVersionName = diagnostics.appVersionName,
    appVersionCode = diagnostics.appVersionCode,
    appSignatureSha256 = diagnostics.appSignatureSha256,
    device = ReportDeviceNetModel(
        manufacturer = diagnostics.device.manufacturer,
        model = diagnostics.device.model,
        androidRelease = diagnostics.device.androidRelease,
        sdkInt = diagnostics.device.sdkInt,
        abi = diagnostics.device.abi,
    ),
    context = ReportContextNetModel(
        recommendationId = context.recommendationId,
        variantLabel = context.variantLabel,
        hookRecipeId = context.hookRecipeId,
        hookTrustStatus = context.hookTrustStatus,
        targetPackage = context.targetPackage,
        targetVersionName = context.targetVersionName,
    ),
    xposedLogs = xposedLogs,
    category = category.wireValue,
)
