/*******************************************************************************
 * Copyright (c) 2012 BuildManPro LLC
 * 
 * BuildManPro LLC., Sterling, Virginia, USA - All Rights Reserved
 * 
 * 
 * 
 * Developer :
 *       Hardik Trivedi
 ******************************************************************************/
/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.paintapp;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.example.paintapp.yuku.ambilwarna.AmbilWarnaDialog;
import com.example.paintapp.yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

public class FingerPaint extends Activity {
	LinearLayout mainContainer;
	LinearLayout buttonContainer;
	LinearLayout.LayoutParams btnParams;
	Button btnText, btnSketch, btnColor, btnUndo, btnRedo, btnDone;
	// MyView drawView;
	DrawingPanel drawView;
	int lastColor = 0xFFFF0000;
	public static final int SUCCESS = 200;

	private final String TAG = getClass().getSimpleName();
	private String textToDraw = null;
	private boolean isTextModeOn = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		generateViews();
	}

	private void generateViews() {
		btnDone = new Button(this);
		btnDone.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		btnDone.setText(getString(R.string.btn_done));
		btnText = new Button(this);
		btnSketch = new Button(this);
		btnColor = new Button(this);
		btnUndo = new Button(this);
		btnRedo = new Button(this);
		mainContainer = new LinearLayout(this);
		mainContainer.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mainContainer.setOrientation(LinearLayout.VERTICAL);
		buttonContainer = new LinearLayout(this);
		buttonContainer.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
		btnParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, 1);
		/*
		 * btnText.setText("Text"); btnSketch.setText("Sketch");
		 * btnColor.setText("Color"); btnUndo.setText("Undo");
		 * btnRedo.setText("Redo");
		 */

		btnText.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_mode_selector));
		btnText.setLayoutParams(btnParams);
		btnSketch.setBackgroundDrawable(getResources().getDrawable(R.drawable.sketch_selector));
		btnSketch.setLayoutParams(btnParams);
		btnColor.setBackgroundDrawable(getResources().getDrawable(R.drawable.color_selector));
		btnColor.setLayoutParams(btnParams);
		btnUndo.setBackgroundDrawable(getResources().getDrawable(R.drawable.undo_selector));
		btnUndo.setLayoutParams(btnParams);
		btnRedo.setBackgroundDrawable(getResources().getDrawable(R.drawable.redo_selector));
		btnRedo.setLayoutParams(btnParams);
		buttonContainer.addView(btnText);
		buttonContainer.addView(btnSketch);
		buttonContainer.addView(btnColor);
		buttonContainer.addView(btnUndo);
		buttonContainer.addView(btnRedo);
		// drawView=new MyView(this);
		drawView = new DrawingPanel(this, lastColor);
		drawView.setDrawingCacheEnabled(true);
		drawView.measure(
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		drawView.layout(0, 0, drawView.getMeasuredWidth(),
				drawView.getMeasuredHeight());
		drawView.buildDrawingCache(true);
		drawView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));
		mainContainer.addView(btnDone);
		mainContainer.addView(drawView);
		mainContainer.addView(buttonContainer);
		setContentView(mainContainer);
		btnSketch.setSelected(true);
		btnText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				resetButtons();
				btnText.setSelected(true);
				AlertDialog.Builder alert = new AlertDialog.Builder(
						FingerPaint.this);
				alert.setMessage(getString(R.string.msg_enter_text_to_draw));
				final EditText edText = new EditText(FingerPaint.this);
				alert.setView(edText);
				alert.setPositiveButton(R.string.btn_ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								if (edText.getText().toString().length() > 0) {
									textToDraw = edText.getText().toString();
									isTextModeOn = true;
									ChooseActivity.displayAlert(FingerPaint.this,
											getString(R.string.msg_tap_image));
								} else {
									ChooseActivity.displayAlert(
											FingerPaint.this,
											getString(R.string.msg_enter_text_to_draw));
								}
							}
						});
				alert.setNegativeButton(R.string.btn_cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								isTextModeOn = false;
							}
						});
				alert.show();
			}
		});
		btnSketch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				resetButtons();
				btnSketch.setSelected(true);
				isTextModeOn = false;
			}
		});
		btnColor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(
						FingerPaint.this, lastColor,
						new OnAmbilWarnaListener() {
							@Override
							public void onOk(AmbilWarnaDialog dialog, int color) {
								// color is the color selected by the user.
								colorChanged(color);
							}

							@Override
							public void onCancel(AmbilWarnaDialog dialog) {
								// cancel was selected by the user
							}
						});

				dialog.show();
			}
		});

		btnUndo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				drawView.onClickUndo();
			}
		});
		btnRedo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				drawView.onClickRedo();
			}
		});
		btnDone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v(TAG, "Here");
				Bitmap editedImage = Bitmap.createBitmap(drawView
						.getDrawingCache());
				editedImage = Bitmap.createScaledBitmap(editedImage, 200, 300,
						true);
				if (editedImage != null) {
					Intent intent = new Intent();
					intent.putExtra(ChooseActivity.BITMAP, editedImage);
					// AddReportItemActivity.mPhoto =
					// drawView.getDrawingCache();
					setResult(SUCCESS, intent);
					finish();
				}
			}
		});
	}

	public void resetButtons() {
		btnText.setSelected(false);
		btnSketch.setSelected(false);
		btnColor.setSelected(false);
		btnUndo.setSelected(false);
		btnRedo.setSelected(false);
	}

	public class DrawingPanel extends View implements OnTouchListener {

		private Canvas mCanvas;
		private Path mPath;
		private Paint mPaint, mBitmapPaint;
		private ArrayList<PathPoints> paths = new ArrayList<PathPoints>();
		private ArrayList<PathPoints> undonePaths = new ArrayList<PathPoints>();
		private Bitmap mBitmap;
		private int color;
		private int x, y;

		public DrawingPanel(Context context, int color) {
			super(context);
			this.color = color;
			setFocusable(true);
			setFocusableInTouchMode(true);

			this.setOnTouchListener(this);

			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setDither(true);
			mPaint.setColor(color);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(3);
			mPaint.setTextSize(30);

			mPath = new Path();
			paths.add(new PathPoints(mPath, color, false));
			mCanvas = new Canvas();
		}

		public void colorChanged(int color) {
			this.color = color;
			mPaint.setColor(color);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			// mBitmap = AddReportItemActivity.mPhoto;
			mBitmap = getIntent().getExtras().getParcelable(ChooseActivity.BITMAP);
			float xscale = (float) w / (float) mBitmap.getWidth();
			float yscale = (float) h / (float) mBitmap.getHeight();
			if (xscale > yscale) // make sure both dimensions fit (use the
									// smaller scale)
				xscale = yscale;
			float newx = (float) w * xscale;
			float newy = (float) h * xscale; // use the same scale for both
												// dimensions
			// if you want it centered on the display (black borders)
			mBitmap = Bitmap.createScaledBitmap(mBitmap, this.getWidth(),
					this.getHeight(), true);
			// mCanvas = new Canvas(mBitmap);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			for (PathPoints p : paths) {
				mPaint.setColor(p.getColor());
				Log.v("", "Color code : " + p.getColor());
				if (p.isTextToDraw()) {
					canvas.drawText(p.textToDraw, p.x, p.y, mPaint);
				} else {
					canvas.drawPath(p.getPath(), mPaint);
				}
			}
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 0;

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			// commit the path to our offscreen
			mCanvas.drawPath(mPath, mPaint);
			// kill this so we don't double draw
			mPath = new Path();
			paths.add(new PathPoints(mPath, color, false));

		}

		private void drawText(int x, int y) {
			Log.v(TAG, "Here");
			Log.v(TAG, "X " + x + " Y " + y);
			this.x = x;
			this.y = y;
			paths.add(new PathPoints(color, textToDraw, true, x, y));
			// mCanvas.drawText(textToDraw, x, y, mPaint);
		}

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!isTextModeOn) {
					touch_start(x, y);
					invalidate();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (!isTextModeOn) {
					touch_move(x, y);
					invalidate();
				}
				break;
			case MotionEvent.ACTION_UP:
				if (isTextModeOn) {
					drawText((int) x, (int) y);
					invalidate();
				} else {
					touch_up();
					invalidate();
				}
				break;
			}
			return true;
		}

		public void onClickUndo() {
			if (paths.size() > 0) {
				undonePaths.add(paths.remove(paths.size() - 1));
				invalidate();
			} else {

			}
			// toast the user
		}

		public void onClickRedo() {
			if (undonePaths.size() > 0) {
				paths.add(undonePaths.remove(undonePaths.size() - 1));
				invalidate();
			} else {

			}
			// toast the user
		}
	}

	/*
	 * public class MyView extends View {
	 * 
	 * private static final float MINP = 0.25f; private static final float MAXP
	 * = 0.75f;
	 * 
	 * private Bitmap mBitmap; private Canvas mCanvas; private Path mPath;
	 * private Paint mBitmapPaint; private ArrayList<Path> undoablePaths;
	 * 
	 * public MyView(Context c) { super(c);
	 * 
	 * mPath = new Path(); mBitmapPaint = new Paint(Paint.DITHER_FLAG);
	 * undoablePaths=new ArrayList<Path>(); }
	 * 
	 * 
	 * @Override protected void onSizeChanged(int w, int h, int oldw, int oldh)
	 * { super.onSizeChanged(w, h, oldw, oldh); mBitmap =
	 * CaptureActivity.mPhoto; mCanvas = new Canvas(mBitmap); }
	 * 
	 * @Override protected void onDraw(Canvas canvas) {
	 * canvas.drawColor(0xFFAAAAAA);
	 * 
	 * canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
	 * 
	 * canvas.drawPath(mPath, mPaint);
	 * 
	 * for(Path path : undoablePaths){ canvas.drawPath(path, mPaint); } }
	 * 
	 * private float mX, mY; private static final float TOUCH_TOLERANCE = 4;
	 * 
	 * private void touch_start(float x, float y) { mPath=new Path();
	 * mPath.reset(); mPath.moveTo(x, y); mX = x; mY = y; } private void
	 * touch_move(float x, float y) { float dx = Math.abs(x - mX); float dy =
	 * Math.abs(y - mY); if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
	 * mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2); mX = x; mY = y; } } private
	 * void touch_up() { mPath.lineTo(mX, mY); // commit the path to our
	 * offscreen mCanvas.drawPath(mPath, mPaint); // Path tempPath=mPath;
	 * undoablePaths.add(mPath); // kill this so we don't double draw
	 * mPath.reset(); }
	 * 
	 * @Override public boolean onTouchEvent(MotionEvent event) { float x =
	 * event.getX(); float y = event.getY();
	 * 
	 * switch (event.getAction()) { case MotionEvent.ACTION_DOWN: touch_start(x,
	 * y); invalidate(); break; case MotionEvent.ACTION_MOVE: touch_move(x, y);
	 * invalidate(); break; case MotionEvent.ACTION_UP: touch_up();
	 * invalidate(); break; } return true; }
	 * 
	 * public void undoLastAction() { undoablePaths.remove(undoablePaths.size()
	 * - 1); invalidate();
	 * 
	 * } }
	 */

	public void colorChanged(int color) {
		// TODO Auto-generated method stub
		lastColor = color;
		drawView.colorChanged(lastColor);
	}

	class PathPoints {
		private Path path;
		// private Paint mPaint;
		private int color;
		private String textToDraw;
		private boolean isTextToDraw;
		private int x, y;

		public PathPoints(Path path, int color, boolean isTextToDraw) {
			this.path = path;
			this.color = color;
			this.isTextToDraw = isTextToDraw;
		}

		public PathPoints(int color, String textToDraw, boolean isTextToDraw,
				int x, int y) {
			this.color = color;
			this.textToDraw = textToDraw;
			this.isTextToDraw = isTextToDraw;
			this.x = x;
			this.y = y;
		}

		public Path getPath() {
			return path;
		}

		public void setPath(Path path) {
			this.path = path;
		}

		/*
		 * private Paint getPaint() { mPaint = new Paint();
		 * mPaint.setAntiAlias(true); mPaint.setColor(color);
		 * mPaint.setStyle(Paint.Style.STROKE);
		 * mPaint.setStrokeJoin(Paint.Join.ROUND);
		 * mPaint.setStrokeCap(Paint.Cap.ROUND); mPaint.setStrokeWidth(6);
		 * return mPaint; }
		 */

		public int getColor() {
			return color;
		}

		public void setColor(int color) {
			this.color = color;
		}

		public String getTextToDraw() {
			return textToDraw;
		}

		public void setTextToDraw(String textToDraw) {
			this.textToDraw = textToDraw;
		}

		public boolean isTextToDraw() {
			return isTextToDraw;
		}

		public void setTextToDraw(boolean isTextToDraw) {
			this.isTextToDraw = isTextToDraw;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

	}
}
