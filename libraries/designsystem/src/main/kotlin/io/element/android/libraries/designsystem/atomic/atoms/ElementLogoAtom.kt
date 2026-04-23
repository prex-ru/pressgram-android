/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@Composable
fun ElementLogoAtom(
    size: ElementLogoAtomSize,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier
            .size(size.outerSize)
            .clip(RoundedCornerShape(size.cornerRadius)),
        painter = painterResource(id = R.drawable.pressgram_logo),
        contentDescription = null,
    )
}

sealed class ElementLogoAtomSize(
    val outerSize: Dp,
    val cornerRadius: Dp,
    val shadowRadius: Dp,
) {
    data object Medium : ElementLogoAtomSize(
        outerSize = 120.dp,
        cornerRadius = 33.dp,
        shadowRadius = 32.dp,
    )

    data object Large : ElementLogoAtomSize(
        outerSize = 158.dp,
        cornerRadius = 44.dp,
        shadowRadius = 60.dp,
    )
}

@Composable
@PreviewsDayNight
internal fun ElementLogoAtomMediumPreview() = ElementPreview {
    ContentToPreview(ElementLogoAtomSize.Medium)
}

@Composable
@PreviewsDayNight
internal fun ElementLogoAtomLargePreview() = ElementPreview {
    ContentToPreview(ElementLogoAtomSize.Large)
}

@ExcludeFromCoverage
@Composable
private fun ContentToPreview(elementLogoAtomSize: ElementLogoAtomSize) {
    Box(
        Modifier.size(elementLogoAtomSize.outerSize + elementLogoAtomSize.shadowRadius * 2),
        contentAlignment = Alignment.Center
    ) {
        ElementLogoAtom(elementLogoAtomSize)
    }
}
