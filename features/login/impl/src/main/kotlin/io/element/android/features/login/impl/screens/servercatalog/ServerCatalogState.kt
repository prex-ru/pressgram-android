/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.servercatalog

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.communityregistry.api.CommunityServer

data class ServerCatalogState(
    val servers: AsyncData<List<CommunityServer>>,
    // Homeserver URL (protocol form) currently selected on the welcome screen,
    // used to mark the active entry in the list. Null until it is resolved.
    val selectedHomeserver: String?,
    val eventSink: (ServerCatalogEvents) -> Unit,
)
