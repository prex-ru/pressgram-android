/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.chooseaccountprovider

import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_2
import io.element.android.libraries.matrix.test.AN_ACCOUNT_PROVIDER_3
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChooseAccountProviderPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    companion object {
        private const val ACCOUNT_PROVIDER_FROM_CONFIG_1 = AN_ACCOUNT_PROVIDER_2
        private const val ACCOUNT_PROVIDER_FROM_CONFIG_2 = AN_ACCOUNT_PROVIDER_3
        val accountProvider1 = AccountProvider(
            url = ACCOUNT_PROVIDER_FROM_CONFIG_1.ensureProtocol(),
            subtitle = null,
            isPublic = false,
            isMatrixOrg = false,
        )
        val accountProvider2 = AccountProvider(
            url = ACCOUNT_PROVIDER_FROM_CONFIG_2.ensureProtocol(),
            subtitle = null,
            isPublic = false,
            isMatrixOrg = false,
        )
    }

    @Test
    fun `present - initial state exposes account providers from enterprise service`() = runTest {
        val presenter = createPresenter(
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG_1, ACCOUNT_PROVIDER_FROM_CONFIG_2) },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.accountProviders).containsExactly(
                accountProvider1,
                accountProvider2,
            )
        }
    }
}

private fun createPresenter(
    enterpriseService: EnterpriseService = FakeEnterpriseService(),
) = ChooseAccountProviderPresenter(
    enterpriseService = enterpriseService,
)
