#include <stdarg.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

typedef enum KatatuiColor {
  Reset = 0,
  Black = 1,
  Red = 2,
  Green = 3,
  Yellow = 4,
  Blue = 5,
  Magenta = 6,
  Cyan = 7,
  Gray = 8,
  DarkGray = 9,
  LightRed = 10,
  LightGreen = 11,
  LightYellow = 12,
  LightBlue = 13,
  LightMagenta = 14,
  LightCyan = 15,
  White = 16,
  Rgb = 17,
  Indexed = 18,
} KatatuiColor;

typedef enum KatatuiConstraintKind {
  Length = 0,
  Percentage = 1,
  Min = 2,
  Max = 3,
  Fill = 4,
} KatatuiConstraintKind;

typedef enum KatatuiDirection {
  Horizontal = 0,
  Vertical = 1,
} KatatuiDirection;

typedef struct KatatuiBarChart KatatuiBarChart;

typedef struct KatatuiBlock KatatuiBlock;

typedef struct KatatuiClear KatatuiClear;

typedef struct KatatuiFrame KatatuiFrame;

typedef struct KatatuiGauge KatatuiGauge;

typedef struct KatatuiImageState KatatuiImageState;

typedef struct KatatuiLayout KatatuiLayout;

typedef struct KatatuiLineGauge KatatuiLineGauge;

typedef struct KatatuiList KatatuiList;

typedef struct KatatuiListState KatatuiListState;

typedef struct KatatuiParagraph KatatuiParagraph;

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

/**
 * `ratatui::init()` already enables raw mode + alternate screen.
 * This function exists for symmetry with the Kotlin API.
 */
bool katatui_terminal_init(struct KatatuiTerminal *terminal);

void katatui_terminal_restore(struct KatatuiTerminal *_terminal);

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

bool katatui_event_poll(uint64_t timeout_ms);

/**
 * Returns the ASCII value of a key press, or a sentinel value for special keys.
 * Special keys: Up=0xF1, Down=0xF2, Left=0xF3, Right=0xF4, Enter=0x0D, Esc=0x1B
 * Returns 0 for non-key events or unrecognised keys.
 */
uint8_t katatui_event_read_key_code(void);

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

struct KatatuiClear *katatui_clear_new(void);

void katatui_clear_free(struct KatatuiClear *clear);

struct KatatuiGauge *katatui_gauge_new(void);

void katatui_gauge_free(struct KatatuiGauge *gauge);

void katatui_gauge_set_percent(struct KatatuiGauge *gauge, uint8_t percent);

void katatui_gauge_set_label(struct KatatuiGauge *gauge, const char *label);

void katatui_gauge_set_style(struct KatatuiGauge *gauge, struct KatatuiStyle style);

void katatui_gauge_set_gauge_style(struct KatatuiGauge *gauge, struct KatatuiStyle style);

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

struct KatatuiParagraph *katatui_paragraph_new(const char *text);

void katatui_paragraph_free(struct KatatuiParagraph *para);

void katatui_paragraph_set_style(struct KatatuiParagraph *para, struct KatatuiStyle style);

void katatui_paragraph_set_text(struct KatatuiParagraph *para, const char *text);

void katatui_paragraph_set_wrap(struct KatatuiParagraph *para, bool wrap);

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
