package ua.polodarb.gmsflags.data.repository.impl.server.mapper

import ua.polodarb.gmsflags.data.network.publicapi.model.AppDetailsNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.AppNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.BadgeNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.FaqEntryNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.FlagCatalogEntryNetModel
import ua.polodarb.gmsflags.data.network.publicapi.model.InfoBlockNetModel
import ua.polodarb.gmsflags.domain.server.content.FaqEntry
import ua.polodarb.gmsflags.domain.server.content.ServerApplication
import ua.polodarb.gmsflags.domain.server.content.ServerApplicationDetails
import ua.polodarb.gmsflags.domain.server.content.ServerBadge
import ua.polodarb.gmsflags.domain.server.content.ServerFlagCatalogEntry
import ua.polodarb.gmsflags.domain.server.content.ServerInfoBlock

internal fun AppNetModel.toDomain() = ServerApplication(
    id = id,
    packageName = packageName,
    displayName = displayName,
    iconUrl = iconUrl,
    disabledFromVersion = disabledFromVersion,
)

internal fun AppDetailsNetModel.toDomain() = ServerApplicationDetails(
    application = app.toDomain(),
    infoBlocks = infoBlocks.map { it.toDomain() },
    highlightedFlags = flagCatalog.map { it.toDomain() },
)

internal fun InfoBlockNetModel.toDomain() = ServerInfoBlock(
    id = id,
    type = type.toInfoBlockType(),
    message = message,
    isActive = isActive,
    sortOrder = sortOrder,
    externalLink = externalLink,
)

private fun FlagCatalogEntryNetModel.toDomain() = ServerFlagCatalogEntry(
    id = id,
    flagName = flagName,
    title = title,
    description = description,
    dangerLevel = dangerLevel.toDangerLevel(),
    badges = badges.map { it.toDomain() },
)

internal fun BadgeNetModel.toDomain() = ServerBadge(label = label)

internal fun FaqEntryNetModel.toDomain() = FaqEntry(
    id = id,
    question = question,
    answer = answer,
)
