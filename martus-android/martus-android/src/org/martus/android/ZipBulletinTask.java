package org.martus.android;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.martus.android.library.utilities.BulletinSender;
import org.martus.clientside.MobileClientBulletinStore;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author roms
 *         Date: 10/3/12
 */
public class ZipBulletinTask extends AsyncTask<Object, Integer, File> {

    private static final SimpleDateFormat FILE_NAME_DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SS_");

    private Bulletin bulletin;
    private BulletinSender sender;

    public ZipBulletinTask(Bulletin bulletin, BulletinSender sender) {
        this.bulletin = bulletin;
        this.sender = sender;
    }

    @Override
    protected File doInBackground(Object... params) {

        final File currentBulletinDir = (File)params[0];
        final MobileClientBulletinStore store = (MobileClientBulletinStore)params[1];

        File file = null;

        try {
            store.saveBulletin(bulletin);
            file = File.createTempFile("tmp_send_" + getCurrentTimeStamp(), ".zip", currentBulletinDir);
            BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(store.getDatabase(), bulletin.getDatabaseKey(), file, bulletin.getSignatureGenerator());
        } catch (Exception e) {
            Log.e("martus", "problem serializing bulletin to zip", e);
        }

        return file;
    }

	@Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(File result) {
        if (null != sender) {
            sender.onZipped(bulletin, result);
        }
        super.onPostExecute(result);
    }

    public static String getCurrentTimeStamp() {
        Date now = new Date();
        return FILE_NAME_DATE_FORMAT.format(now);
    }

}
