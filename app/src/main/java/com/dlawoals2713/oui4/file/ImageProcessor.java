package com.dlawoals2713.oui4.file;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {
    public static final int DIRECTION_HORIZONTAL = 0;
    public static final int DIRECTION_VERTICAL = 1;

    // 이미지 분할 메서드
    public static boolean splitImage(String imagePath, int direction, int splitCount, String outputDirPath, SplitCallback callback) {
        try {
            Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
            if (originalBitmap == null) {
                return false;
            }
    
            File outputDir = new File(outputDirPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
    
            String originalName = new File(imagePath).getName();
            String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
    
            if (direction == DIRECTION_HORIZONTAL) {
                return splitHorizontally(originalBitmap, splitCount, outputDir, baseName, callback);
            } else {
                return splitVertically(originalBitmap, splitCount, outputDir, baseName, callback);
            }
        } catch (Exception e) {
            Log.e("ImageProcessor", "Error splitting image", e);
            ExceptionLogger.log(e, "ImageProcessor:splitImage");
            return false;
        }
    }

    public static boolean splitImageByRatio(String imagePath, int direction, int ratio1, int ratio2,
                                            boolean keepRatio, String outputDirPath, SplitCallback callback) {
        try {
            Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
            if (originalBitmap == null) {
                return false;
            }

            File outputDir = new File(outputDirPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            String originalName = new File(imagePath).getName();
            String baseName = originalName.substring(0, originalName.lastIndexOf('.'));

            if (direction == DIRECTION_HORIZONTAL) {
                return splitHorizontallyByRatio(originalBitmap, ratio1, ratio2, keepRatio,
                        outputDir, baseName, callback);
            } else {
                return splitVerticallyByRatio(originalBitmap, ratio1, ratio2, keepRatio,
                        outputDir, baseName, callback);
            }
        } catch (Exception e) {
            Log.e("ImageProcessor", "Error splitting image by ratio", e);
            ExceptionLogger.log(e, "ImageProcessor:splitImageByRatio");
            return false;
        }
    }

    private static boolean splitHorizontally(Bitmap bitmap, int splitCount, File outputDir, String baseName, SplitCallback callback) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int splitHeight = height / splitCount;
    
        for (int i = 0; i < splitCount; i++) {
            int y = i * splitHeight;
            Bitmap splitBitmap = Bitmap.createBitmap(bitmap, 0, y, width, splitHeight);
    
            File outputFile = new File(outputDir, baseName + "_" + (i + 1) + ".png");
            saveBitmap(splitBitmap, outputFile);
            if (callback != null) callback.onProgress(i);
            splitBitmap.recycle();
        }
        return true;
    }
    
    private static boolean splitVertically(Bitmap bitmap, int splitCount, File outputDir, String baseName, SplitCallback callback) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int splitWidth = width / splitCount;
    
        for (int i = 0; i < splitCount; i++) {
            int x = i * splitWidth;
            Bitmap splitBitmap = Bitmap.createBitmap(bitmap, x, 0, splitWidth, height);
    
            File outputFile = new File(outputDir, baseName + "_" + (i + 1) + ".png");
            saveBitmap(splitBitmap, outputFile);
            if (callback != null) callback.onProgress(i);
            splitBitmap.recycle();
        }
        return true;
    }

    private static boolean splitHorizontallyByRatio(Bitmap bitmap, int ratio1, int ratio2,
                                                    boolean keepRatio, File outputDir,
                                                    String baseName, SplitCallback callback) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // int totalRatio = ratio1 + ratio2; // 이 부분은 1:1 정사각형 분할에는 필요 없습니다.

        // 1:1 비율로 자르기 위해서는 분할될 조각의 높이를 너비와 같게 설정합니다.
        // 핵심 변경: 분할 높이를 너비와 동일하게 설정

        int partCount = 0;
        int currentY = 0;

        while (currentY < height) {
            int remainingHeight = height - currentY;

            // 실제 잘라낼 높이 (남은 높이가 splitHeight보다 작으면 남은 높이만큼만 자릅니다)
            int actualSplitHeight = Math.min(width, remainingHeight);

            // 마지막 부분 처리
            if (actualSplitHeight <= 0) { // 더 이상 자를 부분이 없으면 루프 종료
                break;
            }

            // 원본 비트맵에서 부분 추출
            Bitmap originalPart = Bitmap.createBitmap(bitmap, 0, currentY, width, actualSplitHeight);
            Bitmap finalPart;

            if (keepRatio && actualSplitHeight < width) {
                // 비율 고정 옵션이 켜져 있고, 마지막 조각이 원래 원하는 splitHeight보다 작을 경우
                // 투명 배경의 새로운 비트맵을 만들고 거기에 원본 부분을 그립니다.
                finalPart = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(finalPart);
                canvas.drawColor(Color.TRANSPARENT);
                canvas.drawBitmap(originalPart, 0, 0, null);
                originalPart.recycle(); // 원본 부분은 더 이상 필요 없으므로 재활용
            } else {
                // 비율 고정 옵션이 없거나, 마지막 조각이 splitHeight와 같을 경우
                finalPart = originalPart; // 추출된 부분을 그대로 사용
            }

            savePart(finalPart, outputDir, baseName, ++partCount, callback);
            currentY += actualSplitHeight; // 다음 시작 위치 업데이트
        }
        return true;
    }

    private static boolean splitVerticallyByRatio(Bitmap bitmap, int ratio1, int ratio2,
                                                  boolean keepRatio, File outputDir,
                                                  String baseName, SplitCallback callback) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // int totalRatio = ratio1 + ratio2; // 이 부분은 1:1 정사각형 분할에는 필요 없습니다.

        // 1:1 비율로 자르기 위해서는 분할될 조각의 너비를 높이와 같게 설정합니다.
        // 핵심 변경: 분할 너비를 높이와 동일하게 설정

        int partCount = 0;
        int currentX = 0;

        while (currentX < width) {
            int remainingWidth = width - currentX;

            // 실제 잘라낼 너비 (남은 너비가 splitWidth보다 작으면 남은 너비만큼만 자릅니다)
            int actualSplitWidth = Math.min(height, remainingWidth);

            // 마지막 부분 처리
            if (actualSplitWidth <= 0) { // 더 이상 자를 부분이 없으면 루프 종료
                break;
            }

            // 원본 비트맵에서 부분 추출
            Bitmap originalPart = Bitmap.createBitmap(bitmap, currentX, 0, actualSplitWidth, height);
            Bitmap finalPart;

            if (keepRatio && actualSplitWidth < height) {
                // 비율 고정 옵션이 켜져 있고, 마지막 조각이 원래 원하는 splitWidth보다 작을 경우
                // 투명 배경의 새로운 비트맵을 만들고 거기에 원본 부분을 그립니다.
                finalPart = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(finalPart);
                canvas.drawColor(Color.TRANSPARENT);
                canvas.drawBitmap(originalPart, 0, 0, null);
                originalPart.recycle();
            } else {
                finalPart = originalPart;
            }

            savePart(finalPart, outputDir, baseName, ++partCount, callback);
            currentX += actualSplitWidth;
        }
        return true;
    }

    public static int getExpectedSplitCountByRatio(String imagePath, int direction, int ratio1, int ratio2) {
        Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
        if (originalBitmap == null) {
            return 0; // 이미지를 로드할 수 없으면 0 반환
        }

        int count = 0;
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        if (direction == DIRECTION_HORIZONTAL) {
            // splitHorizontallyByRatio 로직과 동일하게 분할 수 계산
            // 1:1 비율로 가로 분할 시 높이는 너비와 같음
            if (width <= 0) return 0; // 유효하지 않은 분할 높이 방지

            int currentY = 0;
            while (currentY < height) {
                int remainingHeight = height - currentY;
                int actualSplitHeight = Math.min(width, remainingHeight);

                if (actualSplitHeight <= 0) {
                    break;
                }
                count++;
                currentY += actualSplitHeight;
            }
        } else { // DIRECTION_VERTICAL
            // splitVerticallyByRatio 로직과 동일하게 분할 수 계산
            // 1:1 비율로 세로 분할 시 너비는 높이와 같음
            if (height <= 0) return 0; // 유효하지 않은 분할 너비 방지

            int currentX = 0;
            while (currentX < width) {
                int remainingWidth = width - currentX;
                int actualSplitWidth = Math.min(height, remainingWidth);

                if (actualSplitWidth <= 0) {
                    break;
                }
                count++;
                currentX += actualSplitWidth;
            }
        }
        originalBitmap.recycle(); // 사용 후 비트맵 메모리 해제
        return count;
    }

    private static void savePart(Bitmap bitmap, File outputDir, String baseName,
                                 int partCount, SplitCallback callback) {
        File outputFile = new File(outputDir, baseName + "_" + partCount + ".png");
        saveBitmap(bitmap, outputFile);
        bitmap.recycle();
        if (callback != null) callback.onProgress(partCount);
    }

    // 이미지 병합 메서드
    public static boolean mergeImages(List<String> imagePaths, int direction, int wrapCount,
                                    boolean centerAlign, boolean transparentBg, String outputPath,
                                    MergeCallback callback) {
        try {
            List<Bitmap> bitmaps = new ArrayList<>();
            int maxWidth = 0;
            int maxHeight = 0;
    
            // 모든 이미지 로드 및 최대 크기 계산
            for (String path : imagePaths) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                if (bitmap != null) {
                    bitmaps.add(bitmap);
                    maxWidth = Math.max(maxWidth, bitmap.getWidth());
                    maxHeight = Math.max(maxHeight, bitmap.getHeight());
                }
            }
    
            if (bitmaps.isEmpty()) {
                return false;
            }
    
            Bitmap result;
            if (direction == DIRECTION_HORIZONTAL) {
                result = mergeHorizontally(bitmaps, maxWidth, maxHeight, wrapCount, centerAlign, transparentBg, callback);
            } else {
                result = mergeVertically(bitmaps, maxWidth, maxHeight, wrapCount, centerAlign, transparentBg, callback);
            }
    
            if (result != null) {
                File outputFile = new File(outputPath);
                outputFile.getParentFile().mkdirs();
                return saveBitmap(result, outputFile);
            }
            return false;
        } catch (Exception e) {
            Log.e("ImageProcessor", "Error merging images", e);
            ExceptionLogger.log(e, "ImageProcessor:meargeImages");
            return false;
        }
    }

    private static Bitmap mergeHorizontally(List<Bitmap> bitmaps, int maxWidth, int maxHeight,
                                            int wrapCount, boolean centerAlign, boolean transparentBg,
                                            MergeCallback callback) {
        try {
            if (wrapCount <= 0) wrapCount = bitmaps.size();
            int rows = (int) Math.ceil((double) bitmaps.size() / wrapCount);

            // 각 행의 최대 높이 계산
            int[] rowHeights = new int[rows];
            for (int row = 0; row < rows; row++) {
                int rowMaxHeight = 0;
                for (int col = 0; col < wrapCount; col++) {
                    int index = row * wrapCount + col;
                    if (index < bitmaps.size()) {
                        rowMaxHeight = Math.max(rowMaxHeight, bitmaps.get(index).getHeight());
                    }
                }
                rowHeights[row] = rowMaxHeight;
            }

            // 전체 비트맵 크기 계산
            int resultWidth = maxWidth * Math.min(wrapCount, bitmaps.size());
            int resultHeight = 0;
            for (int h : rowHeights) resultHeight += h;

            Bitmap.Config config = transparentBg ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            Bitmap result = Bitmap.createBitmap(resultWidth, resultHeight, config);
            Canvas canvas = new Canvas(result);

            if (!transparentBg) {
                canvas.drawColor(Color.BLACK);
            }

            Paint paint = new Paint();
            paint.setAntiAlias(true);

            int currentY = 0;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < wrapCount; col++) {
                    int index = row * wrapCount + col;
                    if (index >= bitmaps.size()) break;

                    Bitmap bitmap = bitmaps.get(index);

                    int x = col * maxWidth;
                    int y = currentY;

                    if (centerAlign) {
                        y += (rowHeights[row] - bitmap.getHeight()) / 2;
                    }

                    canvas.drawBitmap(bitmap, x, y, paint);
                    if (callback != null) callback.onProgress(index);
                    bitmap.recycle();
                }
                currentY += rowHeights[row];
            }

            return result;
        } catch (Exception e) {
            ExceptionLogger.log(e, "ImageProcessor: mergeHorizontally");
            return null;
        }
    }
    
    private static Bitmap mergeVertically(List<Bitmap> bitmaps, int maxWidth, int maxHeight,
                                        int wrapCount, boolean centerAlign, boolean transparentBg,
                                        MergeCallback callback) {
        try {
            if (wrapCount <= 0) wrapCount = bitmaps.size();
            int rows = Math.min(wrapCount, bitmaps.size());
            int cols = (int) Math.ceil((double) bitmaps.size() / rows);

            // 각 열의 최대 너비 계산
            int[] colWidths = new int[cols];
            for (int c = 0; c < cols; c++) {
                int colMaxWidth = 0;
                for (int r = 0; r < rows; r++) {
                    int index = c * wrapCount + r;
                    if (index < bitmaps.size()) {
                        colMaxWidth = Math.max(colMaxWidth, bitmaps.get(index).getWidth());
                    }
                }
                colWidths[c] = colMaxWidth;
            }

            // 전체 비트맵 크기 계산
            int resultWidth = 0;
            for (int w : colWidths) resultWidth += w;
            int resultHeight = maxHeight * rows;

            Bitmap.Config config = transparentBg ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            Bitmap result = Bitmap.createBitmap(resultWidth, resultHeight, config);
            Canvas canvas = new Canvas(result);

            if (!transparentBg) {
                canvas.drawColor(Color.BLACK);
            }

            Paint paint = new Paint();
            paint.setAntiAlias(true);

            int currentX = 0;
            for (int col = 0; col < cols; col++) {
                for (int row = 0; row < rows; row++) {
                    int index = col * wrapCount + row;
                    if (index >= bitmaps.size()) break;

                    Bitmap bitmap = bitmaps.get(index);

                    int x = currentX;
                    int y = row * maxHeight;

                    if (centerAlign) {
                        x += (colWidths[col] - bitmap.getWidth()) / 2;
                    }

                    canvas.drawBitmap(bitmap, x, y, paint);
                    if (callback != null) callback.onProgress(index);
                    bitmap.recycle();
                }
                currentX += colWidths[col];
            }

            return result;
        } catch (Exception e) {
            ExceptionLogger.log(e, "ImageProcessor: mergeVertically");
            return null;
        }
    }

    // 비트맵 저장 메서드
    private static boolean saveBitmap(Bitmap bitmap, File outputFile) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            return bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            Log.e("ImageProcessor", "Error saving bitmap", e);
            ExceptionLogger.log(e, "ImageProcessor:saveBitmap");
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e("ImageProcessor", "Error closing stream", e);
                ExceptionLogger.log(e, "ImageProcessor:saveBitmap - IOException");
            }
            bitmap.recycle();
        }
    }
    
    public interface MergeCallback {
        void onProgress(int currentIndex);
    }
    
    public interface SplitCallback {
        void onProgress(int currentIndex);
    }
}