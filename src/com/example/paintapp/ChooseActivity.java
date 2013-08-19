package com.example.paintapp;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ChooseActivity extends Activity {

	private final String TAG = getClass().getSimpleName();

	public static final String BITMAP = "bitmap";
	public static final String IMAGE_PATH = "imagePath";
	private Button fromGallery, captureFromCamera;
	private ImageView capturedImage;
	public static final int SUCCESS = 1;
	public static final int FAILURE = 2;

	private static final int TAKE_PICTURE = 0;
	private static final int EDIT_PICTURE = 1;
	private static final int SELECT_PICTURE = 2;
	private Uri mUri;
	private File photoFile;
	public static Bitmap mPhoto;
	String JPEG_FILE_PREFIX = "IMG";

	String JPEG_FILE_SUFFIX = ".jpg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose);

		//fromGallery = (Button) findViewById(R.id.chooseGallery);
		captureFromCamera = (Button) findViewById(R.id.chooseCaptureFromCamera);

		capturedImage = (ImageView) findViewById(R.id.capturedImage);

		/*fromGallery.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						SELECT_PICTURE);
			}
		});*/
		captureFromCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				photoFile = new File(Environment.getExternalStorageDirectory(),
						"photo.jpg");
				mUri = Uri.fromFile(photoFile);
				Intent intent = new Intent(ChooseActivity.this,
						CameraActivity.class);
				intent.putExtra(IMAGE_PATH, photoFile.getAbsolutePath());
				startActivityForResult(intent, TAKE_PICTURE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TAKE_PICTURE && resultCode == SUCCESS) {
			getContentResolver().notifyChange(mUri, null);
			// ContentResolver cr = getContentResolver();
			try {
				// Log.v(TAG,"Orientation is : "+data.getIntExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,-1000));
				new LoadCapturedImageTask().execute();
			} catch (Exception e) {
			}
		} else if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
			Uri selectedImageUri = data.getData();
			photoFile = new File(getPath(selectedImageUri));
			mUri = Uri.fromFile(photoFile);
			getContentResolver().notifyChange(mUri, null);
			new LoadCapturedImageTask().execute();
		} else if (requestCode == EDIT_PICTURE
				&& resultCode == FingerPaint.SUCCESS) {
			Log.v(TAG, "Here too");

			mPhoto = (Bitmap) data.getParcelableExtra(BITMAP);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			mPhoto.compress(Bitmap.CompressFormat.PNG, 100, bos);
			Drawable drawable = new BitmapDrawable(getResources(), mPhoto);
			capturedImage.setBackgroundDrawable(drawable);
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public static void displayAlert(Context context, String msg) {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setMessage(context.getString(R.string.app_name));
		alert.setMessage(msg);
		alert.setPositiveButton(context.getString(R.string.btn_ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});
		alert.show();
	}

	private Bitmap rotateImage(Bitmap bitmap, int angle, int width, int height) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);

		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	}

	class LoadCapturedImageTask extends AsyncTask<Void, Void, Void> {

		ProgressDialog progress;

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(ChooseActivity.this, "",
					getString(R.string.msg_please_wait));
		}

		@Override
		protected Void doInBackground(Void... params) {
			/*
			 * mPhoto = android.provider.MediaStore.Images.Media.getBitmap(cr,
			 * mUri);
			 */
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inSampleSize = 2;
			Log.v(TAG, "Path : " + photoFile.getAbsolutePath());
			mPhoto = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), o)
					.copy(Bitmap.Config.ARGB_8888, true);
			int height = mPhoto.getHeight();
			int width = mPhoto.getWidth();
			/*
			 * Matrix matrix = new Matrix(); matrix.postRotate(90);
			 * 
			 * mPhoto = Bitmap.createBitmap(mPhoto, 0, 0, width, height, matrix,
			 * true);
			 */
			mPhoto = rotateImage(mPhoto, 90, width, height);

			getContentResolver().notifyChange(mUri, null);
			Log.v(TAG,
					"getOrientation : "
							+ getOrientation(ChooseActivity.this,
									photoFile.getAbsolutePath()));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progress.dismiss();
			Intent intent = new Intent(ChooseActivity.this, FingerPaint.class);
			intent.putExtra(BITMAP, mPhoto);
			startActivityForResult(intent, EDIT_PICTURE);
		}
	}

	public static int getOrientation(Context context, String imagePath) {
		int rotate = 0;
		try {
			File imageFile = new File(imagePath);
			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			}

			Log.v("", "Exif orientation: " + orientation);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rotate;
	}
}
