@file:OptIn(ExperimentalForeignApi::class)

package com.hyeonslab.katatui

import cnames.structs.KatatuiImageState
import com.hyeonslab.katatui.cinterop.katatui_image_state_free
import com.hyeonslab.katatui.cinterop.katatui_image_state_from_bytes
import com.hyeonslab.katatui.cinterop.katatui_image_state_from_bytes_with_picker
import com.hyeonslab.katatui.cinterop.katatui_image_state_new
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned

/**
 * Holds per-image rendering state (protocol + resize buffer) across frames. Must be kept alive for
 * the lifetime of the render loop and closed when done.
 *
 * Returns null from [invoke] if the image file cannot be opened or decoded.
 */
class ImageState internal constructor(internal val ptr: CPointer<KatatuiImageState>) :
  AutoCloseable {

  override fun close() {
    katatui_image_state_free(ptr)
  }

  companion object {
    operator fun invoke(path: String): ImageState? {
      val ptr = katatui_image_state_new(path) ?: return null
      return ImageState(ptr)
    }

    fun fromBytes(bytes: ByteArray): ImageState? {
      val ptr =
        bytes.usePinned { pinned ->
          katatui_image_state_from_bytes(pinned.addressOf(0).reinterpret(), bytes.size.toULong())
        } ?: return null
      return ImageState(ptr)
    }

    /**
     * Decodes [bytes] using a [Picker] created on the main thread. Call this from a background
     * thread to offload the CPU work while keeping the terminal protocol query on the main thread.
     */
    fun fromBytesWithPicker(bytes: ByteArray, picker: Picker): ImageState? {
      val ptr =
        bytes.usePinned { pinned ->
          katatui_image_state_from_bytes_with_picker(
            pinned.addressOf(0).reinterpret(),
            bytes.size.toULong(),
            picker.ptr,
          )
        } ?: return null
      return ImageState(ptr)
    }
  }
}
