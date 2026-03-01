pub struct KatatuiClear;

pub fn build_clear(_c: &KatatuiClear) -> ratatui::widgets::Clear {
    ratatui::widgets::Clear
}

#[no_mangle]
pub extern "C" fn katatui_clear_new() -> *mut KatatuiClear {
    Box::into_raw(Box::new(KatatuiClear))
}

#[no_mangle]
pub extern "C" fn katatui_clear_free(clear: *mut KatatuiClear) {
    if !clear.is_null() {
        // SAFETY: `clear` was returned by `katatui_clear_new()`, has not been freed
        // before, and the caller holds exclusive ownership.
        unsafe { drop(Box::from_raw(clear)) };
    }
}
