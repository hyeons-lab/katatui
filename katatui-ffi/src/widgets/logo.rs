#[repr(C)]
#[derive(Copy, Clone)]
pub enum KatatuiLogoSize {
    Tiny = 0,
    Small = 1,
}

pub struct KatatuiLogo {
    pub size: KatatuiLogoSize,
}

pub fn build_logo(l: &KatatuiLogo) -> ratatui::widgets::RatatuiLogo {
    match l.size {
        KatatuiLogoSize::Tiny => ratatui::widgets::RatatuiLogo::tiny(),
        KatatuiLogoSize::Small => ratatui::widgets::RatatuiLogo::small(),
    }
}

#[no_mangle]
pub extern "C" fn katatui_logo_new() -> *mut KatatuiLogo {
    Box::into_raw(Box::new(KatatuiLogo { size: KatatuiLogoSize::Tiny }))
}

#[no_mangle]
pub extern "C" fn katatui_logo_free(logo: *mut KatatuiLogo) {
    if !logo.is_null() {
        unsafe { drop(Box::from_raw(logo)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_logo_set_size(logo: *mut KatatuiLogo, size: KatatuiLogoSize) {
    if logo.is_null() {
        return;
    }
    unsafe { (*logo).size = size };
}

// ---- Mascot ----

/// The mascot's eye state.  ratatui 0.30 `MascotEyeColor` only has `Default` and `Red`.
/// Variants are prefixed with `Eye` to avoid C global-enum namespace collisions.
#[repr(C)]
#[derive(Copy, Clone)]
pub enum KatatuiMascotEyeColor {
    /// Eye open (default)
    EyeDefault = 0,
    /// Eye blinking / red
    EyeRed = 1,
}

impl From<KatatuiMascotEyeColor> for ratatui::widgets::MascotEyeColor {
    fn from(c: KatatuiMascotEyeColor) -> Self {
        match c {
            KatatuiMascotEyeColor::EyeDefault => ratatui::widgets::MascotEyeColor::Default,
            KatatuiMascotEyeColor::EyeRed => ratatui::widgets::MascotEyeColor::Red,
        }
    }
}

pub struct KatatuiMascot {
    pub eye_color: KatatuiMascotEyeColor,
}

pub fn build_mascot(m: &KatatuiMascot) -> ratatui::widgets::RatatuiMascot {
    ratatui::widgets::RatatuiMascot::default().set_eye(m.eye_color.into())
}

#[no_mangle]
pub extern "C" fn katatui_mascot_new() -> *mut KatatuiMascot {
    Box::into_raw(Box::new(KatatuiMascot { eye_color: KatatuiMascotEyeColor::EyeDefault }))
}

#[no_mangle]
pub extern "C" fn katatui_mascot_free(mascot: *mut KatatuiMascot) {
    if !mascot.is_null() {
        unsafe { drop(Box::from_raw(mascot)) };
    }
}

#[no_mangle]
pub extern "C" fn katatui_mascot_set_eye_color(
    mascot: *mut KatatuiMascot,
    eye_color: KatatuiMascotEyeColor,
) {
    if mascot.is_null() {
        return;
    }
    unsafe { (*mascot).eye_color = eye_color };
}
