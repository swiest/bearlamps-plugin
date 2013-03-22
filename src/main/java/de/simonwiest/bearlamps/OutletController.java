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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * The {@link OutletController} encapsulates the network communication with a
 * networked power outlet box.
 * 
 * Currently, this class is talking to a NET-PwrtCtrl box manufactured by
 * http://www.anel-elektronik.de, running firmware version 3.3. A more recent
 * firmware may work as well, but has not been tested yet.
 * 
 * @author swiest
 */
public class OutletController {

  /** Status of a power outlet. */
  public enum Status {
    ON, OFF
  }

  /** Java Logging API logger for this class. */
  private static final Logger LOGGER = Logger.getLogger(OutletController.class.getName());

  private String ipAddress;
  private String ipPort;
  private String password;

  /**
   * Creates a new instance of an outlet controller that communicates with a
   * networked power outlet box.
   * 
   * @param ipAddress ID address of outlet box.
   * @param ipPort IP port to send commands to.
   * @param password Password to authenticate at outlet box.
   */
  public OutletController(String ipAddress, String ipPort, String password) {
    LOGGER.fine("Commands will be sent to IP address '" + ipAddress + "', port '" + ipPort + "'...");
    this.ipAddress = ipAddress;
    this.ipPort = ipPort;
    this.password = password;
  }

  /**
   * Send a test sequence to the outlet box: First, all outlets are switched
   * off, then each outlet is switched on one-by-one, beginning with outlet 1.
   * Finally, all outlets are switched off again.
   */
  public void testSockets() {
    LOGGER.fine("Switching outlet test sequence pattern...");
    switchAllOutletsOff();
    sleep(1000);

    for (int outlet = 1; outlet <= 3; outlet++) {
      switchOutlet(outlet, Status.ON);
      sleep(2000);
    }

    switchAllOutletsOff();
    sleep(1000);
    LOGGER.fine("Done switching outlet test sequence pattern.");
  }

  /**
   * Switches all outlets off.
   */
  public void switchAllOutletsOff() {
    LOGGER.fine("Switching all outlets off...");
    for (int outlet = 1; outlet <= 3; outlet++) {
      switchOutlet(outlet, Status.OFF);
    }
  }

  /**
   * Sets an outlet to a new status sending UDP datagrams.
   * 
   * @param outlet Number of outlet as printed on the outlet box (the numbering
   *          begins with 1, not 0!).
   * @param newStatus New status to set.
   */
  void switchOutlet(int outlet, Status newStatus) {
    LOGGER.fine("Switching outlet no. " + outlet + " to " + newStatus + " using UDP...");
    try {
      String command = Status.ON.equals(newStatus) ? "on" : "off";
      byte[] msg = ("Sw_" + command + outlet + password + (char) 0 + "\r\n").getBytes("iso-8859-1");
      InetAddress destAddr = InetAddress.getByName(ipAddress);
      int destPort = Integer.parseInt(ipPort);
      DatagramSocket udpSocket = new DatagramSocket();
      DatagramPacket message = new DatagramPacket(msg, msg.length, destAddr, destPort);
      udpSocket.send(message);
      udpSocket.close();
      // Give outlet time to switch. Otherwise, fast switchings may be missed.
      sleep(500);
    } catch (IOException e) {
      LOGGER.warning("Could not switch outlet via UDP.\n" + e.getStackTrace());
    }
  }

  private void sleep(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      // Ignore this exception.
    }
  }

}
