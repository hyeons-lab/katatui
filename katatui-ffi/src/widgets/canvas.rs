use crate::types::{color_from_katatui, KatatuiMarker, KatatuiStyle};

/// A buffered drawing command for the Canvas widget.
#[derive(Clone)]
pub(crate) enum CanvasCmd {
    Circle { x: f64, y: f64, radius: f64, color: ratatui::style::Color },
    Line { x1: f64, y1: f64, x2: f64, y2: f64, color: ratatui::style::Color },
    Rectangle { x: f64, y: f64, width: f64, height: f64, color: ratatui::style::Color },
    Points { coords: Vec<(f64, f64)>, color: ratatui::style::Color },
}

pub struct KatatuiCanvas {
    pub x_bounds_min: f64,
    pub x_bounds_max: f64,
    pub y_bounds_min: f64,
    pub y_bounds_max: f64,
    pub marker: KatatuiMarker,
    pub(crate) commands: Vec<CanvasCmd>,
    /// Accumulator for a `Points` batch being built via `begin_points`/`point`/`commit_points`.
    current_points: Vec<(f64, f64)>,
    current_points_color: ratatui::style::Color,
}

fn style_to_fg(style: KatatuiStyle) -> ratatui::style::Color {
    color_from_katatui(style.fg, style.fg_r, style.fg_g, style.fg_b, style.fg_index)
}

#[no_mangle]
pub extern "C" fn katatui_canvas_new() -> *mut KatatuiCanvas {
    Box::into_raw(Box::new(KatatuiCanvas {
        x_bounds_min: 0.0,
        x_bounds_max: 100.0,
        y_bounds_min: 0.0,
        y_bounds_max: 100.0,
        marker: KatatuiMarker::Braille,
        commands: Vec::new(),
        current_points: Vec::new(),
        current_points_color: ratatui::style::Color::Reset,
    }))
}

#[no_mangle]
pub extern "C" fn katatui_canvas_free(canvas: *mut KatatuiCanvas) {
    if !canvas.is_null() {
        // SAFETY: `canvas` was returned by `katatui_canvas_new()`, has not been freed
        // before, and the caller holds exclusive ownership.
        unsafe { drop(Box::from_raw(canvas)) };
    }
}

/// Sets x-axis bounds.  Non-`set_` prefix so codegen skips it (double params).
#[no_mangle]
pub extern "C" fn katatui_canvas_x_bounds(canvas: *mut KatatuiCanvas, min: f64, max: f64) {
    if canvas.is_null() {
        return;
    }
    let c = unsafe { &mut *canvas };
    c.x_bounds_min = min;
    c.x_bounds_max = max;
}

/// Sets y-axis bounds.  Non-`set_` prefix so codegen skips it (double params).
#[no_mangle]
pub extern "C" fn katatui_canvas_y_bounds(canvas: *mut KatatuiCanvas, min: f64, max: f64) {
    if canvas.is_null() {
        return;
    }
    let c = unsafe { &mut *canvas };
    c.y_bounds_min = min;
    c.y_bounds_max = max;
}

#[no_mangle]
pub extern "C" fn katatui_canvas_set_marker(canvas: *mut KatatuiCanvas, marker: KatatuiMarker) {
    if canvas.is_null() {
        return;
    }
    unsafe { (*canvas).marker = marker };
}

/// Clears all buffered drawing commands and resets the current-points batch state.
#[no_mangle]
pub extern "C" fn katatui_canvas_clear(canvas: *mut KatatuiCanvas) {
    if canvas.is_null() {
        return;
    }
    let c = unsafe { &mut *canvas };
    c.commands.clear();
    c.current_points.clear();
    c.current_points_color = ratatui::style::Color::Reset;
}

// ---- Draw commands ----

/// Queues a circle.  Non-`set_`/`add_` prefix so codegen skips it.
#[no_mangle]
pub extern "C" fn katatui_canvas_circle(
    canvas: *mut KatatuiCanvas,
    x: f64,
    y: f64,
    radius: f64,
    color: KatatuiStyle,
) {
    if canvas.is_null() {
        return;
    }
    unsafe {
        (*canvas).commands.push(CanvasCmd::Circle { x, y, radius, color: style_to_fg(color) })
    };
}

/// Queues a line.  Non-`set_`/`add_` prefix so codegen skips it.
#[no_mangle]
pub extern "C" fn katatui_canvas_line(
    canvas: *mut KatatuiCanvas,
    x1: f64,
    y1: f64,
    x2: f64,
    y2: f64,
    color: KatatuiStyle,
) {
    if canvas.is_null() {
        return;
    }
    unsafe {
        (*canvas)
            .commands
            .push(CanvasCmd::Line { x1, y1, x2, y2, color: style_to_fg(color) })
    };
}

/// Queues a rectangle.  Non-`set_`/`add_` prefix so codegen skips it.
#[no_mangle]
pub extern "C" fn katatui_canvas_rectangle(
    canvas: *mut KatatuiCanvas,
    x: f64,
    y: f64,
    width: f64,
    height: f64,
    color: KatatuiStyle,
) {
    if canvas.is_null() {
        return;
    }
    unsafe {
        (*canvas)
            .commands
            .push(CanvasCmd::Rectangle { x, y, width, height, color: style_to_fg(color) })
    };
}

/// Begins a `Points` batch with the given color.
#[no_mangle]
pub extern "C" fn katatui_canvas_begin_points(canvas: *mut KatatuiCanvas, color: KatatuiStyle) {
    if canvas.is_null() {
        return;
    }
    let c = unsafe { &mut *canvas };
    c.current_points.clear();
    c.current_points_color = style_to_fg(color);
}

/// Adds a point to the current `Points` batch.
/// Non-`add_` prefix so codegen skips it.
#[no_mangle]
pub extern "C" fn katatui_canvas_point(canvas: *mut KatatuiCanvas, x: f64, y: f64) {
    if canvas.is_null() {
        return;
    }
    unsafe { (*canvas).current_points.push((x, y)) };
}

/// Commits the current `Points` batch.  No-op if the batch is empty.
#[no_mangle]
pub extern "C" fn katatui_canvas_commit_points(canvas: *mut KatatuiCanvas) {
    if canvas.is_null() {
        return;
    }
    let c = unsafe { &mut *canvas };
    if c.current_points.is_empty() {
        return;
    }
    let coords = std::mem::take(&mut c.current_points);
    c.commands.push(CanvasCmd::Points { coords, color: c.current_points_color });
}

