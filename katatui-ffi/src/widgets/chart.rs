use crate::types::{KatatuiMarker, KatatuiStyle};
use std::ffi::CStr;

#[repr(C)]
#[derive(Copy, Clone)]
pub enum KatatuiGraphType {
    Scatter = 0,
    Line = 1,
    Bar = 2,
}

impl From<KatatuiGraphType> for ratatui::widgets::GraphType {
    fn from(g: KatatuiGraphType) -> Self {
        match g {
            KatatuiGraphType::Scatter => ratatui::widgets::GraphType::Scatter,
            KatatuiGraphType::Line => ratatui::widgets::GraphType::Line,
            KatatuiGraphType::Bar => ratatui::widgets::GraphType::Bar,
        }
    }
}

/// A committed dataset with its data points owned by this struct.
#[derive(Clone)]
pub struct KatatuiDataset {
    pub name: String,
    pub data: Vec<(f64, f64)>,
    pub graph_type: KatatuiGraphType,
    pub marker: KatatuiMarker,
    pub style: Option<KatatuiStyle>,
}

#[derive(Clone)]
pub(crate) struct AxisBuilder {
    pub(crate) title: Option<String>,
    pub(crate) bounds_min: f64,
    pub(crate) bounds_max: f64,
    pub(crate) labels: Vec<String>,
    pub(crate) style: Option<KatatuiStyle>,
}

impl Default for AxisBuilder {
    fn default() -> Self {
        Self { title: None, bounds_min: 0.0, bounds_max: 1.0, labels: Vec::new(), style: None }
    }
}

pub struct KatatuiChart {
    pub datasets: Vec<KatatuiDataset>,
    /// Accumulator for the dataset currently being built.
    current_name: String,
    current_data: Vec<(f64, f64)>,
    current_graph_type: KatatuiGraphType,
    current_marker: KatatuiMarker,
    current_style: Option<KatatuiStyle>,
    pub(crate) x_axis: AxisBuilder,
    pub(crate) y_axis: AxisBuilder,
    pub style: Option<KatatuiStyle>,
}

pub(crate) fn build_axis(a: &AxisBuilder) -> ratatui::widgets::Axis<'static> {
    use ratatui::widgets::Axis;
    let mut axis = Axis::default().bounds([a.bounds_min, a.bounds_max]);
    if let Some(ref title) = a.title {
        axis = axis.title(title.clone());
    }
    if !a.labels.is_empty() {
        let labels: Vec<ratatui::text::Line<'static>> =
            a.labels.iter().map(|l| ratatui::text::Line::from(l.clone())).collect();
        axis = axis.labels(labels);
    }
    if let Some(style) = a.style {
        axis = axis.style(ratatui::style::Style::from(style));
    }
    axis
}

#[no_mangle]
pub extern "C" fn katatui_chart_new() -> *mut KatatuiChart {
    Box::into_raw(Box::new(KatatuiChart {
        datasets: Vec::new(),
        current_name: String::new(),
        current_data: Vec::new(),
        current_graph_type: KatatuiGraphType::Line,
        current_marker: KatatuiMarker::Dot,
        current_style: None,
        x_axis: AxisBuilder::default(),
        y_axis: AxisBuilder::default(),
        style: None,
    }))
}

#[no_mangle]
pub extern "C" fn katatui_chart_free(chart: *mut KatatuiChart) {
    if !chart.is_null() {
        let c = unsafe { &*chart };
        if !c.current_data.is_empty() {
            eprintln!(
                "[katatui] Chart freed with {} uncommitted data points; call commitDataset() first",
                c.current_data.len()
            );
        }
        unsafe { drop(Box::from_raw(chart)) };
    }
}

// ---- Current dataset building ----

#[no_mangle]
pub extern "C" fn katatui_chart_set_dataset_name(
    chart: *mut KatatuiChart,
    name: *const std::ffi::c_char,
) {
    if chart.is_null() || name.is_null() {
        return;
    }
    let c = unsafe { &mut *chart };
    c.current_name = unsafe { CStr::from_ptr(name) }.to_string_lossy().into_owned();
}

#[no_mangle]
pub extern "C" fn katatui_chart_set_dataset_graph_type(
    chart: *mut KatatuiChart,
    graph_type: KatatuiGraphType,
) {
    if chart.is_null() {
        return;
    }
    unsafe { (*chart).current_graph_type = graph_type };
}

#[no_mangle]
pub extern "C" fn katatui_chart_set_dataset_marker(
    chart: *mut KatatuiChart,
    marker: KatatuiMarker,
) {
    if chart.is_null() {
        return;
    }
    unsafe { (*chart).current_marker = marker };
}

#[no_mangle]
pub extern "C" fn katatui_chart_set_dataset_style(
    chart: *mut KatatuiChart,
    style: KatatuiStyle,
) {
    if chart.is_null() {
        return;
    }
    unsafe { (*chart).current_style = Some(style) };
}

/// Adds a data point (x, y) to the current dataset being built.
/// Uses non-`add_` prefix so codegen does not attempt to wrap this function.
#[no_mangle]
pub extern "C" fn katatui_chart_dataset_point(chart: *mut KatatuiChart, x: f64, y: f64) {
    if chart.is_null() {
        return;
    }
    unsafe { (*chart).current_data.push((x, y)) };
}

/// Commits the current dataset into the chart's dataset list.
#[no_mangle]
pub extern "C" fn katatui_chart_commit_dataset(chart: *mut KatatuiChart) {
    if chart.is_null() {
        return;
    }
    let c = unsafe { &mut *chart };
    c.datasets.push(KatatuiDataset {
        name: std::mem::take(&mut c.current_name),
        data: std::mem::take(&mut c.current_data),
        graph_type: c.current_graph_type,
        marker: c.current_marker,
        style: c.current_style.take(),
    });
    c.current_graph_type = KatatuiGraphType::Line;
    c.current_marker = KatatuiMarker::Dot;
}

// ---- X Axis ----

#[no_mangle]
pub extern "C" fn katatui_chart_set_x_title(
    chart: *mut KatatuiChart,
    title: *const std::ffi::c_char,
) {
    if chart.is_null() || title.is_null() {
        return;
    }
    let c = unsafe { &mut *chart };
    c.x_axis.title = Some(unsafe { CStr::from_ptr(title) }.to_string_lossy().into_owned());
}

/// Sets the x-axis bounds.  Uses non-`set_` prefix so codegen skips it (double params).
#[no_mangle]
pub extern "C" fn katatui_chart_x_bounds(chart: *mut KatatuiChart, min: f64, max: f64) {
    if chart.is_null() {
        return;
    }
    let c = unsafe { &mut *chart };
    c.x_axis.bounds_min = min;
    c.x_axis.bounds_max = max;
}

#[no_mangle]
pub extern "C" fn katatui_chart_add_x_label(
    chart: *mut KatatuiChart,
    label: *const std::ffi::c_char,
) {
    if chart.is_null() || label.is_null() {
        return;
    }
    let label = unsafe { CStr::from_ptr(label) }.to_string_lossy().into_owned();
    unsafe { (*chart).x_axis.labels.push(label) };
}

#[no_mangle]
pub extern "C" fn katatui_chart_set_x_style(chart: *mut KatatuiChart, style: KatatuiStyle) {
    if chart.is_null() {
        return;
    }
    unsafe { (*chart).x_axis.style = Some(style) };
}

// ---- Y Axis ----

#[no_mangle]
pub extern "C" fn katatui_chart_set_y_title(
    chart: *mut KatatuiChart,
    title: *const std::ffi::c_char,
) {
    if chart.is_null() || title.is_null() {
        return;
    }
    let c = unsafe { &mut *chart };
    c.y_axis.title = Some(unsafe { CStr::from_ptr(title) }.to_string_lossy().into_owned());
}

/// Sets the y-axis bounds.  Uses non-`set_` prefix so codegen skips it (double params).
#[no_mangle]
pub extern "C" fn katatui_chart_y_bounds(chart: *mut KatatuiChart, min: f64, max: f64) {
    if chart.is_null() {
        return;
    }
    let c = unsafe { &mut *chart };
    c.y_axis.bounds_min = min;
    c.y_axis.bounds_max = max;
}

#[no_mangle]
pub extern "C" fn katatui_chart_add_y_label(
    chart: *mut KatatuiChart,
    label: *const std::ffi::c_char,
) {
    if chart.is_null() || label.is_null() {
        return;
    }
    let label = unsafe { CStr::from_ptr(label) }.to_string_lossy().into_owned();
    unsafe { (*chart).y_axis.labels.push(label) };
}

#[no_mangle]
pub extern "C" fn katatui_chart_set_y_style(chart: *mut KatatuiChart, style: KatatuiStyle) {
    if chart.is_null() {
        return;
    }
    unsafe { (*chart).y_axis.style = Some(style) };
}

// ---- Overall chart style ----

#[no_mangle]
pub extern "C" fn katatui_chart_set_style(chart: *mut KatatuiChart, style: KatatuiStyle) {
    if chart.is_null() {
        return;
    }
    unsafe { (*chart).style = Some(style) };
}

