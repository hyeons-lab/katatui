import Katatui

// --- Core DSL ---

// --- Layout DSL ---

public enum Constraint {
    public static func length(_ v: Int32) -> Katatui.Constraint {
        return Katatui.Constraint.Companion.shared.Length(v: v)
    }
    public static func fill(_ v: Int32) -> Katatui.Constraint {
        return Katatui.Constraint.Companion.shared.Fill(v: v)
    }
    public static func percentage(_ v: Int32) -> Katatui.Constraint {
        return Katatui.Constraint.Companion.shared.Percentage(v: v)
    }
}

public enum Borders {
    public static var none: Katatui.Borders { Katatui.Borders.Companion.shared.none }
    public static var top: Katatui.Borders { Katatui.Borders.Companion.shared.top }
    public static var right: Katatui.Borders { Katatui.Borders.Companion.shared.right }
    public static var bottom: Katatui.Borders { Katatui.Borders.Companion.shared.bottom }
    public static var left: Katatui.Borders { Katatui.Borders.Companion.shared.left }
    public static var all: Katatui.Borders { Katatui.Borders.Companion.shared.all }
}

extension Katatui.Layout {
    public func vertical(_ constraints: [Katatui.Constraint]) -> Katatui.LayoutBuilder {
        return self.vertical(constraints: constraints)
    }
    
    public func horizontal(_ constraints: [Katatui.Constraint]) -> Katatui.LayoutBuilder {
        return self.horizontal(constraints: constraints)
    }
}

// --- Widget DSL ---

extension Frame {
    public func block(area: Rect? = nil, _ closure: (Block) -> Void) {
        let b = Block.Companion.shared.invoke(init: { _ in })
        closure(b)
        self.render(widget: b, area: b.area ?? area ?? self.size)
        b.close()
    }
    
    public func paragraph(area: Rect? = nil, text: String? = nil, _ closure: (Paragraph) -> Void = { _ in }) {
        let p = Paragraph.Companion.shared.invoke(text: text ?? "", init: { _ in })
        closure(p)
        self.render(widget: p, area: p.area ?? area ?? self.size)
        p.close()
    }
    
    public func gauge(area: Rect? = nil, _ closure: (Gauge) -> Void) {
        let g = Gauge.Companion.shared.invoke(init: { _ in })
        closure(g)
        self.render(widget: g, area: g.area ?? area ?? self.size)
        g.close()
    }
}

// --- Event DSL ---

public enum Event {
    public static func readEvent(timeoutMs: Int64 = 100) -> TerminalEvent {
        return EventKt.readEvent(timeoutMs: timeoutMs)
    }
}
