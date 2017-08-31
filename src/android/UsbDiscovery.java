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

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UsbDiscovery extends CordovaPlugin {

    /**
     * Logging TAG
     */
    private static final String TAG = "UsbDiscovery";

    /**
     * Registered actions Android Manifest enum for Multicast socket operations
     */
    private final static int MULTICAST_SOCKET = 5;

    /**
     * Android phone permission
     */
    private final static String INTERNET_PERMISSION = Manifest.permission.INTERNET;

    /**
     * SSDP USB Multicast connector implementation
     */
    private UsbMulticast connector = null;

    @Override
    public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
        this.connector = new UsbMulticast(cordova);
        Log.v(TAG, "Registered!");
    }

    @Override
    public void onDestroy() {
        this.connector.tearDown();
        super.onDestroy();
    }

    /*
     * @Override public void onPause() { connector.tearDown(); super.onPause();
     * }
     */

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action
     *            The action to execute.
     * @param args
     *            JSONArry of arguments for the plugin.
     * @param callbackContext
     *            The callback id used when calling back into JavaScript.
     * @return A PluginResult object with a status and message.
     */
    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        String msg;
        boolean status;
        // Not sure if this block should be here, will check later
        // NOTE: this block is necessary to request permission on Android 6+
        if (!PermissionHelper.hasPermission(this, INTERNET_PERMISSION)) {
            PermissionHelper.requestPermission(this, MULTICAST_SOCKET, INTERNET_PERMISSION);
        }
        if (action.equals("discovery")) {
            final String st = args.optString(0);
            final double ver = Double.parseDouble(args.optString(1));
            final int mx = args.getInt(2);
            // Run on cordova thread pool
            // Regular calls blocks the main thread
            this.cordova.getThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    discovery(st, ver, mx, callbackContext);
                }
            });
        } else if (action.equals("isUsbConnected")) {
            status = (this.connector.isUsbAttached()) ? true : false;
            msg = status ? PluginUtils.USB_CONNECTED : PluginUtils.USB_CABLE_NOT_CONNECTED;
            return buildStatusMessage(status, msg, callbackContext);
        } else if (action.equals("isConnectionTethered")) {
            status = (this.connector.isConnected()) ? true : false;
            msg = status ? PluginUtils.CONNECTION_TETHERED : PluginUtils.CONNECTION_NOT_TETHERED;
            return buildStatusMessage(status, msg, callbackContext);
        } else if (action.equals("isDeviceReady")) {
            boolean usb = this.connector.isUsbAttached();
            status = (usb && this.connector.isConnected()) ? true : false;
            msg = status ? PluginUtils.DEVICE_IS_READY : (usb) ? PluginUtils.CONFIGURATION_NOT_FINISHED_NETWORK : PluginUtils.CONFIGURATION_NOT_FINISHED_USB;
            return buildStatusMessage(status, msg, callbackContext);
        } else {
            // Unhandled call
            return false;
        }

        return true;
    }

    /**
     * Perform the discovery and builds the response to send cordova the results
     *
     * @param st What exactly is expected to be searched over the network
     * @param version UPnP version
     * @param mx maximum wait time, each UPnP version has its own treshold
     */
    private void discovery(final String st, final double version, final int mx, final CallbackContext callbackContext) {
        JSONArray ssdpList = new JSONArray();
        // Current time + mx (max wait time) + 2 seconds * MSEC
        final long loopTime = System.currentTimeMillis() + ((mx + 2) * 1000);
        try {
            this.connector.tearUp();
            this.connector.send(st, version, mx);
            while (System.currentTimeMillis() <= loopTime) {
                String response = this.connector.recv();
                JSONObject ssObj = jsonifyMessage(response);
                if (ssObj.length() > 0) {
                    ssdpList.put(ssObj);
                }
            }

            if (ssdpList.length() > 0) {
                buildStatusMessage(true, ssdpList, callbackContext);
            } else {
                buildStatusMessage(false, PluginUtils.NO_RESULTS, callbackContext);
            }
        } catch (IOException e) {
            // any error which happens while tearing up, sending or receiving will be
            // handled by this exception.
            buildStatusMessage(false, e.getMessage(), callbackContext);
        }
        // Errors while closing the socket is already handled by the
        // connector itself
        this.connector.tearDown();
    }

    /**
     * Builds complaint data format message
     *
     * @param status
     *            message status representation
     * @param message
     *            String containg extended status
     *
     * @return boolean always true, representation of a registered plugin method
     */
    private static boolean buildStatusMessage(final boolean status, final String message, final CallbackContext callbackContext) {
        JSONObject response = new JSONObject();
        try {
            response.put("status", status);
            response.put("message", message);
        } catch (JSONException e) {
            Log.v(TAG, "Error on plugin serialization: " + e.getMessage());
        }
        sendResult(status, response, callbackContext);
        // Returns execute method result (plugin call)
        return true;
    }

    /**
     * Builds complaint data format message
     *
     * @param status
     *            message status representation
     * @param data
     *            Json array contaning parsed response(s)
     */
    private static void buildStatusMessage(final boolean status, final JSONArray data, final CallbackContext callbackContext) {
        JSONObject response = new JSONObject();
        try {
            response.put("status", status);
            response.put("data", data);
            response.put("message", PluginUtils.NETWORK_DISCOVERY_SUCCESS);
        } catch (JSONException e) {
            Log.v(TAG, "Error on plugin serialization: " + e.getMessage());
        }
        sendResult(status, response, callbackContext);
    }

    /**
     * Sends plugin result
     *
     * @param status
     *            message status representation
     * @param response
     *            Json object contaning response
     */
    private static void sendResult(final boolean status, final JSONObject response, final CallbackContext callbackContext) {
        PluginResult result;
        if (status) {
            result = new PluginResult(PluginResult.Status.OK, response);
        } else {
            result = new PluginResult(PluginResult.Status.ERROR, response);
        }
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    /**
     * Converts String response data to JSON object
     *
     * @param message
     *            socket response raw data
     *
     * @return JSONObject parsed ssdp data
     */
    private static JSONObject jsonifyMessage(final String message) {
        JSONObject ssObj = new JSONObject();
        try {
            SSDPParser ss = new SSDPParser(message);
            ssObj.put("ipAddress", ss.getIpAddress());
            ssObj.put("server", ss.getServer());
            ssObj.put("location", ss.getLocation());
            ssObj.put("usn", ss.getUsn());
            ssObj.put("uuid", ss.getUUID());
        } catch (JSONException e) {
            Log.v(TAG, "Invalid payload for SSDP: " + e.getMessage());
        } catch (SSDPParserException e1) {
            Log.v(TAG, "Invalid payload for SSDP: " + e1.getMessage());
        }

        return ssObj;
    }

}
