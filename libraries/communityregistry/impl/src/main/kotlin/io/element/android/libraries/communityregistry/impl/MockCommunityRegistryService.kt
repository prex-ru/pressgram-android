/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.communityregistry.impl

import dev.zacsweers.metro.Inject
import io.element.android.libraries.communityregistry.api.CheckInviteResult
import io.element.android.libraries.communityregistry.api.CommunityRegistryService
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.communityregistry.api.InviteRequestResult
import io.element.android.libraries.communityregistry.api.Registration
import io.element.android.libraries.communityregistry.api.Visibility

// In-memory data source while dl.pgram.im endpoints are not deployed. Replace with
// DefaultCommunityRegistryService once the server is live (§10 mocks become the seed
// data for the real backend). It is wrapped by LoggingCommunityRegistryService, which
// is the actual @ContributesBinding for CommunityRegistryService.
//
// Data mirrors §10 of the pressgram-community-registry spec, with bare hostnames
// (the mapper for the real API also produces bare hostnames).
@Inject
class MockCommunityRegistryService : CommunityRegistryService {
    override suspend fun getCommunityServers(): List<CommunityServer> = listOf(
        CommunityServer(
            homeserver = "pgram.im",
            name = "Pressgram",
            description = "Главный сервер сети для журналистов и медиапрофессионалов.",
            logoUrl = "https://pgram.im/pressgram-logo-blue.png",
            type = "community",
            visibility = Visibility.Public,
            // Local testing: pgram.im uses token registration so the invite-request
            // flow is reachable straight from the default onboarding screen.
            registration = Registration.Token(
                instructions = "Расскажите о себе и вашей работе в медиа — администратор рассмотрит заявку и пришлёт код приглашения.",
                contact = "gulshan.rahimova.dev@gmail.com",
            ),
            owner = "PREX",
            country = "RU",
            language = "ru",
            since = "2026-03-25",
            tags = emptyList(),
            active = true,
            lastSeen = "2026-04-28T10:30:00Z",
        ),
        CommunityServer(
            homeserver = "newsroom.pgram.im",
            name = "Редакция «Новости»",
            description = "Внутренний сервер редакции газеты «Новости». Журналисты, редакторы, корреспонденты.",
            logoUrl = "https://newsroom.pgram.im/logo.png",
            type = "media",
            visibility = Visibility.Public,
            registration = Registration.Token(
                instructions = "Приглашения выдаются штатным сотрудникам редакции. Если вы внештатный корреспондент — укажите, " +
                    "с какой публикацией вы работаете.",
                contact = "invites@newsroom.example.com",
            ),
            owner = "ООО «Новости»",
            country = "RU",
            language = "ru",
            since = "2026-04-15",
            tags = listOf("редакция", "новости"),
            active = true,
            lastSeen = "2026-04-28T09:00:00Z",
        ),
        CommunityServer(
            homeserver = "prexplore.pgram.im",
            name = "Прексплоре",
            description = "Сообщество вокруг проекта Прексплоре. Журналисты, медиаэксперты, аналитики отрасли.",
            logoUrl = "https://prexplore.pgram.im/logo.png",
            type = "community",
            visibility = Visibility.Public,
            registration = Registration.Token(
                instructions = "Сообщество для практикующих журналистов и медиаэкспертов. Укажите, в каком издании работаете " +
                    "и вашу тематику.",
                contact = "@admin:prexplore.pgram.im",
            ),
            country = "RU",
            language = "ru",
            since = "2026-04-20",
            active = true,
            lastSeen = "2026-04-28T10:00:00Z",
        ),
        CommunityServer(
            homeserver = "archive.pgram.im",
            name = "Архив",
            description = "Закрытый архивный сервер.",
            logoUrl = null,
            type = "other",
            visibility = Visibility.Public,
            registration = Registration.Closed,
            country = "RU",
            language = "ru",
            since = "2026-04-01",
            active = true,
            lastSeen = "2026-04-28T08:00:00Z",
        ),
    )

    // Spec §10.2 — public domains only. Includes `x.pgram.im` (test server with E2EE,
    // also public from the registry's perspective). No unlisted entries by design.
    override suspend fun getServerWhitelist(): List<String> = listOf(
        "pgram.im",
        "x.pgram.im",
        "newsroom.pgram.im",
        "prexplore.pgram.im",
        "archive.pgram.im",
    )

    // Spec §10.3 / §10.4 / §10.5: two "happy path" tokens (one public, one unlisted)
    // and a single INVALID_INVITE catch-all for everything else.
    override suspend fun checkInvite(
        homeserver: String,
        token: String,
    ): CheckInviteResult = when (token) {
        NEWSROOM_VALID_TOKEN -> CheckInviteResult.Valid(
            server = CommunityServer(
                homeserver = "newsroom.pgram.im",
                name = "Редакция «Новости»",
                description = "Внутренний сервер редакции газеты «Новости».",
                logoUrl = "https://newsroom.pgram.im/logo.png",
                type = "media",
                visibility = Visibility.Public,
                registration = Registration.Token(
                    instructions = "Приглашения выдаются штатным сотрудникам редакции.",
                    contact = null,
                ),
                owner = "ООО «Новости»",
                country = "RU",
                language = "ru",
                since = "2026-04-15",
                active = true,
                lastSeen = "2026-04-28T09:00:00Z",
            )
        )
        CORPA_VALID_TOKEN -> CheckInviteResult.Valid(
            server = CommunityServer(
                homeserver = "client-corp-a.example.com",
                name = "Корпорация А",
                description = "Внутренний мессенджер компании.",
                logoUrl = "https://client-corp-a.example.com/logo.png",
                type = "other",
                visibility = Visibility.Unlisted,
                registration = Registration.Token(instructions = null, contact = null),
                owner = "ООО «Корпорация А»",
                country = "RU",
                language = "ru",
                since = "2026-04-10",
                active = true,
                lastSeen = "2026-04-28T09:00:00Z",
            )
        )
        else -> CheckInviteResult.Invalid(reason = "INVALID_INVITE")
    }

    // Spec §6.4 has 5 distinct error states; the mock uses email-domain markers to
    // surface each one for manual testing and to keep dev iteration friction low.
    override suspend fun submitInviteRequest(
        homeserver: String,
        email: String,
        message: String,
    ): InviteRequestResult = when {
        email.endsWith("@ratelimit.test") -> InviteRequestResult.RateLimited
        email.endsWith("@notaccepting.test") -> InviteRequestResult.NotAccepting
        email.endsWith("@servererror.test") -> InviteRequestResult.ServerError(message = "Mock 5xx")
        !email.isValidEmail() -> InviteRequestResult.InvalidEmail
        message.length !in MIN_MESSAGE..MAX_MESSAGE -> InviteRequestResult.InvalidMessage
        else -> InviteRequestResult.Submitted
    }

    private fun String.isValidEmail(): Boolean {
        // Pragmatic check — RFC 5322 happens server-side. Just enough to surface
        // obviously-malformed addresses for the mock UX.
        val at = indexOf('@')
        if (at <= 0 || at != lastIndexOf('@')) return false
        val local = substring(0, at)
        val domain = substring(at + 1)
        if (local.isBlank() || domain.isBlank()) return false
        if (!domain.contains('.')) return false
        return true
    }

    private companion object {
        const val NEWSROOM_VALID_TOKEN = "newsroom-2026-VALID"
        const val CORPA_VALID_TOKEN = "corpa-mngr-WzqA"
        const val MIN_MESSAGE = 20
        const val MAX_MESSAGE = 1000
    }
}
