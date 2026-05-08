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
    val description: String?,
    val logoUrl: String?,
    val type: String?,
    val registration: Registration,
)

sealed interface Registration {
    data object Open : Registration
    data class Token(
        val instructions: String?,
        val contact: String?,
    ) : Registration
    data object Closed : Registration
    data object Unknown : Registration
}
