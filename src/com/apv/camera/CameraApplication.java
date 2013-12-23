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
final class CameraScreen extends MainScreen {
	private VideoControl _videoControl;
	private Field _videoField;
	private EncodingProperties[] _encodings;
	private EnhancedFocusControl _efc;
	private ZoomControl _zoomControl;
	private Player _player;
	private MenuItem _turnOffAutoFocusMenuItem;
	private MenuItem _turnOnAutoFocusMenuItem;

	private int _indexOfEncoding = 0;

	/**
	 * Creates a new CameraScreen object
	 */
	public CameraScreen() {
		// Set the title of the screen
		setTitle("Camera Demo");

		// Initialize the camera object and video field
		initializeCamera();

		// Initialize the list of possible encodings
		initializeEncodingList();

		// If the field was constructed successfully, create the UI
		if (_videoField != null) {
			// Add the video field to the screen
			add(_videoField);

			// Initialize the camera features menus

			_turnOffAutoFocusMenuItem = new MenuItem(new StringProvider(
					"Turn Off Auto-Focus"), 0x230020, 0);
			_turnOffAutoFocusMenuItem.setCommand(new Command(
					new CommandHandler() {
						/**
						 * @see CommandHandler#execute(ReadOnlyCommandMetadata,
						 *      Object)
						 */
						public void execute(ReadOnlyCommandMetadata metadata,
								Object context) {
							try {
								if (_efc != null) {
									_efc.stopAutoFocus();
								} else {
									CameraApplication
											.errorDialog("ERROR: Focus control not initialized.");
								}
							} catch (Exception e) {
								CameraApplication.errorDialog("ERROR "
										+ e.getClass() + ":  " + e.getMessage());
							}
						}
					}));

			_turnOnAutoFocusMenuItem = new MenuItem(new StringProvider(
					"Turn on Auto-Focus"), 0x230020, 0);
			_turnOnAutoFocusMenuItem.setCommand(new Command(
					new CommandHandler() {
						/**
						 * @see CommandHandler#execute(ReadOnlyCommandMetadata,
						 *      Object)
						 */
						public void execute(ReadOnlyCommandMetadata metadata,
								Object context) {
							try {
								if (_efc != null) {
									_efc.startAutoFocus();
								} else {
									CameraApplication
											.errorDialog("ERROR: Focus control not initialized.");
								}
							} catch (Exception e) {
								CameraApplication.errorDialog("ERROR "
										+ e.getClass() + ":  " + e.getMessage());
							}
						}
					}));

			MenuItem encodingMenuItem = new MenuItem(new StringProvider(
					"Encoding Settings"), 0x230010, 0);
			encodingMenuItem.setCommand(new Command(new CommandHandler() {
				/**
				 * @see CommandHandler#execute(ReadOnlyCommandMetadata, Object)
				 */
				public void execute(ReadOnlyCommandMetadata metadata,
						Object context) {
					EncodingPropertiesScreen encodingPropertiesScreen = new EncodingPropertiesScreen(
							_encodings, CameraScreen.this, _indexOfEncoding);
					UiApplication.getUiApplication().pushModalScreen(
							encodingPropertiesScreen);
				}
			}));

			addMenuItem(encodingMenuItem);

			// Allow the screen to capture trackpad swipes
			InputSettings settings = NavigationDeviceSettings.createEmptySet();
			settings.set(NavigationDeviceSettings.DETECT_SWIPE, 1);
			addInputSettings(settings);
		}
		// If not, display an error message to the user
		else {
			add(new RichTextField("Error connecting to camera."));
		}
	}

	/**
	 * @see net.rim.device.api.ui.Screen#makeMenu(Menu, int)
	 */
	protected void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);

		if (_efc.isAutoFocusLocked()) {
			menu.add(_turnOffAutoFocusMenuItem);
		} else {
			menu.add(_turnOnAutoFocusMenuItem);
		}
	}

	/**
	 * @see net.rim.device.api.ui.Field#touchEvent(TouchEvent)
	 */
	protected boolean touchEvent(TouchEvent event) {
		if (event.getEvent() == TouchEvent.GESTURE) {
			TouchGesture gesture = event.getGesture();

			// Handle only trackpad swipe gestures
			if (gesture.getEvent() == TouchGesture.NAVIGATION_SWIPE) {
				final int direction = gesture.getSwipeDirection();

				UiApplication.getApplication().invokeLater(new Runnable() {
					public void run() {
						// Determine the direction of the swipe
						if (direction == TouchGesture.SWIPE_NORTH) {
							_zoomControl.setDigitalZoom(ZoomControl.NEXT);
						} else if (direction == TouchGesture.SWIPE_SOUTH) {
							_zoomControl.setDigitalZoom(ZoomControl.PREVIOUS);
						}
					}
				});

				return true;
			}
		}

		return false;
	}

	/**
	 * This method allows an array of menu items to be added to the submenu
	 * which then gets added to the parent menu.
	 * 
	 * @param items
	 *            The array of menu items that represents the submenu
	 * @param menuTitle
	 *            The text string of parent menu item that will contain the
	 *            submenu items
	 * @param ordering
	 *            Ordering of the submenu relative to other items in the parent
	 *            menu
	 */
	private void addSubMenu(Vector items, String menuTitle, int ordering) {
		int size = items.size();

		if (size > 0) {
			SubMenu subMenu = new SubMenu(null, menuTitle, ordering,
					Integer.MAX_VALUE);

			for (int i = size - 1; i >= 0; --i) {
				Object obj = items.elementAt(i);
				if (obj instanceof MenuItem) {
					subMenu.add((MenuItem) obj);
				}
			}

			addMenuItem(subMenu.getMenuItem());
		}
	}

	/**
	 * Builds the menu items for the various focus modes supported on the
	 * device.
	 */
	// private void buildFocusModeMenuItems() {}

	/**
	 * Builds the menu items for the various scene modes supported on the device
	 */

	private static final String PHOTO_DIR = System
			.getProperty("fileconn.dir.photos");
	private static final String EXTENSION = ".bmp";
	private String imageName = "myImage";

	/**
	 * Takes a picture with the selected encoding settings
	 */
	private void takePicture() {
		try {
			// A null encoding indicates that the camera should
			// use the default snapshot encoding.
			String encoding = null;

			if (_encodings != null && _encodings.length > 0) {
				// Use the user-selected encoding
				encoding = _encodings[_indexOfEncoding].getFullEncoding();
			}

			// Retrieve the raw image from the VideoControl and
			// create a screen to display the image to the user.
			final byte[] snapshot = _videoControl.getSnapshot(encoding);

			// Save Image to a file

			SaveStatus saveStatus = saveImageToFile(snapshot);

			createImageScreen(snapshot);
		} catch (Exception e) {
			CameraApplication.errorDialog("ERROR " + e.getClass() + ":  "
					+ e.getMessage());
		}
	}

	private SaveStatus saveImageToFile(byte[] snapshot) {
		try {
			String filePath = PHOTO_DIR + imageName + "_"
					+ System.currentTimeMillis() + EXTENSION;
			FileConnection fconn = (FileConnection) Connector.open(filePath);
			if (!fconn.exists())
				fconn.create();

			Bitmap bitmapImage = Bitmap.createBitmapFromBytes(snapshot, 0, -1,
					1);
			OutputStream out = fconn.openOutputStream();
			PNGEncodedImage encodedImage = PNGEncodedImage.encode(bitmapImage);
			byte[] imageBytes = encodedImage.getData();
			out.write(imageBytes);
			out.close();
			fconn.close();
			System.out.println("Image Created");
			OCR.errorDialog("Image Saved");
			new OCR(imageBytes);
			return new SaveStatus(true, filePath, "Image is Saved.");
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
		}
		return new SaveStatus(false, null, "Image not Saved.");
	}

	class SaveStatus {
		public SaveStatus(boolean isSaved, String filePath, String message) {
			this.isSaved = isSaved;
			this.filePath = filePath;
			this.message = message;
		}

		public boolean isSaved = false;
		public String filePath = "";
		public String message = "";
	}

	/**
	 * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
	 */
	protected boolean onSavePrompt() {
		// Prevent the save dialog from being displayed
		return true;
	}

	/**
	 * @see net.rim.device.api.ui.Screen#close()
	 */
	public void close() {
		if (_player != null) {
			try {
				_player.close();
			} catch (Exception e) {
			}
		}

		super.close();
	}

	/**
	 * Initializes the Player, VideoControl and VideoField
	 */
	private void initializeCamera() {
		try {
			// Create a player for the Blackberry's camera
			_player = Manager.createPlayer("capture://video");

			// Set the player to the REALIZED state (see Player javadoc)
			_player.realize();

			// Get the video control
			_videoControl = (VideoControl) _player.getControl("VideoControl");

			if (_videoControl != null) {
				// Create the video field as a GUI primitive (as opposed to a
				// direct video, which can only be used on platforms with
				// LCDUI support.)
				_videoField = (Field) _videoControl.initDisplayMode(
						VideoControl.USE_GUI_PRIMITIVE,
						"net.rim.device.api.ui.Field");
				_videoControl.setDisplayFullScreen(true);
				_videoControl.setVisible(true);
			}

			// Set the player to the STARTED state (see Player javadoc)
			_player.start();

			// Enable auto-focus for the camera
			_efc = (EnhancedFocusControl) _player
					.getControl("net.rim.device.api.amms.control.camera.EnhancedFocusControl");

			// Enable zoom for the camera
			_zoomControl = (ZoomControl) _player
					.getControl("javax.microedition.amms.control.camera.ZoomControl");
		} catch (Exception e) {
			CameraApplication.errorDialog("ERROR " + e.getClass() + ":  "
					+ e.getMessage());
		}
	}

	/**
	 * Initialize the list of encodings
	 */
	private void initializeEncodingList() {
		try {
			// Retrieve the list of valid encodings
			String encodingString = System
					.getProperty("video.snapshot.encodings");

			// Extract the properties as an array of word
			String[] properties = StringUtilities
					.stringToKeywords(encodingString);

			// The list of encodings
			Vector encodingList = new Vector();

			// Strings representing the three properties of an encoding as
			// returned by System.getProperty().
			String encoding = "encoding";
			String width = "width";
			String height = "height";

			EncodingProperties temp = null;

			for (int i = 0; i < properties.length; ++i) {
				if (properties[i].equals(encoding)) {
					if (temp != null && temp.isComplete()) {
						// Add a new encoding to the list if it
						// has been properly set.
						encodingList.addElement(temp);
					}
					temp = new EncodingProperties();

					// Set the new encoding's format
					++i;
					temp.setFormat(properties[i]);
				} else if (properties[i].equals(width)) {
					// Set the new encoding's width
					++i;
					temp.setWidth(properties[i]);
				} else if (properties[i].equals(height)) {
					// Set the new encoding's height
					++i;
					temp.setHeight(properties[i]);
				}
			}

			// If there is a leftover complete encoding, add it
			if (temp != null && temp.isComplete()) {
				encodingList.addElement(temp);
			}

			// Convert the Vector to an array for later use
			_encodings = new EncodingProperties[encodingList.size()];
			encodingList.copyInto((Object[]) _encodings);
		} catch (Exception e) {
			// Something is wrong, indicate that there are no encoding options
			_encodings = null;
			CameraApplication.errorDialog(e.toString());
		}
	}

	/**
	 * Create a screen used to display a snapshot
	 * 
	 * @param raw
	 *            A byte array representing an image
	 */
	private void createImageScreen(byte[] raw) {
		// Create image to be displayed
		EncodedImage encodedImage = EncodedImage.createEncodedImage(raw, 0,
				raw.length);

		// Initialize the screen
		ImageScreen imageScreen = new ImageScreen(raw, encodedImage);

		Bitmap bitmap = encodedImage.getBitmap();

		// Push screen to display it to the user
		UiApplication.getUiApplication().pushScreen(imageScreen);

	}

	/**
	 * Sets the index of the encoding in the 'encodingList' Vector
	 * 
	 * @param index
	 *            The index of the encoding in the 'encodingList' Vector
	 */
	public void setIndexOfEncoding(int index) {
		_indexOfEncoding = index;
	}

	/**
	 * @see net.rim.device.api.ui.Screen#invokeAction(int)
	 */
	protected boolean invokeAction(int action) {
		boolean handled = super.invokeAction(action);

		if (!handled) {
			if (action == ACTION_INVOKE) {
				takePicture();
				return true;
			}
		}

		return handled;
	}
}
