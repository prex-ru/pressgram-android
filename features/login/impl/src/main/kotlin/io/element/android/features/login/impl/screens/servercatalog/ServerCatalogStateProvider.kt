/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.servercatalog

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.communityregistry.api.Registration

open class ServerCatalogStateProvider : PreviewParameterProvider<ServerCatalogState> {
    override val values: Sequence<ServerCatalogState>
        get() = sequenceOf(
            aServerCatalogState(servers = AsyncData.Loading()),
            aServerCatalogState(servers = AsyncData.Failure(Exception("network error"))),
            aServerCatalogState(
                servers = AsyncData.Success(aCommunityServerList()),
                selectedHomeserver = "https://pgram.im",
            ),
            aServerCatalogState(
                servers = AsyncData.Success(emptyList()),
                searchQuery = "no match",
            ),
        )
}

internal fun aCommunityServerList() = listOf(
    CommunityServer(
        homeserver = "pgram.im",
        name = "Pressgram",
        description = "Главный сервер сети. Открытая регистрация для всех журналистов и медиапрофессионалов.",
        type = "community",
        registration = Registration.Open,
    ),
    CommunityServer(
        homeserver = "newsroom.pgram.im",
        name = "Редакция «Новости»",
        description = "Внутренний сервер редакции газеты «Новости». Журналисты, редакторы, корреспонденты.",
        type = "media",
        registration = Registration.Token(instructions = null, contact = null),
    ),
    CommunityServer(
        homeserver = "archive.pgram.im",
        name = "Архив",
        description = "Закрытый архивный сервер.",
        type = "other",
        registration = Registration.Closed,
    ),
)

internal fun aServerCatalogState(
    servers: AsyncData<List<CommunityServer>> = AsyncData.Success(aCommunityServerList()),
    searchQuery: String = "",
    selectedHomeserver: String? = null,
    eventSink: (ServerCatalogEvents) -> Unit = {},
) = ServerCatalogState(
    servers = servers,
    searchQuery = searchQuery,
    selectedHomeserver = selectedHomeserver,
    eventSink = eventSink,
)
