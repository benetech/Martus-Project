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

package org.martus.client.swingui.jfx.generic.controls;

import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/*
 * NOTE: This class was originally inspired by the code here:
 * https://javafx-demos.googlecode.com/svn-history/r13/trunk/javafx-demos/
 *   src/main/java/com/ezest/javafx/components/freetextfield/ScrollFreeTextArea.java
 */
public class ScrollFreeTextArea extends StackPane
{
	public ScrollFreeTextArea()
	{
		super();
		configure();
	}

	public ScrollFreeTextArea(String text)
	{
		super();
		configure();
		textArea.setText(text);
	}

	public String getText()
	{
		return textProperty().getValue();
	}

	public StringProperty textProperty()
	{
		return textArea.textProperty();
	}

	private void configure()
	{
		setAlignment(Pos.TOP_LEFT);
		// NOTE: Original code set the padding. Doesn't seem to help
//		setPadding(new Insets(4,7,7,7));
		getStyleClass().add("scroll-free-text-area-container");
		
		textArea = new TextAreaWithBetterTabHandling();
		textArea.setWrapText(true);
		textArea.getStyleClass().add("scroll-free-text-area");
		textArea.getChildrenUnmodifiable().addListener(new ScrollPaneBeingAddedListener());
		
		text = new Text();
		text.textProperty().bind(textArea.textProperty());
		text.fontProperty().bind(textArea.fontProperty());
		flow = new TextFlow(text);
		flow.prefWidthProperty().bind(textArea.widthProperty());
		flow.setPadding(getInsetsToRoughlyMatchTextArea());
		flow.setVisible(false);
		
		textArea.prefHeightProperty().bind(flow.heightProperty());
		// NOTE: Binding minHeight forces the box to grow but never shrink, 
		// which is an improvement over the inaccurate shrinking it was doing
		textArea.minHeightProperty().bind(textArea.prefHeightProperty());
		textArea.maxHeightProperty().bind(textArea.prefHeightProperty());

		getChildren().addAll(flow, textArea);
	}

	public Insets getInsetsToRoughlyMatchTextArea()
	{
		// FIXME: These insets should match TextArea, but 
		// so far are just rough guesses
		int topInset = 8;
		int bottomInset = topInset;
		int leftInset = 0;
		int rightInset = leftInset;
		return new Insets(topInset, rightInset, bottomInset, leftInset);
	}
	
	class ScrollPaneBeingAddedListener implements ListChangeListener<Node>
	{
		@Override
		public void onChanged(javafx.collections.ListChangeListener.Change<? extends Node> change)
		{
			while(change.next())
			{
				if(!change.wasAdded())
					continue;
				
				List<? extends Node> addedItems = change.getAddedSubList();
				addedItems.forEach((child) -> grabScrollPane(child));
			}
		}
		
		private void grabScrollPane(Node child)
		{
			boolean isScrollPane = child instanceof ScrollPane;
			if(!isScrollPane)
				return;
			
			ScrollPane scrollPane = (ScrollPane) child;
			scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		}
	}

	private TextFlow flow;
	private Text text;
	private TextArea textArea;
}
