/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2011, Beneficent
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
package org.martus.client.swingui.fields;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.martus.client.search.SearchFieldTreeNode;
import org.martus.client.swingui.fields.UiPopUpFieldChooserEditor.BlankLeafRenderer;
import org.martus.client.swingui.fields.UiPopUpFieldChooserEditor.SearchFieldTree;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MiniLocalization;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.PopUpTreeFieldSpec;
import org.martus.common.fieldspec.SearchableFieldChoiceItem;
import org.martus.swing.UiButton;
import org.martus.swing.UiScrollPane;
import org.martus.swing.Utilities;

public class FieldTreeDialog extends JDialog implements TreeSelectionListener
{
	public FieldTreeDialog(JDialog owner, Point location, PopUpTreeFieldSpec specToUse, MiniLocalization localization)
	{
		super(owner);
		spec = specToUse;
		
		setTitle(localization.getButtonLabel("PopUpTreeChoose"));
		setLocation(location);
		
		okAction = new OkAction(localization.getButtonLabel(EnglishCommonStrings.OK));

		tree = new SearchFieldTree(spec.getModel());
		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addMouseListener(new MouseHandler());
		tree.addKeyListener(new KeyHandler());
		tree.addTreeSelectionListener(this);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new BlankLeafRenderer());
		
		okButton = new UiButton(okAction);
		cancelButton = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancelButton.addActionListener(new CancelButtonHandler());
		createButtonBox(localization);
		
		setModal(true);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(new UiScrollPane(tree), BorderLayout.CENTER);
		contentPane.add(buttonBox, BorderLayout.AFTER_LAST_LINE);
		pack();
		Utilities.fitInScreen(this);

		getRootPane().setDefaultButton(okButton);
	}

	private void createButtonBox(MiniLocalization localization)
	{
		buttonBox = Box.createHorizontalBox();
		Component[] components = getButtonBoxComponents(localization);
		Utilities.addComponentsRespectingOrientation(buttonBox, components);
	}

	protected Component[] getButtonBoxComponents(MiniLocalization localization)
	{
		return new Component[] {Box.createHorizontalGlue(), okButton, cancelButton};
	}
	
	public PopUpTreeFieldSpec getSpec()
	{
		return spec;
	}
	
	public void selectCode(String code)
	{
		tree.selectNodeContainingItem(spec.findCode(code));
	}
	
	public DefaultMutableTreeNode getSelectedNode()
	{
		return selectedNode;
	}
	
	public FieldSpec getSelectedSpec()
	{
		if(getSelectionIfAny() == null)
			return null;
		
		SearchableFieldChoiceItem selectedChoiceItem = (SearchableFieldChoiceItem)getSelectionIfAny().getUserObject();
		if(selectedChoiceItem == null)
			return null;
		
		return selectedChoiceItem.getSpec();
	}
	
	protected void saveAndExitIfValidSelection()
	{
		if(!isValidSelection())
			return;
		if(!canSaveAndExit(getSelectedSpec()))
			return;
		selectedNode = getSelectionIfAny();
		dispose();
	}

	protected boolean canSaveAndExit(FieldSpec selectedSpec)
	{
		return true;
	}

	protected boolean isValidSelection()
	{
		return isSelectionValid();
	}

	SearchFieldTreeNode getSelectionIfAny()
	{
		TreePath selectedPath = tree.getSelectionPath();
		if(selectedPath == null)
			return null;
		SearchFieldTreeNode node = (SearchFieldTreeNode)selectedPath.getLastPathComponent();
		if(node == null)
			return null;
		if(!node.isSelectable())
			return null;
		return node;
	}
	
	boolean isSelectionValid()
	{
		return (getSelectionIfAny() != null);
	}
	
	public void valueChanged(TreeSelectionEvent e)
	{
		okAction.setEnabled(isValidSelection());
		updateScrollerPosition();
	}

	//Java Bug, remove once we upgrade to Java 1.5 (Fixed post Java 1.4.2)
	private void updateScrollerPosition()
	{
		int rows[] = tree.getSelectionRows();
		if(rows != null && rows.length>0)
			tree.scrollRowToVisible(rows[0]);
	}
	
	class OkAction extends AbstractAction
	{
		public OkAction(String label)
		{
			super(label);
		}

		public void actionPerformed(ActionEvent e)
		{
			saveAndExitIfValidSelection();
		}
		
	}
	
	class CancelButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
		
	}
	
	class MouseHandler implements MouseListener
	{
		public void mouseClicked(MouseEvent e)
		{
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}
		
		public void mousePressed(MouseEvent e)
		{
		}
		
		public void mouseReleased(MouseEvent e)
		{
			if(e.getClickCount() != 2)
				return;
			
			JTree clickedTree = (JTree)e.getSource();
			TreePath path = clickedTree.getPathForLocation(e.getX(), e.getY());
			if(path == null)
				return;
			
			saveAndExitIfValidSelection();
		}
	}
	
	class KeyHandler extends KeyAdapter
	{
		public void keyTyped(KeyEvent e)
		{
			if(e.getKeyChar() == KeyEvent.VK_ESCAPE)
				dispose();
		}
	}

	OkAction okAction;
	UiButton okButton;
	UiButton cancelButton;
	private Box buttonBox;
	
	PopUpTreeFieldSpec spec;
	SearchFieldTree tree;
	DefaultMutableTreeNode selectedNode;
}