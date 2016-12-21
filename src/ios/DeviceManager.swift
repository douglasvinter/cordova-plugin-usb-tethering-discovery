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
import UIKit

open class DeviceManager
{

    /// Method responsible for getting a list of all network interface names
    /// - returns: list of all network interface names
    open static func getAllAvailableNetworkInterfaces() -> [NetworkInterface]
    {
        // list of interface names to be returned
        var networkInterfaceList:[NetworkInterface] = []
        // try to get information about all network interfaces using ifaddrs library
        var networkInterfacePtr:UnsafeMutablePointer<ifaddrs>? = nil
        let isNetworkInformationGottenWithSuccess:Bool = getifaddrs(&networkInterfacePtr) == 0
        // if infomartion was sucessfully gotten, iterate over all network interfaces by using the structure provided by ifaddrs library
        if (isNetworkInformationGottenWithSuccess)
        {
            while (networkInterfacePtr != nil)
            {
                // append network interface to the list of all availables
                networkInterfaceList.append(NetworkInterface(ifaddrsData: (networkInterfacePtr?.pointee)!))
                // go to the next network interface pointer
                networkInterfacePtr = networkInterfacePtr?.pointee.ifa_next
            }
            // deallocate pointer
            freeifaddrs(networkInterfacePtr)
        }
        return networkInterfaceList
    }

    /// Method responsible for checking if usb cable is connected to any device
    /// - returns: flag indicating if usb cable is connected or not
    open static func isUSBCableConnected() -> Bool
    {
        // flag to be returned indicating if the usb cable is connected or not
        var isUSBCableConnected:Bool = false;
        // enable batery monitoring on purpose before checking by usb cable connection
        UIDevice.current.isBatteryMonitoringEnabled = true;
        // check if usb cable is really connected
        isUSBCableConnected = (UIDevice.current.batteryState == UIDeviceBatteryState.charging
          || UIDevice.current.batteryState == UIDeviceBatteryState.full);
        // disable batery monitoring on purpose before leaving the method
        UIDevice.current.isBatteryMonitoringEnabled = false;

        return isUSBCableConnected;
    }

    /// Method responsible for checking if device is tethered
    /// - returns: flag indicating if device is tethered
    open static func isDeviceThethered() -> Bool
    {
        // flag to be returned indicating if the device is thethered or not
        var isDeviceThethered:Bool = false;
        // get the list of network interface names and check if there is one 'bridge' available
        let networkInterfaceList:[NetworkInterface] = DeviceManager.getAllAvailableNetworkInterfaces()
        // Checks for bridge 100 interface with IPv4 enabled
        let bridgeNetworkInterface:NetworkInterface? = networkInterfaceList.filter { $0.name.contains(PluginUtils.usbTethering)
          && $0.isUp && $0.isRunning && !$0.isLoopback}.first
        // check if device is tethered
        isDeviceThethered = (bridgeNetworkInterface != nil)

        return isDeviceThethered;
    }

}
