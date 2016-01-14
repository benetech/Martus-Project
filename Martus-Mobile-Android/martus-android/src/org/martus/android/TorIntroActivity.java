package org.martus.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import java.security.SignatureException;

import info.guardianproject.onionkit.ui.OrbotHelper;

/**
 * @author roms
 *         Date: 10/8/13
 */
public class TorIntroActivity extends AbstractTorActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeTorToggleButton();
    }

    @Override
    protected int getLayoutName() {
        return R.layout.tor_during_setup;
    }

    public void nextScreen(View view) {
		Intent intent = new Intent(TorIntroActivity.this, ChooseConnectionActivity.class);
        startActivityForResult(intent, EXIT_REQUEST_CODE);
		finish();
	}

    private void initializeTorToggleButton() {
        setTorToggleButton((CompoundButton) findViewById(R.id.checkBox_use_tor));
        getTorToggleButton().setOnCheckedChangeListener(new TorToggleChangeHandler());
        synchronizeTorSwitchWithCurrentSystemProperties();
    }

    @Override
    public void onResume() {
        super.onResume();

        synchronizeTorSwitchWithCurrentSystemProperties();
        syncTorToggleToMatchOrbotState();
	}
}
