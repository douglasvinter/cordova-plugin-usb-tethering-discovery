/*
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
var cordova = require('cordova'),
    argscheck = require('cordova/argscheck');

/**
 * @description This method will perform the whole discovery process for the
 * given services/devices you require over a USB tethering connection
 *
 * __NOTE__:
 *    -USB Tethering is required before call this function, otherwise
 * you'll receive an errorCallback
 *    - iOS blocks port 1900 on USB tethering, SSDP for those
 * devices wont work.
 * __Supported Platforms__
 *
 * - Android - SSDP Full support
 * - iOS - http address guessing over hotspot address, SSDP is not supported on hotspot
 ** @example
 * navigator.usbdiscovery.httpAddressGuessing(successCallback, errorCallback, httpDiscoveryOptions);
 * @param {module:httpAddressGuessing.onSuccess} successCallback
 * @param {module:httpAddressGuessing.onError}   errorCallback
 * @param {module:httpAddressGuessing.httpDiscoveryOptions} options httpDiscoveryOptions
 * httpDiscoveryOptions is an Object expected to contanin the following:
 * {'server': 'Name of the web server i.e Apache/2.4',
    'extendedUrl': '/home',
 *	'port': 81}
 *
 * @example
 * navigator.usbdiscovery.discovery(successCallback, errorCallback, discoveryOptions);
 * @param {module:discovery.onSuccess} successCallback
 * @param {module:discovery.onError}   errorCallback
 * @param {module:discovery.discoveryOptions} options discoveryOptions
 * discoveryOptions is an Object expected to contanin the following:
 * {'searchTarget': 'ssdp:all',
 *	'upnpVersion': 1.0, (or 1.1)
 *	'maxWaitTime': 10} (between 5 and 100, depending on the upnp version)
 */

module.exports = {

  httpAddressGuessing: function(successCallback, errorCallback, httpDiscoveryOptions) {
    var server = argscheck.getValue(httpDiscoveryOptions.server, 'Apache/2.4');
    var port = argscheck.getValue(httpDiscoveryOptions.port, 80);
    var extendedUrl = argscheck.getValue(httpDiscoveryOptions.extendedUrl, '');
    if (extendedUrl.length > 1 && !extendedUrl.startsWith('/')) {
        extendedUrl = '/' + extendedUrl;
    }
    return cordova.exec(successCallback, errorCallback, 'UsbDiscovery', 'httpAddressGuessing',
    [server, port, extendedUrl]);
  },

  discovery: function(successCallback, errorCallback, discoveryOptions) {
    discoveryOptions = discoveryOptions || {};

    var st = argscheck.getValue(discoveryOptions.searchTarget, 'ssdp:all');
    var ver = argscheck.getValue(discoveryOptions.upnpVersion, 1.1);
    var mx = argscheck.getValue(discoveryOptions.maxWaitTime, 5);

    discoveryOptions = [st, ver, mx];

    return cordova.exec(successCallback, errorCallback, 'UsbDiscovery', 'discovery',
    discoveryOptions);
  },

  isDeviceReady: function(successCallback, errorCallback) {
    return cordova.exec(successCallback, errorCallback, 'UsbDiscovery', 'isDeviceReady', []);
  },

  isUsbConnected: function(successCallback, errorCallback) {
    return cordova.exec(successCallback, errorCallback, 'UsbDiscovery', 'isUsbConnected', []);
  },

  isConnectionTethered: function(successCallback, errorCallback) {
    return cordova.exec(successCallback, errorCallback, 'UsbDiscovery', 'isConnectionTethered', []);
  },

};
