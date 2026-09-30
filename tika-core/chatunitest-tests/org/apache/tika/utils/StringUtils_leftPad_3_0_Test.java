package org.apache.tika.utils;

import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

public class StringUtils_leftPad_3_0_Test {

    @Test
    public void testLeftPadNullString() {
        assertNull(StringUtils.leftPad(null, 5, 'x'));
    }

    @Test
    public void testLeftPadNoPaddingNeeded() {
        assertEquals("abc", StringUtils.leftPad("abc", 3, 'x'));
        assertEquals("abc", StringUtils.leftPad("abc", 2, 'x'));
    }

    @Test
    public void testLeftPadNormalPadding() {
        assertEquals("xxabc", StringUtils.leftPad("abc", 5, 'x'));
    }

    @Test
    public void testLeftPadExceedPadLimit() {
        int originalLimit = StringUtils.PAD_LIMIT;
        try {
            StringUtils.PAD_LIMIT = 5;
            // pads = 7, which is > PAD_LIMIT (5). It should delegate to leftPad(String, int, String)
            assertEquals("xxxxxxxabc", StringUtils.leftPad("abc", 10, 'x'));
        } finally {
            StringUtils.PAD_LIMIT = originalLimit;
        }
    }
}
