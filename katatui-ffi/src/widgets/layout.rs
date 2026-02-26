use crate::types::{KatatuiConstraint, KatatuiDirection, KatatuiRect};

pub struct KatatuiLayout {
    pub(crate) direction: KatatuiDirection,
    pub(crate) constraints: Vec<KatatuiConstraint>,
}

#[no_mangle]
pub extern "C" fn katatui_layout_new(direction: KatatuiDirection) -> *mut KatatuiLayout {
    Box::into_raw(Box::new(KatatuiLayout {
        direction,
        constraints: Vec::new(),
    }))
}

#[no_mangle]
pub extern "C" fn katatui_layout_free(layout: *mut KatatuiLayout) {
    if !layout.is_null() {
        unsafe { drop(Box::from_raw(layout)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_layout_add_constraint(
    layout: *mut KatatuiLayout,
    constraint: KatatuiConstraint,
) {
    if layout.is_null() {
        return;
    }
    unsafe { (*layout).constraints.push(constraint) };
}

/// Splits the layout area into rects according to the constraints.
/// Returns the number of rects written into `out_rects`.
/// `out_rects` must point to a buffer of at least `constraints.len() + 1` elements.
#[no_mangle]
pub extern "C" fn katatui_layout_split(
    layout: *mut KatatuiLayout,
    area: KatatuiRect,
    out_rects: *mut KatatuiRect,
) -> u32 {
    if layout.is_null() || out_rects.is_null() {
        return 0;
    }
    let l = unsafe { &*layout };
    let direction = match l.direction {
        KatatuiDirection::Horizontal => ratatui::layout::Direction::Horizontal,
        KatatuiDirection::Vertical => ratatui::layout::Direction::Vertical,
    };
    let constraints: Vec<ratatui::layout::Constraint> =
        l.constraints.iter().copied().map(Into::into).collect();
    let rects = ratatui::layout::Layout::default()
        .direction(direction)
        .constraints(constraints)
        .split(area.into());
    let count = rects.len();
    unsafe {
        for (i, r) in rects.iter().enumerate() {
            *out_rects.add(i) = (*r).into();
        }
    }
    count as u32
}
