package com.hyeonslab.katatui

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import kotlin.test.Test
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class ScrollbarStateTest {
  private fun withState(block: ScrollbarState.() -> Unit) = ScrollbarState().use { it.block() }

  // ---- contentLength ----

  @Test
  fun `contentLength valid value does not throw`() = withState {
    shouldNotThrowAny { contentLength = 100 }
  }

  @Test
  fun `contentLength upper bound does not throw`() = withState {
    shouldNotThrowAny { contentLength = 65535 }
  }

  @Test
  fun `contentLength negative value throws`() = withState {
    shouldThrow<IllegalArgumentException> { contentLength = -1 }
  }

  @Test
  fun `contentLength value 65536 throws`() = withState {
    shouldThrow<IllegalArgumentException> { contentLength = 65536 }
  }

  // ---- position ----

  @Test
  fun `position valid value does not throw`() = withState { shouldNotThrowAny { position = 100 } }

  @Test
  fun `position upper bound does not throw`() = withState { shouldNotThrowAny { position = 65535 } }

  @Test
  fun `position negative value throws`() = withState {
    shouldThrow<IllegalArgumentException> { position = -1 }
  }

  @Test
  fun `position value 65536 throws`() = withState {
    shouldThrow<IllegalArgumentException> { position = 65536 }
  }

  // ---- viewportContentLength ----

  @Test
  fun `viewportContentLength valid value does not throw`() = withState {
    shouldNotThrowAny { viewportContentLength = 100 }
  }

  @Test
  fun `viewportContentLength upper bound does not throw`() = withState {
    shouldNotThrowAny { viewportContentLength = 65535 }
  }

  @Test
  fun `viewportContentLength negative value throws`() = withState {
    shouldThrow<IllegalArgumentException> { viewportContentLength = -1 }
  }

  @Test
  fun `viewportContentLength value 65536 throws`() = withState {
    shouldThrow<IllegalArgumentException> { viewportContentLength = 65536 }
  }
}
