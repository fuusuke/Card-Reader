package com.apv.http;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.apv.main.BBOCR;

public class HTTPRequest {

	private ConnectionThread _connectionThread = null;

	public HTTPRequest(byte[] imageBytes) {
		_connectionThread = new ConnectionThread(imageBytes);
		_connectionThread.start();
	}

	private class ConnectionThread extends Thread {
		private static final int TIMEOUT = 500; // ms

		private String _theUrl;

		private volatile boolean _fetchStarted = false;
		private volatile boolean _stop = false;
		private volatile byte[] raw = null;

		public ConnectionThread(byte[] imageBytes) {
			this.raw = imageBytes;
		}

		public void run() {
			// Call TestApp
			try {
				if (raw != null) {
					BBOCR.performRecognition(raw);
				} else {
					System.out.println("Image is NULL");
				}
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
		}

	}

	public static void errorDialog(final String message) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Dialog.alert(message);
			}
		});
	}
}
