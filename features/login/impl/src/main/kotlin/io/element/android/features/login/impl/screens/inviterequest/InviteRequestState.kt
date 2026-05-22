/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.inviterequest

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.communityregistry.api.InviteRequestResult

data class InviteRequestState(
    val serverName: String?,
    val serverLogoUrl: String?,
    val email: String,
    val message: String,
    val submitAction: AsyncData<InviteRequestResult>,
    val eventSink: (InviteRequestEvents) -> Unit,
) {
    val isSubmitting: Boolean
        get() = submitAction is AsyncData.Loading

    val isSubmitted: Boolean
        get() = (submitAction as? AsyncData.Success)?.data == InviteRequestResult.Submitted

    val canSubmit: Boolean
        get() = !isSubmitting &&
            email.isNotBlank() &&
            message.length in MIN_MESSAGE_LENGTH..MAX_MESSAGE_LENGTH

    companion object {
        const val MIN_MESSAGE_LENGTH = 20
        const val MAX_MESSAGE_LENGTH = 1000
    }
}
