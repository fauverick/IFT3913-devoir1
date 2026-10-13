/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.parser.pdf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PDFParser_shouldSpool_8_0_Test {

    private PDFParser pdfParser;
    private Method shouldSpoolMethod;

    @BeforeEach
    public void setUp() throws Exception {
        pdfParser = new PDFParser();
        shouldSpoolMethod = PDFParser.class.getDeclaredMethod("shouldSpool", PDFParserConfig.class);
        shouldSpoolMethod.setAccessible(true);
    }

    private boolean invokeShouldSpool(PDFParserConfig config) throws Exception {
        return (boolean) shouldSpoolMethod.invoke(pdfParser, config);
    }

    @Test
    public void testImageStrategyRenderPagesBeforeParse() throws Exception {
        PDFParserConfig config = new PDFParserConfig();
        config.setImageStrategy(PDFParserConfig.IMAGE_STRATEGY.RENDER_PAGES_BEFORE_PARSE);
        config.setExtractIncrementalUpdateInfo(false);
        config.setParseIncrementalUpdates(false);
        assertTrue(invokeShouldSpool(config));
    }

    @Test
    public void testImageStrategyRenderPagesAtPageEnd() throws Exception {
        PDFParserConfig config = new PDFParserConfig();
        config.setImageStrategy(PDFParserConfig.IMAGE_STRATEGY.RENDER_PAGES_AT_PAGE_END);
        config.setExtractIncrementalUpdateInfo(false);
        config.setParseIncrementalUpdates(false);
        assertTrue(invokeShouldSpool(config));
    }

    @Test
    public void testExtractIncrementalUpdateInfoTrue() throws Exception {
        PDFParserConfig config = new PDFParserConfig();
        config.setImageStrategy(PDFParserConfig.IMAGE_STRATEGY.NONE);
        config.setExtractIncrementalUpdateInfo(true);
        config.setParseIncrementalUpdates(false);
        assertTrue(invokeShouldSpool(config));
    }

    @Test
    public void testParseIncrementalUpdatesTrue() throws Exception {
        PDFParserConfig config = new PDFParserConfig();
        config.setImageStrategy(PDFParserConfig.IMAGE_STRATEGY.NONE);
        config.setExtractIncrementalUpdateInfo(false);
        config.setParseIncrementalUpdates(true);
        assertTrue(invokeShouldSpool(config));
    }

    @Test
    public void testOcrStrategyNoOcr() throws Exception {
        PDFParserConfig config = new PDFParserConfig();
        config.setImageStrategy(PDFParserConfig.IMAGE_STRATEGY.NONE);
        config.setExtractIncrementalUpdateInfo(false);
        config.setParseIncrementalUpdates(false);
        OcrConfig ocrConfig = new OcrConfig();
        ocrConfig.setStrategy(OcrConfig.Strategy.NO_OCR);
        config.setOcr(ocrConfig);
        assertFalse(invokeShouldSpool(config));
    }

    @Test
    public void testDefaultConfigShouldSpool() throws Exception {
        PDFParserConfig config = new PDFParserConfig();
        config.setImageStrategy(PDFParserConfig.IMAGE_STRATEGY.NONE);
        config.setExtractIncrementalUpdateInfo(false);
        config.setParseIncrementalUpdates(false);
        OcrConfig ocrConfig = new OcrConfig();
        ocrConfig.setStrategy(OcrConfig.Strategy.AUTO);
        config.setOcr(ocrConfig);
        assertTrue(invokeShouldSpool(config));
    }
}
