
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
}

allprojects {
    group = "kr.donghune"
    version = "1.0.5"
    
    repositories {
        mavenCentral()
    }
}
