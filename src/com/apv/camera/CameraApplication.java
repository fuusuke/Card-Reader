/*
 * CameraDemo.java
 *
 * Copyright © 1998-2012 Research In Motion Ltd.
 * 
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package com.apv.camera;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.amms.control.camera.ZoomControl;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import net.rim.device.api.amms.control.camera.EnhancedFocusControl;
import net.rim.device.api.command.Command;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.PNGEncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.input.InputSettings;
import net.rim.device.api.ui.input.NavigationDeviceSettings;
import net.rim.device.api.ui.menu.SubMenu;
import net.rim.device.api.util.StringProvider;
import net.rim.device.api.util.StringUtilities;

import com.apv.http.OCR;

/**
 * A sample application used to demonstrate the VideoControl.getSnapshot()
 * method. This application can take snapshots using the BlackBerry device's
 * camera. Swiping the trackpad in a north or south direction will zoom the
 * viewfinder in and out.
 */
public final class CameraApplication extends UiApplication {
	/**
	 * Entry point for application
	 * 
	 * @param args
	 *            Command line arguments (not used)
	 */
	public static void main(String[] args) {
		// Create a new instance of the application and make the currently
		// running thread the application's event dispatch thread.
		CameraApplication demo = new CameraApplication();
		demo.enterEventDispatcher();
	}

	/**
	 * Creates a new CameraDemo object
	 */
	public CameraApplication() {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Dialog.alert("Click the trackpad or tap the screen to take a picture.");
			}
		});
		CameraScreen screen = new CameraScreen();
		pushScreen(screen);
	}

	/**
	 * Presents a dialog to the user with a given message
	 * 
	 * @param message
	 *            The text to display
	 */
	public static void errorDialog(final String message) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Dialog.alert(message);
			}
		});
	}
}

/**
 * A UI screen to display the camera display and buttons
 */
