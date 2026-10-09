# Justification of Selected Methods: Coverage Gaps & Living Mutants

This report documents why the selected methods in `tika-core` and `tika-parser-pdf-module` were chosen for test coverage and mutation testing improvements. The justification is based on baseline JaCoCo coverage metrics and the **RIP model** (Reachability, Infection, Propagation) of mutation testing.

---

## 1. Summary Matrix

| Module | Target Class | Target Method | Baseline Coverage (JaCoCo) | Mutation Testing Justification (Living Mutants) |
| :--- | :--- | :--- | :--- | :--- |
| **`tika-core`** | `StringUtils` | `isEmpty` | **0.0%** (0/9 inst, 0/4 branches) | **Zero reachability**: Method is never invoked by any unit test. All mutants survive unexecuted. |
| **`tika-core`** | `StringUtils` | `leftPad` *(both overloads)* | **0.0%** (0/109 inst, 0/22 branches) | **Zero reachability**: Neither overload is invoked. All boundary, padding, and null-check mutants survive unexecuted. |
| **`tika-core`** | `StringUtils` | `repeat` *(both overloads)* | **0.0%** (0/122 inst, 0/23 branches) | **Zero reachability**: Neither overload is invoked. All fast-path and loop mutants survive unexecuted. |
| **`tika-parser-pdf`** | `PDFParser` | `renderPDF` | **0.0%** (0/21 inst, 0/3 lines) | **Unreachable caller**: Only invoked by `renderPagesBeforeParse`, which exits immediately when `RENDER_PAGES_BEFORE_PARSE` is inactive (never enabled in tests). |
| **`tika-parser-pdf`** | `PDFParser` | `extractSignatures` | **Partial** (32.7% inst, 60.0% branches) | **Missing branch**: Existing tests only cover unsigned fields (`signature == null`). The digital signature metadata extraction block (lines 413–427) and `HAS_SIGNATURE` flag never execute. |
| **`tika-parser-pdf`** | `PDFParser` | `shouldSpool` | **Partial** (48.1% inst, 30.0% branches) | **Uncovered branches & weak oracle**: `RENDER_PAGES_*` strategy branches are never hit. Furthermore, spooling decisions do not affect parsed text assertions, so mutants flipping return values survive. |

---

## 2. Module: `tika-core` -> Class: `StringUtils` 

This class originally has 15% code coverage:

![StringUtils Baseline Code Coverage](../media/StringUtils_before.png)

No unit test suite exists for `StringUtils` in `tika-core` (`StringUtilsTest` is absent in `tika-core/src/test/java`). Across the entire module test suite, the class is only referenced to access the constant `StringUtils.EMPTY` in `FilenameUtilsTest`.

* **Target Methods:**
  * `isEmpty(CharSequence cs)`: **0.0%** coverage (0/9 instructions, 0/4 branches).
  * `leftPad(String, int, String)` and `leftPad(String, int, char)`: **0.0%** coverage (0/109 instructions, 0/22 branches).
  * `repeat(char, int)` and `repeat(String, int)`: **0.0%** coverage (0/122 instructions, 0/23 branches).
* **Mutation Justification:**
  * Under the RIP model, a mutant must first be **reached** to be killed. Because none of these methods are invoked in tests, reachability is strictly **0%**.
  * **100% of mutants survive** across all three methods without test execution.

---

## 3. Module: `tika-parser-pdf-module` -> Class: `PDFParser`

This class originally has 81% code coverage:

![PDFParser Baseline Code Coverage](../media/PDFParser_before.png)

### 3.1. `renderPDF(TikaInputStream, ParseContext, PDFParserConfig)`
* **Coverage:** **0.0%** (0/21 instructions, 0/3 lines).
* **Call Path & Justification:** Private helper method called solely within `renderPagesBeforeParse`. Its caller contains an early exit guard:
  ```java
  if (config.getImageStrategy() != PDFParserConfig.IMAGE_STRATEGY.RENDER_PAGES_BEFORE_PARSE) {
      return;
  }
  ```
  Because no test in the baseline test suite enables `RENDER_PAGES_BEFORE_PARSE`, the call to `renderPDF` is never reached. All mutants survive trivially.

### 3.2. `extractSignatures(PDDocument, Metadata)`
* **Coverage:** **32.7%** instructions (32/98), **60.0%** branches (6/10), **50.0%** lines (11/22).
* **Branch Gap:** Baseline tests (`testPDF_acroform3.pdf` and `testPDF_sigflags.pdf`) only evaluate PDF documents containing unsigned signature fields, where `sigField.getSignature() == null`.
* **Living Mutants:**
  * The loop condition `if (signature == null) continue;` is always taken in baseline runs.
  * The block extracting digital signature metadata (lines 413–427: `SIGNATURE_NAME`, `SIGNATURE_DATE`, `SIGNATURE_CONTACT_INFO`, `SIGNATURE_FILTER`, `SIGNATURE_LOCATION`, `SIGNATURE_REASON`) and setting `HAS_SIGNATURE = true` is **never executed**.
  * Any mutant modifying or deleting these metadata mappings survives because no existing test exercises signed PDFs.

### 3.3. `shouldSpool(PDFParserConfig)`
* **Coverage:** **48.1%** instructions (13/27), **30.0%** branches (3/10), **44.4%** lines (4/9).
* **Living Mutants & Weak Propagation:**
  * **Unreached branches:** Checks for `IMAGE_STRATEGY.RENDER_PAGES_BEFORE_PARSE` and `IMAGE_STRATEGY.RENDER_PAGES_AT_PAGE_END` are never enabled in tests, leaving branch conditions unexecuted.
  * **Weak test oracle (lack of propagation):** `shouldSpool` dictates whether an input stream is buffered to a temporary disk file. Since test oracles only assert extracted document text and standard metadata—which remain identical whether spooled to disk or parsed from stream—mutants that invert the boolean return values survive without failing any assertions.
