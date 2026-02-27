// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "sample-app-swift",
    platforms: [.macOS(.v13)],
    targets: [
        .executableTarget(
            name: "sample-app-swift",
            dependencies: ["Katatui"],
            linkerSettings: [
                .unsafeFlags(["-L../katatui-ffi/target/aarch64-apple-darwin/release", "-lkatatui_ffi"])
            ]
        ),
        .binaryTarget(
            name: "Katatui",
            path: "../katatui/build/XCFrameworks/release/Katatui.xcframework"
        )
    ]
)
