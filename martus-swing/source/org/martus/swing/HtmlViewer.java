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

To the extent this copyrighted software code is used in the 
Miradi project, it is subject to a royalty-free license to 
members of the Conservation Measures Partnership when 
used with the Miradi software as specified in the agreement 
between Benetech and WCS dated 5/1/05.
*/

package org.martus.swing;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.FormView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.StyleSheet;

public class HtmlViewer extends UiEditorPane implements HyperlinkListener
{
	public HtmlViewer(String htmlSource, HyperlinkHandler hyperLinkHandler)
	{
		linkHandler = hyperLinkHandler;
		
		setEditable(false);
		setText(htmlSource);
		addHyperlinkListener(this);
	}
	
	public void setText(String text)
	{
		HTMLEditorKit htmlKit = new OurHtmlEditorKit(linkHandler);
		StyleSheet style = htmlKit.getStyleSheet();
		customizeStyleSheet(style);
		htmlKit.setStyleSheet(style);
		setEditorKit(htmlKit);

		Document doc = htmlKit.createDefaultDocument();
		setDocument(doc);
		
		super.setText(text);
		setCaretPosition(0);
	}


// Removed because this is not compatible with Java 1.4
// Miradi has an improved HtmlFormViewer component that Martus 
// should start using when it goes to Java 1.5 or 1.6
//	public void setFixedWidth( Component component, int width )
//	{
//		component.setSize( new Dimension( width, Short.MAX_VALUE ) );
//		Dimension preferredSize = component.getPreferredSize();
//		component.setPreferredSize( new Dimension( width, preferredSize.height ) );
//	}
	
	protected void customizeStyleSheet(StyleSheet style)
	{
		style.addRule("body {background: #ffffff;}");
	}

	public void hyperlinkUpdate(HyperlinkEvent e)
	{
		if(e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
		{
			String clicked = e.getDescription();
			linkHandler.linkClicked(clicked);
		}

	}
	
	class OurHtmlEditorKit extends HTMLEditorKit
	{
		public OurHtmlEditorKit(HyperlinkHandler handler)
		{
			factory = new OurHtmlViewFactory(handler);
			ourStyleSheet = new StyleSheet();
			ourStyleSheet.addStyleSheet(super.getStyleSheet());
		}
		
		public ViewFactory getViewFactory()
		{
			return factory;
		}

		public StyleSheet getStyleSheet()
		{
			return ourStyleSheet;
		}

		public void setStyleSheet(StyleSheet s)
		{
			ourStyleSheet = s;
		}

		ViewFactory factory;
		StyleSheet ourStyleSheet;
	}
	
	class OurHtmlViewFactory extends HTMLEditorKit.HTMLFactory
	{
		public OurHtmlViewFactory(HyperlinkHandler handlerToUse)
		{
			handler = handlerToUse;
		}
		
		public View create(Element elem)
		{
			if(elem.getName().equals("select"))
			{
				return new OurSelectView(elem, handler);
			}
			if(elem.getName().equals("input"))
			{
				AttributeSet attributes = elem.getAttributes();
				Object typeAttribute = attributes.getAttribute(HTML.Attribute.TYPE);
				if(typeAttribute.equals("submit"))
				{
					return new OurButtonView(elem, handler);
				}
				if(typeAttribute.equals("text"))
				{
					return new OurTexView(elem, handler);
				}
				if(typeAttribute.equals("textarea"))
				{
					return new OurTexView(elem, handler);
				}
			}
			else if(elem.getName().equals("img"))
			{
				return createImageView(elem);
			}
			return super.create(elem);
		}

		private OurImageView createImageView(Element elem)
		{
			return new OurImageView(handler.getClass(), elem);
		}
		
		HyperlinkHandler handler;
	}
	
	class OurButtonView extends FormView
	{
		public OurButtonView(Element elem, HyperlinkHandler handlerToUse)
		{
			super(elem);
			handler = handlerToUse;
		}

		protected void submitData(String data)
		{
			String buttonName = (String)getElement().getAttributes().getAttribute(HTML.Attribute.NAME);
			handler.buttonPressed(buttonName);
		}
		
		HyperlinkHandler handler;
	}
	
	class OurSelectView extends FormView implements ItemListener
	{
		public OurSelectView(Element elem, HyperlinkHandler handlerToUse)
		{
			super(elem);
			handler = handlerToUse;
		}

		protected Component createComponent()
		{
			comboBox = (JComboBox)super.createComponent();
			comboBox.addItemListener(this);
			return comboBox;
		}

		public void itemStateChanged(ItemEvent e)
		{
			String name = (String)getElement().getAttributes().getAttribute(HTML.Attribute.NAME);
			handler.valueChanged(name, comboBox.getSelectedItem().toString());
		}
		
		HyperlinkHandler handler;
		JComboBox comboBox;
	}
	
	
	//TODO: eliminate duplicate code between Views (text, textarea)
	class OurTexView extends FormView implements DocumentListener
	{
		public OurTexView(Element elem, HyperlinkHandler handlerToUse)
		{
			super(elem);
			handler = handlerToUse;
		}

		protected Component createComponent()
		{
			textField = (JTextComponent)super.createComponent();
			textField.getDocument().addDocumentListener(this);
			return textField;
		}

		public void changedUpdate(DocumentEvent event) 
		{
			notifyHandler();
		}


		public void insertUpdate(DocumentEvent event) 
		{
			notifyHandler();
		}

		public void removeUpdate(DocumentEvent event) 
		{
			notifyHandler();
		}
		
		private void notifyHandler() 
		{
			String name = (String)getElement().getAttributes().getAttribute(HTML.Attribute.NAME);
			handler.valueChanged(name, textField.getText());
		}
		
		protected void submitData(String data)
		{
		}
		
		HyperlinkHandler handler;
		JTextComponent textField;

	}

	class OurImageView extends ImageView
	{
		public OurImageView(Class handlerClassToUse, Element elem)
		{
			super(elem);
			handlerClass = handlerClassToUse;
			name = (String)elem.getAttributes().getAttribute(HTML.Attribute.SRC);
		}

		public Image getImage()
		{
			if(image == null)
			{
				try
				{
					URL url = handlerClass.getResource("/" + name);
					ImageIcon icon = new ImageIcon(url);
					image = icon.getImage();
				}
				catch(NullPointerException e)
				{
					throw new RuntimeException(name, e);
				}
			}
			return image;
		}
		
		String name;
		Class handlerClass;
		Image image;
	}
	
	HyperlinkHandler linkHandler;
}