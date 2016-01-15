package org.benetech.secureapp.clientside;

import org.martus.clientside.MobileClientBulletinStore;
import org.martus.common.FieldSpecCollection;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;

/**
 * Created by animal@martus.org on 4/23/15.
 */
public class SecureMobileClientBulletinStore extends MobileClientBulletinStore {
    public SecureMobileClientBulletinStore(MartusCrypto cryptoToUse) {
        super(cryptoToUse);
    }

    public Bulletin createEmptyBulletin(FieldSpecCollection topSectionSpecs, FieldSpecCollection bottomSectionSpecs) throws Exception {
        return new SecureMobileBulletin(getSignatureGenerator(), topSectionSpecs, bottomSectionSpecs);
    }
}
