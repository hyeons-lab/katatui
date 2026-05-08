use crate::types::KatatuiStyle;
use std::ffi::CStr;

pub struct KatatuiBarChart {
    pub bars: Vec<(String, u64)>,
    pub bar_width: u16,
    pub bar_gap: u16,
    pub max: u64,
    pub style: Option<KatatuiStyle>,
}

pub fn build_bar_chart(b: &KatatuiBarChart) -> ratatui::widgets::BarChart<'static> {
    use ratatui::widgets::{Bar, BarChart};
    let bars: Vec<Bar<'static>> = b
        .bars
        .iter()
        .map(|(label, value)| Bar::with_label(label.clone(), *value))
        .collect();
    let mut chart = BarChart::new(bars)
        .bar_width(b.bar_width.max(1))
        .bar_gap(b.bar_gap);
    if b.max > 0 {
        chart = chart.max(b.max);
    }
    if let Some(s) = b.style {
        chart = chart.style(s);
    }
    chart
}

#[no_mangle]
pub extern "C" fn katatui_bar_chart_new() -> *mut KatatuiBarChart {
    Box::into_raw(Box::new(KatatuiBarChart {
        bars: Vec::new(),
        bar_width: 1,
        bar_gap: 1,
        max: 0,
        style: None,
    }))
}

#[no_mangle]
pub extern "C" fn katatui_bar_chart_free(chart: *mut KatatuiBarChart) {
    if !chart.is_null() {
        // SAFETY: `chart` was returned by `katatui_bar_chart_new()`, has not been freed
        // before, and the caller holds exclusive ownership.
        unsafe { drop(Box::from_raw(chart)) };
    }
}

/// Appends a bar with the given label and value.
/// Named `_bar` (not `_add_bar`) so codegen skips it; use the hand-written Kotlin extension.
#[no_mangle]
pub extern "C" fn katatui_bar_chart_bar(
    chart: *mut KatatuiBarChart,
    label: *const std::ffi::c_char,
    value: u64,
) {
    if chart.is_null() || label.is_null() {
        return;
    }
    let label = unsafe { CStr::from_ptr(label) }.to_string_lossy().into_owned();
    unsafe { (*chart).bars.push((label, value)) };
}

#[no_mangle]
pub extern "C" fn katatui_bar_chart_set_bar_width(chart: *mut KatatuiBarChart, width: u16) {
    if chart.is_null() {
        return;
    }
    unsafe { (*chart).bar_width = width };
}

#[no_mangle]
pub extern "C" fn katatui_bar_chart_set_bar_gap(chart: *mut KatatuiBarChart, gap: u16) {
    if chart.is_null() {
        return;
    }
    unsafe { (*chart).bar_gap = gap };
}

#[no_mangle]
pub extern "C" fn katatui_bar_chart_set_max(chart: *mut KatatuiBarChart, max: u64) {
    if chart.is_null() {
        return;
    }
    unsafe { (*chart).max = max };
}

#[no_mangle]
pub extern "C" fn katatui_bar_chart_set_style(
    chart: *mut KatatuiBarChart,
    style: KatatuiStyle,
) {
    if chart.is_null() {
        return;
    }
    unsafe { (*chart).style = Some(style) };
}
