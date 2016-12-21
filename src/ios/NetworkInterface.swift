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

/**
 * This class represents a network interface in the system. For example, `en0` with a certain IP address.
 * It is a wrapper around the `getifaddrs` system call.
 *
 * - See: `/usr/include/ifaddrs.h`
 */
public class NetworkInterface
{

    var name:String
    var family:NetworkFamilyType
    var ipAddress:String?
    var netmask:String?
    var isRunning:Bool
    var isUp:Bool
    var isLoopback:Bool
    var isMulticastSupported:Bool
    var isBroadcastSupported:Bool
    var broadcastAddress:String?

    /// Constructor responsible for building a network interface object
    /// from ifaddrs object
    /// - parameters:
    ///     - ifaddrsData: ifaddrs raw object
    public init(ifaddrsData:ifaddrs)
    {
        // get flags from ifaddrs struct. Some of the network information may use it
        let flags:Int32 = Int32(ifaddrsData.ifa_flags)
        // set name
        self.name = String(cString: ifaddrsData.ifa_name)
        self.family = NetworkInterface.getNetworkFamilyType(sockaddrData: ifaddrsData.ifa_addr)
        self.ipAddress = NetworkInterface.getAddress(networkFamilyType: self.family, sockaddrData: ifaddrsData.ifa_addr)
        self.netmask = NetworkInterface.getAddress(networkFamilyType: self.family, sockaddrData: ifaddrsData.ifa_netmask)
        self.isRunning = ((flags & IFF_RUNNING) == IFF_RUNNING)
        self.isUp = ((flags & IFF_UP) == IFF_UP)
        self.isLoopback = ((flags & IFF_LOOPBACK) == IFF_LOOPBACK)
        self.isBroadcastSupported = ((flags & IFF_BROADCAST) == IFF_BROADCAST)
        self.broadcastAddress = NetworkInterface.getAddress(networkFamilyType: self.family, sockaddrData: ifaddrsData.ifa_dstaddr)
        self.isMulticastSupported = ((flags & IFF_MULTICAST) == IFF_MULTICAST)
    }

    /// Function responsible for extracting network family type information from
    /// ifaddrs.ifa_addr object
    /// - parameters:
    ///     - data: sockaddrData raw object
    /// - returns: network family type information (IPv4, IPv6, Unrecognized)
    private static func getNetworkFamilyType(sockaddrData:UnsafeMutablePointer<sockaddr>) -> NetworkFamilyType
    {
        // network family type information to be returned
        var networkFamilyType:NetworkFamilyType = NetworkFamilyType.Unrecognized
        // check sa_family flag
        if (sockaddrData.pointee.sa_family == UInt8(AF_INET))
        {
            networkFamilyType = .IPv4
        } else if (sockaddrData.pointee.sa_family == UInt8(AF_INET6))
        {
            networkFamilyType = .IPv6
        }
        return networkFamilyType
    }

    /// Function responsible for extracting adress (IP or Netmask) information from
    /// ifaddrs.sockaddr object in according of the network family type
    /// - parameters:
    ///     - networkFamilyType: network family type in order to decide which approch will be used
    ///     - sockaddrData: ifaddrs.sockaddrData pointer
    /// - returns: address associated to the object (ip or netmask) or nil in case of network family type is Unrecognized
    private static func getAddress(networkFamilyType:NetworkFamilyType, sockaddrData:UnsafeMutablePointer<sockaddr>?) -> String?
    {
        // address to be returned
        var addressToBeReturned:String? = nil
        // check if sockaddrData has value
        if (sockaddrData != nil)
        {
            // in according of the family type, check which address will be returned
            if (networkFamilyType == .IPv4)
            {
                // assign sockaddrData parameter to a var in order to not get an error on getnameinfo first parameter construction
                var mutableSockaddrData:sockaddr = sockaddrData!.pointee
                // try to get information about host name and if the information was gotten correctly, assign it
                var hostname:[CChar] = [CChar](repeating: 0, count: Int(2049))
                if (getnameinfo(&mutableSockaddrData, socklen_t(mutableSockaddrData.sa_len),
                                &hostname, socklen_t(hostname.count), nil, socklen_t(0), NI_NUMERICHOST) == 0)
                {
                    addressToBeReturned = String(cString: hostname)
                }
            } else if (networkFamilyType == .IPv6)
            {
                // assign sockaddrData parameter to a var in order to not get an error on getnameinfo first parameter construction
                var mutableSockaddrData:sockaddr = sockaddrData!.pointee
                // get address to be returned
                var ip: [Int8] = [Int8](repeating: Int8(0), count: Int(INET6_ADDRSTRLEN))
                addressToBeReturned = inetNtoP(&mutableSockaddrData, ip: &ip)
            }
        }
        return addressToBeReturned
    }

    private static func inetNtoP(_ addr:UnsafeMutablePointer<sockaddr>, ip:UnsafeMutablePointer<Int8>) -> String?
    {
        let addr6 = unsafeBitCast(addr, to: UnsafeMutablePointer<sockaddr_in6>.self)
        let conversion:UnsafePointer<CChar> = inet_ntop(AF_INET6, &addr6.pointee.sin6_addr, ip, socklen_t(INET6_ADDRSTRLEN))
        let s = String(cString: conversion)
        return s
    }
}
