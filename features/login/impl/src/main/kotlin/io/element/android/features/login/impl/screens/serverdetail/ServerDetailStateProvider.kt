/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.serverdetail

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.communityregistry.api.Registration

open class ServerDetailStateProvider : PreviewParameterProvider<ServerDetailState> {
    override val values: Sequence<ServerDetailState>
        get() = sequenceOf(
            aServerDetailState(serverInfo = AsyncData.Loading()),
            aServerDetailState(
                serverInfo = AsyncData.Success(
                    aServerDetailInfo(
                        homeserverUrl = "https://pgram.im",
                        name = "Pressgram",
                        description = "Главный сервер сети. Открытая регистрация для всех журналистов и медиапрофессионалов.",
                        registration = Registration.Open,
                    )
                ),
            ),
            aServerDetailState(
                serverInfo = AsyncData.Success(
                    aServerDetailInfo(
                        homeserverUrl = "https://newsroom.pgram.im",
                        name = "Редакция «Новости»",
                        description = "Внутренний сервер редакции газеты «Новости».",
                        registration = Registration.Token(
                            instructions = "Приглашения выдаются штатным сотрудникам редакции.",
                            contact = "invites@newsroom.example.com",
                        ),
                    )
                ),
            ),
            aServerDetailState(
                serverInfo = AsyncData.Success(
                    aServerDetailInfo(
                        homeserverUrl = "https://archive.pgram.im",
                        name = "Архив",
                        description = "Закрытый архивный сервер.",
                        registration = Registration.Closed,
                    )
                ),
            ),
            aServerDetailState(
                serverInfo = AsyncData.Success(
                    aServerDetailInfo(
                        registration = Registration.Open,
                    )
                ),
                loginMode = AsyncData.Loading(),
            ),
        )
}

internal fun aServerDetailInfo(
    homeserverUrl: String = "https://example.pgram.im",
    name: String = "Example",
    description: String? = null,
    type: String? = null,
    registration: Registration = Registration.Open,
) = ServerDetailInfo(
    homeserverUrl = homeserverUrl,
    name = name,
    description = description,
    type = type,
    registration = registration,
)

internal fun aServerDetailState(
    serverInfo: AsyncData<ServerDetailInfo> = AsyncData.Success(aServerDetailInfo()),
    loginMode: AsyncData<LoginMode> = AsyncData.Uninitialized,
    eventSink: (ServerDetailEvents) -> Unit = {},
) = ServerDetailState(
    serverInfo = serverInfo,
    loginMode = loginMode,
    eventSink = eventSink,
)
