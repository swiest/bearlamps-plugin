/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Tom Huybrechts
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
import hudson.model.ViewProperty;
import hudson.model.ViewPropertyDescriptor;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Configuration settings needed by the {@link StatusUpdateLooper}. The settings
 * will show up on the configuration page of a view.
 */
public class BearLampsProperties extends ViewProperty {

  private static final Logger LOGGER = Logger.getLogger(BearLampsProperties.class.getName());

  /** IP address of IP socket device. */
  private String ipAddress;
  /** UDP port of IP socket device. */
  private String ipPort;
  /** Password of IP socket device. */
  private String password;
  /** If true, bears are temporarily muted on the next status update. */
  private boolean disabled;
  /** If true, bears are switched on only during working hours on work days. */
  private boolean restrictedOperatingHours;

  @DataBoundConstructor
  public BearLampsProperties(String ipAddress, String ipPort, String password, boolean disabled, boolean restrictedOperatingHours) {
    this.ipAddress = StringUtils.trim(ipAddress);
    this.ipPort = StringUtils.trim(ipPort);
    this.password = StringUtils.trim(password);
    this.disabled = disabled;
    this.restrictedOperatingHours = restrictedOperatingHours;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getIpPort() {
    return ipPort;
  }

  public void setIpPort(String ipPort) {
    this.ipPort = ipPort;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public boolean isRestrictedOperatingHours() {
    return restrictedOperatingHours;
  }

  public void setRestrictedOperatingHours(boolean restrictedOperatingHours) {
    this.restrictedOperatingHours = restrictedOperatingHours;
  }

  @Extension
  public static class DescriptorImpl extends ViewPropertyDescriptor {

    @Override
    public String getDisplayName() {
      return "Bear Lamps";
    }

    public FormValidation doTestConnection(@QueryParameter String ipAddress, @QueryParameter String ipPort, @QueryParameter String password) throws IOException, ServletException {
      LOGGER.info("Sending test sequence to IP address '" + ipAddress + "', port '" + ipPort + "' (password not echoed here)...");
      OutletController oc = new OutletController(ipAddress, ipPort, password);
      oc.testSockets();
      return FormValidation.ok("Test sequence completed.");
    }

  }
}
