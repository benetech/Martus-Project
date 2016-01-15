package org.benetech.secureapp.tasks;

import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.FileDatabase;
import org.martus.common.packet.UniversalId;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by animal@martus.org on 9/23/14.
 */
abstract public class AbstractSingleBulletinDataBase extends Database {

    @Override
    public long getPacketTimestamp(DatabaseKey key) throws IOException, Database.RecordHiddenException
    {
        return new Date().getTime();
    }

    @Override
    public int getRecordSize(DatabaseKey databaseKey) throws IOException, Database.RecordHiddenException {
        throw new RuntimeException("getRecordSize method should not be called in " + getClass().getName());
    }

    @Override
    public long getmTime(DatabaseKey databaseKey) throws IOException, Database.RecordHiddenException {
        throw new RuntimeException("getmTime method should not be called in " + getClass().getName());
    }

    @Override
    public String readRecord(DatabaseKey databaseKey, MartusCrypto martusCrypto) throws IOException, MartusCrypto.CryptoException {
        throw new RuntimeException("readRecord method should not be called in " + getClass().getName());
    }

    @Override
    public void visitAllAccounts(AccountVisitor accountVisitor) {
        throw new RuntimeException("visitAllAccounts method should not be called in " + getClass().getName());
    }

    @Override
    public void visitAllRecordsForAccount(PacketVisitor packetVisitor, String s) {
        throw new RuntimeException("visitAllRecordsForAccount method should not be called in " + getClass().getName());
    }

    @Override
    public boolean isHidden(UniversalId universalId) {
        throw new RuntimeException("isHidden method should not be called in " + getClass().getName());
    }

    @Override
    public boolean isHidden(DatabaseKey databaseKey) {
        throw new RuntimeException("isHidden method should not be called in " + getClass().getName());
    }

    @Override
    public String getFolderForAccount(String s) throws IOException {
        throw new RuntimeException("getFolderForAccount method should not be called in " + getClass().getName());
    }

    @Override
    public void verifyAccountMap() throws MartusUtilities.FileVerificationException, FileDatabase.MissingAccountMapSignatureException {
        throw new RuntimeException("verifyAccountMap method should not be called in " + getClass().getName());
    }

    @Override
    public boolean isInQuarantine(DatabaseKey databaseKey) throws Database.RecordHiddenException {
        throw new RuntimeException("isInQuarantine method should not be called in " + getClass().getName());
    }

    @Override
    public void deleteAllData() throws Exception {
        throw new RuntimeException("deleteAllData method should not be called in " + getClass().getName());
    }

    @Override
    public void initialize() throws MartusUtilities.FileVerificationException, FileDatabase.MissingAccountMapException, FileDatabase.MissingAccountMapSignatureException {
        throw new RuntimeException("initialize method should not be called in " + getClass().getName());
    }

    @Override
    public void writeRecord(DatabaseKey key, InputStream record) throws IOException, RecordHiddenException {
        throw new RuntimeException("writeRecord method should not be called in " + getClass().getName());
    }

    @Override
    public void writeRecordEncrypted(DatabaseKey key, String record, MartusCrypto encrypter) throws IOException, RecordHiddenException, MartusCrypto.CryptoException {
        throw new RuntimeException("writeRecordEncrypted method should not be called in " + getClass().getName());
    }

    @Override
    public void discardRecord(DatabaseKey key) {
        throw new RuntimeException("discardRecord method should not be called in " + getClass().getName());
    }

    @Override
    public File getIncomingInterimFile(UniversalId uid) throws IOException, RecordHiddenException {
        throw new RuntimeException("getIncomingInterimFile method should not be called in " + getClass().getName());
    }

    @Override
    public File getOutgoingInterimFile(UniversalId uid) throws IOException, RecordHiddenException {
        throw new RuntimeException("getOutgoingInterimFile method should not be called in " + getClass().getName());
    }

    @Override
    public File getOutgoingInterimPublicOnlyFile(UniversalId uid) throws IOException, RecordHiddenException {
        throw new RuntimeException("getOutgoingInterimPublicOnlyFile method should not be called in " + getClass().getName());
    }

    @Override
    public File getContactInfoFile(String accountId) throws IOException {
        throw new RuntimeException("getContactInfoFile method should not be called in " + getClass().getName());
    }

    @Override
    public File getAbsoluteAccountAccessTokenFolderForAccount(String accountId) throws IOException {
        throw new RuntimeException("getAbsoluteAccountAccessTokenFolderForAccount method should not be called in " + getClass().getName());
    }

    @Override
    public File getAccountAccessTokenFile(String accountId, MartusAccountAccessToken token) throws IOException {
        throw new RuntimeException("getAccountAccessTokenFile method should not be called in " + getClass().getName());
    }

    @Override
    public File getAbsoluteFormTemplatesFolderForAccount(String accountId) throws IOException {
        throw new RuntimeException("getAbsoluteFormTemplatesFolderForAccount method should not be called in " + getClass().getName());
    }

    @Override
    public void moveRecordToQuarantine(DatabaseKey key) throws RecordHiddenException {
        throw new RuntimeException("moveRecordToQuarantine method should not be called in " + getClass().getName());
    }

    @Override
    public void signAccountMap() throws IOException, MartusCrypto.MartusSignatureException {
        throw new RuntimeException("signAccountMap method should not be called in " + getClass().getName());
    }

    @Override
    public void scrubRecord(DatabaseKey key) throws IOException, RecordHiddenException {
        throw new RuntimeException("scrubRecord method should not be called in " + getClass().getName());
    }
}
