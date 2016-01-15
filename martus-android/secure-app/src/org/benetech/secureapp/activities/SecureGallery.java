package org.benetech.secureapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.martus.android.library.io.SecureFile;
import org.benetech.secureapp.R;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileInputStream;
import info.guardianproject.iocipher.camera.viewer.ImageViewerActivity;

/**
 * Created by animal@martus.org on 5/13/15.
 */
public class SecureGallery extends Activity {

    private final static String TAG = "SecureGallery";
    public static final String SECURE_GALLERY_PATH_TAG = "secureGalleryPath";
    private List<String> imagePaths = null;
    private List<String> selectedPaths = null;
    private String galleryPath;

    private GridView gridview;
    private TextView emptyGalleryLabel;
    private HashMap<String,Bitmap> mBitCache = new HashMap<String, Bitmap>();
    private HashMap<String, BitmapWorkerThread> mBitLoaders = new HashMap<String, BitmapWorkerThread>();
    private Handler uiEventHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery);
        gridview = (GridView) findViewById(R.id.gridview);
        emptyGalleryLabel = (TextView) findViewById(R.id.empty_label_id);
        galleryPath = getIntent().getExtras().getString(SECURE_GALLERY_PATH_TAG);

        preventScreenShots();
        createImageGridView();
    }

    private void preventScreenShots() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    public void createImageGridView() {
        fillPathsArrayInReverse();

        gridview.setAdapter(new IconicList());
        gridview.setOnItemClickListener(new ThumbnailClickHandler());
        gridview.setOnItemLongClickListener(new ImageLongClickHandler());
    }

    private void fillPathsArrayInReverse() {
        imagePaths = new ArrayList<String>();
        SecureFile[] childrenGalleryImageFiles = getGalleryDir().listFiles();
        for (int index = childrenGalleryImageFiles.length - 1; index >= 0; index--) {
            File imageFile = childrenGalleryImageFiles[index];
            imagePaths.add(imageFile.getPath());
        }

        if (imagePaths.isEmpty())
            emptyGalleryLabel.setVisibility(View.VISIBLE);
        else
            emptyGalleryLabel.setVisibility(View.GONE);
    }

    private class ImageLongClickHandler implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            SecureFile file = new SecureFile(imagePaths.get(position));
            showThumbnailDialog(file);

            return true;
        }
    }

    private SecureFile getGalleryDir() {
        return new SecureFile(galleryPath);
    }

    private void showThumbnailDialog(final SecureFile file) {
        new AlertDialog.Builder(SecureGallery.this)
                .setIcon(R.drawable.ic_launcher)
                .setTitle("[" + file.getName() + "]")
                .setNegativeButton("Delete", new DeleteImageHandler(file))
                .setPositiveButton("Preview", new PreviewImageHandler(file)).show();
    }

    private class PreviewImageHandler implements  DialogInterface.OnClickListener {
        private File fileToPreview;
        public PreviewImageHandler(File fileToUse) {
            fileToPreview = fileToUse;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            showItem(fileToPreview);
        }
    }

    private class DeleteImageHandler implements DialogInterface.OnClickListener {
        private File fileToDelete;
        public DeleteImageHandler(File fileToUse) {
            fileToDelete = fileToUse;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            fileToDelete.delete();
            createImageGridView();
        }
    }

    private void showItem (File file)
    {
        try {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            if (mimeType.startsWith("image")) {
                Intent intent = new Intent(SecureGallery.this,ImageViewerActivity.class);
                intent.setType(mimeType);
                intent.putExtra("vfs", file.getAbsolutePath());
                startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, getString(R.string.error_message_no_relevant_activity_found), e);
        }
    }

    protected Bitmap getPreview(File fileImage) throws FileNotFoundException {
        Bitmap bitmap;

        synchronized (mBitCache) {
            bitmap = mBitCache.get(fileImage.getAbsolutePath());
            if (bitmap == null && mBitLoaders.get(fileImage.getAbsolutePath())==null) {
                BitmapWorkerThread bwt = new BitmapWorkerThread(fileImage);
                mBitLoaders.put(fileImage.getAbsolutePath(),bwt);
                bwt.start();
            }
        }

        return bitmap;
    }

    private static class ViewHolder {
        ImageView icon;
    }

    private class IconicList extends ArrayAdapter<Object> {

        public IconicList() {
            super(SecureGallery.this, R.layout.gallery_gridsq, imagePaths.toArray());
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.gallery_gridsq, null);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (holder == null)
            {
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(info.guardianproject.iocipher.camera.R.id.icon);
                holder.icon.setImageResource(info.guardianproject.iocipher.camera.R.drawable.text);
            }

            SecureFile imageFileForPosition = new SecureFile(imagePaths.get(position));
            String mimeType = null;
            String[] tokens = imageFileForPosition.getName().split("\\.(?=[^\\.]+$)");

            if (tokens.length > 1)
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(imageFileForPosition.getName().split("\\.")[1]);

            if (mimeType.startsWith("image")) {
                try {
                    Bitmap thumbnailPreview = getPreview(imageFileForPosition);
                    if (thumbnailPreview == null)
                        setPlaceholderImageBeforeImageIsLoaded(holder);
                    else
                        holder.icon.setImageBitmap(thumbnailPreview);
                }
                catch (Exception e) {
                    Log.d(TAG, getString(R.string.error_message_error_showing_thumbnail), e);
                }
            }

            return (convertView);
        }

        private void setPlaceholderImageBeforeImageIsLoaded(ViewHolder viewHolder) {
            viewHolder.icon.setImageResource(info.guardianproject.iocipher.camera.R.drawable.jpeg);
        }
    }

    //THIS CLASS IS DUPLICATE OF GAURDIAN'S GALLERYACTIVITY.BitmapWorkerThread
    private class BitmapWorkerThread extends Thread {
        private File imageFile;

        public BitmapWorkerThread (File imageFileToUse) {
            imageFile = imageFileToUse;
        }

        public void run () {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inSampleSize = 8;
            Bitmap bitmap;
            try {
                FileInputStream fis = new FileInputStream(imageFile);
                bitmap = BitmapFactory.decodeStream(fis, null, bounds);
                fis.close();
                mBitCache.put(imageFile.getAbsolutePath(), bitmap);
                mBitLoaders.remove(imageFile.getAbsolutePath());
                uiEventHandler.post(new NotifyDataChangedThread());
            } catch (Exception e) {
                Log.e(TAG, getString(R.string.error_message_error_decoding_bitmap_preview),e);
            }
        }
    }

    private class NotifyDataChangedThread implements Runnable {
        public void run() {
            ((IconicList) gridview.getAdapter()).notifyDataSetChanged();
        }
    }

    private class ThumbnailClickHandler implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String pathToClickedImage = imagePaths.get(position);
            if (selectedPaths == null)
                selectedPaths = new ArrayList<String>();

            if (selectedPaths.contains(pathToClickedImage))
                selectThumbNail(view, pathToClickedImage);
            else
                unselectThumbnail(view, pathToClickedImage);


            Intent intentResult = new Intent().putExtra(MediaStore.EXTRA_OUTPUT, selectedPaths.toArray(new String[selectedPaths.size()]));
            int resultCode = Activity.RESULT_OK;
            if (selectedPaths.isEmpty())
                resultCode = Activity.RESULT_CANCELED;

            setResult(resultCode, intentResult);
        }

        private void unselectThumbnail(View view, String pathToClickedImage) {
            selectedPaths.add(pathToClickedImage);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(getResources().getColor(R.color.selection_blue));
            gd.setStroke(1, 0xFF000000);
            view.setBackgroundDrawable(gd);
        }

        private void selectThumbNail(View view, String pathToClickedImage) {
            selectedPaths.remove(pathToClickedImage);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(getResources().getColor(R.color.white));
            view.setBackgroundDrawable(gd);
        }
    }
}
