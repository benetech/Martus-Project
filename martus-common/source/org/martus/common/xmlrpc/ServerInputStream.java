/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002-2007, Beneficent
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



/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "XML-RPC" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.martus.common.xmlrpc;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

class ServerInputStream
    extends InputStream
{
    // bytes remaining to be read from the input stream. This is
    // initialized from CONTENT_LENGTH (or getContentLength()).
    // This is used in order to correctly return a -1 when all the
    // data POSTed was read. If this is left to -1, content length is
    // assumed as unknown and the standard InputStream methods will be used
    private long available = -1;
    private long markedAvailable;

    private BufferedInputStream in;

    public ServerInputStream(BufferedInputStream in, int available)
    {
        this.in = in;
        this.available = available;
    }

    public int read() throws IOException
    {
        if (available > 0)
        {
            available--;
            return in.read();
        }
        else if (available == -1)
        {
            return in.read ();
        }
        return -1;
    }

    public int read(byte b[]) throws IOException
    {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException
    {
        if (available > 0)
        {
            if (len > available)
            {
                // shrink len
                len = (int) available;
            }
            int read = in.read(b, off, len);
            if (read != -1)
            {
                available -= read;
            }
            else
            {
                available = -1;
            }
            return read;
        }
        else if (available == -1)
        {
            return in.read(b, off, len);
        }
        return -1;
    }

    public long skip(long n) throws IOException
    {
        long skip = in.skip(n);
        if (available > 0)
        {
            available -= skip;
        }
        return skip;
    }

    public void mark(int readlimit)
    {
        in.mark(readlimit);
        markedAvailable = available;
    }

    public void reset() throws IOException
    {
        in.reset();
        available = markedAvailable;
    }

    public boolean markSupported()
    {
        return true;
    }
}
