/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.login.impl.screens.serverdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.login.LoginModeView
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.communityregistry.api.Registration
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.auth.OidcDetails
import kotlinx.coroutines.launch

@Composable
fun ServerDetailView(
    state: ServerDetailState,
    onBackClick: () -> Unit,
    onOidcDetails: (OidcDetails) -> Unit,
    onNeedLoginPassword: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onCreateAccountContinue: (url: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading by remember(state.loginMode) {
        derivedStateOf { state.loginMode is AsyncData.Loading }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoonMessage = stringResource(id = R.string.screen_server_detail_coming_soon)

    Box(modifier = modifier.fillMaxSize()) {
        HeaderFooterPage(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = { BackButton(onClick = onBackClick) },
                )
            },
            header = { ServerDetailHeader(state.serverInfo) },
            footer = {
                ServerDetailFooter(
                    serverInfo = state.serverInfo,
                    isLoading = isLoading,
                    onRegister = { state.eventSink(ServerDetailEvents.Register) },
                    onSignIn = { state.eventSink(ServerDetailEvents.SignIn) },
                    onComingSoon = {
                        scope.launch { snackbarHostState.showSnackbar(comingSoonMessage) }
                    },
                )
            },
        ) {
            ServerDetailBody(state.serverInfo)
            LoginModeView(
                loginMode = state.loginMode,
                onClearError = { state.eventSink(ServerDetailEvents.ClearError) },
                onLearnMoreClick = onLearnMoreClick,
                onOidcDetails = onOidcDetails,
                onNeedLoginPassword = onNeedLoginPassword,
                onCreateAccountContinue = onCreateAccountContinue,
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ServerDetailHeader(serverInfo: AsyncData<ServerDetailInfo>) {
    val title = (serverInfo as? AsyncData.Success)?.data?.name
        ?: stringResource(id = R.string.screen_server_detail_loading_title)
    IconTitleSubtitleMolecule(
        modifier = Modifier.padding(top = 60.dp),
        iconStyle = BigIcon.Style.Default(CompoundIcons.HomeSolid()),
        title = title,
        subTitle = (serverInfo as? AsyncData.Success)?.data?.homeserverUrl
            ?.removePrefix("https://")
            ?.removePrefix("http://"),
    )
}

@Composable
private fun ServerDetailBody(serverInfo: AsyncData<ServerDetailInfo>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (serverInfo) {
            is AsyncData.Loading, AsyncData.Uninitialized -> {
                CircularProgressIndicator()
            }
            is AsyncData.Failure -> {
                Text(
                    text = stringResource(id = R.string.screen_server_detail_load_error),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textCriticalPrimary,
                    textAlign = TextAlign.Center,
                )
            }
            is AsyncData.Success -> {
                val info = serverInfo.data
                if (!info.description.isNullOrBlank()) {
                    Text(
                        text = info.description,
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = ElementTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    text = stringResource(id = info.registration.modeDescriptionResId()),
                    style = ElementTheme.typography.fontBodySmMedium,
                    color = ElementTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
                val tokenInfo = info.registration as? Registration.Token
                if (tokenInfo != null) {
                    val instructions = tokenInfo.instructions
                    if (!instructions.isNullOrBlank()) {
                        Text(
                            text = instructions,
                            style = ElementTheme.typography.fontBodySmRegular,
                            color = ElementTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                    val contact = tokenInfo.contact
                    if (!contact.isNullOrBlank()) {
                        Text(
                            text = stringResource(
                                id = R.string.screen_server_detail_token_contact_format,
                                contact,
                            ),
                            style = ElementTheme.typography.fontBodySmMedium,
                            color = ElementTheme.colors.textPrimary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ServerDetailFooter(
    serverInfo: AsyncData<ServerDetailInfo>,
    isLoading: Boolean,
    onRegister: () -> Unit,
    onSignIn: () -> Unit,
    onComingSoon: () -> Unit,
) {
    val registration = (serverInfo as? AsyncData.Success)?.data?.registration
    ButtonColumnMolecule {
        Button(
            text = stringResource(id = R.string.screen_server_detail_signin_button),
            showProgress = isLoading,
            onClick = onSignIn,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
        when (registration) {
            Registration.Open -> {
                TextButton(
                    text = stringResource(id = R.string.screen_server_detail_register_open_button),
                    onClick = onRegister,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            is Registration.Token -> {
                TextButton(
                    text = stringResource(id = R.string.screen_server_detail_register_with_token_button),
                    onClick = onComingSoon,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    text = stringResource(id = R.string.screen_server_detail_request_button),
                    onClick = onComingSoon,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Registration.Closed,
            Registration.Unknown,
            null -> Unit
        }
    }
}

private fun Registration.modeDescriptionResId(): Int = when (this) {
    Registration.Open -> R.string.screen_server_detail_mode_open
    is Registration.Token -> R.string.screen_server_detail_mode_token
    Registration.Closed -> R.string.screen_server_detail_mode_closed
    Registration.Unknown -> R.string.screen_server_detail_mode_unknown
}

@PreviewsDayNight
@Composable
internal fun ServerDetailViewPreview(@PreviewParameter(ServerDetailStateProvider::class) state: ServerDetailState) = ElementPreview {
    ServerDetailView(
        state = state,
        onBackClick = {},
        onOidcDetails = {},
        onNeedLoginPassword = {},
        onLearnMoreClick = {},
        onCreateAccountContinue = {},
    )
}
