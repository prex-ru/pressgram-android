import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright 2026 Pressgram contributors.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.communityregistry.impl"
}

setupDependencyInjection()

dependencies {
    api(projects.libraries.communityregistry.api)
    implementation(libs.coroutines.core)
    implementation(platform(libs.network.retrofit.bom))
    implementation(libs.network.retrofit)
    implementation(libs.serialization.json)
    implementation(libs.timber)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.network)

    testCommonDependencies(libs)
    testImplementation(libs.coroutines.test)
}
