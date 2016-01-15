package org.martus.common.network;

/**
 * Created by nimaa on 9/11/14.
 */

import android.os.AsyncTask;

import org.martus.common.crypto.MartusSecurity;

import java.util.Vector;

public class PublicKeyTask extends AsyncTask<Object, Void, Vector> {

    private PublicKeyTaskPostExecuteHandler mPostExecuteHandler;

    public PublicKeyTask(PublicKeyTaskPostExecuteHandler postExecuteHandler) {
        mPostExecuteHandler = postExecuteHandler;
    }

    @Override
    protected Vector doInBackground(Object... params) {

        final NonSSLNetworkAPI server = (NonSSLNetworkAPI)params[0];
        final MartusSecurity security = (MartusSecurity)params[1];
        Vector result = null;

        result = server.getServerInformation();

        return result;
    }

    @Override
    protected void onPostExecute(Vector result) {
        super.onPostExecute(result);

        getPostExecuteHandler().processResult(result);
    }

    private PublicKeyTaskPostExecuteHandler getPostExecuteHandler() {
        return mPostExecuteHandler;
    }
}

