package org.martus.android.library.utilities;

/**
 * @author roms
 *         Date: 10/25/12
 */
public interface BulletinSender extends BulletinZipper {

    public void onSent(String result);

    public void onProgressUpdate(int progress);
}
