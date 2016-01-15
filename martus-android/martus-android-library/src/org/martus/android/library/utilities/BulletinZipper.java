package org.martus.android.library.utilities;

import org.martus.common.bulletin.Bulletin;

import java.io.File;

/**
 * Created by animal@martus.org on 12/22/15.
 */
public interface BulletinZipper {

    public void onZipped(Bulletin bulletin, File zippedFile);
}
