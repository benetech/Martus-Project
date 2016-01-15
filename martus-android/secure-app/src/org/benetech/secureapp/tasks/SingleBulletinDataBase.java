package org.benetech.secureapp.tasks;

import android.os.Environment;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.DatabaseKey;
import org.martus.util.UnicodeReader;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;
import org.martus.util.inputstreamwithseek.StringInputStreamWithSeek;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by animal@martus.org on 9/22/14.
 */

public class SingleBulletinDataBase extends AbstractSingleBulletinDataBase {

    public SingleBulletinDataBase() throws Exception {

        deleteAllData();
    }

    @Override
    public void deleteAllData() throws Exception {
        databaseKeyToStringMap = new HashMap<DatabaseKey, String>();
    }

    @Override
    public InputStreamWithSeek openInputStream(DatabaseKey rawKey, MartusCrypto decryptor) throws IOException, MartusCrypto.CryptoException {
        DatabaseKey legacyDatabaseKey = createLegacyDatabaseKey(rawKey);
        String packetAsString = getDatabaseKeyToStringMap().get(legacyDatabaseKey);

        return new StringInputStreamWithSeek(packetAsString);
    }

    @Override
    public void writeRecord(DatabaseKey rawKey, String record) throws IOException, RecordHiddenException {
        DatabaseKey legacyDatabaseKey = createLegacyDatabaseKey(rawKey);
        getDatabaseKeyToStringMap().put(legacyDatabaseKey, record);
    }

    @Override
    public void visitAllRecords(PacketVisitor visitor) {
        Set keys = getDatabaseKeyToStringMap().keySet();
        Iterator iterator = keys.iterator();
        while(iterator.hasNext())
        {
            DatabaseKey key = (DatabaseKey)iterator.next();
            try
            {
                visitor.visit(key);
            }
            catch (RuntimeException nothingWeCanDoAboutIt)
            {
                // nothing we can do, so ignore it
            }
        }
    }

    @Override
    public boolean doesRecordExist(DatabaseKey rawKey) {
        DatabaseKey legacyDatabaseKey = createLegacyDatabaseKey(rawKey);

        return getDatabaseKeyToStringMap().containsKey(legacyDatabaseKey);
    }

    private DatabaseKey createLegacyDatabaseKey(DatabaseKey rawKey) {
        return DatabaseKey.createLegacyKey(rawKey.getUniversalId());
    }

    private HashMap<DatabaseKey, String> getDatabaseKeyToStringMap() {
        return databaseKeyToStringMap;
    }

    @Override
    public void importFiles(HashMap fileMapping) throws IOException, RecordHiddenException {
        Iterator keys = fileMapping.keySet().iterator();
        while(keys.hasNext()) {
            DatabaseKey key = (DatabaseKey) keys.next();
            File fromFile = (File) fileMapping.get(key);
            String asString = loadFileContentAsUtf8(fromFile);
            getDatabaseKeyToStringMap().put(key, asString);
        }
    }

    private String loadFileContentAsUtf8(File fileToReadIn) throws IOException, RecordHiddenException {
        FileInputStreamWithSeek inputFileStream = new FileInputStreamWithSeek(fileToReadIn);
        try {
            UnicodeReader reader = new UnicodeReader(inputFileStream);

            return reader.readAll();
        } finally {
            inputFileStream.close();
        }
    }

    //FIXME urgent - this needs to be inspected:
    // - is this the correct location we want
    // - are unecryped files being written to this location
    // - should this be dir in IOCipher
    @Override
    public File getInterimDirectory(String accountId) throws IOException {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }



    private HashMap<DatabaseKey, String> databaseKeyToStringMap;
}
