// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "sample-app-swift",
    platforms: [.macOS(.v13)],
    targets: [
        .executableTarget(
            name: "sample-app-swift",
            dependencies: ["Katatui"]
        ),
        .binaryTarget(
            name: "Katatui",
            path: "../katatui/build/XCFrameworks/release/Katatui.xcframework"
        )
    ]
)
