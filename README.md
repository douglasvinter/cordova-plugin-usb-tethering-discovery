# USB Tethering SSDP discovery
To use this plugin you must have a USB cable connected on the target device
and be sure the usb tethering is enabled.

Notes:

 - Your firewall may block multicast packages, make sure to have your environment configured.

 - For Linux embedded devices the USB interface may require a manually kick up `dhclient usbX`, be sure to have your linux distro configured properly.

 - If you don't know how UPnP/SSDP works nor if there's service available on your computer/test environment, you can use Samsung Kies.
 
 - For iOS, SSDP port is blocked over USB Tethering (hotspot) as a workaround you can 'discover' a HTTP speakable service in the correct address range (127.20.10.X). I plan to implement a real network protocol for that and also enable the capability to use wi-fi searches, if you wish.
 
### TO-DO (improvements)

- [ ] $q.notify support for UI with keep callback on cordova
- [ ] ngCordova API - Future implementation
- [ ] Automatic USB Tethering - Can't be done over public APIs *YET*
- [ ] Check for SLP implementation in iOS
- [ ] Plan a bonjour implementation as it works on iOS Hotspots (USB/Wi-fi)

### Implemented features

- [X] UPnP M-SEARCH 1.0 / 1.1 compatible
- [X] Android 4.4+ support (6.1.1 already tested)
- [x] iOS device support - Does not support UPnP multicast over USB Tethering, using HTTP "Guessing"
over a defined LAN Class B - 172.20.1.0/24




# API Wrapping for AngularJS
```javascript

(function() {
  'use strict';

  /**
   * @ngdoc service
   * @name components.services.service:usbDiscoveryService
   * @description
   * A service to handle usb discovery
   */

  angular
    .module('components.services')
    .service('usbDiscoveryService', UsbDiscoveryService);

  function UsbDiscoveryService($q) {
    var vm = this;
    this.$inject = ['$q'];

    /**
     * @ngdoc function
     * @name discovery
     * @methodOf components.services.service:usbDiscoveryService
     * @description
     * Check's if usb cable is attached
    * @param {Object} options - sample expected object:
     * {'searchTarget': 'ssdp:all',
     *	 'upnpVersion': 1.1,
     *	 'maxWaitTime': 5}
     * @returns {Array} success - discovered endpoint(s)/service(s), or empty array if none
     * @returns {String} error - String error adapted for AngularJs Translate
     */
     vm.discovery = function () {
       var defer = $q.defer();

       if ('cordova' in window  && ionic.Platform.platform() == 'android') {
         vm.doDiscoveryAndroid(defer);
       } else if  ('cordova' in window  && ionic.Platform.platform() == 'ios') {
         vm.doDiscoveryiOS(defer);
       } else {
         defer.reject(vm.reject);
       }

       return defer.promise;
     };

     /**
      * @ngdoc function
      * @name discovery
      * @methodOf components.services.service:usbDiscoveryService
      * @description
      * Check's if usb cable is attached
     * @param {Object} options - sample expected object:
      * {'searchTarget': 'ssdp:all',
      *	 'upnpVersion': 1.1,
      *	 'maxWaitTime': 5}
      * @returns {Array} success - discovered endpoint(s)/service(s), or empty array if none
      * @returns {String} error - String error adapted for AngularJs Translate
      */
     vm.doDiscoveryAndroid = function (defer) {
       if ('cordova' in window) {
         window.cordova.plugins.UsbDiscovery.discovery(function(success) {
           success = vm.normalize(success);
           defer.resolve(success);
         }, function(error) {
           error = vm.normalize(error);
           defer.reject(error);
         }, SOME_CONSTANT_WITH_PARAMETERS);
       } else {
         defer.reject(vm.reject);
       }
     };

     /**
      * @ngdoc function
      * @name normalize
      * @methodOf components.service:usbDiscoveryService
      * @description
      * Normalizes Cordova Plugin data
      * iOS Cordova does not send JSON data, needs to parse it.
      */
     vm.normalize = function(data) {
       return (typeof data === 'string') ? JSON.parse(data) : data;
     };

     /**
      * @ngdoc function
      * @name discovery
      * @methodOf components.services.service:usbDiscoveryService
      * @description
      * Check's if usb cable is attached
      * @param {Object} options - sample expected object:
      * {'server': 'Embedded Web Server',
      *   'port': 8080,
      *   'extendedUrl': '/api/some-data'} extendedUrl is optional
      * @returns {Array} success - discovered endpoint(s)/service(s), or empty array if none
      * @returns {String} error - String error adapted for AngularJs Translate
      */
     vm.doDiscoveryiOS = function(defer) {
       if ('cordova' in window) {
         window.cordova.plugins.UsbDiscovery.httpAddressGuessing(function(success) {
           success = vm.normalize(success);
           defer.resolve(success);
         }, function(error) {
           error = vm.normalize(error);
           defer.reject(error);
         }, SOME_CONSTANT_WITH_PARAMETERS);
       } else {
         defer.reject(vm.reject);
       }
    };

    /**
     * @ngdoc function
     * @name isUsbConnected
     * @methodOf components.services.service:usbDiscoveryService
     * @description
     * Check's if usb cable is attached
     * @returns {String} USB_CONNECTED - incase of attached USB
	   * @returns {String} USB_CABLE_NOT_CONNECTED - incase of attached USB
     */
    vm.isUsbConnected = function () {
      var defer = $q.defer();
      if ('cordova' in window) {
        window.cordova.plugins.UsbDiscovery.isUsbConnected(function(success) {
          defer.resolve(success);
        }, function(error) {
          defer.reject(error);
        });
      } else {
        defer.reject('Cannot run plugin in browser mode');
      }

      return defer.promise;
    };

    /**
     * @ngdoc function
     * @name isConnectionTethered
     * @methodOf components.services.service:usbDiscoveryService
     * @description
     * Check's if usb connection is tethered or not
     * @returns {String} CONNECTION_TETHERED - if connection is tethered
	   * @returns {String} CONNECTION_NOT_TETHERED - means cable is attached but tethering is disabled
     */
    vm.isConnectionTethered = function () {
      var defer = $q.defer();

      if ('cordova' in window) {
        window.cordova.plugins.UsbDiscovery.isConnectionTethered(function(success) {
          defer.resolve(success);
        }, function(error) {
          defer.reject(error);
        });
      } else {
        defer.reject('Cannot run plugin in browser mode');
      }

      return defer.promise;
    };

    /**
     * @ngdoc function
     * @name isDeviceReady
     * @methodOf components.services.service:usbDiscoveryService
     * @description
     * Check's if the device is ready to perform the discovery
     * @returns {String} DEVICE_IS_READY - you're ok to fire a discovery
	   * @returns {String} CONFIGURATION_NOT_FINISHED - steps for tethering were not performed
     */
    vm.isDeviceReady = function () {
      var defer = $q.defer();

      if ('cordova' in window) {
        window.cordova.plugins.UsbDiscovery.isDeviceReady(function(success) {
          defer.resolve(success);
        }, function(error) {
          defer.reject(error);
        });
      } else {
        defer.reject('Cannot run plugin in browser mode');
      }

      return defer.promise;
    };

    }
})();
```
