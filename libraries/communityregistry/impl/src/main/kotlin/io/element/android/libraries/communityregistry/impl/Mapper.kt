/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.impl

import io.element.android.libraries.communityregistry.api.CheckInviteResult
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.communityregistry.api.InviteRequestResult
import io.element.android.libraries.communityregistry.api.Registration
import io.element.android.libraries.communityregistry.api.Visibility
import io.element.android.libraries.communityregistry.impl.dto.CheckInviteResponseDto
import io.element.android.libraries.communityregistry.impl.dto.CommunityServerDto
import io.element.android.libraries.communityregistry.impl.dto.InviteRequestResponseDto
import io.element.android.libraries.communityregistry.impl.dto.RegistrationDto
import io.element.android.libraries.communityregistry.impl.dto.ServerWhitelistResponseDto

internal fun CommunityServerDto.toApi(): CommunityServer = CommunityServer(
    homeserver = homeserver,
    name = name,
    description = description,
    logoUrl = logoUrl,
    type = type,
    visibility = visibility.toVisibility(),
    registration = registration.toRegistration(),
    owner = owner,
    country = country,
    language = language,
    since = since,
    tags = tags,
    active = active ?: true,
    lastSeen = lastSeen,
)

internal fun String?.toVisibility(): Visibility = when (this?.lowercase()) {
    "public" -> Visibility.Public
    "unlisted" -> Visibility.Unlisted
    else -> Visibility.Unknown
}

internal fun RegistrationDto?.toRegistration(): Registration = when (this?.mode?.lowercase()) {
    "open" -> Registration.Open
    "token" -> Registration.Token(instructions = instructions, contact = contact)
    "closed" -> Registration.Closed
    else -> Registration.Unknown
}

internal fun ServerWhitelistResponseDto.toApi(): List<String> =
    servers.ifEmpty { allowedHomeservers }

internal fun CheckInviteResponseDto.toApi(): CheckInviteResult = if (valid && server != null) {
    CheckInviteResult.Valid(server.toApi())
} else {
    // Spec §3.3 ships a single INVALID_INVITE. Fall back to whichever field the server
    // included so dev surfaces and tests can introspect; production UI shows one message.
    CheckInviteResult.Invalid(reason = reason ?: code ?: error)
}

internal fun InviteRequestResponseDto.toApi(): InviteRequestResult {
    // Prefer the error envelope `code` (spec §3.4 4xx/5xx responses).
    when (code?.uppercase()) {
        "INVALID_EMAIL" -> return InviteRequestResult.InvalidEmail
        "INVALID_MESSAGE" -> return InviteRequestResult.InvalidMessage
        "NOT_ACCEPTING" -> return InviteRequestResult.NotAccepting
        "RATE_LIMITED" -> return InviteRequestResult.RateLimited
    }
    return when (status?.lowercase()) {
        "submitted", "ok", "success", "queued" -> InviteRequestResult.Submitted
        "rate_limited", "rate-limited" -> InviteRequestResult.RateLimited
        else -> InviteRequestResult.ServerError(message ?: error)
    }
}
