/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import androidx.compose.ui.graphics.Color
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.features.enterprise.api.BugReportUrl
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.communityregistry.api.CommunityRegistryService
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URI

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultEnterpriseService(
    private val communityRegistryService: CommunityRegistryService,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) : EnterpriseService {
    private val cachedHomeservers = MutableStateFlow(listOf(DEFAULT_HOMESERVER))
    private val cachedAllowedHosts = MutableStateFlow<Set<String>?>(null)

    init {
        appCoroutineScope.launch {
            runCatching { communityRegistryService.getCommunityServers() }
                .onSuccess { servers ->
                    if (servers.isNotEmpty()) {
                        // Spec returns bare hostnames; downstream consumers expect URLs with scheme.
                        cachedHomeservers.value = servers.map { it.homeserver.ensureProtocol() }
                    }
                }
                .onFailure { Timber.w(it, "CommunityRegistry: getCommunityServers failed") }
        }
        appCoroutineScope.launch {
            runCatching { communityRegistryService.getServerWhitelist() }
                .onSuccess { cachedAllowedHosts.value = it.toSet() }
                .onFailure { Timber.w(it, "CommunityRegistry: getServerWhitelist failed") }
        }
    }

    override val isEnterpriseBuild = false

    override suspend fun isEnterpriseUser(sessionId: SessionId) = false

    override fun defaultHomeserverList(): List<String> = cachedHomeservers.value

    override suspend fun isAllowedToConnectToHomeserver(homeserverUrl: String): Boolean {
        val whitelist = cachedAllowedHosts.value ?: return true
        val host = runCatching { URI(homeserverUrl).host }.getOrNull() ?: return false
        return whitelist.any { allowed -> host == allowed || host.endsWith(".$allowed") }
    }

    override suspend fun overrideBrandColor(sessionId: SessionId?, brandColor: String?) = Unit

    override fun brandColorsFlow(sessionId: SessionId?): Flow<Color?> {
        return flowOf(null)
    }

    override fun semanticColorsFlow(sessionId: SessionId?): Flow<SemanticColorsLightDark> {
        return flowOf(SemanticColorsLightDark.default)
    }

    override fun firebasePushGateway(): String? = null
    override fun unifiedPushDefaultPushGateway(): String? = null

    override fun bugReportUrlFlow(sessionId: SessionId?): Flow<BugReportUrl> {
        return flowOf(BugReportUrl.UseDefault)
    }

    override fun getNoisyNotificationChannelId(sessionId: SessionId): String? = null

    private companion object {
        const val DEFAULT_HOMESERVER = "https://pgram.im"
    }
}
