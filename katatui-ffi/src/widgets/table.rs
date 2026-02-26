use crate::types::{KatatuiConstraint, KatatuiStyle};
use std::ffi::CStr;

pub struct KatatuiTable {
    pub headers: Vec<String>,
    pub rows: Vec<Vec<String>>,
    pub current_row: Vec<String>,
    pub widths: Vec<KatatuiConstraint>,
    pub style: Option<KatatuiStyle>,
}

pub fn build_table(t: &KatatuiTable) -> ratatui::widgets::Table<'static> {
    use ratatui::widgets::{Cell, Row, Table};
    let header = Row::new(t.headers.iter().map(|h| Cell::new(h.clone())).collect::<Vec<_>>());
    let rows: Vec<Row<'static>> = t
        .rows
        .iter()
        .map(|row| Row::new(row.iter().map(|c| Cell::new(c.clone())).collect::<Vec<_>>()))
        .collect();
    let widths: Vec<ratatui::layout::Constraint> =
        t.widths.iter().copied().map(Into::into).collect();
    let mut table = Table::new(rows, widths).header(header);
    if let Some(s) = t.style {
        table = table.style(s);
    }
    table
}

#[no_mangle]
pub extern "C" fn katatui_table_new() -> *mut KatatuiTable {
    Box::into_raw(Box::new(KatatuiTable {
        headers: Vec::new(),
        rows: Vec::new(),
        current_row: Vec::new(),
        widths: Vec::new(),
        style: None,
    }))
}

#[no_mangle]
pub extern "C" fn katatui_table_free(table: *mut KatatuiTable) {
    if !table.is_null() {
        unsafe { drop(Box::from_raw(table)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_table_add_header(
    table: *mut KatatuiTable,
    header: *const std::ffi::c_char,
) {
    if table.is_null() || header.is_null() {
        return;
    }
    let s = unsafe { CStr::from_ptr(header) }.to_string_lossy().into_owned();
    unsafe { (*table).headers.push(s) };
}

#[no_mangle]
pub extern "C" fn katatui_table_add_cell(
    table: *mut KatatuiTable,
    cell: *const std::ffi::c_char,
) {
    if table.is_null() || cell.is_null() {
        return;
    }
    let s = unsafe { CStr::from_ptr(cell) }.to_string_lossy().into_owned();
    unsafe { (*table).current_row.push(s) };
}

/// Flushes the current row into the rows list and resets the current row buffer.
#[no_mangle]
pub extern "C" fn katatui_table_next_row(table: *mut KatatuiTable) {
    if table.is_null() {
        return;
    }
    let t = unsafe { &mut *table };
    let row = std::mem::take(&mut t.current_row);
    t.rows.push(row);
}

#[no_mangle]
pub extern "C" fn katatui_table_add_width(
    table: *mut KatatuiTable,
    constraint: KatatuiConstraint,
) {
    if table.is_null() {
        return;
    }
    unsafe { (*table).widths.push(constraint) };
}

#[no_mangle]
pub extern "C" fn katatui_table_set_style(table: *mut KatatuiTable, style: KatatuiStyle) {
    if table.is_null() {
        return;
    }
    unsafe { (*table).style = Some(style) };
}
