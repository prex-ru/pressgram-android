/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.inviterequest

sealed interface InviteRequestEvents {
    data class SetEmail(val email: String) : InviteRequestEvents
    data class SetMessage(val message: String) : InviteRequestEvents
    data object Submit : InviteRequestEvents
}
