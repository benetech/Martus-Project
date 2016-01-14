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

package org.martus.client.swingui;

import java.awt.AWTEvent;
import java.awt.Event;
import java.awt.event.AWTEventListener;

class UiInactivityDetector implements AWTEventListener
{
	public UiInactivityDetector()
	{
		java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(this,
				AWTEvent.KEY_EVENT_MASK |
				AWTEvent.MOUSE_EVENT_MASK |
				AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}

	public long secondsSinceLastActivity()
	{
		return (now() - lastActivityAt) / 1000;
	}

	private void trackActivity()
	{
		lastActivityAt = now();
	}

	private long now()
	{
		return System.currentTimeMillis();
	}

	public void eventDispatched(AWTEvent event)
	{
		// a MOUSE_EXIT is automatically generated if
		// we hide the window, so always ignore them
		if(event.getID() == Event.MOUSE_EXIT)
			return;

		trackActivity();
	}

	long lastActivityAt = now();
}

