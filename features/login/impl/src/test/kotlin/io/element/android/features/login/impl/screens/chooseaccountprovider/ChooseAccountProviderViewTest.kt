/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.chooseaccountprovider

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChooseAccountProviderViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        ensureCalledOnce {
            rule.setChooseAccountProviderView(
                state = aChooseAccountProviderState(),
                onBackClick = it,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `tapping on an account provider invokes onServerClick with its url`() {
        val expectedUrl = ChooseAccountProviderPresenterTest.accountProvider1.url
        ensureCalledOnceWithParam(expectedUrl) { onServerClick ->
            rule.setChooseAccountProviderView(
                state = aChooseAccountProviderState(
                    accountProviders = listOf(
                        ChooseAccountProviderPresenterTest.accountProvider1,
                        ChooseAccountProviderPresenterTest.accountProvider2,
                    ),
                ),
                onServerClick = onServerClick,
            )
            rule.onNodeWithText(ChooseAccountProviderPresenterTest.accountProvider1.title).performClick()
        }
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setChooseAccountProviderView(
        state: ChooseAccountProviderState,
        onBackClick: () -> Unit = EnsureNeverCalled(),
        onServerClick: (String) -> Unit = EnsureNeverCalledWithParam(),
    ) {
        setContent {
            ChooseAccountProviderView(
                state = state,
                onBackClick = onBackClick,
                onServerClick = onServerClick,
            )
        }
    }
}
