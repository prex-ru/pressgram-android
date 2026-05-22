/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.communityregistry.api.CheckInviteResult
import io.element.android.libraries.communityregistry.api.CommunityRegistryService
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.communityregistry.api.InviteRequestResult
import timber.log.Timber

/**
 * [CommunityRegistryService] decorator that logs every call — its request and its
 * response — so the registry API is visible in Logcat (filter on [TAG]).
 *
 * It wraps the active data source ([MockCommunityRegistryService] for now). Secrets
 * and user content (invite token, requester email, the message body) are NOT logged,
 * per the project logging rules — only non-sensitive parameters and a result summary.
 */
@Inject
@ContributesBinding(AppScope::class)
class LoggingCommunityRegistryService(
    private val delegate: MockCommunityRegistryService,
) : CommunityRegistryService {
    override suspend fun getCommunityServers(): List<CommunityServer> =
        logCall(
            request = "getCommunityServers()",
            summary = { "${it.size} servers" },
        ) { delegate.getCommunityServers() }

    override suspend fun getServerWhitelist(): List<String> =
        logCall(
            request = "getServerWhitelist()",
            summary = { "${it.size} domains" },
        ) { delegate.getServerWhitelist() }

    override suspend fun checkInvite(homeserver: String, token: String): CheckInviteResult =
        logCall(
            request = "checkInvite(homeserver=$homeserver, token=***)",
            summary = { it::class.simpleName.orEmpty() },
        ) { delegate.checkInvite(homeserver, token) }

    override suspend fun submitInviteRequest(
        homeserver: String,
        email: String,
        message: String,
    ): InviteRequestResult =
        logCall(
            request = "submitInviteRequest(homeserver=$homeserver, email=***, messageLength=${message.length})",
            summary = { it::class.simpleName.orEmpty() },
        ) { delegate.submitInviteRequest(homeserver, email, message) }

    private suspend fun <T> logCall(
        request: String,
        summary: (T) -> String,
        block: suspend () -> T,
    ): T {
        Timber.d("$TAG → $request")
        return try {
            block().also { Timber.d("$TAG ← $request: ${summary(it)}") }
        } catch (error: Throwable) {
            Timber.w(error, "$TAG ✗ $request")
            throw error
        }
    }

    private companion object {
        const val TAG = "CommunityRegistry"
    }
}
