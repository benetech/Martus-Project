package org.martus.android.library.io;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileFilter;

/**
 * Created by animal@martus.org on 4/27/15.
 */
public class SecureFileFilter implements FileFilter {

    private String filePrefix;
    private String fileSuffix;

    public SecureFileFilter(String filePrefixToUse, String fileSuffixToUse) {
        filePrefix = filePrefixToUse;
        fileSuffix = fileSuffixToUse;
    }

    @Override
    public boolean accept(File pathname) {
        String name = pathname.getName();
        return name.startsWith(filePrefix)
                && name.endsWith(fileSuffix);
    }
}
