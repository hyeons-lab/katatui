use std::ffi::CStr;

pub struct KatatuiList {
    pub(crate) items: Vec<String>,
}

pub struct KatatuiListState {
    pub(crate) selected: Option<usize>,
}

pub fn build_list(l: &KatatuiList) -> ratatui::widgets::List<'static> {
    use ratatui::widgets::List;
    let items: Vec<String> = l.items.clone();
    List::new(items)
}

#[no_mangle]
pub extern "C" fn katatui_list_new() -> *mut KatatuiList {
    Box::into_raw(Box::new(KatatuiList { items: Vec::new() }))
}

#[no_mangle]
pub extern "C" fn katatui_list_free(list: *mut KatatuiList) {
    if !list.is_null() {
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
    Box::into_raw(Box::new(KatatuiListState { selected: None }))
}

#[no_mangle]
pub extern "C" fn katatui_list_state_free(state: *mut KatatuiListState) {
    if !state.is_null() {
        unsafe { drop(Box::from_raw(state)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_list_state_select(state: *mut KatatuiListState, index: i32) {
    if state.is_null() {
        return;
    }
    unsafe {
        (*state).selected = if index < 0 {
            None
        } else {
            Some(index as usize)
        };
    }
}
