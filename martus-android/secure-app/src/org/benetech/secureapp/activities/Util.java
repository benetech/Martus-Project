package org.benetech.secureapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import org.benetech.secureapp.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.InstanceProviderAPI;

/**
 * Utilities used among our Activities
 */
public class Util {
	
	public static void setupFloatingLabelEditTextForPassword(EditText editText) {
    	editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    /**
     * Return the currently loading Form Instance Title as reported by the
     * {@link org.odk.collect.android.logic.FormController}
     *
     * @return The title of the current Form instance or null if not available
     */
    public static String getFormInstanceTitle(Context context) {
        return getFormInstanceValue(context, InstanceProviderAPI.InstanceColumns.DISPLAY_NAME);
    }

    public static String getFormInstanceAuthor(Context context) {
        return getFormInstanceValue(context, InstanceProviderAPI.InstanceColumns.FORM_INSTANCE_AUTHOR);
    }

    public static String getFormInstanceOrganization(Context context) {
        return getFormInstanceValue(context, InstanceProviderAPI.InstanceColumns.FORM_INSTANCE_ORGANIZATION);
    }

    private static String getFormInstanceValue(Context context, String name) {
        Cursor result = getFormCursorForCurrentInstance(context);
        if (result != null && result.moveToFirst()) {
            String instanceTitle = result.getString(result.getColumnIndex(name));
            // We successfully got the instance name from the ContentProvider
            if (instanceTitle != null && instanceTitle.length() > 0) {
                return instanceTitle;
            }
        }

        return null;
    }

    public static Cursor getFormCursorForCurrentInstance(Context context) {
        String path = Collect.getInstance().getFormController().getInstancePath().getAbsolutePath();
        Cursor result = context.getContentResolver().query(
                InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                null,
                InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + " = ?",
                new String[]{path},
                null);
        if (result != null && result.moveToFirst()) {
            return result;
        }
        return null;
    }

    public static void alterUserOfUnexpectedError(Context context, Exception e, String message) {
        showErrorMessage(context, context.getString(R.string.error_message_unexpected_error));
        Log.e(context.getClass().getSimpleName(), message, e);
    }

    public static void showMessage(Context context, String msg, String title){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(msg)
                .show();
    }

    public static void showErrorMessage(Context context, String msg){
        showErrorMessage(context, msg, context.getString(R.string.error_message));
    }

    public static void showErrorMessage(Context context, String msg, String title){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setIcon(android.R.drawable.ic_dialog_alert);
        alert.setTitle(title);
        alert.setMessage(msg);
        alert.show();
    }

}
