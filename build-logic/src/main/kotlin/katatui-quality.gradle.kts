plugins {
    id("com.ncorti.ktfmt.gradle")
    id("io.gitlab.arturbosch.detekt")
}

configure<com.ncorti.ktfmt.gradle.KtfmtExtension> {
    googleStyle()
    maxWidth.set(100)
}

configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
    config.setFrom(rootProject.files("detekt.yml"))
    buildUponDefaultConfig = false
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach { jvmTarget = "17" }

afterEvaluate {
    val detektTasks =
        tasks.matching { it.name.startsWith("detektJvm") && it.name.endsWith("Main") }
    if (detektTasks.isNotEmpty()) {
        tasks.named("check") { dependsOn(detektTasks) }
    }
}
