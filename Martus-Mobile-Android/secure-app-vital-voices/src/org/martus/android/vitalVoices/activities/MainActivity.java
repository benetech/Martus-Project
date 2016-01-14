package org.martus.android.vitalVoices.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.martus.android.vitalVoices.R;
import org.martus.android.vitalVoices.application.Constants;
import org.martus.android.vitalVoices.application.MainApplication;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;

public class MainActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        initiliazeExistingFormList();
    }

    private void initiliazeExistingFormList() {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " != ?";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};
        String sortOrder = InstanceProviderAPI.InstanceColumns.STATUS + " DESC, " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";
        Cursor cursor = managedQuery(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, selection, selectionArgs, sortOrder);

        String[] listData = new String[] {
                InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT
        };

        int[] listViews = new int[] {
                org.odk.collect.android.R.id.text1, org.odk.collect.android.R.id.text2
        };

        SimpleCursorAdapter instances = new SimpleCursorAdapter(this, org.odk.collect.android.R.layout.two_item, cursor, listData, listViews);

        setListAdapter(instances);
    }

    /**
     * Stores the path of selected instance in the parent class and finishes.
     */
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        startManagingCursor(cursor);
        Uri instanceUri = ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI, cursor.getLong(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));

        Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick", instanceUri.toString());

        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action)) {
            // caller is waiting on a picked form
            setResult(RESULT_OK, new Intent().setData(instanceUri));
        } else {
            // the form can be edited if it is incomplete or if, when it was
            // marked as complete, it was determined that it could be edited
            // later.
            String status = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS));
            String canEditWhenCompleteSetting = cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE));

            boolean canEdit = status.equals(InstanceProviderAPI.STATUS_INCOMPLETE) || Boolean.parseBoolean(canEditWhenCompleteSetting);
            if (canEdit) {
                startForm(instanceUri);
            }
        }
    }

    public void startNewForm(View view) {
        try {
            Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, 1);
            startForm(formUri);

        } catch (Exception e) {
            Log.e(Constants.LOG_LABEL, "problem finding form file", e);
        }
    }

    private void startForm(Uri formUri) {
        Intent intent = new Intent(this, MainFormEntryActivity.class);
        intent.setData(formUri);

        Cursor cursor = getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null, null, null);
        printContentResolver(cursor);
        startActivity(intent);
    }

    private void printContentResolver(Cursor cursor) {
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
        {
            System.out.println("===========================================");
            String[] columnNames = cursor.getColumnNames();
            for (int index = 0; index < columnNames.length; ++index) {
                System.out.println("---------------------------COLUMN name = " + columnNames[index]);

                int nameColumn = cursor.getColumnIndex(columnNames[index]);

                String name = cursor.getString(nameColumn);
                System.out.println("============================ COL Value = " + name);
            }
            System.out.println("===========================================");
        }
    }
}
