package org.benetech.secureapp.activities;

import android.app.Activity;
import android.os.Bundle;

import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;

/**
 * Created by animal@martus.org on 8/28/14.
 */
abstract public class AbstractCacheWordSubscriberActivity extends Activity implements ICacheWordSubscriber {

    private CacheWordHandler cacheWordActivityHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cacheWordActivityHandler = new CacheWordHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getCacheWordActivityHandler().connectToService();
    }

    @Override
    protected void onPause() {
        super.onPause();

        getCacheWordActivityHandler().disconnectFromService();
    }

    private CacheWordHandler getCacheWordActivityHandler() {
        return cacheWordActivityHandler;
    }
}