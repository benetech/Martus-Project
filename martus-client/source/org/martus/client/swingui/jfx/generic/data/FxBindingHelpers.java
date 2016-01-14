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
package org.martus.client.swingui.jfx.generic.data;

import javafx.beans.property.Property;

public class FxBindingHelpers
{
	public static Property bindToOurPropertyField(Property cellProperty, Property currentFieldProperty, Property cellPropertyBoundToCurrently)
	{
		safelyUnbindProperty(cellProperty, currentFieldProperty,cellPropertyBoundToCurrently);
		if(shouldBindToProperty(cellProperty, cellPropertyBoundToCurrently))
			cellPropertyBoundToCurrently = bindBidirectionally(cellProperty, currentFieldProperty);
		return cellPropertyBoundToCurrently;
	}

	public static void safelyUnbindProperty(Property cellProperty,
			Property currentFieldProperty, Property cellPropertyBoundToCurrently)
	{
		if(cellPropertyBoundToCurrently != null && cellPropertyBoundToCurrently != cellProperty)
			currentFieldProperty.unbindBidirectional(cellPropertyBoundToCurrently);
	}

	private static boolean shouldBindToProperty(Property cellProperty, Property cellPropertyBoundToCurrently)
	{
		if(cellPropertyBoundToCurrently==null) 
			return true;
		if(cellPropertyBoundToCurrently != cellProperty) 
			return true;
		return false;
	}

	private static Property bindBidirectionally(Property hotPropertyToBeBound, Property controlerProperty)
	{
		controlerProperty.bindBidirectional(hotPropertyToBeBound);
		return hotPropertyToBeBound;
	}
}
