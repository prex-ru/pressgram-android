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
import io.element.android.libraries.communityregistry.impl.dto.CheckInviteResponseDto
import io.element.android.libraries.communityregistry.impl.dto.CommunityServerDto
import io.element.android.libraries.communityregistry.impl.dto.InviteRequestResponseDto
import io.element.android.libraries.communityregistry.impl.dto.RegistrationDto
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
    fun `CommunityServerDto maps full server with token registration`() {
        val dto = CommunityServerDto(
            homeserver = "newsroom.pgram.im",
            name = "Newsroom",
            description = "Editorial server",
            logoUrl = "https://newsroom.pgram.im/logo.png",
            type = "media",
            registration = RegistrationDto(
                mode = "token",
                instructions = "Staff only",
                contact = "invites@newsroom.example.com",
            ),
        )
        val server = dto.toApi()
        assertThat(server.homeserver).isEqualTo("newsroom.pgram.im")
        assertThat(server.name).isEqualTo("Newsroom")
        assertThat(server.description).isEqualTo("Editorial server")
        assertThat(server.logoUrl).isEqualTo("https://newsroom.pgram.im/logo.png")
        assertThat(server.type).isEqualTo("media")
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
    fun `CheckInviteResponseDto maps to Valid when valid and homeserver present`() {
        val dto = CheckInviteResponseDto(valid = true, homeserver = "h", displayName = "Press")
        val result = dto.toApi() as CheckInviteResult.Valid
        assertThat(result.homeserver).isEqualTo("h")
        assertThat(result.displayName).isEqualTo("Press")
    }

    @Test
    fun `CheckInviteResponseDto maps to Invalid when valid is false`() {
        val dto = CheckInviteResponseDto(valid = false, reason = "expired")
        val result = dto.toApi() as CheckInviteResult.Invalid
        assertThat(result.reason).isEqualTo("expired")
    }

    @Test
    fun `CheckInviteResponseDto maps to Invalid when homeserver missing despite valid`() {
        val dto = CheckInviteResponseDto(valid = true, homeserver = null, reason = "malformed")
        val result = dto.toApi()
        assertThat(result).isInstanceOf(CheckInviteResult.Invalid::class.java)
    }

    @Test
    fun `InviteRequestResponseDto maps submitted variants to Submitted`() {
        listOf("submitted", "ok", "success", "SUBMITTED").forEach { status ->
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
    fun `InviteRequestResponseDto maps anything else to Error`() {
        val result = InviteRequestResponseDto(status = "weird_state", message = "oops").toApi()
        assertThat(result).isInstanceOf(InviteRequestResult.Error::class.java)
        assertThat((result as InviteRequestResult.Error).message).isEqualTo("oops")
    }
}
