package com.hyeonslab.katatui.codegen

import com.hyeonslab.katatui.codegen.model.FunctionRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
    assertTrue("KatatuiBlock" in p.opaqueTypes)
    assertTrue("KatatuiLineGauge" in p.opaqueTypes)
    assertTrue("KatatuiBarChart" in p.opaqueTypes)
  }

  @Test
  fun `parse does not treat body structs as opaque`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    assertFalse("KatatuiRect" in p.opaqueTypes)
  }

  // --- parse(): structs ---

  @Test
  fun `parse populates body struct with correct fields`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val rect = assertNotNull(p.structs["KatatuiRect"])
    assertEquals(4, rect.fields.size)
    assertEquals("x", rect.fields[0].name)
    assertEquals("uint16_t", rect.fields[0].type)
    assertEquals("height", rect.fields[3].name)
  }

  // --- parse(): enums ---

  @Test
  fun `parse populates enum with variants`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val color = assertNotNull(p.enums["KatatuiColor"])
    assertEquals(2, color.variants.size)
    assertEquals("Reset", color.variants[0].name)
    assertEquals(0, color.variants[0].value)
    assertEquals("Blue", color.variants[1].name)
    assertEquals(5, color.variants[1].value)
  }

  // --- parse(): functions ---

  @Test
  fun `parse collects all katatui_ function declarations`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val names = p.functions.map { it.name }
    assertTrue("katatui_block_new" in names)
    assertTrue("katatui_block_free" in names)
    assertTrue("katatui_block_set_title" in names)
    assertTrue("katatui_line_gauge_new" in names)
    assertTrue("katatui_bar_chart_add_bar" in names)
  }

  @Test
  fun `parse extracts function parameters`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val setter = p.functions.first { it.name == "katatui_block_set_title" }
    assertEquals(2, setter.params.size)
    assertEquals("block", setter.params[0].name)
    assertEquals("title", setter.params[1].name)
  }

  // --- widgetGroups() ---

  @Test
  fun `widgetGroups converts multi-word CamelCase types to snake_case prefix`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val groups = p.widgetGroups()
    val names = groups.map { it.kotlinName }
    assertTrue("LineGauge" in names, "Expected LineGauge but got $names")
    assertTrue("BarChart" in names, "Expected BarChart but got $names")
  }

  @Test
  fun `widgetGroups excludes entries in the exclusion list`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val groups = p.widgetGroups()
    val names = groups.map { it.kotlinName }
    assertFalse("Terminal" in names, "Terminal must be excluded")
    assertFalse("Frame" in names, "Frame must be excluded")
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
    assertTrue(
      listStateGroup == null,
      "list_state group must be absent (excluded via exclusion list)",
    )
  }

  @Test
  fun `widgetGroups returns correct cName and kotlinName for single-word widget`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val block = p.widgetGroups().find { it.kotlinName == "Block" }
    assertNotNull(block)
    assertEquals("KatatuiBlock", block.cName)
  }

  @Test
  fun `widgetGroups assigns constructor and setter roles correctly`() {
    val p = HeaderParser().apply { parse(sampleHeader) }
    val block = p.widgetGroups().find { it.kotlinName == "Block" }!!
    assertNotNull(block.constructor)
    assertEquals(FunctionRole.Constructor, block.constructor!!.role)
    assertEquals(1, block.setters.size)
    assertEquals("katatui_block_set_title", block.setters[0].name)
  }
}
