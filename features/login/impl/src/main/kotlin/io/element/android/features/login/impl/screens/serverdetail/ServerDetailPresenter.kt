/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.serverdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.login.LoginHelper
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.communityregistry.api.CommunityRegistryService
import io.element.android.libraries.communityregistry.api.Registration
import io.element.android.libraries.core.uri.ensureProtocol
import kotlinx.coroutines.launch
import timber.log.Timber

@AssistedInject
class ServerDetailPresenter(
    @Assisted private val params: Params,
    private val communityRegistryService: CommunityRegistryService,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val loginHelper: LoginHelper,
) : Presenter<ServerDetailState> {
    data class Params(
        val homeserverUrl: String,
    )

    @AssistedFactory
    interface Factory {
        fun create(params: Params): ServerDetailPresenter
    }

    @Composable
    override fun present(): ServerDetailState {
        val localCoroutineScope = rememberCoroutineScope()
        val loginMode by loginHelper.collectLoginMode()
        val homeserverUrl = params.homeserverUrl

        val serverInfo by produceState<AsyncData<ServerDetailInfo>>(AsyncData.Loading()) {
            value = runCatching { communityRegistryService.getCommunityServers() }
                .onFailure { Timber.w(it, "ServerDetail: getCommunityServers failed") }
                .map { servers ->
                    val match = servers.firstOrNull { it.homeserver.ensureProtocol() == homeserverUrl }
                    if (match != null) {
                        AsyncData.Success(
                            ServerDetailInfo(
                                homeserverUrl = match.homeserver.ensureProtocol(),
                                name = match.name,
                                description = match.description,
                                type = match.type,
                                registration = match.registration,
                            )
                        )
                    } else {
                        AsyncData.Success(
                            ServerDetailInfo(
                                homeserverUrl = homeserverUrl,
                                name = homeserverUrl.removePrefix("https://").removePrefix("http://"),
                                description = null,
                                type = null,
                                registration = Registration.Unknown,
                            )
                        )
                    }
                }
                .getOrElse { AsyncData.Failure(it) }
        }

        fun handleEvent(event: ServerDetailEvents) {
            when (event) {
                ServerDetailEvents.Register -> localCoroutineScope.launch {
                    accountProviderDataSource.setUrl(homeserverUrl)
                    loginHelper.submit(
                        isAccountCreation = true,
                        homeserverUrl = homeserverUrl,
                        loginHint = null,
                    )
                }
                ServerDetailEvents.SignIn -> localCoroutineScope.launch {
                    accountProviderDataSource.setUrl(homeserverUrl)
                    loginHelper.submit(
                        isAccountCreation = false,
                        homeserverUrl = homeserverUrl,
                        loginHint = null,
                    )
                }
                ServerDetailEvents.ClearError -> loginHelper.clearError()
            }
        }

        return ServerDetailState(
            serverInfo = serverInfo,
            loginMode = loginMode,
            eventSink = ::handleEvent,
        )
    }
}
