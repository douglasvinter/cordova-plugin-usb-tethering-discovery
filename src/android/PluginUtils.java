/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

package org.apache.cordova.usb.tethered.discovery;

import android.util.Log;
import android.text.TextUtils;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class PluginUtils {
	/**
	 * UPnP spec 1.0
	 */
	final static double UPNP_VERSION_10 = 1.0;
	/**
	 * UPnP spec 1.1
	 */
	final static double UPNP_VERSION_11 = 1.1;
	/**
	 * Constant helper for AngularJS translate Message: Usb cable connected
	 */
	final static String USB_CONNECTED = "USB_CONNECTED";
	/**
	 * Constant helper for AngularJS translate Message: Error receiving data,
	 * check your USB cable/connection
	 */
	final static String USB_CABLE_DISCONNECTED = "USB_CABLE_DISCONNECTED";
	/**
	 * Constant helper for AngularJS translate Message: No USB attached
	 */
	final static String USB_CABLE_NOT_CONNECTED = "USB_CABLE_NOT_CONNECTED";
	/**
	 * Constant helper for AngularJS translate Message: Connection tethered
	 */
	final static String CONNECTION_TETHERED = "CONNECTION_TETHERED";
	/**
	 * Constant helper for AngularJS translate Message: Connection not tethered
	 */
	final static String CONNECTION_NOT_TETHERED = "CONNECTION_NOT_TETHERED";
	/**
	 * Constant helper for AngularJS translate Message: USB cable is not present,
	 * tethering cannot be enabled
	 */
	 final static String CONFIGURATION_NOT_FINISHED_USB = "CONFIGURATION_NOT_FINISHED_USB";
	 /**
 	 * Constant helper for AngularJS translate Message: USB cable is present,
 	 * but tethering is not detected and/or enabled
 	 */
	 final static String CONFIGURATION_NOT_FINISHED_NETWORK = "CONFIGURATION_NOT_FINISHED_NETWORK";
	/**
	 * Constant helper for AngularJS translate Message: Device is ready
	 */
	final static String DEVICE_IS_READY = "DEVICE_IS_READY";
	/**
	 * Constant helper for AngularJS translate Message: Search done but no
	 * results
	 */
	final static String NO_RESULTS = "NO_RESULTS";
	/**
	 * Constant helper for AngularJS translate Message: Search done with success
	 */
	final static String NETWORK_DISCOVERY_SUCCESS = "NETWORK_DISCOVERY_SUCCESS";
	/**
	 * USB state constant
	 */
	final static String USB_STATE = "android.hardware.usb.action.USB_STATE";
	/**
	 * expected status from USB
	 */
	final static String USB_CABLE_CONNECTED = "connected";
	/**
	 * USB ethernet device name
	 */
	public final static CharSequence USB_TETHERING = "ndis";
	/**
	 * Logging TAG
	 */
	final static String TAG = "Ssdp";
	/**
	 * MSearch default message body for both UPNP versions
	 */
	final static String[] UPNP_MSEARCH = { "M-SEARCH * HTTP/1.1", "HOST: 239.255.255.250:1900",
			"MAN: \"ssdp:discover\"", "ST: %s", "MX: %s", "", "" };
	/**
	 * SSDP IGMP traffic Group
	 */
	final static SocketAddress GROUP = new InetSocketAddress("239.255.255.250", 1900);
	/**
	 * SSDP Time to Live
	 */
	final static int TTL = 4;

	/**
	 * Builds SSDP M-SEARCH string for either UPnP 1.0 or 1.1, default 1.1
	 *
	 * @param st
	 *            What exactly is expected to be searched over the network
	 * @param version
	 *            UPnP version
	 * @param mx
	 *            maximum wait time, each UPnP version has its own threshold
	 * @return Device/Service IPV4 Address, if any
	 */
	public static String buildMSearch(String st, double version, int mx) {
		assert (version == UPNP_VERSION_10 || version == UPNP_VERSION_11) : "Invalid UPnP Version";
		assert ((version == UPNP_VERSION_10 && mx >= 1 && mx <= 120)
				|| (version == UPNP_VERSION_11 && mx >= 1 && mx <= 5)) : "Invalid mx parameter";
		// Builds M - SEARCH
		String msg = TextUtils.join("\r\n", UPNP_MSEARCH);
		msg = String.format(msg, st, mx);
		Log.v(TAG, "M-SEARCH: \n" + msg);
		return msg;
	}
}
