/*
 * The MIT License
 *
 * Copyright (c) 2013 Simon Wiest
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.simonwiest.bearlamps;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.junit.Test;

public class TestStatusUpdateLooperTest {

  @Test
  public final void testIsInRestrictedOperatingHours() {
    // Restriction depending on the time on a Wednesday.
    assertTrue(StatusUpdateLooper.isInRestrictedOperatingHours(new GregorianCalendar(2011, 3, 1, 6, 0, 0)));
    assertTrue(StatusUpdateLooper.isInRestrictedOperatingHours(new GregorianCalendar(2011, 3, 1, 7, 59, 59)));
    assertFalse(StatusUpdateLooper.isInRestrictedOperatingHours(new GregorianCalendar(2011, 3, 1, 8, 0, 0)));
    assertFalse(StatusUpdateLooper.isInRestrictedOperatingHours(new GregorianCalendar(2011, 3, 1, 18, 0, 0)));
    assertTrue(StatusUpdateLooper.isInRestrictedOperatingHours(new GregorianCalendar(2011, 3, 1, 19, 0, 0)));

    // Restriction depending of the day of week.
    assertFalse("Saturday must be a restricted operating time.", StatusUpdateLooper.isInRestrictedOperatingHours(new GregorianCalendar(2011, 3, 5, 15, 0, 0)));
    assertFalse("Sunday must be a restricted operating time.", StatusUpdateLooper.isInRestrictedOperatingHours(new GregorianCalendar(2011, 3, 6, 15, 0, 0)));
  }

}
