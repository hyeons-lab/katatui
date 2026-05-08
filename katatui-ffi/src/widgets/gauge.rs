use crate::types::KatatuiStyle;
use std::ffi::CStr;

pub struct KatatuiGauge {
    pub percent: u8,
    pub label: Option<String>,
    pub style: Option<KatatuiStyle>,
    pub gauge_style: Option<KatatuiStyle>,
}

pub fn build_gauge(g: &KatatuiGauge) -> ratatui::widgets::Gauge<'static> {
    use ratatui::widgets::Gauge;
    let mut gauge = Gauge::default().percent(g.percent as u16);
    if let Some(ref l) = g.label {
        gauge = gauge.label(l.clone());
    }
    if let Some(s) = g.style {
        gauge = gauge.style(s);
    }
    if let Some(s) = g.gauge_style {
        gauge = gauge.gauge_style(s);
    }
    gauge
}

#[no_mangle]
pub extern "C" fn katatui_gauge_new() -> *mut KatatuiGauge {
    Box::into_raw(Box::new(KatatuiGauge {
        percent: 0,
        label: None,
        style: None,
        gauge_style: None,
    }))
}

#[no_mangle]
pub extern "C" fn katatui_gauge_free(gauge: *mut KatatuiGauge) {
    if !gauge.is_null() {
        // SAFETY: `gauge` was returned by `katatui_gauge_new()`, has not been freed
        // before, and the caller holds exclusive ownership.
        unsafe { drop(Box::from_raw(gauge)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_gauge_set_percent(gauge: *mut KatatuiGauge, percent: u8) {
    if gauge.is_null() {
        return;
    }
    unsafe { (*gauge).percent = percent };
}

#[no_mangle]
pub extern "C" fn katatui_gauge_set_label(
    gauge: *mut KatatuiGauge,
    label: *const std::ffi::c_char,
) {
    if gauge.is_null() {
        return;
    }
    let g = unsafe { &mut *gauge };
    if label.is_null() {
        g.label = None;
    } else {
        g.label = Some(unsafe { CStr::from_ptr(label) }.to_string_lossy().into_owned());
    }
}

#[no_mangle]
pub extern "C" fn katatui_gauge_set_style(gauge: *mut KatatuiGauge, style: KatatuiStyle) {
    if gauge.is_null() {
        return;
    }
    unsafe { (*gauge).style = Some(style) };
}

#[no_mangle]
pub extern "C" fn katatui_gauge_set_gauge_style(
    gauge: *mut KatatuiGauge,
    style: KatatuiStyle,
) {
    if gauge.is_null() {
        return;
    }
    unsafe { (*gauge).gauge_style = Some(style) };
}
