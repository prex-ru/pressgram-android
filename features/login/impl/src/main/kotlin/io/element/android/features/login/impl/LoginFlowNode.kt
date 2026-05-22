/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl

import android.app.Activity
import android.os.Parcelable
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.login.api.LoginEntryPoint
import io.element.android.features.login.impl.accountprovider.AccountProviderDataSource
import io.element.android.features.login.impl.accountprovider.SelectedHomeserverStore
import io.element.android.features.login.impl.qrcode.QrCodeLoginFlowNode
import io.element.android.features.login.impl.screens.changeaccountprovider.ChangeAccountProviderNode
import io.element.android.features.login.impl.screens.chooseaccountprovider.ChooseAccountProviderNode
import io.element.android.features.login.impl.screens.confirmaccountprovider.ConfirmAccountProviderNode
import io.element.android.features.login.impl.screens.createaccount.CreateAccountNode
import io.element.android.features.login.impl.screens.inviterequest.InviteRequestNode
import io.element.android.features.login.impl.screens.loginpassword.LoginPasswordNode
import io.element.android.features.login.impl.screens.onboarding.OnBoardingNode
import io.element.android.features.login.impl.screens.searchaccountprovider.SearchAccountProviderNode
import io.element.android.features.login.impl.screens.servercatalog.ServerCatalogNode
import io.element.android.features.login.impl.screens.serverdetail.ServerDetailNode
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcActionFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
@AssistedInject
class LoginFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val selectedHomeserverStore: SelectedHomeserverStore,
    private val oidcActionFlow: OidcActionFlow,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
) : BaseFlowNode<LoginFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.OnBoarding,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    data class Params(
        val accountProvider: String?,
        val loginHint: String?,
    ) : NodeInputs

    private val callback: LoginEntryPoint.Callback = callback()
    private var activity: Activity? = null
    private var darkTheme: Boolean = false

    private var externalAppStarted = false

    override fun onBuilt() {
        super.onBuilt()
        lifecycleScope.launch {
            selectedHomeserverStore.selectedHomeserverUrl().first()
                ?.takeIf { it.isNotBlank() }
                ?.let { accountProviderDataSource.setUrl(it) }
        }
        lifecycle.subscribe(
            onResume = {
                if (externalAppStarted) {
                    externalAppStarted = false
                    // Workaround to detect that the Custom Chrome Tab has been closed
                    // If there is no coming OidcAction (that would end this Node),
                    // consider that the user has cancelled the login
                    // by pressing back or by closing the Custom Chrome Tab.
                    lifecycleScope.launch {
                        delay(5000)
                        oidcActionFlow.post(OidcAction.GoBack(toUnblock = true))
                    }
                }
            }
        )
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object OnBoarding : NavTarget

        @Parcelize
        data object QrCode : NavTarget

        @Parcelize
        data class ConfirmAccountProvider(
            val isAccountCreation: Boolean,
        ) : NavTarget

        @Parcelize
        data object ChooseAccountProvider : NavTarget

        @Parcelize
        data object ChangeAccountProvider : NavTarget

        @Parcelize
        data object SearchAccountProvider : NavTarget

        @Parcelize
        data object LoginPassword : NavTarget

        @Parcelize
        data class CreateAccount(val url: String) : NavTarget

        @Parcelize
        data class ServerDetail(val homeserverUrl: String) : NavTarget

        @Parcelize
        data object ServerCatalog : NavTarget

        @Parcelize
        data class InviteRequest(val homeserverUrl: String) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.OnBoarding -> {
                val callback = object : OnBoardingNode.Callback {
                    override fun navigateToSignUpFlow() {
                        backstack.push(
                            NavTarget.ConfirmAccountProvider(isAccountCreation = true)
                        )
                    }

                    override fun navigateToSignInFlow(mustChooseAccountProvider: Boolean) {
                        backstack.push(
                            if (mustChooseAccountProvider) {
                                NavTarget.ChooseAccountProvider
                            } else {
                                NavTarget.ConfirmAccountProvider(isAccountCreation = false)
                            }
                        )
                    }

                    override fun navigateToQrCode() {
                        backstack.push(NavTarget.QrCode)
                    }

                    override fun navigateToBugReport() {
                        callback.navigateToBugReport()
                    }

                    override fun navigateToOidc(oidcDetails: OidcDetails) {
                        navigateToMas(oidcDetails)
                    }

                    override fun navigateToCreateAccount(url: String) {
                        backstack.push(NavTarget.CreateAccount(url))
                    }

                    override fun navigateToLoginPassword() {
                        backstack.push(NavTarget.LoginPassword)
                    }

                    override fun onDone() {
                        callback.onDone()
                    }

                    override fun navigateToChangeServer() {
                        backstack.push(NavTarget.ServerCatalog)
                    }

                    override fun navigateToRequestInvite(homeserverUrl: String) {
                        backstack.push(NavTarget.InviteRequest(homeserverUrl))
                    }
                }
                val params = inputs<Params>()
                val inputs = OnBoardingNode.Params(
                    accountProvider = params.accountProvider,
                    loginHint = params.loginHint,
                )
                createNode<OnBoardingNode>(buildContext, listOf(callback, inputs))
            }
            NavTarget.ChooseAccountProvider -> {
                val callback = object : ChooseAccountProviderNode.Callback {
                    override fun navigateToServerDetail(homeserverUrl: String) {
                        backstack.push(NavTarget.ServerDetail(homeserverUrl))
                    }
                }
                createNode<ChooseAccountProviderNode>(buildContext, listOf(callback))
            }
            is NavTarget.ServerDetail -> {
                val inputs = ServerDetailNode.Inputs(homeserverUrl = navTarget.homeserverUrl)
                val callback = object : ServerDetailNode.Callback {
                    override fun navigateToOidc(oidcDetails: OidcDetails) {
                        navigateToMas(oidcDetails)
                    }

                    override fun navigateToCreateAccount(url: String) {
                        backstack.push(NavTarget.CreateAccount(url))
                    }

                    override fun navigateToLoginPassword() {
                        backstack.push(NavTarget.LoginPassword)
                    }

                    override fun navigateToChangeServer() {
                        backstack.singleTop(NavTarget.ServerCatalog)
                    }

                    override fun navigateToRequestInvite() {
                        backstack.push(NavTarget.InviteRequest(navTarget.homeserverUrl))
                    }
                }
                createNode<ServerDetailNode>(buildContext, plugins = listOf(inputs, callback))
            }
            NavTarget.ServerCatalog -> {
                val callback = object : ServerCatalogNode.Callback {
                    override fun onServerSelected(homeserverUrl: String) {
                        // Persist the picked server (survives restart) and update the
                        // in-flow account provider so every login screen reflects it;
                        // then return to wherever the catalog was opened from.
                        val url = homeserverUrl.ensureProtocol()
                        appCoroutineScope.launch {
                            selectedHomeserverStore.setSelectedHomeserverUrl(url)
                            accountProviderDataSource.setUrl(url)
                        }
                        backstack.pop()
                    }
                }
                createNode<ServerCatalogNode>(buildContext, listOf(callback))
            }
            is NavTarget.InviteRequest -> {
                val inputs = InviteRequestNode.Inputs(homeserverUrl = navTarget.homeserverUrl)
                createNode<InviteRequestNode>(buildContext, listOf(inputs))
            }
            NavTarget.QrCode -> {
                val callback = object : QrCodeLoginFlowNode.Callback {
                    override fun navigateBack() {
                        backstack.pop()
                    }
                }
                createNode<QrCodeLoginFlowNode>(buildContext, listOf(callback))
            }
            is NavTarget.ConfirmAccountProvider -> {
                val inputs = ConfirmAccountProviderNode.Inputs(
                    isAccountCreation = navTarget.isAccountCreation,
                )
                val callback = object : ConfirmAccountProviderNode.Callback {
                    override fun navigateToOidc(oidcDetails: OidcDetails) {
                        navigateToMas(oidcDetails)
                    }

                    override fun navigateToCreateAccount(url: String) {
                        backstack.push(NavTarget.CreateAccount(url))
                    }

                    override fun navigateToLoginPassword() {
                        backstack.push(NavTarget.LoginPassword)
                    }

                    override fun navigateToChangeAccountProvider() {
                        // Pressgram: the account-provider picker is our community catalog.
                        backstack.push(NavTarget.ServerCatalog)
                    }
                }
                createNode<ConfirmAccountProviderNode>(buildContext, plugins = listOf(inputs, callback))
            }
            NavTarget.ChangeAccountProvider -> {
                val callback = object : ChangeAccountProviderNode.Callback {
                    override fun onDone() {
                        // Go back to the Account Provider screen
                        val confirmAccountProvider = backstack.elements.value.firstOrNull {
                            it.key.navTarget is NavTarget.ConfirmAccountProvider
                        }?.key?.navTarget ?: NavTarget.ConfirmAccountProvider(isAccountCreation = false)
                        backstack.singleTop(confirmAccountProvider)
                    }

                    override fun navigateToSearchAccountProvider() {
                        backstack.push(NavTarget.SearchAccountProvider)
                    }
                }

                createNode<ChangeAccountProviderNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.SearchAccountProvider -> {
                val callback = object : SearchAccountProviderNode.Callback {
                    override fun onDone() {
                        // Go back to the Account Provider screen
                        val confirmAccountProvider = backstack.elements.value.firstOrNull {
                            it.key.navTarget is NavTarget.ConfirmAccountProvider
                        }?.key?.navTarget ?: NavTarget.ConfirmAccountProvider(isAccountCreation = false)
                        backstack.singleTop(confirmAccountProvider)
                    }
                }

                createNode<SearchAccountProviderNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.LoginPassword -> {
                createNode<LoginPasswordNode>(buildContext)
            }
            is NavTarget.CreateAccount -> {
                val inputs = CreateAccountNode.Inputs(
                    url = navTarget.url,
                )
                createNode<CreateAccountNode>(buildContext, listOf(inputs))
            }
        }
    }

    private fun navigateToMas(oidcDetails: OidcDetails) {
        activity?.let {
            externalAppStarted = true
            it.openUrlInChromeCustomTab(null, darkTheme, oidcDetails.url)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        activity = requireNotNull(LocalActivity.current)
        darkTheme = !ElementTheme.isLightTheme
        DisposableEffect(Unit) {
            onDispose {
                activity = null
                appCoroutineScope.launch {
                    accountProviderDataSource.reset()
                }
            }
        }
        BackstackView()
    }
}
