package com.apv.http;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.apv.main.BBOCR;

public class OCR {

	private ConnectionThread _connectionThread = null;

	public OCR(byte[] raw) {
		_connectionThread = new ConnectionThread(raw);
		_connectionThread.start();
		// try {
		// if (raw != null) {
		// BBOCR.performRecognition(raw);
		// } else {
		// System.out.println("Image is NULL");
		// }
		// } catch (Exception e) {
		// System.out.println("Exception: " + e.getMessage());
		// }
	}

	private class ConnectionThread extends Thread {
		private volatile byte[] raw = null;

		public ConnectionThread(byte[] imageBytes) {
			this.raw = imageBytes;
		}

		public void run() {
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
