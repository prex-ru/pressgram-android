/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.login.impl.screens.inviterequest

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.communityregistry.api.InviteRequestResult
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.PressgramConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.R as DesignSystemR

@Composable
fun InviteRequestView(
    state: InviteRequestState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_invite_request_title),
                        style = ElementTheme.typography.fontHeadingMdBold,
                    )
                },
                navigationIcon = { BackButton(onClick = onBackClick) },
            )
        },
    ) { padding ->
        InviteRequestForm(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
    // On success the form stays behind a scrim and the confirmation dialog is shown.
    if (state.isSubmitted) {
        PressgramConfirmationDialog(
            title = stringResource(R.string.screen_invite_request_success_title),
            content = stringResource(R.string.screen_invite_request_success_message, state.email),
            caption = stringResource(R.string.screen_invite_request_success_hint),
            submitText = stringResource(R.string.screen_invite_request_success_action),
            onSubmitClick = onBackClick,
            onDismiss = onBackClick,
            icon = {
                Icon(
                    imageVector = CompoundIcons.Check(),
                    contentDescription = null,
                    tint = ElementTheme.colors.iconSuccessPrimary,
                    modifier = Modifier.size(32.dp),
                )
            },
        )
    }
}

@Composable
private fun InviteRequestForm(
    state: InviteRequestState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.imePadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            ServerHeader(
                serverName = state.serverName,
                serverLogoUrl = state.serverLogoUrl,
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.screen_invite_request_email_label),
                style = ElementTheme.typography.fontBodyMdMedium,
                color = ElementTheme.colors.textSecondary,
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = state.email,
                onValueChange = { state.eventSink(InviteRequestEvents.SetEmail(it)) },
                enabled = !state.isSubmitting,
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.screen_invite_request_message_label),
                style = ElementTheme.typography.fontBodyMdMedium,
                color = ElementTheme.colors.textSecondary,
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = state.message,
                onValueChange = { state.eventSink(InviteRequestEvents.SetMessage(it)) },
                enabled = !state.isSubmitting,
                shape = RoundedCornerShape(16.dp),
                minLines = 6,
                modifier = Modifier.fillMaxWidth(),
            )
            val errorText = submitErrorText(state.submitAction)
            if (errorText != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorText,
                    style = ElementTheme.typography.fontBodySmMedium,
                    color = ElementTheme.colors.textCriticalPrimary,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
        Button(
            text = stringResource(R.string.screen_invite_request_submit),
            showProgress = state.isSubmitting,
            enabled = state.canSubmit,
            onClick = { state.eventSink(InviteRequestEvents.Submit) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        )
    }
}

@Composable
private fun ServerHeader(
    serverName: String?,
    serverLogoUrl: String?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val fallbackLogo = painterResource(id = DesignSystemR.drawable.pressgram_logo)
        AsyncImage(
            model = serverLogoUrl,
            contentDescription = null,
            placeholder = fallbackLogo,
            error = fallbackLogo,
            fallback = fallbackLogo,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = serverName.orEmpty(),
            style = ElementTheme.typography.fontBodyLgMedium,
            color = ElementTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun submitErrorText(submitAction: AsyncData<InviteRequestResult>): String? {
    val resId = when {
        submitAction is AsyncData.Failure -> R.string.screen_invite_request_error_generic
        submitAction is AsyncData.Success -> when (submitAction.data) {
            InviteRequestResult.InvalidEmail -> R.string.screen_invite_request_error_invalid_email
            InviteRequestResult.InvalidMessage -> R.string.screen_invite_request_error_invalid_message
            InviteRequestResult.NotAccepting -> R.string.screen_invite_request_error_not_accepting
            InviteRequestResult.RateLimited -> R.string.screen_invite_request_error_rate_limited
            is InviteRequestResult.ServerError -> R.string.screen_invite_request_error_generic
            InviteRequestResult.Submitted -> null
        }
        else -> null
    }
    return resId?.let { stringResource(it) }
}

@PreviewsDayNight
@Composable
internal fun InviteRequestViewPreview(
    @PreviewParameter(InviteRequestStateProvider::class) state: InviteRequestState,
) = ElementPreview {
    InviteRequestView(
        state = state,
        onBackClick = {},
    )
}
