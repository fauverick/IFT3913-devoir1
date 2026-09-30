package org.apache.tika.utils;

import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

public class StringUtils_repeat_5_0_Test {

    @Test
    public void testRepeatNull() {
        assertNull(StringUtils.repeat(null, 5));
    }

    @Test
    public void testRepeatZeroOrNegative() {
        assertEquals("", StringUtils.repeat("abc", 0));
        assertEquals("", StringUtils.repeat("abc", -1));
    }

    @Test
    public void testRepeatOneOrEmptyInput() {
        assertEquals("abc", StringUtils.repeat("abc", 1));
        assertEquals("", StringUtils.repeat("", 5));
    }

    @Test
    public void testRepeatSingleCharWithPadLimit() {
        assertEquals("aaa", StringUtils.repeat("a", 3));
        // Test beyond PAD_LIMIT (10000) for single char
        StringUtils.PAD_LIMIT = 5;
        char[] expectedChars = new char[6];
        java.util.Arrays.fill(expectedChars, 'x');
        assertEquals(new String(expectedChars), StringUtils.repeat("x", 6));
        // Reset
        StringUtils.PAD_LIMIT = 10000;
    }

    @Test
    public void testRepeatInputLengthTwo() {
        assertEquals("abab", StringUtils.repeat("ab", 2));
    }

    @Test
    public void testRepeatInputLengthDefault() {
        assertEquals("abcabc", StringUtils.repeat("abc", 2));
    }
}
