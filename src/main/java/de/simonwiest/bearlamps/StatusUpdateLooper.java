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

import hudson.Extension;
import hudson.model.BallColor;
import hudson.model.PeriodicWork;
import hudson.model.TopLevelItem;
import hudson.model.ViewProperty;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.View;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import de.simonwiest.bearlamps.OutletController.Status;

/**
 * The {@link StatusUpdateLooper} is called at a fixed frequency to update the
 * bear status lights. It aggregates the status of jobs contained in a given
 * view and sends switching commands to an outlet controller accordingly.
 * 
 * @author swiest
 */
@Extension
public class StatusUpdateLooper extends PeriodicWork {

  /** Number of outlet that connects to the red bear. */
  private static final int OUTLET_RED = 1;

  /** Number of outlet that connects to the yellow bear. */
  private static final int OUTLET_YELLOW = 2;

  /** Number of outlet that connects to the blue/green bear. */
  private static final int OUTLET_GREEN = 3;

  /** Update interval (in seconds). */
  private static int UPDATE_INTERVAL = 60;

  private static final Logger LOGGER = Logger.getLogger(StatusUpdateLooper.class.getName());

  @Override
  public long getRecurrencePeriod() {
    // Note: This value is read only once during Jenkins startup.
    // Later changes will not be reflected.
    return UPDATE_INTERVAL * 1000;
  }

  @Override
  protected void doRun() throws Exception {
    LOGGER.fine("Updating status of all bear lights...");

    for (View view : Hudson.getInstance().getViews()) {
      updateStatus(view);
    }

    LOGGER.fine("Done updating status of all bear lights.");
  }

  /**
   * Updates the status of a bear light set connected to a single view.
   * 
   * @param view Jenkins view that determines the status of the bear light set.
   */
  private void updateStatus(View view) {
    LOGGER.fine("Updating bear lights connected with view '" + view.getDisplayName() + "'...");

    // Search for a bear light configuration for current view.
    BearLampsProperties blp = null;
    List<ViewProperty> props = view.getAllProperties();
    for (ViewProperty viewProperty : props) {
      if (viewProperty instanceof BearLampsProperties) {
        blp = (BearLampsProperties) viewProperty;
        break;
      }
    }
    if (blp == null) {
      LOGGER.fine("View '" + view.getDisplayName() + "' does not contain a bear lamp configuration. Skipping this view...");
      return;
    }

    // Validate current configuration.
    if (!isValid(blp)) {
      LOGGER.fine("Bear lamps properties for view '" + view.getDisplayName() + "' are invalid/incomplete. Skipping update of lights...");
      return;
    }

    // Create a new controller to communicate with outlet box.
    OutletController oc = new OutletController(blp.getIpAddress(), blp.getIpPort(), blp.getUser(), blp.getPassword());

    // Temporarily disabled? Mute all sockets.
    if (blp.isDisabled()) {
      LOGGER.fine("Bear lamps temporarily disabled. Switching all lights off...");
      oc.switchAllOutletsOff();
      return;
    }

    // Restricted operating hours? Mute sockets.
    if (blp.isRestrictedOperatingHours()) {
      Calendar now = Calendar.getInstance();
      if (isInRestrictedOperatingHours(now)) {
        LOGGER.fine("Bear lamps have restriced operating hours. Switching all lights off...");
        oc.switchAllOutletsOff();
        return;
      }
    }

    boolean isBuilding = false;
    boolean allJobsOk = true;
    for (TopLevelItem item : view.getItems()) {
      if (item instanceof Job) {
        Job job = (Job) item;
        // Is at least one job animated (=currently building)?
        BallColor color = job.getIconColor();
        LOGGER.fine("Color of job '" + job.getFullDisplayName() + "' is '" + color + "'.");
        if (color.isAnimated()) {
          isBuilding = true;
        }
        // Is at least one job 'red' or 'yellow' (=troubled)?
        String colorName = color.name();
        if (colorName.startsWith("RED") || colorName.startsWith("YELLOW")) {
          allJobsOk = false;
        }
      }
    }

    // Update status lights.
    oc.switchOutlet(OUTLET_RED, (allJobsOk) ? Status.OFF : Status.ON);
    oc.switchOutlet(OUTLET_YELLOW, (isBuilding) ? Status.ON : Status.OFF);
    oc.switchOutlet(OUTLET_GREEN, (allJobsOk) ? Status.ON : Status.OFF);
  }

  /**
   * Validates a bear lamp configuration.
   * 
   * @param blp Configuration to be validated.
   * @return True, if configuration is complete and valid.
   */
  static boolean isValid(BearLampsProperties blp) {

    if (StringUtils.isEmpty(blp.getIpAddress())) {
      return false;
    }

    if (StringUtils.isEmpty(blp.getIpPort())) {
      return false;
    }

    return true;
  }

  /**
   * Tests whether a given point in time lies within the restricted operation
   * hours.
   * 
   * @param now Point in time to check for.
   * @return True, if the given in time lies within the restricted operating
   *         hours.
   */
  static boolean isInRestrictedOperatingHours(Calendar now) {
    int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
    int hour = now.get(Calendar.HOUR_OF_DAY);
    if ((dayOfWeek == Calendar.SATURDAY) || (dayOfWeek == Calendar.SUNDAY) || (hour < 8) || (hour > 18)) {
      return true;
    }
    return false;
  }

}
