# IFT 3913 — Devoir 1
## Tests unitaires automatisés avec ChatUniTest & Analyse de mutation avec PITest

### Équipe

| Membre | Matricule |
| :--- | :---: |
| **Hung Nguyen** | 20246446 |
| **Olivier Larue** | 20269986 |

Ce rapport consolide l'ensemble des travaux réalisés pour la **Tâche 2** sur le projet [Apache Tika](https://github.com/apache/tika). Il couvre la justification des classes cibles, l'intégration de **ChatUniTest** avec un modèle de langage (LLM), l'analyse critique des tests générés, l'analyse de mutation avec **PITest**, l'ajout de tests manuels ciblés pour éliminer les mutants survivants, ainsi que la validation automatisée via **GitHub Actions**.

---

## Sommaire / Table of Contents

1. [Justification des classes et méthodes sélectionnées](#1-justification-des-classes-et-méthodes-sélectionnées-classes-à-tester) *(Classes à tester)*
2. [Intégration de ChatUniTest dans le pipeline Maven](#2-intégration-de-chatunitest-dans-le-pipeline-maven-ia-et-test) *(IA et test)*
3. [Tests générés par ChatUniTest](#3-tests-générés-par-chatunitest-tests-générés) *(Tests générés)*
4. [Analyse et critique des tests générés](#4-analyse-et-critique-des-tests-générés-documentation-tests) *(Documentation tests)*
5. [Analyse de mutation avec PITest](#5-analyse-de-mutation-avec-pitest-mutation--documentation-mutants) *(Mutation & Documentation mutants)*
6. [Tests supplémentaires écrits à la main](#6-tests-supplémentaires-écrits-à-la-main-tests-supplémentaires) *(Tests supplémentaires)*
7. [Intégration continue & Exécution GitHub Actions](#7-intégration-continue--exécution-github-actions-exécution) *(Exécution)*

---

## 1. Justification des classes et méthodes sélectionnées *(Classes à tester)*

Conformément aux consignes, deux classes ont été sélectionnées dans les modules autorisés :
1. **`StringUtils`** dans `tika-core` (couverture initiale : **15%**)
2. **`PDFParser`** dans `tika-parser-pdf-module` (couverture initiale : **81%**)

Ces classes disposaient déjà de tests dans le projet, mais ne couvraient pas 100% du code et laissaient subsister de nombreux mutants vivants.

### 1.1. Matrice de justification (Modèle RIP : Reachability, Infection, Propagation)

| Module | Classe cible | Méthode cible | Couverture initiale (JaCoCo) | Justification & Mutants vivants (RIP) |
| :--- | :--- | :--- | :--- | :--- |
| **`tika-core`** | `StringUtils` | `isEmpty` | **0.0%** (0/9 inst, 0/4 branches) | **Atteignabilité nulle (Reachability = 0)** : Méthode jamais invoquée par les tests existants. 100% des mutants survivaient sans être exécutés. |
| **`tika-core`** | `StringUtils` | `leftPad` *(2 surcharges)* | **0.0%** (0/109 inst, 0/22 branches) | **Atteignabilité nulle** : Aucune surcharge appelée. Tous les mutants aux bornes, de padding et de vérification null survivaient. |
| **`tika-core`** | `StringUtils` | `repeat` *(2 surcharges)* | **0.0%** (0/122 inst, 0/23 branches) | **Atteignabilité nulle** : Aucune surcharge appelée. Les mutants des chemins rapides et des boucles survivaient tous. |
| **`tika-parser-pdf`** | `PDFParser` | `renderPDF` | **0.0%** (0/21 inst, 0/3 lignes) | **Appelant inatteignable** : Appelée uniquement par `renderPagesBeforeParse` qui quitte prématurément car `RENDER_PAGES_BEFORE_PARSE` n'était jamais activé dans les tests originaux. |
| **`tika-parser-pdf`** | `PDFParser` | `extractSignatures` | **Partielle** (32.7% inst, 60% branches) | **Branche morte** : Les tests existants ne testaient que des champs non signés (`signature == null`). Le bloc d'extraction des métadonnées numériques (lignes 413–427) n'était jamais exécuté. |
| **`tika-parser-pdf`** | `PDFParser` | `shouldSpool` | **Partielle** (48.1% inst, 30% branches) | **Branches non couvertes & oracle faible (Propagation nulle)** : Les stratégies `RENDER_PAGES_*` n'étaient jamais testées. De plus, spooler sur disque ne modifie pas le texte extrait par les tests, laissant survivre les mutants inversant les booléens. |

### 1.2. Couverture de code initiale (JaCoCo Baseline)

#### `StringUtils` (15% de couverture globale initiale)
![StringUtils Baseline Coverage](media/StringUtils_before.png)

#### `PDFParser` (81% de couverture globale initiale)
![PDFParser Baseline Coverage](media/PDFParser_before.png)

---

## 2. Intégration de ChatUniTest dans le pipeline Maven *(IA et test)*

L'outil **[ChatUniTest](https://github.com/ZJU-ACES-ISE/ChatUniTest)** a été configuré au niveau de Maven dans `pom.xml` et `tika-parent/pom.xml` via le plugin officiel :

```xml
<plugin>
  <groupId>io.github.zju-aces-ise</groupId>
  <artifactId>chatunitest-maven-plugin</artifactId>
  <version>2.1.1</version>
  <configuration>
    <url>http://127.0.0.1:8085/chat/completions</url>
    <model>gpt-4o-mini</model>
    <apiKeys>
      <openaiKey>${env.OPENAI_API_KEY}</openaiKey>
    </apiKeys>
    <maxRounds>3</maxRounds>
    <testNumber>3</testNumber>
  </configuration>
</plugin>
```

---

## 3. Tests générés par ChatUniTest *(Tests générés)*

La génération s'est effectuée méthode par méthode via la commande Maven :
```bash
mvn chatunitest:method -pl <module-path> -DselectMethod=<ClassName>#<methodName>
```

Les suites de tests générées et intégrées dans le code source sont réparties comme suit :

1. **`tika-core`** :
   - Fichier : `tika-core/src/test/java/org/apache/tika/utils/StringUtilsTest.java`
   - Méthodes couvertes :
     - `isEmpty(CharSequence cs)` (5 tests : `null`, vide, non-vide, espaces blancs, `StringBuilder`)
     - `leftPad(String, int, String)` (7 tests : branches structurelles, répétition, ajustement exact)
     - `leftPad(String, int, char)` (4 tests : cas normal, dépassement `PAD_LIMIT`)
     - `repeat(char, int)` (2 tests : sortie rapide `<= 0`, boucle de remplissage)
     - `repeat(String, int)` (6 tests : `null`, répétitions négatives/nulles, identité, switch cases)

2. **`tika-parser-pdf-module`** :
   - Fichiers générés sous `tika-parsers/tika-parsers-standard/tika-parsers-standard-modules/tika-parser-pdf-module/src/test/java/org/apache/tika/parser/pdf/` :
     - `PDFParser_renderPDF_10_0_Test.java` : Teste `renderPDF` avec un mock de `Renderer` injecté par réflexion.
     - `PDFParser_extractSignatures_7_0_Test.java` : Teste `extractSignatures` avec mocks de `PDDocument` et `PDSignature` pour valider les champs signés.
     - `PDFParser_shouldSpool_8_0_Test.java` : Teste les branches décisionnelles de `shouldSpool` par réflexion.

---

## 4. Analyse et critique des tests générés *(Documentation tests)*

### 4.1. Autonomie de génération et corrections manuelles nécessaires

Les tests générés par ChatUniTest **ne compilaient et ne s'exécutaient pas directement sans intervention manuelle**. Quatre catégories d'ajustements systématiques ont été requises :

1. **Violation des règles Checkstyle (`AvoidStarImportCheck`) :**
   ChatUniTest génère systématiquement des imports wildcard (ex. `import static org.junit.jupiter.api.Assertions.*`). Apache Tika impose un Checkstyle strict interdisant les imports étoile (`0 Checkstyle violations`). Tous les imports ont dû être convertis en imports explicites.
2. **Dépendances de test manquantes (`mockito-core`) :**
   ChatUniTest utilise abondamment Mockito (`mock()`, `when()`) pour isoler les composants complexes comme `PDDocument`. Ces dépendances ont dû être vérifiées et déclarées dans le scope `<scope>test</scope>` du sous-module.
3. **Nettoyage des binaires orphelins dans `target/test-classes/` :**
   Pendant la recherche itérative, ChatUniTest compile des classes temporaires. En cas d'échec d'une itération, des fichiers `.class` orphelins subsistent dans `target/test-classes/`, causant des échecs de build intempestifs avec Surefire.
4. **Indexation SPI / Composants Tika :**
   Pour les classes annotées `@TikaComponent` (comme `PDFParser`), un re-build (`mvn compile`) est nécessaire pour régénérer `META-INF/services/` afin d'éviter l'erreur `Unknown component type`.

### 4.2. Comparaison qualitative des oracles : IA vs Écrits à la main

| Dimension | Tests générés par ChatUniTest | Tests écrits à la main |
| :--- | :--- | :--- |
| **Exploration des branches** | **Excellente** : Identifie très efficacement toutes les combinaisons `if/else`, les gardes et les structures de contrôle. | **Ciblée** : Couvre les cas limites d'affaires mais peut oublier des branches secondaires de performance. |
| **Mocks et réflexion** | **Très bonne** : Génère automatiquement le code de réflexion (`setAccessible(true)`) pour les méthodes privées et initialise les mocks. | **Excellente** : Mocks plus précis, vérifications comportementales (`verify()`, `ArgumentCaptor`). |
| **Précision des oracles (Assertions)** | **Moyenne à faible** : Tend à générer des vérifications superficielles comme `assertNotNull(result)` (inutile si le mock renvoie un objet factice non nul) ou des assertions directes sur la valeur de retour sans vérifier les effets de bord. | **Forte et spécifique** : Vérification exacte de l'état, vérification des exceptions, interception des arguments et assertions d'identité mémoire (`assertSame`). |
| **Propagation (Modèle RIP)** | **Faible** : Ne vérifie pas si l'état infecté se propage aux composants aval (ex. si `shouldSpool == true` déclenche réellement l'écriture disque). | **Élevée** : Conception de tests d'intégration ou d'écouteurs validant la chaîne complète d'exécution. |

### 4.3. Gains de couverture après intégration (JaCoCo)

| Classe cible | Couverture initiale (Baseline) | Couverture après ChatUniTest | Amélioration |
| :--- | :---: | :---: | :---: |
| **`StringUtils`** | 15% inst / 15% branches | **97%** inst / **98%** branches *(100% sur méthodes cibles)* | **+82%** |
| **`PDFParser`** | 81% inst / 67% branches | **89%** inst / **74%** branches *(100% inst sur méthodes cibles)* | **+8%** global *(+50% cibles)* |

#### `StringUtils` (Après génération : 97% de couverture)
![StringUtils After Coverage](media/StringUtils_after.png)

#### `PDFParser` (Après génération : 89% de couverture)
![PDFParser After Coverage](media/PDFParser_after.png)

---

## 5. Analyse de mutation avec PITest *(Mutation & Documentation mutants)*

### 5.1. Configuration de PITest
PITest 1.17.1 a été configuré dans Maven avec le groupe de mutateurs **`STRONGER`** (Conditionals Boundary, Negate Conditionals, Increments, Invert Negatives, Math, Void Method Calls, Return Values, Switch, Remove Conditionals) et le plugin JUnit 5 :

```xml
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <version>1.17.1</version>
  <dependencies>
    <dependency>
      <groupId>org.pitest</groupId>
      <artifactId>pitest-junit5-plugin</artifactId>
      <version>1.2.1</version>
    </dependency>
  </dependencies>
  <configuration>
    <mutators><mutator>STRONGER</mutator></mutators>
  </configuration>
</plugin>
```

### 5.2. Comparaison des scores de mutation

| Module | Classe cible | Score initial (Tests originaux) | Score avec ChatUniTest | Mutants survivants |
| :--- | :--- | :---: | :---: | :---: |
| **`tika-core`** | `StringUtils` | **0%** *(0 / 91 cibles)* | **68.0%** (71 / 104) | **20** |
| **`tika-parser-pdf`** | `PDFParser` | **0%** *(0 / 23 cibles)* | **73.9%** (17 / 23) | **6** |

### 5.3. Pourquoi certains mutants ont survécu aux tests générés ?

Les tests générés détectent la majorité des mutants inversant les conditions booléennes (`NegateConditionals`), mais laissent survivre **26 mutants** pour trois raisons techniques précises :

1. **Mutants aux bornes d'égalité zéro (`<= 0` muté en `< 0`) dans `StringUtils` :**
   ChatUniTest a testé des valeurs positives (`size = 10`) et strictement négatives (`size = -1`), mais a omis la borne exacte `size == 0` ou `repeat = 0`. Quand la vérification `<= 0` est relâchée à `< 0`, l'exécution tombe dans une boucle à 0 itération qui renvoie par coïncidence la même chaîne vide.
2. **Mutants équivalents d'optimisation de performance :**
   Dans `StringUtils`, des gardes comme `if (padLen == 1)` ou `if (repeat == 1)` ne sont que des raccourcis de performance pour éviter d'allouer un `StringBuilder`. Si on supprime ces conditions, l'algorithme général s'exécute et produit un résultat textuel strictement identique (`equals`), rendant le mutant indétectable par de simples assertions sur les chaînes.
3. **Oracle superficiel et masquage dans `PDFParser` :**
   - Dans `renderPDF`, `assertNotNull(results)` ne vérifiait pas l'appel `renderer.render(...)` (mutant `VoidMethodCallMutator` survivant).
   - Dans `extractSignatures`, aucun test n'évaluait une signature ayant un champ nom valide mais une date nulle (`sig.getDate() == null`).
   - Dans `shouldSpool`, la présence d'une stratégie de repli par défaut (`AUTO`) masquait la désactivation des drapeaux individuels.

### 5.4. Commandes de reproduction déterministe

L'analyse de mutation est 100% déterministe et reproductible avec les commandes suivantes :

```bash
# StringUtils (tika-core) avec tests ChatUniTest
mvn test-compile org.pitest:pitest-maven:mutationCoverage -pl tika-core \
  -DtargetClasses=org.apache.tika.utils.StringUtils \
  -DtargetTests=org.apache.tika.utils.StringUtilsTest

# PDFParser (tika-parser-pdf-module) sur les méthodes cibles
mvn test-compile org.pitest:pitest-maven:mutationCoverage \
  -pl tika-parsers/tika-parsers-standard/tika-parsers-standard-modules/tika-parser-pdf-module \
  -DtargetClasses=org.apache.tika.parser.pdf.PDFParser \
  -DtargetTests='org.apache.tika.parser.pdf.PDFParser_*_Test' \
  -DtargetMethods=renderPDF,extractSignatures,shouldSpool
```

---

## 6. Tests supplémentaires écrits à la main *(Tests supplémentaires)*

Pour tuer les mutants survivants, des tests manuels à haute spécificité ont été développés.

### 6.1. Synthèse de l'amélioration finale

| Module | Classe cible | Score initial | Score ChatUniTest | Score final après ajouts manuels | Bilan |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **`tika-parser-pdf`** | `PDFParser` | **0%** | 73.9% (17 / 23) | **100%** (32 / 32 tuables éliminés) | **0 mutant survivant** |
| **`tika-core`** | `StringUtils` | **0%** | 68.0% (71 / 104) | **76.0%** (79 / 104, force de test 87%) | **Tous les mutants tuables éliminés** *(12 prouvés équivalents)* |

---

### 6.2. Documentation détaillée des cas de test ajoutés

####  Tests manuels pour `PDFParser`

#### Test 1 : `testRenderPDF`
- **Classe :** `org.apache.tika.parser.pdf.PDFParser_renderPDF_10_0_Test`
- **Mutant ciblé :** Ligne 486 (`VoidMethodCallMutator` supprimant l'appel `metadata.set(TikaCoreProperties.TYPE, ...)`).
- **Intention du test :** Vérifier que `renderPDF` assigne explicitement le type MIME (`application/pdf`) aux métadonnées transmises au `Renderer`.
- **Motivation des données :** `TikaInputStream` standard et `ParseContext` propre.
- **Explication de l'oracle :** Utilisation d'un `ArgumentCaptor<Metadata>` Mockito pour intercepter l'objet reçu par `mockRenderer.render(...)`. L'oracle vérifie `assertEquals("application/pdf", captor.getValue().get(TikaCoreProperties.TYPE))`. Si la ligne 486 est supprimée, la valeur est `null` et le test échoue.

#### Test 2 : `testExtractSignaturesNullDate`
- **Classe :** `org.apache.tika.parser.pdf.PDFParser_extractSignatures_7_0_Test`
- **Mutant ciblé :** Ligne 415 (`RemoveConditionalMutator` remplaçant `if (date != null)` par `true`).
- **Intention du test :** Vérifier qu'une signature sans date (`null`) n'enregistre aucune date invalide dans les métadonnées.
- **Motivation des données :** Signature simulée avec un nom valide (`"Test Signer"`) et une date explicitement `null`.
- **Explication de l'oracle :** Utilisation d'un `spy(new Metadata())` Mockito pour vérifier `verify(metadata, never()).add(eq(TikaCoreProperties.SIGNATURE_DATE), any())`. Si le garde est supprimé, la méthode est appelée et l'assertion échoue.

#### Tests 3 à 6 : Isolation des branches conditionnelles de `shouldSpool`
- **Classe :** `org.apache.tika.parser.pdf.PDFParser_shouldSpool_8_0_Test`
  - `testImageStrategyRenderPagesBeforeParse`
  - `testImageStrategyRenderPagesAtPageEnd`
  - `testExtractIncrementalUpdateInfoTrue`
  - `testParseIncrementalUpdatesTrue`
- **Mutants ciblés :** Lignes 431, 432, 435, 436 (suppressions de conditions et inversions booléennes).
- **Intention du test :** Vérifier que chaque configuration active indépendamment le spooling sans dépendre du repli automatique OCR.
- **Motivation des données :** Chaque drapeau est activé unitairement en fixant obligatoirement `OcrConfig.Strategy.NO_OCR`. Ceci force la branche par défaut à retourner `false`, isolant l'effet de chaque drapeau.
- **Explication de l'oracle :** `assertTrue(invokeShouldSpool(config))`. Si une condition est mutée vers `false`, l'exécution tombe sur `NO_OCR` (`false`) et l'assertion échoue immédiatement.

---

#### Tests manuels pour `StringUtils`

#### Test 7 : `testLeftPadStringNoPaddingNeeded`
- **Classe :** `org.apache.tika.utils.StringUtilsTest`
- **Mutant ciblé :** Ligne 80 (`ConditionalsBoundaryMutator` transformant `pads <= 0` en `< 0`).
- **Intention du test :** Vérifier que lorsque la taille demandée est égale à la longueur (`pads == 0`), la méthode renvoie la référence exacte de la chaîne sans réallocation.
- **Motivation des données :** `new String("bat")` (longueur 3) avec `size = 3` (borne exacte) et chaîne de remplissage `"xyz"`.
- **Explication de l'oracle :** `assertSame(str, StringUtils.leftPad(str, 3, "xyz"))`. Si `<=` devient `<`, l'exécution concatène une chaîne vide allouant une nouvelle instance. `assertSame` vérifie l'identité de référence et tue le mutant.

#### Test 8 : `testLeftPadCharNoPaddingNeeded`
- **Classe :** `org.apache.tika.utils.StringUtilsTest`
- **Mutant ciblé :** Ligne 107 (`ConditionalsBoundaryMutator` transformant `pads <= 0` en `< 0`).
- **Intention du test :** Vérifier que `leftPad(str, size, padChar)` renvoie l'instance d'origine sans instancier de tableau de caractères lorsque aucun padding n'est nécessaire.
- **Motivation des données :** `new String("abc")` avec `size = 3` et caractère `'x'`.
- **Explication de l'oracle :** `assertSame(str, StringUtils.leftPad(str, 3, 'x'))`. Le mutant alloue un nouvel objet et échoue sur `assertSame`.

#### Test 9 : `testRepeatCharZeroOrNegative`
- **Classe :** `org.apache.tika.utils.StringUtilsTest`
- **Mutant ciblé :** Ligne 139 (`ConditionalsBoundaryMutator` transformant `repeat <= 0` en `< 0`).
- **Intention du test :** Vérifier que répéter un caractère 0 fois renvoie la constante globale `StringUtils.EMPTY` plutôt que d'allouer un `char[0]`.
- **Motivation des données :** Caractère `'a'` avec borne `repeat = 0`.
- **Explication de l'oracle :** `assertSame(StringUtils.EMPTY, StringUtils.repeat('a', 0))`. Le mutant alloue un nouveau `String` vide qui ne correspond pas à la constante mise en cache.

#### Test 10 : `testRepeatStringZeroOrNegative`
- **Classe :** `org.apache.tika.utils.StringUtilsTest`
- **Mutant ciblé :** Ligne 176 (`ConditionalsBoundaryMutator` transformant `repeat <= 0` en `< 0`).
- **Intention du test :** Vérifier que répéter une chaîne 0 fois renvoie immédiatement `StringUtils.EMPTY` sans créer de `StringBuilder`.
- **Motivation des données :** Chaîne `"abc"` avec borne `repeat = 0`.
- **Explication de l'oracle :** `assertSame(StringUtils.EMPTY, StringUtils.repeat("abc", 0))`.

#### Test 11 : `testRepeatStringOneOrEmptyInput`
- **Classe :** `org.apache.tika.utils.StringUtilsTest`
- **Mutant ciblé :** Ligne 180 (`RemoveConditionalMutator` sur `repeat == 1 || inputLength == 0`).
- **Intention du test :** Vérifier que répéter une chaîne avec `repeat = 1` ou répéter une chaîne vide renvoie directement l'instance passée en argument.
- **Motivation des données :** `new String("abc")` avec `repeat = 1`, et `new String("")` avec `repeat = 5`.
- **Explication de l'oracle :** `assertSame(str, StringUtils.repeat(str, 1))` et `assertSame(empty, StringUtils.repeat(empty, 5))`. Les mutants contournent le retour immédiat et allouent de nouveaux objets, échouant `assertSame`.

---

## 7. Intégration continue & Exécution GitHub Actions *(Exécution)*

Un workflow GitHub Actions automatisé a été mis en place dans [`.github/workflows/run-new-tests.yml`](.github/workflows/run-new-tests.yml) pour garantir la validation continue de la suite de tests et de l'analyse de mutation :

1. **Validation Checkstyle stricte :**
   Exécute `mvn checkstyle:check` pour garantir 0 violation sur `tika-core` et `tika-parser-pdf-module`.
2. **Compilation et installation des dépendances du réacteur :**
   Exécute `mvn install -DskipTests -am` sur les modules ciblés.
3. **Exécution de tous les nouveaux tests (ChatUniTest et manuels) :**
   Lance spécifiquement `StringUtilsTest`, `PDFParser_extractSignatures_7_0_Test`, `PDFParser_renderPDF_10_0_Test` et `PDFParser_shouldSpool_8_0_Test` :
   ```bash
   mvn test -B -pl tika-core,tika-parsers/tika-parsers-standard/tika-parsers-standard-modules/tika-parser-pdf-module \
     -Dtest='StringUtilsTest,PDFParser_extractSignatures_7_0_Test,PDFParser_renderPDF_10_0_Test,PDFParser_shouldSpool_8_0_Test'
   ```
4. **Exécution de l'analyse de mutation PITest :**
   Génère les rapports de mutation sur `PDFParser` et `StringUtils`.
5. **Archivage des rapports :**
   Téléverse automatiquement les rapports HTML de PITest en tant qu'artefacts de build (`pit-report-pdfparser` et `pit-report-stringutils`).

Tous les tests compilent, respectent Checkstyle et s'exécutent avec succès dans le pipeline CI.