/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
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
package org.martus.client.swingui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javafx.application.Platform;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JViewport;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.UiSession;
import org.martus.client.swingui.WindowObscurer;
import org.martus.client.swingui.bulletincomponent.UiBulletinEditor;
import org.martus.client.swingui.jfx.generic.FxInSwingStage;
import org.martus.client.swingui.jfx.generic.FxRunner;
import org.martus.client.swingui.jfx.landing.bulletins.FxBulletinEditorShellController;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.Bulletin.BulletinState;
import org.martus.swing.UiButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;

public class FxInSwingBulletinModifyDialog extends UiBulletinModifyDlg
{
	public FxInSwingBulletinModifyDialog(Bulletin b, UiMainWindow observerToUse) throws Exception
	{
		super(b, observerToUse);

		realFrame = new JFrame();
		UiMainWindow.updateIcon(getSwingFrame());
		getSwingFrame().setTitle(getLocalization().getWindowTitle("create"));
		
		if(UiSession.isJavaFx())
		{
			FxBulletinEditorShellController bulletinEditorShellController = new FxBulletinEditorShellController(observerToUse, this);

			FxInSwingStage bulletinEditorStage = FxRunner.createAndActivateEmbeddedStage(observerToUse, getSwingFrame(), bulletinEditorShellController);
			setView(bulletinEditorShellController);
			Platform.runLater(() -> safelyPopulateView());
			getSwingFrame().getContentPane().add(bulletinEditorStage.getPanel(), BorderLayout.CENTER);
		}
		else
		{
			setView(new UiBulletinEditor(getMainWindow()));
			getView().copyDataFromBulletin(getBulletin());
			getView().setLanguageChangeListener(new LanguageChangeHandler());

			UiButton send = new UiButton(getLocalization().getButtonLabel("send"));
			send.addActionListener(new SendHandler());
			UiButton draft = new UiButton(getLocalization().getButtonLabel("savedraft"));
			draft.addActionListener(new SaveHandler());
			UiButton cancel = new UiButton(getLocalization().getButtonLabel(EnglishCommonStrings.CANCEL));
			cancel.addActionListener(new CancelHandler());

			addScrollerView();

			Box box = Box.createHorizontalBox();
			Component buttons[] = {send, draft, cancel, Box.createHorizontalGlue()};
			Utilities.addComponentsRespectingOrientation(box, buttons);
			getSwingFrame().getContentPane().add(box, BorderLayout.SOUTH);
		}


		getSwingFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getSwingFrame().addWindowListener(new WindowEventHandler());

		if(!UiSession.isJavaFx())
			getView().scrollToTop();
		
		getSwingFrame().setGlassPane(new WindowObscurer());
		
	}

	public void dispose()
	{
		getSwingFrame().dispose();
	}
	
	public void setVisible(boolean newVisibility)
	{
		getSwingFrame().setVisible(newVisibility);
	}
	
	@Override
	public JFrame getSwingFrame()
	{
		return realFrame;
	}
	
	@Override
	protected void setFrameLocation(Point bulletinEditorPosition)
	{
		getSwingFrame().setLocation(bulletinEditorPosition);
	}

	@Override
	protected void setFrameSize(Dimension bulletinEditorDimension)
	{
		getSwingFrame().setSize(bulletinEditorDimension.width, bulletinEditorDimension.height);
	}

	@Override
	protected void setFrameMaximized(boolean bulletinEditorMaximized)
	{
		if(bulletinEditorMaximized)
		{
			Utilities.maximizeWindow(getSwingFrame());
		}
		else
		{
			int state = getSwingFrame().getExtendedState();
			state &= ~Frame.MAXIMIZED_BOTH;
			getSwingFrame().setExtendedState(state);
		}
			
	}

	@Override
	protected Point getFrameLocation()
	{
		return getSwingFrame().getLocation();
	}
	
	@Override
	protected Dimension getFrameSize() 
	{
		return getSwingFrame().getSize();
	}
	
	@Override
	protected boolean isFrameMaximized()
	{
		return getSwingFrame().getExtendedState() == JFrame.MAXIMIZED_BOTH;
	}
	
	private void addScrollerView() 
	{
		scroller = new UiScrollPane();
		scroller.getVerticalScrollBar().setFocusable(false);
		scroller.getViewport().add(getView().getComponent());
		scroller.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		getSwingFrame().getContentPane().setLayout(new BorderLayout());
		getSwingFrame().getContentPane().add(scroller, BorderLayout.CENTER);
		getSwingFrame().getContentPane().invalidate();
		getSwingFrame().getContentPane().doLayout();
	}
	
	class SaveHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{		
			try
			{
				if(!validateData())
					return;
	
				saveBulletin(false, BulletinState.STATE_SAVE);
			}
			catch (Exception e) 
			{
				getMainWindow().unexpectedErrorDlg(e);
			}
		}
	}

	class SendHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{		
			try
			{
				if(!validateData())
					return;
	
				String tag = "send";
				if (!getMainWindow().confirmDlg(tag))
					return;
													
				saveBulletin(true, BulletinState.STATE_SHARED);
			}
			catch (Exception e) 
			{
				getMainWindow().unexpectedErrorDlg(e);
			}
		}
	}

	class CancelHandler implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent event)
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

	private JFrame realFrame;
	private UiScrollPane scroller;
	
}
