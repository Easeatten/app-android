plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ncorti.ktfmt)
}

ktfmt { kotlinLangStyle() }

tasks.register<com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask>("ktfmtCi") {
    description = "Run ktfmt as a hook for pre-commit-hooks (do not modify)"
    source = project.fileTree(rootDir)
    include("app/src/main/kotlin/**/*.kt", "app/*.kts", "*.kts")
}

tasks.register<com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask>("ktfmtPreCommit") {
    description = "Run ktfmt as a hook for pre-commit-hooks"
    source = project.fileTree(rootDir)
    include("app/src/main/kotlin/**/*.kt", "app/*.kts", "*.kts")
}
