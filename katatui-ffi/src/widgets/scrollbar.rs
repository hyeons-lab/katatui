use crate::types::KatatuiStyle;
use std::ffi::CStr;

#[repr(C)]
#[derive(Copy, Clone)]
pub enum KatatuiScrollbarOrientation {
    VerticalRight = 0,
    VerticalLeft = 1,
    HorizontalBottom = 2,
    HorizontalTop = 3,
}

impl From<KatatuiScrollbarOrientation> for ratatui::widgets::ScrollbarOrientation {
    fn from(o: KatatuiScrollbarOrientation) -> Self {
        use ratatui::widgets::ScrollbarOrientation;
        match o {
            KatatuiScrollbarOrientation::VerticalRight => ScrollbarOrientation::VerticalRight,
            KatatuiScrollbarOrientation::VerticalLeft => ScrollbarOrientation::VerticalLeft,
            KatatuiScrollbarOrientation::HorizontalBottom => ScrollbarOrientation::HorizontalBottom,
            KatatuiScrollbarOrientation::HorizontalTop => ScrollbarOrientation::HorizontalTop,
        }
    }
}

/// Symbols are stored as pre-leaked `&'static str` so `build_scrollbar` can be called
/// every frame without leaking additional memory.  The initial leak happens once in each
/// `set_*_symbol` call.
pub struct KatatuiScrollbar {
    pub orientation: KatatuiScrollbarOrientation,
    thumb_symbol: Option<&'static str>,
    track_symbol: Option<&'static str>,
    begin_symbol: Option<&'static str>,
    end_symbol: Option<&'static str>,
    pub thumb_style: Option<KatatuiStyle>,
    pub track_style: Option<KatatuiStyle>,
    pub begin_style: Option<KatatuiStyle>,
    pub end_style: Option<KatatuiStyle>,
}

pub struct KatatuiScrollbarState {
    pub(crate) inner: ratatui::widgets::ScrollbarState,
}

pub fn build_scrollbar(s: &KatatuiScrollbar) -> ratatui::widgets::Scrollbar<'static> {
    use ratatui::widgets::Scrollbar;
    let mut sb = Scrollbar::new(s.orientation.into());
    if let Some(sym) = s.thumb_symbol {
        sb = sb.thumb_symbol(sym);
    }
    if let Some(sym) = s.track_symbol {
        sb = sb.track_symbol(Some(sym));
    }
    if let Some(sym) = s.begin_symbol {
        sb = sb.begin_symbol(Some(sym));
    }
    if let Some(sym) = s.end_symbol {
        sb = sb.end_symbol(Some(sym));
    }
    if let Some(style) = s.thumb_style {
        sb = sb.thumb_style(ratatui::style::Style::from(style));
    }
    if let Some(style) = s.track_style {
        sb = sb.track_style(ratatui::style::Style::from(style));
    }
    if let Some(style) = s.begin_style {
        sb = sb.begin_style(ratatui::style::Style::from(style));
    }
    if let Some(style) = s.end_style {
        sb = sb.end_style(ratatui::style::Style::from(style));
    }
    sb
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_new() -> *mut KatatuiScrollbar {
    Box::into_raw(Box::new(KatatuiScrollbar {
        orientation: KatatuiScrollbarOrientation::VerticalRight,
        thumb_symbol: None,
        track_symbol: None,
        begin_symbol: None,
        end_symbol: None,
        thumb_style: None,
        track_style: None,
        begin_style: None,
        end_style: None,
    }))
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_free(sb: *mut KatatuiScrollbar) {
    if !sb.is_null() {
        unsafe { drop(Box::from_raw(sb)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_set_orientation(
    sb: *mut KatatuiScrollbar,
    orientation: KatatuiScrollbarOrientation,
) {
    if sb.is_null() {
        return;
    }
    unsafe { (*sb).orientation = orientation };
}

/// Leaks `sym` once per call; subsequent calls on the same Scrollbar abandon the previous
/// leaked string (negligible — typically called at app initialisation, not per frame).
#[no_mangle]
pub extern "C" fn katatui_scrollbar_set_thumb_symbol(
    sb: *mut KatatuiScrollbar,
    sym: *const std::ffi::c_char,
) {
    if sb.is_null() {
        return;
    }
    let s = unsafe { &mut *sb };
    if sym.is_null() {
        s.thumb_symbol = None;
    } else {
        let owned = unsafe { CStr::from_ptr(sym) }.to_string_lossy().into_owned();
        s.thumb_symbol = Some(Box::leak(owned.into_boxed_str()));
    }
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_set_track_symbol(
    sb: *mut KatatuiScrollbar,
    sym: *const std::ffi::c_char,
) {
    if sb.is_null() {
        return;
    }
    let s = unsafe { &mut *sb };
    if sym.is_null() {
        s.track_symbol = None;
    } else {
        let owned = unsafe { CStr::from_ptr(sym) }.to_string_lossy().into_owned();
        s.track_symbol = Some(Box::leak(owned.into_boxed_str()));
    }
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_set_begin_symbol(
    sb: *mut KatatuiScrollbar,
    sym: *const std::ffi::c_char,
) {
    if sb.is_null() {
        return;
    }
    let s = unsafe { &mut *sb };
    if sym.is_null() {
        s.begin_symbol = None;
    } else {
        let owned = unsafe { CStr::from_ptr(sym) }.to_string_lossy().into_owned();
        s.begin_symbol = Some(Box::leak(owned.into_boxed_str()));
    }
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_set_end_symbol(
    sb: *mut KatatuiScrollbar,
    sym: *const std::ffi::c_char,
) {
    if sb.is_null() {
        return;
    }
    let s = unsafe { &mut *sb };
    if sym.is_null() {
        s.end_symbol = None;
    } else {
        let owned = unsafe { CStr::from_ptr(sym) }.to_string_lossy().into_owned();
        s.end_symbol = Some(Box::leak(owned.into_boxed_str()));
    }
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_set_thumb_style(
    sb: *mut KatatuiScrollbar,
    style: KatatuiStyle,
) {
    if sb.is_null() {
        return;
    }
    unsafe { (*sb).thumb_style = Some(style) };
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_set_track_style(
    sb: *mut KatatuiScrollbar,
    style: KatatuiStyle,
) {
    if sb.is_null() {
        return;
    }
    unsafe { (*sb).track_style = Some(style) };
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_set_begin_style(
    sb: *mut KatatuiScrollbar,
    style: KatatuiStyle,
) {
    if sb.is_null() {
        return;
    }
    unsafe { (*sb).begin_style = Some(style) };
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_set_end_style(
    sb: *mut KatatuiScrollbar,
    style: KatatuiStyle,
) {
    if sb.is_null() {
        return;
    }
    unsafe { (*sb).end_style = Some(style) };
}

// ---- ScrollbarState ----

#[no_mangle]
pub extern "C" fn katatui_scrollbar_state_new() -> *mut KatatuiScrollbarState {
    Box::into_raw(Box::new(KatatuiScrollbarState {
        inner: ratatui::widgets::ScrollbarState::default(),
    }))
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_state_free(state: *mut KatatuiScrollbarState) {
    if !state.is_null() {
        unsafe { drop(Box::from_raw(state)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_state_set_content_length(
    state: *mut KatatuiScrollbarState,
    length: u16,
) {
    if state.is_null() {
        return;
    }
    let s = unsafe { &mut *state };
    s.inner = s.inner.content_length(length as usize);
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_state_set_position(
    state: *mut KatatuiScrollbarState,
    position: u16,
) {
    if state.is_null() {
        return;
    }
    let s = unsafe { &mut *state };
    s.inner = s.inner.position(position as usize);
}

#[no_mangle]
pub extern "C" fn katatui_scrollbar_state_set_viewport_content_length(
    state: *mut KatatuiScrollbarState,
    length: u16,
) {
    if state.is_null() {
        return;
    }
    let s = unsafe { &mut *state };
    s.inner = s.inner.viewport_content_length(length as usize);
}
