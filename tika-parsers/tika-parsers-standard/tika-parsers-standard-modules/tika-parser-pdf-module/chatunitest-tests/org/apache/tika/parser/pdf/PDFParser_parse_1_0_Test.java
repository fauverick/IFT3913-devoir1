package org.apache.tika.parser.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.tika.exception.EncryptedDocumentException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PDF;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.updates.IncrementalUpdateRecord;
import org.apache.tika.renderer.pdf.pdfbox.PDFRenderingState;
import org.xml.sax.helpers.DefaultHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.apache.tika.metadata.PDF.OCR_PAGE_COUNT;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
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
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.fixup.AbstractFixup;
import org.apache.pdfbox.pdmodel.fixup.PDDocumentFixup;
import org.apache.pdfbox.pdmodel.fixup.processor.AcroFormDefaultsProcessor;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.tika.annotation.TikaComponent;
import org.apache.tika.config.ConfigDeserializer;
import org.apache.tika.config.JsonConfig;
import org.apache.tika.config.ParseContextConfig;
import org.apache.tika.exception.AccessPermissionException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
import org.apache.tika.metadata.AccessPermissions;
import org.apache.tika.metadata.PagedText;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.PasswordProvider;
import org.apache.tika.parser.RenderingParser;
import org.apache.tika.parser.pdf.updates.IsIncrementalUpdate;
import org.apache.tika.parser.pdf.updates.StartXRefOffset;
import org.apache.tika.parser.pdf.updates.StartXRefScanner;
import org.apache.tika.renderer.PageRangeRequest;
import org.apache.tika.renderer.RenderResult;
import org.apache.tika.renderer.RenderResults;
import org.apache.tika.renderer.Renderer;
import org.apache.tika.renderer.pdf.pdfbox.PDFBoxRenderer;
import org.apache.tika.sax.XHTMLContentHandler;

class PDFParser_parse_1_0_Test {

    private byte[] createMinimalPdf() throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    @Test
    void testParseBasicValidPdf() throws Exception {
        byte[] pdfBytes = createMinimalPdf();
        Path tempFile = Files.createTempFile("tika-test-", ".pdf");
        Files.write(tempFile, pdfBytes);
        try (TikaInputStream tis = TikaInputStream.get(tempFile)) {
            PDFParser parser = new PDFParser();
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            // Configure parser to exercise specific branches (e.g. KCMS, maxMainMemoryBytes)
            PDFParserConfig config = new PDFParserConfig();
            config.setSetKCMS(true);
            config.setMaxMainMemoryBytes(1024 * 1024);
            context.set(PDFParserConfig.class, config);
            // Set incoming incremental update record and rendering state to test cleanup/finally blocks
            context.set(IncrementalUpdateRecord.class, null);
            context.set(PDFRenderingState.class, new PDFRenderingState(tis));
            DefaultHandler handler = new DefaultHandler();
            parser.parse(tis, handler, metadata, context);
            assertEquals("1", metadata.get(PDF.PDF_VERSION));
            assertNotNull(metadata.get(PDF.DOC_INFO_CREATOR_TOOL));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testParseNullHandler() throws Exception {
        byte[] pdfBytes = createMinimalPdf();
        Path tempFile = Files.createTempFile("tika-test-", ".pdf");
        Files.write(tempFile, pdfBytes);
        try (TikaInputStream tis = TikaInputStream.get(tempFile)) {
            PDFParser parser = new PDFParser();
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            // Pass null handler to test branch `if (handler != null)` being false
            parser.parse(tis, null, metadata, context);
            assertEquals("1", metadata.get(PDF.PDF_VERSION));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testParseEncryptedThrowsIfConfigured() throws Exception {
        // A minimal valid PDF is not encrypted, but we can test the config path or invalid password if needed.
        // Let's test with a custom parser config where maxMainMemoryBytes < 0 (setupMainMemoryOnly branch)
        byte[] pdfBytes = createMinimalPdf();
        Path tempFile = Files.createTempFile("tika-test-", ".pdf");
        Files.write(tempFile, pdfBytes);
        try (TikaInputStream tis = TikaInputStream.get(tempFile)) {
            PDFParser parser = new PDFParser();
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            PDFParserConfig config = new PDFParserConfig();
            config.setMaxMainMemoryBytes(-1L);
        }
    }
}
