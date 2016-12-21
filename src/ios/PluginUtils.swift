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

import Foundation

class PluginUtils
{
    /**
     * Constant helper for AngularJS translate Message: Usb cable connected
     */
    static let usbConnected: String = "USB_CONNECTED"
    /**
     * Constant helper for AngularJS translate Message: Error receiving data,
     * check your USB cable/connection
     */
    static let usbCableDisconnected: String = "USB_CABLE_DISCONNECTED"
    /**
     * Constant helper for AngularJS translate Message: No USB attached
     */
    static let usbCableNotConnected: String = "USB_CABLE_DISCONNECTED"
    /**
     * Constant helper for AngularJS translate Message: Connection tethered
     */
    static let connectionTethered: String = "CONNECTION_TETHERED"
    /**
     * Constant helper for AngularJS translate Message: Connection not tethered
     */
    static let connectionNotTethered: String = "CONNECTION_NOT_TETHERED"
    /**
     * Constant helper for AngularJS translate Message: USB cable is not present,
     * tethering cannot be enabled
     */
    static let configurationNotFinishedUsb: String = "CONFIGURATION_NOT_FINISHED_USB"
    /**
     * Constant helper for AngularJS translate Message: USB cable is present,
     * but tethering is not detected and/or enabled
     */
    static let configurationNotFinishedNetwork: String = "CONFIGURATION_NOT_FINISHED_NETWORK"
    /**
     * Constant helper for AngularJS translate Message: Device is ready
     */
    static let deviceIsReady: String = "DEVICE_IS_READY"
    /**
     * Constant helper for AngularJS translate Message: Search done but no
     * results
     */
    static let noResults: String = "NO_RESULTS"
    /**
  	 * Constant helper for AngularJS translate Message: Search done with success
  	 */
    static let networkDiscoverySuccess: String  = "NETWORK_DISCOVERY_SUCCESS"
    /**
     * USB ethernet device name
     * iProducts creates bridge interface (i.e bridge100)
     */
    static let usbTethering: String = "bridge"

    /**
     * iOS ip range for USB tethering starts in 172.20.10.2 to 172.20.10.14
     * @TO-DO: get bridge interface address and calculate maximum IP address
     * Usin CIDR notation
     */
    open static func getUsbIpAddresses() -> [String]
    {
      let prefix: String = "172.20.10."
      var ranges: [String] = [String]()
      // Iterate over expected IP range 127.20.10.0/24
      for i in 2..<15 {
          ranges.append(prefix + String(i))
      }

      return ranges
    }
}
