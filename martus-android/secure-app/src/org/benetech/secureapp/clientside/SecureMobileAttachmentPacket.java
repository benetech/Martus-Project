package org.benetech.secureapp.clientside;

import org.martus.android.library.io.SecureFile;
import org.martus.android.library.io.SecureFileInputStream;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.SessionKey;
import org.martus.common.packet.AttachmentPacket;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by animal@martus.org on 4/23/15.
 */
public class SecureMobileAttachmentPacket extends AttachmentPacket{
    public SecureMobileAttachmentPacket(String account, SessionKey sessionKeyToUse, SecureFile secureFileToAttach, MartusCrypto crypto) {
        super(account, sessionKeyToUse, secureFileToAttach, crypto);

        mSecureFile = secureFileToAttach;
    }

    @Override
    protected InputStream createFileInputStream() throws FileNotFoundException {
        return new SecureFileInputStream(getSecureRawFile());
    }

    private SecureFile getSecureRawFile() {
        return mSecureFile;
    }

    private SecureFile mSecureFile;
}
