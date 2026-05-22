/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import coil3.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.login.LoginModeView
import io.element.android.features.login.impl.screens.onboarding.classic.ConfirmingLoginWithElementClassic
import io.element.android.features.login.impl.screens.onboarding.classic.LoginWithClassicEvent
import io.element.android.features.login.impl.screens.onboarding.classic.LoginWithClassicState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.pages.FlowStepPage
import io.element.android.libraries.designsystem.atomic.pages.OnBoardingPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.designsystem.R as DesignSystemR

// Refs:
// FTUE:
// - https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=133-5427&t=5SHVppfYzjvkEywR-0
// ElementX:
// - https://www.figma.com/file/0MMNu7cTOzLOlWb7ctTkv3/Element-X?type=design&node-id=1816-97419
@Composable
fun OnBoardingView(
    state: OnBoardingState,
    onBackClick: () -> Unit,
    onSignInWithQrCode: () -> Unit,
    onSignIn: (mustChooseAccountProvider: Boolean) -> Unit,
    onCreateAccount: () -> Unit,
    onOidcDetails: (OidcDetails) -> Unit,
    onNeedLoginPassword: () -> Unit,
    onLearnMoreClick: () -> Unit,
    onCreateAccountContinue: (url: String) -> Unit,
    onReportProblem: () -> Unit,
    onChangeServer: () -> Unit,
    onRequestInvite: (homeserverUrl: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val loginView = @Composable {
        LoginModeView(
            loginMode = state.loginMode,
            onClearError = {
                state.eventSink(OnBoardingEvents.ClearError)
            },
            onLearnMoreClick = onLearnMoreClick,
            onOidcDetails = onOidcDetails,
            onNeedLoginPassword = onNeedLoginPassword,
            onCreateAccountContinue = onCreateAccountContinue,
        )
    }
    val buttons = @Composable {
        OnBoardingButtons(
            state = state,
            onSignInWithQrCode = onSignInWithQrCode,
            onSignIn = onSignIn,
            onCreateAccount = onCreateAccount,
            onReportProblem = onReportProblem,
        )
    }
    val pressgramButtons = @Composable {
        PressgramOnboardingButtons(
            state = state,
            onSignInWithPassword = {
                // The server is already chosen on the welcome screen, so sign in
                // straight against it instead of routing through the provider picker.
                state.selectedHomeserverUrl?.let { url ->
                    state.eventSink(OnBoardingEvents.OnSignIn(url))
                }
            },
            onSignInWithQrCode = onSignInWithQrCode,
            onRegister = onCreateAccount,
            onRequestInvite = { state.selectedHomeserverUrl?.let(onRequestInvite) },
        )
    }

    if (state.isAddingAccount) {
        AddOtherAccountScaffold(
            modifier = modifier,
            loginView = loginView,
            buttons = buttons,
            onBackClick = onBackClick,
        )
    } else {
        AddFirstAccountScaffold(
            modifier = modifier,
            state = state,
            loginView = loginView,
            buttons = pressgramButtons,
            onChangeServer = onChangeServer,
        )
    }

    LoginWithElementClassicView(
        state = state.loginWithClassicState,
    )
}

@Composable
private fun LoginWithElementClassicView(
    state: LoginWithClassicState,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        state.eventSink(LoginWithClassicEvent.RefreshData)
    }
    AsyncActionView(
        async = state.loginWithClassicAction,
        confirmationDialog = { confirming ->
            when (confirming) {
                is ConfirmingLoginWithElementClassic -> {
                    // TODO i18n
                    ConfirmationDialog(
                        title = "Sign in with Element Classic",
                        content = "You are signing in as ${confirming.userId} on Element Classic." +
                            " Your existing session on Element Classic will not be signed out. Do you want to continue?",
                        submitText = stringResource(CommonStrings.action_continue),
                        onSubmitClick = { state.eventSink(LoginWithClassicEvent.DoLoginWithClassic) },
                        onDismiss = { state.eventSink(LoginWithClassicEvent.CloseDialog) },
                    )
                }
            }
        },
        onErrorDismiss = {
            state.eventSink(LoginWithClassicEvent.CloseDialog)
        },
        onSuccess = {
            // noop, the view will be closed
        }
    )
}

@Composable
private fun AddFirstAccountScaffold(
    state: OnBoardingState,
    loginView: @Composable () -> Unit,
    buttons: @Composable () -> Unit,
    onChangeServer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnBoardingPage(
        modifier = modifier,
        renderBackground = state.onBoardingLogoResId == null,
        content = {
            OnBoardingContent(
                state = state,
                onChangeServer = onChangeServer,
            )
            loginView()
        },
        footer = {
            buttons()
        }
    )
}

@Composable
private fun AddOtherAccountScaffold(
    loginView: @Composable () -> Unit,
    buttons: @Composable () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowStepPage(
        modifier = modifier,
        title = stringResource(CommonStrings.common_add_account),
        iconStyle = BigIcon.Style.Default(CompoundIcons.HomeSolid()),
        buttons = { buttons() },
        content = loginView,
        onBackClick = onBackClick,
    )
}

@Composable
private fun OnBoardingContent(
    state: OnBoardingState,
    onChangeServer: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            val pressgramLogo = painterResource(id = DesignSystemR.drawable.pressgram_logo)
            // The top logo is always the Pressgram brand logo, never a community logo.
            Image(
                painter = pressgramLogo,
                contentDescription = null,
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(22.dp)),
            )
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = CenterHorizontally,
            ) {
                Text(
                    text = state.selectedServerName
                        ?: stringResource(id = R.string.screen_server_detail_brand_name),
                    style = ElementTheme.typography.fontHeadingLgBold,
                    color = ElementTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.selectedServerDescription
                        ?: stringResource(id = R.string.screen_server_detail_pressgram_tagline),
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // The community logo comes from the registry API and is shown
                        // as a circle; the bundled Pressgram logo is the fallback.
                        AsyncImage(
                            model = state.selectedServerLogoUrl,
                            contentDescription = null,
                            placeholder = pressgramLogo,
                            error = pressgramLogo,
                            fallback = pressgramLogo,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(id = R.string.screen_onboarding_pressgram_server_label),
                                style = ElementTheme.typography.fontBodySmMedium,
                                color = ElementTheme.colors.textSecondary,
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.screen_onboarding_pressgram_server_value_format,
                                    state.selectedServerName.orEmpty(),
                                    state.selectedServerFqdn.orEmpty(),
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = ElementTheme.typography.fontBodySmMedium,
                                color = ElementTheme.colors.textPrimary,
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(
                        modifier = Modifier.clickable(onClick = onChangeServer),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = R.string.screen_onboarding_pressgram_change_server_inline),
                            style = ElementTheme.typography.fontBodySmMedium,
                            color = ElementTheme.colors.textPrimary,
                            textDecoration = TextDecoration.Underline,
                        )
                        Icon(
                            imageVector = CompoundIcons.ChevronRight(),
                            contentDescription = null,
                            tint = ElementTheme.colors.textPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnBoardingButtons(
    state: OnBoardingState,
    onSignInWithQrCode: () -> Unit,
    onSignIn: (mustChooseAccountProvider: Boolean) -> Unit,
    onCreateAccount: () -> Unit,
    onReportProblem: () -> Unit,
) {
    val isLoading by remember(state.loginMode) {
        derivedStateOf {
            state.loginMode is AsyncData.Loading
        }
    }

    ButtonColumnMolecule {
        val signInButtonStringRes = if (state.canLoginWithQrCode || state.canCreateAccount) {
            R.string.screen_onboarding_sign_in_manually
        } else {
            CommonStrings.action_continue
        }
        if (state.loginWithClassicState.canLoginWithClassic) {
            Button(
                text = "Sign in with Element Classic",
                leadingIcon = IconSource.Vector(CompoundIcons.Mobile()),
                onClick = {
                    state.loginWithClassicState.eventSink(
                        LoginWithClassicEvent.StartLoginWithClassic
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.canLoginWithQrCode) {
            Button(
                text = stringResource(id = R.string.screen_onboarding_sign_in_with_qr_code),
                leadingIcon = IconSource.Vector(CompoundIcons.QrCode()),
                onClick = onSignInWithQrCode,
                modifier = Modifier.fillMaxWidth()
            )
        }
        val defaultAccountProvider = state.defaultAccountProvider
        if (defaultAccountProvider == null) {
            Button(
                text = stringResource(id = signInButtonStringRes),
                onClick = {
                    onSignIn(state.mustChooseAccountProvider)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.onBoardingSignIn)
            )
        } else {
            Button(
                text = stringResource(id = R.string.screen_onboarding_sign_in_to, defaultAccountProvider),
                showProgress = isLoading,
                onClick = {
                    state.eventSink(OnBoardingEvents.OnSignIn(defaultAccountProvider))
                },
                enabled = state.submitEnabled || isLoading,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        if (state.canCreateAccount) {
            TextButton(
                text = stringResource(id = R.string.screen_onboarding_sign_up),
                onClick = onCreateAccount,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        if (state.isAddingAccount.not()) {
            if (state.canReportBug) {
                // Add a report problem text button. Use a Text since we need a special theme here.
                Text(
                    modifier = Modifier
                        .clickable(onClick = onReportProblem)
                        .padding(16.dp),
                    text = stringResource(id = CommonStrings.common_report_a_problem),
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            } else {
                Text(
                    modifier = Modifier
                        .clickable {
                            state.eventSink(OnBoardingEvents.OnVersionClick)
                        }
                        .padding(16.dp),
                    text = stringResource(id = R.string.screen_onboarding_app_version, state.version),
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun PressgramOnboardingButtons(
    state: OnBoardingState,
    onSignInWithPassword: () -> Unit,
    onSignInWithQrCode: () -> Unit,
    onRegister: () -> Unit,
    onRequestInvite: () -> Unit,
) {
    val isLoading by remember(state.loginMode) {
        derivedStateOf { state.loginMode is AsyncData.Loading }
    }
    ButtonColumnMolecule {
        Button(
            text = stringResource(id = R.string.screen_onboarding_pressgram_signin_password),
            showProgress = isLoading,
            enabled = !isLoading,
            onClick = onSignInWithPassword,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(
            text = stringResource(id = R.string.screen_onboarding_pressgram_signin_qr),
            onClick = onSignInWithQrCode,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = CenterHorizontally,
        ) {
            val registerTextRes = if (state.requiresInviteCode) {
                R.string.screen_onboarding_pressgram_register_with_token
            } else {
                R.string.screen_onboarding_pressgram_register_open
            }
            PressgramTextLink(
                text = stringResource(id = registerTextRes),
                onClick = onRegister,
            )
            if (state.requiresInviteCode) {
                PressgramTextLink(
                    text = stringResource(id = R.string.screen_onboarding_pressgram_request_invite),
                    onClick = onRequestInvite,
                )
            }
        }
    }
}

@Composable
private fun PressgramTextLink(
    text: String,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 16.dp),
        text = text,
        style = ElementTheme.typography.fontBodyMdMedium,
        color = ElementTheme.colors.textPrimary,
        textDecoration = TextDecoration.Underline,
        textAlign = TextAlign.Center,
    )
}

@PreviewsDayNight
@Composable
internal fun OnBoardingViewPreview(
    @PreviewParameter(OnBoardingStateProvider::class) state: OnBoardingState
) = ElementPreview {
    OnBoardingView(
        state = state,
        onBackClick = {},
        onSignInWithQrCode = {},
        onSignIn = {},
        onCreateAccount = {},
        onReportProblem = {},
        onOidcDetails = {},
        onNeedLoginPassword = {},
        onLearnMoreClick = {},
        onCreateAccountContinue = {},
        onChangeServer = {},
        onRequestInvite = {},
    )
}
