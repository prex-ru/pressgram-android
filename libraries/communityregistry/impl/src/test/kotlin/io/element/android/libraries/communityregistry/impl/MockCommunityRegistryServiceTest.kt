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
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MockCommunityRegistryServiceTest {
    private val service = MockCommunityRegistryService()

    @Test
    fun `getCommunityServers covers Open Token and Closed registration kinds`() = runTest {
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
        assertThat(kinds).containsAtLeast("open", "token", "closed")
    }

    @Test
    fun `Token servers expose instructions and contact`() = runTest {
        val tokenServers = service.getCommunityServers()
            .mapNotNull { it.registration as? Registration.Token }
        assertThat(tokenServers).isNotEmpty()
        tokenServers.forEach { token ->
            assertThat(token.instructions).isNotEmpty()
            assertThat(token.contact).isNotEmpty()
        }
    }

    @Test
    fun `getServerWhitelist contains pgram domain`() = runTest {
        assertThat(service.getServerWhitelist()).contains("pgram.im")
    }

    @Test
    fun `checkInvite valid token returns Valid`() = runTest {
        val result = service.checkInvite("https://newsroom.pgram.im", "press-2026-ok")
        assertThat(result).isInstanceOf(CheckInviteResult.Valid::class.java)
        result as CheckInviteResult.Valid
        assertThat(result.homeserver).isEqualTo("https://newsroom.pgram.im")
    }

    @Test
    fun `checkInvite expired token returns Invalid expired`() = runTest {
        val result = service.checkInvite("https://newsroom.pgram.im", "press-2025-expired")
        assertThat(result).isInstanceOf(CheckInviteResult.Invalid::class.java)
        assertThat((result as CheckInviteResult.Invalid).reason).isEqualTo("expired")
    }

    @Test
    fun `checkInvite unknown token returns Invalid not_found`() = runTest {
        val result = service.checkInvite("https://x.pgram.im", "garbage") as CheckInviteResult.Invalid
        assertThat(result.reason).isEqualTo("not_found")
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
    fun `submitInviteRequest rejects too short message`() = runTest {
        val result = service.submitInviteRequest(
            homeserver = "https://newsroom.pgram.im",
            email = "user@example.com",
            message = "short",
        )
        assertThat(result).isInstanceOf(InviteRequestResult.Error::class.java)
    }

    @Test
    fun `submitInviteRequest rejects invalid email`() = runTest {
        val result = service.submitInviteRequest(
            homeserver = "https://newsroom.pgram.im",
            email = "no-at-sign",
            message = "I would like to join because I write for X paper.",
        )
        assertThat(result).isInstanceOf(InviteRequestResult.Error::class.java)
    }
}
