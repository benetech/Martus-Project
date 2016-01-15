package org.benetech.secureapp.utilities;

import org.apache.commons.io.FileUtils;
import org.martus.android.library.io.SecureFile;
import org.martus.android.library.io.SecureFileInputStream;
import org.martus.android.library.io.SecureFileOutputStream;
import org.martus.util.UnicodeReader;

import java.io.FileNotFoundException;
import java.io.IOException;

import info.guardianproject.iocipher.IOCipherFileChannel;

/**
 * Created by animal@martus.org on 9/16/14.
 */
public class Utility {

    private static final long FILE_COPY_BUFFER_SIZE = FileUtils.ONE_MB * 30;

    public static char[] convertToCharArray(CharSequence passphrase) {
        char[] passphraseChars = new char[passphrase.length()];
        for (int index = 0; index < passphrase.length(); ++index) {
            passphraseChars[index] = passphrase.charAt(index);
        }

        return passphraseChars;
    }

    public static char[] convertToCharArray(byte[] encryptionKey) {
        StringBuffer tempKeyHolder = new StringBuffer();
        for (int index = 0; index < encryptionKey.length; ++index) {
            tempKeyHolder.append(encryptionKey[index]);
        }

        char[] keyAsCharArray = new char[tempKeyHolder.length()];
        for (int index = 0; index < tempKeyHolder.length(); ++index) {
            keyAsCharArray[index] = tempKeyHolder.charAt(index);
        }
        return keyAsCharArray;
    }

    public static String ioCipherFileToString(SecureFile file) throws Exception {
        SecureFileInputStream fileInputStream = new SecureFileInputStream(file);
        UnicodeReader reader = new UnicodeReader(fileInputStream);

        return reader.readAll();
    }

    public static void copySecureFile(SecureFile sourceFile, SecureFile destinationFile) throws Exception {
        copySecureFile(sourceFile, destinationFile, true);
    }

    public static void copySecureFile(SecureFile sourceFile, SecureFile destinationFile, boolean preserveFileDate) throws Exception {
        if (!sourceFile.exists())
            throw new FileNotFoundException("Copy failed due to missing from secure file: " + sourceFile.getAbsolutePath());

        if (!destinationFile.exists())
            destinationFile.createNewFile();

        if (!destinationFile.exists())
            throw new FileNotFoundException("Could not create secure file to copy to: " + destinationFile.getAbsolutePath());

        if (destinationFile.exists() && destinationFile.isDirectory())
            throw new IOException("Destination '" + destinationFile + "' exists but is a directory");

        SecureFileInputStream fis = null;
        SecureFileOutputStream fos = null;
        IOCipherFileChannel input = null;
        IOCipherFileChannel output = null;
        try {
            fis = new SecureFileInputStream(sourceFile);
            fos = new SecureFileOutputStream(destinationFile);
            input  = fis.getChannel();
            output = fos.getChannel();
            final long size = input.size();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                final long remain = size - pos;
                count = remain > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : remain;
                final long bytesCopied = output.transferFrom(input, pos, count);
                if (bytesCopied == 0) {
                    break;
                }
                pos += bytesCopied;
            }
        } finally {
            if (fis != null)
                fis.close();

            if (fos != null)
                fos.close();

            if (input != null)
                input.close();

            if (output != null)
                output.close();
        }

        final long srcLen = sourceFile.length();
        final long dstLen = destinationFile.length();
        if (srcLen != dstLen) {
            throw new IOException("Failed to copy full contents from '" + sourceFile + "' to '" + destinationFile + "' Expected length: " + srcLen +" Actual: " + dstLen);
        }

        if (preserveFileDate) {
            destinationFile.setLastModified(sourceFile.lastModified());
        }
    }
}
