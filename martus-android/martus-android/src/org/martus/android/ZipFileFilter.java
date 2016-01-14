package org.martus.android;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author roms
 *         Date: 1/17/13
 */
public class ZipFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File file, String name) {
        return name.endsWith(".zip");
    }
}
