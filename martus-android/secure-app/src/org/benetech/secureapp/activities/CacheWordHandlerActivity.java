package org.benetech.secureapp.activities;

import android.content.Intent;

import info.guardianproject.cacheword.ICacheWordSubscriber;

/**
 * Created by animal@martus.org on 8/27/14.
 */
public class CacheWordHandlerActivity extends AbstractCacheWordSubscriberActivity implements ICacheWordSubscriber {

    @Override
    public void onCacheWordUninitialized() {
        Intent intent = new Intent(this, CreatePassphraseActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCacheWordLocked() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCacheWordOpened() {
    }
}