use crate::types::KatatuiStyle;

pub struct KatatuiLineGauge {
    pub percent: u8,
    pub style: Option<KatatuiStyle>,
    pub line_style: Option<KatatuiStyle>,
}

pub fn build_line_gauge(g: &KatatuiLineGauge) -> ratatui::widgets::LineGauge<'static> {
    use ratatui::widgets::LineGauge;
    let mut gauge = LineGauge::default().ratio(g.percent as f64 / 100.0);
    if let Some(s) = g.style {
        gauge = gauge.style(s);
    }
    if let Some(s) = g.line_style {
        gauge = gauge.filled_style(s);
    }
    gauge
}

#[no_mangle]
pub extern "C" fn katatui_line_gauge_new() -> *mut KatatuiLineGauge {
    Box::into_raw(Box::new(KatatuiLineGauge {
        percent: 0,
        style: None,
        line_style: None,
    }))
}

#[no_mangle]
pub extern "C" fn katatui_line_gauge_free(gauge: *mut KatatuiLineGauge) {
    if !gauge.is_null() {
        // SAFETY: `gauge` was returned by `katatui_line_gauge_new()`, has not been freed
        // before, and the caller holds exclusive ownership.
        unsafe { drop(Box::from_raw(gauge)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_line_gauge_set_percent(gauge: *mut KatatuiLineGauge, percent: u8) {
    if gauge.is_null() {
        return;
    }
    unsafe { (*gauge).percent = percent };
}

#[no_mangle]
pub extern "C" fn katatui_line_gauge_set_style(
    gauge: *mut KatatuiLineGauge,
    style: KatatuiStyle,
) {
    if gauge.is_null() {
        return;
    }
    unsafe { (*gauge).style = Some(style) };
}

#[no_mangle]
pub extern "C" fn katatui_line_gauge_set_line_style(
    gauge: *mut KatatuiLineGauge,
    style: KatatuiStyle,
) {
    if gauge.is_null() {
        return;
    }
    unsafe { (*gauge).line_style = Some(style) };
}
