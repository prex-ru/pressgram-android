/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.impl

import io.element.android.libraries.communityregistry.impl.dto.CheckInviteRequestDto
import io.element.android.libraries.communityregistry.impl.dto.CheckInviteResponseDto
import io.element.android.libraries.communityregistry.impl.dto.CommunityServersResponseDto
import io.element.android.libraries.communityregistry.impl.dto.InviteRequestRequestDto
import io.element.android.libraries.communityregistry.impl.dto.InviteRequestResponseDto
import io.element.android.libraries.communityregistry.impl.dto.ServerWhitelistResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CommunityRegistryAPI {
    @GET("api/community-servers")
    suspend fun getCommunityServers(): CommunityServersResponseDto

    @GET("api/server-whitelist")
    suspend fun getServerWhitelist(): ServerWhitelistResponseDto

    @POST("api/check-invite")
    suspend fun checkInvite(@Body body: CheckInviteRequestDto): CheckInviteResponseDto

    @POST("api/invite-request")
    suspend fun submitInviteRequest(@Body body: InviteRequestRequestDto): InviteRequestResponseDto
}
