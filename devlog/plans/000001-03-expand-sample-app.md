## Thinking

The sample app currently has 3 tabs (Dashboard, Widgets, Image) and covers only the first 10 widgets. Sessions 9-12 added 5 new widgets: Scrollbar, Chart, Canvas, Logo, and Mascot. These widgets need to be demonstrated in the sample app.

The logical structure is to give each major new widget its own tab, add a Branding tab for Logo + Mascot side-by-side, and expand the Widgets table to list all 15 widgets. The Image tab stays unchanged and Dashboard stays unchanged.

The Chart tab uses `tick` for animation (sin/cos waves scrolling horizontally). Canvas uses tick for a pulsing circle and rotating spokes. Scrollbar auto-scrolls via tick. Branding alternates the mascot eye color on a timer derived from tick.

ScrollbarState is an AutoCloseable (wraps a C pointer), so it must be created before the draw loop and closed after.

## Plan

1. Create plan file (this file)
2. Modify `sample-app/src/nativeMain/kotlin/com/hyeonslab/katatui/sample/main.kt`:
   - Add imports for new widget types and math functions
   - Update TAB_NAMES to 7 entries
   - Add SCROLL_LINES constant (~25 lines of scrollable text)
   - Append 5 new rows to WIDGET_ROWS
   - Add `val scrollbarState = ScrollbarState()` before the draw loop
   - Add `scrollbarState.close()` after the loop
   - Add renderChart(area, tick) function
   - Add renderCanvas(area, tick) function
   - Add renderScrollbar(area, scrollbarState, tick) function
   - Add renderBranding(area, tick) function
   - Expand `when (activeTab)` to cases 0-6
   - Add key bindings '4'-'7'
   - Update help text in renderDashboard
3. Run ktfmtCheck + detekt + tests to verify
4. Commit and update devlog
