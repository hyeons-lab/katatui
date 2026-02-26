use crate::types::KatatuiStyle;
use std::ffi::CStr;

pub struct KatatuiParagraph {
    pub(crate) text: String,
    pub(crate) style: Option<KatatuiStyle>,
    pub(crate) wrap: bool,
}

pub fn build_paragraph(p: &KatatuiParagraph) -> ratatui::widgets::Paragraph<'static> {
    use ratatui::widgets::{Paragraph, Wrap};
    let mut para = Paragraph::new(p.text.clone());
    if let Some(s) = p.style {
        para = para.style(s);
    }
    if p.wrap {
        para = para.wrap(Wrap { trim: true });
    }
    para
}

#[no_mangle]
pub extern "C" fn katatui_paragraph_new(text: *const std::ffi::c_char) -> *mut KatatuiParagraph {
    let t = if text.is_null() {
        String::new()
    } else {
        unsafe { CStr::from_ptr(text) }.to_string_lossy().into_owned()
    };
    Box::into_raw(Box::new(KatatuiParagraph {
        text: t,
        style: None,
        wrap: false,
    }))
}

#[no_mangle]
pub extern "C" fn katatui_paragraph_free(para: *mut KatatuiParagraph) {
    if !para.is_null() {
        unsafe { drop(Box::from_raw(para)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_paragraph_set_style(para: *mut KatatuiParagraph, style: KatatuiStyle) {
    if para.is_null() {
        return;
    }
    unsafe { (*para).style = Some(style) };
}

#[no_mangle]
pub extern "C" fn katatui_paragraph_set_wrap(para: *mut KatatuiParagraph, wrap: bool) {
    if para.is_null() {
        return;
    }
    unsafe { (*para).wrap = wrap };
}
