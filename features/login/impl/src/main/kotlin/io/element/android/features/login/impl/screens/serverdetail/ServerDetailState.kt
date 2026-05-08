/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.serverdetail

import io.element.android.features.login.impl.login.LoginMode
import io.element.android.libraries.architecture.AsyncData

data class ServerDetailState(
    val serverInfo: AsyncData<ServerDetailInfo>,
    val loginMode: AsyncData<LoginMode>,
    val eventSink: (ServerDetailEvents) -> Unit,
)
