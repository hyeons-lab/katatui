// swift-tools-version: 5.9
import PackageDescription

#if arch(arm64)
let rustLibDir = "-L../katatui-ffi/target/aarch64-apple-darwin/release"
#else
let rustLibDir = "-L../katatui-ffi/target/x86_64-apple-darwin/release"
#endif

let package = Package(
    name: "sample-app-swift",
    platforms: [.macOS(.v13)],
    targets: [
        .executableTarget(
            name: "sample-app-swift",
            dependencies: ["Katatui"],
            linkerSettings: [
                .unsafeFlags([rustLibDir, "-lkatatui_ffi"])
            ]
        ),
        .binaryTarget(
            name: "Katatui",
            path: "../katatui/build/XCFrameworks/release/Katatui.xcframework"
        )
    ]
)
