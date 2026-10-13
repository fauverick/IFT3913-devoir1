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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collections;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PDF;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.renderer.PageRangeRequest;
import org.apache.tika.renderer.RenderResults;
import org.apache.tika.renderer.Renderer;

/**
 * Tests manuels de {@link PDFParser} visant les mutants qui survivent aux
 * tests ChatUniTest ({@code PDFParser_*_Test}).
 */
public class PDFParserManualTest {

    @Test
    public void testRenderPDF() throws Exception {
        // Arrange
        PDFParser parser = new PDFParser();
        Renderer mockRenderer = mock(Renderer.class);
        RenderResults mockResults = mock(RenderResults.class);
        when(mockRenderer.render(any(TikaInputStream.class), any(Metadata.class), any(ParseContext.class),
                eq(PageRangeRequest.RENDER_ALL))).thenReturn(mockResults);
        parser.setRenderer(mockRenderer);
        Method method = PDFParser.class.getDeclaredMethod("renderPDF", TikaInputStream.class,
                ParseContext.class, PDFParserConfig.class);
        method.setAccessible(true);

        // Act
        RenderResults results = (RenderResults) method.invoke(parser, TikaInputStream.get(new byte[0]),
                new ParseContext(), new PDFParserConfig());

        // Assert
        assertSame(mockResults, results);
        ArgumentCaptor<Metadata> metadataCaptor = ArgumentCaptor.forClass(Metadata.class);
        verify(mockRenderer).render(any(TikaInputStream.class), metadataCaptor.capture(), any(ParseContext.class),
                eq(PageRangeRequest.RENDER_ALL));
        assertEquals("application/pdf", metadataCaptor.getValue().get(TikaCoreProperties.TYPE));
    }

    @Test
    public void testExtractSignaturesNullDate() throws Exception {
        // Arrange
        PDFParser parser = new PDFParser();
        PDDocument doc = mock(PDDocument.class);
        Metadata metadata = spy(new Metadata());
        PDSignatureField field = mock(PDSignatureField.class);
        PDSignature signature = mock(PDSignature.class);
        when(signature.getName()).thenReturn("Test Signer");
        when(signature.getSignDate()).thenReturn(null);
        when(field.getSignature()).thenReturn(signature);
        when(doc.getSignatureFields()).thenReturn(Collections.singletonList(field));
        Method method = PDFParser.class.getDeclaredMethod("extractSignatures", PDDocument.class, Metadata.class);
        method.setAccessible(true);

        // Act
        method.invoke(parser, doc, metadata);

        // Assert
        assertEquals("true", metadata.get(PDF.HAS_SIGNATURE_FIELDS));
        assertEquals("true", metadata.get(TikaCoreProperties.HAS_SIGNATURE));
        assertEquals("Test Signer", metadata.get(TikaCoreProperties.SIGNATURE_NAME));
        assertNull(metadata.get(TikaCoreProperties.SIGNATURE_DATE));
        verify(metadata, never()).add(eq(TikaCoreProperties.SIGNATURE_DATE), nullable(Calendar.class));
    }

    @Test
    public void testImageStrategyRenderPagesBeforeParse() throws Exception {
        // Arrange
        PDFParserConfig config = noOcrConfig();
        config.setImageStrategy(PDFParserConfig.IMAGE_STRATEGY.RENDER_PAGES_BEFORE_PARSE);

        // Act
        boolean spool = invokeShouldSpool(config);

        // Assert
        assertTrue(spool);
    }

    @Test
    public void testImageStrategyRenderPagesAtPageEnd() throws Exception {
        // Arrange
        PDFParserConfig config = noOcrConfig();
        config.setImageStrategy(PDFParserConfig.IMAGE_STRATEGY.RENDER_PAGES_AT_PAGE_END);

        // Act
        boolean spool = invokeShouldSpool(config);

        // Assert
        assertTrue(spool);
    }

    @Test
    public void testExtractIncrementalUpdateInfoTrue() throws Exception {
        // Arrange
        PDFParserConfig config = noOcrConfig();
        config.setExtractIncrementalUpdateInfo(true);

        // Act
        boolean spool = invokeShouldSpool(config);

        // Assert
        assertTrue(spool);
    }

    @Test
    public void testParseIncrementalUpdatesTrue() throws Exception {
        // Arrange
        PDFParserConfig config = noOcrConfig();
        config.setParseIncrementalUpdates(true);

        // Act
        boolean spool = invokeShouldSpool(config);

        // Assert
        assertTrue(spool);
    }

    private static PDFParserConfig noOcrConfig() {
        PDFParserConfig config = new PDFParserConfig();
        config.setImageStrategy(PDFParserConfig.IMAGE_STRATEGY.NONE);
        config.setExtractIncrementalUpdateInfo(false);
        config.setParseIncrementalUpdates(false);
        OcrConfig ocrConfig = new OcrConfig();
        ocrConfig.setStrategy(OcrConfig.Strategy.NO_OCR);
        config.setOcr(ocrConfig);
        return config;
    }

    private static boolean invokeShouldSpool(PDFParserConfig config) throws Exception {
        Method method = PDFParser.class.getDeclaredMethod("shouldSpool", PDFParserConfig.class);
        method.setAccessible(true);
        return (boolean) method.invoke(new PDFParser(), config);
    }
}
