package org.benetech.secureapp.clientside;

import org.martus.android.library.io.SecureFile;
import org.martus.common.bulletin.AttachmentProxy;

/**
 * Created by animal@martus.org on 4/23/15.
 */
public class SecureMobileAttachmentProxy extends AttachmentProxy {

    private SecureFile mSecureFile;

    public SecureMobileAttachmentProxy(SecureFile securefileToAttach) {
        super(securefileToAttach);

        mSecureFile = securefileToAttach;
    }

    @Override
    public SecureFile getFile() {
        return mSecureFile;
    }
}
