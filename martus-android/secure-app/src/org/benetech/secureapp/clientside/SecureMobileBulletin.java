package org.benetech.secureapp.clientside;

import org.martus.android.library.io.SecureFile;
import org.martus.common.FieldSpecCollection;
import org.martus.common.bulletin.AttachmentProxy;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.SessionKey;
import org.martus.common.packet.AttachmentPacket;
import org.martus.common.packet.BulletinHeaderPacket;

import java.io.IOException;

/**
 * Created by animal@martus.org on 4/23/15.
 */
public class SecureMobileBulletin extends Bulletin {
    public SecureMobileBulletin(MartusCrypto signatureGenerator, FieldSpecCollection topSectionSpecs, FieldSpecCollection bottomSectionSpecs) throws Exception {
        super(signatureGenerator, topSectionSpecs, bottomSectionSpecs);
    }

    @Override
    public void addPublicAttachment(AttachmentProxy attachmentProxy) throws IOException, MartusCrypto.EncryptionException {
        //TODO this implementation is almost a duplicate of the parent method.  Due to code freeze in desktop code, we
        //postponed refactoring of parent method. The parent should be refactored to expose only parts of this method
        //that can be then overriden.
        BulletinHeaderPacket bhp = getBulletinHeaderPacket();
        SecureMobileAttachmentProxy secureAttachmentProxy = (SecureMobileAttachmentProxy) attachmentProxy;
        SecureFile secureRawFile = secureAttachmentProxy.getFile();
        if(secureRawFile != null)
        {
            SessionKey sessionKey = getSignatureGenerator().createSessionKey();
            AttachmentPacket ap = new SecureMobileAttachmentPacket(getAccount(), sessionKey, secureRawFile, getSignatureGenerator());
            bhp.addPublicAttachmentLocalId(ap.getLocalId());
            attachmentProxy.setPendingPacket(ap, sessionKey);
        }
        else
        {
            bhp.addPublicAttachmentLocalId(attachmentProxy.getUniversalId().getLocalId());
        }

        getFieldDataPacket().addAttachment(attachmentProxy);
    }
}
