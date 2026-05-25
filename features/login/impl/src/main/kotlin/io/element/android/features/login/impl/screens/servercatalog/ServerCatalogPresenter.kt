/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.servercatalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.login.impl.accountprovider.SelectedHomeserverStore
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.map
import io.element.android.libraries.communityregistry.api.CommunityRegistryService
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.core.uri.ensureProtocol
import timber.log.Timber

@Inject
class ServerCatalogPresenter(
    private val communityRegistryService: CommunityRegistryService,
    private val selectedHomeserverStore: SelectedHomeserverStore,
    private val enterpriseService: EnterpriseService,
) : Presenter<ServerCatalogState> {
    @Composable
    override fun present(): ServerCatalogState {
        var retryCount by remember { mutableIntStateOf(0) }
        var searchQuery by remember { mutableStateOf("") }
        var pendingSelection by remember { mutableStateOf<String?>(null) }
        val persistedHomeserver by remember { selectedHomeserverStore.selectedHomeserverUrl() }
            .collectAsState(initial = null)
        val activeHomeserver = remember(persistedHomeserver) {
            persistedHomeserver?.takeIf { it.isNotBlank() }
                ?: enterpriseService.defaultHomeserverList().firstOrNull()?.ensureProtocol()
        }
        val selectedHomeserver = pendingSelection ?: activeHomeserver

        val servers by produceState<AsyncData<List<CommunityServer>>>(AsyncData.Loading(), retryCount) {
            value = AsyncData.Loading()
            value = runCatching { communityRegistryService.getCommunityServers() }
                .onFailure { Timber.w(it, "ServerCatalog: getCommunityServers failed") }
                .fold(
                    onSuccess = { AsyncData.Success(it) },
                    onFailure = { AsyncData.Failure(it) },
                )
        }
       val filteredServers = remember(servers, searchQuery) {
            val query = searchQuery.trim()
            if (query.isEmpty()) {
                servers
            } else {
                servers.map { list -> list.filter { it.matchesQuery(query) } }
            }
        }

        fun handleEvent(event: ServerCatalogEvents) {
            when (event) {
                ServerCatalogEvents.Retry -> retryCount++
                is ServerCatalogEvents.UpdateSearchQuery -> searchQuery = event.query
                is ServerCatalogEvents.SelectServer -> pendingSelection = event.homeserver.ensureProtocol()
            }
        }

        return ServerCatalogState(
            servers = filteredServers,
            searchQuery = searchQuery,
            selectedHomeserver = selectedHomeserver,
            eventSink = ::handleEvent,
        )
    }
}

private fun CommunityServer.matchesQuery(query: String): Boolean {
    return name.contains(query, ignoreCase = true) ||
        homeserver.contains(query, ignoreCase = true) ||
        description?.contains(query, ignoreCase = true) == true
}
