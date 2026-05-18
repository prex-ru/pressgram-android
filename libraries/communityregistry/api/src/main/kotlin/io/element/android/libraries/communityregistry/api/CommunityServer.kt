/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.api

data class CommunityServer(
    val homeserver: String,
    val name: String,
    val description: String? = null,
    val logoUrl: String? = null,
    val type: String? = null,
    val visibility: Visibility = Visibility.Public,
    val registration: Registration = Registration.Unknown,
    val owner: String? = null,
    val country: String? = null,
    val language: String? = null,
    val since: String? = null,
    val tags: List<String> = emptyList(),
    val active: Boolean = true,
    val lastSeen: String? = null,
)

sealed interface Visibility {
    data object Public : Visibility
    data object Unlisted : Visibility
    data object Unknown : Visibility
}

sealed interface Registration {
    data object Open : Registration
    data class Token(
        val instructions: String?,
        val contact: String?,
    ) : Registration
    data object Closed : Registration
    data object Unknown : Registration
}
