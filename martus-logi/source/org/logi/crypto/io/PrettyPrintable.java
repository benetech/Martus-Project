// Copyright (C) 1999-2001 Logi Ragnarsson

package org.logi.crypto.io;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Classes implementing this interface can write themselves to a PrintWriter
 * object in a fairly readable manner.
 */
public interface PrettyPrintable
{

    /**
     * Print this object to out, indented with ind tabs, going down at most
     * rec levels of recursion. */
    void prettyPrint(PrintWriter out, int ind, int rec) throws IOException;

    /**
     * Print this object to out. */
    void prettyPrint(PrintWriter out) throws IOException;

}
