package org.martus.android.library.io;

import java.io.FileNotFoundException;

import info.guardianproject.iocipher.FileInputStream;

/**
 * Created by animal@martus.org on 4/27/15.
 */
public class SecureFileInputStream extends FileInputStream {
    public SecureFileInputStream(SecureFile file) throws FileNotFoundException {
        super(file);
    }
}
