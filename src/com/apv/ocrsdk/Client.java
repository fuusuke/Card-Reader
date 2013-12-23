package com.apv.ocrsdk;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.apv.http.OCR;
import com.apv.main.ClientSettings;

public class Client {
	public String applicationId;
	public String password;

	public String serverUrl = "https://cloud.ocrsdk.com";

	public Task processImage(byte[] raw, ProcessingSettings settings) {
		String url = new String(serverUrl + "/processImage?"
				+ settings.asUrlParams());
		System.out.println("URL: " + url);
		HttpConnection httpConn = null;
		int responseCode = 0;
		try {
			ConnectionDescriptor connDesc = new ConnectionFactory()
					.getConnection(url);
			httpConn = (HttpConnection) connDesc.getConnection();
			httpConn.setRequestMethod(HttpConnection.POST);
			String authString = "Basic " + encodeUserPassword();
			// authString = authString.replaceAll("\n", "");
			try {
				System.out.println("AuthString: " + authString);
				httpConn.setRequestProperty("Authorization", authString);
				httpConn.setRequestProperty("Connection", "Keep-Alive");
				httpConn.setRequestProperty("ENCTYPE", "multipart/form-data");
			} catch (IOException e) {
				e.printStackTrace();
			}
			httpConn.setRequestProperty("User-Agent", "BlackBerry");
			httpConn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=----------V2ymHFg03ehbqgZCaKO6jy");

			OutputStream os = httpConn.openOutputStream();

			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			bos.write(("\r\n--" + "----------V2ymHFg03ehbqgZCaKO6jy" + "\r\n")
					.getBytes());
			bos.write(("Content-Disposition: form-data; name=\"mifoto\"; filename=\"myImage.bmp\"\r\n")
					.getBytes());
			bos.write(("Content-Type: image/bmp\r\n\r\n").getBytes());
			bos.write(raw);
			bos.write(("\r\n--" + "----------V2ymHFg03ehbqgZCaKO6jy" + "--\r\n")
					.getBytes());

			bos.close();
			os.write(bos.toByteArray());
			os.close();
		} catch (Exception e) {
			System.out.println("Exception 1: " + e.getMessage());
		}
		try {
			responseCode = httpConn.getResponseCode();
		} catch (IOException e1) {
			final String message = e1.getMessage();
			System.out.println("Exception 2: " + message);
			UiApplication.getUiApplication().invokeLater(new Runnable() {

				public void run() {
					Dialog.alert("Http TimeOut! Exception Message: " + message);
				}
			});
		}
		if (responseCode == 200) {
			InputStream inputStream = null;
			try {
				inputStream = httpConn.openInputStream();
			} catch (IOException e) {
				System.out.println("Exception 3: " + e.getMessage());
			}
			try {
				Task task = new Task(inputStream);
				return task;
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
		} else if (responseCode == 401) {
			OCR.errorDialog("HTTP 401 Unauthorized. Please check your application id and password");
		} else if (responseCode == 407) {
			OCR.errorDialog("HTTP 407. Proxy authentication error");
		} else {
			System.out.println("Error: Exiting from getResponse");
		}
		return null;
	}

	public Task getTaskStatus(String taskId) {
		String url = new String(serverUrl + "/getTaskStatus?taskId=" + taskId);
		System.out.println("URL for getTaskStatus: " + url);
		HttpConnection httpConn = null;
		try {
			httpConn = (HttpConnection) Connector.open(url);
			httpConn.setRequestMethod(HttpConnection.GET);
			String authString = "Basic " + encodeUserPassword();
			httpConn.setRequestProperty("Authorization", authString);
		} catch (IOException e) {
			OCR.errorDialog("getTaskStatus Exception: " + e.getMessage());
		}

		int responseCode = 0;
		try {
			responseCode = httpConn.getResponseCode();
		} catch (IOException e1) {
			System.out.println("Exception: " + e1.getMessage());
		}
		if (responseCode == 200) {
			InputStream inputStream = null;
			try {
				inputStream = httpConn.openInputStream();
			} catch (IOException e) {
				System.out.println("Exception: " + e.getMessage());
			}
			try {
				Task task = new Task(inputStream);
				return task;
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
		} else if (responseCode == 401) {
			System.out
					.println("HTTP 401 Unauthorized. Please check your application id and password");
		} else if (responseCode == 407) {
			System.out.println("HTTP 407. Proxy authentication error");
		} else {
			System.out.println("Exiting from getResponse");
		}
		return null;
	}

	public void downloadResult(Task task, String outputFile) {/*
															 * if (task.Status
															 * !=
															 * Task.TaskStatus
															 * [4]) { throw new
															 * IllegalArgumentException
															 * (
															 * "Invalid task status"
															 * ); }
															 * 
															 * if
															 * (task.DownloadUrl
															 * == null) { throw
															 * new
															 * IllegalArgumentException
															 * (
															 * "Cannot download result without url"
															 * ); }
															 * 
															 * String url = new
															 * String
															 * (task.DownloadUrl
															 * ); HttpConnection
															 * connection =
															 * null; try {
															 * System
															 * .out.println
															 * ("URL: " + url);
															 * connection =
															 * (HttpConnection)
															 * Connector
															 * .open(url); }
															 * catch
															 * (IOException e) {
															 * System
															 * .out.println
															 * ("Exception: " +
															 * e.getMessage());
															 * } // do not use
															 * authenticated
															 * connection
															 * 
															 * try { byte[] data
															 * = IOUtilities.
															 * streamToBytes
															 * (connection
															 * .openInputStream
															 * ());
															 * OCR.errorDialog
															 * (String
															 * .valueOf(data));
															 * } catch
															 * (IOException e) {
															 * System
															 * .out.println
															 * ("Exception: " +
															 * e.getMessage());
															 * }
															 */
	}

	public void downloadResult(Task task) {
		if (task.Status != Task.TaskStatus[4]) {
			throw new IllegalArgumentException("Invalid task status");
		}

		if (task.DownloadUrl == null) {
			throw new IllegalArgumentException(
					"Cannot download result without url");
		}

		String url = new String(task.DownloadUrl);
		HttpConnection connection = null;
		try {
			connection = (HttpConnection) Connector.open(url);
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}

		try {
			final byte[] data = IOUtilities.streamToBytes(connection
					.openInputStream());
			if (data != null) {

				UiApplication.getUiApplication().invokeAndWait(new Runnable() {
					public void run() {
						try {
							Dialog.inform((new String(data, "UTF-8")));
						} catch (UnsupportedEncodingException e) {
							Dialog.inform("Data received but was not encoded properly.");
							System.out.println("Exception: " + e.getMessage());
						}
					}
				});
			}
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}

	private String encodeUserPassword() {
		String toEncode = ClientSettings.APPLICATION_ID + ":"
				+ ClientSettings.PASSWORD;
		System.out.println("To Encode: " + toEncode);
		return Base64.encode(toEncode.getBytes());
	}

	private boolean saveFile(String fileName, String fileContent) {
		DataOutputStream os = null;
		FileConnection fconn = null;
		try {
			fconn = (FileConnection) Connector.open(fileName,
					Connector.READ_WRITE);
			if (!fconn.exists())
				fconn.create();
			os = fconn.openDataOutputStream();
			String myString = fileContent;
			os.write(myString.getBytes());
			os.close();
			fconn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Dialog.alert(e.toString());
			return false;
		}
		return true;
	}

	private String getFileName() {

		return "file:///SDCard/BlackBerry/documents/text.dat";

	}

}
