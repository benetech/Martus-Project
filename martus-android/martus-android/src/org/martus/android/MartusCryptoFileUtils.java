package org.martus.android;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.packet.Packet;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author roms
 *         Date: 10/1/13
 */
public class MartusCryptoFileUtils
{

	public static void encryptAndWriteFileAndSignatureFile(File dataFile, File signatureFile,
					InputStream is, MartusSecurity martusCrypto) throws Exception
	{
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
			martusCrypto.setShouldWriteAuthorDecryptableData(true);
			martusCrypto.encrypt(is, fileOutputStream);
			martusCrypto.setShouldWriteAuthorDecryptableData(false);

			fileOutputStream.close();
			is.close();

			FileInputStream in = new FileInputStream(dataFile);
			byte[] signature = martusCrypto.createSignatureOfStream(in);
			in.close();

			FileOutputStream out = new FileOutputStream(signatureFile);
			out.write(signature);
			out.close();
		} catch (Exception e) {
			throw e;
		}  finally {
			martusCrypto.setShouldWriteAuthorDecryptableData(false);
		}
	}

	public static byte[] verifyAndReadSignedFile(File dataFile, File sigFile, MartusSecurity martusCrypto) throws Exception
	{
		if(!isSignatureFileValid(dataFile, sigFile, martusCrypto))
			throw new Packet.SignatureVerificationException();

		InputStreamWithSeek encryptedInputStream = new FileInputStreamWithSeek(dataFile);
		ByteArrayOutputStream plainTextStream = new ByteArrayOutputStream();
		martusCrypto.decrypt(encryptedInputStream, plainTextStream);

		byte[] plainText = plainTextStream.toByteArray();

		plainTextStream.close();
		encryptedInputStream.close();
		return plainText;
	}

	private static boolean isSignatureFileValid(File dataFile, File sigFile, MartusSecurity martusCrypto) throws IOException, MartusCrypto.MartusSignatureException
	{
		byte[] signature =	new byte[(int)sigFile.length()];
		FileInputStream inSignature = new FileInputStream(sigFile);
		inSignature.read(signature);
		inSignature.close();

		FileInputStream inData = new FileInputStream(dataFile);
		try
		{
			boolean verified = martusCrypto.isValidSignatureOfStream(martusCrypto.getPublicKeyString(), inData, signature);
			return verified;
		}
		finally
		{
			inData.close();
		}
	}

}
