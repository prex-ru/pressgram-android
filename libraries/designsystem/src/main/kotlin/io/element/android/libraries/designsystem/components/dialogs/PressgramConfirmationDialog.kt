/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.libraries.designsystem.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * Pressgram-styled confirmation / notice dialog: an optional centred icon, a title,
 * a body, an optional small caption, and a single full-width call-to-action.
 *
 * Unlike the design-system [ConfirmationDialog] (Material text-button row, no icon
 * for single-action use, no caption) this matches the Pressgram popup design.
 */
@Composable
fun PressgramConfirmationDialog(
    title: String,
    content: String,
    submitText: String,
    onSubmitClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        PressgramConfirmationDialogContent(
            title = title,
            content = content,
            submitText = submitText,
            onSubmitClick = onSubmitClick,
            caption = caption,
            icon = icon,
        )
    }
}

@Composable
private fun PressgramConfirmationDialogContent(
    title: String,
    content: String,
    submitText: String,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .width(304.dp)
            .clip(shape)
            .background(ElementTheme.colors.bgSubtleSecondary)
            .border(width = 1.dp, color = ElementTheme.colors.borderInteractiveSecondary, shape = shape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (icon != null) {
                icon()
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = ElementTheme.typography.fontHeadingSmMedium,
                    color = ElementTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = content,
                    style = ElementTheme.typography.fontBodySmMedium,
                    color = ElementTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (caption != null) {
            Text(
                text = caption,
                style = ElementTheme.typography.fontBodyXsRegular,
                color = ElementTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        Button(
            text = submitText,
            onClick = onSubmitClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun PressgramConfirmationDialogContentPreview() = ElementPreview {
    PressgramConfirmationDialogContent(
        title = "Request sent",
        content = "Your request has been sent to the server admins. A reply will be sent to ivan@example.com.",
        caption = "When you receive an invitation code, come back here and tap \"I have a code\".",
        submitText = "Back to start",
        onSubmitClick = {},
        icon = {
            Icon(
                imageVector = CompoundIcons.Check(),
                contentDescription = null,
                tint = ElementTheme.colors.iconSuccessPrimary,
                modifier = Modifier.size(32.dp),
            )
        },
    )
}
