package com.dlawoals2713.oui4.file;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;
import com.dlawoals2713.oui4.file.databinding.PdfViewerBinding;

import java.io.File;
import java.util.ArrayList;


public class PdfViewerActivity extends BaseThemeActivity {
	private PdfViewerBinding binding;
    private void handleException(Exception e) {
        showToast(e.getMessage());
		ExceptionLogger.log(e, getClass().getSimpleName());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
	
	public final int REQ_CD_FP = 101;
	
	private double n = 0;
	private double page = 0;
	private double pageCount = 0;
	private double f = 0;
	private String pdfFile = "";
	
	private Intent fp = new Intent(Intent.ACTION_GET_CONTENT);
	
    @Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		try {
			binding = PdfViewerBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
		    initialize(_savedInstanceState);
		
		    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
    			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
	    	} else {
		    	initializeLogic();
	    	}
	    } catch(Exception e) {
            handleException(e);
        }
    }
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		fp.setType("*/*");
		fp.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		
		binding.back.setOnClickListener(_view -> {
            if (page > 0) {
                page--;
                _display(page);
				binding.textview1.setText(String.valueOf((long)(page + 1)).concat("/".concat(String.valueOf((long)(pageCount)))));
            }
        });

		binding.next.setOnClickListener(_view -> {
            if (page < (pageCount - 1)) {
                page++;
                _display(page);
				binding.textview1.setText(String.valueOf((long)(page + 1)).concat("/".concat(String.valueOf((long)(pageCount)))));
            }
        });

		binding.linear10.setOnClickListener(_view -> startActivityForResult(fp, REQ_CD_FP));

		binding.linear13.setOnClickListener(_view -> {
            if (n == 0) {
                n++;
				binding.editLinear.setVisibility(View.VISIBLE);
				binding.imageview6.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_arrow_down);
            } else {
                n--;
				binding.editLinear.setVisibility(View.GONE);
				binding.imageview6.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_arrow_up);
            }
        });

		binding.imageview4.setOnClickListener(_view -> {
            if (!binding.edittext1.getText().toString().isEmpty()) {
                if ((Double.parseDouble(binding.edittext1.getText().toString()) > 0) && (Double.parseDouble(binding.edittext1.getText().toString()) < (1 + pageCount))) {
                    page = Double.parseDouble(binding.edittext1.getText().toString()) - 1;
                    _display(page);
					binding.textview1.setText(binding.edittext1.getText().toString().concat("/".concat(String.valueOf((long)(pageCount)))));
					binding.editLinear.setVisibility(View.GONE);
                    n = 0;
					binding.imageview5.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_arrow_up);
                }
            }
        });
	}
	
	private void initializeLogic() {
		_DarkMode();
		touch=new
		ZoomableImageView(this);
		binding.linear1.addView(touch);
		n = 0;
		f = 0;
		binding.editLinear.setVisibility(View.GONE);
		pdfFile = getIntent().getStringExtra("f");
		page = 0;
		try {
			renderer = new PdfRenderer(new ParcelFileDescriptor(ParcelFileDescriptor.open(new File(pdfFile), ParcelFileDescriptor.MODE_READ_ONLY)));
			pageCount = renderer.getPageCount();
			_display(page);
		} catch (Exception ignored){ }
		binding.textview1.setText(String.valueOf((long)(page + 1)).concat("/".concat(String.valueOf((long)(pageCount)))));
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);

        if (_requestCode == REQ_CD_FP) {
            if (_resultCode == Activity.RESULT_OK) {
                ArrayList<String> _filePath = new ArrayList<>();
                if (_data != null) {
                    if (_data.getClipData() != null) {
                        for (int _index = 0; _index < _data.getClipData().getItemCount(); _index++) {
                            ClipData.Item _item = _data.getClipData().getItemAt(_index);
                            _filePath.add(FileUtil.convertUriToFilePath(getApplicationContext(), _item.getUri()));
                        }
                    } else {
                        _filePath.add(FileUtil.convertUriToFilePath(getApplicationContext(), _data.getData()));
                    }
                }
                n = 0;
                f = 0;
				binding.editLinear.setVisibility(View.GONE);
                pdfFile = _filePath.get(0);
                page = 0;
                try {
                    renderer = new PdfRenderer(new ParcelFileDescriptor(ParcelFileDescriptor.open(new File(pdfFile), ParcelFileDescriptor.MODE_READ_ONLY)));
                    pageCount = renderer.getPageCount();
                    _display(page);
                } catch (Exception ignored) {
                }
				binding.textview1.setText(String.valueOf((long) (page + 1)).concat("/".concat(String.valueOf((long) (pageCount)))));
            }
        }
	}

	public void _display(final double _page) {
		PdfRenderer.Page page = renderer.openPage((int)_page);

		int displayWidth = SketchwareUtil.getDisplayWidthPixels(this)*2;
		int displayHeight = SketchwareUtil.getDisplayHeightPixels(this)*2;

		// PDF 페이지의 원본 가로세로 비율을 계산합니다.
		float pdfAspectRatio = (float) page.getWidth() / page.getHeight();
		// 화면의 가로세로 비율을 계산합니다.
		float screenAspectRatio = (float) displayWidth / displayHeight;

		int renderWidth;
		int renderHeight;

		// PDF 페이지를 화면에 최대한 맞추되, 비율을 유지하도록 계산합니다.
		if (pdfAspectRatio > screenAspectRatio) {
			// PDF가 화면보다 가로로 길 때: 너비를 화면에 맞추고 높이를 비율에 따라 조정
			renderWidth = displayWidth;
			renderHeight = (int) (displayWidth / pdfAspectRatio);
		} else {
			// PDF가 화면보다 세로로 길거나 비슷할 때: 높이를 화면에 맞추고 너비를 비율에 따라 조정
			renderHeight = displayHeight;
			renderWidth = (int) (displayHeight * pdfAspectRatio);
		}

		// 계산된 너비와 높이로 비트맵을 생성합니다.
		// 여기서는 화면의 픽셀 크기를 직접 사용하므로 getDip 함수는 필요 없습니다.
		Bitmap mBitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888);

		// 렌더링 매트릭스를 생성하여 PDF 페이지가 생성된 비트맵 크기에 맞춰 렌더링되도록 합니다.
		Matrix matrix = new Matrix();
		float scaleX = (float) renderWidth / page.getWidth();
		float scaleY = (float) renderHeight / page.getHeight();
		matrix.postScale(scaleX, scaleY);

		// PDF 페이지를 mBitmap에 렌더링합니다.
		// 여기서는 비트맵 크기에 맞추기 위한 변환 매트릭스를 전달합니다.
		page.render(mBitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

		touch.setImageBitmap(mBitmap);
		page.close();
		binding.textview1.setText(String.valueOf((long)(_page + 1)).concat("/").concat(String.valueOf((long)(pageCount)))); // 페이지 번호 표시를 기존처럼 유지
	}

	private PdfRenderer renderer;
	ZoomableImageView touch;
	public class ZoomableImageView extends androidx.appcompat.widget.AppCompatImageView {
		Matrix matrix = new Matrix();
		static final int NONE = 0;
		static final int DRAG = 1;
		static final int ZOOM = 2;
		static final int CLICK = 3;
		int mode = NONE;
		PointF last = new PointF();
		PointF start = new PointF();
		float minScale = 1f;
		float maxScale = 10f;
		float[] m;
		float redundantXSpace, redundantYSpace;
		float width, height;
		float saveScale = 1f;
		float right, bottom, origWidth, origHeight, bmWidth, bmHeight;
		ScaleGestureDetector mScaleDetector;
		Context context;

		public ZoomableImageView(Context context) {
			super(context);
			super.setClickable(true);
			this.context = context;
			mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
			matrix.setTranslate(1f, 1f);
			m = new float[9];
			setImageMatrix(matrix); setScaleType(ScaleType.MATRIX);

			setOnTouchListener((v, event) -> {
                mScaleDetector.onTouchEvent(event);
                matrix.getValues(m);
                float x = m[Matrix.MTRANS_X];
                float y = m[Matrix.MTRANS_Y];
                PointF curr = new PointF(event.getX(), event.getY());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        last.set(event.getX(), event.getY());
                        start.set(last);
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        last.set(event.getX(), event.getY());
                        start.set(last);
                        mode = ZOOM;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == ZOOM || (mode == DRAG && saveScale > minScale)) {
                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;
                            float scaleWidth = Math.round(origWidth * saveScale);
                            float scaleHeight = Math.round(origHeight * saveScale);
                            if (scaleWidth < width) {
                                deltaX = 0;
                                if (y + deltaY > 0) deltaY = -y;
                                else if (y + deltaY < -bottom) deltaY = -(y + bottom);
                            } else if (scaleHeight < height) {
                                deltaY = 0;
                                if (x + deltaX > 0) deltaX = -x;
                                else if (x + deltaX < -right) deltaX = -(x + right);
                            } else {
                                if (x + deltaX > 0) deltaX = -x;
                                else if (x + deltaX < -right) deltaX = -(x + right);
                                if (y + deltaY > 0) deltaY = -y;
                                else if (y + deltaY < -bottom) deltaY = -(y + bottom);
                            }
                            matrix.postTranslate(deltaX, deltaY);
                            last.set(curr.x, curr.y);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mode = NONE;
                        int xDiff = (int) Math.abs(curr.x - start.x);
                        int yDiff = (int) Math.abs(curr.y - start.y);
                        if (xDiff < CLICK && yDiff < CLICK) performClick();
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                }
                setImageMatrix(matrix);
                invalidate();
                return true;
            });
		}
		
		@Override
		public void setImageBitmap(Bitmap bm) {
			super.setImageBitmap(bm);
			bmWidth = bm.getWidth();
			bmHeight = bm.getHeight();
		}

		public void setMaxZoom(float x) {
			maxScale = x;
		}

		private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				mode = ZOOM;
				return true;
			}
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				float mScaleFactor = detector.getScaleFactor();
				float origScale = saveScale;
				saveScale *= mScaleFactor;
				if (saveScale > maxScale){
					saveScale = maxScale;
					mScaleFactor = maxScale / origScale;
				} else if (saveScale < minScale) {
					saveScale = minScale;
					mScaleFactor = minScale / origScale;
				}
				right = width * saveScale - width - (2 * redundantXSpace * saveScale);
				bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
				if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
					matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
					if (mScaleFactor < 1) {
						matrix.getValues(m);
						float x = m[Matrix.MTRANS_X];
						float y = m[Matrix.MTRANS_Y];
						if (mScaleFactor < 1) {
							if (Math.round(origWidth * saveScale) < width) {
								if (y < -bottom) matrix.postTranslate(0, -(y + bottom));
								else if (y > 0) matrix.postTranslate(0, -y);
							} else {
								if (x < -right) matrix.postTranslate(-(x + right), 0);
								else if (x > 0) matrix.postTranslate(-x, 0);}
						}
					}
				} else {
					matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY()); matrix.getValues(m);
					float x = m[Matrix.MTRANS_X];
					float y = m[Matrix.MTRANS_Y];
					if (mScaleFactor < 1) {
						if (x < -right) matrix.postTranslate(-(x + right), 0);
						else if (x > 0) matrix.postTranslate(-x, 0);

						if (y < -bottom) matrix.postTranslate(0, -(y + bottom));
						else if (y > 0) matrix.postTranslate(0, -y);
					}
				}
				return true;
			}
		}

		@Override
		protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			width = MeasureSpec.getSize(widthMeasureSpec);
			height = MeasureSpec.getSize(heightMeasureSpec);
			float scale;
			float scaleX = width / bmWidth;
			float scaleY = height / bmHeight;
			scale = Math.min(scaleX, scaleY);
			matrix.setScale(scale, scale);
			setImageMatrix(matrix);
			saveScale = 1f;
			redundantYSpace = height - (scale * bmHeight) ;
			redundantXSpace = width - (scale * bmWidth);
			redundantYSpace /= 2;
			redundantXSpace /= 2; matrix.postTranslate(redundantXSpace, redundantYSpace);
			origWidth = width - 2 * redundantXSpace;
			origHeight = height - 2 * redundantYSpace;
			right = width * saveScale - width - (2 * redundantXSpace * saveScale);
			bottom = height * saveScale - height - (2 * redundantYSpace * saveScale); setImageMatrix(matrix);}
	}

	
	public void _DarkMode() {
		int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
		if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
			binding.background.setBackgroundColor(0xFF000000);
		}
	}

	@SuppressLint("MissingSuperCall")
	@Override
	public void onBackPressed() {
		finishAndRemoveTask();
	}
}
