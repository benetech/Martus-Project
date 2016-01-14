package org.martus.android.dialog;

import android.content.DialogInterface;
import android.view.KeyEvent;

/**
 * @author roms
 *         Date: 1/30/13
 */
public class BackButtonIgnorer implements DialogInterface.OnKeyListener {
    @Override
    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
        return keyCode == KeyEvent.KEYCODE_BACK;
    }
}
