/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.serverdetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.login.impl.util.openLearnMorePage
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.matrix.api.auth.OidcDetails

@ContributesNode(AppScope::class)
@AssistedInject
class ServerDetailNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ServerDetailPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val homeserverUrl: String,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val presenter = presenterFactory.create(
        ServerDetailPresenter.Params(homeserverUrl = inputs.homeserverUrl)
    )

    interface Callback : Plugin {
        fun navigateToLoginPassword()
        fun navigateToOidc(oidcDetails: OidcDetails)
        fun navigateToCreateAccount(url: String)
        fun navigateToChangeServer()
    }

    private val callback: Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current
        ServerDetailView(
            state = state,
            modifier = modifier,
            onBackClick = ::navigateUp,
            onOidcDetails = callback::navigateToOidc,
            onNeedLoginPassword = callback::navigateToLoginPassword,
            onLearnMoreClick = { openLearnMorePage(context) },
            onCreateAccountContinue = callback::navigateToCreateAccount,
            onChangeServer = callback::navigateToChangeServer,
        )
    }
}
