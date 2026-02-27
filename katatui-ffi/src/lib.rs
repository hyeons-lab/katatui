pub mod terminal;
pub mod types;
pub mod widgets;

use crossterm::event::{self, Event, KeyEventKind};
use terminal::{KatatuiFrame, KatatuiTerminal};
use types::KatatuiRect;
use widgets::{
    bar_chart::build_bar_chart,
    block::build_block,
    chart::build_chart,
    clear::build_clear,
    gauge::build_gauge,
    line_gauge::build_line_gauge,
    list::build_list,
    logo::{build_logo, build_mascot},
    paragraph::build_paragraph,
    scrollbar::build_scrollbar,
    sparkline::build_sparkline,
    table::build_table,
    tabs::build_tabs,
};

pub use types::*;

// ---- Terminal lifecycle ----

#[no_mangle]
pub extern "C" fn katatui_terminal_new() -> *mut KatatuiTerminal {
    match ratatui::try_init() {
        Ok(inner) => Box::into_raw(Box::new(KatatuiTerminal {
            inner,
            drawing: false,
            current_frame: None,
        })),
        Err(_) => std::ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "C" fn katatui_terminal_free(terminal: *mut KatatuiTerminal) {
    if !terminal.is_null() {
        unsafe { drop(Box::from_raw(terminal)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_table_state_new() -> *mut widgets::table::KatatuiTableState {
    widgets::table::katatui_table_state_new()
}

#[no_mangle]
pub extern "C" fn katatui_table_state_free(state: *mut widgets::table::KatatuiTableState) {
    widgets::table::katatui_table_state_free(state);
}

#[no_mangle]
pub extern "C" fn katatui_table_state_select(
    state: *mut widgets::table::KatatuiTableState,
    index: i32,
) {
    widgets::table::katatui_table_state_select(state, index);
}

#[no_mangle]
pub extern "C" fn katatui_terminal_restore(_terminal: *mut KatatuiTerminal) {
    ratatui::restore();
}

// ---- Draw session ----

#[no_mangle]
pub extern "C" fn katatui_terminal_begin_draw(
    terminal: *mut KatatuiTerminal,
) -> *mut KatatuiFrame {
    if terminal.is_null() {
        return std::ptr::null_mut();
    }
    let t = unsafe { &mut *terminal };
    if t.drawing {
        return std::ptr::null_mut();
    }
    let size = t.inner.size().unwrap_or_default();
    let area = KatatuiRect { x: 0, y: 0, width: size.width, height: size.height };
    let frame = Box::into_raw(Box::new(KatatuiFrame {
        terminal,
        area,
        ops: Vec::new(),
    }));
    t.drawing = true;
    t.current_frame = Some(frame);
    frame
}

/// Executes all queued render operations in a single `terminal.draw()` call, then releases
/// the frame. This is the only place where ratatui's frame is actually borrowed.
#[no_mangle]
pub extern "C" fn katatui_terminal_end_draw(terminal: *mut KatatuiTerminal) {
    if terminal.is_null() {
        return;
    }
    let t = unsafe { &mut *terminal };
    if let Some(frame_ptr) = t.current_frame.take() {
        let frame = unsafe { Box::from_raw(frame_ptr) };
        let ops = frame.ops;
        let _ = t.inner.draw(|rf| {
            for op in ops {
                op(rf);
            }
        });
    }
    t.drawing = false;
}

// ---- Frame ----

#[no_mangle]
pub extern "C" fn katatui_frame_size(frame: *const KatatuiFrame) -> KatatuiRect {
    if frame.is_null() {
        return KatatuiRect { x: 0, y: 0, width: 0, height: 0 };
    }
    unsafe { (*frame).area }
}

// ---- Frame render ----

#[no_mangle]
pub extern "C" fn katatui_frame_render_block(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    block: *const widgets::block::KatatuiBlock,
) {
    if frame.is_null() || block.is_null() {
        return;
    }
    let widget = build_block(unsafe { &*block });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> =
        Box::new(move |rf| rf.render_widget(widget, area.into()));
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_paragraph(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    para: *const widgets::paragraph::KatatuiParagraph,
) {
    if frame.is_null() || para.is_null() {
        return;
    }
    let widget = build_paragraph(unsafe { &*para });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> =
        Box::new(move |rf| rf.render_widget(widget, area.into()));
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_list(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    list: *const widgets::list::KatatuiList,
    state: *mut widgets::list::KatatuiListState,
) {
    if frame.is_null() || list.is_null() {
        return;
    }
    let widget = build_list(unsafe { &*list });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> = Box::new(move |rf| {
        if state.is_null() {
            rf.render_widget(widget, area.into());
        } else {
            let s = unsafe { &mut *state };
            rf.render_stateful_widget(widget, area.into(), &mut s.inner);
        }
    });
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_clear(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    clear: *const widgets::clear::KatatuiClear,
) {
    if frame.is_null() || clear.is_null() {
        return;
    }
    let widget = build_clear(unsafe { &*clear });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> =
        Box::new(move |rf| rf.render_widget(widget, area.into()));
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_gauge(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    gauge: *const widgets::gauge::KatatuiGauge,
) {
    if frame.is_null() || gauge.is_null() {
        return;
    }
    let widget = build_gauge(unsafe { &*gauge });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> =
        Box::new(move |rf| rf.render_widget(widget, area.into()));
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_line_gauge(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    gauge: *const widgets::line_gauge::KatatuiLineGauge,
) {
    if frame.is_null() || gauge.is_null() {
        return;
    }
    let widget = build_line_gauge(unsafe { &*gauge });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> =
        Box::new(move |rf| rf.render_widget(widget, area.into()));
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_sparkline(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    sparkline: *const widgets::sparkline::KatatuiSparkline,
) {
    if frame.is_null() || sparkline.is_null() {
        return;
    }
    let widget = build_sparkline(unsafe { &*sparkline });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> =
        Box::new(move |rf| rf.render_widget(widget, area.into()));
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_bar_chart(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    chart: *const widgets::bar_chart::KatatuiBarChart,
) {
    if frame.is_null() || chart.is_null() {
        return;
    }
    let widget = build_bar_chart(unsafe { &*chart });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> =
        Box::new(move |rf| rf.render_widget(widget, area.into()));
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_tabs(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    tabs: *const widgets::tabs::KatatuiTabs,
) {
    if frame.is_null() || tabs.is_null() {
        return;
    }
    let widget = build_tabs(unsafe { &*tabs });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> =
        Box::new(move |rf| rf.render_widget(widget, area.into()));
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_table(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    table: *const widgets::table::KatatuiTable,
    state: *mut widgets::table::KatatuiTableState,
) {
    if frame.is_null() || table.is_null() {
        return;
    }
    let widget = build_table(unsafe { &*table });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> = Box::new(move |rf| {
        if state.is_null() {
            rf.render_widget(widget, area.into());
        } else {
            let s = unsafe { &mut *state };
            rf.render_stateful_widget(widget, area.into(), &mut s.inner);
        }
    });
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_image(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    state: *mut widgets::image::KatatuiImageState,
) {
    if frame.is_null() || state.is_null() {
        return;
    }
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> = Box::new(move |rf| {
        let s = unsafe { &mut *state };
        rf.render_stateful_widget(
            ratatui_image::StatefulImage::default(),
            area.into(),
            &mut s.protocol,
        );
    });
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_scrollbar(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    sb: *const widgets::scrollbar::KatatuiScrollbar,
    state: *mut widgets::scrollbar::KatatuiScrollbarState,
) {
    if frame.is_null() || sb.is_null() || state.is_null() {
        return;
    }
    let widget = build_scrollbar(unsafe { &*sb });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> = Box::new(move |rf| {
        let s = unsafe { &mut *state };
        rf.render_stateful_widget(widget, area.into(), &mut s.inner);
    });
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_chart(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    chart: *const widgets::chart::KatatuiChart,
) {
    if frame.is_null() || chart.is_null() {
        return;
    }
    let widget = build_chart(unsafe { &*chart });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> =
        Box::new(move |rf| rf.render_widget(widget, area.into()));
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_canvas(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    canvas: *const widgets::canvas::KatatuiCanvas,
) {
    if frame.is_null() || canvas.is_null() {
        return;
    }
    let c = unsafe { &*canvas };
    let commands = c.commands.clone();
    let x_bounds = [c.x_bounds_min, c.x_bounds_max];
    let y_bounds = [c.y_bounds_min, c.y_bounds_max];
    let marker = c.marker;
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> = Box::new(move |rf| {
        use ratatui::widgets::canvas::{Canvas, Circle, Line as CanvasLine, Points, Rectangle};
        use widgets::canvas::CanvasCmd;
        let widget = Canvas::default()
            .x_bounds(x_bounds)
            .y_bounds(y_bounds)
            .marker(marker.into())
            .paint(move |ctx| {
                for cmd in &commands {
                    match cmd {
                        CanvasCmd::Circle { x, y, radius, color } => {
                            ctx.draw(&Circle { x: *x, y: *y, radius: *radius, color: *color });
                        }
                        CanvasCmd::Line { x1, y1, x2, y2, color } => {
                            ctx.draw(&CanvasLine {
                                x1: *x1,
                                y1: *y1,
                                x2: *x2,
                                y2: *y2,
                                color: *color,
                            });
                        }
                        CanvasCmd::Rectangle { x, y, width, height, color } => {
                            ctx.draw(&Rectangle {
                                x: *x,
                                y: *y,
                                width: *width,
                                height: *height,
                                color: *color,
                            });
                        }
                        CanvasCmd::Points { coords, color } => {
                            ctx.draw(&Points { coords: coords.as_slice(), color: *color });
                        }
                    }
                }
            });
        rf.render_widget(widget, area.into());
    });
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_logo(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    logo: *const widgets::logo::KatatuiLogo,
) {
    if frame.is_null() || logo.is_null() {
        return;
    }
    let widget = build_logo(unsafe { &*logo });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> =
        Box::new(move |rf| rf.render_widget(widget, area.into()));
    unsafe { (*frame).ops.push(op) };
}

#[no_mangle]
pub extern "C" fn katatui_frame_render_mascot(
    frame: *mut KatatuiFrame,
    area: KatatuiRect,
    mascot: *const widgets::logo::KatatuiMascot,
) {
    if frame.is_null() || mascot.is_null() {
        return;
    }
    let widget = build_mascot(unsafe { &*mascot });
    let op: Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)> =
        Box::new(move |rf| rf.render_widget(widget, area.into()));
    unsafe { (*frame).ops.push(op) };
}

// ---- Events ----

#[no_mangle]
pub extern "C" fn katatui_event_poll(timeout_ms: u64) -> bool {
    event::poll(std::time::Duration::from_millis(timeout_ms)).unwrap_or(false)
}

/// Returns the ASCII value of a key press, or a sentinel value for special keys.
/// Special keys: Up=0xF1, Down=0xF2, Left=0xF3, Right=0xF4, Enter=0x0D, Esc=0x1B
/// Returns 0 for non-key events or unrecognised keys.
#[no_mangle]
pub extern "C" fn katatui_event_read_key_code() -> u8 {
    match event::read() {
        Ok(Event::Key(key_event)) if key_event.kind == KeyEventKind::Press => {
            use crossterm::event::KeyCode;
            match key_event.code {
                KeyCode::Char(c) if (c as u32) < 128 => c as u8,
                KeyCode::Enter => b'\r',
                KeyCode::Esc => 0x1B,
                KeyCode::Up => 0xF1,
                KeyCode::Down => 0xF2,
                KeyCode::Left => 0xF3,
                KeyCode::Right => 0xF4,
                _ => 0,
            }
        }
        _ => 0,
    }
}
