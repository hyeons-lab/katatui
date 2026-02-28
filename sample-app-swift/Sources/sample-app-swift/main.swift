import Katatui

Katatui.terminal { t in
    var tick: Int32 = 0

    mainLoop: while true {
        var shouldRender = false
        switch Event.readEvent() {
        case .tick:
            tick = (tick + 1) % 100
            shouldRender = true
        case .key(let key):
            if key?.character == "q" { break mainLoop }
            shouldRender = true
        case .other:
            break
        }

        if shouldRender {
            t.draw { frame in
                let areas = Layout.shared.vertical([
                    Constraint.length(3),
                    Constraint.fill(1)
                ]).split(area: frame.size)

                let headerArea = areas[0]
                let bodyArea = areas[1]

                frame.block(area: headerArea) { b in
                    b.title = "Katatui Swift DSL Sample"
                    b.borders = Borders.all.bits
                }

                frame.paragraph(area: headerArea.inner(), text: "Hello from Swift DSL! Tick: \(tick)")

                frame.block(area: bodyArea) { b in
                    b.title = "Progress"
                    b.borders = Borders.all.bits
                }

                frame.gauge(area: bodyArea.inner()) { g in
                    g.percent = UInt8(tick)
                }
            }
        }
    }
}
