pub mod terminal;
pub mod types;
pub mod widgets;

use crossterm::event::{self, Event, KeyEventKind};
use terminal::{KatatuiFrame, KatatuiTerminal};
use types::KatatuiRect;
use widgets::{block::build_block, list::build_list, paragraph::build_paragraph};

pub use types::*;

// ---- Terminal lifecycle ----

#[no_mangle]
pub extern "C" fn katatui_terminal_new() -> *mut KatatuiTerminal {
    let inner = ratatui::init();
    Box::into_raw(Box::new(KatatuiTerminal {
        inner,
        drawing: false,
        current_frame: None,
    }))
}

#[no_mangle]
pub extern "C" fn katatui_terminal_free(terminal: *mut KatatuiTerminal) {
    if !terminal.is_null() {
        unsafe { drop(Box::from_raw(terminal)) };
    }
}

/// `ratatui::init()` already enables raw mode + alternate screen.
/// This function exists for symmetry with the Kotlin API.
#[no_mangle]
pub extern "C" fn katatui_terminal_init(terminal: *mut KatatuiTerminal) -> bool {
    !terminal.is_null()
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
    _state: *mut widgets::list::KatatuiListState,
) {
    if frame.is_null() || list.is_null() {
        return;
    }
    let widget = build_list(unsafe { &*list });
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
