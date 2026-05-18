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
import io.element.android.libraries.communityregistry.impl.dto.CheckInviteResponseDto
import io.element.android.libraries.communityregistry.impl.dto.CommunityServerDto
import io.element.android.libraries.communityregistry.impl.dto.InviteRequestResponseDto
import io.element.android.libraries.communityregistry.impl.dto.RegistrationDto
import io.element.android.libraries.communityregistry.impl.dto.ServerWhitelistResponseDto
import org.junit.Test

class MapperTest {
    @Test
    fun `RegistrationDto open maps to Registration_Open`() {
        assertThat(RegistrationDto(mode = "open").toRegistration()).isEqualTo(Registration.Open)
        assertThat(RegistrationDto(mode = "OPEN").toRegistration()).isEqualTo(Registration.Open)
    }

    @Test
    fun `RegistrationDto token maps to Registration_Token preserving instructions and contact`() {
        val dto = RegistrationDto(
            mode = "Token",
            instructions = "Ask the editor",
            contact = "editor@example.com",
        )
        val token = dto.toRegistration() as Registration.Token
        assertThat(token.instructions).isEqualTo("Ask the editor")
        assertThat(token.contact).isEqualTo("editor@example.com")
    }

    @Test
    fun `RegistrationDto token without instructions or contact still maps to Token`() {
        val token = RegistrationDto(mode = "token").toRegistration() as Registration.Token
        assertThat(token.instructions).isNull()
        assertThat(token.contact).isNull()
    }

    @Test
    fun `RegistrationDto closed maps to Registration_Closed`() {
        assertThat(RegistrationDto(mode = "closed").toRegistration()).isEqualTo(Registration.Closed)
    }

    @Test
    fun `RegistrationDto null and unknown values map to Registration_Unknown`() {
        assertThat((null as RegistrationDto?).toRegistration()).isEqualTo(Registration.Unknown)
        assertThat(RegistrationDto(mode = null).toRegistration()).isEqualTo(Registration.Unknown)
        assertThat(RegistrationDto(mode = "invite").toRegistration()).isEqualTo(Registration.Unknown)
        assertThat(RegistrationDto(mode = "").toRegistration()).isEqualTo(Registration.Unknown)
    }

    @Test
    fun `visibility string maps to Visibility sealed type`() {
        assertThat("public".toVisibility()).isEqualTo(Visibility.Public)
        assertThat("PUBLIC".toVisibility()).isEqualTo(Visibility.Public)
        assertThat("unlisted".toVisibility()).isEqualTo(Visibility.Unlisted)
        assertThat(null.toVisibility()).isEqualTo(Visibility.Unknown)
        assertThat("invalid".toVisibility()).isEqualTo(Visibility.Unknown)
    }

    @Test
    fun `CommunityServerDto maps full server with token registration`() {
        val dto = CommunityServerDto(
            homeserver = "newsroom.pgram.im",
            name = "Newsroom",
            description = "Editorial server",
            logoUrl = "https://newsroom.pgram.im/logo.png",
            type = "media",
            visibility = "public",
            registration = RegistrationDto(
                mode = "token",
                instructions = "Staff only",
                contact = "invites@newsroom.example.com",
            ),
            owner = "ООО «Новости»",
            country = "RU",
            language = "ru",
            since = "2026-04-15",
            tags = listOf("редакция"),
            active = true,
            lastSeen = "2026-04-28T09:00:00Z",
        )
        val server = dto.toApi()
        assertThat(server.homeserver).isEqualTo("newsroom.pgram.im")
        assertThat(server.name).isEqualTo("Newsroom")
        assertThat(server.description).isEqualTo("Editorial server")
        assertThat(server.logoUrl).isEqualTo("https://newsroom.pgram.im/logo.png")
        assertThat(server.type).isEqualTo("media")
        assertThat(server.visibility).isEqualTo(Visibility.Public)
        assertThat(server.owner).isEqualTo("ООО «Новости»")
        assertThat(server.country).isEqualTo("RU")
        assertThat(server.language).isEqualTo("ru")
        assertThat(server.since).isEqualTo("2026-04-15")
        assertThat(server.tags).containsExactly("редакция")
        assertThat(server.active).isTrue()
        assertThat(server.lastSeen).isEqualTo("2026-04-28T09:00:00Z")
        val token = server.registration as Registration.Token
        assertThat(token.instructions).isEqualTo("Staff only")
        assertThat(token.contact).isEqualTo("invites@newsroom.example.com")
    }

    @Test
    fun `CommunityServerDto without registration block maps to Registration_Unknown`() {
        val dto = CommunityServerDto(
            homeserver = "h",
            name = "n",
            registration = null,
        )
        assertThat(dto.toApi().registration).isEqualTo(Registration.Unknown)
    }

    @Test
    fun `CommunityServerDto without active flag defaults to true`() {
        val dto = CommunityServerDto(homeserver = "h", name = "n", active = null)
        assertThat(dto.toApi().active).isTrue()
    }

    @Test
    fun `CheckInviteResponseDto maps to Valid when server present`() {
        val dto = CheckInviteResponseDto(
            valid = true,
            server = CommunityServerDto(
                homeserver = "newsroom.pgram.im",
                name = "Press",
                visibility = "public",
            ),
        )
        val result = dto.toApi() as CheckInviteResult.Valid
        assertThat(result.server.homeserver).isEqualTo("newsroom.pgram.im")
        assertThat(result.server.name).isEqualTo("Press")
        assertThat(result.server.visibility).isEqualTo(Visibility.Public)
    }

    @Test
    fun `CheckInviteResponseDto unlisted server is propagated`() {
        val dto = CheckInviteResponseDto(
            valid = true,
            server = CommunityServerDto(
                homeserver = "corp.example.com",
                name = "Corp",
                visibility = "unlisted",
            ),
        )
        val result = dto.toApi() as CheckInviteResult.Valid
        assertThat(result.server.visibility).isEqualTo(Visibility.Unlisted)
    }

    @Test
    fun `CheckInviteResponseDto maps to Invalid when valid is false`() {
        val dto = CheckInviteResponseDto(valid = false, reason = "expired")
        val result = dto.toApi() as CheckInviteResult.Invalid
        assertThat(result.reason).isEqualTo("expired")
    }

    @Test
    fun `CheckInviteResponseDto with only error envelope maps to Invalid carrying code`() {
        val dto = CheckInviteResponseDto(valid = false, error = "Invalid invite", code = "INVALID_INVITE")
        val result = dto.toApi() as CheckInviteResult.Invalid
        assertThat(result.reason).isEqualTo("INVALID_INVITE")
    }

    @Test
    fun `CheckInviteResponseDto maps to Invalid when server missing despite valid`() {
        val dto = CheckInviteResponseDto(valid = true, server = null)
        assertThat(dto.toApi()).isInstanceOf(CheckInviteResult.Invalid::class.java)
    }

    @Test
    fun `ServerWhitelistResponseDto prefers servers field over allowed_homeservers`() {
        val dto = ServerWhitelistResponseDto(
            servers = listOf("pgram.im"),
            allowedHomeservers = listOf("legacy.example"),
        )
        assertThat(dto.toApi()).containsExactly("pgram.im")
    }

    @Test
    fun `ServerWhitelistResponseDto falls back to allowed_homeservers when servers empty`() {
        val dto = ServerWhitelistResponseDto(
            servers = emptyList(),
            allowedHomeservers = listOf("legacy.example"),
        )
        assertThat(dto.toApi()).containsExactly("legacy.example")
    }

    @Test
    fun `InviteRequestResponseDto maps submitted variants to Submitted`() {
        listOf("submitted", "ok", "success", "SUBMITTED", "queued").forEach { status ->
            assertThat(InviteRequestResponseDto(status = status).toApi())
                .isEqualTo(InviteRequestResult.Submitted)
        }
    }

    @Test
    fun `InviteRequestResponseDto maps rate limit status to RateLimited`() {
        assertThat(InviteRequestResponseDto(status = "rate_limited").toApi())
            .isEqualTo(InviteRequestResult.RateLimited)
        assertThat(InviteRequestResponseDto(status = "rate-limited").toApi())
            .isEqualTo(InviteRequestResult.RateLimited)
    }

    @Test
    fun `InviteRequestResponseDto maps error code to specific variant`() {
        assertThat(InviteRequestResponseDto(code = "INVALID_EMAIL").toApi())
            .isEqualTo(InviteRequestResult.InvalidEmail)
        assertThat(InviteRequestResponseDto(code = "INVALID_MESSAGE").toApi())
            .isEqualTo(InviteRequestResult.InvalidMessage)
        assertThat(InviteRequestResponseDto(code = "NOT_ACCEPTING").toApi())
            .isEqualTo(InviteRequestResult.NotAccepting)
        assertThat(InviteRequestResponseDto(code = "RATE_LIMITED").toApi())
            .isEqualTo(InviteRequestResult.RateLimited)
    }

    @Test
    fun `InviteRequestResponseDto maps anything else to ServerError`() {
        val result = InviteRequestResponseDto(status = "weird_state", message = "oops").toApi()
        assertThat(result).isInstanceOf(InviteRequestResult.ServerError::class.java)
        assertThat((result as InviteRequestResult.ServerError).message).isEqualTo("oops")
    }
}
