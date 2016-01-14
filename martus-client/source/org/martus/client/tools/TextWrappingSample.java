/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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

package org.martus.client.tools;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.martus.swing.UiWrappedTextPanel;


public class TextWrappingSample extends JFrame
{
    /** Creates new form TestWrappingTest */
  public TextWrappingSample() 
  {
	  setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      String message =
          " When in the Course of human events, it becomes necessary " +
          "for one people to dissolve the political bands which have " +
          "connected them with another, and to assume among the powers " +
          "of the earth, the separate and equal station to which the " +
          "Laws of Nature and of Nature's God entitle them, a decent " +
          "respect to the opinions of mankind requires that they should " +
          "declare the causes which impel them to the separation.";

	  UiWrappedTextPanel textComponent = new UiWrappedTextPanel(message);
	  getContentPane().setLayout(new BorderLayout());

	  boolean doWhatWorks = false;
	  
	  if(doWhatWorks)
	  {
	      getContentPane().add(textComponent, BorderLayout.CENTER);
	  }
	  else
	  {
		  JPanel between = new JPanel();
		  between.setLayout(new BorderLayout());
		  between.add(textComponent, BorderLayout.CENTER);
	      getContentPane().add(between, BorderLayout.CENTER);
	  }
	  pack();
  }
  
  public static void main(String args[]) {
      java.awt.EventQueue.invokeLater(new Runnable() {
          public void run() {
              new TextWrappingSample().setVisible(true);
          }
      });
  }
}

