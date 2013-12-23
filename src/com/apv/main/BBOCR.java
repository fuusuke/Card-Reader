package com.apv.main;

import com.apv.ocrsdk.Client;
import com.apv.ocrsdk.ProcessingSettings;
import com.apv.ocrsdk.Task;

public class BBOCR {
	private static Client restClient = new Client();

	public BBOCR() {
		if (!checkAppId()) {
			return;
		}

		ClientSettings.setupProxy();
		restClient.serverUrl = "http://cloud.ocrsdk.com";
		restClient.applicationId = ClientSettings.APPLICATION_ID;
		restClient.password = ClientSettings.PASSWORD;
	}

	public static boolean checkAppId() {
		String appId = ClientSettings.APPLICATION_ID;
		String password = ClientSettings.PASSWORD;
		if (appId == null || password == null || appId.length() < 1
				|| password.length() < 1) {
			System.err
					.println("Error: No application id and password are specified.");
			System.err.println("Please specify them in ClientSettings.java.");
			return false;
		}
		return true;
	}

	public static void performRecognition(byte[] raw) throws Exception {

		Task task = null;
		System.out.println("Uploading file..");
		ProcessingSettings settings = new ProcessingSettings();
		task = restClient.processImage(raw, settings);
		System.out.println("File Uploaded.. Task ID: "
				+ (task != null ? ("Task ID" + task.Id) : "Task is NULL"));
		if (task != null) {
			System.out.println("Going into waitAndDownloadResult");
			waitAndDownloadResult(task);
		}
	}

	private static Task waitForCompletion(Task task) {
		while (task.isTaskActive()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				System.out.println("Exception in thread.sleep: "
						+ e.getMessage());
			}
			System.out.println("Waiting.. for task id: " + task.Id);
			task = restClient.getTaskStatus(task.Id);
		}
		return task;
	}

	private static void waitAndDownloadResult(Task task) {
		task = waitForCompletion(task);

		if (task.Status == Task.TaskStatus[4]) {
			System.out.println("Downloading..");
			restClient.downloadResult(task);
			System.out.println("Ready");
		} else if (task.Status == Task.TaskStatus[7]) {
			System.out.println("Not enough credits to process document. "
					+ "Please add more pages to your application's account.");
		} else {
			System.out.println("Task failed");
		}

	}

}
