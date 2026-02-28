package com.hyeonslab.katatui.codegen

import com.hyeonslab.katatui.codegen.model.FunctionRole
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HeaderParserTest {
  // Minimal header that exercises all parser branches used by katatui.h
  private val sampleHeader =
    """
    |#include <stdint.h>
    |
    |typedef enum {
    |  Reset = 0,
    |  Blue = 5,
    |} KatatuiColor;
    |
    |typedef struct {
    |  uint16_t x;
    |  uint16_t y;
    |  uint16_t width;
    |  uint16_t height;
    |} KatatuiRect;
    |
    |typedef struct KatatuiBlock KatatuiBlock;
    |typedef struct KatatuiLineGauge KatatuiLineGauge;
    |typedef struct KatatuiBarChart KatatuiBarChart;
    |typedef struct KatatuiTerminal KatatuiTerminal;
    |typedef struct KatatuiFrame KatatuiFrame;
    |typedef struct KatatuiListState KatatuiListState;
    |
    |struct KatatuiBlock *katatui_block_new(void);
    |void katatui_block_free(struct KatatuiBlock *block);
    |void katatui_block_set_title(struct KatatuiBlock *block, const char *title);
    |struct KatatuiLineGauge *katatui_line_gauge_new(void);
    |void katatui_line_gauge_free(struct KatatuiLineGauge *gauge);
    |void katatui_line_gauge_set_percent(struct KatatuiLineGauge *gauge, uint8_t percent);
    |struct KatatuiBarChart *katatui_bar_chart_new(void);
    |void katatui_bar_chart_free(struct KatatuiBarChart *chart);
    |void katatui_bar_chart_add_bar(struct KatatuiBarChart *chart, uint64_t value);
    |struct KatatuiFrame *katatui_frame_new(void);
    |struct KatatuiListState *katatui_list_state_new(void);
    |void katatui_list_state_free(struct KatatuiListState *s);
    """
      .trimMargin()
      .lines()

  // --- parse(): opaque types ---

  @Test
  fun `parse collects opaque struct typedefs`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    p.opaqueTypes shouldContain "KatatuiBlock"
    p.opaqueTypes shouldContain "KatatuiLineGauge"
    p.opaqueTypes shouldContain "KatatuiBarChart"
  }

  @Test
  fun `parse does not treat body structs as opaque`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    p.opaqueTypes shouldNotContain "KatatuiRect"
  }

  // --- parse(): structs ---

  @Test
  fun `parse populates body struct with correct fields`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val rect = checkNotNull(p.structs["KatatuiRect"])
    rect.fields.size shouldBe 4
    rect.fields[0].name shouldBe "x"
    rect.fields[0].type shouldBe "uint16_t"
    rect.fields[3].name shouldBe "height"
  }

  // --- parse(): enums ---

  @Test
  fun `parse populates enum with variants`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val color = checkNotNull(p.enums["KatatuiColor"])
    color.variants.size shouldBe 2
    color.variants[0].name shouldBe "Reset"
    color.variants[0].value shouldBe 0
    color.variants[1].name shouldBe "Blue"
    color.variants[1].value shouldBe 5
  }

  // --- parse(): functions ---

  @Test
  fun `parse collects all katatui_ function declarations`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val names = p.functions.map { it.name }
    names shouldContain "katatui_block_new"
    names shouldContain "katatui_block_free"
    names shouldContain "katatui_block_set_title"
    names shouldContain "katatui_line_gauge_new"
    names shouldContain "katatui_bar_chart_add_bar"
  }

  @Test
  fun `parse extracts function parameters`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val setter = p.functions.first { it.name == "katatui_block_set_title" }
    setter.params.size shouldBe 2
    setter.params[0].name shouldBe "block"
    setter.params[1].name shouldBe "title"
  }

  // --- widgetGroups() ---

  @Test
  fun `widgetGroups converts multi-word CamelCase types to snake_case prefix`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val names = p.widgetGroups().map { it.kotlinName }
    names shouldContain "LineGauge"
    names shouldContain "BarChart"
  }

  @Test
  fun `widgetGroups excludes entries in the exclusion list`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val names = p.widgetGroups().map { it.kotlinName }
    names shouldNotContain "Terminal"
    names shouldNotContain "Frame"
  }

  @Test
  fun `widgetGroups excludes functions whose rest starts with state`() {
    // katatui_list_state_new should be skipped even though KatatuiList* doesn't exist
    // because any fn body starting with <prefix>_state is filtered
    val p = HeaderParser().apply { parse(sampleHeader) }
    val groups = p.widgetGroups()
    // KatatuiListState is in opaqueTypes so it would be a candidate if not excluded
    val listStateGroup = groups.find { it.cName == "KatatuiListState" }
    // Must be excluded via the explicit exclusion list ("list_state" maps to excluded)
    // regardless of whether list_state_new was in functions
    listStateGroup shouldBe null
  }

  @Test
  fun `widgetGroups returns correct cName and kotlinName for single-word widget`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val block = checkNotNull(p.widgetGroups().find { it.kotlinName == "Block" })
    block.cName shouldBe "KatatuiBlock"
  }

  @Test
  fun `widgetGroups assigns constructor and setter roles correctly`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val block = checkNotNull(p.widgetGroups().find { it.kotlinName == "Block" })
    val constructor = checkNotNull(block.constructor)
    constructor.role shouldBe FunctionRole.Constructor
    block.setters.size shouldBe 1
    block.setters[0].name shouldBe "katatui_block_set_title"
  }
}
