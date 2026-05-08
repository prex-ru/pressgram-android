/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.api

sealed interface CheckInviteResult {
    data class Valid(
        val homeserver: String,
        val displayName: String?,
    ) : CheckInviteResult

    data class Invalid(val reason: String?) : CheckInviteResult
}
