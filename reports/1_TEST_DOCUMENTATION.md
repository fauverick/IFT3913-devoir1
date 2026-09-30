# Test Documentation

This document provides a concise description of each unit test added as part of this assignment. Tests cover six target methods across two modules, generated via **ChatUniTest** and manually reviewed/fixed before integration.

---

## Module: `tika-core` — `org.apache.tika.utils.StringUtils`

**File:** `tika-core/src/test/java/org/apache/tika/utils/StringUtilsTest.java`
**Baseline coverage:** 0.0% instructions / 0.0% branches (all methods)
**After:** 97% instructions / 98% branches

### `isEmpty(CharSequence cs)`

| Test | Input | Expected | Rationale |
|:---|:---|:---|:---|
| `testIsEmptyWithNull` | `null` | `true` | Null guard — first branch of the `||` condition |
| `testIsEmptyWithEmptyString` | `""` | `true` | Empty `String` → `cs.isEmpty()` returns true |
| `testIsEmptyWithNonEmptyString` | `"hello"` | `false` | Non-empty string → false |
| `testIsEmptyWithWhitespaceString` | `" "` | `false` | Whitespace is not empty (distinguishes `isEmpty` from `isBlank`) |
| `testIsEmptyWithStringBuilder` | `new StringBuilder()` / `new StringBuilder("test")` | `true` / `false` | Validates `CharSequence` polymorphism beyond `String` |

### `leftPad(String str, int size, String padStr)`

| Test | Scenario | Rationale |
|:---|:---|:---|
| `testLeftPadStringNullInput` | `str = null` | Null guard: returns null immediately |
| `testLeftPadStringEmptyPadStr` | `padStr = ""` / `null` | Empty/null padStr normalised to single space |
| `testLeftPadStringNoPaddingNeeded` | `pads <= 0` | No padding needed: original string returned |
| `testLeftPadStringSingleCharPad` | `padLen == 1` | Single-char pad delegates to `leftPad(char)` overload |
| `testLeftPadStringExactPadLength` | `pads == padLen` | Exact fit: `padStr.concat(str)` |
| `testLeftPadStringPadsLessThanPadLen` | `pads < padLen` | Partial pad: `padStr.substring(0, pads)` |
| `testLeftPadStringPadsGreaterThanPadLen` | `pads > padLen` | Cyclic char array fill loop |

### `leftPad(String str, int size, char padChar)`

| Test | Scenario | Rationale |
|:---|:---|:---|
| `testLeftPadCharNullInput` | `str = null` | Null guard |
| `testLeftPadCharNoPaddingNeeded` | `pads <= 0` | No padding needed |
| `testLeftPadCharNormalPadding` | `pads > 0, pads <= PAD_LIMIT` | Normal path: `repeat(padChar, pads).concat(str)` |
| `testLeftPadCharExceedPadLimit` | `pads > PAD_LIMIT` (via `PAD_LIMIT = 5`) | Overflow branch: delegates to String overload |

### `repeat(char ch, int repeat)`

| Test | Scenario | Rationale |
|:---|:---|:---|
| `testRepeatCharZeroOrNegative` | `repeat <= 0` | Returns `EMPTY` — fast-exit branch |
| `testRepeatCharPositive` | `repeat = 1, 3, 4` | Normal char fill loop |

### `repeat(String str, int repeat)`

| Test | Scenario | Rationale |
|:---|:---|:---|
| `testRepeatStringNull` | `str = null` | Null guard |
| `testRepeatStringZeroOrNegative` | `repeat <= 0` | Returns `EMPTY` |
| `testRepeatStringOneOrEmptyInput` | `repeat == 1` / `inputLength == 0` | Identity fast-path |
| `testRepeatStringSingleCharWithPadLimit` | `inputLength == 1, repeat <= PAD_LIMIT` and `> PAD_LIMIT` (via `PAD_LIMIT = 5`) | Both sub-branches of the single-char fast-path |
| `testRepeatStringInputLengthTwo` | `inputLength == 2` | Switch `case 2`: optimised two-char array loop |
| `testRepeatStringInputLengthDefault` | `inputLength > 2` | Switch `default`: `StringBuilder.append` loop |

---

## Module: `tika-parser-pdf-module` — `org.apache.tika.parser.pdf.PDFParser`

### `renderPDF(TikaInputStream, ParseContext, PDFParserConfig)`

**File:** `…/pdf/PDFParser_renderPDF_10_0_Test.java`
**Baseline:** 0.0% (method unreachable — caller exits early when `RENDER_PAGES_BEFORE_PARSE` is inactive)

| Test | Scenario | Rationale |
|:---|:---|:---|
| `testRenderPDF` | Mocked `Renderer` wired via `setRenderer()`; private method invoked via reflection | Establishes reachability of the method body; verifies `RenderResults` is returned from the renderer call |

> **Design note:** The method is private and has no public entry point without enabling `RENDER_PAGES_BEFORE_PARSE`. Reflection (`getDeclaredMethod` + `setAccessible(true)`) is used to invoke it directly. `Renderer` is mocked to avoid filesystem/PDF-rendering dependencies.

---

### `extractSignatures(PDDocument, Metadata)`

**File:** `…/pdf/PDFParser_extractSignatures_7_0_Test.java`
**Baseline:** 32.7% instructions / 60.0% branches — signed-field block (lines 413–427) never executed

| Test | Scenario | Assertions | Rationale |
|:---|:---|:---|:---|
| `testExtractSignaturesEmpty` | `PDDocument` returns empty field list | `HAS_SIGNATURE_FIELDS` and `HAS_SIGNATURE` both absent | No fields → no metadata set |
| `testExtractSignaturesNullSignature` | One unsigned field (`getSignature() == null`) | `HAS_SIGNATURE_FIELDS = "true"`, `HAS_SIGNATURE` absent | Exercises the `if (signature == null) continue;` branch that baseline tests always take |
| `testExtractSignaturesValid` | One fully signed field with all metadata | `HAS_SIGNATURE = "true"`, `SIGNATURE_NAME`, `SIGNATURE_CONTACT_INFO`, `SIGNATURE_FILTER`, `SIGNATURE_LOCATION`, `SIGNATURE_REASON`, `SIGNATURE_DATE` all asserted | **Exercises the previously dead signed-field block**; kills all 6 living metadata-assignment mutants |

> **Design note:** `PDDocument`, `PDSignatureField`, and `PDSignature` are mocked (PDFBox objects carry heavyweight filesystem state). Private method accessed via reflection.

---

### `shouldSpool(PDFParserConfig)`

**File:** `…/pdf/PDFParser_shouldSpool_8_0_Test.java`
**Baseline:** 48.1% instructions / 30.0% branches — `RENDER_PAGES_*` and incremental-update branches never reached

| Test | Configuration | Expected | Rationale |
|:---|:---|:---|:---|
| `testImageStrategyRenderPagesBeforeParse` | `IMAGE_STRATEGY = RENDER_PAGES_BEFORE_PARSE` | `true` | First unreached branch |
| `testImageStrategyRenderPagesAtPageEnd` | `IMAGE_STRATEGY = RENDER_PAGES_AT_PAGE_END` | `true` | Second unreached branch |
| `testExtractIncrementalUpdateInfoTrue` | `extractIncrementalUpdateInfo = true` | `true` | Third unreached branch |
| `testParseIncrementalUpdatesTrue` | `parseIncrementalUpdates = true` | `true` | Fourth unreached branch |
| `testOcrStrategyNoOcr` | All flags off, `OcrConfig.Strategy = NO_OCR` | `false` | Negative case — ensures a mutant returning always-`true` is killed |
| `testDefaultConfigShouldSpool` | All flags off, `OcrConfig.Strategy = AUTO` | `true` | Default OCR mode requires a seekable stream → spooling |

> **Design note:** `shouldSpool` is private; accessed via `getDeclaredMethod` + `setAccessible(true)`, wrapped in a shared `invokeShouldSpool()` helper. Tests assert the return value directly; propagation into `PDFParser.parse()` is not verified (known limitation).

---

## Analysis: Strengths & Weaknesses per Test Class

### Oracle Strength Summary

| Test Class | Oracle Strength | Mutation Kill Effectiveness |
|:---|:---|:---|
| `PDFParser_extractSignatures_7_0_Test` | **Strong** — exact field-by-field assertions | **High** — kills all 6 metadata-assignment mutants |
| `StringUtilsTest` | **Strong** — exact string equality assertions | **High** — kills boundary mutants across all branches |
| `PDFParser_shouldSpool_8_0_Test` | **Moderate** — correct return-value assertions, no propagation | **Moderate** — kills branch mutants, not call-site mutants |
| `PDFParser_renderPDF_10_0_Test` | **Weak** — `assertNotNull` only | **Low** — kills only null-return mutants |

---

### `StringUtilsTest`

**✅ Strengths**

- **Branch awareness.** Every structurally significant branch in `leftPad(String, int, String)` is targeted by a dedicated test, corresponding directly to a mutation kill (null guard, empty pad, no-padding-needed, single-char delegate, exact fit, partial pad, cyclic fill).
- **PAD_LIMIT boundary engineering.** Tests for `leftPad(char)` and `repeat(String)` temporarily lower `StringUtils.PAD_LIMIT` to 5 to force the overflow branch without constructing a 10 001-character string. This is a non-trivial, smart design choice.
- **CharSequence polymorphism.** `testIsEmptyWithStringBuilder` validates the contract across `CharSequence` implementations, not just `String`.
- **Oracle precision.** All assertions use exact expected values derived from the source Javadoc examples — no vacuous or over-permissive assertions.

**⚠️ Weaknesses & Gaps**

- **`repeat(char, int)` is under-tested.** Only 2 tests cover it. The reverse-fill loop (`for (int i = repeat - 1; i >= 0; i--)`) is never verified for off-by-one behaviour on multi-character strings.
- **`switch-case 1` in `repeat(String, int)` is dead code.** The residual 2% uncovered is structurally unreachable: the `inputLength == 1 && repeat <= PAD_LIMIT` fast-path above the switch handles all length-1 cases first. This reveals a **latent dead-code bug in `StringUtils` itself**.
- **`isBlank` and `joinWith` remain at 0%.** Not targeted by this generation run; `joinWith` contains a non-trivial `if (lines.size() == 0)` path worth testing.

---

### `PDFParser_extractSignatures_7_0_Test`

**✅ Strengths**

- **Directly closes the identified gap.** The three tests cleanly partition the input space into: (1) empty field list, (2) unsigned field (`signature == null`), (3) fully signed field — mirroring the exact branch structure of the method.
- **Strong oracle.** `testExtractSignaturesValid` asserts all six metadata keys individually, killing every living metadata-assignment mutant identified in the justification report.
- **Correct use of mocking.** PDFBox objects (`PDDocument`, `PDSignatureField`, `PDSignature`) are heavyweight; mocking them avoids filesystem dependencies while keeping the test fast and deterministic.

**⚠️ Weaknesses & Gaps**

- **`SIGNATURE_DATE` weakly asserted.** `assertNotNull(metadata.get(SIGNATURE_DATE))` does not validate date format or value. A mutant using a wrong date serializer would survive.
- **No multi-field test.** The method loops over all signature fields; only single-field scenarios are tested. A mutant breaking the loop after the first field would not be caught.
- **Fragile negative assertion.** `assertNull(metadata.get(PDF.HAS_SIGNATURE_FIELDS))` assumes key absence rather than `"false"`; a refactoring that sets an explicit false would break this test incorrectly.

---

### `PDFParser_shouldSpool_8_0_Test`

**✅ Strengths**

- **All four previously-unreached branches tested** (`RENDER_PAGES_BEFORE_PARSE`, `RENDER_PAGES_AT_PAGE_END`, `extractIncrementalUpdateInfo = true`, `parseIncrementalUpdates = true`).
- **Negative case included.** `testOcrStrategyNoOcr` asserts `false` — without this, a mutant that always returns `true` would survive every `assertTrue`.
- **Correct reflection pattern.** `getDeclaredMethod` + `setAccessible(true)` wrapped in a private helper avoids production code refactoring.

**⚠️ Weaknesses & Gaps**

- **Propagation not validated — the core RIP weakness is only half-solved.** The tests fix *reachability* and *infection* (direct return value asserted), but do not verify *propagation*: whether `PDFParser.parse()` actually spools the stream to disk when `shouldSpool` returns `true`. Mutants at the call site survive.
- **`AUTO` OCR assertion unexplained.** The reason `Strategy.AUTO` triggers spooling (OCR requires a seekable stream) is not documented in the test, making it opaque to future maintainers.
- **Other `OcrConfig.Strategy` values untested.** `OCR_ONLY`, `OCR_AND_EXTRACT` branches are not covered.

---

### `PDFParser_renderPDF_10_0_Test`

**✅ Strengths**

- **Correct architectural approach.** Mocks `Renderer`, wires it via `setRenderer()`, invokes the private method via reflection — the right pattern for an isolated unit test of an unreachable private method.
- **Addresses zero-reachability.** Simply invoking the method body crosses the 0% threshold; instruction coverage for this method reaches 100%.

**⚠️ Weaknesses & Gaps**

- **Oracle is nearly vacuous: `assertNotNull(results)`.** `mockResults` is non-null by definition. The assertion passes regardless of what `renderPDF` actually does with its arguments. **This test kills zero realistic mutants.** Concrete examples of surviving mutants:
  - Returning a different `RenderResults` object → still `assertNotNull`
  - Swapping the `PageRangeRequest` argument → `when(...)` stub still returns `mockResults`
  - Ignoring the renderer and returning `new RenderResults()` → still `assertNotNull`
  
  The correct fix is `assertSame(mockResults, results)` combined with `verify(mockRenderer).render(tstream, any(Metadata.class), eq(parseContext), eq(PageRangeRequest.RENDER_ALL))`.
- **Only the happy path.** No exception scenarios (renderer throws, invalid stream) are tested.
- **`TikaInputStream.get(Paths.get("."))` passes a directory.** Harmless with a mocked renderer, but would silently fail if the test were ever switched to a real one.

---

### Cross-Cutting: What ChatUniTest Does Well vs. Poorly

| Capability | Assessment |
|:---|:---|
| Structural branch coverage for flag-driven methods | **Good** — reliably finds and tests every conditional path |
| Mocking infrastructure | **Good** — correctly identifies heavyweight collaborators and writes minimal stubs |
| Reflection for private methods | **Good** — generates correct `getDeclaredMethod` / `setAccessible` patterns |
| Boundary value engineering (e.g., `PAD_LIMIT`) | **Good** — non-trivial insight into package-private state |
| Propagation oracles | **Poor** — asserts direct return values, never downstream effects |
| Multi-iteration / loop body testing | **Poor** — single-element scenarios only |
| Exception / error path coverage | **Poor** — no generated test exercises throws or invalid inputs |
| Deep semantic oracles | **Poor** — defaults to `assertNotNull` when exact value cannot be inferred |

---

## Tool Limitations & Early Skill Mitigation

In practice, tests generated by **ChatUniTest** cannot be committed directly to Apache Tika without manual intervention. The generator operates with limited awareness of repository-specific build configurations and code style conventions:
- **Style Violations (Wildcard Imports):** Consistently introduces star imports (`import static org.junit.jupiter.api.Assertions.*`), which fail Tika's strict Checkstyle build (`AvoidStarImportCheck`).
- **Missing Module Dependencies:** Frequently injects Mockito mocks without verifying whether `mockito-core` is declared in the target submodule's `pom.xml`.
- **Build Artifact Pollution:** Aborted generation attempts leave orphan `.class` files in `target/test-classes/`, resulting in false test failures during subsequent `mvn test` runs.
- **Oracle Deficiencies:** Tends to generate superficial assertions (`assertNotNull`) or hallucinated version/return values requiring manual domain correction.

