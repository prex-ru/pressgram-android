/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.serverdetail

import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.features.login.impl.screens.onboarding.createLoginHelper
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.communityregistry.api.Registration
import io.element.android.libraries.communityregistry.test.FakeCommunityRegistryService
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.matrix.test.auth.aMatrixHomeServerDetails
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ServerDetailPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    companion object {
        private const val HOMESERVER_URL = "https://newsroom.pgram.im"
        private val openServer = CommunityServer(
            homeserver = "newsroom.pgram.im",
            name = "Newsroom",
            description = "Открытый сервер редакции",
            logoUrl = null,
            type = "official",
            registration = Registration.Open,
        )
        private val tokenServer = openServer.copy(
            homeserver = "prexplore.pgram.im",
            name = "Prexplore",
            registration = Registration.Token(instructions = "Ask the editor", contact = "editor@example.com"),
        )
        private val closedServer = openServer.copy(
            homeserver = "archive.pgram.im",
            name = "Archive",
            registration = Registration.Closed,
        )
    }

    @Test
    fun `present - loads metadata for matching homeserver`() = runTest {
        val presenter = createPresenter(
            homeserverUrl = HOMESERVER_URL,
            communityRegistryService = FakeCommunityRegistryService(
                getCommunityServersResult = { listOf(openServer, tokenServer, closedServer) },
            ),
        )
        presenter.test {
            assertThat(awaitItem().serverInfo).isInstanceOf(AsyncData.Loading::class.java)
            val ready = awaitItem()
            val info = (ready.serverInfo as AsyncData.Success).data
            assertThat(info.name).isEqualTo("Newsroom")
            assertThat(info.registration).isEqualTo(Registration.Open)
        }
    }

    @Test
    fun `present - falls back to Unknown when homeserver not in catalog`() = runTest {
        val presenter = createPresenter(
            homeserverUrl = "https://unknown.pgram.im",
            communityRegistryService = FakeCommunityRegistryService(
                getCommunityServersResult = { listOf(openServer) },
            ),
        )
        presenter.test {
            skipItems(1) // Loading
            val ready = awaitItem()
            val info = (ready.serverInfo as AsyncData.Success).data
            assertThat(info.registration).isEqualTo(Registration.Unknown)
            assertThat(info.name).isEqualTo("unknown.pgram.im")
        }
    }

    @Test
    fun `present - Register triggers OIDC submit with account-creation prompt`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsOidcLogin = true))
            },
        )
        val presenter = createPresenter(
            homeserverUrl = HOMESERVER_URL,
            communityRegistryService = FakeCommunityRegistryService(
                getCommunityServersResult = { listOf(openServer) },
            ),
            authenticationService = authenticationService,
        )
        presenter.test {
            skipItems(1) // Loading metadata
            val ready = awaitItem()
            ready.eventSink(ServerDetailEvents.Register)
            skipItems(1) // Loading
            val successState = awaitItem()
            assertThat(successState.loginMode).isInstanceOf(AsyncData.Success::class.java)
            assertThat(successState.loginMode.dataOrNull()).isInstanceOf(LoginMode.Oidc::class.java)
        }
    }

    @Test
    fun `present - SignIn triggers loginHelper submit with isAccountCreation=false`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.success(aMatrixHomeServerDetails(supportsPasswordLogin = true))
            },
        )
        val presenter = createPresenter(
            homeserverUrl = HOMESERVER_URL,
            communityRegistryService = FakeCommunityRegistryService(
                getCommunityServersResult = { listOf(openServer) },
            ),
            authenticationService = authenticationService,
        )
        presenter.test {
            skipItems(1) // Loading metadata
            val ready = awaitItem()
            ready.eventSink(ServerDetailEvents.SignIn)
            skipItems(1) // Loading
            val successState = awaitItem()
            assertThat(successState.loginMode.dataOrNull()).isEqualTo(LoginMode.PasswordLogin)
        }
    }

    @Test
    fun `present - ClearError resets loginMode after failure`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = { Result.failure(AN_EXCEPTION) },
        )
        val presenter = createPresenter(
            homeserverUrl = HOMESERVER_URL,
            communityRegistryService = FakeCommunityRegistryService(
                getCommunityServersResult = { listOf(openServer) },
            ),
            authenticationService = authenticationService,
        )
        presenter.test {
            skipItems(1) // Loading metadata
            val ready = awaitItem()
            ready.eventSink(ServerDetailEvents.SignIn)
            skipItems(1) // Loading
            val failed = awaitItem()
            assertThat(failed.loginMode).isInstanceOf(AsyncData.Failure::class.java)
            failed.eventSink(ServerDetailEvents.ClearError)
            val cleared = awaitItem()
            assertThat(cleared.loginMode).isEqualTo(AsyncData.Uninitialized)
        }
    }

    private fun createPresenter(
        homeserverUrl: String,
        communityRegistryService: FakeCommunityRegistryService = FakeCommunityRegistryService(),
        authenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
    ) = ServerDetailPresenter(
        params = ServerDetailPresenter.Params(homeserverUrl = homeserverUrl),
        communityRegistryService = communityRegistryService,
        accountProviderDataSource = AccountProviderDataSource(FakeEnterpriseService()),
        loginHelper = createLoginHelper(authenticationService = authenticationService),
    )
}
