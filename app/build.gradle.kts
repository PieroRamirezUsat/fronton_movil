import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.example.aplicacion_fronton"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.aplicacion_fronton"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Client ID tipo "Aplicación web" de Google Cloud Console (Credenciales) —
        // se usa como serverClientId en Credential Manager para que el idToken traiga
        // una audiencia que el backend pueda verificar. Reemplazar cuando esté creado.
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"1054440556019-lota9jv6nqpq69r2n01puno7nnt3pg09.apps.googleusercontent.com\"")

        // API key de "Maps SDK for Android" (Google Cloud Console) — a diferencia
        // de las credenciales de backend, esta SÍ va directo en el manifest (lugar
        // estándar documentado por Google): se restringe por nombre de paquete +
        // huella SHA-1, no por mantenerla en secreto. TODO: reemplazar cuando esté creada.
        manifestPlaceholders["MAPS_API_KEY"] = "AIzaSyBvYsDZ5YqPXD458TS5bLWeTZun9f6xaes"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            // El Firewall de Windows de esta PC bloquea las conexiones entrantes al
            // puerto 8001 desde la red virtual del emulador (10.0.2.2), y agregar una
            // regla necesita permisos de administrador que esta sesión no tiene. Se usa
            // 127.0.0.1 + "adb reverse tcp:8001 tcp:8001" (túnel por la propia conexión
            // adb, no pasa por el firewall) — hay que correr ese comando cada vez que
            // se reinicia el emulador. Alternativa si se consigue permiso de admin:
            // agregar la regla de firewall y volver a usar 10.0.2.2 sin el paso de adb.
            // En un celular físico en la misma wifi, usar la IP de red local de la PC.
            buildConfigField("String", "BASE_URL", "\"http://127.0.0.1:8001/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("String", "BASE_URL", "\"https://fronton-api-production.up.railway.app/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // minSdk 24 no trae java.time nativo (recién en API 26) — necesario para
        // parsear fecha_hora como instante real (OffsetDateTime) al validar si un
        // versus ya comenzó, en vez de comparar substrings de fecha a mano.
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}