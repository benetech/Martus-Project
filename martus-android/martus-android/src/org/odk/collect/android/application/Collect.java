/*
 * Copyright (C) 2011 University of Washington
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
 *
 * This class modified extensively by Benetech (Rom Srinivasan)
 *
 */

package org.odk.collect.android.application;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.martus.android.AppConfig;
import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.PropertyManager;


import java.io.File;

/**
 * Extends the Application class to implement
 *
 * @author carlhartung
 */
public class Collect  {

    // Storage paths
    public static String ODK_ROOT;
    public static String FORMS_PATH;
    public static String INSTANCES_PATH;
    public static String CACHE_PATH;
    public static String METADATA_PATH;
    public static String TMPFILE_PATH;
    public static String TMPDRAWFILE_PATH;
	public static final String FORMS_DIR_NAME = "odk";

    public static String MARTUS_TEMPLATE_PATH;

    private ActivityLogger mActivityLogger;
    private FormController mFormController = null;

    private static Collect singleton = null;
	private Context context;

    public static Collect getInstance() {
        return singleton;
    }

    public ActivityLogger getActivityLogger() {
        return mActivityLogger;
    }

    public FormController getFormController() {
        return mFormController;
    }

    public void setFormController(FormController controller) {
        mFormController = controller;
    }

    /**
     * Creates required directories on the SDCard (or other external storage)
     *
     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
     */
    public static void createODKDirs() throws RuntimeException {


        String[] dirs = {
                ODK_ROOT, FORMS_PATH, INSTANCES_PATH, CACHE_PATH, METADATA_PATH, MARTUS_TEMPLATE_PATH
        };

        for (String dirName : dirs) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    RuntimeException e =
                            new RuntimeException("ODK reports :: Cannot create directory: "
                                    + dirName);
                    throw e;
                }
            } else {
                if (!dir.isDirectory()) {
                    RuntimeException e =
                            new RuntimeException("ODK reports :: " + dirName
                                    + " exists, but is not a directory");
                    throw e;
                }
            }
        }
    }

    /**
     * Predicate that tests whether a directory path might refer to an
     * ODK Tables instance data directory (e.g., for media attachments).
     *
     * @param directory
     * @return
     */
    public static boolean isODKTablesInstanceDataDirectory(File directory) {
		/**
		 * Special check to prevent deletion of files that
		 * could be in use by ODK Tables.
		 */
    	String dirPath = directory.getAbsolutePath();
    	if ( dirPath.startsWith(Collect.ODK_ROOT) ) {
    		dirPath = dirPath.substring(Collect.ODK_ROOT.length());
    		String[] parts = dirPath.split(File.separator);
    		// [appName, instances, tableId, instanceId ]
    		if ( parts.length == 4 && parts[1].equals("instances") ) {
    			return true;
    		}
    	}
    	return false;
	}

	public static void initInstance(Context context ) {
	        if (singleton == null) {
	            singleton = new Collect(context);
	        }
	    }

	private Collect(Context context) {
		ODK_ROOT = context.getCacheDir().getParent() + File.separator + FORMS_DIR_NAME;
		FORMS_PATH = ODK_ROOT + File.separator + "forms";
		INSTANCES_PATH = ODK_ROOT + File.separator + "instances";
		CACHE_PATH = ODK_ROOT + File.separator + ".cache";
		METADATA_PATH = ODK_ROOT + File.separator + "metadata";
		TMPFILE_PATH = CACHE_PATH + File.separator + "tmp.jpg";
		TMPDRAWFILE_PATH = CACHE_PATH + File.separator + "tmpDraw.jpg";
		MARTUS_TEMPLATE_PATH = ODK_ROOT + File.separator + "template";

		createODKDirs();
		this.context = context;
        mActivityLogger = new ActivityLogger();
	}

}
