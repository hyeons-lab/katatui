#include <stdarg.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

typedef enum KatatuiColor {
  KatatuiColor_Reset = 0,
  KatatuiColor_Black = 1,
  KatatuiColor_Red = 2,
  KatatuiColor_Green = 3,
  KatatuiColor_Yellow = 4,
  KatatuiColor_Blue = 5,
  KatatuiColor_Magenta = 6,
  KatatuiColor_Cyan = 7,
  KatatuiColor_Gray = 8,
  KatatuiColor_DarkGray = 9,
  KatatuiColor_LightRed = 10,
  KatatuiColor_LightGreen = 11,
  KatatuiColor_LightYellow = 12,
  KatatuiColor_LightBlue = 13,
  KatatuiColor_LightMagenta = 14,
  KatatuiColor_LightCyan = 15,
  KatatuiColor_White = 16,
  KatatuiColor_Rgb = 17,
  KatatuiColor_Indexed = 18,
} KatatuiColor;

typedef enum KatatuiConstraintKind {
  KatatuiConstraintKind_Length = 0,
  KatatuiConstraintKind_Percentage = 1,
  KatatuiConstraintKind_Min = 2,
  KatatuiConstraintKind_Max = 3,
  KatatuiConstraintKind_Fill = 4,
} KatatuiConstraintKind;

typedef enum KatatuiDirection {
  KatatuiDirection_Horizontal = 0,
  KatatuiDirection_Vertical = 1,
} KatatuiDirection;

typedef enum KatatuiGraphType {
  KatatuiGraphType_Scatter = 0,
  KatatuiGraphType_Line = 1,
  KatatuiGraphType_Bar = 2,
} KatatuiGraphType;

typedef enum KatatuiLogoSize {
  KatatuiLogoSize_Tiny = 0,
  KatatuiLogoSize_Small = 1,
} KatatuiLogoSize;

typedef enum KatatuiMarker {
  KatatuiMarker_Dot = 0,
  KatatuiMarker_Block = 1,
  KatatuiMarker_Bar = 2,
  KatatuiMarker_Braille = 3,
  KatatuiMarker_HalfBlock = 4,
  KatatuiMarker_Quadrant = 5,
} KatatuiMarker;

/**
 * The mascot's eye state.  ratatui 0.30 `MascotEyeColor` only has `Default` and `Red`.
 */
typedef enum KatatuiMascotEyeColor {
  KatatuiMascotEyeColor_Default = 0,
  KatatuiMascotEyeColor_Red = 1,
} KatatuiMascotEyeColor;

typedef enum KatatuiScrollbarOrientation {
  KatatuiScrollbarOrientation_VerticalRight = 0,
  KatatuiScrollbarOrientation_VerticalLeft = 1,
  KatatuiScrollbarOrientation_HorizontalBottom = 2,
  KatatuiScrollbarOrientation_HorizontalTop = 3,
} KatatuiScrollbarOrientation;

typedef struct KatatuiBarChart KatatuiBarChart;

typedef struct KatatuiBlock KatatuiBlock;

typedef struct KatatuiCanvas KatatuiCanvas;

typedef struct KatatuiChart KatatuiChart;

typedef struct KatatuiClear KatatuiClear;

typedef struct KatatuiFrame KatatuiFrame;

typedef struct KatatuiGauge KatatuiGauge;

typedef struct KatatuiImageState KatatuiImageState;

typedef struct KatatuiLayout KatatuiLayout;

typedef struct KatatuiLineGauge KatatuiLineGauge;

typedef struct KatatuiList KatatuiList;

typedef struct KatatuiListState KatatuiListState;

typedef struct KatatuiLogo KatatuiLogo;

typedef struct KatatuiMascot KatatuiMascot;

typedef struct KatatuiParagraph KatatuiParagraph;

/**
 * Opaque handle to a ratatui_image Picker.
 * Must be created on the main thread (where terminal I/O is available) so that
 * `Picker::from_query_stdio()` can detect the best image protocol.
 */
typedef struct KatatuiPicker KatatuiPicker;

/**
 * Symbols are stored as owned `String`; drop is automatic when the struct is freed.
 */
typedef struct KatatuiScrollbar KatatuiScrollbar;

typedef struct KatatuiScrollbarState KatatuiScrollbarState;

typedef struct KatatuiSparkline KatatuiSparkline;

typedef struct KatatuiTable KatatuiTable;

typedef struct KatatuiTableState KatatuiTableState;

typedef struct KatatuiTabs KatatuiTabs;

typedef struct KatatuiTerminal KatatuiTerminal;

typedef struct KatatuiRect {
  uint16_t x;
  uint16_t y;
  uint16_t width;
  uint16_t height;
} KatatuiRect;

typedef struct KatatuiStyle {
  enum KatatuiColor fg;
  enum KatatuiColor bg;
  bool bold;
  bool italic;
  bool underlined;
  bool dim;
  bool crossed_out;
  /**
   * RGB/Indexed payload — valid when fg == Rgb or Indexed respectively
   */
  uint8_t fg_r;
  uint8_t fg_g;
  uint8_t fg_b;
  uint8_t fg_index;
  /**
   * RGB/Indexed payload — valid when bg == Rgb or Indexed respectively
   */
  uint8_t bg_r;
  uint8_t bg_g;
  uint8_t bg_b;
  uint8_t bg_index;
} KatatuiStyle;

typedef struct KatatuiConstraint {
  enum KatatuiConstraintKind kind;
  uint16_t value;
} KatatuiConstraint;

struct KatatuiTerminal *katatui_terminal_new(void);

void katatui_terminal_free(struct KatatuiTerminal *terminal);

struct KatatuiTableState *katatui_table_state_new(void);

void katatui_table_state_free(struct KatatuiTableState *state);

void katatui_table_state_select(struct KatatuiTableState *state, int32_t index);

void katatui_terminal_restore(struct KatatuiTerminal *terminal);

struct KatatuiFrame *katatui_terminal_begin_draw(struct KatatuiTerminal *terminal);

/**
 * Executes all queued render operations in a single `terminal.draw()` call, then releases
 * the frame. This is the only place where ratatui's frame is actually borrowed.
 */
void katatui_terminal_end_draw(struct KatatuiTerminal *terminal);

struct KatatuiRect katatui_frame_size(const struct KatatuiFrame *frame);

void katatui_frame_render_block(struct KatatuiFrame *frame,
                                struct KatatuiRect area,
                                const struct KatatuiBlock *block);

void katatui_frame_render_paragraph(struct KatatuiFrame *frame,
                                    struct KatatuiRect area,
                                    const struct KatatuiParagraph *para);

void katatui_frame_render_list(struct KatatuiFrame *frame,
                               struct KatatuiRect area,
                               const struct KatatuiList *list,
                               struct KatatuiListState *state);

void katatui_frame_render_clear(struct KatatuiFrame *frame,
                                struct KatatuiRect area,
                                const struct KatatuiClear *clear);

void katatui_frame_render_gauge(struct KatatuiFrame *frame,
                                struct KatatuiRect area,
                                const struct KatatuiGauge *gauge);

void katatui_frame_render_line_gauge(struct KatatuiFrame *frame,
                                     struct KatatuiRect area,
                                     const struct KatatuiLineGauge *gauge);

void katatui_frame_render_sparkline(struct KatatuiFrame *frame,
                                    struct KatatuiRect area,
                                    const struct KatatuiSparkline *sparkline);

void katatui_frame_render_bar_chart(struct KatatuiFrame *frame,
                                    struct KatatuiRect area,
                                    const struct KatatuiBarChart *chart);

void katatui_frame_render_tabs(struct KatatuiFrame *frame,
                               struct KatatuiRect area,
                               const struct KatatuiTabs *tabs);

void katatui_frame_render_table(struct KatatuiFrame *frame,
                                struct KatatuiRect area,
                                const struct KatatuiTable *table,
                                struct KatatuiTableState *state);

void katatui_frame_render_image(struct KatatuiFrame *frame,
                                struct KatatuiRect area,
                                struct KatatuiImageState *state);

void katatui_frame_render_scrollbar(struct KatatuiFrame *frame,
                                    struct KatatuiRect area,
                                    const struct KatatuiScrollbar *sb,
                                    struct KatatuiScrollbarState *state);

void katatui_frame_render_chart(struct KatatuiFrame *frame,
                                struct KatatuiRect area,
                                const struct KatatuiChart *chart);

void katatui_frame_render_canvas(struct KatatuiFrame *frame,
                                 struct KatatuiRect area,
                                 const struct KatatuiCanvas *canvas);

void katatui_frame_render_logo(struct KatatuiFrame *frame,
                               struct KatatuiRect area,
                               const struct KatatuiLogo *logo);

void katatui_frame_render_mascot(struct KatatuiFrame *frame,
                                 struct KatatuiRect area,
                                 const struct KatatuiMascot *mascot);

bool katatui_event_poll(uint64_t timeout_ms);

/**
 * Returns the ASCII value of a key press, or a sentinel value for special keys.
 * Special keys: Up=0xF1, Down=0xF2, Left=0xF3, Right=0xF4, Enter=0x0D, Esc=0x1B
 * Returns 0 for non-key events or unrecognised keys.
 */
uint8_t katatui_event_read_key_code(void);

/**
 * Blocking event read with tick timeout. Blocks until either a terminal event arrives or
 * `timeout_ms` milliseconds elapse. Returns:
 *   256 = Tick (timeout elapsed — no event within the interval)
 *   1–255 = key code (same mapping as katatui_event_read_key_code)
 *   0 = other/unknown event (real resize, mouse, paste, etc.)
 */
uint32_t katatui_event_read_extended(uint64_t timeout_ms);

struct KatatuiBarChart *katatui_bar_chart_new(void);

void katatui_bar_chart_free(struct KatatuiBarChart *chart);

/**
 * Appends a bar with the given label and value.
 * Named `_bar` (not `_add_bar`) so codegen skips it; use the hand-written Kotlin extension.
 */
void katatui_bar_chart_bar(struct KatatuiBarChart *chart, const char *label, uint64_t value);

void katatui_bar_chart_set_bar_width(struct KatatuiBarChart *chart, uint16_t width);

void katatui_bar_chart_set_bar_gap(struct KatatuiBarChart *chart, uint16_t gap);

void katatui_bar_chart_set_max(struct KatatuiBarChart *chart, uint64_t max);

void katatui_bar_chart_set_style(struct KatatuiBarChart *chart, struct KatatuiStyle style);

struct KatatuiBlock *katatui_block_new(void);

void katatui_block_free(struct KatatuiBlock *block);

void katatui_block_set_title(struct KatatuiBlock *block, const char *title);

void katatui_block_set_borders(struct KatatuiBlock *block, uint32_t borders);

void katatui_block_set_style(struct KatatuiBlock *block, struct KatatuiStyle style);

struct KatatuiCanvas *katatui_canvas_new(void);

void katatui_canvas_free(struct KatatuiCanvas *canvas);

/**
 * Sets x-axis bounds.  Non-`set_` prefix so codegen skips it (double params).
 */
void katatui_canvas_x_bounds(struct KatatuiCanvas *canvas, double min, double max);

/**
 * Sets y-axis bounds.  Non-`set_` prefix so codegen skips it (double params).
 */
void katatui_canvas_y_bounds(struct KatatuiCanvas *canvas, double min, double max);

void katatui_canvas_set_marker(struct KatatuiCanvas *canvas, enum KatatuiMarker marker);

/**
 * Clears all buffered drawing commands and resets the current-points batch state.
 */
void katatui_canvas_clear(struct KatatuiCanvas *canvas);

/**
 * Queues a circle.  Non-`set_`/`add_` prefix so codegen skips it.
 */
void katatui_canvas_circle(struct KatatuiCanvas *canvas,
                           double x,
                           double y,
                           double radius,
                           struct KatatuiStyle color);

/**
 * Queues a line.  Non-`set_`/`add_` prefix so codegen skips it.
 */
void katatui_canvas_line(struct KatatuiCanvas *canvas,
                         double x1,
                         double y1,
                         double x2,
                         double y2,
                         struct KatatuiStyle color);

/**
 * Queues a rectangle.  Non-`set_`/`add_` prefix so codegen skips it.
 */
void katatui_canvas_rectangle(struct KatatuiCanvas *canvas,
                              double x,
                              double y,
                              double width,
                              double height,
                              struct KatatuiStyle color);

/**
 * Begins a `Points` batch with the given color.
 */
void katatui_canvas_begin_points(struct KatatuiCanvas *canvas, struct KatatuiStyle color);

/**
 * Adds a point to the current `Points` batch.
 * Non-`add_` prefix so codegen skips it.
 */
void katatui_canvas_point(struct KatatuiCanvas *canvas, double x, double y);

/**
 * Commits the current `Points` batch.  No-op if the batch is empty.
 */
void katatui_canvas_commit_points(struct KatatuiCanvas *canvas);

struct KatatuiChart *katatui_chart_new(void);

void katatui_chart_free(struct KatatuiChart *chart);

void katatui_chart_set_dataset_name(struct KatatuiChart *chart, const char *name);

void katatui_chart_set_dataset_graph_type(struct KatatuiChart *chart,
                                          enum KatatuiGraphType graph_type);

void katatui_chart_set_dataset_marker(struct KatatuiChart *chart, enum KatatuiMarker marker);

void katatui_chart_set_dataset_style(struct KatatuiChart *chart, struct KatatuiStyle style);

/**
 * Adds a data point (x, y) to the current dataset being built.
 * Uses non-`add_` prefix so codegen does not attempt to wrap this function.
 */
void katatui_chart_dataset_point(struct KatatuiChart *chart, double x, double y);

/**
 * Commits the current dataset into the chart's dataset list.
 */
void katatui_chart_commit_dataset(struct KatatuiChart *chart);

void katatui_chart_set_x_title(struct KatatuiChart *chart, const char *title);

/**
 * Sets the x-axis bounds.  Uses non-`set_` prefix so codegen skips it (double params).
 */
void katatui_chart_x_bounds(struct KatatuiChart *chart, double min, double max);

void katatui_chart_add_x_label(struct KatatuiChart *chart, const char *label);

void katatui_chart_set_x_style(struct KatatuiChart *chart, struct KatatuiStyle style);

void katatui_chart_set_y_title(struct KatatuiChart *chart, const char *title);

/**
 * Sets the y-axis bounds.  Uses non-`set_` prefix so codegen skips it (double params).
 */
void katatui_chart_y_bounds(struct KatatuiChart *chart, double min, double max);

void katatui_chart_add_y_label(struct KatatuiChart *chart, const char *label);

void katatui_chart_set_y_style(struct KatatuiChart *chart, struct KatatuiStyle style);

void katatui_chart_set_style(struct KatatuiChart *chart, struct KatatuiStyle style);

struct KatatuiClear *katatui_clear_new(void);

void katatui_clear_free(struct KatatuiClear *clear);

struct KatatuiGauge *katatui_gauge_new(void);

void katatui_gauge_free(struct KatatuiGauge *gauge);

void katatui_gauge_set_percent(struct KatatuiGauge *gauge, uint8_t percent);

void katatui_gauge_set_label(struct KatatuiGauge *gauge, const char *label);

void katatui_gauge_set_style(struct KatatuiGauge *gauge, struct KatatuiStyle style);

void katatui_gauge_set_gauge_style(struct KatatuiGauge *gauge, struct KatatuiStyle style);

/**
 * Creates a Picker by querying the terminal for the best image protocol.
 * Falls back to half-block rendering if the query fails.
 * Must be called from the main thread.
 */
struct KatatuiPicker *katatui_picker_new(void);

void katatui_picker_free(struct KatatuiPicker *picker);

/**
 * Creates image state from in-memory bytes using a pre-created Picker.
 * Safe to call from any thread as long as no other thread is concurrently
 * accessing the same Picker.
 * Returns null if the bytes cannot be decoded.
 */
struct KatatuiImageState *katatui_image_state_from_bytes_with_picker(const uint8_t *data,
                                                                     uintptr_t len,
                                                                     const struct KatatuiPicker *picker);

/**
 * Creates image state from a file path.
 * Uses Unicode half-block rendering, which works in every terminal.
 * Returns null if the file cannot be opened or decoded.
 */
struct KatatuiImageState *katatui_image_state_new(const char *path);

/**
 * Creates image state from in-memory bytes (PNG, JPEG, GIF, WebP, etc.).
 * Returns null if the bytes cannot be decoded as a supported image format.
 */
struct KatatuiImageState *katatui_image_state_from_bytes(const uint8_t *data, uintptr_t len);

void katatui_image_state_free(struct KatatuiImageState *state);

struct KatatuiLayout *katatui_layout_new(enum KatatuiDirection direction);

void katatui_layout_free(struct KatatuiLayout *layout);

void katatui_layout_add_constraint(struct KatatuiLayout *layout,
                                   struct KatatuiConstraint constraint);

/**
 * Splits the layout area into rects according to the constraints.
 * Returns the number of rects written into `out_rects`.
 * `out_rects` must point to a buffer of at least `constraints.len() + 1` elements.
 */
uint32_t katatui_layout_split(struct KatatuiLayout *layout,
                              struct KatatuiRect area,
                              struct KatatuiRect *out_rects);

struct KatatuiLineGauge *katatui_line_gauge_new(void);

void katatui_line_gauge_free(struct KatatuiLineGauge *gauge);

void katatui_line_gauge_set_percent(struct KatatuiLineGauge *gauge, uint8_t percent);

void katatui_line_gauge_set_style(struct KatatuiLineGauge *gauge, struct KatatuiStyle style);

void katatui_line_gauge_set_line_style(struct KatatuiLineGauge *gauge, struct KatatuiStyle style);

struct KatatuiList *katatui_list_new(void);

void katatui_list_free(struct KatatuiList *list);

void katatui_list_add_item(struct KatatuiList *list, const char *item);

struct KatatuiListState *katatui_list_state_new(void);

void katatui_list_state_free(struct KatatuiListState *state);

void katatui_list_state_select(struct KatatuiListState *state, int32_t index);

struct KatatuiLogo *katatui_logo_new(void);

void katatui_logo_free(struct KatatuiLogo *logo);

void katatui_logo_set_size(struct KatatuiLogo *logo, enum KatatuiLogoSize size);

struct KatatuiMascot *katatui_mascot_new(void);

void katatui_mascot_free(struct KatatuiMascot *mascot);

void katatui_mascot_set_eye_color(struct KatatuiMascot *mascot,
                                  enum KatatuiMascotEyeColor eye_color);

struct KatatuiParagraph *katatui_paragraph_new(const char *text);

void katatui_paragraph_free(struct KatatuiParagraph *para);

void katatui_paragraph_set_style(struct KatatuiParagraph *para, struct KatatuiStyle style);

void katatui_paragraph_set_text(struct KatatuiParagraph *para, const char *text);

void katatui_paragraph_set_wrap(struct KatatuiParagraph *para, bool wrap);

struct KatatuiScrollbar *katatui_scrollbar_new(void);

void katatui_scrollbar_free(struct KatatuiScrollbar *sb);

void katatui_scrollbar_set_orientation(struct KatatuiScrollbar *sb,
                                       enum KatatuiScrollbarOrientation orientation);

void katatui_scrollbar_set_thumb_symbol(struct KatatuiScrollbar *sb, const char *sym);

void katatui_scrollbar_set_track_symbol(struct KatatuiScrollbar *sb, const char *sym);

void katatui_scrollbar_set_begin_symbol(struct KatatuiScrollbar *sb, const char *sym);

void katatui_scrollbar_set_end_symbol(struct KatatuiScrollbar *sb, const char *sym);

void katatui_scrollbar_set_thumb_style(struct KatatuiScrollbar *sb, struct KatatuiStyle style);

void katatui_scrollbar_set_track_style(struct KatatuiScrollbar *sb, struct KatatuiStyle style);

void katatui_scrollbar_set_begin_style(struct KatatuiScrollbar *sb, struct KatatuiStyle style);

void katatui_scrollbar_set_end_style(struct KatatuiScrollbar *sb, struct KatatuiStyle style);

struct KatatuiScrollbarState *katatui_scrollbar_state_new(void);

void katatui_scrollbar_state_free(struct KatatuiScrollbarState *state);

void katatui_scrollbar_state_set_content_length(struct KatatuiScrollbarState *state,
                                                uint16_t length);

void katatui_scrollbar_state_set_position(struct KatatuiScrollbarState *state, uint16_t position);

void katatui_scrollbar_state_set_viewport_content_length(struct KatatuiScrollbarState *state,
                                                         uint16_t length);

struct KatatuiSparkline *katatui_sparkline_new(void);

void katatui_sparkline_free(struct KatatuiSparkline *sparkline);

void katatui_sparkline_add_data(struct KatatuiSparkline *sparkline, uint64_t value);

void katatui_sparkline_set_max(struct KatatuiSparkline *sparkline, uint64_t max);

void katatui_sparkline_set_style(struct KatatuiSparkline *sparkline, struct KatatuiStyle style);

void katatui_sparkline_set_bar_style(struct KatatuiSparkline *sparkline, struct KatatuiStyle style);

struct KatatuiTable *katatui_table_new(void);

void katatui_table_free(struct KatatuiTable *table);

void katatui_table_add_header(struct KatatuiTable *table, const char *header);

void katatui_table_add_cell(struct KatatuiTable *table, const char *cell);

/**
 * Flushes the current row into the rows list and resets the current row buffer.
 */
void katatui_table_next_row(struct KatatuiTable *table);

void katatui_table_add_width(struct KatatuiTable *table, struct KatatuiConstraint constraint);

void katatui_table_set_style(struct KatatuiTable *table, struct KatatuiStyle style);

struct KatatuiTabs *katatui_tabs_new(void);

void katatui_tabs_free(struct KatatuiTabs *tabs);

void katatui_tabs_add_title(struct KatatuiTabs *tabs, const char *title);

void katatui_tabs_set_selected(struct KatatuiTabs *tabs, uint32_t selected);

void katatui_tabs_set_style(struct KatatuiTabs *tabs, struct KatatuiStyle style);
