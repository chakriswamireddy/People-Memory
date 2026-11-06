
[versions]
agp = "8.8.0"
kotlin = "2.1.0"
ksp = "2.1.0-1.0.29"
coil = "2.7.0"
litertLm = "1.0.1"
firebaseBom = "33.7.0"
playServicesAuth = "21.3.0"
composeBom = "2024.12.01"

[libraries]
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }
litert-lm = { module = "com.google.ai.edge.litert:litert-lm", version.ref = "litertLm" }
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { module = "com.google.firebase:firebase-auth" }
firebase-firestore = { module = "com.google.firebase:firebase-firestore" }
play-services-auth = { module = "com.google.android.gms:play-services-auth", version.ref = "playServicesAuth" }
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3" }
androidx-compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
google-services = { id = "com.google.gms.google-services", version = "4.4.2" }
