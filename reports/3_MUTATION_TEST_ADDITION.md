# Manual Mutation Test Additions Report

This document details the unit tests added or enhanced manually to address surviving mutants identified during mutation testing with **PITest** on `PDFParser` and `StringUtils`.

---

## 1. Summary of Mutation Improvements

| Module | Target Class | Baseline Score | Score with ChatUniTest | Score after Manual Additions | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`tika-parser-pdf`** | `PDFParser` | **0%** | **73.9%** (17 / 23) | **100%** (32 / 32 covered killed) | **All surviving mutants killed (0 survive)** |
| **`tika-core`** | `StringUtils` | **0%** | **68.0%** (71 / 104) | **76.0%** (79 / 104 killed, 87% test strength) | **All killable mutants killed (12 equivalent)** |

---

## 2. Test Cases Added / Enhanced for `PDFParser`

### Test 1: `testRenderPDF`
- **Class:** `org.apache.tika.parser.pdf.PDFParser_renderPDF_10_0_Test`
- **Target Mutant:** Line 486 (`VoidMethodCallMutator` — removing call to `metadata.set(TikaCoreProperties.TYPE, ...)`).
- **Test Intent:** Verify that `renderPDF` explicitly sets the document media type (`application/pdf`) on the metadata passed to the `Renderer`.
- **Test Data Motivation:** Standard `TikaInputStream` and clean `ParseContext`. The test focuses on verifying internal metadata propagation to the renderer.
- **Oracle Explanation:** Uses Mockito's `ArgumentCaptor<Metadata>` to intercept the metadata object received by `mockRenderer.render(...)`. The oracle asserts `assertEquals("application/pdf", captor.getValue().get(TikaCoreProperties.TYPE))`. If line 486 is deleted or mutated, the media type is null and the test fails.

---

### Test 2: `testExtractSignaturesNullDate`
- **Class:** `org.apache.tika.parser.pdf.PDFParser_extractSignatures_7_0_Test`
- **Target Mutant:** Line 415 (`RemoveConditionalMutator` — replacing `if (date != null)` with `true`).
- **Test Intent:** Verify that signatures without a signing date (`null`) do not attempt to add a date property to metadata.
- **Test Data Motivation:** A mock signature with a valid name (`"Test Signer"`) and an explicitly `null` signing date (`null`).
- **Oracle Explanation:** Uses a Mockito spy on `Metadata` (`spy(new Metadata())`) and checks `verify(metadata, never()).add(eq(TikaCoreProperties.SIGNATURE_DATE), nullable(Calendar.class))`, alongside `assertNull(metadata.get(TikaCoreProperties.SIGNATURE_DATE))`. If the null guard is removed, `add(...)` is called with `null`, and Mockito verification fails.

---

### Tests 3–6: Branch Isolation for `shouldSpool`
- **Class:** `org.apache.tika.parser.pdf.PDFParser_shouldSpool_8_0_Test`
  - `testImageStrategyRenderPagesBeforeParse`
  - `testImageStrategyRenderPagesAtPageEnd`
  - `testExtractIncrementalUpdateInfoTrue`
  - `testParseIncrementalUpdatesTrue`
- **Target Mutants:** Lines 431, 432, 435, 436 (conditional removals and boolean flips in compound `if` statements).
- **Test Intent:** Ensure that each specific configuration flag independently causes `shouldSpool` to return `true`, without masking through the OCR fallback return path (`AUTO`).
- **Test Data Motivation:** Enables each target flag individually while explicitly setting `OcrConfig.Strategy.NO_OCR`. Setting `NO_OCR` forces the fallback path to return `false`, ensuring that `shouldSpool` returns `true` *strictly* due to the tested flag.
- **Oracle Explanation:** `assertTrue(invokeShouldSpool(config))`. If any of the tested conditional branches is removed or mutated to `false`, execution falls through to the `NO_OCR` branch which returns `false`, immediately failing `assertTrue`.

---

## 3. Test Cases Added / Enhanced for `StringUtils`

### Test 7: `testLeftPadStringNoPaddingNeeded`
- **Class:** `org.apache.tika.utils.StringUtilsTest`
- **Target Mutants:** Line 80 (`ConditionalsBoundaryMutator` changing `pads <= 0` to `< 0`, and `RemoveConditionalMutator`).
- **Test Intent:** Verify that when requested size is less than or equal to string length (`pads <= 0`), `leftPad` returns the exact original `String` reference without unnecessary allocations.
- **Test Data Motivation:** An allocated `new String("bat")` (length 3) with `size = 3` (exact boundary `pads == 0`) and multi-char pad string `"xyz"`.
- **Oracle Explanation:** `assertSame(str, StringUtils.leftPad(str, 3, "xyz"))`. If `pads <= 0` is mutated to `< 0`, execution falls through to substring and string concatenation (`"".concat(str)`), which allocates a new `String` object. `assertSame` checks reference identity and fails.

---

### Test 8: `testLeftPadCharNoPaddingNeeded`
- **Class:** `org.apache.tika.utils.StringUtilsTest`
- **Target Mutants:** Line 107 (`ConditionalsBoundaryMutator` changing `pads <= 0` to `< 0`, and `RemoveConditionalMutator`).
- **Test Intent:** Verify that `leftPad(str, size, padChar)` returns the original `String` reference when no padding is required.
- **Test Data Motivation:** `new String("abc")` with `size = 3` (boundary `pads == 0`) and character `'x'`.
- **Oracle Explanation:** `assertSame(str, StringUtils.leftPad(str, 3, 'x'))`. If `pads <= 0` is mutated to `< 0`, execution falls through to `repeat('x', 0).concat(str)`, creating a new `String` object. `assertSame` detects that the original instance was not returned and fails.

---

### Test 9: `testRepeatCharZeroOrNegative`
- **Class:** `org.apache.tika.utils.StringUtilsTest`
- **Target Mutant:** Line 139 (`ConditionalsBoundaryMutator` changing `repeat <= 0` to `< 0`).
- **Test Intent:** Verify that repeating a character 0 times returns the cached constant `StringUtils.EMPTY` rather than allocating an empty `char[0]` buffer.
- **Test Data Motivation:** Character `'a'` with `repeat = 0` (boundary value) and `repeat = -1`.
- **Oracle Explanation:** `assertSame(StringUtils.EMPTY, StringUtils.repeat('a', 0))`. Mutating `<=` to `<` causes `repeat = 0` to execute `new String(new char[0])`. `assertSame` verifies identity against `StringUtils.EMPTY` and fails.

---

### Test 10: `testRepeatStringZeroOrNegative`
- **Class:** `org.apache.tika.utils.StringUtilsTest`
- **Target Mutant:** Line 176 (`ConditionalsBoundaryMutator` changing `repeat <= 0` to `< 0`).
- **Test Intent:** Verify that repeating a string 0 times returns `StringUtils.EMPTY` directly without instantiating a `StringBuilder`.
- **Test Data Motivation:** String `"abc"` with `repeat = 0` (boundary) and `repeat = -1`.
- **Oracle Explanation:** `assertSame(StringUtils.EMPTY, StringUtils.repeat("abc", 0))`. Mutating `<=` to `<` causes `repeat = 0` to execute `new StringBuilder(0).toString()`. `assertSame` catches the newly allocated instance and fails.

---

### Test 11: `testRepeatStringOneOrEmptyInput`
- **Class:** `org.apache.tika.utils.StringUtilsTest`
- **Target Mutants:** Line 180 (`RemoveConditionalMutator` on `repeat == 1 || inputLength == 0`).
- **Test Intent:** Verify that repeating a string with `repeat = 1` or an empty string returns the input instance directly without copying into buffers.
- **Test Data Motivation:** `new String("abc")` with `repeat = 1`, and `new String("")` with `repeat = 5`.
- **Oracle Explanation:** `assertSame(str, StringUtils.repeat(str, 1))` and `assertSame(empty, StringUtils.repeat(empty, 5))`. Mutating either condition bypasses the early return and routes to switch loops, producing new String objects that fail `assertSame`.
---

