/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.serverdetail

import io.element.android.libraries.communityregistry.api.Registration

data class ServerDetailInfo(
    val homeserverUrl: String,
    val name: String,
    val description: String?,
    val type: String?,
    val registration: Registration,
)
