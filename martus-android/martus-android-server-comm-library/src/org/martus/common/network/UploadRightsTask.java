package org.martus.common.network;

import android.os.AsyncTask;
import android.util.Log;

import org.martus.clientside.MobileClientSideNetworkGateway;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;

/**
 * Created by animal@martus.org on 9/11/14.
 */
public class UploadRightsTask extends AsyncTask<Object, Void, NetworkResponse> {

    private static final String TAG = "UploadRightsTask";
    private NetwordResponseHander mNetwordResponseHander;

    public UploadRightsTask(NetwordResponseHander networdResponseHander) {
        mNetwordResponseHander = networdResponseHander;
    }

    @Override
    protected NetworkResponse doInBackground(Object... params) {

        final MobileClientSideNetworkGateway gateway = (MobileClientSideNetworkGateway)params[0];
        final MartusSecurity signer = (MartusSecurity)params[1];
        final String magicWord = (String)params[2];

        NetworkResponse result = null;

        try {
            result = gateway.getUploadRights(signer, magicWord);
        } catch (MartusCrypto.MartusSignatureException e) {
            Log.e(TAG, "problem getting upload rights", e);
        }

        return result;
    }

    @Override
    protected void onPostExecute(NetworkResponse result) {
        super.onPostExecute(result);

        mNetwordResponseHander.processMagicWordResponse(result);
    }
}
