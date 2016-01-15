/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.benetech.secureapp.collect.tasks;

import java.io.IOException;

import org.benetech.secureapp.collect.io.SecureFileStorageManager;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.martus.android.library.io.SecureFile;
import org.odk.collect.android.listeners.SavePointListener;
import org.odk.collect.android.tasks.SavePointTask;

/**
 * Background task for saving a partially complete form to secure storage.
 * @author David Brodsky (dbro@dbro.pro)
 */
public class SecureSavePointTask extends SavePointTask {

	private final static String t = "SavePointTask";
    private final SecureFileStorageManager mSecureStorage;

    public SecureSavePointTask(SecureFileStorageManager secureStorage,
			SavePointListener listener) {
		super(listener);
		mSecureStorage = secureStorage;
	}
    
    @Override
    protected void exportXmlFile(ByteArrayPayload payload, String path) throws IOException {
        SecureFile file = new SecureFile(path);
        if (file.exists() && !file.delete()) {
            throw new IOException("Cannot overwrite " + path + ". Perhaps the file is locked?");
        }

        try {
            mSecureStorage.writeFile(path, payload.getPayloadStream());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
