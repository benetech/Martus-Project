package org.benetech.secureapp.collect.provider;

import org.martus.android.library.io.SecureFile;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.provider.InstanceProvider;
import org.odk.collect.android.utilities.MediaUtils;

import android.util.Log;

/**
 * An InstanceProvider that channels all File I/O through IOCipher
 * 
 * @author David Brodsky (dbro@dbro.pro)
 *
 */
public class SecureInstanceProvider extends InstanceProvider {
	private static final String t = "SecureInstanceProvider";
	
	protected void deleteAllFilesInDirectory(String directoryPath) {
    	SecureFile directory = new SecureFile(directoryPath);
        if (directory.exists()) {
        	// do not delete the directory if it might be an
        	// ODK Tables instance data directory. Let ODK Tables
        	// manage the lifetimes of its filled-in form data
        	// media attachments.
            if (directory.isDirectory() && !Collect.isODKTablesInstanceDataDirectory(directory)) {
            	// delete any media entries for files in this directory...
                int images = MediaUtils.deleteImagesInFolderFromMediaProvider(directory);
                int audio = MediaUtils.deleteAudioInFolderFromMediaProvider(directory);
                int video = MediaUtils.deleteVideoInFolderFromMediaProvider(directory);

                Log.i(t, "removed from content providers: " + images
                        + " image files, " + audio + " audio files,"
                        + " and " + video + " video files.");

                // delete all the files in the directory
                SecureFile[] files = directory.listFiles();
                for (SecureFile f : files) {
                    // should make this recursive if we get worried about
                    // the media directory containing directories
                    f.delete();
                }
            }
            directory.delete();
        }
    }
	
	protected java.io.File getParent(String childPath) {
    	return new SecureFile(childPath).getParentFile();
    }

}
