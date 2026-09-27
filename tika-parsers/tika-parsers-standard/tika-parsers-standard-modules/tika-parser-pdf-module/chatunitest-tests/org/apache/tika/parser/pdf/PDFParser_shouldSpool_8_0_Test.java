package org.apache.tika.parser.pdf;

import java.lang.reflect.Method;
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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
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
import org.apache.tika.exception.EncryptedDocumentException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.AccessPermissions;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PDF;
import org.apache.tika.metadata.PagedText;
import org.apache.tika.metadata.TikaCoreProperties;
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
