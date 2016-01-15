package org.benetech.secureapp.utilities;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.ValidateOutcome;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;

import java.io.ByteArrayInputStream;

/**
 * Created by animal@martus.org on 5/4/15.
 */
public class OdkFormInstanceValidator {

    private String formModel;
    private String formInstance;

    public OdkFormInstanceValidator(String formModelToUse, String formInstanceToUse) {
        formModel = formModelToUse;
        formInstance = formInstanceToUse;
        initializeJavaRosa();
    }

    public int validateUserAnswersOneAtATime() throws Exception {
        FormDef formDef = XFormUtils.getFormFromInputStream(new ByteArrayInputStream(getFormModel().getBytes("UTF-8")));
        FormEntryModel formEntryModel = new FormEntryModel(formDef);
        FormEntryController formEntryController = new FormEntryController(formEntryModel);
        TreeElement modelRootElement = formEntryController.getModel().getForm().getInstance().getRoot().deepCopy(true);
        TreeElement instanceRootElement = XFormParser.restoreDataModel(getFormInstance().getBytes("UTF-8"), null).getRoot();

        populateDataModel(modelRootElement);
        modelRootElement.populate(instanceRootElement, formEntryController.getModel().getForm());
        populateFormEntryControllerModel(formEntryController, modelRootElement);
        fixLanguageIusses(formEntryController);

        return createFieldSpecsFromXForms(formEntryController);
    }

    private int createFieldSpecsFromXForms(FormEntryController formEntryController) throws Exception
    {
        FormDef formDef = formEntryController.getModel().getForm();
        ValidateOutcome outcome = formDef.validate(true);
        if (outcome == null)
            return FormEntryController.ANSWER_OK;

        return outcome.outcome;
    }

    private void populateFormEntryControllerModel(FormEntryController formEntryController, TreeElement modelRoot)
    {
        formEntryController.getModel().getForm().getInstance().setRoot(modelRoot);
    }

    private void fixLanguageIusses(FormEntryController formEntryController)
    {
        //NOTE: this comment is from Collect's java rosa seference
        // fix any language issues
        // : http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
        if (formEntryController.getModel().getLanguages() != null)
            formEntryController.getModel().getForm().localeChanged(formEntryController.getModel().getLanguage(), formEntryController.getModel().getForm().getLocalizer());
    }

    private void populateDataModel(TreeElement modelRootElement)
    {
        TreeReference treeReference = TreeReference.rootRef();
        treeReference.add(modelRootElement.getName(), TreeReference.INDEX_UNBOUND);
    }

    private static final void initializeJavaRosa()
    {
        new XFormsModule().registerModule();
    }

    private String getFormInstance() {
        return formInstance;
    }

    private String getFormModel() {
        return formModel;
    }
}
