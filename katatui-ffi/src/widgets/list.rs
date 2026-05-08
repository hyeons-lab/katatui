use std::ffi::CStr;

pub struct KatatuiList {
    pub(crate) items: Vec<String>,
}

pub struct KatatuiListState {
    pub(crate) inner: ratatui::widgets::ListState,
}

pub fn build_list(l: &KatatuiList) -> ratatui::widgets::List<'static> {
    use ratatui::widgets::{List, ListItem};
    let items: Vec<ListItem> = l.items.iter().map(|s| ListItem::new(s.clone())).collect();
    List::new(items)
}

#[no_mangle]
pub extern "C" fn katatui_list_new() -> *mut KatatuiList {
    Box::into_raw(Box::new(KatatuiList { items: Vec::new() }))
}

#[no_mangle]
pub extern "C" fn katatui_list_free(list: *mut KatatuiList) {
    if !list.is_null() {
        // SAFETY: `list` was returned by `katatui_list_new()`, has not been freed
        // before, and the caller holds exclusive ownership.
        unsafe { drop(Box::from_raw(list)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_list_add_item(list: *mut KatatuiList, item: *const std::ffi::c_char) {
    if list.is_null() || item.is_null() {
        return;
    }
    let s = unsafe { CStr::from_ptr(item) }.to_string_lossy().into_owned();
    unsafe { (*list).items.push(s) };
}

#[no_mangle]
pub extern "C" fn katatui_list_state_new() -> *mut KatatuiListState {
    Box::into_raw(Box::new(KatatuiListState {
        inner: ratatui::widgets::ListState::default(),
    }))
}

#[no_mangle]
pub extern "C" fn katatui_list_state_free(state: *mut KatatuiListState) {
    if !state.is_null() {
        // SAFETY: `state` was returned by `katatui_list_state_new()`, has not been freed
        // before, and the caller holds exclusive ownership.
        unsafe { drop(Box::from_raw(state)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_list_state_select(state: *mut KatatuiListState, index: i32) {
    if state.is_null() {
        return;
    }
    let s = unsafe { &mut *state };
    if index < 0 {
        s.inner.select(None);
    } else {
        s.inner.select(Some(index as usize));
    }
}
