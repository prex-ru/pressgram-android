/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.communityregistry.api.CheckInviteResult
import io.element.android.libraries.communityregistry.api.CommunityRegistryService
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.communityregistry.api.InviteRequestResult
import io.element.android.libraries.communityregistry.api.Registration

// Active binding while dl.pgram.im endpoints are not deployed. Replace with
// DefaultCommunityRegistryService once the server is live (§10 mocks become
// the seed data for the real backend).
//
// Data mirrors §10 of the pressgram-community-registry spec exactly, including
// bare hostnames (mapper for the real API also produces bare hostnames).
@ContributesBinding(AppScope::class)
class MockCommunityRegistryService : CommunityRegistryService {
    override suspend fun getCommunityServers(): List<CommunityServer> = listOf(
        CommunityServer(
            homeserver = "pgram.im",
            name = "Pressgram",
            description = "Главный сервер сети. Открытая регистрация для всех журналистов и медиапрофессионалов.",
            logoUrl = "https://pgram.im/pressgram-logo-blue.png",
            type = "community",
            registration = Registration.Open,
        ),
        CommunityServer(
            homeserver = "newsroom.pgram.im",
            name = "Редакция «Новости»",
            description = "Внутренний сервер редакции газеты «Новости». Журналисты, редакторы, корреспонденты.",
            logoUrl = "https://newsroom.pgram.im/logo.png",
            type = "media",
            registration = Registration.Token(
                instructions = "Приглашения выдаются штатным сотрудникам редакции. Если вы внештатный корреспондент — укажите, " +
                    "с какой публикацией вы работаете.",
                contact = "invites@newsroom.example.com",
            ),
        ),
        CommunityServer(
            homeserver = "prexplore.pgram.im",
            name = "Прексплоре",
            description = "Сообщество вокруг проекта Прексплоре. Журналисты, медиаэксперты, аналитики отрасли.",
            logoUrl = "https://prexplore.pgram.im/logo.png",
            type = "community",
            registration = Registration.Token(
                instructions = "Сообщество для практикующих журналистов и медиаэкспертов. Укажите, в каком издании работаете " +
                    "и вашу тематику.",
                contact = "@admin:prexplore.pgram.im",
            ),
        ),
        CommunityServer(
            homeserver = "archive.pgram.im",
            name = "Архив",
            description = "Закрытый архивный сервер.",
            logoUrl = null,
            type = "other",
            registration = Registration.Closed,
        ),
    )

    override suspend fun getServerWhitelist(): List<String> = listOf(
        "pgram.im",
        "newsroom.pgram.im",
        "prexplore.pgram.im",
        "archive.pgram.im",
    )

    override suspend fun checkInvite(
        homeserver: String,
        token: String,
    ): CheckInviteResult = when (token) {
        VALID_MOCK_TOKEN -> CheckInviteResult.Valid(
            homeserver = homeserver,
            displayName = "Press invite",
        )
        EXPIRED_MOCK_TOKEN -> CheckInviteResult.Invalid(reason = "expired")
        else -> CheckInviteResult.Invalid(reason = "not_found")
    }

    override suspend fun submitInviteRequest(
        homeserver: String,
        email: String,
        message: String,
    ): InviteRequestResult = when {
        email.endsWith("@ratelimit.test") -> InviteRequestResult.RateLimited
        email.contains("@") && message.length in MIN_MESSAGE..MAX_MESSAGE -> InviteRequestResult.Submitted
        else -> InviteRequestResult.Error("invalid_input")
    }

    private companion object {
        const val VALID_MOCK_TOKEN = "press-2026-ok"
        const val EXPIRED_MOCK_TOKEN = "press-2025-expired"
        const val MIN_MESSAGE = 20
        const val MAX_MESSAGE = 1000
    }
}
