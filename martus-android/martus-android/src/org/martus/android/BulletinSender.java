package org.martus.android;

import org.martus.common.bulletin.Bulletin;

import java.io.File;

/**
 * @author roms
 *         Date: 10/25/12
 */
public interface BulletinSender {

    public void onSent(String result);
    public void onProgressUpdate(int progress);
    public void onZipped(Bulletin bulletin, File zippedFile);
}
