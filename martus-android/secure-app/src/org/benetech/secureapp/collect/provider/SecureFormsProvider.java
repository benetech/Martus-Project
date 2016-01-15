package org.benetech.secureapp.collect.provider;

import java.io.FileNotFoundException;

import org.benetech.secureapp.R;
import org.benetech.secureapp.collect.io.SecureFileStorageManager;
import org.martus.android.library.io.SecureFile;
import org.odk.collect.android.provider.FormsProvider;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaUtils;

import android.util.Log;

/**
 * A FormsProvider that channels all File I/O through IOCipher
 * 
 * @author David Brodsky (dbro@dbro.pro)
 *
 */
public class SecureFormsProvider extends FormsProvider {
	private static final String t = "SecureFormsProvider";
	
	protected String getNameForFormAtPath(String path) {
		return new SecureFile(path).getName();
	}
	
	protected String normalizePath(String path) {
		SecureFile form = new SecureFile(path);
		return form.getAbsolutePath();
	}
	
	protected String getMd5HashForFormAtPath(String path) {
		try {
			return FileUtils.getMd5Hash(SecureFileStorageManager.openFile(path));
		} catch (FileNotFoundException e) {
			Log.e(t, getContext().getString(R.string.error_message_could_not_find_data_for_path, path), e);
			return null;
		}
	}

	protected void deleteFileOrDir(String fileName) {
		SecureFile file = new SecureFile(fileName);
		if (file.exists()) {
			if (file.isDirectory()) {
				// delete any media entries for files in this directory...
				int images = MediaUtils
						.deleteImagesInFolderFromMediaProvider(file);
				int audio = MediaUtils
						.deleteAudioInFolderFromMediaProvider(file);
				int video = MediaUtils
						.deleteVideoInFolderFromMediaProvider(file);

				Log.i(t, "removed from content providers: " + images
						+ " image files, " + audio + " audio files," + " and "
						+ video + " video files.");

				// delete all the containing files
				SecureFile[] files = file.listFiles();
				for (SecureFile f : files) {
					// should make this recursive if we get worried about
					// the media directory containing directories
					Log.i(t,
							"attempting to delete file: " + f.getAbsolutePath());
					f.delete();
				}
			}
			file.delete();
			Log.i(t, "attempting to delete file: " + file.getAbsolutePath());
		}
	}

}
