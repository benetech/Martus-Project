package org.martus.android.library.io;

import android.util.Log;

import java.net.URI;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FilenameFilter;

/**
 * Created by animal@martus.org on 4/24/15.
 */
public class SecureFile extends File {

    private static final String TAG = "SecureFile";
    public SecureFile(URI uri) {
        super(uri);
    }

    public SecureFile(File file) {
        this(file.getAbsolutePath());
    }

    public SecureFile(String dirPath, String name) {
        super(dirPath, name);
    }

    public SecureFile(String path) {
        super(path);
    }

    public SecureFile(java.io.File dir, String name) {
        super(dir, name);
    }

    @Override
    public SecureFile getParentFile() {
        String tempParent = getParent();
        if (tempParent == null) {
            return null;
        }
        return new SecureFile(tempParent);
    }

    public SecureFile[] listFiles(SecureFileFilter filter) {
        File[] ioCipherFiles = super.listFiles(filter);

        return convertToSecureFiles(ioCipherFiles);
    }

    @Override
    public SecureFile[] listFiles(FilenameFilter filter) {
        File[] files = super.listFiles(filter);
        SecureFile[] secureFiles = convertToSecureFiles(files);

        return secureFiles;
    }

    private SecureFile[] convertToSecureFiles(File[] files) {
        SecureFile[] secureFiles = new SecureFile[files.length];
        for (int index = 0; index < files.length; ++index)
        {
            secureFiles[index] = new SecureFile(files[index]);
        }

        return secureFiles;
    }

    @Override
    public SecureFile[] listFiles() {
        return filenamesToFiles(list());
    }

    private SecureFile[] filenamesToFiles(String[] filenames) {
        if (filenames == null) {
            return new SecureFile[0];
        }
        SecureFile[] result = new SecureFile[filenames.length];
        for (int i = 0; i < filenames.length; ++i) {
            result[i] = new SecureFile(this, filenames[i]);
        }
        return result;
    }

    @Override
    public boolean delete(){
        if (isFile())
            return super.delete();

        SecureFile[] children = listFiles();
        for (SecureFile child : children) {
            child.delete();
        }

        if (listFiles().length != 0)
            Log.e(TAG, "Children files where not deleted");

        return super.delete();
    }
}
