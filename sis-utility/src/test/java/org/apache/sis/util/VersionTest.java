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
package org.apache.sis.util;

import org.apache.sis.test.TestCase;
import org.junit.Test;

import static org.apache.sis.test.Assert.*;


/**
 * Tests the {@link Version} class, especially the {@code compareTo} method.
 *
 * @author  Martin Desruisseaux (IRD)
 * @since   0.3 (derived from geotk-2.4)
 * @version 0.3
 * @module
 */
public final strictfp class VersionTest extends TestCase {
    /**
     * Tests a numeric-only version.
     */
    @Test
    public void testNumeric() {
        final Version version = new Version("6.11.2");
        assertEquals("6.11.2", version.toString());
        assertEquals( 6, version.getMajor());
        assertEquals(11, version.getMinor());
        assertEquals( 2, version.getRevision());
        assertSame(version.getRevision(), version.getComponent(2));
        assertNull(version.getComponent(3));

        assertTrue(version.compareTo(new Version("6.11.2")) == 0);
        assertTrue(version.compareTo(new Version("6.8"   )) >  0);
        assertTrue(version.compareTo(new Version("6.12.0")) <  0);
        assertTrue(version.compareTo(new Version("6.11"  )) >  0);
    }

    /**
     * Tests a alpha-numeric version.
     */
    @Test
    public void testAlphaNumeric() {
        final Version version = new Version("1.6.b2");
        assertEquals("1.6.b2", version.toString());
        assertEquals( 1, version.getMajor());
        assertEquals( 6, version.getMinor());
        assertEquals("b2", version.getRevision());
        assertSame(version.getRevision(), version.getComponent(2));
        assertNull(version.getComponent(3));

        assertTrue(version.compareTo(new Version("1.6.b2")) == 0);
        assertTrue(version.compareTo(new Version("1.6.b1"))  > 0);
        assertTrue(version.compareTo(new Version("1.07.b1")) < 0);
    }

    /**
     * Tests serialization.
     */
    @Test
    public void testSerialization() {
        final Version version = new Version("1.6.b2");
        assertNotSame(version, assertSerializedEquals(version));
    }
}
