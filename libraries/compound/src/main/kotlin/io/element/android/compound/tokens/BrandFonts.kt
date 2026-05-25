/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTextApi::class)

package io.element.android.compound.tokens

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import io.element.android.compound.R

private val frauncesVariationSettings = FontVariation.Settings(
    FontVariation.weight(FontWeight.SemiBold.weight),
    FontVariation.Setting("SOFT", 0f),
    FontVariation.Setting("WONK", 1f),
)

val BrandFontFamily: FontFamily = FontFamily(
    Font(
        resId = R.font.fraunces,
        weight = FontWeight.SemiBold,
        variationSettings = frauncesVariationSettings,
    )
)
