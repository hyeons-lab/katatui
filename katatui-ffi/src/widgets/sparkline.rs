use crate::types::KatatuiStyle;

pub struct KatatuiSparkline {
    pub data: Vec<u64>,
    pub max: u64,
    pub style: Option<KatatuiStyle>,
    pub bar_style: Option<KatatuiStyle>,
}

pub fn build_sparkline(s: &KatatuiSparkline) -> ratatui::widgets::Sparkline<'static> {
    use ratatui::widgets::{Sparkline, SparklineBar};
    // When bar_style is set, wrap each data point in SparklineBar so the style
    // is applied per-bar rather than to the whole widget.
    let mut sparkline = if let Some(bar_style) = s.bar_style {
        let bar_style: ratatui::style::Style = bar_style.into();
        let styled: Vec<SparklineBar> = s
            .data
            .iter()
            .map(|&v| SparklineBar::from(v).style(bar_style))
            .collect();
        Sparkline::default().data(styled)
    } else {
        Sparkline::default().data(s.data.iter().copied())
    };
    if s.max > 0 {
        sparkline = sparkline.max(s.max);
    }
    if let Some(style) = s.style {
        sparkline = sparkline.style(style);
    }
    sparkline
}

#[no_mangle]
pub extern "C" fn katatui_sparkline_new() -> *mut KatatuiSparkline {
    Box::into_raw(Box::new(KatatuiSparkline {
        data: Vec::new(),
        max: 0,
        style: None,
        bar_style: None,
    }))
}

#[no_mangle]
pub extern "C" fn katatui_sparkline_free(sparkline: *mut KatatuiSparkline) {
    if !sparkline.is_null() {
        // SAFETY: `sparkline` was returned by `katatui_sparkline_new()`, has not been freed
        // before, and the caller holds exclusive ownership.
        unsafe { drop(Box::from_raw(sparkline)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_sparkline_add_data(sparkline: *mut KatatuiSparkline, value: u64) {
    if sparkline.is_null() {
        return;
    }
    unsafe { (*sparkline).data.push(value) };
}

#[no_mangle]
pub extern "C" fn katatui_sparkline_set_max(sparkline: *mut KatatuiSparkline, max: u64) {
    if sparkline.is_null() {
        return;
    }
    unsafe { (*sparkline).max = max };
}

#[no_mangle]
pub extern "C" fn katatui_sparkline_set_style(
    sparkline: *mut KatatuiSparkline,
    style: KatatuiStyle,
) {
    if sparkline.is_null() {
        return;
    }
    unsafe { (*sparkline).style = Some(style) };
}

#[no_mangle]
pub extern "C" fn katatui_sparkline_set_bar_style(
    sparkline: *mut KatatuiSparkline,
    style: KatatuiStyle,
) {
    if sparkline.is_null() {
        return;
    }
    unsafe { (*sparkline).bar_style = Some(style) };
}
