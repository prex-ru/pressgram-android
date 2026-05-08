/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.impl.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityServersResponseDto(
    @SerialName("version") val version: Int? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("servers") val servers: List<CommunityServerDto> = emptyList(),
)

@Serializable
data class CommunityServerDto(
    @SerialName("homeserver") val homeserver: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("visibility") val visibility: String? = null,
    @SerialName("registration") val registration: RegistrationDto? = null,
    @SerialName("active") val active: Boolean? = null,
)

@Serializable
data class RegistrationDto(
    @SerialName("mode") val mode: String? = null,
    @SerialName("instructions") val instructions: String? = null,
    @SerialName("contact") val contact: String? = null,
)

@Serializable
data class ServerWhitelistResponseDto(
    @SerialName("allowed_homeservers") val allowedHomeservers: List<String> = emptyList(),
)

@Serializable
data class CheckInviteRequestDto(
    @SerialName("homeserver") val homeserver: String,
    @SerialName("token") val token: String,
)

@Serializable
data class CheckInviteResponseDto(
    @SerialName("valid") val valid: Boolean,
    @SerialName("homeserver") val homeserver: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("reason") val reason: String? = null,
)

@Serializable
data class InviteRequestRequestDto(
    @SerialName("homeserver") val homeserver: String,
    @SerialName("email") val email: String,
    @SerialName("message") val message: String,
)

@Serializable
data class InviteRequestResponseDto(
    @SerialName("status") val status: String,
    @SerialName("message") val message: String? = null,
)
