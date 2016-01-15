package org.benetech.secureapp.collect.io;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.benetech.secureapp.R;
import org.benetech.secureapp.application.MainApplication;
import org.martus.android.library.io.SecureFile;
import org.martus.android.library.io.SecureFileInputStream;
import org.martus.android.library.io.SecureFileOutputStream;
import org.martus.util.UnicodeReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.iocipher.IOCipherFileChannel;
import info.guardianproject.iocipher.VirtualFileSystem;

/**
 * Wrapper around a secure file storage system. This implementation uses
 * IOCipher https://guardianproject.info/code/iocipher/
 * 
 * @author David Brodsky (dbro@dbro.pro)
 * 
 */
public class SecureFileStorageManager {
	private static final String TAG = "SecureFileSM";
	private String mVirtualFilesystemPath;
    public static final String XFORMS_DIR_NAME = "xforms";
    private static final String DB_FILE_NAME = "secureApp.db";

	/**
	 * Create a new secure virtual file system hosted within the provided file.
	 */
	public SecureFileStorageManager(CacheWordHandler cacheWordActivityHandler, String virtualFilesystemPath) {
        if (cacheWordActivityHandler == null)
            throw new IllegalArgumentException(MainApplication.getInstance().getString(R.string.error_message_cacheWord_activity_handler_is_null));

		mVirtualFilesystemPath = virtualFilesystemPath;
	}

	/**
	 * Mount the secure virtual file system using the given decryption key
	 */
	public void mountFilesystem(Context context, byte[] passPhrase) {
        java.io.File dbFile;
        if (mVirtualFilesystemPath == null) {
            dbFile = new java.io.File(context.getDir("vfs", Context.MODE_PRIVATE),  DB_FILE_NAME);
        }
        else {
            dbFile = new java.io.File(mVirtualFilesystemPath);
        }

        dbFile.getParentFile().mkdirs();
        if (!dbFile.exists()) {
            VirtualFileSystem.get().createNewContainer(dbFile.getAbsolutePath(), passPhrase);
        }

        if (!VirtualFileSystem.get().isMounted()) {
            VirtualFileSystem.get().mount(dbFile.getAbsolutePath(), passPhrase);
        }
    }

    /**
	 * Unmount the secure virtual file system.
	 */
	public void unmountFilesystem() {
        try
        {
            VirtualFileSystem.get().unmount();
			if (VirtualFileSystem.get().isMounted())
				throw new Exception("Failed to unmount secure file system");
        }
		catch (IllegalStateException e)
		{
			Log.e("IOCipher","error unmounting - still active?", e);
		}
		catch (Exception e )
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public boolean isFilesystemMounted() {
		return VirtualFileSystem.get().isMounted();
	}

	/**
	 * Write an InputStream to a virtual file within the secure file system.
	 * 
	 * @param fileName
	 * @param is
	 * @throws IOException 
	 */
	public void writeFile(String fileName, InputStream is) throws Exception {
		SecureFileOutputStream fos = new SecureFileOutputStream(fileName);

		ReadableByteChannel sourceFileChannel = Channels.newChannel(is);
		IOCipherFileChannel destinationFileChannel = fos.getChannel();
		destinationFileChannel.transferFrom(sourceFileChannel, 0, is.available());
        sourceFileChannel.close();
        destinationFileChannel.close();

		Log.i(TAG, "Writing " + fileName);
	}

	/**
	 * Returns a BufferedInputStream corresponding to the data 
	 * stored in the virtual secure filesystem as fileName.
	 * 
	 * @throws FileNotFoundException if fileName does not exist in the virtual
	 * secure filesystem.
	 */
	public byte[] readFile(String fileName) throws Exception {
		Log.i(TAG, "Reading " + fileName);

		SecureFileInputStream fis = openFile(fileName);
		UnicodeReader unicodeReader = new UnicodeReader(fis);
		try {
			return unicodeReader.readAll().getBytes("UTF-8");
		} catch (IOException e) {
			Log.e(TAG, MainApplication.getInstance().getString(R.string.error_message_error_reading, fileName), e);
		}
		finally {
			fis.close();
			unicodeReader.close();
		}
		return null;
	}
	
	public static SecureFileInputStream openFile(String fileName) throws FileNotFoundException {
		return new SecureFileInputStream(new SecureFile(fileName));
	}

    public String getSecureFileSystemPath() {
        return mVirtualFilesystemPath;
    }

    public SecureFile getSecureFileSysteDir() {
        return new SecureFile(getSecureFileSystemPath());
    }

    public SecureFile getXFormsDir() {
        return new SecureFile(getSecureFileSysteDir(), XFORMS_DIR_NAME);
    }
}
