package org.benetech.secureapp.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.benetech.secureapp.R;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.IFormElement;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.martus.android.library.common.dialog.ProgressDialogHandler;
import org.odk.collect.android.activities.FormHierarchyActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.HierarchyElement;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;

import java.util.ArrayList;
import java.util.List;

public class FormGroupActivity extends FormHierarchyActivity {
    private static final String TAG = "FormGroupActivity";

    protected static final int GROUP = 555;

    private EditText mFormTitle;
    private EditText mFormAuthor;
    private EditText mFormOrganization;
    private boolean mFormTitleNeedsSaving;
    private ProgressDialogHandler mProgressDialogHandler;

    /** Every time the Form Title EditText is changed mFormTitleNeedsSaving is set true.
     *
     * Both the IME_ACTION on mFormTitle (Keyboard "Done") and {@link #onPause()} will check
     * if mFormTitleNeedsSaving is true at their invocation, calling {@link #saveFormTitle(String)}
     * if appropriate.
     */
    private TextWatcher mFormTitleWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mFormTitleNeedsSaving = true;
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable "Up" Navigation
        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);
        // Note that calling setContentView after super.onCreate
        // invalidates any View references made there.
        setContentView(R.layout.form_group_layout);
        mFormTitle = (EditText) findViewById(R.id.formTitle);
        mFormAuthor = (EditText) findViewById(R.id.authorField);
        mFormOrganization = (EditText) findViewById(R.id.organizationField);

        setFormTitle();
        setFormAuthor();
        setFormOrganization();

        addFormFieldEditorActionListener(mFormTitle);
        addFormFieldEditorActionListener(mFormAuthor);
        addFormFieldEditorActionListener(mFormOrganization);

        this.mFormTitle.addTextChangedListener(mFormTitleWatcher);
        mFormAuthor.addTextChangedListener(mFormTitleWatcher);
        mFormOrganization.addTextChangedListener(mFormTitleWatcher);

        mProgressDialogHandler = new ProgressDialogHandler(this);
    }

    private void addFormFieldEditorActionListener(EditText formField) {
        formField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (mFormTitleNeedsSaving)
                    saveFormTitle(v.getText().toString());

                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFormTitleNeedsSaving)
            saveFormTitle(mFormTitle.getText().toString());
    }

    /**
     * Updates the UI with the Form title
     */
    private void setFormTitle() {
        String instanceTitle = Util.getFormInstanceTitle(this);
        if (isValidValue(instanceTitle)) {
            mFormTitle.setText(instanceTitle);
            setTitle(instanceTitle);
        } else {
            // For ActionBar use Form title if no instance available
            setTitle(Collect.getInstance().getFormController().getFormTitle());
        }
    }

    private void setFormAuthor() {
        String instanceTitle = Util.getFormInstanceAuthor(this);
        setTextFieldTextSafely(mFormAuthor, instanceTitle);
    }

    private void setFormOrganization() {
        String instanceTitle = Util.getFormInstanceOrganization(this);
        setTextFieldTextSafely(mFormOrganization, instanceTitle);
    }

    private void setTextFieldTextSafely(EditText editText, String value) {
        if (isValidValue(value)) {
            editText.setText(value);
        }
    }

    private boolean isValidValue(String value) {
        return value != null && value.length() > 0;
    }

    private void saveFormTitle(String title) {
        mFormTitleNeedsSaving = false;
        if (isDatabaseEmpty())
            insertNewRow(title);
        else
            updateExistingRow(title);
    }

    private boolean isDatabaseEmpty() {
        Cursor cursor = null;
        try {
            cursor = Collect.getInstance().getContentResolver().query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + "=?", new String[]{getInstancePath(),}, null);
            if (cursor == null)
                return true;

            cursor.moveToFirst();
            return cursor.getCount() == 0;
        } finally {
            cursor.close();
        }
    }

    private String getInstancePath() {
        return Collect.getInstance().getFormController().getInstancePath().getAbsolutePath();
    }

    private void updateExistingRow(String title) {
        String path = getInstancePath();
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, title);
        updatedValues.put(InstanceProviderAPI.InstanceColumns.FORM_INSTANCE_AUTHOR, mFormAuthor.getText().toString());
        updatedValues.put(InstanceProviderAPI.InstanceColumns.FORM_INSTANCE_ORGANIZATION, mFormOrganization.getText().toString());

        getContentResolver().update(InstanceProviderAPI.InstanceColumns.CONTENT_URI, updatedValues, InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + "=?", new String[]{path,});
    }

    private void insertNewRow(String title) {
        Cursor cursor = Collect.getInstance().getContentResolver().query(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null, null, null);
        cursor.moveToFirst();
        String jrformid = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_FORM_ID));
        String jrversion = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.JR_VERSION));
        String submissionUri = null;
        if ( !cursor.isNull(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.SUBMISSION_URI)) ) {
            submissionUri = cursor.getString(cursor.getColumnIndex(FormsProviderAPI.FormsColumns.SUBMISSION_URI));
        }

        String path = getInstancePath();
        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, title);
        values.put(InstanceProviderAPI.InstanceColumns.FORM_INSTANCE_AUTHOR, mFormAuthor.getText().toString());
        values.put(InstanceProviderAPI.InstanceColumns.FORM_INSTANCE_ORGANIZATION, mFormOrganization.getText().toString());

        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, path);
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, jrformid);
        values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, jrversion);
        values.put(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI, submissionUri);
        getContentResolver().insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);
        cursor.close();
    }

    /**
     * Override refreshView to only iterate over groups, and to not step within groups
     */
    @Override
    public void refreshView() {
        try {
            FormController formController = Collect.getInstance().getFormController();
            // Record the current index so we can return to the same place if the user hits 'back'.
            currentIndex = formController.getFormIndex();

            // If we're not at the first level, we're inside a repeated group so we want to only display
            // everything enclosed within that group.
            String contextGroupRef = "";
            formList = new ArrayList<HierarchyElement>();

            // We always want to go back to the beginning of group page.  Group page only displays group fields.
            formController.jumpToIndex(FormIndex.createBeginningOfFormIndex());

            int event = formController.getEvent();
            if (event == FormEntryController.EVENT_BEGINNING_OF_FORM) {
                // The beginning of form has no valid prompt to display.
                formController.stepToNextEvent(FormController.STEP_OVER_GROUP);
                contextGroupRef = formController.getFormIndex().getReference().getParentRef().toString(true);
                mPath.setVisibility(View.GONE);
                jumpPreviousButton.setEnabled(false);
            } else {
                mPath.setVisibility(View.VISIBLE);
                mPath.setText(getCurrentPath());
                jumpPreviousButton.setEnabled(true);
            }

            // Refresh the current event in case we did step forward.
            event = formController.getEvent();

            // Big change from prior implementation:
            //
            // The ref strings now include the instance number designations
            // i.e., [0], [1], etc. of the repeat groups (and also [1] for
            // non-repeat elements).
            //
            // The contextGroupRef is now also valid for the top-level form.
            //
            // The repeatGroupRef is null if we are not skipping a repeat
            // section.
            //
            String repeatGroupRef = null;
            FormDef formDef = formController.getFormDef();
            List<IFormElement> childrenOfRoot = formDef.getChildren();
            event_search:
            while (event != FormEntryController.EVENT_END_OF_FORM) {

                // get the ref to this element
                String currentRef = formController.getFormIndex().getReference().toString(true);

                // retrieve the current group
                String curGroup = (repeatGroupRef == null) ? contextGroupRef : repeatGroupRef;

                if (!currentRef.startsWith(curGroup)) {
                    // We have left the current group
                    if ( repeatGroupRef == null ) {
                        // We are done.
                        break event_search;
                    } else {
                        // exit the inner repeat group
                        repeatGroupRef = null;
                    }
                }

                if (repeatGroupRef != null) {
                    // We're in a repeat group within the one we want to list
                    // skip this question/group/repeat and move to the next index.
                    event = formController.stepToNextEvent(FormController.STEP_OVER_GROUP);
                    continue;
                }

                switch (event) {
                    case FormEntryController.EVENT_QUESTION:
                        FormEntryPrompt formEntryPrompt = formController.getQuestionPrompt();
                        IFormElement formElement = formEntryPrompt.getFormElement();
                        boolean isOutsideOfAGroup = childrenOfRoot.contains(formElement);
                        if (isOutsideOfAGroup) {
                            String questionLabel = formEntryPrompt.getLongText();
                            if (!formEntryPrompt.isReadOnly() || (questionLabel != null && questionLabel.length() > 0)) {
                                // show the question if it is an editable field.
                                // or if it is read-only and the label is not blank.
                                String orphanedQuestionGroupName = getString(R.string.label_auto_generated_orphan_group_name);
                                HierarchyElement hierarchyElement = new HierarchyElement(orphanedQuestionGroupName, formEntryPrompt.getAnswerText(), null, Color.WHITE, QUESTION, formEntryPrompt.getIndex());
                                formList.add(hierarchyElement);

                                //Loop past all questions
                                while (event == (FormEntryController.EVENT_QUESTION)) {
                                    event = formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
                                }
                            }
                        }

                        break;
                    case FormEntryController.EVENT_GROUP:
                        // add group events
                        FormEntryCaption fec = formController.getCaptionPrompt();
                        formList.add(new HierarchyElement(fec.getLongText(), null, null, Color.WHITE, GROUP, fec.getIndex()));

                        break;
                    case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                        // this would display the 'add new repeat' dialog
                        // ignore it.
                        break;
                    case FormEntryController.EVENT_REPEAT:
                        FormEntryCaption fc = formController.getCaptionPrompt();
                        // push this repeat onto the stack.
                        repeatGroupRef = currentRef;
                        // Because of the guard conditions above, we will skip
                        // everything until we exit this repeat.
                        //
                        // Note that currentRef includes the multiplicity of the
                        // repeat (e.g., [0], [1], ...), so every repeat will be
                        // detected as different and reach this case statement.
                        // Only the [0] emits the repeat header.
                        // Every one displays the descend-into action element.

                        if (fc.getMultiplicity() == 0) {
                            // Display the repeat header for the group.
                            HierarchyElement group = new HierarchyElement(fc.getLongText(), null, getResources().getDrawable(org.odk.collect.android.R.drawable.expander_ic_minimized), Color.WHITE, COLLAPSED, fc.getIndex());
                            formList.add(group);
                        }
                        // Add this group name to the drop down list for this repeating group.
                        HierarchyElement h = formList.get(formList.size() - 1);
                        h.addChild(new HierarchyElement(mIndent + fc.getLongText() + " " + (fc.getMultiplicity() + 1), null, null, Color.WHITE, CHILD, fc.getIndex()));
                        break;
                }
                event = formController.stepToNextEvent(FormController.STEP_INTO_GROUP);
            }

            setAdapterItems(formList);

            // set the controller back to the current index in case the user hits 'back'
            formController.jumpToIndex(currentIndex);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            createErrorDialog(e.getMessage());
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        HierarchyElement h = (HierarchyElement) l.getItemAtPosition(position);
        FormIndex index = h.getFormIndex();
        if (index == null) {
            goUpLevel();
            return;
        }

        switch (h.getType()) {
            case GROUP:
            case COLLAPSED:
                Collect.getInstance().getFormController().jumpToIndex(index);
                setResult(RESULT_OK);
                goBackToMainFormEntryActivity();
                break;
            case QUESTION:
                Collect.getInstance().getActivityLogger().logInstanceAction(this, "onListItemClick", "QUESTION-JUMP", index);
                Collect.getInstance().getFormController().jumpToIndex(index);
                if ( Collect.getInstance().getFormController().indexIsInFieldList() ) {
                    try {
                        Collect.getInstance().getFormController().stepToPreviousScreenEvent();
                    } catch (JavaRosaException e) {
                        Log.e(TAG, e.getMessage(), e);
                        createErrorDialog(e.getCause().getMessage());
                        return;
                    }
                }
                setResult(RESULT_OK);
                goBackToMainFormEntryActivity();
                return;
            default:
                throw new IllegalStateException(getString(R.string.error_message_activity_should_only_should_groups, h.getType()));
        }
    }

    private void goBackToMainFormEntryActivity() {
        Intent entryIntent = new Intent(this, MainFormEntryActivity.class);
        entryIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(entryIntent);
    }

    @Override
    public void onBackPressed() {
        if (mFormTitleNeedsSaving) {
            saveFormTitle(mFormTitle.getText().toString());
        }

        // Go back to MainActivity
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
    }

    @Override
    protected void setAdapterItems(List<HierarchyElement> elements) {
        Log.i("list", "setting array adapter");
        ArrayAdapter<HierarchyElement> adapter = new ArrayAdapter(this, R.layout.group_list_item, elements);
        setListAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.show_version_menu_item:
                MainActivity.showVersionNumberAsToast(this);
                return true;

            case R.id.show_receiving_contact_public_key_menu_item:
                MainActivity.showReceivingContactPublicKey(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    private ProgressDialogHandler getProgressDialogHandler() {
        return mProgressDialogHandler;
    }
}
