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
import io.element.android.libraries.communityregistry.impl.dto.CheckInviteResponseDto
import io.element.android.libraries.communityregistry.impl.dto.CommunityServerDto
import io.element.android.libraries.communityregistry.impl.dto.InviteRequestResponseDto
import io.element.android.libraries.communityregistry.impl.dto.RegistrationDto

internal fun CommunityServerDto.toApi(): CommunityServer = CommunityServer(
    homeserver = homeserver,
    name = name,
    description = description,
    logoUrl = logoUrl,
    type = type,
    registration = registration.toRegistration(),
)

internal fun RegistrationDto?.toRegistration(): Registration = when (this?.mode?.lowercase()) {
    "open" -> Registration.Open
    "token" -> Registration.Token(instructions = instructions, contact = contact)
    "closed" -> Registration.Closed
    else -> Registration.Unknown
}

internal fun CheckInviteResponseDto.toApi(): CheckInviteResult = if (valid && homeserver != null) {
    CheckInviteResult.Valid(homeserver = homeserver, displayName = displayName)
} else {
    CheckInviteResult.Invalid(reason = reason)
}

internal fun InviteRequestResponseDto.toApi(): InviteRequestResult = when (status.lowercase()) {
    "submitted", "ok", "success" -> InviteRequestResult.Submitted
    "rate_limited", "rate-limited" -> InviteRequestResult.RateLimited
    else -> InviteRequestResult.Error(message)
}
