/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.login.impl.screens.servercatalog

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.designsystem.R as DesignSystemR

@Composable
fun ServerCatalogView(
    state: ServerCatalogState,
    onBackClick: () -> Unit,
    onContinueClick: (homeserverUrl: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_server_catalog_title),
                        style = ElementTheme.typography.fontHeadingMdBold.copy(
                            fontSize = 26.sp,
                            letterSpacing = 0.04.em,
                        ),
                    )
                },
                navigationIcon = { BackButton(onClick = onBackClick) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // The search field stays out of the way when the catalog itself failed
            // to load, since there is nothing to filter yet.
            if (state.servers !is AsyncData.Failure) {
                ServerCatalogSearchField(
                    query = state.searchQuery,
                    onQueryChange = { state.eventSink(ServerCatalogEvents.UpdateSearchQuery(it)) },
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                when (val servers = state.servers) {
                    AsyncData.Uninitialized,
                    is AsyncData.Loading -> CircularProgressIndicator()
                    is AsyncData.Failure -> ServerCatalogError(
                        onRetry = { state.eventSink(ServerCatalogEvents.Retry) },
                    )
                    is AsyncData.Success -> if (servers.data.isEmpty()) {
                        ServerCatalogEmpty()
                    } else {
                        ServerCatalogList(
                            servers = servers.data,
                            selectedHomeserver = state.selectedHomeserver,
                            onServerClick = { homeserver ->
                                state.eventSink(ServerCatalogEvents.SelectServer(homeserver))
                            },
                        )
                    }
                }
            }
            // Picking a row only highlights it; this button is what actually
            // confirms the choice and returns to onboarding. It stays in place
            // (disabled) while the catalog loads so the layout doesn't jump.
            Button(
                text = stringResource(id = CommonStrings.action_continue),
                enabled = state.servers is AsyncData.Success && state.selectedHomeserver != null,
                onClick = { state.selectedHomeserver?.let(onContinueClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun ServerCatalogSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = stringResource(id = R.string.screen_server_catalog_search_placeholder),
        placeholderStyle = ElementTheme.typography.fontBodySmMedium.copy(fontSize = 13.sp),
        leadingIcon = {
            Icon(
                imageVector = CompoundIcons.Search(),
                contentDescription = null,
                tint = ElementTheme.colors.iconSecondary,
            )
        },
        shape = RoundedCornerShape(28.dp),
        singleLine = true,
    )
}

@Composable
private fun ServerCatalogEmpty() {
    Text(
        modifier = Modifier.padding(32.dp),
        text = stringResource(id = R.string.screen_server_catalog_search_empty),
        style = ElementTheme.typography.fontBodyMdRegular,
        color = ElementTheme.colors.textSecondary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ServerCatalogList(
    servers: List<CommunityServer>,
    selectedHomeserver: String?,
    onServerClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ElementTheme.colors.bgSubtleSecondary)
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
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = server.name,
                style = ElementTheme.typography.fontBodyLgMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.04.em,
                ),
                color = ElementTheme.colors.textPrimary,
            )
            val description = server.description
            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = ElementTheme.typography.fontBodyMdMedium,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val badgeLabelResId = server.registration.badgeLabelResId()
            if (badgeLabelResId != null) {
                Spacer(Modifier.height(6.dp))
                RegistrationBadge(labelResId = badgeLabelResId)
            }
        }
        // The check icon slot is always rendered (invisible when not selected)
        // so the inner Column width stays the same and the description text
        // doesn't reflow when a row is picked.
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = CompoundIcons.Check(),
            contentDescription = null,
            // Accent tokens now resolve to the Pressgram blue palette.
            tint = ElementTheme.colors.iconAccentPrimary,
            modifier = Modifier.alpha(if (isSelected) 1f else 0f),
        )
    }
}

@Composable
private fun RegistrationBadge(
    @StringRes labelResId: Int,
) {
    val shape = RoundedCornerShape(percent = 50)
    // Pressgram blue from the design system, with the translucent fill/border
    // opacities from the design (10% / 45%).
    val accent = ElementTheme.colors.bgAccentRest
    Text(
        modifier = Modifier
            .clip(shape)
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.45f), shape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        text = stringResource(id = labelResId),
        style = ElementTheme.typography.fontBodySmMedium,
        color = ElementTheme.colors.textPrimary,
    )
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

// Short, pill-sized labels for the catalog cards. Unknown registration shows no
// badge rather than a vague placeholder.
@StringRes
private fun Registration.badgeLabelResId(): Int? = when (this) {
    Registration.Open -> R.string.screen_server_catalog_badge_open
    is Registration.Token -> R.string.screen_server_catalog_badge_token
    Registration.Closed -> R.string.screen_server_catalog_badge_closed
    Registration.Unknown -> null
}

@PreviewsDayNight
@Composable
internal fun ServerCatalogViewPreview(
    @PreviewParameter(ServerCatalogStateProvider::class) state: ServerCatalogState,
) = ElementPreview {
    ServerCatalogView(
        state = state,
        onBackClick = {},
        onContinueClick = {},
    )
}
