package com.apv.ocrsdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.ui.component.Dialog;

import com.apv.http.HTTPRequest;
import com.apv.main.ClientSettings;

public class Client {
	public String applicationId;
	public String password;

	public String serverUrl = "http://cloud.ocrsdk.com";

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
			String authString = "Basic: " + encodeUserPassword();
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

			os.write(bos.toByteArray());
			// os.close();
		} catch (Exception e) {
			System.out.println("Exception 1: " + e.getMessage());
		}
		try {
			responseCode = httpConn.getResponseCode();
		} catch (IOException e1) {
			System.out.println("Exception 2: " + e1.getMessage());
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
				inputStream.close();
				httpConn.close();
				return task;
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
		} else if (responseCode == 401) {
			HTTPRequest
					.errorDialog("HTTP 401 Unauthorized. Please check your application id and password");
		} else if (responseCode == 407) {
			HTTPRequest.errorDialog("HTTP 407. Proxy authentication error");
		} else {
			HTTPRequest.errorDialog("Exiting from getResponse");
		}
		return null;
	}

	public Task getTaskStatus(String taskId) {
		String url = new String(serverUrl + "/getTaskStatus?taskId=" + taskId);

		HttpConnection httpConn = null;
		try {
			httpConn = (HttpConnection) Connector.open(url);
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}

		int responseCode = 0;
		try {
			responseCode = httpConn.getResponseCode();
		} catch (IOException e1) {
			e1.printStackTrace();
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
				inputStream.close();
				httpConn.close();
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

	public void downloadResult(Task task, String outputFile) {
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
			System.out.println("URL: " + url);
			connection = (HttpConnection) Connector.open(url);
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}
		// do not use authenticated connection

		try {
			byte[] data = IOUtilities.streamToBytes(connection
					.openInputStream());
			HTTPRequest.errorDialog(String.valueOf(data));
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}
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

		byte[] data = null;
		try {
			data = IOUtilities.streamToBytes(connection.openInputStream());
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}
		if (data != null)
			try {
				Dialog.inform(new String(data, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				HTTPRequest
						.errorDialog("Data received but was not encoded properly.");
				e.printStackTrace();
			}
	}

	private String encodeUserPassword() {
		String toEncode = ClientSettings.APPLICATION_ID + ":"
				+ ClientSettings.PASSWORD;
		System.out.println("To Encode: " + toEncode);
		return Base64.encode(toEncode);
	}

	// private Task[] getTaskListResponse(HttpURLConnection connection)
	// throws Exception {
	// int responseCode = connection.getResponseCode();
	// if (responseCode == 200) {
	// InputStream inputStream = connection.getInputStream();
	// BufferedReader reader = new BufferedReader(new InputStreamReader(
	// inputStream));
	//
	// return Task.LoadTasks(reader);
	// } else if (responseCode == 401) {
	// throw new Exception(
	// "HTTP 401 Unauthorized. Please check your application id and password");
	// } else if (responseCode == 407) {
	// throw new Exception("HTTP 407. Proxy authentication error");
	// } else {
	// String message = "";
	// try {
	// InputStream errorStream = connection.getErrorStream();
	//
	// BufferedReader reader = new BufferedReader(
	// new InputStreamReader(errorStream));
	//
	// // Parse xml error response
	// InputSource source = new InputSource();
	// source.setCharacterStream(reader);
	// DocumentBuilder builder = DocumentBuilderFactory.newInstance()
	// .newDocumentBuilder();
	// Document doc = builder.parse(source);
	//
	// NodeList error = doc.getElementsByTagName("error");
	// Element err = (Element) error.item(0);
	//
	// message = err.getTextContent();
	// } catch (Exception e) {
	// throw new Exception("Error getting server response");
	// }
	//
	// throw new Exception("Error: " + message);
	// }
	// }

}
