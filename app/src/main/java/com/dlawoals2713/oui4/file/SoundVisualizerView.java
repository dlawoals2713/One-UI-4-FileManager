package com.dlawoals2713.oui4.file;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class SoundVisualizerView extends View {
    private Paint barPaint;
    private byte[] fftData;
    private Context context; // Context를 저장할 멤버 변수 추가

    // 부드러운 움직임을 위한 변수 추가
    private float[] smoothHeights;
    private float smoothingFactor = 0.5f; // 부드러움 정도 (0에 가까울수록 더 부드러워짐)

    public SoundVisualizerView(Context context) {
        super(context);
        this.context = context;
        init(context, null); // null을 전달하여 기본값 사용
    }

    public SoundVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context, attrs); // attrs를 전달하여 XML 속성 읽기
    }

    public SoundVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context, attrs); // attrs를 전달하여 XML 속성 읽기
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        barPaint = new Paint();
        barPaint.setStrokeWidth(5f);
        barPaint.setStrokeCap(Paint.Cap.ROUND);

        // attrs가 null이 아닐 때만 속성을 읽어옵니다.
        if (attrs != null) {
            // styles.xml에 정의된 스타일 속성을 가져옵니다.
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SoundVisualizerView);
            // "barColor" 속성을 가져옵니다. 기본값으로 R.color.textColor를 사용합니다.
            int color = typedArray.getColor(R.styleable.SoundVisualizerView_barColor, ContextCompat.getColor(context, R.color.textColor));
            barPaint.setColor(color);
            // TypedArray 사용이 끝나면 recycle()을 호출해야 합니다.
            typedArray.recycle();
        } else {
            // attrs가 null인 경우 기본 색상을 설정합니다.
            barPaint.setColor(ContextCompat.getColor(context, R.color.textColor));
        }
    }

    // Context를 설정하는 공개 메서드 추가
    public void setContext(Context context) {
        this.context = context;
    }

    public void updateVisualizer(byte[] fft) {
        this.fftData = fft;
        // FFT 데이터가 들어올 때 smoothHeights 배열을 초기화합니다.
        if (smoothHeights == null || smoothHeights.length != fft.length / 2) {
            smoothHeights = new float[fft.length / 2];
        }
        invalidate(); // View 갱신 요청
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (fftData == null || smoothHeights == null || context == null) {
            return;
        }

        // 막대 개수를 조절하는 예시 (전체 512개 중 0~100번 막대만 사용)
        int numBarsToDraw = Integer.parseInt(Setting.getFileSVBar(context)); // context 변수 사용
        float width = getWidth();
        float height = getHeight();

        float barWidth = width / numBarsToDraw;

        for (int i = 0; i < numBarsToDraw; i++) {
            byte real = fftData[i * 2];
            byte imaginary = fftData[i * 2 + 1];

            // FFT 크기(magnitude) 계산
            double magnitude = Math.sqrt(real * real + imaginary * imaginary);
            float currentHeight = (float) (height * (magnitude / 128.0));

            // 지수 평균 필터를 적용하여 높이를 부드럽게 만듭니다.
            if (i < smoothHeights.length) {
                smoothHeights[i] = (smoothingFactor * currentHeight) + ((1 - smoothingFactor) * smoothHeights[i]);
            }

            // 실제 그릴 막대 높이
            float drawHeight = smoothHeights[i];

            float x = i * barWidth + barWidth / 2;
            float startY = height;
            float endY = height - drawHeight;

            canvas.drawLine(x, startY, x, endY, barPaint);
        }
    }
}