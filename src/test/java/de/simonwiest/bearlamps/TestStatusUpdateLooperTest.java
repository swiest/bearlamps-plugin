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
    assertFalse("Saturday must be a restricted operating time.",
        StatusUpdateLooper.isInRestrictedOperatingHours(new GregorianCalendar(2011, 3, 5, 15, 0, 0)));
    assertFalse("Sunday must be a restricted operating time.",
        StatusUpdateLooper.isInRestrictedOperatingHours(new GregorianCalendar(2011, 3, 6, 15, 0, 0)));
  }

}
