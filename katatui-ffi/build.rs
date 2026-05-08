fn main() {
    let crate_dir = std::env::var("CARGO_MANIFEST_DIR").unwrap();
    let config = cbindgen::Config::from_file(
        std::path::Path::new(&crate_dir).join("cbindgen.toml"),
    )
    .expect("Unable to load cbindgen.toml");
    cbindgen::Builder::new()
        .with_crate(&crate_dir)
        .with_config(config)
        .generate()
        .expect("cbindgen failed")
        .write_to_file("../katatui/src/nativeInterop/cinterop/katatui.h");
}
