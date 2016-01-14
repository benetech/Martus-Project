package org.martus.client.loadtest;

import java.io.File;

import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.packet.UniversalId;

/**
 * @author roms
 *         Date: 2/1/13
 */
public class BulletinSenderRunnable implements Runnable
{

    public BulletinSenderRunnable(MartusSecurity crypto, ClientSideNetworkGateway gateway, File zippedFile,
          UniversalId bulletinId)
    {
        this.martusCrypto = crypto;
        this.gateway = gateway;
        this.zippedFile = zippedFile;
        this.bulletinId = bulletinId;
    }

    public void run()
    {
        try
        {
            String result = ServerLoader.uploadBulletinZipFile(bulletinId, zippedFile, gateway, martusCrypto);
            MartusLogger.log("upload result is " + result);
            zippedFile.delete();
        } catch (Exception e)
        {
            MartusLogger.log("unable to send bulletin");
            MartusLogger.logException(e);
        }
    }

    private MartusSecurity martusCrypto;
    private ClientSideNetworkGateway gateway;
    private UniversalId bulletinId;
    private File zippedFile;
}
