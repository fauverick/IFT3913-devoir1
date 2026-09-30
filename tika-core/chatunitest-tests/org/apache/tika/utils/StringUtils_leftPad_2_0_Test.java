package org.apache.tika.utils;

import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

public class StringUtils_leftPad_2_0_Test {

    @Test
    public void testLeftPadNullString() {
        assertNull(StringUtils.leftPad(null, 10, "-"));
    }

    @Test
    public void testLeftPadEmptyPadString() {
        // padStr is empty, should default to SPACE (" ")
        assertEquals("   bat", StringUtils.leftPad("bat", 6, ""));
        assertEquals("   bat", StringUtils.leftPad("bat", 6, null));
    }

    @Test
    public void testLeftPadNoPaddingNeeded() {
        assertEquals("bat", StringUtils.leftPad("bat", 3, "z"));
        assertEquals("bat", StringUtils.leftPad("bat", 2, "z"));
    }

    @Test
    public void testLeftPadSingleCharPad() {
        // Triggers branch: padLen == 1 && pads <= PAD_LIMIT
        assertEquals("zzzbat", StringUtils.leftPad("bat", 6, "z"));
    }

    @Test
    public void testLeftPadExactPadLength() {
        // Triggers branch: pads == padLen
        assertEquals("xybat", StringUtils.leftPad("bat", 5, "xy"));
    }

    @Test
    public void testLeftPadPadsLessThanPadLen() {
        // Triggers branch: pads < padLen
        assertEquals("xbat", StringUtils.leftPad("bat", 4, "xyz"));
    }

    @Test
    public void testLeftPadPadsGreaterThanPadLen() {
        // Triggers branch: else (pads > padLen, multi-char pad, padLen > 1)
        assertEquals("xyxybat", StringUtils.leftPad("bat", 7, "xy"));
    }
}
