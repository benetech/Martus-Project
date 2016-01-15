/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.validate;

import android.util.Log;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.InvalidReferenceException;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.util.XFormUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Uses the javarosa-core library to process a form and show errors, if any.
 *
 * @author Adam Lerer (adam.lerer@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormValidator {
    /**
     * Classes needed to serialize objects. Need to put anything from JR in here.
     */
    public final static String[] SERIALIABLE_CLASSES = {
            "org.javarosa.core.services.locale.ResourceFileDataSource", // JavaRosaCoreModule
            "org.javarosa.core.services.locale.TableLocaleSource", // JavaRosaCoreModule
            "org.javarosa.core.model.FormDef",
            "org.javarosa.core.model.SubmissionProfile", // CoreModelModule
            "org.javarosa.core.model.QuestionDef", // CoreModelModule
            "org.javarosa.core.model.GroupDef", // CoreModelModule
            "org.javarosa.core.model.instance.FormInstance", // CoreModelModule
            "org.javarosa.core.model.data.BooleanData", // CoreModelModule
            "org.javarosa.core.model.data.DateData", // CoreModelModule
            "org.javarosa.core.model.data.DateTimeData", // CoreModelModule
            "org.javarosa.core.model.data.DecimalData", // CoreModelModule
            "org.javarosa.core.model.data.GeoPointData", // CoreModelModule
            "org.javarosa.core.model.data.GeoShapeData", // CoreModelModule
            "org.javarosa.core.model.data.GeoTraceData", // CoreModelModule
            "org.javarosa.core.model.data.IntegerData", // CoreModelModule
            "org.javarosa.core.model.data.LongData", // CoreModelModule
            "org.javarosa.core.model.data.MultiPointerAnswerData", // CoreModelModule
            "org.javarosa.core.model.data.PointerAnswerData", // CoreModelModule
            "org.javarosa.core.model.data.SelectMultiData", // CoreModelModule
            "org.javarosa.core.model.data.SelectOneData", // CoreModelModule
            "org.javarosa.core.model.data.StringData", // CoreModelModule
            "org.javarosa.core.model.data.TimeData", // CoreModelModule
            "org.javarosa.core.model.data.UncastData", // CoreModelModule
            "org.javarosa.core.model.data.helper.BasicDataPointer", // CoreModelModule
            "org.javarosa.core.model.Action", // CoreModelModule
            "org.javarosa.core.model.actions.SetValueAction" //CoreModelModule
    };

    private boolean inError = false;

    public static void main(String[] args) {
        if ( args.length == 1 ) {
            String path = args[0];
            new FormValidator(path);
        } else {
           System.out.println("Please enter one path to form to be validated");
        }
    }

    public FormValidator() {
    }

    public FormValidator(String path) {
        try {
            validate(path);
        } catch (Exception e ) {
            System.err.println("\nException: " + e.toString());
            setError(true);
        }

        if ( inError ) {
            System.err.println("\nResult: Invalid");
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    private void setError(boolean outcome) {
        inError = outcome;
    }

    boolean stepThroughEntireForm(FormEntryModel model) throws InvalidReferenceException {
        boolean outcome = false;
        Set<String> loops = new HashSet<String>();
        // step through every value in the form
        FormIndex idx = FormIndex.createBeginningOfFormIndex();
        int event;
        for (;;) {
            idx = model.incrementIndex(idx);
            event = model.getEvent(idx);
            if ( event == FormEntryController.EVENT_END_OF_FORM ) break;

            if (event == FormEntryController.EVENT_PROMPT_NEW_REPEAT) {
                String elementPath = idx.getReference().toString().replaceAll("\\[\\d+\\]", "");
                if ( !loops.contains(elementPath) ) {
                    loops.add(elementPath);
                    model.getForm().createNewRepeat(idx);
                    idx = model.getFormIndex();
                }
            } else if (event == FormEntryController.EVENT_GROUP) {
                GroupDef gd = (GroupDef) model.getForm().getChild(idx);
                if ( gd.getChildren() == null || gd.getChildren().size() == 0 ) {
                    outcome = true;
                    setError(true);
                    String elementPath = idx.getReference().toString().replaceAll("\\[\\d+\\]", "");
                    System.err.println("Group has no children! Group: " + elementPath + ". The XML is invalid.\n");
                }
            } else if (event != FormEntryController.EVENT_QUESTION) {
                continue;
            } else {
                FormEntryPrompt prompt = model.getQuestionPrompt(idx);
                if ( prompt.getControlType() == Constants.CONTROL_SELECT_MULTI ||
                        prompt.getControlType() == Constants.CONTROL_SELECT_ONE ) {
                    String elementPath = idx.getReference().toString().replaceAll("\\[\\d+\\]", "");
                    List<SelectChoice> items;
                    items = prompt.getSelectChoices();
                    // check for null values...
                    for ( int i = 0 ; i < items.size() ; ++i ) {
                        SelectChoice s = items.get(i);
                        String text = prompt.getSelectChoiceText(s);
                        String image = prompt.getSpecialFormSelectChoiceText(s,
                                FormEntryCaption.TEXT_FORM_IMAGE);
                        if ((text == null || text.trim().length() == 0 ) &&
                                (image == null || image.trim().length() == 0)) {
                            System.err.println("Selection choice label text and image uri are both missing for: " + elementPath + " choice: " + (i+1) + ".\n");
                        }
                        if ( s.getValue() == null || s.getValue().trim().length() == 0) {
                            outcome = true;
                            setError(true);
                            System.err.println("Selection value is missing for: " + elementPath + " choice: " + (i+1) + ". The XML is invalid.\n");
                        }
                    }
                }
            }
        }
        return outcome;
    }


//    void validate(String path) {
//
//        File src = new File(path);
//        if ( !src.exists() ) {
//            setError(true);
//            System.err.println("File: " + src.getAbsolutePath() + " does not exist.");
//            return;
//        }
//
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream(src);
//
//            // validate well formed xml
//            // System.out.println("Checking form...");
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            factory.setNamespaceAware(true);
//            try {
//                factory.newDocumentBuilder().parse(new File(path));
//            } catch (Exception e) {
//                setError(true);
//                System.err.println("\n\n\n>> XML is invalid. See above for the errors.");
//                return;
//            }
//
//            // need a list of classes that formdef uses
//            // unfortunately, the JR registerModule() functions do more than this.
//            // register just the classes that would have been registered by:
//            // new JavaRosaCoreModule().registerModule();
//            // new CoreModelModule().registerModule();
//            // replace with direct call to PrototypeManager
//            PrototypeManager.registerPrototypes(SERIALIABLE_CLASSES);
//            // initialize XForms module
//            new XFormsModule().registerModule();
//
//
//            // needed to override rms property manager
//            org.javarosa.core.services.PropertyManager
//                    .setPropertyManager(new StubPropertyManager());
//
//            // validate if the xform can be parsed.
//            try {
//                FormDef fd = XFormUtils.getFormFromInputStream(fis);
//                if (fd == null) {
//                    setError(true);
//                    System.err.println("\n\n\n>> Something broke the parser. Try again.");
//                    return;
//                }
//
//                // make sure properties get loaded
//                fd.getPreloader().addPreloadHandler(new FakePreloadHandler("property"));
//
//                // update evaluation context for function handlers
//                fd.getEvaluationContext().addFunctionHandler(new IFunctionHandler() {
//
//                    public String getName() {
//                        return "pulldata";
//                    }
//
//                    public List<Class[]> getPrototypes() {
//                        return new ArrayList<Class[]>();
//                    }
//
//                    public boolean rawArgs() {
//                        return true;
//                    }
//
//                    public boolean realTime() {
//                        return false;
//                    }
//
//                    public Object eval(Object[] args, EvaluationContext ec) {
//                        // no actual implementation here -- just a stub to facilitate validation
//                        return args[0];
//                    }});
//
//                // check for runtime errors
//                fd.initialize(true, new InstanceInitializationFactory());
//
//                System.out.println("\n\n>> Xform parsing completed! See above for any warnings.\n");
//
//                // create FormEntryController from formdef
//                FormEntryModel fem = new FormEntryModel(fd);
//
//                // and try to step through the form...
//                if ( stepThroughEntireForm(fem) ) {
//                    setError(true);
//                    System.err.println("\n\n>> Xform is invalid! See above for errors and warnings.");
//                } else {
//                    System.out.println("\n\n>> Xform is valid! See above for any warnings.");
//                }
//
//            } catch (XFormParseException e) {
//                setError(true);
//                System.err.println(e.toString());
//                e.printStackTrace();
//                System.err.println("\n\n>> XForm is invalid. See above for the errors.");
//
//            } catch (Exception e) {
//                setError(true);
//                System.err.println(e.toString());
//                e.printStackTrace();
//                System.err.println("\n\n>> Something broke the parser. See above for a hint.");
//
//            }
//        } catch (FileNotFoundException e) {
//            setError(true);
//            System.err.println("Please choose a file before attempting to validate.");
//            return;
//        } finally {
//            if ( fis != null ) {
//                try {
//                    fis.close();
//                } catch (IOException e) {
//                    // ignore
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//

    void validate(String path) {

        File src = new File(path);
        if ( !src.exists() ) {
            setError(true);
            System.err.println("File: " + src.getAbsolutePath() + " does not exist.");
            return;
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(src);
        } catch (FileNotFoundException e) {
            setError(true);
            System.err.println("Please choose a file before attempting to validate.");
            return;
        }
        new XFormsModule().registerModule();

        // validate if the xform can be parsed.
        validate(fis);
    }

    public void validate(InputStream is) {
        if (!is.markSupported()) throw new IllegalArgumentException("InputStream must support marking");
        is.mark(Integer.MAX_VALUE);
        // validate well formed xml
        // System.out.println("Checking form...");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.newDocumentBuilder().parse(is);
        } catch (Exception e) {
            setError(true);
            System.err.println("\n\n\n>> XML is invalid. See above for the errors.");
            return;
        }

        // need a list of classes that formdef uses
        // unfortunately, the JR registerModule() functions do more than this.
        // register just the classes that would have been registered by:
        // new JavaRosaCoreModule().registerModule();
        // new CoreModelModule().registerModule();
        // replace with direct call to PrototypeManager
        PrototypeManager.registerPrototypes(SERIALIABLE_CLASSES);
        // initialize XForms module

        // Re-open InputStream, as it was consumed by DocumentBuilderFactory#newDocumentBuilder()#parse(InputStream) above
        try {
            is.reset();
        } catch (IOException e1) {
            Log.i("validate", "failed to reset InputStream");
            e1.printStackTrace();
        }


        try {
            FormDef fd = XFormUtils.getFormFromInputStream(is);
            if (fd == null) {
                setError(true);
                System.err.println("\n\n\n>> Something broke the parser. Try again.");
                return;
            }

            // make sure properties get loaded
            fd.getPreloader().addPreloadHandler(new FakePreloadHandler("property"));

            // new evaluation context for function handlers
            EvaluationContext ec = new EvaluationContext(null);
            ec.addFunctionHandler(new IFunctionHandler() {

                public String getName() {
                    return "pulldata";
                }

                @SuppressWarnings("rawtypes")
                public Vector getPrototypes() {
                    return new Vector();
                }

                public boolean rawArgs() {
                    return true;
                }

                public boolean realTime() {
                    return false;
                }

                public Object eval(Object[] args, EvaluationContext ec) {
                    // no actual implementation here -- just a stub to facilitate validation
                    return args[0];
                }});

            // check for runtime errors
            fd.initialize(true, new InstanceInitializationFactory());

            System.out.println("\n\n>> Xform parsing completed! See above for any warnings.\n");

            // create FormEntryController from formdef
            FormEntryModel fem = new FormEntryModel(fd);

            // and try to step through the form...
            if ( stepThroughEntireForm(fem) ) {
                setError(true);
                System.err.println("\n\n>> Xform is invalid! See above for errors and warnings.");
            } else {
                System.out.println("\n\n>> Xform is valid! See above for any warnings.");
            }

        } catch (XFormParseException e) {
            setError(true);
            System.err.println(e.toString());
            e.printStackTrace();
            System.err.println("\n\n>> XForm is invalid. See above for the errors.");

        } catch (Exception e) {
            setError(true);
            System.err.println(e.toString());
            e.printStackTrace();
            System.err.println("\n\n>> Something broke the parser. See above for a hint.");

        }
    }

    private class FakePreloadHandler implements IPreloadHandler {

        String preloadHandled;


        public FakePreloadHandler(String preloadHandled) {
            this.preloadHandled = preloadHandled;
        }


        public boolean handlePostProcess(TreeElement arg0, String arg1) {
            // TODO Auto-generated method stub
            return false;
        }


        public IAnswerData handlePreload(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }


        public String preloadHandled() {
            // TODO Auto-generated method stub
            return preloadHandled;
        }

    }

}

