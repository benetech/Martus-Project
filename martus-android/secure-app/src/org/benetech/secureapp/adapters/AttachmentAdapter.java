package org.benetech.secureapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.benetech.secureapp.R;

import java.util.ArrayList;

/**
 * Created by animal@martus.org on 4/28/15.
 */
public class AttachmentAdapter extends BaseAdapter {

    private AttachmentAdapterItemClickListener mListener;
    private ArrayList<String> mAttachmentFileNames;
    private LayoutInflater mLayoutInflator;

    public AttachmentAdapter(LayoutInflater layoutInflater, ArrayList<String> attachmentFileNames, AttachmentAdapterItemClickListener listener) {
        mLayoutInflator = layoutInflater;
        mAttachmentFileNames = attachmentFileNames;
        mListener = listener;
    }
  @Override
    public int getCount() {
        return mAttachmentFileNames.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup viewGroup) {
        View view = mLayoutInflator.inflate(R.layout.attachments_list_item, null);
        TextView attachmentName = (TextView) view.findViewById(R.id.attachmentFileName);
        attachmentName.setText(mAttachmentFileNames.get(index));

        ImageView deleteButton = (ImageView) view.findViewById(R.id.deleteAttachmentButton);
        deleteButton.setOnClickListener(mDeleteButtonListener);

        return view;
    }

    private View.OnClickListener mDeleteButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (mListener == null)
                return;

            View parentView = (View) view.getParent();
            TextView attachmentFileNameTextView = (TextView) parentView.findViewById(R.id.attachmentFileName);
            String attachmentFileName = attachmentFileNameTextView.getText().toString();
            mListener.onDeleteRequested(attachmentFileName);
        }
    };

    public void updateAttachmentFiles(ArrayList<String> attachmentFileNames) {
        mAttachmentFileNames = attachmentFileNames;
        super.notifyDataSetChanged();
    }
}
