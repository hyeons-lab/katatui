use ratatui_image::{picker::Picker, protocol::StatefulProtocol};
use std::ffi::CStr;

pub struct KatatuiImageState {
    pub(crate) protocol: StatefulProtocol,
}

/// Creates image state from a file path.
/// Uses Unicode half-block rendering, which works in every terminal.
/// Returns null if the file cannot be opened or decoded.
#[no_mangle]
pub extern "C" fn katatui_image_state_new(
    path: *const std::ffi::c_char,
) -> *mut KatatuiImageState {
    if path.is_null() {
        return std::ptr::null_mut();
    }
    let path_str = unsafe { CStr::from_ptr(path) }.to_string_lossy();
    let dyn_img = match image::open(path_str.as_ref()) {
        Ok(img) => img,
        Err(_) => return std::ptr::null_mut(),
    };
    let picker = Picker::from_query_stdio().unwrap_or_else(|_| Picker::halfblocks());
    let protocol = picker.new_resize_protocol(dyn_img);
    Box::into_raw(Box::new(KatatuiImageState { protocol }))
}

/// Creates image state from in-memory bytes (PNG, JPEG, GIF, WebP, etc.).
/// Returns null if the bytes cannot be decoded as a supported image format.
#[no_mangle]
pub extern "C" fn katatui_image_state_from_bytes(
    data: *const u8,
    len: usize,
) -> *mut KatatuiImageState {
    if data.is_null() || len == 0 {
        return std::ptr::null_mut();
    }
    let bytes = unsafe { std::slice::from_raw_parts(data, len) };
    let dyn_img = match image::load_from_memory(bytes) {
        Ok(img) => img,
        Err(_) => return std::ptr::null_mut(),
    };
    let picker = Picker::from_query_stdio().unwrap_or_else(|_| Picker::halfblocks());
    let protocol = picker.new_resize_protocol(dyn_img);
    Box::into_raw(Box::new(KatatuiImageState { protocol }))
}

#[no_mangle]
pub extern "C" fn katatui_image_state_free(state: *mut KatatuiImageState) {
    if !state.is_null() {
        unsafe { drop(Box::from_raw(state)) };
    }
}