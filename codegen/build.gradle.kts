plugins {
  id("katatui-quality")
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.kotlinpoet)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotest.assertions.core)
}
