package com.dlawoals2713.oui4.file;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.dlawoals2713.oui4.file.base.BaseThemeActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FilemanagerMultiImageActivity extends BaseThemeActivity {
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
    private ViewPager viewPager;

    private ArrayList<File> imageFiles = new ArrayList<>();
    private int currentPosition = 0;
    private static final String KEY_CURRENT_POSITION = "current_position";

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.filemanager_image);
        initialize(_savedInstanceState);
        initializeLogic(_savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_POSITION, viewPager.getCurrentItem());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
        if (viewPager != null && viewPager.getAdapter() != null) {
            viewPager.setCurrentItem(currentPosition);
        }
    }

    private void initialize(Bundle _savedInstanceState) {
        hscroll1 = findViewById(R.id.hscroll1);
        linear2 = findViewById(R.id.linear2);
        textview1 = findViewById(R.id.textview1);

        // Initialize ViewPager
        viewPager = new ViewPager(this);
        viewPager.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linear2.addView(viewPager);

        textview1.setOnLongClickListener(_view -> {
            Toast.makeText(FilemanagerMultiImageActivity.this, "이미지 이름을 길게 눌렀습니다", Toast.LENGTH_SHORT).show();
            return true;
        });

        textview1.setOnClickListener(_view -> {
            linear2.setBackgroundColor(0xFF000000);
            hscroll1.setVisibility(View.GONE);
            hide = true;
        });
    }

    private void initializeLogic(Bundle _savedInstanceState) {
        String initialImagePath = getIntent().getStringExtra("path");
        int sortType = getIntent().getIntExtra("sort", 0);

        if (initialImagePath != null) {
            File initialFile = new File(initialImagePath);
            File parentDir = initialFile.getParentFile();

            if (parentDir != null && parentDir.exists()) {
                // Get all image files from the directory
                File[] files = parentDir.listFiles((dir, name) -> {
                    String lowerCase = name.toLowerCase();
                    return lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg") ||
                            lowerCase.endsWith(".png") || lowerCase.endsWith(".gif") ||
                            lowerCase.endsWith(".bmp") || lowerCase.endsWith(".webp");
                });

                if (files != null) {
                    imageFiles = new ArrayList<>(Arrays.asList(files));
                    SketchwareUtil.showMessage(getApplicationContext(), String.valueOf(sortType));

                    // Sort files based on sortType
                    switch (sortType) {
                        case 0:
                            imageFiles.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
                            break;
                        case 1:
                            imageFiles.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER).reversed());
                            break;
                        case 2:
                            imageFiles.sort(Comparator.comparingLong(File::length));
                            break;
                        case 3:
                            imageFiles.sort(Comparator.comparingLong(File::length).reversed());
                            break;
                        case 4:
                            imageFiles.sort(Comparator.comparingLong(File::lastModified));
                            break;
                        case 5:
                            imageFiles.sort(Comparator.comparingLong(File::lastModified).reversed());
                            break;
                        case 6: // 유형 순 (같은 유형은 이름 순)
                            imageFiles.sort((f1, f2) -> {
                                String ext1 = getFileExtension(f1.getName());
                                String ext2 = getFileExtension(f2.getName());
                                int extCompare = ext1.compareToIgnoreCase(ext2);
                                if (extCompare == 0) { // 확장자가 같으면
                                    return f1.getName().compareToIgnoreCase(f2.getName()); // 이름 순 정렬
                                }
                                return extCompare;
                            });
                            break;
                        case 7: // 유형 역순 (같은 유형은 이름 역순)
                            imageFiles.sort((f1, f2) -> {
                                String ext1 = getFileExtension(f1.getName());
                                String ext2 = getFileExtension(f2.getName());
                                int extCompare = ext1.compareToIgnoreCase(ext2);
                                if (extCompare == 0) { // 확장자가 같으면
                                    return f2.getName().compareToIgnoreCase(f1.getName()); // 이름 역순 정렬
                                }
                                return ext2.compareToIgnoreCase(ext1); // 확장자 역순 정렬
                            });
                            break;
                    }

                    // Find initial image position
                    currentPosition = imageFiles.indexOf(initialFile);
                    if (currentPosition == -1) {
                        currentPosition = 0;
                    }

                    // Setup ViewPager adapter
                    ImagePagerAdapter adapter = new ImagePagerAdapter(this, imageFiles);
                    viewPager.setAdapter(adapter);
                    if (_savedInstanceState == null) {
                        viewPager.setCurrentItem(currentPosition);
                    }

                    // Update title when page changes
                    viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                        @Override
                        public void onPageSelected(int position) {
                            currentPosition = position;
                            updateTitle();
                        }
                    });

                    updateTitle();
                }
            }
        }

        hide = false;
        darkmode = false;
        _DarkMode();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
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

    @SuppressLint("DefaultLocale")
    private void updateTitle() {
        if (currentPosition >= 0 && currentPosition < imageFiles.size()) {
            File currentFile = imageFiles.get(currentPosition);
            textview1.setText(String.format("%s\n(%d/%d)", currentFile.getName(), currentPosition + 1, imageFiles.size()));
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot != -1) {
            return fileName.substring(lastDot + 1);
        }
        return "";
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

    public void _DarkMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            hscroll1.setBackgroundColor(0xFF000000);
            linear2.setBackgroundColor(0xFF000000);
            textview1.setTextColor(0xFFFFFFFF);
            darkmode = true;
        }
    }

    private static class ImagePagerAdapter extends androidx.viewpager.widget.PagerAdapter {
        private Context context;
        private ArrayList<File> imageFiles;

        public ImagePagerAdapter(Context context, ArrayList<File> imageFiles) {
            this.context = context;
            this.imageFiles = imageFiles;
        }

        @Override
        public int getCount() {
            return imageFiles.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(android.view.ViewGroup container, int position) {
            // !!!!! 변경된 부분: FilemanagerSingleImageActivity.ZoomableImageView를 사용합니다.
            FilemanagerSingleImageActivity.ZoomableImageView imageView = new FilemanagerSingleImageActivity.ZoomableImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER); // 필요에 따라 ScaleType 설정

            // Glide로 이미지 로드
            Glide.with(context)
                    .load(imageFiles.get(position)) // Load from File
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .into(imageView); // ZoomableImageView에 로드

            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(android.view.ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
            // Glide 리소스 해제 (선택 사항이지만 좋은 습관)
            if (object instanceof ImageView) {
                Glide.with(context).clear((ImageView) object);
            }
        }
    }
}