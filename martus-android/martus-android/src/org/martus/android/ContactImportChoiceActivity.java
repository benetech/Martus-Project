package org.martus.android;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.martus.android.dialog.AddContactActivity;

/**
 * Created by nimaa on 4/11/14.
 */
public class ContactImportChoiceActivity extends BaseActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.contact_import_choice);
        TextView addContactFromFileLinkTextView = (TextView) findViewById(R.id.addContactFromFileLink);
        addContactFromFileLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());
        makeTextViewClickableHyperlink(addContactFromFileLinkTextView);
        addContactFromFileLinkTextView.setOnClickListener(new TextViewClickHandler());
    }

    public void addContactUsingAccessToken(View view){
        Intent intent = new Intent(ContactImportChoiceActivity.this, AddContactActivity.class);
        startActivityForResult(intent, EXIT_REQUEST_CODE);
        finish();
    }

    private class TextViewClickHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(ContactImportChoiceActivity.this, DesktopKeyActivity.class);
            startActivityForResult(intent, EXIT_REQUEST_CODE);
            finish();
        }
    }
}
