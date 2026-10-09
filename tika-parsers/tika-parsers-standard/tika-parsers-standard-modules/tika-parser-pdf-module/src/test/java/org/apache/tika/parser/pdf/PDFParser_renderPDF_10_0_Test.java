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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.renderer.PageRangeRequest;
import org.apache.tika.renderer.RenderResults;
import org.apache.tika.renderer.Renderer;

public class PDFParser_renderPDF_10_0_Test {

    @Test
    public void testRenderPDF() throws Exception {
        PDFParser parser = new PDFParser();
        Renderer mockRenderer = Mockito.mock(Renderer.class);
        RenderResults mockResults = Mockito.mock(RenderResults.class);
        when(mockRenderer.render(any(TikaInputStream.class), any(Metadata.class), any(ParseContext.class),
                eq(PageRangeRequest.RENDER_ALL))).thenReturn(mockResults);
        parser.setRenderer(mockRenderer);
        TikaInputStream tstream = TikaInputStream.get(Paths.get("."));
        ParseContext parseContext = new ParseContext();
        PDFParserConfig localConfig = new PDFParserConfig();
        Method method = PDFParser.class.getDeclaredMethod("renderPDF", TikaInputStream.class, ParseContext.class,
                PDFParserConfig.class);
        method.setAccessible(true);
        RenderResults results = (RenderResults) method.invoke(parser, tstream, parseContext, localConfig);
        assertNotNull(results);
        org.junit.jupiter.api.Assertions.assertSame(mockResults, results);
        org.mockito.ArgumentCaptor<Metadata> metadataCaptor = org.mockito.ArgumentCaptor.forClass(Metadata.class);
        Mockito.verify(mockRenderer).render(any(TikaInputStream.class), metadataCaptor.capture(), any(ParseContext.class),
                eq(PageRangeRequest.RENDER_ALL));
        org.junit.jupiter.api.Assertions.assertEquals("application/pdf",
                metadataCaptor.getValue().get(org.apache.tika.metadata.TikaCoreProperties.TYPE));
    }
}
