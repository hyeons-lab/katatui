use crate::types::KatatuiRect;

type RenderOp = Box<dyn for<'a> FnOnce(&mut ratatui::Frame<'a>)>;

pub struct KatatuiTerminal {
    pub(crate) inner: ratatui::DefaultTerminal,
    pub(crate) drawing: bool,
    pub(crate) current_frame: Option<*mut KatatuiFrame>,
}

pub struct KatatuiFrame {
    pub(crate) area: KatatuiRect,
    pub(crate) ops: Vec<RenderOp>,
}
