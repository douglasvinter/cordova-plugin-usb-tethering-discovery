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


import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URL;


class SSDPParser implements Serializable {

	final static long serialVersionUID = 1L;
	final static Pattern SSDP_RESPONSE = Pattern.compile("(.+?): (.+)");
	private String usn = "";
	private String uuid = "";
	private String server = "";
	private String location = "";
	private String ipAddress = "";

	public SSDPParser(String payload) throws SSDPParserException {
		if (payload.length() == 0) {
			throw new SSDPParserException("Invalid UPnP payload");
		}

		HashMap<String, String> search = new HashMap<String, String>();
		Matcher m = SSDP_RESPONSE.matcher(payload);

		while (m.find()) {
			if (m.groupCount() > 1) {
				search.put(m.group(m.groupCount() - 1).toLowerCase(), m.group(m.groupCount()));
			}
		}

		if (!search.containsKey("usn") || !(search.containsKey("al") || search.containsKey("location"))) {
			throw new SSDPParserException("Invalid UPnP payload");
		}

		this.setServer((search.get("server") != null) ? search.get("server") : "");
		this.setLocation((search.get("al") != null) ? search.get("al") : search.get("location"));
		try {
			InetAddress address = InetAddress.getByName(new URL(this.getLocation()).getHost());
			this.setIpAddress(address.getHostAddress().toString());
		} catch (IOException e) {
			throw new SSDPParserException("Non complaint UPnP payload");
		}
		this.setUsn(search.get("usn"));
		if (search.get("usn") != null && search.get("usn").contains("uuid")) {
			String[] usn = search.get("usn").split(":");
			this.setUUID(usn[1]);
		}
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getLocation() {
		return location;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getUsn() {
		return usn;
	}

	public void setUsn(String usn) {
		this.usn = usn;
	}

	public String getUUID() {
		return uuid;
	}

	public void setUUID(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return "SSDPParser [server=" + server + ", location=" + location + ", usn=" + usn + ", uuid=" + uuid + "]";
	}

}
