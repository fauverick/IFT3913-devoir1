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
package org.apache.tika.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StringUtils}, generated with ChatUniTest and covering
 * {@code isEmpty}, {@code leftPad} (both overloads), and {@code repeat} (both overloads).
 *
 * <p>Baseline JaCoCo coverage before this test class: 0.0% instructions / 0.0% branches
 * for all three method groups (zero reachability – no existing test ever invoked them).
 */
public class StringUtilsTest {

    // -----------------------------------------------------------------------
    // isEmpty(CharSequence)
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // leftPad(String, int, String)
    // -----------------------------------------------------------------------

    @Test
    public void testLeftPadStringNullInput() {
        assertNull(StringUtils.leftPad(null, 10, "-"));
    }

    @Test
    public void testLeftPadStringEmptyPadStr() {
        // padStr is empty / null → defaults to a single space
        assertEquals("   bat", StringUtils.leftPad("bat", 6, ""));
        assertEquals("   bat", StringUtils.leftPad("bat", 6, null));
    }

    @Test
    public void testLeftPadStringNoPaddingNeeded() {
        assertEquals("bat", StringUtils.leftPad("bat", 3, "z"));
        assertEquals("bat", StringUtils.leftPad("bat", 2, "z"));
    }

    @Test
    public void testLeftPadStringSingleCharPad() {
        // Branch: padLen == 1 && pads <= PAD_LIMIT → delegates to char overload
        assertEquals("zzzbat", StringUtils.leftPad("bat", 6, "z"));
    }

    @Test
    public void testLeftPadStringExactPadLength() {
        // Branch: pads == padLen
        assertEquals("xybat", StringUtils.leftPad("bat", 5, "xy"));
    }

    @Test
    public void testLeftPadStringPadsLessThanPadLen() {
        // Branch: pads < padLen → substring(0, pads)
        assertEquals("xbat", StringUtils.leftPad("bat", 4, "xyz"));
    }

    @Test
    public void testLeftPadStringPadsGreaterThanPadLen() {
        // Branch: else (pads > padLen, padLen > 1) → cyclic char array
        assertEquals("xyxybat", StringUtils.leftPad("bat", 7, "xy"));
    }

    // -----------------------------------------------------------------------
    // leftPad(String, int, char)
    // -----------------------------------------------------------------------

    @Test
    public void testLeftPadCharNullInput() {
        assertNull(StringUtils.leftPad(null, 5, 'x'));
    }

    @Test
    public void testLeftPadCharNoPaddingNeeded() {
        assertEquals("abc", StringUtils.leftPad("abc", 3, 'x'));
        assertEquals("abc", StringUtils.leftPad("abc", 2, 'x'));
    }

    @Test
    public void testLeftPadCharNormalPadding() {
        assertEquals("xxabc", StringUtils.leftPad("abc", 5, 'x'));
    }

    @Test
    public void testLeftPadCharExceedPadLimit() {
        int originalLimit = StringUtils.PAD_LIMIT;
        try {
            StringUtils.PAD_LIMIT = 5;
            // pads = 7, which is > PAD_LIMIT (5) → delegates to leftPad(String, int, String)
            assertEquals("xxxxxxxabc", StringUtils.leftPad("abc", 10, 'x'));
        } finally {
            StringUtils.PAD_LIMIT = originalLimit;
        }
    }

    // -----------------------------------------------------------------------
    // repeat(char, int)
    // -----------------------------------------------------------------------

    @Test
    public void testRepeatCharZeroOrNegative() {
        assertEquals("", StringUtils.repeat('a', 0));
        assertEquals("", StringUtils.repeat('a', -1));
    }

    @Test
    public void testRepeatCharPositive() {
        assertEquals("a", StringUtils.repeat('a', 1));
        assertEquals("aaa", StringUtils.repeat('a', 3));
        assertEquals("    ", StringUtils.repeat(' ', 4));
    }

    // -----------------------------------------------------------------------
    // repeat(String, int)
    // -----------------------------------------------------------------------

    @Test
    public void testRepeatStringNull() {
        assertNull(StringUtils.repeat(null, 5));
    }

    @Test
    public void testRepeatStringZeroOrNegative() {
        assertEquals("", StringUtils.repeat("abc", 0));
        assertEquals("", StringUtils.repeat("abc", -1));
    }

    @Test
    public void testRepeatStringOneOrEmptyInput() {
        assertEquals("abc", StringUtils.repeat("abc", 1));
        assertEquals("", StringUtils.repeat("", 5));
    }

    @Test
    public void testRepeatStringSingleCharWithPadLimit() {
        // inputLength == 1, repeat <= PAD_LIMIT → fast-path via repeat(char, int)
        assertEquals("aaa", StringUtils.repeat("a", 3));
        // Force inputLength == 1 && repeat > PAD_LIMIT → switch-case 1
        int originalLimit = StringUtils.PAD_LIMIT;
        try {
            StringUtils.PAD_LIMIT = 5;
            char[] expectedChars = new char[6];
            java.util.Arrays.fill(expectedChars, 'x');
            assertEquals(new String(expectedChars), StringUtils.repeat("x", 6));
        } finally {
            StringUtils.PAD_LIMIT = originalLimit;
        }
    }

    @Test
    public void testRepeatStringInputLengthTwo() {
        // switch-case 2 → optimised two-char array loop
        assertEquals("abab", StringUtils.repeat("ab", 2));
    }

    @Test
    public void testRepeatStringInputLengthDefault() {
        // switch default → StringBuilder append loop
        assertEquals("abcabc", StringUtils.repeat("abc", 2));
    }
}
