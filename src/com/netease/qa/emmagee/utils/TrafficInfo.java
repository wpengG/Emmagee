/*
 * Copyright (c) 2012-2013 NetEase, Inc. and other contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.netease.qa.emmagee.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.net.TrafficStats;
import android.util.Log;

/**
 * information of network traffic
 * 
 * @author andrewleo
 */
public class TrafficInfo {

	private static final String LOG_TAG = "Emmagee-" + TrafficInfo.class.getSimpleName();
	private static final int UNSUPPORTED = -1;

	private String uid;

	public TrafficInfo(String uid) {
		this.uid = uid;
	}

	/**
	 * get total network traffic, which is the sum of upload and download
	 * traffic.
	 * 
	 * @return total traffic include received and send traffic
	 */
	public long getTrafficInfo() {

		Log.i(LOG_TAG, "get traffic information");
		Log.d(LOG_TAG, "uid===" + uid);

		long rcvTraffic = UNSUPPORTED;
		long sndTraffic = UNSUPPORTED;

		// Use getUidRxBytes and getUidTxBytes to get network traffic,these API
		// return both tcp and udp usage
		rcvTraffic = TrafficStats.getUidRxBytes(Integer.parseInt(uid));
		sndTraffic = TrafficStats.getUidTxBytes(Integer.parseInt(uid));

		if (rcvTraffic == UNSUPPORTED || sndTraffic == UNSUPPORTED) {
			return UNSUPPORTED;
		}

		RandomAccessFile rafRcv = null, rafSnd = null;
		String rcvPath = "/proc/uid_stat/" + uid + "/tcp_rcv";
		String sndPath = "/proc/uid_stat/" + uid + "/tcp_snd";

		try {
			rafRcv = new RandomAccessFile(rcvPath, "r");
			rafSnd = new RandomAccessFile(sndPath, "r");
			rcvTraffic = Long.parseLong(rafRcv.readLine());
			sndTraffic = Long.parseLong(rafSnd.readLine());
		} catch (FileNotFoundException e) {
			rcvTraffic = UNSUPPORTED;
			sndTraffic = UNSUPPORTED;
		} catch (NumberFormatException e) {
			Log.e(LOG_TAG, "NumberFormatException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG_TAG, "IOException: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (rafRcv != null) {
					rafRcv.close();
				}
				if (rafSnd != null)
					rafSnd.close();
			} catch (IOException e) {
				Log.w(LOG_TAG, "Close randomAccessFile exception: " + e.getMessage());
			}
		}

		if (rcvTraffic == UNSUPPORTED || sndTraffic == UNSUPPORTED) {
			return UNSUPPORTED;
		} else
			return rcvTraffic + sndTraffic;
	}
}
