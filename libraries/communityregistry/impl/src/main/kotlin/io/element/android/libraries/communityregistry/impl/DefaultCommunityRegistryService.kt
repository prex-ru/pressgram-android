/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.impl

import io.element.android.libraries.communityregistry.api.CheckInviteResult
import io.element.android.libraries.communityregistry.api.CommunityRegistryService
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.communityregistry.api.InviteRequestResult
import io.element.android.libraries.communityregistry.impl.dto.CheckInviteRequestDto
import io.element.android.libraries.communityregistry.impl.dto.InviteRequestRequestDto

// Real HTTP-backed implementation. Not @ContributesBinding yet — MockCommunityRegistryService
// is the active binding while dl.pgram.im endpoints are not deployed.
class DefaultCommunityRegistryService(
    private val apiFactory: CommunityRegistryApiFactory,
) : CommunityRegistryService {
    override suspend fun getCommunityServers(): List<CommunityServer> =
        apiFactory.create().getCommunityServers().servers.map { it.toApi() }

    override suspend fun getServerWhitelist(): List<String> =
        apiFactory.create().getServerWhitelist().allowedHomeservers

    override suspend fun checkInvite(homeserver: String, token: String): CheckInviteResult =
        apiFactory.create()
            .checkInvite(CheckInviteRequestDto(homeserver = homeserver, token = token))
            .toApi()

    override suspend fun submitInviteRequest(
        homeserver: String,
        email: String,
        message: String,
    ): InviteRequestResult = apiFactory.create()
        .submitInviteRequest(
            InviteRequestRequestDto(homeserver = homeserver, email = email, message = message)
        )
        .toApi()
}
