package org.apache.tika.utils;

import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

public class StringUtils_isEmpty_0_0_Test {

    @Test
    public void testIsEmptyWithNull() {
        assertTrue(StringUtils.isEmpty(null));
    }

    @Test
    public void testIsEmptyWithEmptyString() {
        assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    public void testIsEmptyWithNonEmptyString() {
        assertFalse(StringUtils.isEmpty("hello"));
    }

    @Test
    public void testIsEmptyWithWhitespaceString() {
        assertFalse(StringUtils.isEmpty(" "));
    }

    @Test
    public void testIsEmptyWithStringBuilder() {
        assertTrue(StringUtils.isEmpty(new StringBuilder()));
        assertFalse(StringUtils.isEmpty(new StringBuilder("test")));
    }
}
