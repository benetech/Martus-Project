/*
 * Copyright (C) 2009 University of Washington
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

package org.benetech.secureapp.collect.tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.benetech.secureapp.collect.io.SecureFileStorageManager;
import org.odk.collect.android.tasks.SaveToDiskTask;

import android.net.Uri;

/**
 * Background task for saving a complete form to secure storage.
 *
 * @author David Brodsky (dbro@dbro.pro)
 */
public class SecureSaveToDiskTask extends SaveToDiskTask {
    private final static String t = "SaveToDiskTask";

    private SecureFileStorageManager mSecureStorage;

    public SecureSaveToDiskTask(Uri uri, SecureFileStorageManager secureStorage, Boolean saveAndExit, Boolean markCompleted, String updatedName) {
        super(uri, saveAndExit, markCompleted, updatedName);
    	mSecureStorage = secureStorage;
    }


    /**
     * This method actually writes the xml to disk.
     * @param payload
     * @param path
     * @return
     */
    @Override
    protected void exportXmlFile(ByteArrayPayload payload, String path) throws IOException {
    	File file = new File(path);
        if (file.exists() && !file.delete()) {
            throw new IOException("Cannot overwrite " + path + ". Perhaps the file is locked?");
        }

        // create data stream
        InputStream is = payload.getPayloadStream();
        int len = (int) payload.getLength();

        // read from data stream
        try {
            mSecureStorage.writeFile(path, is);
        } catch (Exception e) {
            throw new IOException(e);
        }

    }
}
