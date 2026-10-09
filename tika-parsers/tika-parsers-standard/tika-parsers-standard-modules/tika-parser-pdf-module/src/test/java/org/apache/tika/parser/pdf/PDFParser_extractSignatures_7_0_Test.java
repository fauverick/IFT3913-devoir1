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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PDF;
import org.apache.tika.metadata.TikaCoreProperties;

public class PDFParser_extractSignatures_7_0_Test {

    private void invokeExtractSignatures(PDFParser parser, PDDocument doc, Metadata metadata) throws Exception {
        Method method = PDFParser.class.getDeclaredMethod("extractSignatures", PDDocument.class, Metadata.class);
        method.setAccessible(true);
        method.invoke(parser, doc, metadata);
    }

    @Test
    public void testExtractSignaturesEmpty() throws Exception {
        PDFParser parser = new PDFParser();
        PDDocument doc = mock(PDDocument.class);
        Metadata metadata = new Metadata();
        when(doc.getSignatureFields()).thenReturn(Collections.emptyList());
        invokeExtractSignatures(parser, doc, metadata);
        assertNull(metadata.get(PDF.HAS_SIGNATURE_FIELDS));
        assertNull(metadata.get(TikaCoreProperties.HAS_SIGNATURE));
    }

    @Test
    public void testExtractSignaturesNullSignature() throws Exception {
        PDFParser parser = new PDFParser();
        PDDocument doc = mock(PDDocument.class);
        Metadata metadata = new Metadata();
        PDSignatureField field = mock(PDSignatureField.class);
        when(field.getSignature()).thenReturn(null);
        when(doc.getSignatureFields()).thenReturn(Collections.singletonList(field));
        invokeExtractSignatures(parser, doc, metadata);
        assertEquals("true", metadata.get(PDF.HAS_SIGNATURE_FIELDS));
        assertNull(metadata.get(TikaCoreProperties.HAS_SIGNATURE));
    }

    @Test
    public void testExtractSignaturesValid() throws Exception {
        PDFParser parser = new PDFParser();
        PDDocument doc = mock(PDDocument.class);
        Metadata metadata = new Metadata();
        PDSignatureField field = mock(PDSignatureField.class);
        PDSignature signature = mock(PDSignature.class);
        Calendar cal = Calendar.getInstance();
        when(signature.getName()).thenReturn("Test Signer");
        when(signature.getSignDate()).thenReturn(cal);
        when(signature.getContactInfo()).thenReturn("test@example.com");
        when(signature.getFilter()).thenReturn("Adobe.PPKLite");
        when(signature.getLocation()).thenReturn("Wonderland");
        when(signature.getReason()).thenReturn("Approval");
        when(field.getSignature()).thenReturn(signature);
        when(doc.getSignatureFields()).thenReturn(Collections.singletonList(field));
        invokeExtractSignatures(parser, doc, metadata);
        assertEquals("true", metadata.get(PDF.HAS_SIGNATURE_FIELDS));
        assertEquals("true", metadata.get(TikaCoreProperties.HAS_SIGNATURE));
        assertEquals("Test Signer", metadata.get(TikaCoreProperties.SIGNATURE_NAME));
        assertEquals("test@example.com", metadata.get(TikaCoreProperties.SIGNATURE_CONTACT_INFO));
        assertEquals("Adobe.PPKLite", metadata.get(TikaCoreProperties.SIGNATURE_FILTER));
        assertEquals("Wonderland", metadata.get(TikaCoreProperties.SIGNATURE_LOCATION));
        assertEquals("Approval", metadata.get(TikaCoreProperties.SIGNATURE_REASON));
        assertNotNull(metadata.get(TikaCoreProperties.SIGNATURE_DATE));
    }

    @Test
    public void testExtractSignaturesNullDate() throws Exception {
        PDFParser parser = new PDFParser();
        PDDocument doc = mock(PDDocument.class);
        Metadata metadata = spy(new Metadata());
        PDSignatureField field = mock(PDSignatureField.class);
        PDSignature signature = mock(PDSignature.class);
        when(signature.getName()).thenReturn("Test Signer");
        when(signature.getSignDate()).thenReturn(null);
        when(field.getSignature()).thenReturn(signature);
        when(doc.getSignatureFields()).thenReturn(Collections.singletonList(field));
        invokeExtractSignatures(parser, doc, metadata);
        assertEquals("true", metadata.get(PDF.HAS_SIGNATURE_FIELDS));
        assertEquals("true", metadata.get(TikaCoreProperties.HAS_SIGNATURE));
        assertEquals("Test Signer", metadata.get(TikaCoreProperties.SIGNATURE_NAME));
        assertNull(metadata.get(TikaCoreProperties.SIGNATURE_DATE));
        verify(metadata, never()).add(eq(TikaCoreProperties.SIGNATURE_DATE),
                nullable(Calendar.class));
    }
}
