/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.communityregistry.api.CheckInviteResult
import io.element.android.libraries.communityregistry.api.InviteRequestResult
import io.element.android.libraries.communityregistry.api.Registration
import io.element.android.libraries.communityregistry.api.Visibility
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MockCommunityRegistryServiceTest {
    private val service = MockCommunityRegistryService()

    @Test
    fun `getCommunityServers covers Token and Closed registration kinds`() = runTest {
        val servers = service.getCommunityServers()
        assertThat(servers).isNotEmpty()
        val kinds = servers.map { server ->
            when (server.registration) {
                Registration.Open -> "open"
                is Registration.Token -> "token"
                Registration.Closed -> "closed"
                Registration.Unknown -> "unknown"
            }
        }.toSet()
        assertThat(kinds).containsAtLeast("token", "closed")
    }

    @Test
    fun `Token servers expose instructions`() = runTest {
        val tokenServers = service.getCommunityServers()
            .mapNotNull { it.registration as? Registration.Token }
        assertThat(tokenServers).isNotEmpty()
        tokenServers.forEach { token ->
            assertThat(token.instructions).isNotEmpty()
        }
    }

    @Test
    fun `getCommunityServers exposes only public visibility entries`() = runTest {
        val visibilities = service.getCommunityServers().map { it.visibility }.toSet()
        assertThat(visibilities).containsExactly(Visibility.Public)
    }

    @Test
    fun `getServerWhitelist matches spec section 10_2 public domains`() = runTest {
        assertThat(service.getServerWhitelist()).containsExactly(
            "pgram.im",
            "x.pgram.im",
            "newsroom.pgram.im",
            "prexplore.pgram.im",
            "archive.pgram.im",
        ).inOrder()
    }

    @Test
    fun `checkInvite newsroom token returns public server metadata`() = runTest {
        val result = service.checkInvite("https://newsroom.pgram.im", "newsroom-2026-VALID")
        assertThat(result).isInstanceOf(CheckInviteResult.Valid::class.java)
        val valid = result as CheckInviteResult.Valid
        assertThat(valid.server.homeserver).isEqualTo("newsroom.pgram.im")
        assertThat(valid.server.visibility).isEqualTo(Visibility.Public)
    }

    @Test
    fun `checkInvite corpa token reveals unlisted server metadata`() = runTest {
        val result = service.checkInvite("client-corp-a.example.com", "corpa-mngr-WzqA")
        assertThat(result).isInstanceOf(CheckInviteResult.Valid::class.java)
        val valid = result as CheckInviteResult.Valid
        assertThat(valid.server.visibility).isEqualTo(Visibility.Unlisted)
        assertThat(valid.server.name).isEqualTo("Корпорация А")
    }

    @Test
    fun `checkInvite unknown token returns single INVALID_INVITE`() = runTest {
        val result = service.checkInvite("https://x.pgram.im", "garbage") as CheckInviteResult.Invalid
        assertThat(result.reason).isEqualTo("INVALID_INVITE")
    }

    @Test
    fun `submitInviteRequest accepts well-formed payload`() = runTest {
        val result = service.submitInviteRequest(
            homeserver = "https://newsroom.pgram.im",
            email = "user@example.com",
            message = "I would like to join because I write for X paper.",
        )
        assertThat(result).isEqualTo(InviteRequestResult.Submitted)
    }

    @Test
    fun `submitInviteRequest reports rate limit for marker email`() = runTest {
        val result = service.submitInviteRequest(
            homeserver = "https://newsroom.pgram.im",
            email = "spammer@ratelimit.test",
            message = "I would like to join because I write for X paper.",
        )
        assertThat(result).isEqualTo(InviteRequestResult.RateLimited)
    }

    @Test
    fun `submitInviteRequest reports NotAccepting for marker email`() = runTest {
        val result = service.submitInviteRequest(
            homeserver = "https://newsroom.pgram.im",
            email = "anyone@notaccepting.test",
            message = "I would like to join because I write for X paper.",
        )
        assertThat(result).isEqualTo(InviteRequestResult.NotAccepting)
    }

    @Test
    fun `submitInviteRequest reports ServerError for marker email`() = runTest {
        val result = service.submitInviteRequest(
            homeserver = "https://newsroom.pgram.im",
            email = "anyone@servererror.test",
            message = "I would like to join because I write for X paper.",
        )
        assertThat(result).isInstanceOf(InviteRequestResult.ServerError::class.java)
    }

    @Test
    fun `submitInviteRequest rejects too short message`() = runTest {
        val result = service.submitInviteRequest(
            homeserver = "https://newsroom.pgram.im",
            email = "user@example.com",
            message = "short",
        )
        assertThat(result).isEqualTo(InviteRequestResult.InvalidMessage)
    }

    @Test
    fun `submitInviteRequest rejects too long message`() = runTest {
        val result = service.submitInviteRequest(
            homeserver = "https://newsroom.pgram.im",
            email = "user@example.com",
            message = "x".repeat(1001),
        )
        assertThat(result).isEqualTo(InviteRequestResult.InvalidMessage)
    }

    @Test
    fun `submitInviteRequest rejects invalid email`() = runTest {
        val result = service.submitInviteRequest(
            homeserver = "https://newsroom.pgram.im",
            email = "no-at-sign",
            message = "I would like to join because I write for X paper.",
        )
        assertThat(result).isEqualTo(InviteRequestResult.InvalidEmail)
    }
}
