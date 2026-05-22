/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.inviterequest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.communityregistry.api.CommunityRegistryService
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.communityregistry.api.InviteRequestResult
import io.element.android.libraries.core.uri.ensureProtocol
import kotlinx.coroutines.launch
import timber.log.Timber

@AssistedInject
class InviteRequestPresenter(
    @Assisted private val params: Params,
    private val communityRegistryService: CommunityRegistryService,
) : Presenter<InviteRequestState> {
    data class Params(
        val homeserverUrl: String,
    )

    @AssistedFactory
    interface Factory {
        fun create(params: Params): InviteRequestPresenter
    }

    @Composable
    override fun present(): InviteRequestState {
        val coroutineScope = rememberCoroutineScope()
        var email by rememberSaveable { mutableStateOf("") }
        var message by rememberSaveable { mutableStateOf("") }
        var submitAction by remember { mutableStateOf<AsyncData<InviteRequestResult>>(AsyncData.Uninitialized) }

        // Resolve the server header (name + logo) from the registry.
        val server by produceState<CommunityServer?>(initialValue = null) {
            value = runCatching { communityRegistryService.getCommunityServers() }
                .onFailure { Timber.w(it, "InviteRequest: getCommunityServers failed") }
                .getOrNull()
                ?.firstOrNull { it.homeserver.ensureProtocol() == params.homeserverUrl }
        }

        fun handleEvent(event: InviteRequestEvents) {
            when (event) {
                is InviteRequestEvents.SetEmail -> {
                    email = event.email
                    submitAction = submitAction.clearIfError()
                }
                is InviteRequestEvents.SetMessage -> {
                    message = event.message
                    submitAction = submitAction.clearIfError()
                }
                InviteRequestEvents.Submit -> {
                    submitAction = AsyncData.Loading()
                    coroutineScope.launch {
                        submitAction = runCatching {
                            communityRegistryService.submitInviteRequest(
                                homeserver = params.homeserverUrl,
                                email = email,
                                message = message,
                            )
                        }
                            .onFailure { Timber.w(it, "InviteRequest: submitInviteRequest failed") }
                            .fold(
                                onSuccess = { AsyncData.Success(it) },
                                onFailure = { AsyncData.Failure(it) },
                            )
                    }
                }
            }
        }

        return InviteRequestState(
            serverName = server?.name,
            serverLogoUrl = server?.logoUrl,
            email = email,
            message = message,
            submitAction = submitAction,
            eventSink = ::handleEvent,
        )
    }
}

/**
 * Drop a previous error so the form is clean again as soon as the user edits a
 * field. A successful submission ([InviteRequestResult.Submitted]) is kept.
 */
private fun AsyncData<InviteRequestResult>.clearIfError(): AsyncData<InviteRequestResult> = when {
    this is AsyncData.Failure -> AsyncData.Uninitialized
    this is AsyncData.Success && data != InviteRequestResult.Submitted -> AsyncData.Uninitialized
    else -> this
}
