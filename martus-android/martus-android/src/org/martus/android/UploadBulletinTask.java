package org.martus.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.martus.android.library.utilities.BulletinSender;
import org.martus.clientside.MobileClientSideNetworkGateway;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.packet.UniversalId;
import org.martus.util.StreamableBase64;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

/**
 * @author roms
 *         Date: 10/3/12
 */
public class UploadBulletinTask extends AsyncTask<Object, Integer, String> implements ProgressUpdater {

    public static final String BULLETIN_SEND_COMPLETED_BROADCAST = "send_completed";
    public static final String FAILED_BULLETINS_DIR = "failed_bulletins";

    private NotificationHelper mNotificationHelper;
    private BulletinSender sender;
    private Application myApplication;

    public UploadBulletinTask(Application application, BulletinSender sender, UniversalId bulletinId) {
        myApplication = application;
        mNotificationHelper = new NotificationHelper(myApplication.getApplicationContext(), bulletinId.hashCode());
        this.sender = sender;
    }

    @Override
    protected String doInBackground(Object... params) {

        final UniversalId uid = (UniversalId)params[0];
        final File zippedFile = (File)params[1];
        final MobileClientSideNetworkGateway gateway = (MobileClientSideNetworkGateway)params[2];
        final MartusSecurity signer = (MartusSecurity)params[3];


        return doSend(uid, zippedFile, gateway, signer, this, myApplication);
    }

    public static String doSend(UniversalId uid, File zippedFile, MobileClientSideNetworkGateway gateway,
                                MartusSecurity signer, ProgressUpdater updater, Context context) {
        String result = null;

        try {
	        if (NetworkUtilities.isNetworkAvailable(context))
                result = uploadBulletinZipFile(uid, zippedFile, gateway, signer, updater);
        } catch (MartusUtilities.FileTooLargeException e) {
            Log.e(AppConfig.LOG_LABEL, "file too large to upload", e);
            result = e.getMessage();
        } catch (IOException e) {
            Log.e(AppConfig.LOG_LABEL, "io problem uploading file", e);
            result = e.getMessage();
        } catch (MartusCrypto.MartusSignatureException e) {
            Log.e(AppConfig.LOG_LABEL, "crypto problem uploading file", e);
            result = e.getMessage();
        } catch (Exception e) {
            Log.e(AppConfig.LOG_LABEL, "exception uploading file", e);
            result = e.getMessage();
        } finally {
            if (null != zippedFile) {
                if ((null != result) && (result.equals(NetworkInterfaceConstants.OK))) {
                    zippedFile.delete();
                } else {
                    if (zippedFile.getParentFile().equals(context.getCacheDir().getParentFile())) {
                        File failedBulletinsDir = new File (context.getCacheDir().getParent(), FAILED_BULLETINS_DIR);
                        failedBulletinsDir.mkdirs();
                        File movedFile = new File(failedBulletinsDir.toString(), zippedFile.getName());
                        boolean successfulMove = zippedFile.renameTo(movedFile);
                        if (!successfulMove) {
                            Log.e(AppConfig.LOG_LABEL, "problem moving failed bulletin to failed directory");
                            result = "problem moving failed record";
                        }
                    }
                }
            }
        }
        signer.clearKeyPair();
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        createInitialNotification(mNotificationHelper, myApplication);

    }

    public static void createInitialNotification(NotificationHelper notificationHelper, Context context) {
        final Time now = new Time();
        now.setToNow();
        final String timeAsTitle = now.format("%T");
        notificationHelper.createNotification(context.getString(R.string.notification_title, timeAsTitle),
                context.getString(R.string.starting_send_notification));
    }

    @Override
    protected void onPostExecute(String s) {
        mNotificationHelper.completed(s);
	    if (s == null) {
            s = myApplication.getString(R.string.send_failed_cant_reach_server);
        }
        if (null != sender) {
            sender.onSent(s);
        }
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        if (null != sender) {
            sender.onProgressUpdate(progress[0]);
            mNotificationHelper.updateProgress(myApplication.getString(R.string.bulletin_sending_progress),   progress[0]);
        }
    }

     public static String uploadBulletinZipFile(UniversalId uid, File tempFile, MobileClientSideNetworkGateway gateway, MartusCrypto crypto, ProgressUpdater fileSender)
        		throws
                    MartusUtilities.FileTooLargeException, IOException, MartusCrypto.MartusSignatureException
    {
        final int totalSize = MartusUtilities.getCappedFileLength(tempFile);
        int offset = gateway.getOffsetToStartUploading(uid, tempFile, crypto);
        byte[] rawBytes = new byte[NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE];
        FileInputStream inputStream = new FileInputStream(tempFile);
	    inputStream.skip(offset);
        String result = null;
        while(true)
        {
            int chunkSize = inputStream.read(rawBytes);
            if(chunkSize <= 0)
                break;
            byte[] chunkBytes = new byte[chunkSize];
            System.arraycopy(rawBytes, 0, chunkBytes, 0, chunkSize);

            String authorId = uid.getAccountId();
            String bulletinLocalId = uid.getLocalId();
            String encoded = StreamableBase64.encode(chunkBytes);

            NetworkResponse response = gateway.putBulletinChunk(crypto,
                                authorId, bulletinLocalId, totalSize, offset, chunkSize, encoded);
            result = response.getResultCode();
            if(!result.equals(NetworkInterfaceConstants.CHUNK_OK) && !result.equals(NetworkInterfaceConstants.OK))
                break;
            offset += chunkSize;

            fileSender.showProgress(offset * 100 / totalSize);
        }
        inputStream.close();
        return result;
    }

    @Override
    public void showProgress(int value) {
        publishProgress(value);
    }
}
