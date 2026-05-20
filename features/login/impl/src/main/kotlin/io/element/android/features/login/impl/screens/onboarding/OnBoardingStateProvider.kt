/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.features.login.impl.screens.onboarding.classic.LoginWithClassicState
import io.element.android.features.login.impl.screens.onboarding.classic.aLoginWithClassicState
import io.element.android.libraries.architecture.AsyncData

open class OnBoardingStateProvider : PreviewParameterProvider<OnBoardingState> {
    override val values: Sequence<OnBoardingState>
        get() = sequenceOf(
//            anOnBoardingState(canLoginWithQrCode = true),
//            anOnBoardingState(canCreateAccount = true),
//            anOnBoardingState(canLoginWithQrCode = true, canCreateAccount = true),
//            anOnBoardingState(canLoginWithQrCode = true, canCreateAccount = true, canReportBug = true),
//            anOnBoardingState(defaultAccountProvider = "element.io", canCreateAccount = false, canReportBug = true),
//            anOnBoardingState(customLogoResId = R.drawable.sample_background),
//            anOnBoardingState(
//                isAddingAccount = true,
//                canLoginWithQrCode = true,
//                canCreateAccount = true,
//            ),
            // Pressgram welcome — Open registration variant.
            anOnBoardingState(
                selectedHomeserverUrl = "https://pgram.im",
                selectedServerName = "Pressgram",
                selectedServerFqdn = "pgram.im",
                selectedServerDescription = "Главный сервер сети. Открытая регистрация для всех журналистов и медиапрофессионалов.",
                requiresInviteCode = false,
            ),
            // Pressgram welcome — Token (invitation-only) variant.
            anOnBoardingState(
                selectedHomeserverUrl = "https://newsroom.pgram.im",
                selectedServerName = "Редакция «Новости»",
                selectedServerFqdn = "newsroom.pgram.im",
                selectedServerDescription = "Внутренний сервер редакции газеты «Новости». Журналисты, редакторы, корреспонденты.",
                requiresInviteCode = true,
            ),
        )
}

fun anOnBoardingState(
    isAddingAccount: Boolean = false,
    productionApplicationName: String = "Pressgram",
    defaultAccountProvider: String? = null,
    mustChooseAccountProvider: Boolean = false,
    canLoginWithQrCode: Boolean = false,
    canCreateAccount: Boolean = false,
    canReportBug: Boolean = false,
    version: String = "1.0.0",
    @DrawableRes
    customLogoResId: Int? = null,
    loginMode: AsyncData<LoginMode> = AsyncData.Uninitialized,
    loginWithClassicState: LoginWithClassicState = aLoginWithClassicState(),
    selectedHomeserverUrl: String? = null,
    selectedServerName: String? = null,
    selectedServerFqdn: String? = null,
    selectedServerDescription: String? = null,
    selectedServerLogoUrl: String? = null,
    requiresInviteCode: Boolean = false,
    eventSink: (OnBoardingEvents) -> Unit = {},
) = OnBoardingState(
    isAddingAccount = isAddingAccount,
    productionApplicationName = productionApplicationName,
    defaultAccountProvider = defaultAccountProvider,
    mustChooseAccountProvider = mustChooseAccountProvider,
    canLoginWithQrCode = canLoginWithQrCode,
    canCreateAccount = canCreateAccount,
    canReportBug = canReportBug,
    version = version,
    loginMode = loginMode,
    onBoardingLogoResId = customLogoResId,
    loginWithClassicState = loginWithClassicState,
    selectedHomeserverUrl = selectedHomeserverUrl,
    selectedServerName = selectedServerName,
    selectedServerFqdn = selectedServerFqdn,
    selectedServerDescription = selectedServerDescription,
    selectedServerLogoUrl = selectedServerLogoUrl,
    requiresInviteCode = requiresInviteCode,
    eventSink = eventSink,
)
