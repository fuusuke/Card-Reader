package com.apv.ocrsdk;

import java.io.IOException;
import java.io.InputStream;

import net.rim.device.api.xml.parsers.DocumentBuilder;
import net.rim.device.api.xml.parsers.DocumentBuilderFactory;
import net.rim.device.api.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Task {
	public static String TaskStatus[] = new String[] { "Unknown", "Submitted",
			"Queued", "InProgress", "Completed", "ProcessingFailed", "Deleted",
			"NotEnoughCredits" };

	public Task(InputStream byteStream) {
		InputSource source = new InputSource();
		source.setByteStream(byteStream);
		DocumentBuilder builder = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println("Exception: " + e.getMessage());
		}
		Document doc = null;
		try {
			doc = builder.parse(source);
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		} catch (SAXException e) {
			System.out.println("Exception: " + e.getMessage());
		}

		NodeList taskNodes = doc.getElementsByTagName("task");
		Element task = (Element) taskNodes.item(0);

		parseTask(task);
		try {
			byteStream.close();
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}

	public String Status = TaskStatus[0];
	public String Id;
	public String DownloadUrl;

	public boolean isTaskActive() {
		if (Status == TaskStatus[2] || Status == TaskStatus[3]) {
			return true;
		}

		return false;
	}

	private void parseTask(Element taskElement) {
		Id = taskElement.getAttribute("id");
		System.out.println("ID: " + Id);
		Status = parseTaskStatus(taskElement.getAttribute("status"));
		System.out.println("Status: " + Status);
		if (Status == TaskStatus[4]) {
			DownloadUrl = taskElement.getAttribute("resultUrl");
			System.out.println("Download URL: " + DownloadUrl);
		}
	}

	private String parseTaskStatus(String status) {
		if (status.equals("Submitted")) {
			return TaskStatus[1];
		} else if (status.equals("Queued")) {
			return TaskStatus[2];
		} else if (status.equals("InProgress")) {
			return TaskStatus[3];
		} else if (status.equals("Completed")) {
			return TaskStatus[4];
		} else if (status.equals("ProcessingFailed")) {
			return TaskStatus[5];
		} else if (status.equals("Deleted")) {
			return TaskStatus[6];
		} else if (status.equals("NotEnoughCredits")) {
			return TaskStatus[7];
		} else {
			return TaskStatus[0];
		}
	}

}
