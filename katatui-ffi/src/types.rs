#[repr(C)]
#[derive(Copy, Clone)]
pub struct KatatuiRect {
    pub x: u16,
    pub y: u16,
    pub width: u16,
    pub height: u16,
}

#[repr(C)]
#[derive(Copy, Clone)]
pub enum KatatuiColor {
    Reset = 0,
    Black = 1,
    Red = 2,
    Green = 3,
    Yellow = 4,
    Blue = 5,
    Magenta = 6,
    Cyan = 7,
    Gray = 8,
    DarkGray = 9,
    LightRed = 10,
    LightGreen = 11,
    LightYellow = 12,
    LightBlue = 13,
    LightMagenta = 14,
    LightCyan = 15,
    White = 16,
    Rgb = 17,
    Indexed = 18,
}

#[repr(C)]
#[derive(Copy, Clone)]
pub struct KatatuiStyle {
    pub fg: KatatuiColor,
    pub bg: KatatuiColor,
    pub bold: bool,
    pub italic: bool,
    pub underlined: bool,
    pub dim: bool,
    pub crossed_out: bool,
    /// RGB/Indexed payload — valid when fg == Rgb or Indexed respectively
    pub fg_r: u8,
    pub fg_g: u8,
    pub fg_b: u8,
    pub fg_index: u8,
    /// RGB/Indexed payload — valid when bg == Rgb or Indexed respectively
    pub bg_r: u8,
    pub bg_g: u8,
    pub bg_b: u8,
    pub bg_index: u8,
}

#[repr(C)]
#[derive(Copy, Clone)]
pub enum KatatuiDirection {
    Horizontal = 0,
    Vertical = 1,
}

#[repr(C)]
#[derive(Copy, Clone)]
pub enum KatatuiConstraintKind {
    Length = 0,
    Percentage = 1,
    Min = 2,
    Max = 3,
    Fill = 4,
}

#[repr(C)]
#[derive(Copy, Clone)]
pub struct KatatuiConstraint {
    pub kind: KatatuiConstraintKind,
    pub value: u16,
}

/// Marker character for canvas/chart data points.
/// Variants are prefixed with `Marker` to avoid C global-enum namespace collisions.
#[repr(C)]
#[derive(Copy, Clone)]
pub enum KatatuiMarker {
    MarkerDot = 0,
    MarkerBlock = 1,
    MarkerBar = 2,
    MarkerBraille = 3,
    MarkerHalfBlock = 4,
    MarkerQuadrant = 5,
}

impl From<KatatuiMarker> for ratatui::symbols::Marker {
    fn from(m: KatatuiMarker) -> Self {
        use ratatui::symbols::Marker;
        match m {
            KatatuiMarker::MarkerDot => Marker::Dot,
            KatatuiMarker::MarkerBlock => Marker::Block,
            KatatuiMarker::MarkerBar => Marker::Bar,
            KatatuiMarker::MarkerBraille => Marker::Braille,
            KatatuiMarker::MarkerHalfBlock => Marker::HalfBlock,
            KatatuiMarker::MarkerQuadrant => Marker::Quadrant,
        }
    }
}

impl From<KatatuiColor> for ratatui::style::Color {
    fn from(c: KatatuiColor) -> Self {
        match c {
            KatatuiColor::Reset => ratatui::style::Color::Reset,
            KatatuiColor::Black => ratatui::style::Color::Black,
            KatatuiColor::Red => ratatui::style::Color::Red,
            KatatuiColor::Green => ratatui::style::Color::Green,
            KatatuiColor::Yellow => ratatui::style::Color::Yellow,
            KatatuiColor::Blue => ratatui::style::Color::Blue,
            KatatuiColor::Magenta => ratatui::style::Color::Magenta,
            KatatuiColor::Cyan => ratatui::style::Color::Cyan,
            KatatuiColor::Gray => ratatui::style::Color::Gray,
            KatatuiColor::DarkGray => ratatui::style::Color::DarkGray,
            KatatuiColor::LightRed => ratatui::style::Color::LightRed,
            KatatuiColor::LightGreen => ratatui::style::Color::LightGreen,
            KatatuiColor::LightYellow => ratatui::style::Color::LightYellow,
            KatatuiColor::LightBlue => ratatui::style::Color::LightBlue,
            KatatuiColor::LightMagenta => ratatui::style::Color::LightMagenta,
            KatatuiColor::LightCyan => ratatui::style::Color::LightCyan,
            KatatuiColor::White => ratatui::style::Color::White,
            KatatuiColor::Rgb | KatatuiColor::Indexed => {
                unreachable!("Rgb/Indexed carry payload — use color_from_katatui, not From<KatatuiColor>")
            }
        }
    }
}

pub(crate) fn color_from_katatui(
    kind: KatatuiColor,
    r: u8,
    g: u8,
    b: u8,
    index: u8,
) -> ratatui::style::Color {
    match kind {
        KatatuiColor::Rgb => ratatui::style::Color::Rgb(r, g, b),
        KatatuiColor::Indexed => ratatui::style::Color::Indexed(index),
        other => other.into(),
    }
}

impl From<KatatuiStyle> for ratatui::style::Style {
    fn from(s: KatatuiStyle) -> Self {
        use ratatui::style::Modifier;
        let fg = color_from_katatui(s.fg, s.fg_r, s.fg_g, s.fg_b, s.fg_index);
        let bg = color_from_katatui(s.bg, s.bg_r, s.bg_g, s.bg_b, s.bg_index);
        let mut style = ratatui::style::Style::default().fg(fg).bg(bg);
        if s.bold {
            style = style.add_modifier(Modifier::BOLD);
        }
        if s.italic {
            style = style.add_modifier(Modifier::ITALIC);
        }
        if s.underlined {
            style = style.add_modifier(Modifier::UNDERLINED);
        }
        if s.dim {
            style = style.add_modifier(Modifier::DIM);
        }
        if s.crossed_out {
            style = style.add_modifier(Modifier::CROSSED_OUT);
        }
        style
    }
}

impl From<KatatuiRect> for ratatui::layout::Rect {
    fn from(r: KatatuiRect) -> Self {
        ratatui::layout::Rect {
            x: r.x,
            y: r.y,
            width: r.width,
            height: r.height,
        }
    }
}

impl From<ratatui::layout::Rect> for KatatuiRect {
    fn from(r: ratatui::layout::Rect) -> Self {
        KatatuiRect {
            x: r.x,
            y: r.y,
            width: r.width,
            height: r.height,
        }
    }
}

impl From<KatatuiConstraint> for ratatui::layout::Constraint {
    fn from(c: KatatuiConstraint) -> Self {
        match c.kind {
            KatatuiConstraintKind::Length => {
                ratatui::layout::Constraint::Length(c.value)
            }
            KatatuiConstraintKind::Percentage => {
                ratatui::layout::Constraint::Percentage(c.value)
            }
            KatatuiConstraintKind::Min => ratatui::layout::Constraint::Min(c.value),
            KatatuiConstraintKind::Max => ratatui::layout::Constraint::Max(c.value),
            KatatuiConstraintKind::Fill => ratatui::layout::Constraint::Fill(c.value),
        }
    }
}
