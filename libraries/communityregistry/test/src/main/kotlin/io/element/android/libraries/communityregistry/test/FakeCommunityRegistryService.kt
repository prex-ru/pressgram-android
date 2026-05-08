/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.test

import io.element.android.libraries.communityregistry.api.CheckInviteResult
import io.element.android.libraries.communityregistry.api.CommunityRegistryService
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.communityregistry.api.InviteRequestResult
import io.element.android.tests.testutils.simulateLongTask

class FakeCommunityRegistryService(
    private val getCommunityServersResult: () -> List<CommunityServer> = { emptyList() },
    private val getServerWhitelistResult: () -> List<String> = { emptyList() },
    private val checkInviteResult: (homeserver: String, token: String) -> CheckInviteResult = { _, _ ->
        CheckInviteResult.Invalid(reason = "not_found")
    },
    private val submitInviteRequestResult: (homeserver: String, email: String, message: String) -> InviteRequestResult = { _, _, _ ->
        InviteRequestResult.Submitted
    },
) : CommunityRegistryService {
    override suspend fun getCommunityServers(): List<CommunityServer> = simulateLongTask {
        getCommunityServersResult()
    }

    override suspend fun getServerWhitelist(): List<String> = simulateLongTask {
        getServerWhitelistResult()
    }

    override suspend fun checkInvite(homeserver: String, token: String): CheckInviteResult = simulateLongTask {
        checkInviteResult(homeserver, token)
    }

    override suspend fun submitInviteRequest(
        homeserver: String,
        email: String,
        message: String,
    ): InviteRequestResult = simulateLongTask {
        submitInviteRequestResult(homeserver, email, message)
    }
}
