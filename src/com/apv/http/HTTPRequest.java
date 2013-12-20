package com.apv.http;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.apv.main.TestApp;

/**
 * This sample makes a an http or https connection to a specified URL and
 * retrieves and displays html content.
 */
public class HTTPRequest {

	private ConnectionThread _connectionThread = null;

	/**
	 * Creates a new HTTPDemo object
	 * 
	 * @param imageBytes
	 */
	public HTTPRequest(byte[] imageBytes) {
		// _connectionThread = new ConnectionThread(imageBytes);
		// _connectionThread.start();
		if (imageBytes != null) {
			try {
				TestApp.performRecognition(imageBytes);
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
		} else {
			System.err.println("Image is NULL");
		}

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

		/**
		 * Indicates whether the thread has started fetching yet
		 * 
		 * @return True if the fetching has started, false otherwise
		 */
		private boolean isStarted() {
			return _fetchStarted;
		}

		/**
		 * Fetches a page
		 * 
		 * @param url
		 *            The url of the page to fetch
		 */
		private void fetch(String url) {
			_fetchStarted = true;
			_theUrl = url;
		}

		/**
		 * Stop the thread
		 */
		private void stop() {
			_stop = true;
		}

		/**
		 * This method is where the thread retrieves the content from the page
		 * whose url is associated with this thread.
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			// Call TestApp
			try {
				if (raw != null) {
					TestApp.performRecognition(raw);
				} else {
					System.err.println("Image is NULL");
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
