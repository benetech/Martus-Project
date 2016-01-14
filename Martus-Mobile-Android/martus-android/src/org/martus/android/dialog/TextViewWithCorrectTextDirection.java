package org.martus.android.dialog;

import android.content.Context;
import android.view.Gravity;
import android.widget.EditText;

import java.util.Locale;

/**
 * Created by nimaa on 5/21/14.
 */
public class TextViewWithCorrectTextDirection extends EditText {

    public TextViewWithCorrectTextDirection(Context context) {
        super(context);

        setPasswordTextFieldGravityToMatchLanguageDirection();
    }

    public TextViewWithCorrectTextDirection(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);

        setPasswordTextFieldGravityToMatchLanguageDirection();
    }


    public TextViewWithCorrectTextDirection(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setPasswordTextFieldGravityToMatchLanguageDirection();
    }

    private void setPasswordTextFieldGravityToMatchLanguageDirection() {
        setGravity(getGravityDirectionBasedOnLocale());
    }

    private static int getGravityDirectionBasedOnLocale() {
        if(isRightToLeftLocale())
            return Gravity.RIGHT;

        return Gravity.LEFT;
    }

    private static boolean isRightToLeftLocale() {
        Locale locale = Locale.getDefault();
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        if (directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT)
            return true;

        if (directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC)
            return true;

        return false;
    }
}
