/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.cordova.usb.tethered.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collections;

import org.apache.cordova.CordovaInterface;

import android.content.Intent;
import android.content.IntentFilter;

public class UsbMulticast {

    /**
     * Logging TAG
     */
    private static final String TAG = "UsbMulticast";

    /**
     * Multicast Socket, force socket to NOT bind
     */
    private static MulticastSocket conn = null;

    /**
     * socket timeout
     */
    private int socketTimeout = 1;

    /**
     * Cordova interface to device
     */
    private CordovaInterface cordova;

    /**
     * Constructor
     */
    public UsbMulticast(final CordovaInterface cordova) {
        this.cordova = cordova;
    }

    /**
     * Checks if the USB is attached and working
     *
     * @return Boolean USB state working true for working false for not
     *         working/attached
     */
    public boolean isUsbAttached() {
        Intent intent = this.cordova.getActivity().registerReceiver(null, new IntentFilter(PluginUtils.USB_STATE));
        return intent.getExtras().getBoolean(PluginUtils.USB_CABLE_CONNECTED);
    }

    /**
     * Checks for the device network interfaces and returns USB Tether interface
     *
     * @return NetworkInterface USB Tethered interface
     * @throws IOException
     *             In case of no USB attached or no USB connection tether
     */
    public NetworkInterface getInterface() throws IOException {
        if (!isUsbAttached()) {
            throw new IOException(PluginUtils.USB_CABLE_NOT_CONNECTED);
        }

        for (NetworkInterface netIf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (Collections.list(netIf.getInetAddresses()).size() > 0 && !netIf.isLoopback() && netIf.supportsMulticast()
                    && netIf.getDisplayName().contains(PluginUtils.USB_TETHERING)) {
                Log.v(TAG, "Using: " + netIf.getDisplayName());
                for (InetAddress ifAddr : Collections.list(netIf.getInetAddresses())) {
                    if (ifAddr instanceof Inet4Address) {
                        Log.v(TAG, "Using: " + ifAddr.getHostAddress().toString());
                        return netIf;
                    }
                }
            }
        }
        throw new IOException(PluginUtils.CONNECTION_NOT_TETHERED);
    }

    /**
     * Wordaround to know when an interface is ready
     *
     * @return boolean usb network status
     */
    public boolean isConnected() {
        try {
            getInterface();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Send message over IGMP group
     *
     * @param st
     *            What exactly is expected to be searched over the network
     * @param version
     *            UPnP version
     * @param mx
     *            maximum wait time, each UPnP version has its own treshold
     * @throws IOException
     *             - Underling socket error
     */
    public void send(final String st, final double version, final int mx) throws IOException {
        String msg = PluginUtils.buildMSearch(st, version, mx);
        byte[] data = msg.getBytes();
        conn.send(new DatagramPacket(data, data.length, PluginUtils.GROUP));
        Log.v(TAG, "Multicast sent!: " + msg);
    }

    /**
     * Receive unicast packages responses only, this is not made to receive
     * multicast data
     *
     * @return String contaning translated body
     * @throws IOException
     *             - Underling socket error
     */
    public String recv() throws IOException {
        byte[] buff = new byte[1024];
        DatagramPacket pkg = new DatagramPacket(buff, buff.length);

        try {
            conn.receive(pkg);
        } catch (SocketTimeoutException e) {
            Log.v(TAG, "Socket timeout");
        } catch (SocketException e) {
            tearDown();
            throw new IOException(PluginUtils.USB_CABLE_DISCONNECTED);
        }

        return new String(pkg.getData()).trim();
    }

    /**
     * Creates multicast socket to send the request, as of Multicast socket
     * class this is suppose to also allow the receive of unicast datacordova
     *
     * @throws IOException
     *             - Underling socket error
     */
    public void tearUp() throws IOException {
        conn = new MulticastSocket(null);
        NetworkInterface netIf = getInterface();
        conn.setReuseAddress(true);
        conn.setNetworkInterface(netIf);
        conn.setTimeToLive(PluginUtils.TTL);
        conn.setSoTimeout(this.socketTimeout);
        conn.joinGroup(PluginUtils.GROUP, netIf);
        Log.v(TAG, "Joined multicast group successfully");
    }

    /**
     * Closes gracefuly the socket, not closing the socket may affect another
     * networked apps depending on the target SO_MAX_CONN or max socket backlog
     * or max_igmp connections But this also dependens on how the device handles
     * dead connections/file descriptor life cycle
     *
     * @throws IOException
     *             - Underling socket error
     */
    public void tearDown() {
        if (conn != null && conn.isConnected()) {
            try {
                conn.leaveGroup(PluginUtils.GROUP, conn.getNetworkInterface());
                conn.close();
            } catch (IOException e) {
                Log.v(TAG, "Error dead socket found, supressed.");
            } catch (IllegalArgumentException e) {
                Log.v(TAG, "Error unexpected life cycle, supressed.");
            }
            conn = null;
        }
        Log.v(TAG, "Socket closed");
    }
}
