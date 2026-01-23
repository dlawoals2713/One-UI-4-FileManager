package com.dlawoals2713.oui4.file;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.dlawoals2713.oui4.file.base.BaseThemeActivity;

public class FilemanagerSingleImageActivity extends BaseThemeActivity {
	private static final int UI_OPTIONS = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

	private void hideSystemUI() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) actionBar.hide();
		getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS);
	}
	
	private boolean hide = false;
	private boolean darkmode = false;
	
	private HorizontalScrollView hscroll1;
	private LinearLayout linear2;
	private TextView textview1;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.filemanager_image);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		hscroll1 = findViewById(R.id.hscroll1);
		linear2 = findViewById(R.id.linear2);
		textview1 = findViewById(R.id.textview1);
		
		textview1.setOnLongClickListener(_view -> {
            if (Build.VERSION.SDK_INT >= 26) {
				//Trigger PiP mode
				try {
					Rational rational = new
					Rational(linear2.getWidth(),
						linear2.getHeight());
						PictureInPictureParams mParams =
							new PictureInPictureParams.Builder()
							.setAspectRatio(rational)
						.build();
						enterPictureInPictureMode(mParams);
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(FilemanagerSingleImageActivity.this, "API 버전이 26보다 낮아서 PiP를 실행할 수 없어요", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
		
		textview1.setOnClickListener(_view -> {
            linear2.setBackgroundColor(0xFF000000);
            hscroll1.setVisibility(View.GONE);
            hide = true;
        });
	}
	
	private void initializeLogic() {
        ZoomableImageView zoom1 = new ZoomableImageView(FilemanagerSingleImageActivity.this);
		zoom1.setScaleType(ImageView.ScaleType.FIT_CENTER);
		zoom1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
		linear2.addView(zoom1);
		textview1.setText(getIntent().getStringExtra("title"));
		Glide.with(this) // 액티비티 컨텍스트 사용
				.load(getIntent().getStringExtra("path")) // 파일 경로 로드
				.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
				.into(zoom1);
		hide = false;
		darkmode = false;
		_DarkMode();
		SketchwareUtil.showMessage(getApplicationContext(), "화면에 고정하려면 이미지 이름을 길게 누르세요.");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
		}
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		getWindow().setStatusBarColor(Color.TRANSPARENT);
		// this lines ensure only the status-bar to become transparent without affecting the nav-bar
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		hideSystemUI();
		    
		getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
			if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
				hideSystemUI();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Glide 메모리 캐시를 즉시 삭제합니다.
		Glide.get(this).clearMemory();
		// Glide 디스크 캐시는 백그라운드 스레드에서 삭제합니다.
		new Thread(() -> Glide.get(this).clearDiskCache()).start();
	}
	
	@SuppressLint("MissingSuperCall")
    @Override
	public void onBackPressed() {
		if (hide) {
			hscroll1.setVisibility(View.VISIBLE);
			if (darkmode) {
				linear2.setBackgroundColor(0xFF000000);
			} else {
				linear2.setBackgroundColor(Color.TRANSPARENT);
			}
			hide = false;
		} else {
			finishAndRemoveTask();
		}
	}

	public static class ZoomableImageView extends androidx.appcompat.widget.AppCompatImageView implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {
		private Matrix mCurrentMatrix;
		private GestureDetector mGestureDetector;
		private static final String TAG = "ZoomView";
		private ScaleGestureDetector scaleGestureDetector;
		private PointF mRect = new PointF();
		private PointF mCurrentZoomPoint = new PointF();
		private MatrixValueManager matrixValueManager, mImageMatrixManager;
		private android.os.Handler mHandler = new android.os.Handler();
		private float mLastPositionY;
		private float mLastPositionX;
		private boolean isZooming = false;

		public ZoomableImageView(Context context) {
			super(context);
			initaial();
		}

		@Override
		public void setImageBitmap(Bitmap bm) {
				super.setImageBitmap(bm);
			}

		@Override
		public void setImageDrawable(android.graphics.drawable.Drawable drawable) {
			super.setImageDrawable(drawable);
		}

		public ZoomableImageView(Context context, android.util.AttributeSet attrs) {
			super(context, attrs);
			initaial();
		}

		public ZoomableImageView(Context context, android.util.AttributeSet attrs, int defStyleAttr) {
			super(context, attrs, defStyleAttr);
			initaial();
		}

		@SuppressLint("ClickableViewAccessibility")
        private void initaial(){
			matrixValueManager = new MatrixValueManager();
			mImageMatrixManager = new MatrixValueManager();
			setLayerType(LAYER_TYPE_HARDWARE, null);
			mCurrentMatrix = getImageMatrix();
			mGestureDetector = new GestureDetector(getContext(), this);
			scaleGestureDetector = new ScaleGestureDetector(getContext(), this);
			mGestureDetector.setOnDoubleTapListener(this);
			this.setOnTouchListener((view, motionEvent) -> {
                scaleGestureDetector.onTouchEvent(motionEvent);
                mGestureDetector.onTouchEvent(motionEvent);
                return true;
            });
		}

		@Override
		public void invalidate() {
			super.invalidate();
			matrixValueManager.setMatrix(mCurrentMatrix);
		}

		protected void onActionUp() {
			if (matrixValueManager.getScaleX() <= 1) {
				zoomAnimation(1.0f);
			} else {
				mHandler.post(this::adjustPosition);
			}
		}

		private void adjustPosition() {
			float imgH = (getHeight() - (mImageMatrixManager.getTransitionY() *2)) * matrixValueManager.getScaleY();
			float mY = (matrixValueManager.getTransitionY() + (mImageMatrixManager.getTransitionY()) * matrixValueManager.getScaleY());
			float scrollAbleY = (getHeight() - imgH);

			float vH = ((getHeight() * matrixValueManager.getScaleY()) - getHeight()) / 2;
			float vW = ((getWidth() * matrixValueManager.getScaleX()) - getWidth()) / 2;

			float x = 0, y = 0;

			if (imgH < getHeight()){
				y = (-vH - matrixValueManager.getTransitionY());
			} else if (imgH >= getHeight()){
				if (mY > 0) {
					y = -mY;
				} else if (mY < scrollAbleY) {
					y = scrollAbleY - mY;
				}
			}

			float mX = (matrixValueManager.getTransitionX() + (mImageMatrixManager.getTransitionX()) * matrixValueManager.getScaleX());
			float imgW = (getWidth() - (mImageMatrixManager.getTransitionX() * 2)) * matrixValueManager.getScaleX();
			float scrollAbleX = (getWidth() - imgW);

			if (imgW < getWidth()){
				x = (-vW - matrixValueManager.getTransitionX());
			} else if (imgW >= getWidth()){
				if (mX > 0) {
					x = -mX;
				} else if (mX < scrollAbleX) {
					x = scrollAbleX - mX;
				}
			}

			if (x != 0 || y != 0) {
				moveAnimation(x, y);
			} else {
				findCurrentZoomPoint();
			}
		}

		private void moveAnimation(final float x, final float y) {

			mHandler.post(() -> {
                mLastPositionY = 0;
                mLastPositionX = 0;
                android.animation.PropertyValuesHolder valueY = android.animation.PropertyValuesHolder.ofFloat("y", 0, y);
                android.animation.PropertyValuesHolder valueX = android.animation.PropertyValuesHolder.ofFloat("x", 0, x);
                android.animation.ValueAnimator anim = new android.animation.ValueAnimator();
                anim.setValues(valueX, valueY);
                anim.addUpdateListener(animation -> {
                    float valueX1 = (float) animation.getAnimatedValue("x");
                    float valueY1 = (float) animation.getAnimatedValue("y");
                    mCurrentMatrix.postTranslate(valueX1 - mLastPositionX, valueY1 - mLastPositionY);
                    matrixValueManager.setMatrix(mCurrentMatrix);
                    postInvalidate();
                    mLastPositionY = valueY1;
                    mLastPositionX = valueX1;
                    if (valueX1 >= x && valueY1 >= y){
                        findCurrentZoomPoint();
                    }
                });
                anim.setDuration(250);
                anim.start();
            });

		}

		private void move(float x, float y) {
			mCurrentMatrix.postTranslate(x, y);
			postInvalidate();
			mHandler.post(this::findCurrentZoomPoint);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.save();
			canvas.concat(mCurrentMatrix);
			matrixValueManager.setMatrix(mCurrentMatrix);
			mImageMatrixManager.setMatrix(getImageMatrix());
			super.onDraw(canvas);
			canvas.restore();
		}

		@Override
		public boolean onDown(MotionEvent motionEvent) {
			mRect.set(motionEvent.getX(motionEvent.getPointerCount() -1), motionEvent.getY(motionEvent.getPointerCount() -1));
			return true;
		}

		@Override
		public void onShowPress(@NonNull MotionEvent motionEvent) {

		}

		@Override
		public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
				return false;
			}

		@Override
		public boolean onScroll(final MotionEvent motionEvent, @NonNull final MotionEvent motionEvent1, float v, float v1) {
			if (!isZooming) {
				if (!mRect.equals(motionEvent1.getX(), motionEvent1.getY())) {
					calculatePosition(motionEvent1.getX(), motionEvent1.getY());
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean canScrollHorizontally(int direction) {
			return matrixValueManager.getScaleX() > 1;
		}

		@Override
		public boolean canScrollVertically(int direction) {
			return matrixValueManager.getScaleY() > 1;
		}

		private void calculatePosition(float rawX, float rawY){
			float x = (rawX - mRect.x);
			float y  = (rawY - mRect.y);

			float mY = (matrixValueManager.getTransitionY() + (mImageMatrixManager.getTransitionY() * matrixValueManager.getScaleY()));
			float imgH = (getHeight() - (mImageMatrixManager.getTransitionY() * 2)) * matrixValueManager.getScaleY();
			float scrollAbleY = (getHeight() - imgH);
			if (imgH > getHeight()){
				float r = (mY + y);
				float s = (r - scrollAbleY);

				if (s < 0) {
					y = 0;
				}

				if (r > 0) {
					y = 0;
				}

			} else {
				y = 0;
			}

			float mX = (matrixValueManager.getTransitionX() + (mImageMatrixManager.getTransitionX() * matrixValueManager.getScaleX()));
			float imgW = (getWidth() - (mImageMatrixManager.getTransitionX() * 2)) * matrixValueManager.getScaleX();
			float scrollAbleX = (getWidth() - imgW);
			if ((imgW) > getWidth()){
				float l = (mX + x);
				float s = (l - scrollAbleX);

				if (s < 0) {
					x = 0;
				}

				if (l > 0) {
					x = 0;
				}

			} else {
				x = 0;
			}

			mRect.set(rawX, rawY);
			move(x, y);
		}

		private void findCurrentZoomPoint() {
			float _x, _y;
			//X
			float imgW = (getWidth() - (mImageMatrixManager.getTransitionX() * 2)) * matrixValueManager.getScaleX();
			float scrollAbleX = (getWidth() - imgW);
			if (scrollAbleX < 0) {
				float mX = ((matrixValueManager.getTransitionX() / matrixValueManager.getScaleX()));
				float visibleScreenX = (getWidth() / matrixValueManager.getScaleX());
				float percentX = ((Math.abs(mX)) * 100) / (getWidth() - visibleScreenX);
				_x = Math.abs(mX) + ((percentX * visibleScreenX) / 100);
			} else {
				_x = (float) getWidth() / 2;
			}
			//Y
			float imgH = (getHeight() - (mImageMatrixManager.getTransitionY() * 2)) * matrixValueManager.getScaleY();
			float scrollAbleY = (getHeight() - imgH);
			if (scrollAbleY < 0) {
				float mY = ((matrixValueManager.getTransitionY() / matrixValueManager.getScaleY()));
				float visibleScreenY = (getHeight() / matrixValueManager.getScaleY());
				float percentY = ((Math.abs(mY)) * 100) / (getHeight() - visibleScreenY);
				_y = Math.abs(mY) + ((percentY * visibleScreenY) / 100);
			} else {
				_y = (float) getHeight() / 2;
			}
			mCurrentZoomPoint.set(_x, _y);
		}

		@Override
		public void onLongPress(@NonNull MotionEvent motionEvent) {

		}

		@Override
		public boolean onFling(final MotionEvent motionEvent, @NonNull final MotionEvent motionEvent1, float v, float v1) {
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(@NonNull MotionEvent motionEvent) {
				return false;
			}

		float mLastScale = 0;
		public void releaseZoom(){
			if (matrixValueManager.getScaleX() > 1 || matrixValueManager.getScaleY()> 1) {
				isZooming = true;
				mLastScale = 0;
				final float scale = matrixValueManager.getScaleX();
				android.animation.ValueAnimator valueAnimator = android.animation.ValueAnimator.ofFloat(scale, 1.0f);
				valueAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
				valueAnimator.addUpdateListener(animation -> {
                    float value = (float) animation.getAnimatedValue();
                    if (value != scale) {
                        mCurrentMatrix.setScale(value, value, mCurrentZoomPoint.x, mCurrentZoomPoint.y);
                        postInvalidate();
                        if (value == 1) {
                            isZooming = false;
                        }
                    }
                });
				valueAnimator.setDuration(250);
				valueAnimator.start();
			}
		}

		@Override
		public boolean onDoubleTap(@NonNull final MotionEvent motionEvent) {
				return true;
			}

		private void zoomAnimation(final float scale) {
			isZooming = true;
			android.animation.ValueAnimator valueAnimator = android.animation.ValueAnimator.ofFloat(matrixValueManager.getScaleX(), scale);
			valueAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                mCurrentMatrix.setScale(value, value, mCurrentZoomPoint.x, mCurrentZoomPoint.y);
                postInvalidate();
                if (value == scale) {
                    mHandler.postDelayed(this::adjustPosition, 100);
                    isZooming = false;
                }
            });
			valueAnimator.setDuration(250);
			valueAnimator.start();
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent motionEvent) {
			if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
				if (matrixValueManager.getScaleX() > 1) {
					releaseZoom();
				} else {
					mCurrentZoomPoint.set(motionEvent.getX(), motionEvent.getY());
					zoomAnimation(2.0F);
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
			isZooming = true;
			float scale = scaleGestureDetector.getCurrentSpan() / scaleGestureDetector.getPreviousSpan();
			if (matrixValueManager.getScaleX() >= 1) {
				float focusX = scaleGestureDetector.getFocusX();
				float focusY = scaleGestureDetector.getFocusY();
				mCurrentMatrix.postScale(scale, scale, focusX, focusY);
				postInvalidate();
				mCurrentZoomPoint.set(focusX, focusY);
				return true;
			}
			return false;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
			mRect.set(scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
			mRect.set(scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
			isZooming = false;
			onActionUp();
		}
		public static final class MatrixValueManager {
			float[] floats;

			public void setMatrix(Matrix matrix){
				floats = new float[9];
				matrix.getValues(floats);
			}

			public float getTransitionX(){
					return floats[Matrix.MTRANS_X];
				}

			public float getTransitionY(){
					return floats[Matrix.MTRANS_Y];
				}

			public float getScaleX(){
					return floats[Matrix.MSCALE_X];
				}

			public float getScaleY(){
					return floats[Matrix.MSCALE_Y];
				}
			
		}
	}

    public void _DarkMode() {
		int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
		if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
			hscroll1.setBackgroundColor(0xFF000000);
			linear2.setBackgroundColor(0xFF000000);
			textview1.setTextColor(0xFFFFFFFF);
			darkmode = true;
		}
	}
}
