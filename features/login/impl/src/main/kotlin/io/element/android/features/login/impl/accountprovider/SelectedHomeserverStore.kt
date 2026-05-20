/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accountprovider

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val selectedHomeserverUrlKey = stringPreferencesKey("selectedHomeserverUrl")

/**
 * Persists the homeserver the user picked in the Server Catalog so the welcome
 * screen still reflects it after the app is restarted.
 *
 * Unlike [AccountProviderDataSource] — which is in-memory and reset with every
 * login flow — this is backed by a DataStore on disk and survives process death.
 */
@SingleIn(AppScope::class)
@Inject
class SelectedHomeserverStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) {
    private val store = preferenceDataStoreFactory.create("pressgram_login_preferences")

    fun selectedHomeserverUrl(): Flow<String?> = store.data.map { prefs ->
        prefs[selectedHomeserverUrlKey]
    }

    suspend fun setSelectedHomeserverUrl(url: String?) {
        store.edit { prefs ->
            if (url != null) {
                prefs[selectedHomeserverUrlKey] = url
            } else {
                prefs.remove(selectedHomeserverUrlKey)
            }
        }
    }
}
