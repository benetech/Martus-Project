package org.martus.android.library.io;

import java.io.FileNotFoundException;

import info.guardianproject.iocipher.FileOutputStream;

/**
 * Created by animal@martus.org on 4/27/15.
 */
public class SecureFileOutputStream extends FileOutputStream {
    public SecureFileOutputStream(String path) throws FileNotFoundException {
        super(path);
    }

    public SecureFileOutputStream(SecureFile file) throws FileNotFoundException {
        super(file);
    }
}
