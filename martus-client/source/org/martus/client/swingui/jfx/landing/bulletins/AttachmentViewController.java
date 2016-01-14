/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.client.swingui.jfx.landing.bulletins;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;

import javax.activation.MimetypesFileTypeMap;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.PopupConfirmationWithHideForSessionController;
import org.martus.common.MartusLogger;
import org.martus.common.utilities.GeoTag;
import org.martus.common.utilities.JpegGeoTagReader;
import org.martus.common.utilities.JpegGeoTagReader.NotJpegException;

public class AttachmentViewController extends FxController
{
	public AttachmentViewController(UiMainWindow mainWindowToUse, File attachmentFile)
	{
		super(mainWindowToUse);
		try
		{
			attachmentFileToView = attachmentFile;
			attachmentFileType = determineFileType(attachmentFileToView);
			if(canViewInProgram())
				loadAttachment();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		} 
	}
	
	@Override
	protected Dimension getPreferredDimension()
	{
		// NOTE: This might be asking for the preferred total dimension 
		// of the dialog, not the dimension of this controller's panel
		int imageWidth = (int) attachmentImage.getWidth();
		int imageHeight = (int) attachmentImage.getHeight();
		if(attachmentGeoTag.hasData())
		{
			imageWidth = Integer.max(imageWidth, MAP_WIDTH);
			imageHeight = Integer.max(imageHeight, MAP_HEIGHT);
		}
		
		// FIXME: Instead of fudging here, we should find a way to ask 
		// what the actual dialog size will be...and better yet, change 
		// our dialog launcher to ask what size the contents want to be, 
		// rather than asking the contents how big the dialog should be
		
		// NOTE: I think when we drop Swing, we will be able to have dialogs 
		// automatically size themseives to accommodate their contents. The 
		// problem now is that the swing dialog needs to know the size before 
		// the fx thread has had a chance to initialize the contents.
		int dialogWidth = imageWidth + 100;
		int dialogHeight = imageHeight + 100;
		
		Screen screen = Screen.getPrimary();
		Rectangle2D screenBounds = screen.getVisualBounds();
		double screenWidth = screenBounds.getWidth();
		double screenHeight = screenBounds.getHeight();
		int availableWidth = (int)(screenWidth * 0.9);
		int availableHeight = (int)(screenHeight * 0.9);
		
		dialogWidth = Integer.min(dialogWidth, availableWidth);
		dialogHeight = Integer.min(dialogHeight, availableHeight);
		return new Dimension(dialogWidth, dialogHeight);
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		try
		{
			SimpleBooleanProperty suppressWarningProperty = PopupConfirmationWithHideForSessionController.obtainProperty(confirmationTag);
			doNotShowAgainCheckBox.selectedProperty().bindBidirectional(suppressWarningProperty);
			cancelButton.disableProperty().bind(suppressWarningProperty);
			displayAttachment();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	public boolean canViewInProgram()
	{
		return canViewInProgram(attachmentFileType);
	}

	public static boolean canViewInProgram(FileType fileTypeToView)
	{
		return fileTypeToView != FileType.Unsupported;
	}
	
	public static FileType determineFileType(File file) throws IOException
	{
		String fileName = file.getName();

		return determineFileType(fileName);
	}

	public static FileType determineFileType(String fileName)
	{
		MimetypesFileTypeMap mimeTypeMap = new MimetypesFileTypeMap();
		mimeTypeMap.addMimeTypes("image png tif jpg jpeg bmp");
		String mimetype = mimeTypeMap.getContentType(fileName.toLowerCase());
        String type = mimetype.split("/")[0].toLowerCase();
        if(type.equals("image"))
			return FileType.Image;
		return FileType.Unsupported;
	}

	private void loadAttachment() throws Exception
	{
		attachmentGeoTag = readGeoTag();

		FileInputStream in = new FileInputStream(attachmentFileToView);
		try
		{
			attachmentImage = new Image(in);
		}
		finally
		{
			in.close();
		}
	}

	private void displayAttachment()
	{
		Platform.runLater(() -> {
			attachmentImageView.setImage(attachmentImage);
			Dimension preferredDimension = getPreferredDimension();
			attachmentImageView.fitWidthProperty().set(preferredDimension.getWidth());
			attachmentImageView.fitHeightProperty().set(preferredDimension.getHeight());
			showNode(attachmentPane);

			boolean hasGeoTagData = attachmentGeoTag.hasData();
			showMapButton.setVisible(hasGeoTagData);
			
			boolean isOnline = getApp().getTransport().isOnline();
			showMapButton.setDisable(!isOnline);
		});
	}

	private void showNode(Node nodeToShow)
	{
		ObservableList<Node> children = containerPane.getChildren();
		children.forEach((child) -> child.setVisible(false));
		nodeToShow.setVisible(true);
	}

	private GeoTag readGeoTag() throws Exception
	{
		InputStream in = attachmentFileToView.toURI().toURL().openStream();
		try
		{
			JpegGeoTagReader reader = new JpegGeoTagReader();
			GeoTag tag = reader.readMetadata(in);
			return tag;
		}
		catch(NotJpegException e)
		{
			// NOTE: this is harmless
			return new GeoTag();
		}
		catch(Exception e)
		{
			logAndNotifyUnexpectedError(e);
			return new GeoTag();
		}
		finally
		{
			in.close();
		}
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/AttachmentViewer.fxml";
	}
	
	@FXML
	private void onShowMap()
	{
		try
		{
			if(needToConfirmBypassingTor())
				showNode(torConfirmationPane);
			else
				onConfirmShowMap();

		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
			displayAttachment();
		}
	}

	public boolean needToConfirmBypassingTor()
	{
		if(doNotShowAgainCheckBox.selectedProperty().getValue())
			return false;

		return getApp().getTransport().isTorEnabled();
	}
	
	@FXML
	private void onShowImage()
	{
		displayAttachment();
	}
	
	@FXML
	private void onConfirmShowMap()
	{
		try
		{
			URL mapRequestUrl = createMapRequestUrl();
			Thread thread = new Thread(() -> downloadAndDisplayImage(mapRequestUrl));
			thread.setDaemon(true);
			thread.start();
			
			showNode(workingPane);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
			displayAttachment();
		}
	}

	private void downloadAndDisplayImage(URL mapRequestUrl)
	{
		try
		{
			MartusLogger.log("Map URL: " + mapRequestUrl);
			byte[] imageBytes = readEntireContents(mapRequestUrl);
			
			MartusLogger.log("Image size: " + imageBytes.length);
			Platform.runLater(() -> showMap(imageBytes));
			return;
		} 
		catch(IOException e)
		{
			Platform.runLater(() -> showNotifyDialog("ErrorServerConnection"));
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}

		displayAttachment();
	}

	public void showMap(byte[] imageBytes)
	{
		try
		{
			Image image = createImage(imageBytes);
			mapImageView.setImage(image);
			showNode(mapPane);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
			displayAttachment();
		}
	}

	private Image createImage(byte[] imageBytes) throws IOException
	{
		InputStream imageInputStream = new ByteArrayInputStream(imageBytes);
		Image image = new Image(imageInputStream);
		imageInputStream.close();
		return image;
	}

	private byte[] readEntireContents(URL mapRequestUrl) throws IOException
	{
		HttpsURLConnection huc = createHttpsConnection(mapRequestUrl);
		InputStream in = huc.getInputStream();
		try
		{
			byte[] imageBytes = readEntireContents(in);
			return imageBytes;
		}
		finally
		{
			in.close();
		}
	}

	private HttpsURLConnection createHttpsConnection(URL mapRequestUrl) throws IOException
	{
		URLConnection connection = mapRequestUrl.openConnection();
		HttpsURLConnection huc = (HttpsURLConnection) connection;
		huc.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
		return huc;
	}

	private byte[] readEntireContents(InputStream in) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while(true)
		{
			int b = in.read();
			if(b < 0)
				break;
			baos.write(b);
		}
		baos.close();
		return baos.toByteArray();
	}
	
	private URL createMapRequestUrl() throws Exception
	{
		int zoomFactor = 14;
		GeoTag tag = attachmentGeoTag;
		String baseUrl = "https://maps.googleapis.com/maps/api/staticmap";
		String marker = "markers=%7C" + tag.getLatitude() + "," + tag.getLongitude();
		String size = "size=" + MAP_WIDTH + "x" + MAP_HEIGHT;
		String zoom = "zoom=" + zoomFactor;
		return new URL(baseUrl + "?" + 	marker + "&" + size + "&" + zoom);
	}

	public static boolean canViewAttachmentInProgram(File attachmentFileToView) throws IOException
	{
		FileType fileType = determineFileType(attachmentFileToView);
		return canViewInProgram(fileType);
	}
	
	private static final String confirmationTag = "confirmShowOnMapBypassesTor";
	private static final int MAP_WIDTH = 640;
	private static final int MAP_HEIGHT = 640;

	public static enum FileType{Unsupported, Image};

	@FXML
	private StackPane containerPane;
	
	@FXML
	private StackPane attachmentPane;
	
	@FXML
	private StackPane torConfirmationPane;

	@FXML
	private StackPane workingPane;
	
	@FXML
	private StackPane mapPane;

	@FXML
	private ImageView attachmentImageView;
	
	@FXML
	private ImageView mapImageView;
	
	@FXML
	private Button showMapButton;
	
	@FXML
	private CheckBox doNotShowAgainCheckBox;
	
	@FXML
	private Button cancelButton;

	private File attachmentFileToView;
	private FileType attachmentFileType;
	private Image attachmentImage;
	private GeoTag attachmentGeoTag;
}
