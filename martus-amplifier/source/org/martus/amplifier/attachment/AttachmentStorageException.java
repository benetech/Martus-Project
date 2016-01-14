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
package org.martus.amplifier.attachment;

/**
 * An exception class intended to encapsulate lower-level exceptions
 * that may be thrown from different implementations of AttachmentManager.
 * 
 * @author PDAlbora
 */
public class AttachmentStorageException extends Exception 
{

	/**
	 * Constructor for AttachmentStorageException.
	 */
	public AttachmentStorageException() {
		super();
	}

	/**
	 * Constructor for AttachmentStorageException.
	 * @param message
	 */
	public AttachmentStorageException(String message) {
		super(message);
	}

	/**
	 * Constructor for AttachmentStorageException.
	 * @param message
	 * @param cause
	 */
	public AttachmentStorageException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for AttachmentStorageException.
	 * @param cause
	 */
	public AttachmentStorageException(Throwable cause) {
		super(cause);
	}

}
