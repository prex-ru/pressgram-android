/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.inviterequest

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.communityregistry.api.InviteRequestResult

open class InviteRequestStateProvider : PreviewParameterProvider<InviteRequestState> {
    override val values: Sequence<InviteRequestState>
        get() = sequenceOf(
            anInviteRequestState(),
            anInviteRequestState(
                email = "reporter@example.com",
                message = "I am a freelance reporter covering local politics and would like to join.",
            ),
            anInviteRequestState(
                email = "reporter@example.com",
                message = "I am a freelance reporter covering local politics and would like to join.",
                submitAction = AsyncData.Loading(),
            ),
            anInviteRequestState(
                email = "bad-email",
                message = "Too short",
                submitAction = AsyncData.Success(InviteRequestResult.InvalidMessage),
            ),
        )
}

// The submitted/success state is not previewed through InviteRequestView because it
// shows a Dialog (unsupported in @Preview); see InviteRequestSentDialogContentPreview.

internal fun anInviteRequestState(
    serverName: String? = "Редакция «Новости»",
    serverLogoUrl: String? = null,
    email: String = "",
    message: String = "",
    submitAction: AsyncData<InviteRequestResult> = AsyncData.Uninitialized,
    eventSink: (InviteRequestEvents) -> Unit = {},
) = InviteRequestState(
    serverName = serverName,
    serverLogoUrl = serverLogoUrl,
    email = email,
    message = message,
    submitAction = submitAction,
    eventSink = eventSink,
)
