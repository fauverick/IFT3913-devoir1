package org.apache.tika.parser.pdf;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { PDFParser_getSupportedTypes_0_0_Test.class, PDFParser_parse_1_0_Test.class })
public class PDFParser_Suite {
}
