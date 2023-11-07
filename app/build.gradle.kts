@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

val mayor = 1
val minor = 0
val patch = 0
val hotfix = 1

android {
    namespace = "de.dertyp7214.rboardmanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "de.dertyp7214.rboardmanager"
        minSdk = 33
        targetSdk = 34
        versionCode = mayor * 100000000 + minor * 1000000 + patch * 10000 + hotfix
        versionName = "$mayor.$minor.$patch"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_20
        targetCompatibility = JavaVersion.VERSION_20
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_20.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    applicationVariants.all { variant ->
        if (variant.buildType.isMinifyEnabled) {
            variant.assembleProvider.get().doLast {
                for (file in variant.mappingFileProvider.get().files) {
                    copy {
                        from(file).into("${rootDir}/proguardTools")
                        rename { "mapping-${variant.name}.txt" }
                    }
                }
            }
        }
        true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}