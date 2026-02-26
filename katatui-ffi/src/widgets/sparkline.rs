use crate::types::KatatuiStyle;

pub struct KatatuiSparkline {
    pub data: Vec<u64>,
    pub max: u64,
    pub style: Option<KatatuiStyle>,
    pub bar_style: Option<KatatuiStyle>,
}

pub fn build_sparkline(s: &KatatuiSparkline) -> ratatui::widgets::Sparkline<'static> {
    use ratatui::widgets::Sparkline;
    // data() accepts any IntoIterator<Item: Into<SparklineBar>>; u64 implements that.
    let mut sparkline = Sparkline::default().data(s.data.iter().copied());
    if s.max > 0 {
        sparkline = sparkline.max(s.max);
    }
    // Apply bar_style first so explicit style wins if both are set.
    if let Some(style) = s.bar_style {
        sparkline = sparkline.style(style);
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
