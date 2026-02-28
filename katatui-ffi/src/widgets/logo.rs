#[repr(C)]
#[derive(Copy, Clone)]
pub enum KatatuiLogoSize {
    Tiny = 0,
    Small = 1,
}

pub struct KatatuiLogo {
    pub size: KatatuiLogoSize,
}

// "katatui" rendered in the same block-character style as the ratatui logo.
// Only the first letter differs: r → k (▌▞/▌▚ for tiny, █▌▞▝/█▌▚▗ for small).
const KATATUI_LOGO_TINY: &str =
    "▌▞▗▀▖▜▘▞▚▝▛▐ ▌▌\n▌▚▐▀▌▐ ▛▜ ▌▝▄▘▌\n";

const KATATUI_LOGO_SMALL: &str =
    "█▌▞▝ ▄▀▀▄▝▜▛▘▄▀▀▄▝▜▛▘█  █ █\n█▌▚▗ █▀▀█ ▐▌ █▀▀█ ▐▌ ▀▄▄▀ █\n";

pub fn build_logo(l: &KatatuiLogo) -> ratatui::text::Text<'static> {
    ratatui::text::Text::raw(match l.size {
        KatatuiLogoSize::Tiny => KATATUI_LOGO_TINY,
        KatatuiLogoSize::Small => KATATUI_LOGO_SMALL,
    })
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
#[repr(C)]
#[derive(Copy, Clone)]
pub enum KatatuiMascotEyeColor {
    Default = 0,
    Red = 1,
}

impl From<KatatuiMascotEyeColor> for ratatui::widgets::MascotEyeColor {
    fn from(c: KatatuiMascotEyeColor) -> Self {
        match c {
            KatatuiMascotEyeColor::Default => ratatui::widgets::MascotEyeColor::Default,
            KatatuiMascotEyeColor::Red => ratatui::widgets::MascotEyeColor::Red,
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
    Box::into_raw(Box::new(KatatuiMascot { eye_color: KatatuiMascotEyeColor::Default }))
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
