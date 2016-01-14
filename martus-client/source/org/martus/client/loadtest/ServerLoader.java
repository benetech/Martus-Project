package org.martus.client.loadtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.network.OrchidTransportWrapperWithActiveProperty;
import org.martus.client.swingui.Martus;
import org.martus.clientside.ClientPortOverride;
import org.martus.clientside.ClientSideNetworkGateway;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinZipUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.common.network.NetworkResponse;
import org.martus.common.network.NonSSLNetworkAPIWithHelpers;
import org.martus.common.network.OrchidTransportWrapper;
import org.martus.common.packet.UniversalId;
import org.martus.util.DirectoryUtils;
import org.martus.util.Stopwatch;
import org.martus.util.StreamableBase64;

/**
 * @author roms
 *         Date: 1/30/13
 */
public class ServerLoader {

    public ServerLoader(String serverIP, String magicWord, int numThreads, int numBulletins)
    {
        this.serverIP = serverIP;
        this.magicWord = magicWord;
        this.numThreads = numThreads;
        this.numBulletins = numBulletins;
    }

    public final static void main( String[] args )
    {
        if( args.length < 4 )
        {
            usage("Not enough arguments.");
        }
        else {

            try
            {
                Martus.addThirdPartyJarsToClasspath();
            }
            catch (Exception e)
            {
                System.out.println("Error loading third-party jars");
                e.printStackTrace();
            }

            String serverIp = args[0];
            String magicWord = args[1];
            int numThreads = Integer.valueOf(args[2]);
            int numBulletins = Integer.valueOf(args[3]);
            
            if(args.length >= 5)
            {
            	String flag = args[4];
            	if(flag.equals("--insecure-ports"))
            	{
        			ClientPortOverride.useInsecurePorts = true;
            	}
            	else
            	{
            		System.err.println("Unknown flag: " + flag);
            		System.exit(1);
            	}
            }

            final ServerLoader loader = new ServerLoader(serverIp, magicWord, numThreads, numBulletins);
            loader.startLoading();
        }
    }

	public void startLoading()
    {

        try
        {
            martusCrypto = new MartusSecurity();
            martusCrypto.createKeyPair();
        } catch (Exception e) {
            MartusLogger.log("unable to create crypto");
            MartusLogger.logException(e);
        }

        store = new ClientBulletinStore(martusCrypto);
        try
        {
            tempDir = DirectoryUtils.createTempDir();
            store.doAfterSigninInitialization(tempDir);
            store.createFieldSpecCacheFromDatabase();
        } catch (Exception e) {
            MartusLogger.log("unable to initialize store");
            MartusLogger.logException(e);
        }

        MartusLogger.log("tempDir is " + tempDir);
        zippedBulletins = new File[numBulletins];
        bulletinIds = new UniversalId[numBulletins];
        try
        {
            createZippedBulletins();

            MartusLogger.log("Verifying server");
            try {
                while (!verifyServer()) {
                    Thread.sleep(5 * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sendBulletins();
        } catch (Exception e) {
            MartusLogger.log("problem sending bulletins");
            MartusLogger.logException(e);
        }
        DirectoryUtils.deleteEntireDirectoryTree(tempDir);
    }

    private void sendBulletins() {
    	Stopwatch sw = new Stopwatch();
        MartusLogger.log("Start sending bulletins");
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int i = 0;
        for (File file : zippedBulletins) {
            Runnable sender = new BulletinSenderRunnable(martusCrypto, gateway, file, bulletinIds[i++]);
            executor.execute(sender);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            //do nothing - just waiting
        }
        MartusLogger.log("Finished sending bulletins to " + serverIP);
        MartusLogger.log("Time required to create " + numBulletins + " bulletins: " + minutesElapsedCreatingBulletins + " minutes.");
        MartusLogger.log("Time required for " + numThreads + " threads to send bulletins: " + sw.elapsedInMinutes() + " minutes.");
    }

    private boolean verifyServer() throws Exception
    {
	    OrchidTransportWrapper transport = OrchidTransportWrapperWithActiveProperty.createWithoutPersistentStore();
        NonSSLNetworkAPIWithHelpers server = new ClientSideNetworkHandlerUsingXmlRpcWithUnverifiedServer(serverIP, transport);
        try
        {
            String result = server.getServerPublicKey(martusCrypto);


            gateway = ClientSideNetworkGateway.buildGateway(serverIP, result, transport);
            NetworkResponse response = gateway.getUploadRights(martusCrypto, magicWord);
            if (!response.getResultCode().equals(NetworkInterfaceConstants.OK))
            {
                MartusLogger.log("couldn't verify magic word");
                return false;
            }
        } catch (Exception e) {
            MartusLogger.log("couldn't verify server");
            return false;
        }
        return true;
    }

    private void createZippedBulletins() throws Exception
    {
    	Stopwatch sw = new Stopwatch();
        MartusLogger.log("Creating bulletins by the hundreds");
        for (int i = 0; i < numBulletins; i++) {
            Bulletin bulletin = createBulletin(i);
            store.saveBulletin(bulletin);
            File file = File.createTempFile("tmp_send_", ".zip", tempDir);
            BulletinZipUtilities.exportBulletinPacketsFromDatabaseToZipFile(store.getDatabase(),
                    bulletin.getDatabaseKey(), file, bulletin.getSignatureGenerator());
            zippedBulletins[i] = file;
            bulletinIds[i] = bulletin.getUniversalId();
            store.destroyBulletin(bulletin);
            if (i > 0 && (i % 100 == 0)) {
                MartusLogger.log("created " + i);
            }
        }
        minutesElapsedCreatingBulletins = sw.elapsedInMinutes();
    }

    private Bulletin createBulletin(int num) throws Exception
    {
        Bulletin b = store.createEmptyBulletin();
        b.set(Bulletin.TAGTITLE, "loadtest title " + num);
        b.set(Bulletin.TAGSUMMARY, "loadtest summary " + num);
        b.setMutable();
        b.setAllPrivate(true);
        return b;
    }

    public static String uploadBulletinZipFile(UniversalId uid, File tempFile, ClientSideNetworkGateway gateway,
            MartusCrypto crypto)
            throws MartusUtilities.FileTooLargeException, IOException, MartusCrypto.MartusSignatureException
    {
        final int totalSize = MartusUtilities.getCappedFileLength(tempFile);
        int offset = 0;
        byte[] rawBytes = new byte[NetworkInterfaceConstants.CLIENT_MAX_CHUNK_SIZE];
        FileInputStream inputStream = new FileInputStream(tempFile);
        String result = null;
        while(true)
        {
            int chunkSize = inputStream.read(rawBytes);
            if(chunkSize <= 0)
                break;
            byte[] chunkBytes = new byte[chunkSize];
            System.arraycopy(rawBytes, 0, chunkBytes, 0, chunkSize);

            String authorId = uid.getAccountId();
            String bulletinLocalId = uid.getLocalId();
            String encoded = StreamableBase64.encode(chunkBytes);

            NetworkResponse response = gateway.putBulletinChunk(crypto,
                                authorId, bulletinLocalId, totalSize, offset, chunkSize, encoded);
            result = response.getResultCode();
            if(!result.equals(NetworkInterfaceConstants.CHUNK_OK) && !result.equals(NetworkInterfaceConstants.OK))
                break;
            offset += chunkSize;
        }
        inputStream.close();
        return result;
    }


    /**
     * Prints command line usage.
     *
     * @param msg A message to include with usage info.
     */
    private static void usage( String msg )
    {
        System.err.println( msg );
        System.err.println( "Usage: java ServerLoader  <server ip> <magic word>  <number of threads> <number of bulletins> [--insecure]" );
    }


    private MartusSecurity martusCrypto;
    private ClientSideNetworkGateway gateway;
    private String serverIP;
    private String magicWord;
    private ClientBulletinStore store;
    private File tempDir;
    private int numThreads;
    private int numBulletins;
    private File[] zippedBulletins;
    private UniversalId[] bulletinIds;
    private int minutesElapsedCreatingBulletins;
}
