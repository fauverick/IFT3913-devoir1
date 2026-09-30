package org.apache.tika.utils;

import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

public class StringUtils_repeat_4_0_Test {

    @Test
    public void testRepeatZeroOrNegative() {
        assertEquals("", StringUtils.repeat('a', 0));
        assertEquals("", StringUtils.repeat('a', -1));
    }

    @Test
    public void testRepeatPositive() {
        assertEquals("a", StringUtils.repeat('a', 1));
        assertEquals("aaa", StringUtils.repeat('a', 3));
        assertEquals("    ", StringUtils.repeat(' ', 4));
    }
}
