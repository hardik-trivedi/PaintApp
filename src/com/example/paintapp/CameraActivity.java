package com.example.paintapp;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class CameraActivity extends Activity {
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
	private Button btnCancel, btnCapture;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		preview = (SurfaceView) findViewById(R.id.preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCapture = (Button) findViewById(R.id.btnCapture);

		btnCapture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (inPreview) {
					camera.takePicture(null, null, photoCallback);
					inPreview = false;
				}
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				// AddReportItemActivity.mPhoto =
				// drawView.getDrawingCache();
				setResult(ChooseActivity.FAILURE, intent);
				finish();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		/*
		 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
		 * Camera.CameraInfo info = new Camera.CameraInfo();
		 * 
		 * for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
		 * Camera.getCameraInfo(i, info);
		 * 
		 * if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) { camera =
		 * Camera.open(i); } } }
		 */

		// if (camera == null) {
		camera = Camera.open();
		// }

		startPreview();
	}

	@Override
	public void onPause() {
		if (inPreview) {
			camera.stopPreview();
		}

		if (camera != null) {
			camera.release();
			camera = null;
		}

		inPreview = false;

		super.onPause();
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { new
	 * MenuInflater(this).inflate(R.menu.activity_camera, menu);
	 * 
	 * return (super.onCreateOptionsMenu(menu)); }
	 */

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return (result);
	}

	private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (result == null) {
				result = size;
			} else {
				int resultArea = result.width * result.height;
				int newArea = size.width * size.height;

				if (newArea < resultArea) {
					result = size;
				}
			}
		}

		return (result);
	}

	private void initPreview(int width, int height) {
		if (camera != null && previewHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(previewHolder);
			} catch (Throwable t) {
				Log.e("PreviewDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t);
				Toast.makeText(CameraActivity.this, t.getMessage(),
						Toast.LENGTH_LONG).show();
			}

			if (!cameraConfigured) {
				Camera.Parameters parameters = camera.getParameters();
				Camera.Size size = getBestPreviewSize(width, height, parameters);
				Camera.Size pictureSize = getSmallestPictureSize(parameters);

				if (size != null && pictureSize != null) {
					parameters.setPreviewSize(size.width, size.height);
					parameters.setPictureSize(pictureSize.width,
							pictureSize.height);
					parameters.setPictureFormat(ImageFormat.JPEG);
					camera.setParameters(parameters);
					cameraConfigured = true;
				}
			}
		}
	}

	private void startPreview() {
		if (cameraConfigured && camera != null) {
			camera.setDisplayOrientation(90);
			camera.startPreview();
			inPreview = true;
		}
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// no-op -- wait until surfaceChanged()
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			initPreview(width, height);
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};

	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SavePhotoTask().execute(data);
			// camera.startPreview();
			// inPreview=true;
		}
	};

	class SavePhotoTask extends AsyncTask<byte[], String, Integer> {
		@Override
		protected Integer doInBackground(byte[]... jpeg) {
			File photo = new File(getIntent().getStringExtra(
					ChooseActivity.IMAGE_PATH));

			if (photo.exists()) {
				photo.delete();
			}

			try {
				FileOutputStream fos = new FileOutputStream(photo.getPath());

				fos.write(jpeg[0]);
				fos.close();
			} catch (java.io.IOException e) {
				return ChooseActivity.FAILURE;
			}
			return ChooseActivity.SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == ChooseActivity.SUCCESS) {
				Intent intent = new Intent();
				// AddReportItemActivity.mPhoto =
				// drawView.getDrawingCache();
				setResult(ChooseActivity.SUCCESS, intent);
				finish();
			} else {
				Intent intent = new Intent();
				// AddReportItemActivity.mPhoto =
				// drawView.getDrawingCache();
				setResult(ChooseActivity.FAILURE, intent);
				finish();
			}
		}
	}
}
