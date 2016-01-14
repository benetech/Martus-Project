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
package org.martus.client.network;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;

import org.martus.common.network.MartusOrchidDirectoryStore;
import org.martus.common.network.OrchidTransportWrapper;

public class OrchidTransportWrapperWithActiveProperty extends OrchidTransportWrapper
{
	public static OrchidTransportWrapperWithActiveProperty createWithoutPersistentStore() throws Exception
	{
		return create(new MartusOrchidDirectoryStore());
	}
	
	public static OrchidTransportWrapperWithActiveProperty create(MartusOrchidDirectoryStore storeToUse) throws Exception
	{
		return new OrchidTransportWrapperWithActiveProperty(storeToUse);
	}
	
	protected OrchidTransportWrapperWithActiveProperty(MartusOrchidDirectoryStore storeToUse) throws Exception
	{
		super(storeToUse);

		isTorActive = new SimpleBooleanProperty();
	}
	
	public Property <Boolean> getIsTorActiveProperty()
	{
		return isTorActive;
	}
	
	@Override
	public boolean isTorEnabled()
	{
		return getIsTorActiveProperty().getValue();
	}
	
	public void startTor()
	{
		isTorActive.setValue(true);
		super.startTor();
	}

	public void startTorInSameThread()
	{
		isTorActive.setValue(true);
		super.startTorInSameThread();
	}
	
	public void stopTor()
	{
		isTorActive.setValue(false);
		super.stopTor();
	}
	

	private Property <Boolean> isTorActive;
}
