/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.features.enterprise.api.BugReportUrl
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.communityregistry.api.Registration
import io.element.android.libraries.communityregistry.test.FakeCommunityRegistryService
import io.element.android.libraries.matrix.test.A_SESSION_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultEnterpriseServiceTest {
    @Test
    fun `isEnterpriseBuild is false`() = runTest {
        val service = createService(scope = this)
        assertThat(service.isEnterpriseBuild).isFalse()
    }

    @Test
    fun `defaultHomeserverList returns default before registry loads`() = runTest {
        val service = createService(
            scope = this,
            communityRegistry = FakeCommunityRegistryService(
                getCommunityServersResult = { emptyList() }
            ),
        )
        assertThat(service.defaultHomeserverList()).containsExactly("https://pgram.im")
    }

    @Test
    fun `defaultHomeserverList is replaced once registry returns servers`() = runTest {
        val service = createService(
            scope = this,
            communityRegistry = FakeCommunityRegistryService(
                getCommunityServersResult = {
                    listOf(
                        CommunityServer(
                            homeserver = "newsroom.pgram.im",
                            name = "Newsroom",
                            description = null,
                            logoUrl = null,
                            type = "official",
                            registration = Registration.Open,
                        ),
                        CommunityServer(
                            homeserver = "prexplore.pgram.im",
                            name = "Prexplore",
                            description = null,
                            logoUrl = null,
                            type = "community",
                            registration = Registration.Token(instructions = null, contact = null),
                        ),
                    )
                },
            ),
        )
        advanceUntilIdle()
        assertThat(service.defaultHomeserverList())
            .containsExactly("https://newsroom.pgram.im", "https://prexplore.pgram.im")
            .inOrder()
    }

    @Test
    fun `defaultHomeserverList keeps default when registry returns empty list`() = runTest {
        val service = createService(
            scope = this,
            communityRegistry = FakeCommunityRegistryService(
                getCommunityServersResult = { emptyList() }
            ),
        )
        advanceUntilIdle()
        assertThat(service.defaultHomeserverList()).containsExactly("https://pgram.im")
    }

    @Test
    fun `defaultHomeserverList keeps default when registry throws`() = runTest {
        val service = createService(
            scope = this,
            communityRegistry = FakeCommunityRegistryService(
                getCommunityServersResult = { error("network failure") }
            ),
        )
        advanceUntilIdle()
        assertThat(service.defaultHomeserverList()).containsExactly("https://pgram.im")
    }

    @Test
    fun `isAllowedToConnectToHomeserver allows everything before whitelist loads`() = runTest {
        val service = createService(
            scope = this,
            communityRegistry = FakeCommunityRegistryService(
                getServerWhitelistResult = { error("not yet loaded") }
            ),
        )
        assertThat(service.isAllowedToConnectToHomeserver("https://random.example.org")).isTrue()
    }

    @Test
    fun `isAllowedToConnectToHomeserver respects loaded whitelist`() = runTest {
        val service = createService(
            scope = this,
            communityRegistry = FakeCommunityRegistryService(
                getServerWhitelistResult = { listOf("pgram.im", "trusted.example") }
            ),
        )
        advanceUntilIdle()
        assertThat(service.isAllowedToConnectToHomeserver("https://pgram.im")).isTrue()
        assertThat(service.isAllowedToConnectToHomeserver("https://newsroom.pgram.im")).isTrue()
        assertThat(service.isAllowedToConnectToHomeserver("https://trusted.example")).isTrue()
        assertThat(service.isAllowedToConnectToHomeserver("https://evil.example")).isFalse()
    }

    @Test
    fun `isAllowedToConnectToHomeserver returns false for malformed url`() = runTest {
        val service = createService(
            scope = this,
            communityRegistry = FakeCommunityRegistryService(
                getServerWhitelistResult = { listOf("pgram.im") }
            ),
        )
        advanceUntilIdle()
        assertThat(service.isAllowedToConnectToHomeserver("not a url")).isFalse()
    }

    @Test
    fun `isEnterpriseUser always return false`() = runTest {
        val service = createService(scope = this)
        assertThat(service.isEnterpriseUser(A_SESSION_ID)).isFalse()
    }

    @Test
    fun `semanticColorsFlow always emits the same value`() = runTest {
        val service = createService(scope = this)
        service.semanticColorsFlow(null).test {
            assertThat(awaitItem()).isEqualTo(SemanticColorsLightDark.default)
            awaitComplete()
        }
    }

    @Test
    fun `brandColorsFlow always emits null`() = runTest {
        val service = createService(scope = this)
        service.brandColorsFlow(null).test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `semanticColorsFlow always emits the same value for a session`() = runTest {
        val service = createService(scope = this)
        service.semanticColorsFlow(A_SESSION_ID).test {
            assertThat(awaitItem()).isEqualTo(SemanticColorsLightDark.default)
            awaitComplete()
        }
    }

    @Test
    fun `overrideBrandColor has no effect`() = runTest {
        val service = createService(scope = this)
        service.overrideBrandColor(A_SESSION_ID, "aColor")
    }

    @Test
    fun `firebasePushGateway returns null`() = runTest {
        val service = createService(scope = this)
        assertThat(service.firebasePushGateway()).isNull()
    }

    @Test
    fun `unifiedPushDefaultPushGateway returns null`() = runTest {
        val service = createService(scope = this)
        assertThat(service.unifiedPushDefaultPushGateway()).isNull()
    }

    @Test
    fun `bugReportUrlFlow only emits UseDefault`() = runTest {
        val service = createService(scope = this)
        service.bugReportUrlFlow(A_SESSION_ID).test {
            assertThat(awaitItem()).isEqualTo(BugReportUrl.UseDefault)
            awaitComplete()
        }
    }

    @Test
    fun `getNoisyNotificationChannelId returns null`() = runTest {
        val service = createService(scope = this)
        assertThat(service.getNoisyNotificationChannelId(A_SESSION_ID)).isNull()
    }

    private fun createService(
        scope: TestScope,
        communityRegistry: FakeCommunityRegistryService = FakeCommunityRegistryService(),
    ): DefaultEnterpriseService = DefaultEnterpriseService(
        communityRegistryService = communityRegistry,
        appCoroutineScope = scope as CoroutineScope,
    )
}
