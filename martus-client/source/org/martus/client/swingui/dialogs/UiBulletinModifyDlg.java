/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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

package org.martus.client.swingui.dialogs;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.swing.SwingUtilities;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.TopLevelWindowInterface;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentEditorSection;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentInterface;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.clientside.UiLocalization;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.Bulletin.BulletinState;
import org.martus.common.fieldspec.DateRangeInvertedException;
import org.martus.common.fieldspec.DateTooEarlyException;
import org.martus.common.fieldspec.DateTooLateException;
import org.martus.common.fieldspec.RequiredFieldIsBlankException;

abstract public class UiBulletinModifyDlg implements TopLevelWindowInterface
{
	public UiBulletinModifyDlg(Bulletin b, UiMainWindow observerToUse) throws Exception
	{
		setBulletin(b);
		observer = observerToUse;

		ClientBulletinStore store = observerToUse.getApp().getStore();
		Property<String> currentTemplateNameProperty = store.getCurrentFormTemplateNameProperty();
		currentTemplateNameProperty.addListener(new TemplateChangeHandler(observerToUse));

	}

	public UiLocalization getLocalization()
	{
		UiLocalization localization = getMainWindow().getLocalization();
		return localization;
	}
	
	@Override
	public void repaint()
	{
		getSwingFrame().repaint();
	}
	
	protected UiMainWindow getMainWindow()
	{
		return observer;
	}
	
	class WindowEventHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent event)
		{
			try
			{
				closeWindowIfUserConfirms();
			}
			catch (Exception e)
			{
				unexpectedErrorDlg(e);
			}
		}
	}
	
	class LanguageChangeHandler implements BulletinLanguageChangeListener
	{
		@Override
		public void bulletinLanguageHasChanged(String newLanguageCode)
		{
			//TODO add this back when its working correctly
			/*		if(observer.getLocalization().doesLanguageRequirePadding(newLanguage))
						LanguageOptions.setLanguagePaddingRequired();
					else
						LanguageOptions.setLanguagePaddingNotRequired();
					getContentPane().remove(scroller);
					addScrollerView();
			*/
		}
	}
	
	abstract public void dispose();
	abstract public void setVisible(boolean newState);
	
	protected void unexpectedErrorDlg(Exception e)
	{
		observer.unexpectedErrorDlg(e);
	}

	protected void safelyPopulateView()
	{
		try
		{
			getView().copyDataFromBulletin(getBulletin());
			getView().setLanguageChangeListener(new LanguageChangeHandler());
			getView().scrollToTop();
		} 
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	class TemplateChangeHandler implements ChangeListener<String>
	{
		public TemplateChangeHandler(UiMainWindow mainWindowToUse)
		{
			mainWindow = mainWindowToUse;
		}
		
		@Override
		public void changed(ObservableValue<? extends String> currentTemplateName, String oldValue, String newValue)
		{
			try
			{
				MartusLogger.log("Switching Templates:'" + newValue + "'  was ='" + oldValue +"'");
				ClientBulletinStore store = mainWindow.getApp().getStore();
				Bulletin clonedBulletin = createClonedBulletinUsingCurrentTemplate(store);
				SwingUtilities.invokeLater(() -> showBulletin(clonedBulletin));
			} 
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
		}

		private UiMainWindow mainWindow;
	}
	
	public Bulletin createClonedBulletinUsingCurrentTemplate(ClientBulletinStore store) throws Exception
	{
		Bulletin bulletinWithOldTemplateButLatestData = getBulletin();
		getView().copyDataToBulletin(bulletinWithOldTemplateButLatestData);
		Bulletin clonedBulletin = store.createNewDraftWithCurrentTemplateButIdAndDataAndHistoryFrom(bulletinWithOldTemplateButLatestData);
		return clonedBulletin;
	}
	
	public void setBulletin(Bulletin bulletin)
	{
		this.bulletin = bulletin;
	}

	protected Bulletin getBulletin()
	{
		return bulletin;
	}

	protected void showBulletin(Bulletin bulletinToShow)
	{
		setBulletin(bulletinToShow);
		try
		{
			getView().copyDataFromBulletin(getBulletin());
			getView().scrollToTop();
		} 
		catch (Exception e)
		{
			observer.unexpectedErrorDlg(e);
		}
	}
	
	protected boolean validateData()
	{
		try
		{	
			getView().validateData();
			return true;
		}
		catch(UiDateEditor.DateFutureException e)
		{
			observer.messageDlg(getSwingFrame(),"ErrorDateInFuture", e.getlocalizedTag());
		}
		catch(DateRangeInvertedException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			observer.messageDlg("ErrorDateRangeInverted", "", map);
		}
		catch(DateTooEarlyException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			map.put("#MinimumDate#", observer.getLocalization().convertStoredDateToDisplay(e.getMinimumDate()));
			observer.messageDlg("ErrorDateTooEarly", "", map);
		}
		catch(DateTooLateException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			map.put("#MaximumDate#", observer.getLocalization().convertStoredDateToDisplay(e.getMaximumDate()));
			observer.messageDlg("ErrorDateTooLate", "", map);
		}
		catch(UiBulletinComponentEditorSection.AttachmentMissingException e)
		{
			observer.messageDlg(getSwingFrame(), "ErrorAttachmentMissing", e.getlocalizedTag());
		}
		catch(RequiredFieldIsBlankException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			observer.messageDlg("ErrorRequiredFieldBlank", "", map);
		}
		catch (Exception e) 
		{
			observer.unexpectedErrorDlg(e);
		}
		return false;
	}

	public void saveBulletin(boolean neverDeleteFromServer, BulletinState bulletinState)
	{
		Cursor originalCursor = getSwingFrame().getCursor();
		getSwingFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try 
		{
			MartusApp app = observer.getApp();
			ClientBulletinStore store = app.getStore();
			BulletinFolder outboxToUse = null;
			BulletinFolder draftOutbox = store.getFolderDraftOutbox();

			// NOTE: must copyDataToBulletin before setSealed or setDraft
			// NOTE: after copyDataToBulletin, should not allow user to cancel
			getView().copyDataToBulletin(getBulletin());
			getBulletin().changeState(bulletinState);
			
			if(neverDeleteFromServer)
			{
				store.removeBulletinFromFolder(draftOutbox, getBulletin());
				getBulletin().setImmutable();
				outboxToUse = store.getFolderSealedOutbox();
			}
			else
			{
				getBulletin().setMutable();
				outboxToUse = draftOutbox;
			}
			saveBulletinAndUpdateFolders(store, outboxToUse);
			wasBulletinSavedFlag = true;
			cleanupAndExit();
		} 
		catch (Exception e) 
		{
			observer.unexpectedErrorDlg(e);
		} 
		finally 
		{
			getSwingFrame().setCursor(originalCursor);
		}
	}

	private void saveBulletinAndUpdateFolders(ClientBulletinStore store, BulletinFolder outboxToUse) throws Exception
	{
		observer.getApp().saveBulletin(getBulletin(), outboxToUse);

		observer.folderContentsHaveChanged(store.getFolderSaved());
		observer.folderContentsHaveChanged(store.getFolderDiscarded());
		observer.selectBulletinInCurrentFolderIfExists(getBulletin().getUniversalId());
		observer.bulletinContentsHaveChanged(getBulletin());
	}

	public boolean wasBulletinSaved()
	{
		return wasBulletinSavedFlag;
	}
	
	public void restoreFrameState()
	{
		setFrameLocation(observer.getBulletinEditorPosition());
		Dimension bulletinEditorDimension = observer.getBulletinEditorDimension();
		boolean bulletinEditorMaximized = observer.isBulletinEditorMaximized();
		if(!isEditorDimensionsValid(bulletinEditorDimension))
		{
			bulletinEditorDimension = NON_MAXIMIZED_MINIMUM_STARTING_EDITOR_DIMENSIONS;
			bulletinEditorMaximized = true;
		}
		setFrameSize(bulletinEditorDimension);
		setFrameMaximized(bulletinEditorMaximized);
	}

	public boolean isEditorDimensionsValid(Dimension bulletinEditorDimension)
	{
		return( bulletinEditorDimension.getHeight() > MINIMUM_EDITOR_HEIGHT && 
				bulletinEditorDimension.getWidth() > MINIMUM_EDITOR_WIDTH);
	}

	abstract protected void setFrameLocation(Point bulletinEditorPosition);
	abstract protected void setFrameSize(Dimension bulletinEditorDimension);
	abstract protected void setFrameMaximized(boolean bulletinEditorMaximized);

	public void cleanupAndExit()
	{
		observer.doneModifyingBulletin();
		
		observer.setBulletinEditorDimension(getFrameSize());
		observer.setBulletinEditorPosition(getFrameLocation());
		observer.setBulletinEditorMaximized(isFrameMaximized());
		observer.saveState();
		
		dispose();
	}
	
	abstract protected Point getFrameLocation();
	abstract protected Dimension getFrameSize();
	abstract protected boolean isFrameMaximized();

	protected void closeWindowIfUserConfirms()
	{	
		try
		{
			boolean needConfirmation = getView().isBulletinModified();
			if(needConfirmation)
			{
				if(!observer.confirmDlg("CancelModifyBulletin"))
					return;
			}
				
			cleanupAndExit();
		}
		catch (Exception e)
		{
			unexpectedErrorDlg(e);
		}
	}
	
	protected void setView(UiBulletinComponentInterface view)
	{
		this.view = view;
	}

	protected UiBulletinComponentInterface getView()
	{
		return view;
	}

	private static final double MINIMUM_EDITOR_WIDTH = 200.0;
	private static final double MINIMUM_EDITOR_HEIGHT = 200.0;
	private static final Dimension NON_MAXIMIZED_MINIMUM_STARTING_EDITOR_DIMENSIONS = new Dimension(640,480);

	private Bulletin bulletin;
	private UiMainWindow observer;

	private UiBulletinComponentInterface view;
	
	private boolean wasBulletinSavedFlag;
}

