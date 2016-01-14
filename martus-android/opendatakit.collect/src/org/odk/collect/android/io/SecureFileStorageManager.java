package org.odk.collect.android.io;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileInputStream;
import info.guardianproject.iocipher.IOCipherFileChannel;
import info.guardianproject.iocipher.VirtualFileSystem;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import android.util.Log;

/**
 * Wrapper around a secure file storage system. This implementation uses
 * IOCipher https://guardianproject.info/code/iocipher/
 * 
 * @author davidbrodsky
 * 
 */
public class SecureFileStorageManager {
	private static final String TAG = "SecureFileStorageManager";

	private String mVirtualFilesystemPath;
	private VirtualFileSystem mVfs;

	/**
	 * Create a new secure virtual file system hosted within the provided file.
	 */
	public SecureFileStorageManager(String virtualFilesystemPath) {
		mVirtualFilesystemPath = virtualFilesystemPath;
	}

	/**
	 * Mount the secure virtual file system using the given decryption key
	 */
	public void mountFilesystem(String password) {
		if (mVfs != null && mVfs.isMounted()) {
			Log.w(TAG,
					"mountFilesystem called with filesystem all ready mounted. Ignoring.");
			return;
		}
		mVfs = new VirtualFileSystem(mVirtualFilesystemPath);
		mVfs.mount(password);
	}

	/**
	 * Unmount the secure virtual file system.
	 */
	public void unmountFilesystem() {
		if (mVfs == null || !mVfs.isMounted()) {
			Log.w(TAG,
					"unmountFilesystem called without filesystem mounted. Ignoring.");
			return;
		}
		mVfs.unmount();
	}

	public boolean isFilesystemMounted() {
		return mVfs.isMounted();
	}

	/**
	 * Write an InputStream to a virtual file within the secure file system.
	 * 
	 * @param fileName
	 * @param is
	 * @throws IOException 
	 */
	public void writeFile(String fileName, InputStream is) throws IOException {
		info.guardianproject.iocipher.FileOutputStream fos = new info.guardianproject.iocipher.FileOutputStream(
				fileName);

		ReadableByteChannel sourceFileChannel = Channels.newChannel(is);
		IOCipherFileChannel destinationFileChannel = fos.getChannel();
		destinationFileChannel.transferFrom(sourceFileChannel, 0,
				is.available());
		
		// Testing
//			byte[] writtenFile = readFile(fileName);
//			TreeElement savedRoot = XFormParser.restoreDataModel(writtenFile, null).getRoot();

		Log.i(TAG, "Writing " + fileName);
		
	}
	
	/**
	 * Returns a BufferedInputStream corresponding to the data 
	 * stored in the virtual secure filesystem as fileName.
	 * 
	 * @throws FileNotFoundException if fileName does not exist in the virtual
	 * secure filesystem.
	 */
	public byte[] readFile(String fileName) throws FileNotFoundException {
		Log.i(TAG, "Reading " + fileName);
		FileInputStream fis = openFile(fileName);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead;
		try {
			while ((bytesRead = fis.read(buffer)) != -1) {
				bos.write(buffer, 0, bytesRead);
			}
			return bos.toByteArray();
		} catch (IOException e) {
			Log.w(TAG, "Error reading " + fileName);
			e.printStackTrace();
		}
		return null;
	}
	
	public FileInputStream openFile(String fileName) throws FileNotFoundException {
		return new FileInputStream(new File(fileName));
	}

}
