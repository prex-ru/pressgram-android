/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.login.impl.screens.servercatalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.communityregistry.api.CommunityServer
import io.element.android.libraries.communityregistry.api.Registration
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.designsystem.R as DesignSystemR

@Composable
fun ServerCatalogView(
    state: ServerCatalogState,
    onBackClick: () -> Unit,
    onServerClick: (homeserverUrl: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_server_catalog_title),
                        style = ElementTheme.typography.fontBodyLgMedium,
                    )
                },
                navigationIcon = { BackButton(onClick = onBackClick) },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (val servers = state.servers) {
                AsyncData.Uninitialized,
                is AsyncData.Loading -> CircularProgressIndicator()
                is AsyncData.Failure -> ServerCatalogError(
                    onRetry = { state.eventSink(ServerCatalogEvents.Retry) },
                )
                is AsyncData.Success -> ServerCatalogList(
                    servers = servers.data,
                    selectedHomeserver = state.selectedHomeserver,
                    onServerClick = onServerClick,
                )
            }
        }
    }
}

@Composable
private fun ServerCatalogList(
    servers: List<CommunityServer>,
    selectedHomeserver: String?,
    onServerClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(servers) { server ->
            ServerCatalogRow(
                server = server,
                isSelected = server.homeserver.ensureProtocol() == selectedHomeserver,
                onClick = { onServerClick(server.homeserver) },
            )
        }
    }
}

@Composable
private fun ServerCatalogRow(
    server: CommunityServer,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val fallbackLogo = painterResource(id = DesignSystemR.drawable.pressgram_logo)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = server.logoUrl,
            contentDescription = null,
            placeholder = fallbackLogo,
            error = fallbackLogo,
            fallback = fallbackLogo,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = server.name,
                style = ElementTheme.typography.fontBodyLgMedium,
                color = ElementTheme.colors.textPrimary,
            )
            val description = server.description
            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(id = server.registration.labelResId()),
                style = ElementTheme.typography.fontBodySmMedium,
                color = ElementTheme.colors.textSecondary,
            )
        }
        if (isSelected) {
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = CompoundIcons.Check(),
                contentDescription = null,
                tint = ElementTheme.colors.iconPrimary,
            )
        }
    }
}

@Composable
private fun ServerCatalogError(
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.screen_server_catalog_error),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Button(
            text = stringResource(id = CommonStrings.action_retry),
            onClick = onRetry,
        )
    }
}

private fun Registration.labelResId(): Int = when (this) {
    Registration.Open -> R.string.screen_server_detail_mode_open
    is Registration.Token -> R.string.screen_server_detail_mode_token
    Registration.Closed -> R.string.screen_server_detail_mode_closed
    Registration.Unknown -> R.string.screen_server_detail_mode_unknown
}

@PreviewsDayNight
@Composable
internal fun ServerCatalogViewPreview(
    @PreviewParameter(ServerCatalogStateProvider::class) state: ServerCatalogState,
) = ElementPreview {
    ServerCatalogView(
        state = state,
        onBackClick = {},
        onServerClick = {},
    )
}
