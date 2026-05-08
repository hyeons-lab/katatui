use crate::types::KatatuiStyle;
use std::ffi::CStr;

pub struct KatatuiTabs {
    pub titles: Vec<String>,
    pub selected: u32,
    pub style: Option<KatatuiStyle>,
}

pub fn build_tabs(t: &KatatuiTabs) -> ratatui::widgets::Tabs<'static> {
    use ratatui::widgets::Tabs;
    let titles: Vec<String> = t.titles.clone();
    let mut tabs = Tabs::new(titles).select(t.selected as usize);
    if let Some(s) = t.style {
        tabs = tabs.style(s);
    }
    tabs
}

#[no_mangle]
pub extern "C" fn katatui_tabs_new() -> *mut KatatuiTabs {
    Box::into_raw(Box::new(KatatuiTabs {
        titles: Vec::new(),
        selected: 0,
        style: None,
    }))
}

#[no_mangle]
pub extern "C" fn katatui_tabs_free(tabs: *mut KatatuiTabs) {
    if !tabs.is_null() {
        // SAFETY: `tabs` was returned by `katatui_tabs_new()`, has not been freed
        // before, and the caller holds exclusive ownership.
        unsafe { drop(Box::from_raw(tabs)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_tabs_add_title(tabs: *mut KatatuiTabs, title: *const std::ffi::c_char) {
    if tabs.is_null() || title.is_null() {
        return;
    }
    let s = unsafe { CStr::from_ptr(title) }.to_string_lossy().into_owned();
    unsafe { (*tabs).titles.push(s) };
}

#[no_mangle]
pub extern "C" fn katatui_tabs_set_selected(tabs: *mut KatatuiTabs, selected: u32) {
    if tabs.is_null() {
        return;
    }
    unsafe { (*tabs).selected = selected };
}

#[no_mangle]
pub extern "C" fn katatui_tabs_set_style(tabs: *mut KatatuiTabs, style: KatatuiStyle) {
    if tabs.is_null() {
        return;
    }
    unsafe { (*tabs).style = Some(style) };
}
