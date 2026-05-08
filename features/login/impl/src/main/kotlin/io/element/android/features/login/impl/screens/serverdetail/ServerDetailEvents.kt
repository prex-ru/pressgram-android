/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.serverdetail

sealed interface ServerDetailEvents {
    data object Register : ServerDetailEvents
    data object SignIn : ServerDetailEvents
    data object ClearError : ServerDetailEvents
}
