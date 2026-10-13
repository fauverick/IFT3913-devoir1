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

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Tests manuels de {@link StringUtils} visant les mutants qui survivent à
 * {@link StringUtilsTest} (ChatUniTest).
 */
public class StringUtilsManualTest {

    @Test
    public void testLeftPadStringNoPaddingNeeded() {
        // Arrange
        String str = "bat";

        // Act
        String exactSize = StringUtils.leftPad(str, 3, "xyz");
        String smallerSize = StringUtils.leftPad(str, 2, "xyz");

        // Assert
        assertSame(str, exactSize);
        assertSame(str, smallerSize);
    }

    @Test
    public void testLeftPadCharNoPaddingNeeded() {
        // Arrange
        String str = "abc";

        // Act
        String exactSize = StringUtils.leftPad(str, 3, 'x');
        String smallerSize = StringUtils.leftPad(str, 2, 'x');

        // Assert
        assertSame(str, exactSize);
        assertSame(str, smallerSize);
    }

    @Test
    public void testRepeatCharZeroOrNegative() {
        // Arrange
        char ch = 'a';

        // Act
        String zero = StringUtils.repeat(ch, 0);
        String negative = StringUtils.repeat(ch, -1);

        // Assert
        assertSame(StringUtils.EMPTY, zero);
        assertSame(StringUtils.EMPTY, negative);
    }

    @Test
    public void testRepeatStringZeroOrNegative() {
        // Arrange
        String str = "abc";

        // Act
        String zero = StringUtils.repeat(str, 0);
        String negative = StringUtils.repeat(str, -1);

        // Assert
        assertSame(StringUtils.EMPTY, zero);
        assertSame(StringUtils.EMPTY, negative);
    }

    @Test
    public void testRepeatStringOneOrEmptyInput() {
        // Arrange
        String str = "abc";
        String empty = "";

        // Act
        String repeatedOnce = StringUtils.repeat(str, 1);
        String repeatedEmpty = StringUtils.repeat(empty, 5);

        // Assert
        assertSame(str, repeatedOnce);
        assertSame(empty, repeatedEmpty);
    }
}
