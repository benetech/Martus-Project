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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.BulletinLanguageChangeListener;
import org.martus.client.core.MartusApp;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiSession;
import org.martus.client.swingui.WindowObscurer;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentEditorSection;
import org.martus.client.swingui.bulletincomponent.UiBulletinComponentInterface;
import org.martus.client.swingui.bulletincomponent.UiBulletinEditor;
import org.martus.client.swingui.fields.UiDateEditor;
import org.martus.client.swingui.jfx.generic.FxRunner;
import org.martus.client.swingui.jfx.landing.bulletins.FxBulletinEditorShellController;
import org.martus.client.swingui.jfx.landing.bulletins.FxGenericStage;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.Bulletin.BulletinState;
import org.martus.common.fieldspec.DateRangeInvertedException;
import org.martus.common.fieldspec.DateTooEarlyException;
import org.martus.common.fieldspec.DateTooLateException;
import org.martus.common.fieldspec.RequiredFieldIsBlankException;
import org.martus.swing.UiButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;

public class UiBulletinModifyDlg extends JFrame implements ActionListener, WindowListener, BulletinLanguageChangeListener
{
	public UiBulletinModifyDlg(Bulletin b, UiMainWindow observerToUse) throws Exception
	{
		observer = observerToUse;
		UiLocalization localization = observer.getLocalization();
		setTitle(localization.getWindowTitle("create"));
		UiMainWindow.updateIcon(this);
		bulletin = b;

		if(UiSession.isJavaFx)
		{
			FxBulletinEditorShellController bulletinEditorShellController = new FxBulletinEditorShellController(observerToUse, this);

			String cssName = "Bulletin.css";
			bulletinEditorStage = FxRunner.createAndActivateEmbeddedStage(observerToUse, this, bulletinEditorShellController, cssName);
			view = bulletinEditorShellController;
			Platform.runLater(() -> safelyPopulateView());
		}
		else
		{
			view = new UiBulletinEditor(observer);
			view.copyDataFromBulletin(bulletin);
			view.setLanguageChangeListener(this);
		}

		send = new UiButton(localization.getButtonLabel("send"));
		send.addActionListener(this);
		draft = new UiButton(localization.getButtonLabel("savedraft"));
		draft.addActionListener(this);
		cancel = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancel.addActionListener(this);

		if(UiSession.isJavaFx)
		{
			getContentPane().add(bulletinEditorStage, BorderLayout.CENTER);
		}
		else
		{
			addScrollerView();

			Box box = Box.createHorizontalBox();
			Component buttons[] = {send, draft, cancel, Box.createHorizontalGlue()};
			Utilities.addComponentsRespectingOrientation(box, buttons);
			getContentPane().add(box, BorderLayout.SOUTH);
		}

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);

		Dimension screenSize = Utilities.getViewableScreenSize();
		Dimension editorDimension = observerToUse.getBulletinEditorDimension();
		Point editorPosition = observerToUse.getBulletinEditorPosition();
		boolean showMaximized = false;
		if(Utilities.isValidScreenPosition(screenSize, editorDimension, editorPosition))
		{
			setLocation(editorPosition);
			setSize(editorDimension);
			if(observerToUse.isBulletinEditorMaximized())
				showMaximized = true;
		}
		else
			showMaximized = true;
		if(showMaximized)
		{
			setSize(screenSize.width - 50, screenSize.height - 50);
			Utilities.maximizeWindow(this);
		}
		
		if(!UiSession.isJavaFx)
			view.scrollToTop();
		
		setGlassPane(new WindowObscurer());
		
		ClientBulletinStore store = observerToUse.getApp().getStore();
		Property<String> currentTemplateNameProperty = store.getCurrentFormTemplateNameProperty();
		currentTemplateNameProperty.addListener(new TemplateChangeHandler(observerToUse));
	}

	private void safelyPopulateView()
	{
		try
		{
			view.copyDataFromBulletin(bulletin);
			view.setLanguageChangeListener(this);
			view.scrollToTop();
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
				ClientBulletinStore store = mainWindow.getApp().getStore();
				Bulletin clonedBulletin = createClonedBulletinUsingCurrentTemplate(store);
				SwingUtilities.invokeLater(() -> showBulletin(clonedBulletin));
			} 
			catch (Exception e)
			{
				
			}
		}

		private UiMainWindow mainWindow;
	}
	
	public Bulletin createClonedBulletinUsingCurrentTemplate(ClientBulletinStore store) throws Exception
	{
		Bulletin bulletinWithOldTemplateButLatestData = getCurrentBulletin();
		view.copyDataToBulletin(bulletinWithOldTemplateButLatestData);
		Bulletin clonedBulletin = store.createNewDraftWithCurrentTemplateButDataFrom(bulletinWithOldTemplateButLatestData);
		return clonedBulletin;
	}

	protected Bulletin getCurrentBulletin()
	{
		return bulletin;
	}

	protected void showBulletin(Bulletin bulletinToShow)
	{
		bulletin = bulletinToShow;
		try
		{
			view.copyDataFromBulletin(bulletin);
			view.scrollToTop();
		} 
		catch (Exception e)
		{
			observer.unexpectedErrorDlg(e);
		}
	}
	
	private void addScrollerView() 
	{
		scroller = new UiScrollPane();
		scroller.getVerticalScrollBar().setFocusable(false);
		scroller.getViewport().add(view.getComponent());
		scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(scroller, BorderLayout.CENTER);
		getContentPane().invalidate();
		getContentPane().doLayout();
	}

	public void actionPerformed(ActionEvent ae)
	{		
		try
		{
			if(ae.getSource() == cancel)
			{				
				closeWindowIfUserConfirms();
				return;
			}	
	
			if(!validateData())
				return;

			boolean userChoseSeal = (ae.getSource() == send);
			
			BulletinState state =  BulletinState.STATE_SAVE;
			if(userChoseSeal)
			{
				String tag = "send";
					
				if (!observer.confirmDlg(this, tag))
					return;
				state = BulletinState.STATE_SHARED;
			}
												
			saveBulletin(userChoseSeal, state);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			observer.notifyDlg(this, "UnexpectedError");
		}
	}

	private boolean validateData()
	{
		try
		{	
			view.validateData();
			return true;
		}
		catch(UiDateEditor.DateFutureException e)
		{
			observer.messageDlg(this,"ErrorDateInFuture", e.getlocalizedTag());
		}
		catch(DateRangeInvertedException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			observer.messageDlg(this, "ErrorDateRangeInverted", "", map);
		}
		catch(DateTooEarlyException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			map.put("#MinimumDate#", observer.getLocalization().convertStoredDateToDisplay(e.getMinimumDate()));
			observer.messageDlg(this, "ErrorDateTooEarly", "", map);
		}
		catch(DateTooLateException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			map.put("#MaximumDate#", observer.getLocalization().convertStoredDateToDisplay(e.getMaximumDate()));
			observer.messageDlg(this, "ErrorDateTooLate", "", map);
		}
		catch(UiBulletinComponentEditorSection.AttachmentMissingException e)
		{
			observer.messageDlg(this,"ErrorAttachmentMissing", e.getlocalizedTag());
		}
		catch(RequiredFieldIsBlankException e)
		{
			HashMap map = new HashMap();
			map.put("#FieldLabel#", e.getFieldLabel());
			observer.messageDlg(this, "ErrorRequiredFieldBlank", "", map);
		}
		catch (Exception e) 
		{
			MartusLogger.logException(e);
			observer.notifyDlg(this, "UnexpectedError");
		}
		return false;
	}

	public void saveBulletin(boolean neverDeleteFromServer, BulletinState bulletinState)
	{
		Cursor originalCursor = getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try 
		{
			MartusApp app = observer.getApp();
			ClientBulletinStore store = app.getStore();
			BulletinFolder outboxToUse = null;
			BulletinFolder draftOutbox = store.getFolderDraftOutbox();

			// NOTE: must copyDataToBulletin before setSealed or setDraft
			// NOTE: after copyDataToBulletin, should not allow user to cancel
			view.copyDataToBulletin(bulletin);
			bulletin.changeState(bulletinState);
			
			if(neverDeleteFromServer)
			{
				store.removeBulletinFromFolder(draftOutbox, bulletin);
				bulletin.setImmutable();
				outboxToUse = store.getFolderSealedOutbox();
			}
			else
			{
				bulletin.setMutable();
				outboxToUse = draftOutbox;
			}
			saveBulletinAndUpdateFolders(store, outboxToUse);
			wasBulletinSavedFlag = true;
			cleanupAndExit();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			observer.notifyDlg(this, "ErrorSavingBulletin");
		} 
		finally 
		{
			setCursor(originalCursor);
		}
	}

	private void saveBulletinAndUpdateFolders(ClientBulletinStore store, BulletinFolder outboxToUse) throws Exception
	{
		observer.getApp().saveBulletin(bulletin, outboxToUse);

		observer.folderContentsHaveChanged(store.getFolderSaved());
		observer.folderContentsHaveChanged(store.getFolderDiscarded());
		observer.selectBulletinInCurrentFolderIfExists(bulletin.getUniversalId());
		observer.bulletinContentsHaveChanged(bulletin);
	}

	public boolean wasBulletinSaved()
	{
		return wasBulletinSavedFlag;
	}

	// WindowListener interface
	public void windowActivated(WindowEvent event) {}
	public void windowClosed(WindowEvent event) {}
	public void windowDeactivated(WindowEvent event) {}
	public void windowDeiconified(WindowEvent event) {}
	public void windowIconified(WindowEvent event) {}
	public void windowOpened(WindowEvent event) {}

	public void windowClosing(WindowEvent event)
	{
		try
		{
			closeWindowIfUserConfirms();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			observer.notifyDlg(this, "UnexpectedError");
		}
	}
	// end WindowListener interface


	public void bulletinLanguageHasChanged(String newLanguage) 
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
	
	public void cleanupAndExit()
	{
		observer.doneModifyingBulletin();
		saveEditorState(getSize(), getLocation());
		dispose();
	}

	public void saveEditorState(Dimension size, Point location)
	{
		boolean maximized = getExtendedState() == MAXIMIZED_BOTH;
		observer.setBulletinEditorDimension(size);
		observer.setBulletinEditorPosition(location);
		observer.setBulletinEditorMaximized(maximized);
		observer.saveState();
	}

	private void closeWindowIfUserConfirms() throws Exception
	{	
		boolean needConfirmation = view.isBulletinModified();
		if(needConfirmation)
		{
			if(!observer.confirmDlg(this, "CancelModifyBulletin"))
				return;
		}
			
		cleanupAndExit();
	}
	
	private Bulletin bulletin;
	private UiMainWindow observer;

	private UiBulletinComponentInterface view;
	private UiScrollPane scroller;
	private FxGenericStage bulletinEditorStage;
	
	private JButton send;
	private JButton draft;
	private JButton cancel;

	private boolean wasBulletinSavedFlag;
}

