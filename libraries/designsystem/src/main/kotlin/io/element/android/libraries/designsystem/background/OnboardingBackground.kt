/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.background

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Gradient background for FTUE (onboarding) screens.
 *
 * Two soft blue radial glows over a near-black canvas: an upper glow positioned
 * right-of-center near the top, and a lower glow bleeding in from the left edge
 * mid-screen — matches the Pressgram welcome screen design.
 */
@Suppress("ModifierMissing")
@Composable
fun OnboardingBackground() {
    // Figma reference uses Pressgram brand primary #2e5bff on near-black #010308.
    val glowColor = Color(0xFF4071EC)
    val upprerColor = Color(0xFF1C7FA6)
    val canvasColor = if (ElementTheme.isLightTheme) ElementTheme.colors.bgCanvasDefault else Color(0xFF010308)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Figma layout is 390x844; positions are converted to fractions so the
            // background scales to any screen size.
            val upperCenter = Offset(x = size.width * 0.90f, y = size.height * 0.155f)
            val lowerCenter = Offset(x = size.width * 0.20f, y = size.height * 0.42f)
            val radius = size.width * 0.70f

            val upperBrush = ShaderBrush(
                RadialGradientShader(
                    center = upperCenter,
                    radius = radius,
                    colors = listOf(upprerColor.copy(alpha = 0.55f), Color.Transparent),
                )
            )
            val lowerBrush = ShaderBrush(
                RadialGradientShader(
                    center = lowerCenter,
                    radius = radius,
                    colors = listOf(glowColor.copy(alpha = 0.45f), Color.Transparent),
                )
            )
            drawRect(brush = upperBrush, size = size)
            drawRect(brush = lowerBrush, size = size)
        }
    }
}

@PreviewsDayNight
@Composable
internal fun OnboardingBackgroundPreview() {
    ElementPreview {
        OnboardingBackground()
    }
}
