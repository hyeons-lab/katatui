use crate::types::KatatuiStyle;
use std::ffi::CStr;

pub struct KatatuiBlock {
    pub(crate) title: Option<String>,
    pub(crate) borders: u32,
    pub(crate) style: Option<KatatuiStyle>,
}

impl KatatuiBlock {
    pub fn new() -> Self {
        KatatuiBlock {
            title: None,
            borders: 0,
            style: None,
        }
    }
}

pub fn build_block(b: &KatatuiBlock) -> ratatui::widgets::Block<'static> {
    use ratatui::widgets::{Block, Borders};
    let mut block = Block::default().borders(Borders::from_bits_truncate(b.borders as u8));
    if let Some(ref t) = b.title {
        block = block.title(t.clone());
    }
    if let Some(s) = b.style {
        block = block.style(s);
    }
    block
}

#[no_mangle]
pub extern "C" fn katatui_block_new() -> *mut KatatuiBlock {
    Box::into_raw(Box::new(KatatuiBlock::new()))
}

#[no_mangle]
pub extern "C" fn katatui_block_free(block: *mut KatatuiBlock) {
    if !block.is_null() {
        // SAFETY: `block` was returned by `katatui_block_new()`, has not been freed
        // before, and the caller holds exclusive ownership.
        unsafe { drop(Box::from_raw(block)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_block_set_title(block: *mut KatatuiBlock, title: *const std::ffi::c_char) {
    if block.is_null() {
        return;
    }
    let b = unsafe { &mut *block };
    if title.is_null() {
        b.title = None;
    } else {
        b.title = Some(unsafe { CStr::from_ptr(title) }.to_string_lossy().into_owned());
    }
}

#[no_mangle]
pub extern "C" fn katatui_block_set_borders(block: *mut KatatuiBlock, borders: u32) {
    if block.is_null() {
        return;
    }
    unsafe { (*block).borders = borders };
}

#[no_mangle]
pub extern "C" fn katatui_block_set_style(block: *mut KatatuiBlock, style: KatatuiStyle) {
    if block.is_null() {
        return;
    }
    unsafe { (*block).style = Some(style) };
}
