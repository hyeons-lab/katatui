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

typedef struct KatatuiBlock KatatuiBlock;

typedef struct KatatuiFrame KatatuiFrame;

typedef struct KatatuiLayout KatatuiLayout;

typedef struct KatatuiList KatatuiList;

typedef struct KatatuiListState KatatuiListState;

typedef struct KatatuiParagraph KatatuiParagraph;

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
                               struct KatatuiListState *_state);

bool katatui_event_poll(uint64_t timeout_ms);

/**
 * Returns the ASCII value of a key press, or a sentinel value for special keys.
 * Special keys: Up=0xF1, Down=0xF2, Left=0xF3, Right=0xF4, Enter=0x0D, Esc=0x1B
 * Returns 0 for non-key events or unrecognised keys.
 */
uint8_t katatui_event_read_key_code(void);

struct KatatuiBlock *katatui_block_new(void);

void katatui_block_free(struct KatatuiBlock *block);

void katatui_block_set_title(struct KatatuiBlock *block, const char *title);

void katatui_block_set_borders(struct KatatuiBlock *block, uint32_t borders);

void katatui_block_set_style(struct KatatuiBlock *block, struct KatatuiStyle style);

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

struct KatatuiList *katatui_list_new(void);

void katatui_list_free(struct KatatuiList *list);

void katatui_list_add_item(struct KatatuiList *list, const char *item);

struct KatatuiListState *katatui_list_state_new(void);

void katatui_list_state_free(struct KatatuiListState *state);

void katatui_list_state_select(struct KatatuiListState *state, int32_t index);

struct KatatuiParagraph *katatui_paragraph_new(const char *text);

void katatui_paragraph_free(struct KatatuiParagraph *para);

void katatui_paragraph_set_style(struct KatatuiParagraph *para, struct KatatuiStyle style);

void katatui_paragraph_set_wrap(struct KatatuiParagraph *para, bool wrap);
