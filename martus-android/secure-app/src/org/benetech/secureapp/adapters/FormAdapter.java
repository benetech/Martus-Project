package org.benetech.secureapp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.odk.collect.android.provider.InstanceProviderAPI;

/**
 * An adapter for displaying Martus Forms 
 * 
 * @author David Brodsky (dbro@dbro.pro)
 *
 */
public class FormAdapter extends SimpleCursorAdapter {
	
	/** Click Listeners for internal use */
    private View.OnClickListener mSyncButtonListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (mListener != null) mListener.onSyncRequested(getFormForView(v));
		}
	};
	
    private View.OnClickListener mDeleteButtonListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (mListener != null) mListener.onDeleteRequested(getFormForView(v));
		}
	};

    private View.OnClickListener mManageAttachmentsButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mListener != null) mListener.onManageAttachments(getFormForView(v));
        }
    };

    /** Click listeners for client use */
	private FormAdapterItemClickListener mListener;
	
	public interface FormAdapterItemClickListener {
		public void onSyncRequested(Cursor form);
		public void onDeleteRequested(Cursor form);
        public void onManageAttachments(Cursor form);
	}

    public FormAdapter(Context context, Cursor c, FormAdapterItemClickListener listener) {
        super(context, org.benetech.secureapp.R.layout.form_list_item, c, new String[]{} , new int[]{}, 0);
        mListener = listener;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        ViewCache viewCache = (ViewCache) view.getTag(org.benetech.secureapp.R.id.tag_view_cache);
        if (viewCache == null) {
        	viewCache = new ViewCache();
        	viewCache.title = (TextView) view.findViewById(org.benetech.secureapp.R.id.title);
            viewCache.syncButton = (ImageButton) view.findViewById(org.benetech.secureapp.R.id.syncButton);
            viewCache.deleteButton = (ImageButton) view.findViewById(org.benetech.secureapp.R.id.deleteButton);
            viewCache.manageAttachmentsButton = (ImageButton) view.findViewById(org.benetech.secureapp.R.id.manageAttachmentsButton);

            viewCache.id_col = cursor.getColumnIndexOrThrow(InstanceProviderAPI.InstanceColumns._ID);
        	viewCache.title_col = cursor.getColumnIndexOrThrow(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME);
        	viewCache.status_col= cursor.getColumnIndexOrThrow(InstanceProviderAPI.InstanceColumns.STATUS);
        	
        	viewCache.syncButton.setOnClickListener(mSyncButtonListener);
        	viewCache.deleteButton.setOnClickListener(mDeleteButtonListener);
            viewCache.manageAttachmentsButton.setOnClickListener(mManageAttachmentsButtonListener);
        }
       
        view.setTag(org.benetech.secureapp.R.id.tag_view_cache_object_id, cursor.getInt(viewCache.id_col));
        viewCache.title.setText(cursor.getString(viewCache.title_col));
        
        if (cursor.getString(viewCache.status_col).equals(InstanceProviderAPI.STATUS_SUBMITTED)) {
        	// If the form is submitted, hide submit button
        	// TODO: Show "synced" icon
        	viewCache.syncButton.setVisibility(View.INVISIBLE);
        } else {
        	viewCache.syncButton.setVisibility(View.VISIBLE);
        }
    }
	
	// Cache the views within a ListView row item 
    static class ViewCache {
        TextView title;
        ImageButton syncButton;
        ImageButton deleteButton;
        ImageButton manageAttachmentsButton;
        
        int id_col;
        int title_col;
        int status_col;
    }
	
	private static Cursor getFormForView(View button) {
		int id = (Integer) ((View) button.getParent()).getTag(org.benetech.secureapp.R.id.tag_view_cache_object_id);
		Cursor form = button.getContext()
						  .getContentResolver()
						  .query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, 
								 null, 
								 InstanceProviderAPI.InstanceColumns._ID + " = ?",
								 new String[] { String.valueOf(id)}, null);
		
		if (form != null && form.moveToFirst()) {
			return form;
		}
		return null;
	}
}
