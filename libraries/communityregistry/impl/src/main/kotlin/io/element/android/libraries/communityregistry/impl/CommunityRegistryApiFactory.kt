/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.network.RetrofitFactory

interface CommunityRegistryApiFactory {
    fun create(): CommunityRegistryAPI
}

@ContributesBinding(AppScope::class)
class DefaultCommunityRegistryApiFactory(
    private val retrofitFactory: RetrofitFactory,
) : CommunityRegistryApiFactory {
    override fun create(): CommunityRegistryAPI =
        retrofitFactory.create(CommunityRegistryConfig.BASE_URL)
            .create(CommunityRegistryAPI::class.java)
}
