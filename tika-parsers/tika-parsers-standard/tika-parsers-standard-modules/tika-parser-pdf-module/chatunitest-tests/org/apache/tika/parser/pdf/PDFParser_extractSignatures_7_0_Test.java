package org.apache.tika.parser.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PDF;
import org.apache.tika.metadata.TikaCoreProperties;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.apache.tika.metadata.PDF.OCR_PAGE_COUNT;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.io.RandomAccessStreamCache;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.fixup.AbstractFixup;
import org.apache.pdfbox.pdmodel.fixup.PDDocumentFixup;
import org.apache.pdfbox.pdmodel.fixup.processor.AcroFormDefaultsProcessor;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.tika.annotation.TikaComponent;
import org.apache.tika.config.ConfigDeserializer;
import org.apache.tika.config.JsonConfig;
import org.apache.tika.config.ParseContextConfig;
import org.apache.tika.exception.AccessPermissionException;
import org.apache.tika.exception.EncryptedDocumentException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.AccessPermissions;
import org.apache.tika.metadata.PagedText;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.PasswordProvider;
import org.apache.tika.parser.RenderingParser;
import org.apache.tika.parser.pdf.updates.IncrementalUpdateRecord;
import org.apache.tika.parser.pdf.updates.IsIncrementalUpdate;
import org.apache.tika.parser.pdf.updates.StartXRefOffset;
import org.apache.tika.parser.pdf.updates.StartXRefScanner;
import org.apache.tika.renderer.PageRangeRequest;
import org.apache.tika.renderer.RenderResult;
import org.apache.tika.renderer.RenderResults;
import org.apache.tika.renderer.Renderer;
import org.apache.tika.renderer.pdf.pdfbox.PDFBoxRenderer;
import org.apache.tika.renderer.pdf.pdfbox.PDFRenderingState;
import org.apache.tika.sax.XHTMLContentHandler;

public class PDFParser_extractSignatures_7_0_Test {

    private void invokeExtractSignatures(PDFParser parser, PDDocument doc, Metadata metadata) throws Exception {
        Method method = PDFParser.class.getDeclaredMethod("extractSignatures", PDDocument.class, Metadata.class);
        method.setAccessible(true);
        method.invoke(parser, doc, metadata);
    }

    @Test
    public void testExtractSignaturesEmpty() throws Exception {
        PDFParser parser = new PDFParser();
        PDDocument doc = Mockito.mock(PDDocument.class);
        Metadata metadata = new Metadata();
        when(doc.getSignatureFields()).thenReturn(Collections.emptyList());
        invokeExtractSignatures(parser, doc, metadata);
        assertNull(metadata.get(PDF.HAS_SIGNATURE_FIELDS));
        assertNull(metadata.get(TikaCoreProperties.HAS_SIGNATURE));
    }

    @Test
    public void testExtractSignaturesNullSignature() throws Exception {
        PDFParser parser = new PDFParser();
        PDDocument doc = Mockito.mock(PDDocument.class);
        Metadata metadata = new Metadata();
        PDSignatureField field = Mockito.mock(PDSignatureField.class);
        when(field.getSignature()).thenReturn(null);
        when(doc.getSignatureFields()).thenReturn(Collections.singletonList(field));
        invokeExtractSignatures(parser, doc, metadata);
        assertEquals("true", metadata.get(PDF.HAS_SIGNATURE_FIELDS));
        assertNull(metadata.get(TikaCoreProperties.HAS_SIGNATURE));
    }

    @Test
    public void testExtractSignaturesValid() throws Exception {
        PDFParser parser = new PDFParser();
        PDDocument doc = Mockito.mock(PDDocument.class);
        Metadata metadata = new Metadata();
        PDSignatureField field = Mockito.mock(PDSignatureField.class);
        PDSignature signature = Mockito.mock(PDSignature.class);
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
        assertNotNull(metadata.getDate(TikaCoreProperties.SIGNATURE_DATE));
    }
}
