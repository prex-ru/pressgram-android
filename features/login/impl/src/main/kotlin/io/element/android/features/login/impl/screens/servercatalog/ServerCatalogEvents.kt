/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.servercatalog

sealed interface ServerCatalogEvents {
    data object Retry : ServerCatalogEvents
    data class UpdateSearchQuery(val query: String) : ServerCatalogEvents
    data class SelectServer(val homeserver: String) : ServerCatalogEvents
}
