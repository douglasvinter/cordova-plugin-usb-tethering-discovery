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

@objc(UsbDiscovery)
class UsbDiscovery : CDVPlugin, HttpConnectorDelegate
{

  private var connector: HttpConnector?
  private var results: [[String: String]] = []

  @objc(log:)
  func log (_ message: String)
  {
    NSLog("HttpDiscovery - %@", message)
  }

  @objc(httpAddressGuessing:)
  func httpAddressGuessing(_ command: CDVInvokedUrlCommand)
  {
    commandDelegate!.run(inBackground:
    {
      let expectedTarget: String = String(describing: command.arguments[0]);
      let withPort: String = String(describing: command.arguments[1]);
      let extendedUrl: String = String(describing: command.arguments[2]);
      if self.connector == nil
      {
        self.connector = HttpConnector(self)
      }
      if (DeviceManager.isUSBCableConnected() && DeviceManager.isDeviceThethered())
      {
        for addr in PluginUtils.getUsbIpAddresses()
        {
          self.connector?.sendRequestFor(addr, withPort, extendedUrl, expectedTarget, command)
        }
      } else
      {
        self.isDeviceReady(command)
      }
    })
  }

  func httpConnector(_ httpConnector: HttpConnector, _ ipAddress: String, _ isAddressRequested: Bool, _ command: CDVInvokedUrlCommand)
  {
    if PluginUtils.getUsbIpAddresses().last == ipAddress  && !isAddressRequested
    {
      URLSession.shared.invalidateAndCancel()
      self.buildStatusMessage(false, PluginUtils.noResults, command)
    }
    if isAddressRequested
    {
      URLSession.shared.invalidateAndCancel()
      let data: [String: String] = ["ipAddress": ipAddress]
      self.buildResultMessage(true, data, command)
    }
  }

  @objc(isDeviceReady:)
  func isDeviceReady(_ command: CDVInvokedUrlCommand)
  {
    // isUSBCableConnected - flag to be returned indicating if the usb cable is connected or not
    // isDeviceThethered - flag to be returned indicating if the device is tethered
    let usb: Bool = DeviceManager.isUSBCableConnected() ? true: false;
    let status: Bool = (usb && DeviceManager.isDeviceThethered())
    // Check which part of the configuration failed.
    let resultMsg: String = (status) ? PluginUtils.deviceIsReady : (usb) ?
    PluginUtils.configurationNotFinishedNetwork : PluginUtils.configurationNotFinishedUsb;
    buildStatusMessage(status, resultMsg, command)
  }

  @objc(isUsbConnected:)
  func isUsbConnected(_ command: CDVInvokedUrlCommand)
  {
    // flag to be returned indicating if the usb cable is connected or not
    let status: Bool = DeviceManager.isUSBCableConnected()
    let resultMsg: String = (status) ? PluginUtils.usbConnected : PluginUtils.usbCableNotConnected
    buildStatusMessage(status, resultMsg, command)
  }

  @objc(isConnectionTethered:)
  func isConnectionTethered(_ command: CDVInvokedUrlCommand)
  {
    // flag to be returned indicating if the device is tethered
    let status: Bool = DeviceManager.isDeviceThethered()
    let resultMsg: String = (status) ? PluginUtils.connectionTethered : PluginUtils.connectionNotTethered
    buildStatusMessage(status, resultMsg, command)
  }

  func buildStatusMessage(_ status: Bool, _ message: String, _ command: CDVInvokedUrlCommand)
  {
    let result: String! = toJSON(["status": status, "message": message])
    sendResult(status, result, command)
  }

  func buildResultMessage(_ status: Bool, _ data: [String: String], _ command: CDVInvokedUrlCommand)
  {
    let result: String! = toJSON(["status": status, "data": [data],
    "message": PluginUtils.networkDiscoverySuccess])
    sendResult(status, result, command)
  }

  func sendResult(_ status: Bool, _ message: String!, _ command: CDVInvokedUrlCommand)
  {
    let result: CDVPluginResult

    if status
    {
      result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: message)
    } else
    {
      result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: message)
    }

    result.setKeepCallbackAs(true)
    commandDelegate!.send(result, callbackId: command.callbackId)
  }

  func toJSON(_ data: [String: Any]) -> String?
  {
    do
    {
      let jsonData = try JSONSerialization.data(withJSONObject: data, options: .prettyPrinted)
      return String(data: jsonData, encoding: String.Encoding.utf8)
    } catch let error
    {
        print("error converting to json: \(error)")
        return nil
    }
  }
}
