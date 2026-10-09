# Mutation Testing Report

Analysis of mutation results using **PITest 1.17.1** (`STRONGER` mutators, Java 21) on `tika-core` and `tika-parser-pdf-module`.

---

## 1. Mutation Testing Configuration & Setup

PITest was integrated via Maven in `tika-parent/pom.xml` and the root `pom.xml` using the following profile:

- **Engine:** `org.pitest:pitest-maven:1.17.1` with `org.pitest:pitest-junit5-plugin:1.2.3`.
- **JUnit 5 / Platform Alignment:** `org.junit.platform:junit-platform-launcher:${junit6.version}` explicitly bound to ensure compatibility with Tika's test engine on Java 21.
- **Mutator Group:** `STRONGER` (Conditionals Boundary, Negate Conditionals, Increments, Invert Negatives, Math, Void Method Calls, Return Values, Switch, and Remove Conditionals).

---

## 2. Mutation Scores Overview

| Module | Target Class | Baseline Score (Original Tests) | Score with ChatUniTest | Surviving Mutants |
| :--- | :--- | :--- | :--- | :--- |
| **`tika-core`** | `StringUtils` | **0%** *(0 / 91 in target methods)* | **68%** (71 / 104) | **20** |
| **`tika-parser-pdf`** | `PDFParser` | **0%** *(0 / 23 in target methods)* | **73.9%** (17 / 23) | **6** |

---

## 3. Module: `tika-core` — `StringUtils`

Across `leftPad` and `repeat`, **20 mutants survived**. They fall into three main categories:

### A. Missing Zero-Boundaries (`<= 0` mutated to `< 0`)
* **Lines affected:** 80, 107 (`leftPad`), 139, 176 (`repeat`).
* **Why they survived:** ChatUniTest tested positive values (e.g. `size = 10`) and negative values (e.g. `size = -1`), but omitted testing exact equality with `0` (`size == str.length()` or `repeat = 0`). If the zero check is removed, the method falls through to a loop with 0 iterations, returning the exact same string.

### B. Missing Threshold Boundaries (`PAD_LIMIT`)
* **Lines affected:** 110 (`pads <= PAD_LIMIT` mutated to `<`), 183 (`outputLength > PAD_LIMIT` mutated to `>=`).
* **Why they survived:** The exact threshold value `PAD_LIMIT = 5` was never passed as an input. Tests only used smaller (<= 4) or larger (>= 6) values, so mutating the comparison boundary produced no observable difference.

### C. Equivalent Optimization Shortcuts
* **Lines affected:** 83 (`padLen == 1`), 87/89 (`pads == padLen`), 180 (`inputLength == 1`).
* **Why they survived:** These branches are performance shortcuts (e.g., direct concatenation instead of a loop, or delegating 1-character strings to a faster method). When removed, execution falls back to the general string buffer loop, which constructs the exact same output string.

---

## 4. Module: `tika-parser-pdf-module` — `PDFParser`

Across `renderPDF`, `extractSignatures`, and `shouldSpool`, **6 mutants survived**:

### A. Weak Test Oracle in `renderPDF`
* **Line affected:** 486 (`VoidMethodCallMutator` on `renderer.render(...)`).
* **Why it survived:** The generated test only asserted `assertNotNull(results)`. Because the mocked renderer returned a pre-canned non-null object, removing the actual call to `renderer.render()` went undetected.  
* **Fix needed:** Verify the mock interaction using `Mockito.verify(mockRenderer).render(...)`.

### B. Missing Null Field Scenario in `extractSignatures`
* **Line affected:** 415 (`if (sig.getName() != null)` replaced with `true`).
* **Why it survived:** The test data only provided signatures with a valid name (`"John Doe"`). No test exercised a signature where `sig.getName() == null` alongside other populated fields.

### C. Compound Condition Masking in `shouldSpool`
* **Lines affected:** 431, 432, 435, 436 (conditional removals in compound `if` statements).
* **Why they survived:** The method uses multiple fallback checks. When an individual sub-condition was removed or replaced, subsequent default rules evaluated to the same boolean output for the broad configurations tested.

---

## 5. Reproducing Results (Deterministic Commands)

PITest mutations and test executions are **100% deterministic** on unchanged source and bytecode. The exact same mutants and survival rates can be reproduced using these commands:

### A. `StringUtils` (`tika-core`)
* **Baseline (Original Tests Only):**
  ```bash
  mvn test-compile org.pitest:pitest-maven:mutationCoverage -pl tika-core \
    -DtargetClasses=org.apache.tika.utils.StringUtils \
    -DtargetTests=org.apache.tika.io.FilenameUtilsTest
  ```
* **With ChatUniTest (`StringUtilsTest`):**
  ```bash
  mvn test-compile org.pitest:pitest-maven:mutationCoverage -pl tika-core \
    -DtargetClasses=org.apache.tika.utils.StringUtils \
    -DtargetTests=org.apache.tika.utils.StringUtilsTest
  ```
  *(HTML report generated at `tika-core/target/pit-reports/index.html`)*

### B. `PDFParser` (`tika-parser-pdf-module`)
* **With ChatUniTest (`PDFParser_*_Test` on target methods):**
  ```bash
  mvn test-compile org.pitest:pitest-maven:mutationCoverage \
    -pl tika-parsers/tika-parsers-standard/tika-parsers-standard-modules/tika-parser-pdf-module \
    -DtargetClasses=org.apache.tika.parser.pdf.PDFParser \
    -DtargetTests='org.apache.tika.parser.pdf.PDFParser_*_Test' \
    -DtargetMethods=renderPDF,extractSignatures,shouldSpool
  ```
  *(HTML report generated at `tika-parsers/.../tika-parser-pdf-module/target/pit-reports/index.html`)*

