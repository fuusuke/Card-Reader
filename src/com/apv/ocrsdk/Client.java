package com.apv.ocrsdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;

import com.apv.http.HTTPRequest;

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
			os.close();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		if (responseCode == 200) {
			InputStream inputStream = null;
			try {
				responseCode = httpConn.getResponseCode();
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

	public Task processDocument(String taskId, ProcessingSettings settings) {
		String url = new String(serverUrl + "/processDocument?taskId=" + taskId
				+ "&" + settings.asUrlParams());
		System.out.println("URL: " + url);
		HttpConnection httpConn = null;
		try {
			httpConn = (HttpConnection) Connector.open(url);
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}
		int responseCode = 0;
		if (responseCode == 200) {
			InputStream inputStream = null;
			try {
				responseCode = httpConn.getResponseCode();
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

	// -----------NOT REQUIRED
	/*
	 * public Task processRemoteImage(String fileUrl, ProcessingSettings
	 * settings) throws Exception { URL url = new
	 * URL(String.format("%s/processRemoteImage?source=%s&%s", serverUrl,
	 * URLEncoder.encode(fileUrl, "UTF-8"), settings.asUrlParams()));
	 * 
	 * HttpURLConnection connection = openGetConnection(url); return
	 * getResponse(connection); }
	 * 
	 * public Task processBusinessCard(String filePath, BusCardSettings
	 * settings) throws Exception { URL url = new URL(serverUrl +
	 * "/processBusinessCard?" + settings.asUrlParams()); byte[] fileContents =
	 * readDataFromFile(filePath);
	 * 
	 * HttpURLConnection connection = openPostConnection(url);
	 * 
	 * connection.setRequestProperty("Content-Length",
	 * Integer.toString(fileContents.length));
	 * connection.getOutputStream().write(fileContents);
	 * 
	 * return getResponse(connection); }
	 * 
	 * public Task processTextField(String filePath, TextFieldSettings settings)
	 * throws Exception { URL url = new URL(serverUrl + "/processTextField?" +
	 * settings.asUrlParams()); byte[] fileContents =
	 * readDataFromFile(filePath);
	 * 
	 * HttpURLConnection connection = openPostConnection(url);
	 * 
	 * connection.setRequestProperty("Content-Length",
	 * Integer.toString(fileContents.length));
	 * connection.getOutputStream().write(fileContents);
	 * 
	 * return getResponse(connection); }
	 * 
	 * public Task processBarcodeField(String filePath, BarcodeSettings
	 * settings) throws Exception { URL url = new URL(serverUrl +
	 * "/processBarcodeField?" + settings.asUrlParams()); byte[] fileContents =
	 * readDataFromFile(filePath);
	 * 
	 * HttpURLConnection connection = openPostConnection(url);
	 * 
	 * connection.setRequestProperty("Content-Length",
	 * Integer.toString(fileContents.length));
	 * connection.getOutputStream().write(fileContents);
	 * 
	 * return getResponse(connection); }
	 * 
	 * public Task processCheckmarkField(String filePath) throws Exception { URL
	 * url = new URL(serverUrl + "/processCheckmarkField"); byte[] fileContents
	 * = readDataFromFile(filePath);
	 * 
	 * HttpURLConnection connection = openPostConnection(url);
	 * 
	 * connection.setRequestProperty("Content-Length",
	 * Integer.toString(fileContents.length));
	 * connection.getOutputStream().write(fileContents);
	 * 
	 * return getResponse(connection); }
	 * 
	 * public Task processFields(String taskId, String settingsPath) throws
	 * Exception { URL url = new URL(serverUrl + "/processFields?taskId=" +
	 * taskId); byte[] fileContents = readDataFromFile(settingsPath);
	 * 
	 * HttpURLConnection connection = openPostConnection(url);
	 * 
	 * connection.setRequestProperty("Content-Length",
	 * Integer.toString(fileContents.length));
	 * connection.getOutputStream().write(fileContents);
	 * 
	 * return getResponse(connection); }
	 * 
	 * public Task processMrz(String filePath) throws Exception { URL url = new
	 * URL(serverUrl + "/processMrz"); byte[] fileContents =
	 * readDataFromFile(filePath);
	 * 
	 * HttpURLConnection connection = openPostConnection(url);
	 * 
	 * connection.setRequestProperty("Content-Length",
	 * Integer.toString(fileContents.length));
	 * connection.getOutputStream().write(fileContents);
	 * 
	 * return getResponse(connection); } public Task captureData(String
	 * filePath, String templateName) throws Exception { URL url = new
	 * URL(serverUrl + "/captureData?template=" + templateName); byte[]
	 * fileContents = readDataFromFile(filePath);
	 * 
	 * HttpURLConnection connection = openPostConnection(url);
	 * 
	 * connection.setRequestProperty("Content-Length",
	 * Integer.toString(fileContents.length));
	 * connection.getOutputStream().write(fileContents);
	 * 
	 * return getResponse(connection); }
	 * 
	 * public Task createTemplate(String taskId, String templateName, String
	 * settingsFilePath) throws Exception { URL url = new URL(serverUrl +
	 * "/createTemplate?taskId=" + taskId + "&template=" + templateName); byte[]
	 * fileContents = readDataFromFile(settingsFilePath);
	 * 
	 * HttpURLConnection connection = openPostConnection(url);
	 * 
	 * connection.setRequestProperty("Content-Length",
	 * Integer.toString(fileContents.length));
	 * connection.getOutputStream().write(fileContents);
	 * 
	 * return getResponse(connection); }
	 */
	public Task getTaskStatus(String taskId) {
		String url = new String(serverUrl + "/getTaskStatus?taskId=" + taskId);

		HttpConnection httpConn = null;
		try {
			httpConn = (HttpConnection) Connector.open(url);
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}

		int responseCode = 0;
		if (responseCode == 200) {
			InputStream inputStream = null;
			try {
				responseCode = httpConn.getResponseCode();
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
			HTTPRequest.errorDialog(String.valueOf(data));
	}

	private String encodeUserPassword() {
		String toEncode = applicationId + ":" + password;
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
