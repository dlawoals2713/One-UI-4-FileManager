package com.dlawoals2713.oui4.file;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dlawoals2713.oui4.file.base.BaseThemeActivity;

import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;

import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import de.dlyt.yanndroid.oneui.layout.DrawerLayout;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.view.IndexScrollView;
import de.dlyt.yanndroid.oneui.view.RecyclerView;
import de.dlyt.yanndroid.oneui.widget.SwipeRefreshLayout;

public class FilemanagerActivity extends BaseThemeActivity {
    RecyclerView listView;
    private ImageAdapter imageAdapter;
    private HashMap<Integer, Boolean> selected = new HashMap<>();
    private HashMap<String, Parcelable> scrollStateMap = new HashMap<>();
    private HashMap<String, String> fileTypeCache = new HashMap<>(); // 파일 타입 캐시
    private HashMap<String, Bitmap> audioThumbnailCache = new HashMap<>(); // 오디오 썸네일 캐시
    private Map<String, FileInfo> fileInfoCache = new HashMap<>();
    private Map<String, Drawable> apkThumbnailCache = new HashMap<>();
    private final Map<String, Bitmap> videoThumbnailCache = new HashMap<>();
	private boolean mSelecting = false;
    private boolean checkAllListening = true;
    private boolean sr_refresh = true;
	private boolean sb = true;
	private int sort = 0;
	private int file_image = 0;
	private int file_video = 0;
    private DrawerLayout drawerLayout;
    private de.dlyt.yanndroid.oneui.widget.OptionButton _ftp_connect;
    private de.dlyt.yanndroid.oneui.widget.OptionButton _ftp_folder;
    private de.dlyt.yanndroid.oneui.widget.OptionButton _ftp_disconnect;
    private Context mContext;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    private FrameLayout mini_player_container;
    private LinearLayout mini_player;
    private MusicService musicService;
    private boolean isServiceBound = false;
    private ImageView mini_album_art;
    private TextView mini_song_title;
    private ImageButton mini_play_pause;

    private ImageView move_imageview1;
    private TextView move_size;
    private TextView move_count;
    private TextView move_path;
    private Button move_cancel;
    private Button move_move;

	private List<FTPFile> ftpFiles;
	private FTPHelper ftpHelper;
	private String currentPath = "/"; // 최상위 경로로 초기화

	private boolean title_unit = false;
	private int mode = 0;
	private boolean copy = false;
	private boolean backPressed = false;
	private boolean so = false;
	private double pos = 0;
	private int port = 0;
	private String folder = "";
    private String server = "";
	private String user = "";
	private String password = "";
	private int image_view = 0;
	private int video_view = 0;

	private ArrayList<HashMap<String, Object>> list = new ArrayList<>();
	private ArrayList<String> listinstring = new ArrayList<>();
	private ArrayList<String> list_name = new ArrayList<>();
	private ArrayList<String> fileSelect = new ArrayList<>();

    private SwipeRefreshLayout swipe_refresh;
	private IndexScrollView indexScrollView;
    private TextView path;

    private Intent intent = new Intent();

    private boolean isRTL() {
        return getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

	private boolean isBrowseZip = false;
	private String currentZipFilePath = "";
	private String currentZipInnerPath = ""; // Path inside the zip file
	private Stack<String> zipNavigationStack = new Stack<>(); // To keep track of navigation inside zip

	private String currentFirebasePath = "";
	// Pair의 첫 번째는 폴더 목록, 두 번째는 파일 목록을 저장합니다.
	private String userBasePath = ""; // 사용자의 기본 경로 (예: "Smart All in one/UID/")
	private boolean debugmode = false;

    public void setSelecting(boolean enabled) {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_view);
        FrameLayout move_status = findViewById(R.id.move_status);
        move_imageview1 = move_status.findViewById(R.id.move_imageview1);
        move_size = move_status.findViewById(R.id.move_size);
        move_count = move_status.findViewById(R.id.move_count);
        move_path = move_status.findViewById(R.id.move_path);
        move_cancel = move_status.findViewById(R.id.move_cancel);
        move_move = move_status.findViewById(R.id.move_move);

        if (enabled) {
            mSelecting = true;
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.getItemCount() - 1);
            drawerLayout.setSelectModeBottomMenu(R.menu.select_mode_menu, item -> {
                if (item.getItemId() == R.id.move) {
                    move_path.setText("원래 위치: " + folder);
                    move_status.setVisibility(View.VISIBLE);
                    copy = false;
                    new CalculateFileSizeTask().execute();
                } else if (item.getItemId() == R.id.copy) {
                    move_path.setText("원래 위치: " + folder);
                    move_status.setVisibility(View.VISIBLE);
                    copy = true;
                    new CalculateFileSizeTask().execute();
                } else if (item.getItemId() == R.id.share) {
                    share();
                } else if (item.getItemId() == R.id.delete) {
                    delete();
                } else if (item.getItemId() == R.id.make) {
                    make();
                } else if (item.getItemId() == R.id.edit) {
                    Toast.makeText(this, "오류", Toast.LENGTH_LONG).show();
                } else if (item.getItemId() == R.id.info) {
                    info();
                } else if (item.getItemId() == R.id.rename) {
                    rename();
                } else if (item.getItemId() == R.id.zip) {
                    zip();
                } else if (item.getItemId() == R.id.album_ex) {
                    extractAlbumArt();
                } else if (item.getItemId() == R.id.open) {
					openExternal();
				}
                //item.setBadge(item.getBadge() + 1);
                return true;
            });

            if (mode == 0) {
				int[] menuIdsToHide = {R.id.download};
				for (int id : menuIdsToHide) {
					de.dlyt.yanndroid.oneui.menu.MenuItem item = drawerLayout.getSelectModeBottomMenu().findItem(id);
					if (item != null) {
						item.setVisible(false);
					}
				}
			} else if (mode == 2) {
				int[] menuIdsToHide = {R.id.move, R.id.copy, R.id.share, R.id.info, R.id.zip, R.id.album_ex, R.id.make, R.id.rename, R.id.open};
				for (int id : menuIdsToHide) {
					de.dlyt.yanndroid.oneui.menu.MenuItem item = drawerLayout.getSelectModeBottomMenu().findItem(id);
					if (item != null) {
						item.setVisible(false);
					}
				}
			} else {
				int[] menuIdsToHide = {R.id.zip, R.id.album_ex, R.id.make};
				for (int id : menuIdsToHide) {
					de.dlyt.yanndroid.oneui.menu.MenuItem item = drawerLayout.getSelectModeBottomMenu().findItem(id);
					if (item != null) {
						item.setVisible(false);
					}
				}
			}
            drawerLayout.showSelectMode();
            drawerLayout.setSelectModeAllCheckedChangeListener((buttonView, isChecked) -> {
				if (checkAllListening) {
					for (int i = 0; i < imageAdapter.getItemCount() - 1; i++) {
						selected.put(i, isChecked);
						imageAdapter.notifyItemChanged(i);
					}
				}
				int count = 0;
				for (Boolean b : selected.values()) if (b) count++;
				drawerLayout.setSelectModeCount(count);
			});
        } else {
			mSelecting = false;
			for (int i = 0; i < imageAdapter.getItemCount() - 1; i++) selected.put(i, false);
			imageAdapter.notifyItemRangeChanged(0, imageAdapter.getItemCount() - 1);

			drawerLayout.setSelectModeCount(0);
			drawerLayout.dismissSelectMode();
		}
    }

    public void toggleItemSelected(int position) {
		selected.put(position, !selected.get(position));
		imageAdapter.notifyItemChanged(position);

		checkAllListening = false;
		int count = 0;
		for (Boolean b : selected.values()) if (b) count++;
		DrawerLayout drawerLayout = findViewById(R.id.drawer_view);
		drawerLayout.setSelectModeAllChecked(count == imageAdapter.getItemCount() - 1);
		drawerLayout.setSelectModeCount(count);
		checkAllListening = true;
	}

	private final ExecutorService thumbnailExecutor = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors() // 기기 코어 수만큼 스레드 생성
	);

	private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

		@Override
		public int getItemCount() {
				return list.size();
			}

		@Override
		public long getItemId(final int position) {
				return position;
			}

		@Override
		public int getItemViewType(final int position) {
			if (Objects.equals(list.get(position).get("file"), "-1")) return 1;
			return 0;
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			int resId = 0;

			switch (viewType) {
				case 0:
					resId = R.layout.icon_tab_listview_item;
					break;
				case 1:
					resId = R.layout.icon_tab_listview_bottom_spacing;
					break;
			}

			View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
			return new ViewHolder(view, viewType);
		}

		@SuppressLint("DiscouragedApi")
		@Override
		public void onBindViewHolder(ImageAdapter.ViewHolder holder, final int position) {
			// Get the item HashMap which contains all details for the current position
			// This assumes 'list' is accessible (e.g., ImageAdapter is an inner class or 'list' is passed)
			HashMap<String, Object> currentItem = list.get(position);

			if (holder.isItem) { // Assumes isItem logic is correctly set in ViewHolder constructor based on viewType
				holder.checkBox.setVisibility(mSelecting ? View.VISIBLE : View.GONE);
				holder.checkBox.setChecked(selected.get(position));

				if (mode == 1) {
					// FTP 모드 처리
					FTPFile ftpFile = getFTPFile(position);
					if (ftpFile != null) {
						// 파일 타입에 따라 아이콘 설정
						if (ftpFile.isDirectory()) {
							holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_folder", "drawable", getPackageName()));
						} else {
							String fileType = getFTPFileType(ftpFile.getName());
							int resId = getResources().getIdentifier("ic_samsung_file_type_" + fileType, "drawable", getPackageName());
							if (resId != 0) {
								holder.imageView.setImageResource(resId);
							} else {
								holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_etc", "drawable", getPackageName()));
							}
						}

						holder.textView.setText(ftpFile.getName());
						holder.textView_count.setText(ftpFile.isDirectory() ? "" : formatFileSize(ftpFile.getSize()));
						SimpleDateFormat sdf = new SimpleDateFormat("y년 M월 d일 a h:m:s", Locale.getDefault());
						holder.textView_date.setText(sdf.format(ftpFile.getTimestamp().getTime()));
					}
				} else if (isBrowseZip) { // --- MODIFIED: Handle zip Browse mode ---
					String name = (String) currentItem.get("name");
					boolean isDirectory = (Boolean) currentItem.get("isDirectory");
					String size = (String) currentItem.get("size");
					String date = (String) currentItem.get("date");

					holder.textView.setText(name);
					holder.textView_count.setText(size); // Display size for zip entry
					holder.textView_date.setText(date);   // Display date for zip entry

					// Handle ".." specifically for display, even if onBackPressed handles actual navigation
					if (name.equals("..")) {
						holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_folder_up", "drawable", getPackageName()));
					} else if (isDirectory) {
						holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_folder", "drawable", getPackageName()));
					} else {
						// Determine icon for file inside zip based on extension
						String filePathInZip = (String) currentItem.get("file"); // This is the full path inside the zip
						String fileType = getFileType(filePathInZip); // Re-use getFileType for extension check
						int resId = getResources().getIdentifier("ic_samsung_file_type_" + fileType, "drawable", getPackageName());
						if (resId != 0) {
							holder.imageView.setImageResource(resId);
						} else {
							holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_etc", "drawable", getPackageName()));
						}
					}
				} else {
					String filePath = listinstring.get(position);
					holder.textView.setText(list_name.get(position)); // 이름은 바로 설정

					// (RecyclerView 재활용 버그 방지)
					holder.itemView.setTag(filePath);

					// 1. 캐시에서 파일 타입과 정보 모두 확인
					final FileInfo cachedInfo = fileInfoCache.get(filePath);
					final String cachedFileType = fileTypeCache.get(filePath);

					if (cachedInfo != null && cachedFileType != null) {
						// 2. 캐시 히트: 정보와 아이콘 바로 설정
						holder.textView_count.setText(cachedInfo.sizeOrCount);
						holder.textView_date.setText(cachedInfo.modifiedDate);
						loadIconBasedOnType(holder, filePath, cachedFileType); // (아래 3번 항목 참조)
					} else {
						// 3. 캐시 미스: 플레이스홀더 설정 후 비동기 로드
						holder.imageView.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_file_type_etc);
						holder.textView_count.setText("로드 중");
						holder.textView_date.setText("로드 중");

						thumbnailExecutor.execute(() -> {
							// 4. (느린 작업) 파일 정보와 타입을 백그라운드에서 가져오기
							final FileInfo fileInfo = getFileInfo(filePath);
							final String fileType = getFileType(filePath); // MediaMetadataRetriever 호출

							// 5. 결과 캐싱
							fileInfoCache.put(filePath, fileInfo);
							fileTypeCache.put(filePath, fileType); // 파일 타입도 캐시

							// 6. 메인 스레드에서 UI 업데이트
							mainHandler.post(() -> {
								// 7. (중요) 뷰 재활용 확인
								if (filePath.equals(holder.itemView.getTag())) {
									holder.textView_count.setText(fileInfo.sizeOrCount);
									holder.textView_date.setText(fileInfo.modifiedDate);
									// 아이콘도 여기서 로드
									loadIconBasedOnType(holder, filePath, fileType);
								}
							});
						});
					}
				}

				// Common click listeners (remain unchanged)
				holder.parentView.setOnClickListener(view -> {
					if (mSelecting) {
						toggleItemSelected(position);
					} else {
						backPressed = false;
						_open(position);
					}
				});

				holder.parentView.setOnLongClickListener(v -> {
					if (!mSelecting) setSelecting(true);
					toggleItemSelected(position);
					listView.seslStartLongPressMultiSelection();
					listView.seslSetLongPressMultiSelectionListener(new RecyclerView.SeslLongPressMultiSelectionListener() {
						@Override
						public void onItemSelected(RecyclerView var1, View var2, int var3, long var4) {
							if (getItemViewType(var3) == 0) toggleItemSelected(var3);
						}

						@Override
						public void onLongPressMultiSelectionEnded(int var1, int var2) {}

						@Override
						public void onLongPressMultiSelectionStarted(int var1, int var2) {}
					});
					return true;
				});
			}
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			boolean isItem;

			RelativeLayout parentView;
			ImageView imageView;
			TextView textView;
			CheckBox checkBox;
			TextView textView_count;
			TextView textView_date;

			public ViewHolder(View itemView, int viewType) {
				super(itemView);

				isItem = viewType == 0;

				if (isItem) {
					parentView = (RelativeLayout) itemView;
					imageView = parentView.findViewById(R.id.icon_tab_item_image);
					textView = parentView.findViewById(R.id.icon_tab_item_text);
					checkBox = parentView.findViewById(R.id.checkbox);
					textView_count = parentView.findViewById(R.id.count);
					textView_date = parentView.findViewById(R.id.date);
				}
			}
		}
	}

    public class ItemDecoration extends RecyclerView.ItemDecoration {
		private Drawable mDivider;
		private int mDividerHeight;

		@Override
		public void seslOnDispatchDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.State state) {
			super.seslOnDispatchDraw(canvas, recyclerView, state);
			int childCount = recyclerView.getChildCount();
			int width = recyclerView.getWidth();

			for (int i = 0; i < childCount; i++) {
				View childAt = recyclerView.getChildAt(i);
				int y = ((int) childAt.getY()) + childAt.getHeight();
				if (mDivider != null) {
					int moveRTL = isRTL() ? 130 : 0;
					mDivider.setBounds(130 - moveRTL, y, width - moveRTL, mDividerHeight + y);
					mDivider.draw(canvas);
				}
			}
		}

		public void setDivider(Drawable d) {
			mDivider = d;
			mDividerHeight = d.getIntrinsicHeight();
			listView.invalidateItemDecorations();
		}
	}

	private void loadIconBasedOnType(ImageAdapter.ViewHolder holder, String filePath, String fileType) {
		switch (fileType) {
			case "folder":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_folder", "drawable", getPackageName()));
				break;
			case "apk":
				loadApkThumbnail(holder, filePath);
				break;
			case "audio":
				loadAudioThumbnail(holder, filePath);
				break;
			case "video":
				loadVideoThumbnail(holder, filePath);
				break;
			case "image_raw":
			case "image":
				loadImageThumbnail(holder, filePath);
				break;
			case "txt":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_txt", "drawable", getPackageName()));
				break;
			case "zip":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_zip", "drawable", getPackageName()));
				break;
			case "pdf":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_adobe", "drawable", getPackageName()));
				break;
			case "html":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_html", "drawable", getPackageName()));
				break;
			case "excel":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_excel", "drawable", getPackageName()));
				break;
			case "gltf":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_gltf", "drawable", getPackageName()));
				break;
			case "hwp":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_hangul", "drawable", getPackageName()));
				break;
			case "ppt":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_ppt", "drawable", getPackageName()));
				break;
			case "snb":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_snb", "drawable", getPackageName()));
				break;
			case "spd":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_spd", "drawable", getPackageName()));
				break;
			case "word":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_word", "drawable", getPackageName()));
				break;
			case "vlc_audio":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_audio_vlc", "drawable", getPackageName()));
				break;
			case "vlc_video":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_video_vlc", "drawable", getPackageName()));
				break;
			case "vlc_other":
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_etc_vlc", "drawable", getPackageName()));
				break;
			default:
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_etc", "drawable", getPackageName()));
				break;
		}
	}

    // 파일 정보 클래스 (크기, 날짜, 개수 포함)
    private static class FileInfo {
		String sizeOrCount;
		String modifiedDate;

		FileInfo(String sizeOrCount, String modifiedDate) {
			this.sizeOrCount = sizeOrCount;
			this.modifiedDate = modifiedDate;
		}
	}

    // 파일 정보 가져오기 (폴더 개수 / 파일 크기 & 수정 날짜)
	// FilemanagerActivity.java 파일 내 getFileInfo 메서드
	private FileInfo getFileInfo(String filePath) {
		File file = new File(filePath);
		SimpleDateFormat desiredDateFormat = new SimpleDateFormat("yyyy년 M월 d일 a h:m:s", Locale.getDefault());

		// 파일이나 폴더가 실제로 존재하는지 확인
		if (!file.exists()) {
			return new FileInfo("파일/폴더 없음", "날짜 정보 없음"); // 더 명확한 메시지
		}

		if (file.isDirectory()) {
			long lastModifiedMillis = file.lastModified();
			String modifiedDate;
			if (lastModifiedMillis > 0) { // 수정 시간이 유효한지 확인
				modifiedDate = desiredDateFormat.format(new Date(lastModifiedMillis));
			} else {
				modifiedDate = "날짜 정보 없음"; // 유효하지 않은 수정 시간인 경우
			}

			int fileCount = 0;
			File[] files = file.listFiles();
			if (files != null) {
				fileCount = files.length;
				// 파일 목록을 성공적으로 가져온 경우
				return new FileInfo(fileCount + "개", modifiedDate);
			} else {
				// file.listFiles()가 null을 반환하는 경우 (대부분 권한 문제)
				return new FileInfo("알 수 없음", modifiedDate); // 더 구체적인 메시지
			}
		} else { // 파일인 경우
			long lastModifiedMillis = file.lastModified();
			String modifiedDate;
			if (lastModifiedMillis > 0) {
				modifiedDate = desiredDateFormat.format(new Date(lastModifiedMillis));
			} else {
				modifiedDate = "-";
			}
			return new FileInfo(formatFileSize(file.length()), modifiedDate);
		}
	}

    // 파일 크기 포맷팅
    private String formatFileSize(long size) {
		if (size <= 0) return "0 B";
		final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}


    // 파일 타입 확인 메서드
    private String getFileType(String filePath) {
		if (mode == 1) {
			return "etc";
		} else {
			if (fileTypeCache.containsKey(filePath)) {
				return fileTypeCache.get(filePath); // 캐시된 파일 타입 반환
			}

			String fileType;
			if (FileUtil.isDirectory(filePath)) {
				fileType = "folder";
			} else if (filePath.endsWith(".apk")) {
				fileType = "apk";
			} else if (isAudioFile(filePath)) {
				fileType = "audio";
			} else if (isVideoFile(filePath)) {
				fileType = "video";
			} else if (isRawImage(filePath)) {
				fileType = "image_raw";
			} else if (isImageFile(filePath)) {
				fileType = "image";
			} else if (filePath.toLowerCase().endsWith(".txt")) {
				fileType = "txt";
			} else if (filePath.toLowerCase().endsWith(".zip")) {
				fileType = "zip";
			} else if (filePath.toLowerCase().endsWith(".pdf")) {
				fileType = "pdf";
			} else if (filePath.toLowerCase().endsWith(".xml") || filePath.toLowerCase().endsWith(".html")) {
				fileType = "html";
			} else if (isExcelFile(filePath)) {
				fileType = "excel";
			} else if (filePath.toLowerCase().endsWith(".gltf")) {
				fileType = "gltf";
			} else if (filePath.toLowerCase().endsWith(".hwp")) {
				fileType = "hwp";
			} else if (isPptFile(filePath)) {
				fileType = "ppt";
			} else if (filePath.toLowerCase().endsWith(".snb")) {
				fileType = "snb";
			} else if (filePath.toLowerCase().endsWith(".spd")) {
				fileType = "spd";
			} else if (filePath.toLowerCase().endsWith(".vnt")) {
				fileType = "vnt";
			} else if (isWordFile(filePath)) {
				fileType = "word";
			} else if (isVlcAudio(filePath)) {
				fileType = "vlc_audio";
			} else if (isVlcVideo(filePath)) {
				fileType = "vlc_video";
			} else if (isVlcOther(filePath)) {
				fileType = "vlc_other";
			} else {
				fileType = "etc"; // 기본 파일 타입
			}

			fileTypeCache.put(filePath, fileType); // 캐시에 저장
			return fileType;
		}
	}

    private void loadImageThumbnail(ImageAdapter.ViewHolder holder, String filePath) {
		if ("image_raw".equals(getFileType(filePath))) {
			Glide.with(holder.itemView.getContext())
				.load(new File(filePath))
				.override(100, 100) // 이미지 크기 조정
				.diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 사용
				.placeholder(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_file_type_raw) // 로딩 중 표시할 이미지
				.error(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_file_type_raw) // 에러 시 표시할 이미지
				.into(holder.imageView);
		} else {
			Glide.with(holder.itemView.getContext())
				.load(new File(filePath))
				.override(100, 100) // 이미지 크기 조정
				.diskCacheStrategy(DiskCacheStrategy.ALL) // 캐싱 사용
				.placeholder(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_file_type_image) // 로딩 중 표시할 이미지
				.error(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_file_type_image) // 에러 시 표시할 이미지
				.into(holder.imageView);
		}
	}

    @SuppressLint("DiscouragedApi")
    private void loadAudioThumbnail(ImageAdapter.ViewHolder holder, String filePath) {
		if (audioThumbnailCache.containsKey(filePath)) {
			Bitmap cachedBitmap = audioThumbnailCache.get(filePath);
			if (cachedBitmap != null) {
				androidx.core.graphics.drawable.RoundedBitmapDrawable rbd = androidx.core.graphics.drawable.RoundedBitmapDrawableFactory.create(getResources(), cachedBitmap);
				rbd.setCircular(true);
				holder.imageView.setImageDrawable(rbd);
			} else {
				holder.imageView.setImageResource(getResources().getIdentifier((filePath.toLowerCase().endsWith(".amr") || filePath.toLowerCase().endsWith(".m4a")) ? "ic_samsung_file_type_amr" : "ic_samsung_file_type_audio", "drawable", getPackageName()));
			}
		} else {
			thumbnailExecutor.execute(() -> {
				MediaMetadataRetriever retriever = new MediaMetadataRetriever();
				try {
					retriever.setDataSource(filePath);
					byte[] art = retriever.getEmbeddedPicture();
					if (art != null) {
						Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
						audioThumbnailCache.put(filePath, bitmap); // 캐시에 저장

						new Handler(Looper.getMainLooper()).post(() -> {
							androidx.core.graphics.drawable.RoundedBitmapDrawable rbd = androidx.core.graphics.drawable.RoundedBitmapDrawableFactory.create(getResources(), bitmap);
							rbd.setCircular(true);
							holder.imageView.setImageDrawable(rbd);
						});
					} else {
						new Handler(Looper.getMainLooper()).post(() -> holder.imageView.setImageResource(getResources().getIdentifier((filePath.toLowerCase().endsWith(".amr") || filePath.toLowerCase().endsWith(".m4a")) ? "ic_samsung_file_type_amr" : "ic_samsung_file_type_audio", "drawable", getPackageName())));
					}
				} catch (Exception e) {
					ExceptionLogger.log(e, "FileManagerActivity:loadAudioThumbnail");
					e.printStackTrace();
					new Handler(Looper.getMainLooper()).post(() -> holder.imageView.setImageResource(getResources().getIdentifier((filePath.toLowerCase().endsWith(".amr") || filePath.toLowerCase().endsWith(".m4a")) ? "ic_samsung_file_type_amr" : "ic_samsung_file_type_audio", "drawable", getPackageName())));
				} finally {
					try {
						retriever.release();
					} catch (IOException e) {
						ExceptionLogger.log(e, "FileManagerActivity:loadAudioThumbnail-IOException");
						e.printStackTrace();
					}
				}
			});
		}
	}

    private void loadApkThumbnail(ImageAdapter.ViewHolder holder, String filePath) {
		if (apkThumbnailCache.containsKey(filePath)) {
			Drawable cachedDrawable = apkThumbnailCache.get(filePath);
			if (cachedDrawable != null) {
				holder.imageView.setImageDrawable(cachedDrawable);
			} else {
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_apk", "drawable", getPackageName()));
			}
		} else {
			thumbnailExecutor.execute(() -> {
				try {
					PackageManager pm = getPackageManager();
					PackageInfo pi = pm.getPackageArchiveInfo(filePath, 0);
					if (pi != null) {
						ApplicationInfo ai = pi.applicationInfo;
						ai.sourceDir = filePath;
						ai.publicSourceDir = filePath;
						Drawable icon = ai.loadIcon(pm);
						apkThumbnailCache.put(filePath, icon); // 캐시에 저장

						new Handler(Looper.getMainLooper()).post(() -> holder.imageView.setImageDrawable(icon));
					} else {
						throw new Exception("PackageInfo is null");
					}
				} catch (Exception e) {
					ExceptionLogger.log(e, "FileManagerActivity:loadApkThumbnail");
					e.printStackTrace();
					apkThumbnailCache.put(filePath, null); // 실패한 경우에도 null로 캐시
					new Handler(Looper.getMainLooper()).post(() -> holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_apk", "drawable", getPackageName())));
				}
			});
		}
	}

    private void loadVideoThumbnail(ImageAdapter.ViewHolder holder, String filePath) {
		if (videoThumbnailCache.containsKey(filePath)) {
			Bitmap cachedBitmap = videoThumbnailCache.get(filePath);
			if (cachedBitmap != null) {
				holder.imageView.setImageBitmap(cachedBitmap);
			} else {
				holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_video_vlc", "drawable", getPackageName()));
			}
		} else {
			thumbnailExecutor.execute(() -> {
				try {
					Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
					if (thumbnail != null) {
						Bitmap cropped = cropCenterSquare(thumbnail);
						videoThumbnailCache.put(filePath, cropped);
						new Handler(Looper.getMainLooper()).post(() -> holder.imageView.setImageBitmap(cropped));
					} else {
						throw new Exception("썸네일 null");
					}
				} catch (Exception e) {
					ExceptionLogger.log(e, "FileManagerActivity:loadVideoThumbnail");
					videoThumbnailCache.put(filePath, null); // 캐시에도 실패 저장
					new Handler(Looper.getMainLooper()).post(() ->
						holder.imageView.setImageResource(getResources().getIdentifier("ic_samsung_file_type_video", "drawable", getPackageName()))
					);
				}
			});
		}
	}

    private Bitmap cropCenterSquare(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int newEdge = Math.min(width, height);

		int xOffset = (width - newEdge) / 2;
		int yOffset = (height - newEdge) / 2;

		return Bitmap.createBitmap(bitmap, xOffset, yOffset, newEdge, newEdge);
	}

    // 캐시를 위한 Map
    private Map<String, String> mimeTypeCache = new HashMap<>();


    private String formatSize(long size) {
		if (size <= 0) return "0 B";

		final String[] units = {"B", "KB", "MB", "GB", "TB"};
		int unitIndex = 0;
		double sizeValue = size;

		while (sizeValue >= 1024 && unitIndex < units.length - 1) {
			sizeValue /= 1024;
			unitIndex++;
		}

		return String.format("%.2f %s", sizeValue, units[unitIndex]);
	}

    private class CalculateFileSizeTask extends AsyncTask<Void, Void, FileSizeResult> {
		@Override
		protected void onPreExecute() {
			move_size.setText("계산 중...");
			move_move.setText(copy ? "여기로 복사" : "여기로 이동");
		}

		@Override
		protected FileSizeResult doInBackground(Void... params) {
			try {
				int trueCount = 0;
				long totalSize = 0;

				for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
					if (entry.getValue()) {
						int index = entry.getKey();
						if (index >= 0 && index < listinstring.size()) {
							String filePath = listinstring.get(index);
							fileSelect.add(filePath);
							trueCount++;

                            if (mode == 1) {
                                // FTP 파일 크기 계산
                                totalSize += ftpHelper.getFtpFileSize(filePath);
                            } else {
								File file = new File(filePath);
								if (file.exists()) {
									if (file.isFile()) {
										totalSize += file.length();
									} else if (file.isDirectory()) {
										totalSize += getDirectorySize(file);
									}
								}
							}
						}
					}
				}

				return new FileSizeResult(trueCount, totalSize);
			} catch (Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:CalculateFileSizeTask");
                throw new RuntimeException(e);
            }
        }

		@Override
		protected void onPostExecute(FileSizeResult result) {
			super.onPostExecute(result);
			move_count.setText(result.trueCount + "개");
			move_size.setText(formatSize(result.totalSize));
			setSelecting(false);
		}
	}

    // 결과를 저장할 클래스
    private static class FileSizeResult {
		int trueCount;
		long totalSize;

		FileSizeResult(int trueCount, long totalSize) {
			this.trueCount = trueCount;
			this.totalSize = totalSize;
		}
	}

    // 폴더 내 파일 크기 합산 함수
    private long getDirectorySize(File directory) {
		long size = 0;
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					size += getDirectorySize(file); // 재귀적으로 폴더 크기 합산
				} else if (file.isFile()) {
					size += file.length(); // 파일 크기 합산
				}
			}
		}
		return size;
	}

    // 현재 폴더의 모든 항목 캐시 데이터 지우기
    private void clearCacheForCurrentFolder() {
		for (String filePath : listinstring) {
			fileTypeCache.remove(filePath);
			fileInfoCache.remove(filePath);
			audioThumbnailCache.remove(filePath);
			mimeTypeCache.remove(filePath);
			apkThumbnailCache.remove(filePath);
			videoThumbnailCache.remove(filePath);
		}

		fileTypeCache.remove(folder);
		fileInfoCache.remove(folder);
		mimeTypeCache.remove(folder);

		// Glide 캐시도 사용 중이면
		Glide.get(FilemanagerActivity.this).clearMemory();
		new Thread(() -> Glide.get(FilemanagerActivity.this).clearDiskCache()).start();
	}

	private class MultiFileOperationTask extends AsyncTask<Void, Integer, String> {
	    private de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog;
	    private final String operationType;
	    private int totalFiles = 0;
	    private int processedFiles = 0;
	    private String currentFileName = "";
	    private boolean isCountingFiles = true;
	    private boolean applyToAll = false;
	    private int lastAction = DialogInterface.BUTTON_POSITIVE; // 기본값: 이름 바꾸기

	    public MultiFileOperationTask(String operationType) {
			this.operationType = operationType;
		}

	    @Override
	    protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(FilemanagerActivity.this);
			progressDialog.setTitle(operationType.equals("move") ? "파일 이동 중" : "파일 복사 중");
			progressDialog.setMessage("(0/?)\n계산 중"); // 초기 메시지 설정
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.show();

			// 파일 개수 계산을 별도의 스레드에서 수행
			new Thread(() -> {
				totalFiles = 0;
				for (String filePath : fileSelect) {
					totalFiles += countFiles(new File(filePath));
				}
				isCountingFiles = false; // 파일 개수 계산 완료
				publishProgress(0); // 계산 완료 후 UI 업데이트
			}).start();
		}

		@Override
		protected String doInBackground(Void... voids) {
			try {
				while (isCountingFiles) {
					Thread.sleep(100);
				}
				for (String filePath : fileSelect) {
					if (isCancelled()) break;
					File source = new File(filePath);
					File destination = new File(folder, source.getName());

					currentFileName = source.getName();
					publishProgress(processedFiles);

					if (source.isDirectory()) {
						if (operationType.equals("move")) {
							moveDirWithProgress(source, destination);
						} else {
							copyDirWithProgress(source, destination);
						}
					} else {
						handleFileOperation(source, destination);
					}
					// 현재 파일/폴더의 작업이 완료된 후, 처리된 파일/폴더의 개수를 증가시킵니다.
					processedFiles++;
				}
				return null;
			} catch (Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:MultiFileOperationTask");
				return e.getMessage();
			}
		}

	    private void handleFileOperation(File source, File destination) throws Exception {
			if (!destination.exists()) {
				performOperation(source, destination);
				return;
			}

			if (!applyToAll) {
				final AtomicInteger action = new AtomicInteger(lastAction);
				final CountDownLatch latch = new CountDownLatch(1);

				runOnUiThread(() -> showConflictDialog(source, destination, action, latch));

				latch.await(); // 사용자 응답 대기

				if (action.get() == DialogInterface.BUTTON_NEGATIVE) { // 취소
					cancel(true);
					return;
				}

				lastAction = action.get();
			}

			if (lastAction == DialogInterface.BUTTON_POSITIVE) { // 이름 바꾸기
				File newDestination = getUniqueFile(destination);
				performOperation(source, newDestination);
			}
			// 건너뛰기(BUTTON_NEUTRAL)인 경우 아무 작업도 하지 않음
		}

	    private void showConflictDialog(File source, File destination, AtomicInteger action, CountDownLatch latch) {
			View dialogView = LayoutInflater.from(FilemanagerActivity.this)
					.inflate(R.layout.dialog_file_conflict, null);

			TextView txtSourceInfo = dialogView.findViewById(R.id.txt_source_info);
			TextView txtDestInfo = dialogView.findViewById(R.id.txt_dest_info);
			CheckBox chkApplyAll = dialogView.findViewById(R.id.chk_apply_all);

			// 파일 정보 설정
			String sourceInfo = String.format(Locale.getDefault(),
					"이름: %s\n크기: %s\n수정일: %s",
					source.getName(),
					formatFileSize(source.length()),
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(source.lastModified())));

			String destInfo = String.format(Locale.getDefault(),
					"이름: %s\n크기: %s\n수정일: %s",
					destination.getName(),
					formatFileSize(destination.length()),
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(destination.lastModified())));

			txtSourceInfo.setText(sourceInfo);
			txtDestInfo.setText(destInfo);

			new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(FilemanagerActivity.this)
					.setTitle("파일 충돌")
					.setView(dialogView)
					.setPositiveButton("이름 바꾸기", (dialog, which) -> {
						action.set(which);
						applyToAll = chkApplyAll.isChecked();
						latch.countDown();
					})
					.setNegativeButton("취소", (dialog, which) -> {
						action.set(which);
						latch.countDown();
					})
					.setNeutralButton("건너뛰기", (dialog, which) -> {
						action.set(which);
						applyToAll = chkApplyAll.isChecked();
						latch.countDown();
					})
					.setOnCancelListener(dialog -> {
						action.set(DialogInterface.BUTTON_NEGATIVE);
						latch.countDown();
					})
					.show();
		}

	    private File getUniqueFile(File original) {
			String baseName = original.getName();
			String extension = "";
			int dotIndex = baseName.lastIndexOf('.');

			if (dotIndex > 0) {
				extension = baseName.substring(dotIndex);
				baseName = baseName.substring(0, dotIndex);
			}

			int counter = 1;
			File newFile;
			do {
				String newName = baseName + "_" + counter + extension;
				newFile = new File(original.getParent(), newName);
				counter++;
			} while (newFile.exists());

			return newFile;
		}

	    private void performOperation(File source, File destination) throws Exception {
			if (operationType.equals("move")) {
				FileUtil.moveFile(source.getAbsolutePath(), destination.getAbsolutePath());
			} else {
				FileUtil.copyFile(source.getAbsolutePath(), destination.getAbsolutePath());
			}
			processedFiles++;
			publishProgress((processedFiles * 100) / totalFiles);
		}

	    @Override
	    protected void onProgressUpdate(Integer... values) {
			if (isCountingFiles) {
				progressDialog.setMessage("0/?\n계산 중"); // 계산 중 메시지 유지
			} else {
				progressDialog.setProgress(values[0]);
				progressDialog.setMessage("(" + processedFiles + "/" + totalFiles + ")\n" + currentFileName);
			}
		}

	    @Override
	    protected void onPostExecute(String errorMessage) {
			progressDialog.dismiss();
			if (errorMessage == null && !isCancelled()) {
				Toast.makeText(FilemanagerActivity.this,
					"파일을 " + (operationType.equals("move") ? "이동" : "복사") + "했습니다.",
					Toast.LENGTH_SHORT).show();
				fileSelect.clear();
			}
			backPressed = true;
			_refresh();
			setSelecting(false);
		}

	    @Override
	    protected void onCancelled() {
			progressDialog.dismiss();
			Toast.makeText(FilemanagerActivity.this, "작업이 취소되었습니다", Toast.LENGTH_SHORT).show();
			backPressed = true;
			_refresh();
			setSelecting(false);
		}

	    private int countFiles(File dir) {
			if (!dir.exists()) return 0; // 파일 또는 디렉토리가 존재하지 않으면 0 반환

			if (!dir.isDirectory()) {
				return 1; // 디렉토리가 아닌 파일인 경우 1 반환
			}

			File[] files = dir.listFiles();
			if (files == null) return 0; // 디렉토리 내용을 읽을 수 없는 경우 0 반환

			int count = 0;
			for (File file : files) {
				if (file.isDirectory()) {
					count += countFiles(file); // 디렉토리인 경우 재귀적으로 계산
				}
				count++; // 현재 파일 또는 디렉토리 개수 추가
			}
			return count;
		}

	    private void moveDirWithProgress(File source, File destination) throws Exception {
			File[] files = source.listFiles();
			if (files == null) return;

			// 디렉토리 충돌 처리
			if (destination.exists() && !applyToAll) {
				final AtomicInteger action = new AtomicInteger(lastAction);
				final CountDownLatch latch = new CountDownLatch(1);
				final File finalDestination = destination;

				runOnUiThread(() -> showConflictDialog(source, finalDestination, action, latch));
				latch.await();

				if (action.get() == DialogInterface.BUTTON_NEGATIVE) {
					cancel(true);
					return;
				}

				lastAction = action.get();
				destination = finalDestination;
			}

			if (lastAction == DialogInterface.BUTTON_POSITIVE && destination.exists()) {
				destination = getUniqueFile(destination);
			}

			if (!destination.exists()) destination.mkdirs();

			for (File file : files) {
				if (isCancelled()) break;

				currentFileName = file.getName();
				File newDest = new File(destination, file.getName());

				if (file.isDirectory()) {
					moveDirWithProgress(file, newDest);
				} else {
					handleFileOperation(file, newDest);
				}

				processedFiles++;
				int progress = (processedFiles * 100) / totalFiles;
				publishProgress(progress);
			}

			if (source.list().length == 0) {
				source.delete();
			}
		}

	    private void copyDirWithProgress(File source, File destination) throws Exception {
			File[] files = source.listFiles();
			if (files == null) return;

			// 디렉토리 충돌 처리
			if (destination.exists() && !applyToAll) {
				final AtomicInteger action = new AtomicInteger(lastAction);
				final CountDownLatch latch = new CountDownLatch(1);
				final File finalDestination = destination;

				runOnUiThread(() -> showConflictDialog(source, finalDestination, action, latch));
				latch.await();

				if (action.get() == DialogInterface.BUTTON_NEGATIVE) {
					cancel(true);
					return;
				}

				lastAction = action.get();
				destination = finalDestination;
			}

			if (lastAction == DialogInterface.BUTTON_POSITIVE && destination.exists()) {
				destination = getUniqueFile(destination);
			}

			if (!destination.exists()) destination.mkdirs();

			for (File file : files) {
				if (isCancelled()) break;

				currentFileName = file.getName();
				File newDest = new File(destination, file.getName());

				if (file.isDirectory()) {
					copyDirWithProgress(file, newDest);
				} else {
					handleFileOperation(file, newDest);
				}

				processedFiles++;
				int progress = (processedFiles * 100) / totalFiles;
				publishProgress(progress);
			}
		}
	}

	private void delete() {
		de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder builder = new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(FilemanagerActivity.this);
		builder.setTitle("삭제 확인");

		// 선택된 파일과 폴더의 개수를 계산
		int fileCount = 0;
		int folderCount = 0;
		for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
			if (entry.getValue()) {
				// 인덱스 범위 확인
				if (entry.getKey() >= 0 && entry.getKey() < listinstring.size()) {
					String filePath = listinstring.get(entry.getKey());
					File file = new File(filePath);
					if (file.isDirectory()) {
						folderCount++;
					} else {
						fileCount++;
					}
				} else {
					// 잘못된 인덱스인 경우 로그 출력
					Log.e("FilemanagerActivity", "Invalid index: " + entry.getKey());
				}
			}
		}

		// 메시지 설정
		StringBuilder messageBuilder = new StringBuilder();
		if (folderCount > 0) {
			messageBuilder.append("폴더 ").append(folderCount).append("개");
		}
		if (fileCount > 0) {
			if (folderCount > 0) {
				messageBuilder.append(", ");
			}
			messageBuilder.append("파일 ").append(fileCount).append("개");
		}
		messageBuilder.append("를 완전히 삭제할까요?");

		builder.setMessage(messageBuilder.toString());

		builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
			@SuppressLint("StaticFieldLeak")
			@Override
			public void onClick(DialogInterface _dialog, int _which) {
				new AsyncTask<Void, Integer, String>() {
					private de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog;
					private int totalFiles = 0; // 총 삭제할 파일/폴더 개수
					private int processedFiles = 0; // 현재까지 삭제된 파일/폴더 개수
					private String currentFileName = ""; // 현재 삭제 중인 파일 이름

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(FilemanagerActivity.this);
						progressDialog.setTitle("파일 삭제 중");
						progressDialog.setMessage("0/?\n계산 중"); // 초기 메시지 설정
						progressDialog.setCancelable(false);
						progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						progressDialog.show();

						// 총 삭제할 파일/폴더 개수 계산
						totalFiles = 0;
						for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
							if (entry.getValue()) {
								String filePath = listinstring.get(entry.getKey());
								File file = new File(filePath);
								if (file.isDirectory()) {
									totalFiles += countFiles(file); // 폴더 내부의 파일 및 폴더 개수 포함
								} else {
									totalFiles++; // 파일인 경우 1 추가
								}
							}
						}
						progressDialog.setMessage("0/" + totalFiles + "\n삭제 중"); // 계산 완료 후 메시지 업데이트
					}

					@Override
					protected String doInBackground(Void... voids) {
						try {
							// listinstring을 기반으로 삭제 수행
							for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
								if (entry.getValue()) { // 선택된 파일만 처리
									int position = entry.getKey();
									if (position >= 0 && position < listinstring.size()) { // 유효한 인덱스인지 확인
										String filePath = listinstring.get(position); // listinstring에서 파일 경로 가져오기
										File file = new File(filePath);
										currentFileName = file.getName(); // 현재 삭제 중인 파일 이름 업데이트

										if (file.exists()) {
											if (file.isDirectory()) {
												deleteDirWithProgress(file); // 폴더 삭제
											} else {
												FileUtil.deleteFile(filePath); // 파일 삭제
											}
										} else {
											Log.e("MainActivity", "파일이 존재하지 않습니다: " + filePath);
										}

										// 삭제 완료 후 진행 상황 업데이트
										processedFiles++;
										int progress = (int) ((processedFiles / (float) totalFiles) * 100);
										publishProgress(progress);
									} else {
										Log.e("MainActivity", "Invalid index: " + position);
									}
								}
							}
							return null;
						} catch (Exception e) {
							ExceptionLogger.log(e, "FileManagerActivity:delete");
							return e.getMessage();
						}
					}

					@Override
					protected void onProgressUpdate(Integer... values) {
						super.onProgressUpdate(values);
						// 진행 상황 업데이트
						progressDialog.setProgress(values[0]);
						progressDialog.setMessage(processedFiles + "/" + totalFiles + "\n" + currentFileName); // 삭제된 개수/전체 항목 및 현재 파일 이름 표시
					}

					@Override
					protected void onPostExecute(String errorMessage) {
						super.onPostExecute(errorMessage);
						progressDialog.dismiss();

						if (errorMessage == null) {
							Toast.makeText(FilemanagerActivity.this, "파일을 삭제 했습니다.", Toast.LENGTH_SHORT).show();

							// 삭제 완료 후 fileSelect 및 selected 비우기
							fileSelect.clear();

							// UI 갱신
							backPressed = true;
							_refresh();
						} else {
							Toast.makeText(FilemanagerActivity.this, "파일을 삭제하지 못했습니다. " + errorMessage, Toast.LENGTH_SHORT).show();
						}
						setSelecting(false);
					}

					// 폴더 삭제 메서드 (재귀적으로 파일 및 폴더 삭제)
					private void deleteDirWithProgress(File dir) {
						if (!dir.exists()) return;

						File[] files = dir.listFiles();
						if (files != null) {
							for (File file : files) {
								currentFileName = file.getName(); // 현재 삭제 중인 파일 이름 업데이트
								if (file.isDirectory()) {
									deleteDirWithProgress(file); // 재귀적으로 폴더 삭제
								} else {
									FileUtil.deleteFile(file.getAbsolutePath()); // 파일 삭제
								}
								processedFiles++;
								int progress = (int) ((processedFiles / (float) totalFiles) * 100);
								publishProgress(progress);
							}
						}
						dir.delete(); // 빈 폴더 삭제
					}

					// 파일 및 폴더 개수 계산 메서드
					private int countFiles(File dir) {
						if (!dir.exists()) return 0;

						if (!dir.isDirectory()) {
							return 1; // 파일인 경우 1 반환
						}

						File[] files = dir.listFiles();
						if (files == null) return 0;

						int count = 0;
						for (File file : files) {
							if (file.isDirectory()) {
								count += countFiles(file); // 재귀적으로 폴더 내부의 파일 및 폴더 개수 계산
							}
							count++; // 현재 파일 또는 폴더 개수 추가
						}
						return count;
					}
				}.execute();
			}
		});

		builder.setNegativeButton("취소", (_dialog, _which) -> {
		});

		builder.setPositiveButtonColor(mUseOUI4Theme ? mContext.getResources().getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red) : 0);

		builder.create().show();
	}

	private void share() {
		try {


			// 선택된 파일 목록을 저장할 리스트
			ArrayList<Uri> fileUris = new ArrayList<>();
			final int[] fileCount = {0}; // 파일 개수
			final int[] folderCount = {0}; // 폴더 개수

			// 선택된 항목을 순회하며 파일과 폴더를 구분
			for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
				if (entry.getValue()) { // 선택된 항목인 경우
					int position = entry.getKey();
					if (position >= 0 && position < listinstring.size()) { // 유효한 인덱스인지 확인
						String filePath = listinstring.get(position);
						File file = new File(filePath);

						if (file.isDirectory()) {
							folderCount[0]++; // 폴더 개수 증가
						} else {
							fileCount[0]++; // 파일 개수 증가
							// 파일 URI를 리스트에 추가
							Uri fileUri = FileProvider.getUriForFile(
									this,
									getPackageName() + ".provider", // FileProvider authority
									file
							);
							fileUris.add(fileUri);
						}
					}
				}
			}

			// 폴더가 하나라도 있는 경우 AlertDialog 표시
			if (folderCount[0] > 0) {
				new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this)
						.setTitle("확인")
						.setMessage("파일 " + fileCount[0] + "개를 공유할까요? 폴더 " + folderCount[0] + "개는 공유할 수 없습니다.")
						.setPositiveButton("확인", (dialog, which) -> {
							if (fileCount[0] > 0) {
								shareFiles(fileUris); // 파일 공유
							}
						})
						.setNegativeButton("취소", (dialog, which) -> {})
						.show();
			} else if (fileCount[0] > 0) {
				// 폴더가 없고 파일만 있는 경우 바로 공유
				shareFiles(fileUris);
			} else {
				// 선택된 파일이 없는 경우
				Toast.makeText(this, "공유할 파일이 없습니다.", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			ExceptionLogger.log(e, "FileManagerActivity:share");
		}
	}

	private void shareFiles(ArrayList<Uri> fileUris) {
	    if (fileUris.isEmpty()) {
			Toast.makeText(this, "공유할 파일이 없습니다.", Toast.LENGTH_SHORT).show();
			return;
		}

	    try {
			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
			shareIntent.setType("*/*");
			shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);

			// 파일 읽기 권한 부여
			shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

			// 공유 다이얼로그 표시
			startActivity(Intent.createChooser(shareIntent, "파일 공유"));
		} catch (Exception e) {
			ExceptionLogger.log(e, "FileManagerActivity:shareFiles");
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	private boolean isVlcAudio(String path) {
	    String ext = getFileExtension(path).toLowerCase();
	    List<String> vlcAudioExtensions = Arrays.asList(
	        "mp3", "aac", "m4a", "flac", "ogg", "wav", "wma", "opus", "amr", "aiff", "alac", "ape", "mka"
	    );
	    return vlcAudioExtensions.contains(ext);
	}

	private boolean isVlcVideo(String filePath) {
	    String lowerPath = filePath.toLowerCase();
	    return lowerPath.endsWith(".mp4") || lowerPath.endsWith(".mkv") || lowerPath.endsWith(".avi") ||
	           lowerPath.endsWith(".mov") || lowerPath.endsWith(".flv") || lowerPath.endsWith(".webm") ||
	           lowerPath.endsWith(".wmv") || lowerPath.endsWith(".3gp") || lowerPath.endsWith(".m4v") ||
	           lowerPath.endsWith(".ts") || lowerPath.endsWith(".mpeg") || lowerPath.endsWith(".mpg") ||
	           lowerPath.endsWith(".ogv") || lowerPath.endsWith(".rm") || lowerPath.endsWith(".rmvb");
	}

	private boolean isVlcOther(String path) {
	    String ext = getFileExtension(path).toLowerCase();
	    List<String> vlcOtherExtensions = Arrays.asList(
	        "m3u", "m3u8", "pls", "xspf",
	        "asf", "ogm", "divx", "dv", "f4v", "fli", "gxf", "mtv", "nsv", "nut", "ogx", "ps", "rec", "vob", "drc",
	        "m2p", "m2ts", "mpe", "trp", "vro", "xesc"
	    );
	    return vlcOtherExtensions.contains(ext);
	}

	private boolean isPptFile(String path) {
	    String ext = getFileExtension(path).toLowerCase();
	    return Arrays.asList("ppt", "pptx", "pps", "ppsx", "pot", "potx").contains(ext);
	}

	private boolean isExcelFile(String path) {
	    String ext = getFileExtension(path).toLowerCase();
	    return Arrays.asList("xls", "xlsx", "xlsm", "xlsb", "xlt", "xltx", "xla").contains(ext);
	}

	private boolean isWordFile(String path) {
	    String ext = getFileExtension(path).toLowerCase();
	    return Arrays.asList("doc", "docx", "docm", "dot", "dotx", "rtf").contains(ext);
	}

	private boolean isRawImage(String path) {
	    String ext = getFileExtension(path).toLowerCase();
	    List<String> rawExtensions = Arrays.asList(
	        "dng", "cr2", "cr3", "nef", "arw", "srf", "sr2",
	        "raf", "rw2", "orf", "pef", "srw", "x3f", "3fr", "mrw"
	    );
	    return rawExtensions.contains(ext);
	}

	private String getFileExtension(String path) {
	    int lastDot = path.lastIndexOf('.');
	    return (lastDot == -1) ? "" : path.substring(lastDot + 1);
	}

	private boolean isAudioFile(String filePath) {
	    return checkMimeType(filePath, "audio/");
}

	private boolean isVideoFile(String filePath) {
	    return checkMimeType(filePath, "video/");
}

	private boolean isImageFile(String filePath) {
	    // 먼저 파일 확장자로 간단히 검사
	    String fileExtension = filePath.toLowerCase();
	    if (fileExtension.endsWith(".jpg") || fileExtension.endsWith(".jpeg") || fileExtension.endsWith(".png") || fileExtension.endsWith(".gif") || fileExtension.endsWith(".bmp") || fileExtension.endsWith(".webp") || fileExtension.endsWith(".svg") || fileExtension.endsWith(".ico") || fileExtension.endsWith(".tif") || fileExtension.endsWith(".tiff") || fileExtension.endsWith(".jfif") || fileExtension.endsWith(".wbmp")) {
			return true;
		}
	    // 확장자만으로 판별 안되면 MIME 타입으로 확인
	    return checkMimeType(filePath, "image/");
	}

	private boolean checkMimeType(String filePath, String mimeTypePrefix) {
	    // 캐시된 MIME 타입이 있으면 바로 사용
	    String mimeType = mimeTypeCache.get(filePath);
	    if (mimeType == null) {
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			try {
				retriever.setDataSource(filePath);
				mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
				mimeTypeCache.put(filePath, mimeType); // 캐시 저장
			} catch (Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:checkMimeType");
				return false;
			} finally {
				try {
					retriever.release();
				} catch (IOException e) {
					ExceptionLogger.log(e, "FileManagerActivity:checkMimeType-IOException");
					e.printStackTrace();
				}
			}
		}
	    return mimeType != null && mimeType.startsWith(mimeTypePrefix);
	}

	private void info() {
	    // ProgressDialog 생성 및 표시
	    de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog =
	        new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(this);
	    progressDialog.setMessage("불러오는 중...");
	    progressDialog.setCancelable(false); // 사용자가 취소할 수 없도록 설정
	    progressDialog.show();

	    // 백그라운드 스레드에서 작업 수행
	    new Thread(() -> {
			int selectedFileCount = 0; // 파일 개수
			int selectedFolderCount = 0; // 폴더 개수
			int childFileCount = 0; // 파일 개수
			int childFolderCount = 0; // 폴더 개수
			long totalSize = 0;
			String type = "그룹"; // 유형 (기본값: 그룹)
			String modifiedDate = ""; // 수정한 날짜
			String permissions = ""; // 권한
			String fsPath = ""; // 파일 시스템 경로
			String fsType = ""; // 파일 시스템 유형
			String md5 = ""; // MD5 해시
			String imageInfo = ""; // 이미지 정보
			String videoInfo = ""; // 비디오 정보
			String audioInfo = ""; // 오디오 정보
			String archiveInfo = ""; // 압축 파일 정보
			String apkInfo = ""; // 압축 파일 정보

			// 선택된 파일 및 폴더 개수 계산 (자식 포함 X)
			for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
				if (entry.getValue()) { // 선택된 항목인 경우
					int position = entry.getKey();
					if (position >= 0 && position < listinstring.size()) { // 유효한 인덱스인지 확인
						String filePath = listinstring.get(position);
						File file = new File(filePath);

						if (file.isDirectory()) {
							selectedFolderCount++; // 선택된 폴더 개수 증가
						} else {
							selectedFileCount++; // 선택된 파일 개수 증가
							totalSize += file.length(); // 파일 크기를 totalSize에 추가
						}
					}
				}
			}

			// 선택된 폴더 내의 자식 파일 및 폴더 개수 계산
			for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
				if (entry.getValue()) { // 선택된 항목인 경우
					int position = entry.getKey();
					if (position >= 0 && position < listinstring.size()) { // 유효한 인덱스인지 확인
						String filePath = listinstring.get(position);
						File file = new File(filePath);

						if (file.isDirectory()) {
							// 선택된 폴더 내의 자식 파일 및 폴더 개수 계산
							childFileCount += countFilesInDirectory(file);
							childFolderCount += countFoldersInDirectory(file);
							totalSize += calculateDirectorySize(file); // 폴더 내 파일 크기를 totalSize에 추가
						}
					}
				}
			}

			// 타이틀 동적 생성
			StringBuilder titleBuilder = new StringBuilder("파일 정보: ");
			if (selectedFileCount > 0) {
				titleBuilder.append("파일 ").append(selectedFileCount).append("개");
			}
			if (selectedFolderCount > 0) {
				if (selectedFileCount > 0) {
					titleBuilder.append(", ");
				}
				titleBuilder.append("폴더 ").append(selectedFolderCount).append("개");
			}
			String title = titleBuilder.toString();

			// 정보 메시지 구성
			StringBuilder infoMessage = new StringBuilder();
			infoMessage.append("유형: ").append(selectedFileCount + selectedFolderCount > 1 ? "그룹" : "파일").append("\n");

			// 단일 폴더 선택 시 자식 파일/폴더 수 표시
			if (selectedFileCount + selectedFolderCount == 1 && selectedFolderCount == 1) {
				infoMessage.append("파일 수: ").append(childFileCount).append("개\n");
				infoMessage.append("폴더 수: ").append(childFolderCount).append("개\n");
			}

			// 그룹 선택 시 자식 파일/폴더 수 표시
			if (selectedFileCount + selectedFolderCount > 1) {
				if (childFileCount > 0) {
					infoMessage.append("파일 수: ").append(childFileCount).append("개\n");
				}
				if (childFolderCount > 0) {
					infoMessage.append("폴더 수: ").append(childFolderCount).append("개\n");
				}
			}

			infoMessage.append("크기: ").append(formatSize(totalSize)).append("\n");

			// 단일 파일 선택 시 추가 정보 계산
			if (selectedFileCount + selectedFolderCount == 1) {
				for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
					if (entry.getValue()) {
						int position = entry.getKey();
						if (position >= 0 && position < listinstring.size()) {
							String filePath = listinstring.get(position);
							File file = new File(filePath);

							// 유형 설정
							if (file.isDirectory()) {
								type = "폴더";
							} else {
								type = getMimeType(filePath); // 파일 유형 가져오기
							}

							// 수정한 날짜
							modifiedDate = new SimpleDateFormat("y/M/d(c) a h:mm:ss", Locale.getDefault())
									.format(new Date(file.lastModified()));

							// 권한
							permissions = getFilePermissions(file);

							// 파일 시스템 경로 및 유형
							fsPath = file.getParent();
							fsType = getFileSystemType(file);

							// MD5 해시 (파일인 경우에만)
							if (file.isFile()) {
								md5 = calculateMD5(file);
							}

							// 이미지 정보
							if (isImageFile(filePath)) {
								BitmapFactory.Options options = new BitmapFactory.Options();
								options.inJustDecodeBounds = true;
								BitmapFactory.decodeFile(filePath, options);

								String mimeType = options.outMimeType;

								StringBuilder sb = new StringBuilder();
								sb.append("픽셀: ").append(options.outWidth).append("×").append(options.outHeight).append("\n");
								sb.append("형식: ").append(mimeType).append("\n");

								try {
									ExifInterface exif = new ExifInterface(filePath);

									String date = exif.getAttribute(ExifInterface.TAG_DATETIME);
									String make = exif.getAttribute(ExifInterface.TAG_MAKE);
									String model = exif.getAttribute(ExifInterface.TAG_MODEL);
									String orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);

									String aperture = exif.getAttribute(ExifInterface.TAG_APERTURE_VALUE);
									String iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS);
									String exposure = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);

									String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
									String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

									if (date != null) sb.append("촬영일: ").append(date).append("\n");
									if (make != null || model != null) sb.append("카메라: ").append(make).append(" ").append(model).append("\n");
									if (aperture != null) sb.append("조리개: f/").append(aperture).append("\n");
									if (iso != null) sb.append("ISO: ").append(iso).append("\n");
									if (exposure != null) sb.append("노출 시간: ").append(exposure).append("s\n");
									if (lat != null && lon != null) sb.append("위치: ").append(lat).append(", ").append(lon).append("\n");

									// 회전 정보 (선택적으로 이미지 미리보기에서 회전 보정 시 활용 가능)
									if (orientation != null) sb.append("회전: ").append(orientation).append(" (EXIF)\n");

								} catch (IOException e) {
									ExceptionLogger.log(e, "FileManagerActivity:info");
									e.printStackTrace();
								}

								imageInfo = sb.toString();

							}

							// 비디오 정보
							if (isVideoFile(filePath)) {
								MediaMetadataRetriever retriever = new MediaMetadataRetriever();
								retriever.setDataSource(filePath);
								String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
								String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
								String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
								String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
								String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
								String mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
								String date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
								String author = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
								String mtitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
								try {
									retriever.release();
								} catch (IOException e) {
									ExceptionLogger.log(e, "FileManagerActivity:info-IOException");
									e.printStackTrace();
								}

								videoInfo = "길이: " + formatDuration(Long.parseLong(duration)) + "\n" +
										"픽셀: " + width + "×" + height + "\n" +
										"비트레이트: " + (Integer.parseInt(bitrate) / 1000) + " kb/s\n" +
										"회전 각도: " + rotation + "°\n" +
										"인코딩 형식: " + mimeType + "\n" +
										(mtitle != null ? "제목: " + mtitle + "\n" : "") +
										(author != null ? "제작자: " + author + "\n" : "") +
										(date != null ? "촬영일: " + date + "\n" : "");
							}

							// 오디오 정보
							if (isAudioFile(filePath)) {
								MediaMetadataRetriever retriever = new MediaMetadataRetriever();
								retriever.setDataSource(filePath);

								String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
								String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
								String mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
								String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
								String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
								String mtitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
								String genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
								String date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
								String track = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);

								try {
									retriever.release();
								} catch (IOException e) {
									ExceptionLogger.log(e, "FileManagerActivity:info-IOException");
									e.printStackTrace();
								}

								StringBuilder sb = new StringBuilder();
								sb.append("길이: ").append(formatDuration(Long.parseLong(duration))).append("\n");
								sb.append("비트레이트: ").append(bitrate).append(" b/s\n");
								if (mime != null) sb.append("코덱: ").append(mime).append("\n");
								if (mtitle != null) sb.append("제목: ").append(mtitle).append("\n");
								if (artist != null) sb.append("아티스트: ").append(artist).append("\n");
								if (album != null) sb.append("앨범: ").append(album).append("\n");
								if (genre != null) sb.append("장르: ").append(genre).append("\n");
								if (track != null) sb.append("트랙 번호: ").append(track).append("\n");
								if (date != null) sb.append("작성일: ").append(date).append("\n");

								audioInfo = sb.toString();
							}

							// 압축 파일 정보
							if (isArchiveFile(filePath)) {
								long uncompressedSize = 0; // 압축이 풀렸을 때의 크기
								long compressedSize = 0; // 압축된 크기
								int fileCount = 0; // 압축 파일 내부의 파일 수

								try (ZipFile zipFile = new ZipFile(filePath)) {
									Enumeration<? extends ZipEntry> entries = zipFile.entries();
									while (entries.hasMoreElements()) {
										ZipEntry zipEntry = entries.nextElement();
										if (!zipEntry.isDirectory()) { // 디렉토리는 제외
											uncompressedSize += zipEntry.getSize(); // 원본 크기 합산
											compressedSize += zipEntry.getCompressedSize(); // 압축된 크기 합산
											fileCount++; // 파일 수 증가
										}
									}
								} catch (IOException e) {
									ExceptionLogger.log(e, "FileManagerActivity:info-IOException");
									e.printStackTrace();
								}

								// 압축비 계산
								double compressionRatio = 0;
								if (uncompressedSize > 0) {
									compressionRatio = (double) compressedSize / uncompressedSize * 100;
								}

								// 압축 파일 정보 구성
								archiveInfo = "유형: " + getArchiveType(filePath) + "\n" +
									"파일 수: " + fileCount + "\n" +
									"압축 해제 크기: " + formatSize(uncompressedSize) + "\n" +
									"압축된 크기: " + formatSize(compressedSize) + "\n" +
									"압축비: " + String.format("%.2f%%", compressionRatio);
							}

							if (isApkFile(filePath)) {
								apkInfo = getApkInfo(filePath);
								// apkInfo 변수에는 apk 메타데이터가 문자열로 저장됨
							}
						}
					}
				}

				infoMessage.append("수정한 날짜: ").append(modifiedDate).append("\n");
				infoMessage.append("권한: ").append(permissions).append("\n");
				infoMessage.append("FS 경로: ").append(fsPath).append("\n");
				infoMessage.append("FS 유형: ").append(fsType).append("\n");
				if (selectedFileCount == 1) {
					infoMessage.append("MD5: ").append(md5).append("\n");
				}

				if (!imageInfo.isEmpty()) {
					infoMessage.append("\n이미지 정보\n").append(imageInfo);
				}
				if (!videoInfo.isEmpty()) {
					infoMessage.append("\n비디오 정보\n").append(videoInfo);
				}
				if (!audioInfo.isEmpty()) {
					infoMessage.append("\n오디오 정보\n").append(audioInfo);
				}
				if (!archiveInfo.isEmpty()) {
					infoMessage.append("\n압축파일 정보\n").append(archiveInfo).append("\n");
				}
				if (!apkInfo.isEmpty()) {
					infoMessage.append("\nAPK 정보\n").append(apkInfo).append("\n").append("\n");
				}
			}


			runOnUiThread(() -> {
				progressDialog.dismiss(); // 작업 완료 후 다이얼로그 닫기

				// 다이얼로그 표시
				new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this)
					.setTitle(title) // 동적으로 생성된 타이틀 사용
					.setMessage(infoMessage.toString())
					.setPositiveButton("확인", (dialog, which) -> {})
					.show();
			});
		}).start();
	}

	private String sdkToAndroidVersion(int sdk) {
		switch (sdk) {
			case 1: return "1.0";
			case 2: return "1.1";
			case 3: return "1.5";
			case 4: return "1.6";
			case 5: return "2.0";
			case 6: return "2.0.1";
			case 7: return "2.1";
			case 8: return "2.2";
			case 9: return "2.3";
			case 10: return "2.3.3";
			case 11: return "3.0";
			case 12: return "3.1";
			case 13: return "3.2";
			case 14: return "4.0";
			case 15: return "4.0.3";
			case 16: return "4.1";
			case 17: return "4.2";
			case 18: return "4.3";
			case 19: return "4.4";
			case 20: return "4.4W";
			case 21: return "5.0";
			case 22: return "5.1";
			case 23: return "6.0";
			case 24: return "7.0";
			case 25: return "7.1";
			case 26: return "8.0";
			case 27: return "8.1";
			case 28: return "9";
			case 29: return "10";
			case 30: return "11";
			case 31: return "12";
			case 32: return "12L";
			case 33: return "13";
			case 34: return "14";
			case 35: return "15";
			case 36: return "16";
			case 37: return "17?";
			case 38: return "18?";
			case 39: return "19?";
			case 40: return "20?";
			default: return "Unknown";
		}
	}

	private String getApkInfo(String filePath) {
		try {
			PackageManager pm = mContext.getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_META_DATA);

			if (info != null) {
				ApplicationInfo appInfo = info.applicationInfo;
				appInfo.sourceDir = filePath;
				appInfo.publicSourceDir = filePath;

				String appName = pm.getApplicationLabel(appInfo).toString();
				String packageName = info.packageName;
				String versionName = info.versionName;
				int versionCode = info.versionCode;
				int minSdk = appInfo.minSdkVersion;
				int targetSdk = appInfo.targetSdkVersion;

				return "앱 이름: " + appName + "\n" +
						"패키지 이름: " + packageName + "\n" +
						"버전 이름: " + versionName + "\n" +
						"버전 코드: " + versionCode + "\n" +
						"Min SDK: " + minSdk + " (Android " + sdkToAndroidVersion(minSdk) + ")\n" +
						"Target SDK: " + targetSdk + " (Android " + sdkToAndroidVersion(targetSdk) + ")";
			}
		} catch (Exception e) {
			ExceptionLogger.log(e, "FileManagerActivity:getApkInfo");
			e.printStackTrace();
		}

		return "APK 정보를 가져올 수 없습니다.";
	}

	private String getFilePermissions(File file) {
	    String permissions = "";
	    if (file.canRead()) permissions += "r"; else permissions += "-";
	    if (file.canWrite()) permissions += "w"; else permissions += "-";
	    if (file.canExecute()) permissions += "x"; else permissions += "-";
	    return permissions;
	}

	public String getFileSystemType(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader("/proc/mounts"))) {
			String path = file.getAbsolutePath();
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\\s+");
				if (parts.length >= 3) {
					String mountPoint = parts[1];
					if (path.startsWith(mountPoint)) {
						return parts[2]; // 파일 시스템 타입 (e.g., fuse, ext4 등)
					}
				}
			}
		} catch (IOException e) {
			ExceptionLogger.log(e, "FileManagerActivity:getFileSystemType");
			e.printStackTrace();
		}
		return "unknown";
	}

	private String calculateMD5(File file) {
		try (InputStream inputStream = new FileInputStream(file)) {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[8192];
			int read;
			while ((read = inputStream.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			byte[] md5Bytes = digest.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : md5Bytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			ExceptionLogger.log(e, "FileManagerActivity:calculateMD5");
			e.printStackTrace();
			return "error";
		}
	}

   private String formatDuration(long duration) {
	   long seconds = duration / 1000;
	   long hours = seconds / 3600;
	   long minutes = (seconds % 3600) / 60;
	   long secs = seconds % 60;
	   return String.format("%d시간 %d분 %d초", hours, minutes, secs);
   }

// isArchiveFile 메서드 정의
	private boolean isArchiveFile(String filePath) {
	    String mimeType = getMimeType(filePath);
	    return mimeType != null && (
	        mimeType.equals("application/zip") ||
	        mimeType.equals("application/x-rar-compressed") ||
	        mimeType.equals("application/x-tar")
	    );
	}

	private boolean isApkFile(String filePath) {
		String mimeType = getMimeType(filePath);
		return (mimeType != null && mimeType.equals("application/vnd.android.package-archive")) || filePath.toLowerCase().endsWith(".apk");
	}

	// getMimeType 메서드 정의
	private String getMimeType(String filePath) {
	    String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
	    if (extension != null) {
			return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
		}
	    return "application/octet-stream"; // 기본 MIME 타입
	}

	private String getArchiveType(String filePath) {
	    String mimeType = getMimeType(filePath);
        switch (mimeType) {
            case "application/zip":
                return "zip";
            case "application/x-rar-compressed":
                return "rar";
            case "application/x-tar":
                return "tar";
        }
	    return "unknown";
	}

	private long calculateDirectorySize(File directory) {
	    long size = 0;
	    File[] files = directory.listFiles();
	    if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					size += calculateDirectorySize(file); // 재귀적으로 폴더 크기 계산
				} else {
					size += file.length(); // 파일 크기 추가
				}
			}
		}
	    return size;
	}

	private int countFilesInDirectory(File directory) {
	    int count = 0;
	    File[] files = directory.listFiles();
	    if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					count += countFilesInDirectory(file); // 재귀적으로 폴더 내 파일 개수 계산
				} else {
					count++; // 파일인 경우 개수 증가
				}
			}
		}
	    return count;
	}

	private int countFoldersInDirectory(File directory) {
	    int count = 0;
	    File[] files = directory.listFiles();
	    if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					count++; // 폴더인 경우 개수 증가
					count += countFoldersInDirectory(file); // 재귀적으로 폴더 내 폴더 개수 계산
				}
			}
		}
	    return count;
	}

	private void zip() {
		// 1. 선택된 파일/폴더 목록 가져오기
		ArrayList<File> selectedFiles = new ArrayList<>();
		ArrayList<File> selectedFolders = new ArrayList<>();

		for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
			if (entry.getValue()) {
				int position = entry.getKey();
				if (position >= 0 && position < listinstring.size()) {
					String filePath = listinstring.get(position);
					File file = new File(filePath);
					if (file.isDirectory()) {
						selectedFolders.add(file);
					} else {
						selectedFiles.add(file);
					}
				}
			}
		}

		if (selectedFiles.isEmpty() && selectedFolders.isEmpty()) {
			Toast.makeText(this, "압축할 파일이 없습니다.", Toast.LENGTH_SHORT).show();
			return;
		}

		// 2. 다이얼로그 설정
		de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder builder =
				new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this);
		builder.setTitle("압축 생성");
		View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_zip_settings, null);
		builder.setView(dialogView);

		// 3. UI 컴포넌트 바인딩
		EditText zipNameEditText = dialogView.findViewById(R.id.zip_name);
		Spinner formatSpinner = dialogView.findViewById(R.id.format_spinner);
		Spinner compressionLevelSpinner = dialogView.findViewById(R.id.compression_level_spinner);
		Spinner encryptionSpinner = dialogView.findViewById(R.id.encryption_spinner);
		EditText passwordEditText = dialogView.findViewById(R.id.password_edittext);
		EditText splitSizeEditText = dialogView.findViewById(R.id.split_size_edittext);

		// ★★★ 추가된 체크박스 바인딩 ★★★
		CheckBox includeEmptyFoldersCheckbox = dialogView.findViewById(R.id.include_empty_folders_checkbox);

		ArrayAdapter<CharSequence> formatAdapter = ArrayAdapter.createFromResource(
				this,
				R.array.zip_formats,
				android.R.layout.simple_spinner_item
		);
		formatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		formatSpinner.setAdapter(formatAdapter);

		ArrayAdapter<CharSequence> compressionAdapter = ArrayAdapter.createFromResource(
				this,
				R.array.compression_levels,
				android.R.layout.simple_spinner_item
		);
		compressionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		compressionLevelSpinner.setAdapter(compressionAdapter);

		ArrayAdapter<CharSequence> encryptionAdapter = ArrayAdapter.createFromResource(
				this,
				R.array.encryption_methods,
				android.R.layout.simple_spinner_item
		);
		encryptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		encryptionSpinner.setAdapter(encryptionAdapter);

		formatSpinner.setSelection(0);
		compressionLevelSpinner.setSelection(3);
		encryptionSpinner.setSelection(0);

		formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String selectedFormat = parent.getItemAtPosition(position).toString();
				boolean isTar = selectedFormat.startsWith("tar");
				boolean is7z = selectedFormat.equals("7z");

				encryptionSpinner.setEnabled(!isTar);
				passwordEditText.setEnabled(!isTar);

				if (is7z) {
					encryptionSpinner.setSelection(4); // AES-256 고정
					encryptionSpinner.setEnabled(false);
				}

				compressionLevelSpinner.setEnabled(!selectedFormat.equals("tar"));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});

		builder.setPositiveButton("확인", (dialog, which) -> {
			setSelecting(false);

			String zipName = zipNameEditText.getText().toString().trim();
			if (zipName.isEmpty()) zipName = "archive_" + System.currentTimeMillis();

			String format = formatSpinner.getSelectedItem().toString();
			File outputFile = new File(folder, zipName + "." + format);

			boolean includeEmptyFolders = includeEmptyFoldersCheckbox.isChecked();

			if (outputFile.exists()) {
				showOverwriteDialog(outputFile, selectedFiles, selectedFolders,
						format, compressionLevelSpinner.getSelectedItem().toString(),
						encryptionSpinner.getSelectedItem().toString(),
						passwordEditText.getText().toString(),
						parseSplitSize(splitSizeEditText.getText().toString()),
						includeEmptyFolders); // 전달
			} else {
				startNewCompression(outputFile, selectedFiles, selectedFolders,
						format, compressionLevelSpinner.getSelectedItem().toString(),
						encryptionSpinner.getSelectedItem().toString(),
						passwordEditText.getText().toString(),
						parseSplitSize(splitSizeEditText.getText().toString()),
						includeEmptyFolders); // 전달
			}
		});

		builder.setNegativeButton("취소", (dialog, which) -> {});
		builder.show();
		}

	// Helper Methods
	private long parseSplitSize(String sizeStr) {
	    if (sizeStr.isEmpty()) return 0;
	    return Long.parseLong(sizeStr) * 1024 * 1024;
	}

	private void setCompressionLevel(net.lingala.zip4j.model.ZipParameters parameters, String level) {
	    switch (level) {
			case "가장 빠르게":
				parameters.setCompressionLevel(CompressionLevel.FASTEST);
				break;
			case "더 빠르게":
				parameters.setCompressionLevel(CompressionLevel.FASTER);
				break;
			case "빠르게":
				parameters.setCompressionLevel(CompressionLevel.FAST);
				break;
			case "약간 빠르게":
				parameters.setCompressionLevel(CompressionLevel.MEDIUM_FAST);
				break;
			case "보통":
				parameters.setCompressionLevel(CompressionLevel.NORMAL);
				break;
			case "높게":
				parameters.setCompressionLevel(CompressionLevel.HIGHER);
				break;
			case "최대":
				parameters.setCompressionLevel(CompressionLevel.MAXIMUM);
				break;
			case "거의 극한":
				parameters.setCompressionLevel(CompressionLevel.PRE_ULTRA);
				break;
			case "극한":
				parameters.setCompressionLevel(CompressionLevel.ULTRA);
				break;
			default:
				parameters.setCompressionLevel(CompressionLevel.NO_COMPRESSION);
		}
	}

	private void setEncryption(net.lingala.zip4j.model.ZipParameters parameters, String method) {
	    parameters.setEncryptFiles(true);
	    switch (method) {
			case "AES-128":
				parameters.setEncryptionMethod(net.lingala.zip4j.model.enums.EncryptionMethod.AES);
				parameters.setAesKeyStrength(net.lingala.zip4j.model.enums.AesKeyStrength.KEY_STRENGTH_128);
				break;
			case "AES-192":
				parameters.setEncryptionMethod(net.lingala.zip4j.model.enums.EncryptionMethod.AES);
				parameters.setAesKeyStrength(net.lingala.zip4j.model.enums.AesKeyStrength.KEY_STRENGTH_192);
				break;
			case "AES-256":
				parameters.setEncryptionMethod(net.lingala.zip4j.model.enums.EncryptionMethod.AES);
				parameters.setAesKeyStrength(net.lingala.zip4j.model.enums.AesKeyStrength.KEY_STRENGTH_256);
				break;
			default: // ZipCrypto
				parameters.setEncryptionMethod(net.lingala.zip4j.model.enums.EncryptionMethod.ZIP_STANDARD);
		}
	}

	private void updateProgress(de.dlyt.yanndroid.oneui.dialog.ProgressDialog dialog,
								int processed, int total, String filename) {
	    runOnUiThread(() -> {
			int progress = (int) ((processed / (float) total) * 100);
			dialog.setProgress(progress);
			dialog.setMessage(filename + " (" + processed + "/" + total + ")");
		});
	}

	private void showOverwriteDialog(File existingFile,
									 ArrayList<File> selectedFiles,
									 ArrayList<File> selectedFolders,
									 String format,
									 String compressionLevel,
									 String encryptionMethod,
									 String password,
									 long splitSize,
									 boolean includeEmptyFolders) {

		new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this)
				.setTitle("파일 충돌")
				.setMessage("'" + existingFile.getName() + "' 파일이 이미 존재합니다.")
				.setPositiveButton("이름 변경", (dialog, which) -> {
					File newFile = generateNewFilename(existingFile);

					startNewCompression(
							newFile,
							selectedFiles,
							selectedFolders,
							format,
							compressionLevel,
							encryptionMethod,
							password,
							splitSize,
							includeEmptyFolders // ✅ 여기에 전달
					);
				})
				.setNegativeButton("기존 파일에 추가", (dialog, which) -> {
					addToExistingZip(existingFile, selectedFiles, selectedFolders);
				})
				.setNeutralButton("취소", (dialog, which) -> {
					zip();
				})
				.show();

		}

	private boolean isDirectoryEmpty(File directory) {
		if (directory.isDirectory()) {
			String[] files = directory.list();
			return files == null || files.length == 0;
		}
		return false;
	}

	private void startNewCompression(File outputFile,
									 ArrayList<File> selectedFiles,
									 ArrayList<File> selectedFolders,
									 String format,
									 String compressionLevel,
									 String encryptionMethod,
									 String password,
									 long splitSize,
									 boolean includeEmptyFolders) {
		// 1. 압축할 전체 파일 개수 계산 (폴더 제외)
		final int totalFileCount = getTotalFileCount(selectedFiles, selectedFolders);
		if (totalFileCount == 0 && !includeEmptyFolders) {
			Toast.makeText(this, "압축할 파일이 없습니다.", Toast.LENGTH_SHORT).show();
			return;
		}

		de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog =
				new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(this);
		progressDialog.setTitle("압축 중...");
		progressDialog.setProgressStyle(de.dlyt.yanndroid.oneui.dialog.ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMax(100); // 프로그래스 바는 백분율로 표시
		progressDialog.setMessage("압축 준비 중...");
		progressDialog.setCancelable(false);
		progressDialog.show();

		new Thread(() -> {
			try {
				// ZIP 파라미터 설정
				net.lingala.zip4j.model.ZipParameters parameters = new net.lingala.zip4j.model.ZipParameters();
				setCompressionLevel(parameters, compressionLevel);

				// 암호화 설정
				if (!password.isEmpty()) {
					setEncryption(parameters, encryptionMethod);
				}

				// ZIP 파일 생성
				net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(outputFile);
				if (!password.isEmpty()) {
					zipFile.setPassword(password.toCharArray());
				}

				// 진행률 추적을 위한 카운터
				java.util.concurrent.atomic.AtomicInteger compressedFileCount = new java.util.concurrent.atomic.AtomicInteger(0);

				// --- 최적화된 압축 로직 ---
				// 분할 압축과 일반 압축 로직을 분리하여 중복 실행을 방지합니다.
				if (splitSize > 0 && format.equals("zip")) {
					// 분할 압축 로직 (임시 폴더 사용)
					// 참고: 이 방식은 zip4j 라이브러리의 요구사항이며, 파일 복사로 인해 시간이 소요될 수 있습니다.
					// 파일 복사 진행률을 표시하려면 createTempFolder 메서드도 수정이 필요합니다.
					updateCompressionProgress(progressDialog, 0, 1, "임시 폴더 생성 중...");
					File tempFolder = createTempFolder(selectedFiles, selectedFolders, includeEmptyFolders);

					updateCompressionProgress(progressDialog, 0, 1, "분할 압축 파일 생성 중...");
					zipFile.createSplitZipFileFromFolder(tempFolder, parameters, true, splitSize);
					deleteFolder(tempFolder);
				} else {
					// 일반 압축 로직
					// 1. 선택된 개별 파일 추가
					for (File file : selectedFiles) {
						// 먼저 다음에 압축할 파일 번호를 가져와 다이얼로그에 표시
						int nextFileNumber = compressedFileCount.get() + 1;
						updateCompressionProgress(progressDialog, nextFileNumber, totalFileCount, file.getAbsolutePath());

						// 다이얼로그 업데이트 후 실제 압축 실행
						zipFile.addFile(file, parameters);

						// 압축 완료 후 실제 카운터 증가
						compressedFileCount.incrementAndGet();
					}

					// 2. 선택된 폴더 추가 (재귀적으로 탐색하며 파일 단위로 추가)
					for (File folder : selectedFolders) {
						// zip 내부 경로를 만들기 위한 기준 경로(선택한 폴더의 부모)를 계산합니다.
						String rootPath = folder.getParentFile().getAbsolutePath();
						addFolderToZip(zipFile, folder, rootPath, parameters, compressedFileCount, totalFileCount, progressDialog, includeEmptyFolders);
					}

					// 3. 사용자가 빈 폴더만 선택했을 경우 처리
					if (totalFileCount == 0 && includeEmptyFolders) {
						for (File folder : selectedFolders) {
							if (isDirectoryEmpty(folder)) {
								ZipParameters folderParam = new ZipParameters(parameters);
								folderParam.setFileNameInZip(folder.getName() + "/");
								zipFile.addStream(new ByteArrayInputStream(new byte[0]), folderParam);
							}
						}
					}
				}

				runOnUiThread(() -> {
					progressDialog.dismiss();
					Toast.makeText(this, "압축 완료: " + outputFile.getName(), Toast.LENGTH_SHORT).show();
					backPressed = true;
					_refresh();
				});

			} catch (Exception e) {
				runOnUiThread(() -> {
					progressDialog.dismiss();
					Toast.makeText(this, "압축 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
					backPressed = true;
					_refresh();
					ExceptionLogger.log(e, "FileManagerActivity:startNewCompression");
					e.printStackTrace();
				});
			}
		}).start();
	}

	public int getTotalFileCount(List<File> initialFiles, List<File> initialFolders) {
		// 초기 파일 개수 설정. initialFiles가 null인 경우 0으로 시작.
		int count = Objects.requireNonNullElse(initialFiles, new ArrayList<File>()).size();

		// 초기 폴더 리스트가 null인 경우 빈 리스트로 처리하여 NullPointerException 방지
		for (File folder : Objects.requireNonNullElse(initialFolders, new ArrayList<File>())) {
			// 폴더가 디렉토리인지 확인하고 유효한 파일 목록을 가져옴
			if (folder.isDirectory()) {
				// listFiles()가 null을 반환할 수 있으므로 안전하게 처리
				File[] filesInFolder = folder.listFiles();
				if (filesInFolder != null) {
					for (File file : filesInFolder) {
						if (file.isDirectory()) {
							// 하위 폴더인 경우 재귀적으로 호출하여 파일 개수를 추가
							// 이때, 하위 폴더에 있는 파일들만 세기 위해 initialFiles는 비어있는 리스트로 전달
							List<File> subFolders = new ArrayList<>();
							subFolders.add(file);
							count += getTotalFileCount(new ArrayList<>(), subFolders);
						} else {
							// 파일인 경우 개수 증가
							count++;
						}
					}
				}
			}
		}
		return count;
	}

	private void addFolderToZip(net.lingala.zip4j.ZipFile zipFile,
								File folderToAdd,
								String rootPath, // 경로 계산을 위해 추가된 파라미터
								net.lingala.zip4j.model.ZipParameters params,
								java.util.concurrent.atomic.AtomicInteger progressCounter,
								int totalFiles,
								de.dlyt.yanndroid.oneui.dialog.ProgressDialog dialog,
								boolean includeEmptyFolders) throws net.lingala.zip4j.exception.ZipException {

		File[] items = folderToAdd.listFiles();

		// 1. 폴더가 비어있고, 빈 폴더를 포함해야 하는 경우 처리
		if (includeEmptyFolders && (items == null || items.length == 0)) {
			// Zip 내부 경로 생성 (예: 'MySubFolder/')
			String entryName = folderToAdd.getAbsolutePath().substring(rootPath.length() + 1).replace(File.separator, "/") + "/";

			// zip4j는 경로 마지막에 '/'가 있어야 폴더로 인식합니다.
			if (!entryName.endsWith("/")) {
				entryName += "/";
			}

			ZipParameters folderParams = new ZipParameters(params);
			folderParams.setFileNameInZip(entryName);
			zipFile.addStream(new java.io.ByteArrayInputStream(new byte[0]), folderParams);
			return;
		}

		// 2. 폴더 내의 파일 및 하위 폴더 순회
		if (items != null) {
			for (File item : items) {
				if (item.isDirectory()) {
					// 하위 폴더의 경우, 재귀 호출로 계속 탐색
					addFolderToZip(zipFile, item, rootPath, params, progressCounter, totalFiles, dialog, includeEmptyFolders);
				} else {
					// 파일의 경우, 압축 파일에 직접 추가
					ZipParameters fileParams = new ZipParameters(params);
					// Zip 내부 경로 계산 (예: 'MyFolder/image.jpg')
					String entryName = item.getAbsolutePath().substring(rootPath.length() + 1).replace(File.separator, "/");
					fileParams.setFileNameInZip(entryName);

					// 먼저 다음에 압축할 파일 번호를 가져와 다이얼로그에 표시
					int nextFileNumber = progressCounter.get() + 1;
					updateCompressionProgress(dialog, nextFileNumber, totalFiles, item.getAbsolutePath());

					// 다이얼로그 업데이트 후 실제 압축 실행
					zipFile.addFile(item, fileParams);

					// 압축 완료 후 실제 카운터 증가
					progressCounter.incrementAndGet();
				}
			}
		}
	}

	private void updateCompressionProgress(de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog,
										   int compressedCount,
										   int totalCount,
										   String currentPath) {
		runOnUiThread(() -> {
			int progress = 0;
			// totalCount가 0일 때 0으로 나누기 오류 방지
			if (totalCount > 0) {
				progress = (int) (((float) compressedCount / totalCount) * 100);
			}

			progressDialog.setProgress(progress);

			String message = "(" + compressedCount + "/" + totalCount + ")\n"
					+ currentPath;
			progressDialog.setMessage(message);
		});
	}

	// 임시 폴더 생성 메서드
	private File createTempFolder(ArrayList<File> files, ArrayList<File> folders, boolean includeEmptyFolders) throws IOException {
		File tempFolder = new File(folder, "temp_zip_" + System.currentTimeMillis());
		if (!tempFolder.mkdirs()) throw new IOException("임시 폴더 생성 실패");

		// 1. 파일 복사
		for (File file : files) {
			File dest = new File(tempFolder, file.getName());
			copyFileCompat(file, dest);
		}

		// 2. 폴더 복사 (재귀 포함)
		for (File srcFolder : folders) {
			File destFolder = new File(tempFolder, srcFolder.getName());
			copyFolderRecursive(srcFolder, destFolder, includeEmptyFolders);
		}

		return tempFolder;
	}

	private void copyFileCompat(File source, File dest) throws IOException {
		InputStream in = new FileInputStream(source);
		OutputStream out = new FileOutputStream(dest);
		byte[] buffer = new byte[4096];
		int len;
		while ((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}
		in.close();
		out.close();
	}

	private void copyFolderRecursive(File source, File dest, boolean includeEmptyFolders) throws IOException {
		if (source.isDirectory()) {
			if (!dest.exists() && (includeEmptyFolders || source.listFiles().length > 0)) {
				dest.mkdirs();
			}
			File[] files = source.listFiles();
			if (files != null) {
				for (File file : files) {
					copyFolderRecursive(file, new File(dest, file.getName()), includeEmptyFolders);
				}
			}
		} else {
			copyFileCompat(source, dest);
		}
	}

	private void deleteFolder(File folder) {
		if (folder != null && folder.exists()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						deleteFolder(file); // 재귀
					} else {
						file.delete();
					}
				}
			}
			folder.delete(); // 마지막에 자기 자신 삭제
		}
	}

	private int duplicateCount;

// 3. 개선된 addToExistingZip 메서드
	private void addToExistingZip(File zipFile,
                            ArrayList<File> filesToAdd,
                            ArrayList<File> foldersToAdd) {
	    de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(this);
	    progressDialog.setTitle("파일 추가 중...");
	    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    progressDialog.show();
		new Thread(() -> {
			net.lingala.zip4j.ZipFile zip = new net.lingala.zip4j.ZipFile(zipFile);
			try {
				// 4. 중복 파일 카운팅 로직 추가
				duplicateCount = 0;
				int total = filesToAdd.size() + foldersToAdd.size();
				int processed = 0;

				for (File file : filesToAdd) {
					if (zip.getFileHeader(file.getName()) != null) duplicateCount++;
					zip.addFile(file);
					updateProgress(progressDialog, ++processed, total, file.getName());
				}

				for (File folder : foldersToAdd) {
					zip.addFolder(folder);
					updateProgress(progressDialog, ++processed, total, folder.getName());
				}

				runOnUiThread(() -> {
					progressDialog.dismiss();
					String msg = duplicateCount > 0 ?
						duplicateCount + "개 파일이 덮어써졌습니다" : "추가 완료!";
					Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
					backPressed = true;
					_refresh();
				});
				} catch (Exception e) {
					runOnUiThread(() -> {
						progressDialog.dismiss();
						Toast.makeText(this, "추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
						backPressed = true;
						ExceptionLogger.log(e, "FileManagerActivity:addToExistingZip");
						_refresh();
					});
				}
		}).start();
	}

	private File generateNewFilename(File originalFile) {
	    String originalName = originalFile.getName();
	    String baseName = originalName.replaceFirst("\\(\\d+\\)\\..+$", "");  // 기존 번호 제거
	    String extension = "";

	    // 확장자 분리 (예: .zip, .7z)
	    int dotIndex = originalName.lastIndexOf('.');
	    if (dotIndex > 0) {
			baseName = originalName.substring(0, dotIndex);
			extension = originalName.substring(dotIndex);
		}

	    // 번호 증가 로직
	    int counter = 1;
	    File newFile;
	    do {
			newFile = new File(originalFile.getParent(),
							  baseName + " (" + counter + ")" + extension);
			counter++;
		} while (newFile.exists());

	    return newFile;
	}
	private void rename() {
	    // 선택된 항목 확인 (기존 코드 동일)
	    List<Integer> selectedIndices = new ArrayList<>();
	    for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
			if (entry.getValue()) {
				selectedIndices.add(entry.getKey());
			}
		}

	    if (selectedIndices.isEmpty()) {
			Toast.makeText(this, "이름을 변경할 항목을 선택하세요.", Toast.LENGTH_SHORT).show();
			return;
		}

	    boolean hasFolder = false;
	    for (int index : selectedIndices) {
			if (index >= 0 && index < listinstring.size()) {
				File file = new File(listinstring.get(index));
				if (file.isDirectory()) {
					hasFolder = true;
					break;
				}
			}
		}

	    if (selectedIndices.size() > 1 && hasFolder) {
			Toast.makeText(this, "폴더가 포함된 경우 여러 파일의 이름을 한꺼번에 변경할 수 없습니다.", Toast.LENGTH_SHORT).show();
			return;
		}

	    File originalFile = new File(listinstring.get(selectedIndices.get(0)));
	    final String originalName = originalFile.getName();
	    final boolean isDirectory = originalFile.isDirectory();

	    de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder builder = new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this);
	    builder.setTitle("이름 변경");

	    final EditText input = new EditText(this);
	    input.setText(originalName);
	    input.setSelectAllOnFocus(true);
	    builder.setView(input);

	    // 다이얼로그 객체를 final로 선언
	    final de.dlyt.yanndroid.oneui.dialog.AlertDialog[] dialogHolder = new de.dlyt.yanndroid.oneui.dialog.AlertDialog[1];

	    if (selectedIndices.size() == 1) {
			builder.setPositiveButton("변경", null); // 먼저 null로 설정
		} else {
			builder.setMessage("<number>를 포함하여 이름을 입력하세요.\n예: 파일_<number>.txt");
			builder.setPositiveButton("변경", null); // 먼저 null로 설정
		}

	    builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

	    dialogHolder[0] = builder.create();

	    // 다이얼로그 표시 후 PositiveButton 리스너 설정
	    dialogHolder[0].setOnShowListener(dialogInterface -> {
			Button positiveButton = dialogHolder[0].getButton(DialogInterface.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(view -> {
				String newName = input.getText().toString().trim();
				boolean isValid;

				if (selectedIndices.size() == 1) {
					isValid = isValidFileName(newName, isDirectory);
					if (isValid) {
						new RenameTask(selectedIndices, newName, false).execute();
						dialogHolder[0].dismiss();
					} else {
						showInvalidNameError(input, "유효하지 않은 파일 이름입니다.");
					}
				} else {
					if (!newName.contains("<number>")) {
						showInvalidNameError(input, "이름 패턴에 <number>를 포함해야 합니다.");
						return;
					}

					String testName = newName.replace("<number>", "1");
					isValid = isValidFileName(testName, false);
					if (isValid) {
						// 재확인 다이얼로그 표시
						new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(FilemanagerActivity.this)
							.setTitle("확인")
							.setMessage("선택한 모든 파일이 다음 이름으로 변경됩니다:\n" +
								newName.replace("<number>", "1") + "\n" +
								newName.replace("<number>", "2") + "\n...")
							.setPositiveButton("변경", (d, w) -> {
								new RenameTask(selectedIndices, newName, true).execute();
								dialogHolder[0].dismiss();
							})
							.setNegativeButton("취소", (dialog, which) -> {})
							.show();
						} else {
						showInvalidNameError(input, "유효하지 않은 파일 이름 패턴입니다.");
						}
				}
			});
		});

	    // EditText 밑줄 색상 변경 리스너 (기존 코드 동일)
	    input.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				String newName = s.toString().trim();
				boolean isValid = selectedIndices.size() == 1 ?
					isValidFileName(newName, isDirectory) :
					newName.contains("<number>") && isValidFileName(newName.replace("<number>", "1"), false);

				if (isValid) {
					input.getBackground().clearColorFilter();
				} else {
					input.getBackground().setColorFilter(getResources().getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red), PorterDuff.Mode.SRC_IN);
				}
			}
		});

	    dialogHolder[0].show();
		}

// 파일/폴더 이름 유효성 검사
	private boolean isValidFileName(String name, boolean isFolder) {
	    if (name.isEmpty()) return false;

	    // Windows에서 허용되지 않는 문자 (Linux/macOS도 고려)
	    String invalidChars = "/\\:*?\"<>|";
	    for (int i = 0; i < invalidChars.length(); i++) {
			if (name.contains(String.valueOf(invalidChars.charAt(i)))) {
				return false;
			}
		}

	    // 폴더 이름은 마지막에 . 허용 안함
	    if (isFolder && name.endsWith(".")) {
			return false;
		}

	    // 예약된 이름 확인
	    String[] reservedNames = {"CON", "PRN", "AUX", "NUL",
		                             "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
		                             "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
	    for (String reserved : reservedNames) {
			if (name.equalsIgnoreCase(reserved)) {
				return false;
			}
		}

	    return true;
	}

	// 이름 변경 작업을 위한 AsyncTask
	private class RenameTask extends AsyncTask<Void, Integer, Boolean> {
	    private de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog;
	    private List<Integer> selectedIndices;
	    private String newNamePattern;
	    private boolean isMultiple;
	    private String errorMessage;

	    public RenameTask(List<Integer> selectedIndices, String newNamePattern, boolean isMultiple) {
			this.selectedIndices = selectedIndices;
			this.newNamePattern = newNamePattern;
			this.isMultiple = isMultiple;
		}

	    @Override
	    protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(FilemanagerActivity.this);
			progressDialog.setTitle("이름 변경 중");
			progressDialog.setMessage("처리 중...");
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(selectedIndices.size());
			progressDialog.show();
		}

	    @Override
	    protected Boolean doInBackground(Void... voids) {
			try {
				// 변경 전 모든 캐시 데이터 지우기
				clearCacheForSelectedItems();

				for (int i = 0; i < selectedIndices.size(); i++) {
					int index = selectedIndices.get(i);
					if (index >= 0 && index < listinstring.size()) {
						String originalPath = listinstring.get(index);
						File originalFile = new File(originalPath);

						String newName;
						if (isMultiple) {
							newName = newNamePattern.replace("<number>", String.valueOf(i + 1));
						} else {
							newName = newNamePattern;
						}

						String newPath = originalFile.getParent() + File.separator + newName;
						File newFile = new File(newPath);

						if (newFile.exists()) {
							errorMessage = "'" + newName + "' 이름은 이미 사용 중입니다.";
							return false;
						}

						if (!originalFile.renameTo(newFile)) {
							errorMessage = "이름 변경에 실패했습니다.";
							return false;
						}

						publishProgress(i + 1);
					}
				}
				return true;
			} catch (Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:RenameTask");
				errorMessage = e.getMessage();
				return false;
			}
		}

	    // 선택된 항목의 캐시 데이터 지우기
	    private void clearCacheForSelectedItems() {
			for (int index : selectedIndices) {
				if (index >= 0 && index < listinstring.size()) {
					String filePath = listinstring.get(index);
					// 파일 타입 캐시 지우기
					fileTypeCache.remove(filePath);
					// 파일 정보 캐시 지우기
					fileInfoCache.remove(filePath);
					// 오디오 썸네일 캐시 지우기
					audioThumbnailCache.remove(filePath);
					// 비디오 썸네일 캐시 지우기
					videoThumbnailCache.remove(filePath);
					// MIME 타입 캐시 지우기
					mimeTypeCache.remove(filePath);
				}
			}
		}

	    @Override
	    protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			progressDialog.dismiss();

			if (success) {
				Toast.makeText(FilemanagerActivity.this, "이름 변경이 완료되었습니다.", Toast.LENGTH_SHORT).show();
				setSelecting(false);
				backPressed = true;
				// 캐시 클리어 후 새로고침
				_refresh();
			} else {
				Toast.makeText(FilemanagerActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
			}
		}
	}

	// 유효하지 않은 이름 입력 시 에러 표시 메서드
	private void showInvalidNameError(EditText input, String message) {
	    // 밑줄 색상 변경
	    input.getBackground().setColorFilter(getResources().getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red), PorterDuff.Mode.SRC_IN);

	    // 에러 메시지 표시
	    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

	    // 입력 필드에 포커스 주기
	    input.requestFocus();

	    // 전체 텍스트 선택
	    input.setSelection(0, input.getText().length());
	}

	private void addItem(final boolean isFile) {
	    // 다이얼로그 생성
	    de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder builder =
	        new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this);
	    builder.setTitle(isFile ? "새 파일 만들기" : "새 폴더 만들기");

	    // EditText 설정
	    final EditText input = new EditText(this);
	    input.setHint(isFile ? "파일 이름 입력" : "폴더 이름 입력");
	    input.setSingleLine();
	    builder.setView(input);

	    // 다이얼로그 객체 생성
	    final de.dlyt.yanndroid.oneui.dialog.AlertDialog dialog = builder.create();

	    // 확인 버튼 설정
	    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "만들기", (d, which) -> {
			String name = input.getText().toString().trim();
			// 이름 유효성 검사
			if (!isValidFileName(name, !isFile)) {
				// 유효하지 않은 이름일 경우
				input.getBackground().setColorFilter(
					getResources().getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red),
					PorterDuff.Mode.SRC_IN
				);
				Toast.makeText(this, "유효하지 않은 이름입니다", Toast.LENGTH_SHORT).show();
				return;
			}

			// 파일/폴더 생성
			File newItem = new File(folder, name);
			try {
				if (isFile) {
					if (newItem.createNewFile()) {
						Toast.makeText(this, "파일이 생성되었습니다", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(this, "파일 생성 실패", Toast.LENGTH_SHORT).show();
						return;
					}
				} else {
					if (newItem.mkdir()) {
						Toast.makeText(this, "폴더가 생성되었습니다", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(this, "폴더 생성 실패", Toast.LENGTH_SHORT).show();
						return;
					}
				}

				// 성공 시 처리
				setSelecting(false);
				backPressed = true;
				_refresh();
			} catch (IOException e) {
				Toast.makeText(this, "생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				ExceptionLogger.log(e, "FileManagerActivity:addItem");
			}
		});

	    // 취소 버튼 설정
	    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소", (d, which) -> {
			dialog.dismiss();
			});

	    // EditText 텍스트 변경 리스너
	    input.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				// 입력값이 변경될 때마다 유효성 검사 및 UI 업데이트
				boolean isValid = isValidFileName(s.toString().trim(), !isFile);
				if (isValid) {
					input.getBackground().clearColorFilter();
				} else {
					input.getBackground().setColorFilter(
						getResources().getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red),
						PorterDuff.Mode.SRC_IN
					);
				}
			}
		});

	    // 다이얼로그 표시
	    dialog.show();
		}

	@SuppressLint("StaticFieldLeak")
    private void extractAlbumArt() {
	    de.dlyt.yanndroid.oneui.dialog.ProgressDialog loadingDialog =
	        new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(this);
	    loadingDialog.setMessage("파일 검색 중...");
	    loadingDialog.setCancelable(false);
	    loadingDialog.show();
		new AsyncTask<Void, Void, Pair<ArrayList<String>, Pair<Integer, Integer>>>() {
			@Override
			protected Pair<ArrayList<String>, Pair<Integer, Integer>> doInBackground(Void... voids) {
				ArrayList<String> selectedItems = new ArrayList<>();
				int audioCount = 0;
				int nonAudioCount = 0;

				for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
					if (entry.getValue() && entry.getKey() < listinstring.size()) {
						String path = listinstring.get(entry.getKey());
						if (new File(path).isDirectory()) {
							Pair<Integer, Integer> counts = processDirectory(path, selectedItems);
							audioCount += counts.first;
							nonAudioCount += counts.second;
						} else {
							if (isAudioFile(path)) {
								selectedItems.add(path);
								audioCount++;
							} else {
								nonAudioCount++;
							}
						}
					}
				}
				return new Pair<>(selectedItems, new Pair<>(audioCount, nonAudioCount));
			}

			private Pair<Integer, Integer> processDirectory(String dirPath, ArrayList<String> audioFiles) {
				int audioCount = 0;
				int nonAudioCount = 0;
				File dir = new File(dirPath);
				File[] files = dir.listFiles();

				if (files != null) {
					for (File file : files) {
						if (file.isDirectory()) {
							Pair<Integer, Integer> subCounts = processDirectory(file.getAbsolutePath(), audioFiles);
							audioCount += subCounts.first;
							nonAudioCount += subCounts.second;
						} else {
							if (isAudioFile(file.getAbsolutePath())) {
								audioFiles.add(file.getAbsolutePath());
								audioCount++;
							} else {
								nonAudioCount++;
							}
						}
					}
				}
				return new Pair<>(audioCount, nonAudioCount);
			}

			@Override
			protected void onPostExecute(Pair<ArrayList<String>, Pair<Integer, Integer>> result) {
				loadingDialog.dismiss();
				ArrayList<String> audioFiles = result.first;
				int audioCount = result.second.first;
				int nonAudioCount = result.second.second;

				if (audioCount == 0) {
					Toast.makeText(FilemanagerActivity.this, "추출할 음악 파일이 없습니다", Toast.LENGTH_SHORT).show();
					return;
				}

				String outputDirPath = Environment.getExternalStorageDirectory().getPath() +
									 "/Pictures/OUI FileManager/Extracted Album";
				File outputDir = new File(outputDirPath);
				if (!outputDir.exists()) {
					outputDir.mkdirs();
				}

				if (nonAudioCount > 0) {
					new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(FilemanagerActivity.this)
						.setTitle("앨범아트 추출")
						.setMessage("음악 " + audioCount + "개의 앨범아트를 추출할까요?\n" +
								   "항목 " + nonAudioCount + "개는 추출할 수 없습니다.")
						.setPositiveButton("추출", (dialog, which) -> {
							new ExtractAlbumArtTask(audioFiles, outputDirPath).execute();
							})
						.setNegativeButton("취소", (dialog, which) -> {})
						.show();
					} else {
					new ExtractAlbumArtTask(audioFiles, outputDirPath).execute();
				}
			}
		}.execute();
	}

	private class ExtractAlbumArtTask extends AsyncTask<Void, Integer, Integer> {
	    private de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog;
	    private List<String> audioFiles;
	    private String outputDirPath;
	    private int successCount = 0;

	    public ExtractAlbumArtTask(List<String> audioFiles, String outputDirPath) {
			this.audioFiles = audioFiles;
			this.outputDirPath = outputDirPath;
		}

	    @Override
	    protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(FilemanagerActivity.this);
			progressDialog.setTitle("앨범아트 추출 중...");
			progressDialog.setMessage("0/" + audioFiles.size());
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(audioFiles.size());
			progressDialog.setCancelable(false);
			progressDialog.show();
			}

	    @Override
	    protected Integer doInBackground(Void... voids) {
			successCount = 0;

			for (int i = 0; i < audioFiles.size(); i++) {
				String currentFilePath = audioFiles.get(i);
				MediaMetadataRetriever retriever = null;
				FileOutputStream fos = null;

				try {
					retriever = new MediaMetadataRetriever();
					retriever.setDataSource(currentFilePath);

					byte[] artBytes = retriever.getEmbeddedPicture();
					if (artBytes != null && artBytes.length > 0) {
						String fileName = generateUniqueFileName(currentFilePath);
						File outputFile = new File(outputDirPath, fileName);

						fos = new FileOutputStream(outputFile);
						fos.write(artBytes);
						fos.flush();
						successCount++;
					}
				} catch (Exception e) {
					ExceptionLogger.log(e, "FileManagerActivity:ExtractAlbumArtTask");
					e.printStackTrace();
				} finally {
					try {
						if (retriever != null) {
							retriever.release();
						}
					} catch (IOException e) {
						ExceptionLogger.log(e, "FileManagerActivity:ExtractAlbumArtTask-IOException");
						e.printStackTrace();
					}

					try {
						if (fos != null) {
							fos.close();
						}
					} catch (IOException e) {
						ExceptionLogger.log(e, "FileManagerActivity:ExtractAlbumArtTask-IOException");
						e.printStackTrace();
					}

					publishProgress(i + 1);
				}
			}
			return successCount;
		}

	    private String generateUniqueFileName(String filePath) {
			String baseName = new File(filePath).getName()
							  .replaceFirst("[.][^.]+$", "")
							  .replaceAll("[\\\\/:*?\"<>|]", "_"); // 특수문자 처리
			String pattern = baseName + "(?:_(\\d+))?.png";
			Pattern r = Pattern.compile(pattern);

			int maxNum = 0;
			File[] existingFiles = new File(outputDirPath).listFiles((dir, name) -> name.matches(baseName + "(?:_\\d+)?.png"));

			if (existingFiles != null) {
				for (File file : existingFiles) {
					Matcher m = r.matcher(file.getName());
					if (m.find()) {
						String numStr = m.group(1);
						int currentNum = numStr != null ? Integer.parseInt(numStr) : 0;
						maxNum = Math.max(maxNum, currentNum);
					}
				}
			}

			return maxNum == 0 ? baseName + ".png" : baseName + "_" + (maxNum + 1) + ".png";
		}

	    @Override
	    protected void onProgressUpdate(Integer... values) {
			progressDialog.setProgress(values[0]);
			progressDialog.setMessage(values[0] + "/" + audioFiles.size());
		}

	    @Override
	    protected void onPostExecute(Integer result) {
			progressDialog.dismiss();
			String message = result == audioFiles.size() ?
				"모든 앨범아트를 성공적으로 추출했습니다" :
				result > 0 ?
					result + "개의 앨범아트 추출 성공 (" + (audioFiles.size() - result) + "개 실패)" :
					"앨범아트 추출에 실패했습니다";

			Toast.makeText(FilemanagerActivity.this, message, Toast.LENGTH_SHORT).show();
			setSelecting(false);
			backPressed = true;
			_refresh();
		}
	}
	@SuppressLint("StaticFieldLeak")
    private void make() {
	    de.dlyt.yanndroid.oneui.dialog.ProgressDialog loadingDialog =
	        new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(this);
	    loadingDialog.setMessage("이미지 파일 확인 중...");
	    loadingDialog.setCancelable(false);
	    loadingDialog.show();
		new AsyncTask<Void, Void, Pair<ArrayList<String>, Integer>>() {
			@Override
			protected Pair<ArrayList<String>, Integer> doInBackground(Void... voids) {
				ArrayList<String> imageFiles = new ArrayList<>();
				int nonImageCount = 0;

				for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
					if (entry.getValue() && entry.getKey() < listinstring.size()) {
						String path = listinstring.get(entry.getKey());
						File file = new File(path);

						if (file.isDirectory()) {
							Pair<Integer, Integer> counts = processDirectory(path, imageFiles);
							nonImageCount += counts.second;
						} else {
							if (isImageFile(path)) {
								imageFiles.add(path);
							} else {
								nonImageCount++;
							}
						}
					}
				}
				return new Pair<>(imageFiles, nonImageCount);
			}

			private Pair<Integer, Integer> processDirectory(String dirPath, ArrayList<String> imageFiles) {
				int imageCount = 0;
				int nonImageCount = 0;
				File dir = new File(dirPath);
				File[] files = dir.listFiles();

				if (files != null) {
					for (File file : files) {
						if (file.isDirectory()) {
							Pair<Integer, Integer> subCounts = processDirectory(file.getAbsolutePath(), imageFiles);
							imageCount += subCounts.first;
							nonImageCount += subCounts.second;
						} else {
							if (isImageFile(file.getAbsolutePath())) {
								imageFiles.add(file.getAbsolutePath());
								imageCount++;
							} else {
								nonImageCount++;
							}
						}
					}
				}
				return new Pair<>(imageCount, nonImageCount);
			}

			@Override
			protected void onPostExecute(Pair<ArrayList<String>, Integer> result) {
				loadingDialog.dismiss();
				ArrayList<String> imageFiles = result.first;
				int nonImageCount = result.second;

				if (imageFiles.isEmpty()) {
					Toast.makeText(FilemanagerActivity.this, "처리할 수 있는 이미지 파일이 없습니다.", Toast.LENGTH_SHORT).show();
					return;
				}

				if (nonImageCount > 0) {
					new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(FilemanagerActivity.this)
					.setTitle("경고")
					.setMessage("선택한 항목 중 이미지가 아닌 파일이 " + nonImageCount + "개 포함되어 있습니다.\n제외하고 계속할까요?")
					.setPositiveButton("계속", (dialog, which) -> {
							runMakeDialog(imageFiles);
							})
					.setNegativeButton("취소", (dialog, which) -> {})
					.show();
					} else {
					runMakeDialog(imageFiles);
				}
			}

			private void runMakeDialog(ArrayList<String> imageFiles) {
				if (imageFiles.size() == 1) {
					showSingleImageDialog(listinstring.indexOf(imageFiles.get(0)));
				} else {
					// 여러 이미지가 선택되었을 때의 새로운 로직
					List<Integer> indices = new ArrayList<>();
					for (String path : imageFiles) {
						int index = listinstring.indexOf(path);
						if (index != -1) indices.add(index);
					}

					new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(FilemanagerActivity.this)
							.setTitle("작업 선택")
							.setMessage("여러 이미지를 분할하거나 병합할 수 있습니다.")
							.setPositiveButton("분할", (dialog, which) -> {
								// 새로 추가된 '여러 이미지 분할' 다이얼로그 호출
								showMultiImageSplitDialog(imageFiles);
							})
							.setNegativeButton("병합", (dialog, which) -> {
								// 기존의 '이미지 병합' 다이얼로그 호출
								showMultiImageDialog(indices);
							})
							.setNeutralButton("취소", (dialog, which) -> {})
							.show();
					}
			}
		}.execute();
	}

	// 단일 이미지 처리 다이얼로그
	private void showSingleImageDialog(int selectedIndex) {
		View dialogView = getLayoutInflater().inflate(R.layout.dialog_split_image, null);
		de.dlyt.yanndroid.oneui.view.OptionGroup directionGroup = dialogView.findViewById(R.id.direction_group);
		EditText splitInputEdit = dialogView.findViewById(R.id.split_input);
		CheckBox keepRatioCheckbox = dialogView.findViewById(R.id.keep_ratio_checkbox);

		// EditText 입력 리스너 추가
		splitInputEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				boolean isRatioMode = s.toString().contains(":");
				keepRatioCheckbox.setVisibility(isRatioMode ? View.VISIBLE : View.GONE);
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});

		new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this)
				.setTitle("이미지 분할 설정")
				.setView(dialogView)
				.setPositiveButton("확인", (dialog, which) -> {
					try {
						int direction = directionGroup.getSelectedOptionButton().getId() == R.id.horizontal_split ? 0 : 1;
						String imagePath = listinstring.get(selectedIndex);
						String inputText = splitInputEdit.getText().toString();

						if (inputText.contains(":")) {
							// 비율 분할 모드
							String[] ratioParts = inputText.split(":");
							if (ratioParts.length != 2) {
								Toast.makeText(this, "비율을 n:n 형식으로 입력해주세요.", Toast.LENGTH_SHORT).show();
								return;
							}
							int ratio1 = Integer.parseInt(ratioParts[0].trim());
							int ratio2 = Integer.parseInt(ratioParts[1].trim());
							if (ratio1 <= 0 || ratio2 <= 0) {
								Toast.makeText(this, "비율은 양수로 입력해주세요.", Toast.LENGTH_SHORT).show();
								return;
							}
							boolean keepRatio = keepRatioCheckbox.isChecked();
							new SplitImageByRatioTask(imagePath, direction, ratio1, ratio2, keepRatio).execute();
						} else {
							// 개수 분할 모드
							int splitCount = Integer.parseInt(inputText);
							if (splitCount < 2 || splitCount > 10) {
								Toast.makeText(this, "분할 수는 2~10 사이로 입력해주세요.", Toast.LENGTH_SHORT).show();
								return;
							}
							new SplitImageTask(imagePath, direction, splitCount).execute();
						}
					} catch (NumberFormatException e) {
						ExceptionLogger.log(e, "FileManagerActivity:showSingleImageDialog-NumberFormatException");
						Toast.makeText(this, "입력값을 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
						}
				})
				.setNegativeButton("취소", (dialog, which) -> {})
				.show();
		}

	// 다중 이미지 처리 다이얼로그
	private void showMultiImageDialog(List<Integer> selectedIndices) {
	    View dialogView = getLayoutInflater().inflate(R.layout.dialog_merge_image, null);
	    de.dlyt.yanndroid.oneui.view.OptionGroup directionGroup = dialogView.findViewById(R.id.direction_group);
	    EditText wrapCountEdit = dialogView.findViewById(R.id.wrap_count);
	    CheckBox centerAlignCheck = dialogView.findViewById(R.id.center_align_check);
	    CheckBox transparentBgCheck = dialogView.findViewById(R.id.transparent_bg_check);

	    new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this)
	        .setTitle("이미지 병합 설정")
	        .setView(dialogView)
	        .setPositiveButton("다음", (dialog, which) -> {
				try {
					int direction = directionGroup.getSelectedOptionButton().getId() == R.id.horizontal_merge ? 0 : 1;
					int wrapCount = Integer.parseInt(wrapCountEdit.getText().toString());
					boolean centerAlign = centerAlignCheck.isChecked();
					boolean transparentBg = transparentBgCheck.isChecked();

					showSaveDialog(selectedIndices, direction, wrapCount, centerAlign, transparentBg);
				} catch (NumberFormatException e) {
					ExceptionLogger.log(e, "FileManagerActivity:showMultiImageDialog-NumberFormatException");
					Toast.makeText(this, "줄 바꿈 수를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
				}
			})
	        .setNegativeButton("취소", (dialog, which) -> {})
	        .show();
		}

	private void showMultiImageSplitDialog(ArrayList<String> imageFiles) {
		View dialogView = getLayoutInflater().inflate(R.layout.dialog_split_image, null);
		de.dlyt.yanndroid.oneui.view.OptionGroup directionGroup = dialogView.findViewById(R.id.direction_group);
		EditText splitInputEdit = dialogView.findViewById(R.id.split_input);
		CheckBox keepRatioCheckbox = dialogView.findViewById(R.id.keep_ratio_checkbox);

		splitInputEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				boolean isRatioMode = s.toString().contains(":");
				keepRatioCheckbox.setVisibility(isRatioMode ? View.VISIBLE : View.GONE);
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});

		new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this)
				.setTitle("여러 이미지 분할 설정")
				.setView(dialogView)
				.setPositiveButton("확인", (dialog, which) -> {
					try {
						int direction = directionGroup.getSelectedOptionButton().getId() == R.id.horizontal_split ? 0 : 1;
						String inputText = splitInputEdit.getText().toString();

						if (inputText.contains(":")) {
							String[] ratioParts = inputText.split(":");
							if (ratioParts.length != 2) {
								Toast.makeText(this, "비율을 n:n 형식으로 입력해주세요.", Toast.LENGTH_SHORT).show();
								return;
							}
							int ratio1 = Integer.parseInt(ratioParts[0].trim());
							int ratio2 = Integer.parseInt(ratioParts[1].trim());
							if (ratio1 <= 0 || ratio2 <= 0) {
								Toast.makeText(this, "비율은 양수로 입력해주세요.", Toast.LENGTH_SHORT).show();
								return;
							}
							boolean keepRatio = keepRatioCheckbox.isChecked();
							// 여러 이미지 분할을 처리할 새로운 AsyncTask 실행
							new MultiSplitImageTask(imageFiles, direction, inputText, keepRatio).execute();
						} else {
							int splitCount = Integer.parseInt(inputText);
							if (splitCount < 2 || splitCount > 10) {
								Toast.makeText(this, "분할 수는 2~10 사이로 입력해주세요.", Toast.LENGTH_SHORT).show();
								return;
							}
							// 여러 이미지 분할을 처리할 새로운 AsyncTask 실행
							new MultiSplitImageTask(imageFiles, direction, inputText, false).execute();
						}
					} catch (NumberFormatException e) {
						ExceptionLogger.log(e, "FileManagerActivity:showMultiImageSplitDialog-NumberFormatException");
						Toast.makeText(this, "입력값을 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
						}
				})
				.setNegativeButton("취소", (dialog, which) -> {})
				.show();
		}

	// 저장 다이얼로그 표시
	private void showSaveDialog(List<Integer> selectedIndices, int direction, int wrapCount, boolean centerAlign, boolean transparentBg) {
	    View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_image, null);
	    EditText fileNameEdit = dialogView.findViewById(R.id.file_name);

	    de.dlyt.yanndroid.oneui.dialog.AlertDialog saveDialog = new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this)
	        .setTitle("저장할 이름 입력")
	        .setView(dialogView)
	        .setPositiveButton("저장", null)
	        .setNegativeButton("취소", null)
	        .create();

	    saveDialog.setOnShowListener(dialog -> {
			Button positiveButton = saveDialog.getButton(DialogInterface.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(v -> {
				String fileName = fileNameEdit.getText().toString().trim();

				if (fileName.isEmpty()) {
					Toast.makeText(this, "파일 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
					return;
				}

				if (!isValidFileName(fileName, false)) {
					fileNameEdit.getBackground().setColorFilter(getResources().getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red), PorterDuff.Mode.SRC_IN);
					Toast.makeText(this, "유효하지 않은 파일 이름입니다.", Toast.LENGTH_SHORT).show();
					return;
				}

				if (isDuplicateFileName(fileName)) {
					fileNameEdit.getBackground().setColorFilter(getResources().getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red), PorterDuff.Mode.SRC_IN);
					Toast.makeText(this, "같은 이름의 파일이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
					return;
				}

				List<String> imagePaths = new ArrayList<>();
				for (int index : selectedIndices) {
					imagePaths.add(listinstring.get(index));
				}

				new MergeImageTask(imagePaths, direction, wrapCount, centerAlign, transparentBg, fileName).execute();
				saveDialog.dismiss();
				});
		});

	    fileNameEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				String name = s.toString().trim();
				if (isValidFileName(name, false) && !isDuplicateFileName(name)) {
					fileNameEdit.getBackground().clearColorFilter();
				} else {
					fileNameEdit.getBackground().setColorFilter(getResources().getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red), PorterDuff.Mode.SRC_IN);
				}
			}
		});

	    saveDialog.show();
		}

	private class SplitImageTask extends AsyncTask<Void, Integer, Boolean> {
	    private de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog;
	    private String imagePath;
	    private int direction;
	    private int splitCount;
	    private String errorMessage;

	    public SplitImageTask(String imagePath, int direction, int splitCount) {
			this.imagePath = imagePath;
			this.direction = direction;
			this.splitCount = splitCount;
		}

	    @Override
	    protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(FilemanagerActivity.this);
			progressDialog.setTitle("이미지 분할 중");
			progressDialog.setMessage(new File(imagePath).getName());
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(splitCount);
			progressDialog.show();
			}

	    @Override
	    protected Boolean doInBackground(Void... voids) {
			try {
				File outputDir = new File(Environment.getExternalStorageDirectory(), "Pictures/OUI FileManager/generated image");
				if (!outputDir.exists() && !outputDir.mkdirs()) {
					errorMessage = "결과 폴더를 생성할 수 없습니다.";
					return false;
				}

				return ImageProcessor.splitImage(
					imagePath,
					direction,
					splitCount,
					outputDir.getAbsolutePath(),
                        current -> publishProgress(current)
                );
			} catch (Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:SplitImageTask");
				errorMessage = e.getMessage();
				return false;
			}
		}

	    @Override
	    protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressDialog.setProgress(values[0]);
		}

	    @Override
	    protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			progressDialog.dismiss();

			if (success) {
				Toast.makeText(FilemanagerActivity.this, "이미지 분할이 완료되었습니다.", Toast.LENGTH_SHORT).show();
				setSelecting(false);
				backPressed = true;
				_refresh();
			} else {
				Toast.makeText(FilemanagerActivity.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class SplitImageByRatioTask extends AsyncTask<Void, Integer, Boolean> {
		private de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog;
		private String imagePath;
		private int direction;
		private int ratio1;
		private int ratio2;
		private String errorMessage;
		private boolean keepRatio;
		private int totalParts; // 추가: 전체 분할될 이미지 수를 저장할 변수

		public SplitImageByRatioTask(String imagePath, int direction, int ratio1, int ratio2, boolean keepRatio) {
			this.imagePath = imagePath;
			this.direction = direction;
			this.ratio1 = ratio1;
			this.ratio2 = ratio2;
			this.keepRatio = keepRatio;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			// 1. 여기서 전체 분할될 이미지 수를 미리 계산합니다.
			// 이 부분은 UI 스레드에서 이미지 파일을 읽기 때문에 주의가 필요하지만,
			// 단순히 메타데이터를 읽는 것이므로 일반적으로 안전합니다.
			// 만약 이미지 파일 로딩이 오래 걸릴 가능성이 있다면 doInBackground로 옮길 수 있습니다.
			totalParts = ImageProcessor.getExpectedSplitCountByRatio(imagePath, direction, ratio1, ratio2);
			if (totalParts <= 0) {
				// 예상 분할 수가 없으면 (예: 이미지 로드 실패) 기본값으로 설정하거나 오류 처리
				totalParts = 1; // 최소 1로 설정하여 ProgressDialog가 멈추지 않도록 합니다.
				errorMessage = "이미지 분석에 실패했거나 분할할 수 없습니다.";
			}


			progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(FilemanagerActivity.this);
			progressDialog.setTitle("이미지 분할 중");
			progressDialog.setMessage(new File(imagePath).getName());
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(totalParts); // 2. 계산된 전체 분할 수로 Max 설정
			progressDialog.show();
			}

		@Override
		protected Boolean doInBackground(Void... voids) {
			if (totalParts <= 0 && errorMessage != null) {
				return false; // onPreExecute에서 이미 오류가 감지된 경우
			}

			try {
				File outputDir = new File(Environment.getExternalStorageDirectory(), "Pictures/OUI FileManager/generated image");
				if (!outputDir.exists() && !outputDir.mkdirs()) {
					errorMessage = "결과 폴더를 생성할 수 없습니다.";
					return false;
				}

				return ImageProcessor.splitImageByRatio(
						imagePath,
						direction,
						ratio1,
						ratio2,
						keepRatio,
						outputDir.getAbsolutePath(),
						// 3. ImageProcessor.java의 SplitCallback에서 전달되는 current 값은 0부터 시작하는 인덱스가 아니라
						// 이미 저장된 이미지의 개수 (1부터 시작)를 나타내므로, publishProgress에 그대로 넘겨도 됩니다.
                        this::publishProgress
				);
			} catch (Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:SplitImageByRatioTask");
				errorMessage = e.getMessage();
				return false;
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			// values[0]은 현재 저장된 이미지의 개수 (1부터 시작)
			progressDialog.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			if (progressDialog != null && progressDialog.isShowing()) { // 다이얼로그가 표시 중인 경우에만 닫기
				progressDialog.dismiss();
				}


			if (success) {
				Toast.makeText(FilemanagerActivity.this, "이미지 분할이 완료되었습니다.", Toast.LENGTH_SHORT).show();
				setSelecting(false);
				backPressed = true;
				_refresh();
			} else {
				Toast.makeText(FilemanagerActivity.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class MergeImageTask extends AsyncTask<Void, Integer, Boolean> {
	    private de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog;
	    private List<String> imagePaths;
	    private int direction;
	    private int wrapCount;
	    private boolean centerAlign;
	    private boolean transparentBg;
	    private String fileName;
	    private String errorMessage;

	    public MergeImageTask(List<String> imagePaths, int direction, int wrapCount,
						 boolean centerAlign, boolean transparentBg, String fileName) {
			this.imagePaths = imagePaths;
			this.direction = direction;
			this.wrapCount = wrapCount;
			this.centerAlign = centerAlign;
			this.transparentBg = transparentBg;
			this.fileName = fileName;
		}

	    @Override
	    protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(FilemanagerActivity.this);
			progressDialog.setTitle("이미지 병합 중");
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(imagePaths.size());
			progressDialog.show();
			}

	    @Override
	    protected Boolean doInBackground(Void... voids) {
			try {
				return ImageProcessor.mergeImages(
					imagePaths,
					direction,
					wrapCount,
					centerAlign,
					transparentBg,
					new File(Environment.getExternalStorageDirectory(), "Pictures/OUI FileManager/generated image/" + fileName + ".png").getAbsolutePath(),
                        this::publishProgress
                );
			} catch (Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:MergeImageTask");
				errorMessage = e.getMessage();
				return false;
			}
		}

	    @Override
	    protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			int index = values[0];
			progressDialog.setProgress(index);
			if (index >= 0 && index < imagePaths.size()) {
				progressDialog.setMessage(new File(imagePaths.get(index)).getName());
			}
		}

	    @Override
	    protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			progressDialog.dismiss();

			if (success) {
				Toast.makeText(FilemanagerActivity.this, "이미지 병합이 완료되었습니다.", Toast.LENGTH_SHORT).show();
				setSelecting(false);
				backPressed = true;
				_refresh();
			} else {
				Toast.makeText(FilemanagerActivity.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class MultiSplitImageTask extends AsyncTask<Void, String, Boolean> {
		private de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog;
		private ArrayList<String> imageFiles;
		private int direction;
		private String splitInput; // "5" 또는 "1:1" 같은 입력값
		private boolean keepRatioForRatioSplit;
		private String errorMessage;
		private int totalSuccessCount = 0;

		public MultiSplitImageTask(ArrayList<String> imageFiles, int direction, String splitInput, boolean keepRatioForRatioSplit) {
			this.imageFiles = imageFiles;
			this.direction = direction;
			this.splitInput = splitInput;
			this.keepRatioForRatioSplit = keepRatioForRatioSplit;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(FilemanagerActivity.this);
			progressDialog.setTitle("이미지 분할 중");
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(imageFiles.size());
			progressDialog.show();
			}

		@Override
		protected Boolean doInBackground(Void... voids) {
			File outputDir = new File(Environment.getExternalStorageDirectory(), "Pictures/OUI FileManager/generated image");
			if (!outputDir.exists() && !outputDir.mkdirs()) {
				errorMessage = "결과 폴더를 생성할 수 없습니다.";
				return false;
			}

			for (int i = 0; i < imageFiles.size(); i++) {
				String imagePath = imageFiles.get(i);
				publishProgress(String.valueOf(i), new File(imagePath).getName());

				boolean success;
				try {
					if (splitInput.contains(":")) {
						// 비율로 분할
						String[] ratioParts = splitInput.split(":");
						int ratio1 = Integer.parseInt(ratioParts[0].trim());
						int ratio2 = Integer.parseInt(ratioParts[1].trim());
						success = ImageProcessor.splitImageByRatio(imagePath, direction, ratio1, ratio2, keepRatioForRatioSplit, outputDir.getAbsolutePath(), null);
					} else {
						// 개수로 분할
						int splitCount = Integer.parseInt(splitInput);
						success = ImageProcessor.splitImage(imagePath, direction, splitCount, outputDir.getAbsolutePath(), null);
					}

					if (success) {
						totalSuccessCount++;
					}

				} catch (Exception e) {
					ExceptionLogger.log(e, "FileManagerActivity:MultiSplitImageTask");
					// 하나가 실패해도 다음 이미지로 계속 진행
				}
			}

			if (totalSuccessCount == 0 && !imageFiles.isEmpty()) {
				errorMessage = "모든 이미지 분할에 실패했습니다.";
				return false;
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			int progress = Integer.parseInt(values[0]);
			String fileName = values[1];
			progressDialog.setProgress(progress);
			progressDialog.setMessage("(" + (progress + 1) + "/" + imageFiles.size() + ")\n" + fileName);
		}

		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			progressDialog.dismiss();

			if (success) {
				String message = totalSuccessCount + "개의 이미지 분할을 완료했습니다.";
				if (totalSuccessCount < imageFiles.size()) {
					message += " (" + (imageFiles.size() - totalSuccessCount) + "개 실패)";
				}
				Toast.makeText(FilemanagerActivity.this, message, Toast.LENGTH_SHORT).show();
				setSelecting(false);
				backPressed = true;
				_refresh();
			} else {
				Toast.makeText(FilemanagerActivity.this, "오류: " + errorMessage, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private boolean isDuplicateFileName(String fileName) {
	    File outputDir = new File(Environment.getExternalStorageDirectory(), "Pictures/OUI FileManager/generated image");
	    File file = new File(outputDir, fileName + ".png");
	    return file.exists();
	}

    private void showFullPlayer() {
		Intent intent_player = new Intent(FilemanagerActivity.this, FullPlayerActivity.class);
		startActivity(intent_player);
		Toast.makeText(this, "show", Toast.LENGTH_SHORT).show();
	}

    // MusicService와의 연결을 관리하는 ServiceConnection
    private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
			musicService = binder.getService();
			isServiceBound = true;
			mini_player_container = findViewById(R.id.mini_player_container);

			// 리스너 강력하게 재등록
			registerMusicServiceListener();

			// 초기 상태 강제 업데이트
			forceUpdatePlayerUI();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
				isServiceBound = false;
			}
	};

    // 음악 서비스 리스너 등록 메서드 분리
    private void registerMusicServiceListener() {
		if (!isServiceBound) return;

		musicService.setListener(new MusicService.MusicServiceListener() {
			@Override
			public void onMusicStateChanged(boolean isPlaying) {
				runOnUiThread(() -> {
					updatePlayPauseButton(isPlaying);
					// 미니 플레이어 가시성 관리
					mini_player_container.setVisibility(View.VISIBLE);
				});
			}

			@Override
			public void onMusicInfoChanged(String title, String artist, String albumArt) {
				runOnUiThread(() -> {
					updateSongTitle(title);
					updateAlbumArt(albumArt);
					// 미니 플레이어 강제 표시 (새 곡 재생 시)
					mini_player_container.setVisibility(View.VISIBLE);
				});
			}

			@Override
			public void onProgressUpdated(int currentPosition) {
				// 필요 시 구현
			}
		});
	}

    // UI 강제 업데이트 메서드 추가
    private void forceUpdatePlayerUI() {
		if (!isServiceBound) return;

		runOnUiThread(() -> {
			updatePlayPauseButton(musicService.isPlaying());
			updateSongTitle(musicService.getCurrentTitle());
			updateAlbumArt(musicService.getCurrentAlbumArt());
		});
	}

    // 곡 재생 메서드 수정
    private void playMusicFromPath(String filePath) {
		try {
			if (isServiceBound) {
				musicService.playMusic(filePath);

				// 안전한 핸들러 호출
				new Handler(Looper.getMainLooper()).postDelayed(() -> {
					if (!isFinishing() && !isDestroyed()) {
						forceUpdatePlayerUI();
					}
				}, 100);
			}
		} catch (Exception e) {
			ExceptionLogger.log(e, "FileManagerActivity:playMusicFromPath");
			forceUpdatePlayerUI(); // 실패 시 즉시 업데이트 시도
		}
	}

    // 액티비티 생명주기 관리 강화
    @Override
    protected void onResume() {
		super.onResume();
		if (isServiceBound) {
			forceUpdatePlayerUI();
		}
	}

    @Override
    protected void onDestroy() {
		super.onDestroy();
		// 서비스 언바인딩
		if (isServiceBound) {
			unbindService(serviceConnection);
			isServiceBound = false;
		}
	}

    private void updatePlayPauseButton(boolean isPlaying) {
		if (isPlaying != musicService.isPlaying()) {
			Toast.makeText(this, "동기화 상태 지연이 감지되었습니다", Toast.LENGTH_SHORT).show();
			isPlaying = musicService.isPlaying();
		}

		if (isPlaying) {
			mini_play_pause.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_pause); // 재생 중일 때 아이콘
			mini_player_container.setVisibility(View.VISIBLE);
		} else {
			mini_play_pause.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_play); // 정지 상태일 때 아이콘
		}
	}

    // 곡 제목 업데이트
    private void updateSongTitle(String title) {
	        mini_song_title.setText(title);
	    }

    // 앨범 아트 업데이트
    private void updateAlbumArt(String albumArt) {
		if (albumArt != null && !albumArt.isEmpty()) {
			// Base64로 인코딩된 문자열을 비트맵으로 변환
			Bitmap bitmap = getAlbumArtFromBase64(albumArt);
			if (bitmap != null) {
				mini_album_art.setImageBitmap(bitmap); // ImageView에 비트맵 설정
			} else {
				mini_album_art.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_file_type_audio); // 기본 앨범 아트
			}
		} else {
			mini_album_art.setImageResource(de.dlyt.yanndroid.oneui.R.drawable.ic_samsung_file_type_audio); // 기본 앨범 아트
		}
	}

    // Base64로 인코딩된 문자열을 비트맵으로 변환
    private Bitmap getAlbumArtFromBase64(String base64String) {
		try {
			byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
			return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
		} catch (Exception e) {
			ExceptionLogger.log(e, "FileManagerActivity:getAlbumArtFromBase64");
			Log.e("MainActivity", "Error decoding album art", e);
			return null;
		}
	}
	// FTP 연결 상태 확인 및 파일 목록 가져오기
	private class FetchFTPFilesTask extends AsyncTask<Void, Void, Boolean> {
	    private de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog;

	    @Override
	    protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(FilemanagerActivity.this);
			progressDialog.setMessage("FTP 서버에 연결 중...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			}

	    @Override
	    protected Boolean doInBackground(Void... voids) {
			try {
				ftpHelper = new FTPHelper();
				if (ftpHelper.connect(server, port, user, password)) {
					ftpFiles = ftpHelper.getFileList();
					currentPath = ftpHelper.getCurrentDirectory();
					return true;
				}
			} catch (Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:FetchFTPFilesTask");
				e.printStackTrace();
			}
			return false;
		}

	    @Override
	    protected void onPostExecute(Boolean success) {
			progressDialog.dismiss();
			if (success) {
				mode = 1;
				_refreshFTPList();
				_ftp_connect.setVisibility(View.GONE);
				_ftp_folder.setVisibility(View.VISIBLE);
				_ftp_disconnect.setVisibility(View.VISIBLE);
				Toast.makeText(FilemanagerActivity.this, "FTP 연결 성공", Toast.LENGTH_SHORT).show();
				drawerLayout.setDrawerOpen(false, true);
			} else {
				Toast.makeText(FilemanagerActivity.this, "FTP 연결 실패", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void _refreshFTPList() {
	    list.clear();
	    listinstring.clear();
	    list_name.clear();
	    selected.clear();

	    // 상위 폴더 항목 제거 (뒤로가기 버튼으로만 이동 가능)
	    if (ftpFiles != null) {
			for (int i = 0; i < ftpFiles.size(); i++) {
				FTPFile file = ftpFiles.get(i);
				if (file != null && file.getName() != null && !file.getName().isEmpty()) {
					addFileItem(file, i);
				}
			}
		}

	    HashMap<String, Object> _item = new HashMap<>();
	    _item.put("file", "-1");
	    list.add(_item);
	    selected.put(list.size() - 1, false);

	    runOnUiThread(() -> {
			// FTP 서버 주소 형식으로 경로 표시 (예: "ftp://example.com:21/current/path")
			String displayPath = "ftp://" + ftpHelper.getServerAddress() + currentPath;
			path.setText(displayPath);

			imageAdapter.notifyDataSetChanged();
			listView.scrollToPosition(0);
			indexScrollView.setVisibility(View.GONE);
			if (listView != null) {
				listView.seslSetFastScrollerEnabled(true);
			}
			_refresh_title();
		});
	}

	private void addFileItem(FTPFile file, int position) {
	    String filePath = currentPath + (currentPath.endsWith("/") ? "" : "/") + file.getName();
	    int actualPosition = list.size(); // 현재 리스트 크기를 기준으로 위치 계산

	    HashMap<String, Object> item = new HashMap<>();
	    item.put("file", filePath);
	    list.add(item);
	    listinstring.add(filePath);
	    list_name.add(file.getName());
	    selected.put(actualPosition, false);
	}

	private File getFtpDownloadDirectory() {
	    // 외부 저장소 기본 경로 (내장 저장공간)
	    File baseDir = Environment.getExternalStorageDirectory();

	    // 원하는 서브 디렉토리 생성
	    File downloadDir = new File(baseDir, "Download/OUI FileManager/FTP Download");
	    if (!downloadDir.exists()) {
			downloadDir.mkdirs();
		}
	    return downloadDir;
	}

	private void downloadFTPFile(FTPFile ftpFile, boolean open, Runnable onComplete) {
		// UI 요소는 runOnUiThread에서 생성
		File downloadDir = getFtpDownloadDirectory();
		String remoteFileName = ftpFile.getName();
		File localFile = new File(downloadDir, remoteFileName);

		// ProgressDialog 참조를 위한 배열 (runOnUiThread 내에서 접근하기 위함)
		final de.dlyt.yanndroid.oneui.dialog.ProgressDialog[] progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog[1];

		runOnUiThread(() -> {
			progressDialog[0] = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(this);
			progressDialog[0].setTitle(remoteFileName + " 다운로드 중");
			progressDialog[0].setMessage("준비 중...");
			progressDialog[0].setProgressStyle(de.dlyt.yanndroid.oneui.dialog.ProgressDialog.STYLE_HORIZONTAL);
			progressDialog[0].setCancelable(false);
			progressDialog[0].setMax(100);
			progressDialog[0].show();
			});

		final long[] lastUpdateTime = {System.currentTimeMillis()};
		final long[] lastBytesTransferred = {0};

		new Thread(() -> {
			try {
				String remotePath = currentPath + (currentPath.endsWith("/") ? "" : "/") + remoteFileName;

				// long fileSize = ftpFile.getSize(); // <--- 사용되지 않으므로 제거

				boolean success = ftpHelper.downloadFile(
						remotePath,
						localFile.getAbsolutePath(),
						(bytesTransferred, totalBytes) -> {
							long currentTime = System.currentTimeMillis();
							// 200ms 이상 경과했거나, 전체의 1% 이상 전송되었을 때만 UI 업데이트
							if (currentTime - lastUpdateTime[0] > 200 ||
									(bytesTransferred - lastBytesTransferred[0]) > totalBytes * 0.01) {

								double speed = (bytesTransferred - lastBytesTransferred[0]) /
										((currentTime - lastUpdateTime[0]) / 1000.0) / 1024;

								runOnUiThread(() -> {
									if (progressDialog[0] != null) {
										// int progress = (int) ((bytesTransferred * 100) / totalBytes);
										// 정수 오버플로우 방지를 위해 long으로 계산 후 형변환
										int progress = (int) ((bytesTransferred * 100L) / totalBytes);
										progressDialog[0].setProgress(progress);
										progressDialog[0].setMessage(
												String.format(Locale.KOREAN,
														"%d%% (%.1f KB/s)\n%s / %s",
														progress,
														speed,
														formatFileSize(bytesTransferred),
														formatFileSize(totalBytes))
										);
									}
								});

								lastUpdateTime[0] = currentTime;
								lastBytesTransferred[0] = bytesTransferred;
							}
						}
				);

				runOnUiThread(() -> {
					if (progressDialog[0] != null) progressDialog[0].dismiss();
					if (success) {
						Toast.makeText(FilemanagerActivity.this, "다운로드 완료", Toast.LENGTH_SHORT).show();
						if (open) openDownloadedFile(localFile, remoteFileName);
					} else {
						Toast.makeText(FilemanagerActivity.this,
								"다운로드 실패: " + ftpHelper.getLastReply(), Toast.LENGTH_SHORT).show();
					}
					if (onComplete != null) onComplete.run();
				});

			} catch (Exception e) {
				runOnUiThread(() -> {
					if (progressDialog[0] != null) progressDialog[0].dismiss();
					Toast.makeText(FilemanagerActivity.this,
							"오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
					Log.e("FTP_DOWNLOAD", "Download failed", e);
					ExceptionLogger.log(e, "FileManagerActivity:downloadFTPFile");
					if (localFile.exists()) localFile.delete();

					if (onComplete != null) onComplete.run();
				});
			}
		}).start();
	}

	private void openDownloadedFile(File file, String fileName) {
	    Intent intent = new Intent(Intent.ACTION_VIEW);
	    Uri fileUri = FileProvider.getUriForFile(this,
	        getPackageName() + ".provider", file);

	    // MIME 타입 결정
	    String mimeType = getMimeType(fileName);
	    if (mimeType == null) {
			mimeType = "*/*";
		}

	    intent.setDataAndType(fileUri, mimeType);
	    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

	    try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			ExceptionLogger.log(e, "FileManagerActivity:openDownloadedFile-ActivityNotFoundException");
			Toast.makeText(this,
				"이 파일을 열 수 있는 앱이 없습니다", Toast.LENGTH_SHORT).show();
		}
	}

	private void downloadFTPFilesSequentially(List<FTPFile> files) {
		if (files == null || files.isEmpty()) return;

		Iterator<FTPFile> iterator = files.iterator();

		// 재귀적으로 순차 다운로드
		downloadNext(iterator);
	}

	private void downloadNext(Iterator<FTPFile> iterator) {
		if (!iterator.hasNext()) return;

		FTPFile currentFile = iterator.next();

		// downloadFTPFile 완료 후 다음 파일을 이어서 다운로드하게 하기 위해 콜백 추가
		downloadFTPFile(currentFile, false, () -> downloadNext(iterator));
	}

	// FTP 디렉토리 변경을 위한 AsyncTask
	private class ChangeDirectoryTask extends AsyncTask<Void, Void, Boolean> {
	    private de.dlyt.yanndroid.oneui.dialog.ProgressDialog progressDialog;
	    private String targetDir;

	    public ChangeDirectoryTask(String targetDir) {
		        this.targetDir = targetDir;
		    }

	    @Override
	    protected void onPreExecute() {
			progressDialog = new de.dlyt.yanndroid.oneui.dialog.ProgressDialog(FilemanagerActivity.this);
			progressDialog.setMessage(targetDir.equals("..") ? "상위 폴더로 이동 중..." : "폴더 이동 중...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			}

	    @Override
	    protected Boolean doInBackground(Void... voids) {
			try {
				if (!ftpHelper.isConnected()) {
					if (!ftpHelper.connect(server, port, user, password)) {
						return false;
					}
				}

				boolean success;
				if (targetDir.equals("..")) {
					success = ftpHelper.goToParentDirectory();
				} else {
					success = ftpHelper.changeDirectory(targetDir);
				}

				if (success) {
					currentPath = ftpHelper.getCurrentDirectory();
					ftpFiles = ftpHelper.getFileList();
					return true;
				}
			} catch (Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:ChangeDirectoryTask");
				Log.e("FTP", "Error changing directory: " + e.getMessage());
			}
			return false;
		}

	    @Override
	    protected void onPostExecute(Boolean success) {
			progressDialog.dismiss();
			if (success) {
				_refreshFTPList();
				swipe_refresh.setRefreshing(false);
			} else {
				Toast.makeText(FilemanagerActivity.this, "폴더 이동 실패", Toast.LENGTH_SHORT).show();
				swipe_refresh.setRefreshing(false);
				de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder dialog = new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(FilemanagerActivity.this);
				dialog.setMessage("폴더 이동에 실패 했습니다. FTP 서버에 다시 연결 할까요?");
				dialog.setCancelable(false);

				// 확인 버튼 클릭 리스너
				dialog.setPositiveButton("확인", (dialog1, which) -> {
                    // ftp_connect_dialog() 실행
                    ftp_connect_dialog();
					});

				// 취소 버튼 클릭 리스너
				dialog.setNegativeButton("취소", (dialog2, which) -> {
					dialog2.dismiss();  // 취소 시 다이얼로그 닫기
					});

				dialog.show();
				}
		}
	}

    private String getFTPFileType(String fileName) {
		if (fileName.toLowerCase().endsWith(".txt")) return "txt";
		if (fileName.toLowerCase().endsWith(".hwp")) return "hwp";
		if (fileName.toLowerCase().endsWith(".html")) return "html";
		if (fileName.toLowerCase().endsWith(".pdf")) return "pdf";
		if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".webp") || fileName.toLowerCase().endsWith(".gif")) return "image";
		if (fileName.toLowerCase().endsWith(".mp3") || fileName.toLowerCase().endsWith(".wav") || fileName.toLowerCase().endsWith(".ogg") || fileName.toLowerCase().endsWith(".flac") || fileName.toLowerCase().endsWith(".mid")) return "audio";
		if (fileName.toLowerCase().endsWith(".amr") || fileName.toLowerCase().endsWith(".m4a")) return "amr";
		if (fileName.toLowerCase().endsWith(".mp4") || fileName.toLowerCase().endsWith(".avi") || fileName.toLowerCase().endsWith(".mkv") || fileName.toLowerCase().endsWith(".webm")) return "video";
		if (fileName.toLowerCase().endsWith(".zip") || fileName.toLowerCase().endsWith(".rar") || fileName.toLowerCase().endsWith(".7z")) return "zip";
		if (fileName.toLowerCase().endsWith(".dng") || fileName.toLowerCase().endsWith(".cr2") || fileName.toLowerCase().endsWith(".cr3")) return "raw";
		if (fileName.toLowerCase().endsWith(".ppt") || fileName.toLowerCase().endsWith(".pptx")) return "ppt";
		if (fileName.toLowerCase().endsWith(".apk")) return "apk";
		if (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".csv")) return "excel";
		if (fileName.toLowerCase().endsWith(".doc") || fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".docm")) return "word";
		return "etc";
	}

    // 위치에 해당하는 FTPFile 객체 가져오기
    private FTPFile getFTPFile(int position) {
		if (ftpFiles == null || position < 0 || position >= ftpFiles.size()) {
			return null;
		}
		return ftpFiles.get(position);
	}

    private void ftp_connect_dialog() {
		de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder builder = new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(FilemanagerActivity.this);
		builder.setTitle("FTP로 연결");

		// LayoutInflater로 커스텀 레이아웃 설정
		View dialogView = LayoutInflater.from(FilemanagerActivity.this).inflate(R.layout.dialog_layout, null);
		builder.setView(dialogView);

		// EditText 참조
		final EditText ftpEditText = dialogView.findViewById(R.id.ftpEditText);
		final EditText portEditText = dialogView.findViewById(R.id.portEditText);
		final EditText userEditText = dialogView.findViewById(R.id.userEditText);
		final EditText passwordEditText = dialogView.findViewById(R.id.passwordEditText);

		// 힌트 설정
		ftpEditText.setHint("FTP 주소");
		portEditText.setHint("포트");
		userEditText.setHint("사용자");
		passwordEditText.setHint("비밀번호");

		if (!server.isEmpty()) ftpEditText.setText(server);
		if (port != 0) portEditText.setText(String.valueOf(port));
		if (!user.isEmpty()) userEditText.setText(user);
		if (!password.isEmpty()) passwordEditText.setText(password);

		builder.setPositiveButton("연결", (dialog, which) -> {
            try {
				server = ftpEditText.getText().toString().trim();
                port = Integer.parseInt(portEditText.getText().toString().trim());
                user = userEditText.getText().toString().trim();
                password = passwordEditText.getText().toString().trim();

                if (server.isEmpty()) {
					Toast.makeText(FilemanagerActivity.this, "서버 주소를 입력하세요.", Toast.LENGTH_SHORT).show();
					return;
                }

                if ((!user.isEmpty() && password.isEmpty()) || (user.isEmpty() && !password.isEmpty())) {
                    Toast.makeText(FilemanagerActivity.this, "사용자 이름과 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
					return;
                }

                // 여기까지 오면 서버는 입력되어 있고, user-password는 둘 다 비었거나 둘 다 채워졌거나임
                // 계속 진행
            } catch (NumberFormatException e) {
				ExceptionLogger.log(e, "FileManagerActivity:ftp_connect_dialog-NumberFormatException");
                Toast.makeText(FilemanagerActivity.this, "유효한 포트 번호를 입력하세요.", Toast.LENGTH_SHORT).show();
				return;
            }

            ftpHelper = new FTPHelper();
            new FetchFTPFilesTask().execute();
        });

		builder.setNegativeButton("취소", (dialog, which) -> {
			dialog.dismiss();
			});

		builder.setOnCancelListener(dialog -> {});

		// 다이얼로그 표시
		builder.show();
		}

    private void ftp_disconnect_dialog() {
		de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder builder = new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(FilemanagerActivity.this);
		builder.setTitle("FTP 연결 해제");
		builder.setMessage("FTP 서버 연결을 해제 하시겠습니까?");
		builder.setPositiveButton("연결 해제", (dialog, which) -> {
			mode = 0;
			ftpHelper.disconnect();
			folder = FileUtil.getExternalStorageDir();
			backPressed = false;
			_refresh();
			_ftp_connect.setVisibility(View.VISIBLE);
			_ftp_folder.setVisibility(View.GONE);
			_ftp_disconnect.setVisibility(View.GONE);
			drawerLayout.setDrawerOpen(false, true);
		});

		builder.setNegativeButton("취소", (dialog, which) -> {
			dialog.dismiss();
			});

		builder.setPositiveButtonColor(getResources().getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red));

		builder.show();
	}

	private static final int PERMISSION_REQUEST_CODE = 1000;

	private void checkAndRequestPermissions() {
		// 1. 요청할 권한 목록을 담을 리스트 생성
		ArrayList<String> permissionsToRequest = new ArrayList<>();

		// 2. 마이크 권한 확인 (Visualizer에 필요)
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
				== PackageManager.PERMISSION_DENIED) {
			permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
		}

		// 3. 안드로이드 버전에 따른 저장공간 권한 확인
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			// Android 13 (API 33) 이상: READ_MEDIA_AUDIO
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
					== PackageManager.PERMISSION_DENIED) {
				permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO);
			}
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			// Android 10 (API 29) ~ 12 (API 32): READ_EXTERNAL_STORAGE
			// WRITE_EXTERNAL_STORAGE는 필요 없음
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_DENIED) {
				permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
			}
		} else {
			// Android 9 (API 28) 이하: READ + WRITE
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_DENIED) {
				permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
			}
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_DENIED) {
				permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			}
		}

		// 4. 요청할 권한이 있다면 요청, 없다면 로직 실행
		if (!permissionsToRequest.isEmpty()) {
			// 권한 목록을 String 배열로 변환하여 요청
			ActivityCompat.requestPermissions(this,
					permissionsToRequest.toArray(new String[0]),
					PERMISSION_REQUEST_CODE);
		} else {
			// 모든 권한이 이미 허용된 경우
			initializeLogic();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == PERMISSION_REQUEST_CODE) {
			boolean allGranted = true;
			for (int grantResult : grantResults) {
				if (grantResult == PackageManager.PERMISSION_DENIED) {
					allGranted = false;
					break;
				}
			}

			if (allGranted) {
				// 모든 권한이 허용되었을 때
				recreate();
			} else {
				// 하나라도 거부된 권한이 있을 때
				Toast.makeText(this, "기능을 사용하려면 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
				// (선택 사항) 사용자에게 다시 요청하거나 앱을 종료하는 등의 처리
			}
		}
	}

	private String getMimeTypeOpen(String fileName) {
	    String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
	    if (extension == null || extension.isEmpty()) {
			int dotIndex = fileName.lastIndexOf('.');
			if (dotIndex != -1) {
				extension = fileName.substring(dotIndex + 1).toLowerCase();
			}
		}

	    switch (extension) {
			case "txt": return "text/plain";
			case "pdf": return "application/pdf";
			case "doc":
			case "docx": return "application/msword";
			case "xls":
			case "xlsx": return "application/vnd.ms-excel";
			case "ppt":
			case "pptx": return "application/vnd.ms-powerpoint";
			case "zip": return "application/zip";
			case "rar": return "application/x-rar-compressed";
			case "mp3": return "audio/mpeg";
			case "mp4": return "video/mp4";
			case "jpg":
			case "jpeg": return "image/jpeg";
			case "png": return "image/png";
			// 필요에 따라 더 추가 가능
			default:
				return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
	}

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity_filemanager);
		initialize(_savedInstanceState);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> drawerLayout.onSearchModeVoiceInputResult(result));

		checkAndRequestPermissions();
	}

	private void initialize(Bundle _savedInstanceState) {
		swipe_refresh = findViewById(R.id.swipe_refresh);
		indexScrollView = findViewById(R.id.indexScrollView);
		path = findViewById(R.id.path);
	}

	@SuppressLint("CutPasteId")
    private void initializeLogic() {
		// MusicService 시작 및 바인딩
		Intent serviceIntent = new Intent(this, MusicService.class);
		startService(serviceIntent); // 서비스 시작
		bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE); // 서비스 바인딩
		mContext = this;

		drawerLayout = findViewById(R.id.drawer_view);
		drawerLayout.setDrawerButtonOnClickListener(v -> {
			startActivity(new Intent(this, AboutActivity.class));
			});
		drawerLayout.setDrawerButtonTooltip(getText(de.dlyt.yanndroid.oneui.R.string.app_info));
		drawerLayout.inflateToolbarMenu(R.menu.filemanager);
		drawerLayout.getToolbarMenu().findItem(R.id.search).setTitle("검색");
		drawerLayout.setOnToolbarMenuItemClickListener(item -> {
			int id = item.getItemId();

			if (id == R.id.search) {
				drawerLayout.showSearchMode();
			} else if (id == R.id.edit) {
				setSelecting(true);
				drawerLayout.dismissSelectMode();
			} else if (id == R.id.add_file) {
				addItem(true);
			} else if (id == R.id.add_folder) {
				addItem(false);
			} else if (id == R.id.sort) {
				_sort();
			} else if (id == R.id.vlc) {
				_vlc();
			} else if (id == R.id.setting) {
				startActivity(new Intent(this, SettingActivity.class));
				finish();
			}
			return true;
		});

		drawerLayout.setSearchModeListener(new ToolbarLayout.SearchModeListener() {
			@Override
			public void onKeyboardSearchClick(CharSequence s) {
				_Query(s.toString());
				}

			@Override
			public void onVoiceInputClick(Intent intent) {
					activityResultLauncher.launch(intent);
				}
		});

		LinearLayout _nav_view = findViewById(R.id.drawer_view);
        de.dlyt.yanndroid.oneui.widget.OptionButton _sc_appRoot = _nav_view.findViewById(R.id.sc_app_root);
        de.dlyt.yanndroid.oneui.widget.OptionButton _sc_appData = _nav_view.findViewById(R.id.sc_app_data);
		de.dlyt.yanndroid.oneui.widget.OptionButton _sc_app = _nav_view.findViewById(R.id.sc_app);
		de.dlyt.yanndroid.oneui.widget.OptionButton _sc_user = _nav_view.findViewById(R.id.sc_user);
        de.dlyt.yanndroid.oneui.widget.OptionButton _sc_device = _nav_view.findViewById(R.id.sc_device);
        de.dlyt.yanndroid.oneui.widget.OptionButton _sc_audio = _nav_view.findViewById(R.id.sc_audio);
        de.dlyt.yanndroid.oneui.widget.OptionButton _sc_document = _nav_view.findViewById(R.id.sc_document);
        de.dlyt.yanndroid.oneui.widget.OptionButton _sc_image = _nav_view.findViewById(R.id.sc_image);
        de.dlyt.yanndroid.oneui.widget.OptionButton _sc_video = _nav_view.findViewById(R.id.sc_video);
        de.dlyt.yanndroid.oneui.widget.OptionButton _sc_download = _nav_view.findViewById(R.id.sc_download);
		_ftp_connect = _nav_view.findViewById(R.id.ftp_connect);
		_ftp_folder = _nav_view.findViewById(R.id.ftp_folder);
		_ftp_disconnect = _nav_view.findViewById(R.id.ftp_disconnect);

		FrameLayout mini_player_container = findViewById(R.id.mini_player_container);
		mini_player = mini_player_container.findViewById(R.id.mini_player);
		mini_song_title = mini_player_container.findViewById(R.id.mini_song_title);
		mini_play_pause = mini_player_container.findViewById(R.id.mini_play_pause);
		mini_album_art = mini_player_container.findViewById(R.id.mini_album_art); // ImageView 초기화

		FrameLayout move_status = findViewById(R.id.move_status);
		move_imageview1 = move_status.findViewById(R.id.move_imageview1);
		move_size = move_status.findViewById(R.id.move_size);
		move_count = move_status.findViewById(R.id.move_count);
		move_path = move_status.findViewById(R.id.move_path);
		move_cancel = move_status.findViewById(R.id.move_cancel);
		move_move = move_status.findViewById(R.id.move_move);

		move_imageview1.setImageResource(getResources().getIdentifier("ic_samsung_file_type_folder", "drawable", getPackageName()));

		move_cancel.setOnClickListener(_view -> {
			move_status.setVisibility(View.GONE);
			fileSelect.clear();
        });

		move_move.setOnClickListener(_view -> {
			move_status.setVisibility(View.GONE);
			new MultiFileOperationTask(copy ? "copy" : "move").execute();
        });

		mini_player.setOnClickListener(_view -> showFullPlayer());

		mini_play_pause.setOnClickListener(v -> {
			if (isServiceBound) {
				musicService.togglePlayPause();
				updatePlayPauseButton(musicService.isPlaying());
			}
		});

		_sc_appRoot.setOnClickListener(v -> {
			mode = 0;
			folder = getApplicationContext().getDataDir().getAbsolutePath();
			backPressed = false;
			_refresh();
			drawerLayout.setDrawerOpen(false, true);
		});

		_sc_appData.setOnClickListener(v -> {
			mode = 0;
			folder = getApplicationContext().getExternalFilesDir(null).getParent();
			backPressed = false;
			_refresh();
			drawerLayout.setDrawerOpen(false, true);
		});

		_sc_app.setOnClickListener(v -> {
			mode = 0;
			folder = new File(Environment.getExternalStorageDirectory() + "/Smart all in one").getAbsolutePath();
			backPressed = false;
			_refresh();
			drawerLayout.setDrawerOpen(false, true);
		});

		_sc_user.setOnClickListener(v -> {
			mode = 0;
			folder = new File(Environment.getExternalStorageDirectory() + "/saio").getAbsolutePath();
			backPressed = false;
			_refresh();
			drawerLayout.setDrawerOpen(false, true);
		});

		_sc_device.setText(Build.MODEL);

		_sc_device.setOnClickListener(v -> {
			mode = 0;
			folder = FileUtil.getExternalStorageDir();
			backPressed = false;
			_refresh();
			drawerLayout.setDrawerOpen(false, true);
		});

		_sc_audio.setOnClickListener(v -> {
			mode = 0;
			folder = FileUtil.getPublicDir(Environment.DIRECTORY_MUSIC);
			backPressed = false;
			_refresh();
			drawerLayout.setDrawerOpen(false, true);
		});

		_sc_document.setOnClickListener(v -> {
			mode = 0;
			folder = FileUtil.getPublicDir(Environment.DIRECTORY_DOCUMENTS);
			backPressed = false;
			_refresh();
			drawerLayout.setDrawerOpen(false, true);
		});

		_sc_image.setOnClickListener(v -> {
			mode = 0;
			folder = FileUtil.getPublicDir(Environment.DIRECTORY_PICTURES);
			backPressed = false;
			_refresh();
			drawerLayout.setDrawerOpen(false, true);
		});

		_sc_video.setOnClickListener(v -> {
			mode = 0;
			folder = FileUtil.getPublicDir(Environment.DIRECTORY_MOVIES);
			backPressed = false;
			_refresh();
			drawerLayout.setDrawerOpen(false, true);
		});

		_sc_download.setOnClickListener(v -> {
			mode = 0;
			folder = FileUtil.getPublicDir(Environment.DIRECTORY_DOWNLOADS);
			backPressed = false;
			_refresh();
			drawerLayout.setDrawerOpen(false, true);
		});

		title_unit = Setting.getFileTitle(this) == 0;
		so = Setting.getFileSort(this) == 0;
		sr_refresh = Setting.getFileSort(this) == 1;
		file_image = Setting.getFileImage(this);
		file_video = Setting.getFileVideo(this);

		_ftp_connect.setOnClickListener(v -> ftp_connect_dialog());

		_ftp_folder.setOnClickListener(v -> {
			mode = 1;
			new ChangeDirectoryTask("/").execute();
			drawerLayout.setDrawerOpen(false, true);
		});

		_ftp_disconnect.setOnClickListener(v -> ftp_disconnect_dialog());

		swipe_refresh.seslSetRefreshOnce(true);
		swipe_refresh.setOnRefreshListener(() -> {
			if (sr_refresh) {
				clearCacheForCurrentFolder();
			}
			if (mode == 1) {
				new ChangeDirectoryTask(currentPath).execute();
				swipe_refresh.setRefreshing(true);
			} else {
				backPressed = false;
				_refresh();
			}
		});
		folder = FileUtil.getExternalStorageDir();
		_refresh();

		TypedValue divider = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.listDivider, divider, true);

		for (int i = 0; i < list.size(); i++) selected.put(i, false);

		listView = findViewById(R.id.images);
		listView.setLayoutManager(new LinearLayoutManager(this));
		imageAdapter = new ImageAdapter();
		listView.setAdapter(imageAdapter);

		ItemDecoration decoration = new ItemDecoration();
		listView.addItemDecoration(decoration);
		decoration.setDivider(getDrawable(divider.resourceId));

		listView.setItemAnimator(null);
		listView.seslSetFillBottomEnabled(true);
		listView.seslSetGoToTopEnabled(true);
		listView.seslSetLastRoundedCorner(false);
		listView.setHasFixedSize(true);

		IndexScrollView indexScrollView = findViewById(R.id.indexScrollView);
		indexScrollView.syncWithRecyclerView(listView, list_name, true);
		indexScrollView.setIndexBarGravity(isRTL() ? 0 : 1);

		if (so) {
			if (listView != null && list_name != null && !list_name.isEmpty()) {
				indexScrollView.syncWithRecyclerView(listView, list_name, true);
				indexScrollView.setVisibility(View.VISIBLE);
			} else {
				indexScrollView.setVisibility(View.GONE);
			}

			if (listView != null) {
				listView.seslSetFastScrollerEnabled(false);
			}
		} else {
			indexScrollView.setVisibility(View.GONE);
			if (listView != null) {
				listView.seslSetFastScrollerEnabled(true);
			}
		}

		if (!FileUtil.readFile("/data/user/0/com.dlawoals2713.oui4.file/data/DebugMode.txt").equals("1")) {
			_sc_app.setVisibility(View.GONE);
			_sc_appRoot.setVisibility(View.GONE);
			_sc_appData.setVisibility(View.GONE);
		} else {
			debugmode = true;
		}
	}

	private void _sort() {
		int previousSort = sort;
		new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this)
				.setTitle("정렬 설정")
				.setSingleChoiceItems(
						new String[]{"이름 순 (A-Z)", "이름 역순 (Z-A)", "작은 파일 순", "큰 파일 순", "오래된 파일 순", "최근 파일 순", "유형 순 (.A-.Z)", "유형 역순 (.Z-.A)"}, // 옵션 항목
						sort, // 현재 선택된 항목
						(dialog, which) -> {
							// 사용자가 항목을 선택할 때마다 임시로 sort 값 업데이트
							sort = which;
						}
				)
				.setPositiveButton("확인", (dialog, which) -> {
					// 기존 설정과 동일한지 확인
					if (sort != previousSort) {
						// 설정이 변경된 경우
						backPressed = true;
						_refresh();
					}
					dialog.dismiss();
				})
				.setNegativeButton("취소", (dialog, which) -> {
					// 취소 버튼 클릭 시 아무 작업도 하지 않음
					dialog.dismiss();
				})
				.show();
	}

	@SuppressLint("MissingSuperCall")
	@Override
	public void onBackPressed() {
		if (mSelecting) {
			setSelecting(false);
		} else if (isBrowseZip) {
			if (zipNavigationStack.isEmpty()) {
				// Exiting zip Browse mode: Go back to the parent folder of the zip file
				isBrowseZip = false;
				String parentOfZip = new File(currentZipFilePath).getParent();
				folder = (parentOfZip != null) ? parentOfZip : Environment.getExternalStorageDirectory().getAbsolutePath(); // Default to external storage root if parent is null
				currentZipFilePath = "";
				currentZipInnerPath = "";
				backPressed = true;
				_refresh(); // Refresh the list with the actual file system content
			} else {
				// Navigate up inside the zip
				zipNavigationStack.pop();
				currentZipInnerPath = zipNavigationStack.isEmpty() ? "" : zipNavigationStack.peek();
				browseZipFile(currentZipFilePath); // Refresh list with new inner path
			}
		} else if (mode == 1) {
			// FTP 모드일 때 상위 폴더로 이동
			if (currentPath.equals("/")) {
				// 최상위 폴더면 연결 해제
				ftp_disconnect_dialog();
			} else {
				new ChangeDirectoryTask("..").execute();
			}
		} else {
			// Existing local file system back navigation logic
			if (folder.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
				finish(); // Exit app if at root of external storage
			} else {
				folder = new File(folder).getParent();
				if (folder == null) {
					folder = Environment.getExternalStorageDirectory().getAbsolutePath();
				}
				backPressed = true;
				_refresh();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		try {
		//    if (hasManageAllFilesPermission()) {
			// RecyclerView의 상태 저장
			Parcelable recyclerViewState = listView.getLayoutManager().onSaveInstanceState();
			outState.putParcelable("recycler_state", recyclerViewState);
			outState.putString("foler_path", folder);
		//    }
		} catch (Exception e) {
			ExceptionLogger.log(e, "FileManagerActivity:onSaveInstanceState");
			e.printStackTrace();
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		// RecyclerView의 상태 복원
		try {
			if (savedInstanceState != null) {
				Parcelable recyclerViewState = savedInstanceState.getParcelable("recycler_state");
				listView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
				folder = savedInstanceState.getString("foler_path");
				backPressed = false;
				_refresh();
			}
			super.onRestoreInstanceState(savedInstanceState);
		} catch (Exception e) {
			ExceptionLogger.log(e, "FileManagerActivity:onRestoreInstanceState");
			e.printStackTrace();
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	public void _refresh() {
		if (isBrowseZip) {
			browseZipFile(currentZipFilePath);
		} else if (mode == 0) {
			try {
				list.clear();
				listinstring.clear();
				list_name.clear();
				selected.clear();

				_refresh_title();
                String sub = folder.replace(FileUtil.getExternalStorageDir(), "");
				FileUtil.listDir(folder, listinstring);

				if (so) {
					// 정렬 기준에 따라 다른 Comparator 적용
					sb = true;
					Comparator<String> comparator;
					switch (sort) {
						case 0: // 이름순 (기본)
							comparator = String.CASE_INSENSITIVE_ORDER;
							break;

						case 1: // 이름순 (기본)
							comparator = String.CASE_INSENSITIVE_ORDER.reversed();
							break;

						case 2: // 파일 크기/폴더 개수순 (폴더 우선, 개별 역순 정렬)
							comparator = (path1, path2) -> {
								File file1 = new File(path1);
								File file2 = new File(path2);

								// 1. 폴더와 파일 여부를 먼저 비교하여 폴더 우선 정렬
								if (file1.isDirectory() && !file2.isDirectory()) {
									return -1; // path1이 폴더이고 path2가 파일이면 path1이 먼저 옴
								}
								if (!file1.isDirectory() && file2.isDirectory()) {
									return 1; // path1이 파일이고 path2가 폴더면 path2가 먼저 옴
								}

								// 2. 둘 다 폴더이거나 둘 다 파일인 경우
								long size1 = file1.isDirectory() ? (file1.list() != null ? file1.list().length : 0) : file1.length();
								long size2 = file2.isDirectory() ? (file2.list() != null ? file2.list().length : 0) : file2.length();

								// 크기/개수를 역순으로 비교 (기존의 Long.compare(size2, size1)과 동일한 효과)
								if (size1 != size2) {
									return Long.compare(size1, size2); // 오름차순으로 비교하여, reversed() 적용 효과
								} else {
									// 크기/개수가 같으면 경로를 역순으로 비교 (path2.compareToIgnoreCase(path1)과 동일한 효과)
									return path2.compareToIgnoreCase(path1); // 대소문자 무시하고 내림차순
								}
							};
							// 여기서는 .reversed()를 호출하지 않습니다. 위 로직 자체가 역순 정렬을 포함하고 있기 때문입니다.
							sb = false;
							break;

						case 3: // 파일 크기/폴더 개수순
							comparator = (path1, path2) -> {
								File file1 = new File(path1);
								File file2 = new File(path2);

								long size1 = file1.isDirectory() ? (file1.list() != null ? file1.list().length : 0) : file1.length();
								long size2 = file2.isDirectory() ? (file2.list() != null ? file2.list().length : 0) : file2.length();

								if (size1 != size2) {
									return Long.compare(size2, size1);
								} else {
									return path1.compareToIgnoreCase(path2);
								}
							};
							sb = false;
							break;

						case 4: // 날짜순
							comparator = (path1, path2) -> {
								File file1 = new File(path1);
								File file2 = new File(path2);

								long date1 = file1.lastModified();
								long date2 = file2.lastModified();

								if (date1 != date2) {
									return Long.compare(date1, date2);
								} else {
									return path1.compareToIgnoreCase(path2);
								}
							};
							sb = false;
							break;

						case 5: // 날짜순
							comparator = (path1, path2) -> {
                                File file1 = new File(path1);
                                File file2 = new File(path2);

                                long date1 = file1.lastModified();
                                long date2 = file2.lastModified();

                                if (date1 != date2) {
                                    return Long.compare(date2, date1);
                                } else {
                                    return path1.compareToIgnoreCase(path2);
                                }
                            };
							sb = false;
							break;

						case 6: // 확장자 형식순
							comparator = new Comparator<String>() {
								@Override
								public int compare(String path1, String path2) {
									File file1 = new File(path1);
									File file2 = new File(path2);

									if (file1.isDirectory() && file2.isDirectory()) {
										return path1.compareToIgnoreCase(path2);
									}
									else if (file1.isDirectory()) {
										return -1;
									}
									else if (file2.isDirectory()) {
										return 1;
									}
									else {
										String ext1 = getFileExtension(path1);
										String ext2 = getFileExtension(path2);

										int extCompare = ext1.compareToIgnoreCase(ext2);
										if (extCompare != 0) {
											return extCompare;
										} else {
											return path1.compareToIgnoreCase(path2);
										}
									}
								}

								private String getFileExtension(String path) {
									int lastDot = path.lastIndexOf('.');
									if (lastDot != -1 && lastDot < path.length() - 1) {
										return path.substring(lastDot + 1).toLowerCase();
									}
									return "";
								}
							};
							sb = false;
							break;

						case 7: // 확장자 형식순 (Z-A)
							comparator = new Comparator<String>() {
								@Override
								public int compare(String path1, String path2) {
									File file1 = new File(path1);
									File file2 = new File(path2);

									// 1. 폴더와 파일 여부를 먼저 비교하여 폴더 우선 정렬 (기존 로직 유지)
									if (file1.isDirectory() && file2.isDirectory()) {
										return path1.compareToIgnoreCase(path2); // 폴더는 경로 기준 A-Z 정렬
									}
									else if (file1.isDirectory()) {
										return -1; // path1이 폴더이면 먼저 옴
									}
									else if (file2.isDirectory()) {
										return 1; // path2가 폴더이면 먼저 옴
									}
									else {
										// 2. 둘 다 파일인 경우 확장자 비교
										String ext1 = getFileExtension(path1);
										String ext2 = getFileExtension(path2);

										// 확장자 비교 결과의 부호를 반전시켜 Z-A 순서로 만듭니다.
										int extCompare = ext1.compareToIgnoreCase(ext2);
										if (extCompare != 0) {
											return -extCompare; // 여기가 변경된 부분: Z-A 정렬을 위해 -extCompare 반환
										} else {
											// 확장자가 같으면 경로를 기준으로 Z-A 정렬
											return path2.compareToIgnoreCase(path1); // 여기가 변경된 부분: 경로도 Z-A 정렬
										}
									}
								}

								// 파일 확장자를 추출하는 헬퍼 메서드 (동일)
								private String getFileExtension(String path) {
									int lastDot = path.lastIndexOf('.');
									if (lastDot != -1 && lastDot < path.length() - 1) {
										return path.substring(lastDot + 1).toLowerCase();
									}
									return "";
								}
							};
							sb = false;
							break;

						default:
							comparator = String.CASE_INSENSITIVE_ORDER;
							break;
					}

					listinstring.sort(comparator);
					pos = 0;

					for (int _repeat26 = 0; _repeat26 < listinstring.size(); _repeat26++) {
						HashMap<String, Object> _item = new HashMap<>();
						_item.put("file", listinstring.get((int)(pos)));
						list.add(_item);
						list_name.add(Uri.parse(listinstring.get((int)(pos))).getLastPathSegment());
						selected.put((int) pos, false);
						pos++;
					}

					HashMap<String, Object> _item = new HashMap<>();
					_item.put("file", "-1");
					list.add(_item);
					selected.put(list.size() - 1, false);

					path.setText(folder);
					if (listinstring.isEmpty()) {
						SketchwareUtil.showMessage(getApplicationContext(), "이 폴더는 비었습니다.");
					} else {
						_refresh_list();
					}

					if (sb) {
						if (listView != null && list_name != null && !list_name.isEmpty()) {
							indexScrollView.syncWithRecyclerView(listView, list_name, true);
							indexScrollView.setVisibility(View.VISIBLE);
						} else {
							indexScrollView.setVisibility(View.GONE);
						}

						if (listView != null) {
							listView.seslSetFastScrollerEnabled(false);
						}
					} else {
						indexScrollView.setVisibility(View.GONE);
						if (listView != null) {
							listView.seslSetFastScrollerEnabled(true);
						}
					}
				} else {
					indexScrollView.setVisibility(View.GONE);
					if (listView != null) {
						listView.seslSetFastScrollerEnabled(true);
					}

					ArrayList<String> folders = new ArrayList<>();
					ArrayList<String> files = new ArrayList<>();

					for (String path : listinstring) {
						File file = new File(path);
						if (file.isDirectory()) {
							folders.add(path);
						} else {
							files.add(path);
						}
					}

					Comparator<String> comparator;
					switch (sort) {
						case 0: // 이름순 (기본)
							comparator = String.CASE_INSENSITIVE_ORDER;
							break;

						case 1: // 이름순 (기본)
							comparator = String.CASE_INSENSITIVE_ORDER.reversed();
							break;

						case 2: // 파일 크기/폴더 개수순 (폴더 우선, 개별 역순 정렬)
							comparator = (path1, path2) -> {
								File file1 = new File(path1);
								File file2 = new File(path2);

								// 1. 폴더와 파일 여부를 먼저 비교하여 폴더 우선 정렬
								if (file1.isDirectory() && !file2.isDirectory()) {
									return -1; // path1이 폴더이고 path2가 파일이면 path1이 먼저 옴
								}
								if (!file1.isDirectory() && file2.isDirectory()) {
									return 1; // path1이 파일이고 path2가 폴더면 path2가 먼저 옴
								}

								// 2. 둘 다 폴더이거나 둘 다 파일인 경우
								long size1 = file1.isDirectory() ? (file1.list() != null ? file1.list().length : 0) : file1.length();
								long size2 = file2.isDirectory() ? (file2.list() != null ? file2.list().length : 0) : file2.length();

								// 크기/개수를 역순으로 비교 (기존의 Long.compare(size2, size1)과 동일한 효과)
								if (size1 != size2) {
									return Long.compare(size1, size2); // 오름차순으로 비교하여, reversed() 적용 효과
								} else {
									// 크기/개수가 같으면 경로를 역순으로 비교 (path2.compareToIgnoreCase(path1)과 동일한 효과)
									return path2.compareToIgnoreCase(path1); // 대소문자 무시하고 내림차순
								}
							};
							// 여기서는 .reversed()를 호출하지 않습니다. 위 로직 자체가 역순 정렬을 포함하고 있기 때문입니다.
							break;

						case 3: // 파일 크기/폴더 개수순
							comparator = (path1, path2) -> {
								File file1 = new File(path1);
								File file2 = new File(path2);

								long size1 = file1.isDirectory() ? (file1.list() != null ? file1.list().length : 0) : file1.length();
								long size2 = file2.isDirectory() ? (file2.list() != null ? file2.list().length : 0) : file2.length();

								if (size1 != size2) {
									return Long.compare(size2, size1);
								} else {
									return path1.compareToIgnoreCase(path2);
								}
							};
							break;

						case 4: // 날짜순
							comparator = (path1, path2) -> {
								File file1 = new File(path1);
								File file2 = new File(path2);

								long date1 = file1.lastModified();
								long date2 = file2.lastModified();

								if (date1 != date2) {
									return Long.compare(date1, date2);
								} else {
									return path1.compareToIgnoreCase(path2);
								}
							};
							break;

						case 5: // 날짜순
							comparator = (path1, path2) -> {
								File file1 = new File(path1);
								File file2 = new File(path2);

								long date1 = file1.lastModified();
								long date2 = file2.lastModified();

								if (date1 != date2) {
									return Long.compare(date2, date1);
								} else {
									return path1.compareToIgnoreCase(path2);
								}
							};
							break;

						case 6: // 확장자 형식순
							comparator = new Comparator<String>() {
								@Override
								public int compare(String path1, String path2) {
									File file1 = new File(path1);
									File file2 = new File(path2);

									if (file1.isDirectory() && file2.isDirectory()) {
										return path1.compareToIgnoreCase(path2);
									}
									else if (file1.isDirectory()) {
										return -1;
									}
									else if (file2.isDirectory()) {
										return 1;
									}
									else {
										String ext1 = getFileExtension(path1);
										String ext2 = getFileExtension(path2);

										int extCompare = ext1.compareToIgnoreCase(ext2);
										if (extCompare != 0) {
											return extCompare;
										} else {
											return path1.compareToIgnoreCase(path2);
										}
									}
								}

								private String getFileExtension(String path) {
									int lastDot = path.lastIndexOf('.');
									if (lastDot != -1 && lastDot < path.length() - 1) {
										return path.substring(lastDot + 1).toLowerCase();
									}
									return "";
								}
							};
							break;

						case 7: // 확장자 형식순 (Z-A)
							comparator = new Comparator<String>() {
								@Override
								public int compare(String path1, String path2) {
									File file1 = new File(path1);
									File file2 = new File(path2);

									// 1. 폴더와 파일 여부를 먼저 비교하여 폴더 우선 정렬 (기존 로직 유지)
									if (file1.isDirectory() && file2.isDirectory()) {
										return path1.compareToIgnoreCase(path2); // 폴더는 경로 기준 A-Z 정렬
									}
									else if (file1.isDirectory()) {
										return -1; // path1이 폴더이면 먼저 옴
									}
									else if (file2.isDirectory()) {
										return 1; // path2가 폴더이면 먼저 옴
									}
									else {
										// 2. 둘 다 파일인 경우 확장자 비교
										String ext1 = getFileExtension(path1);
										String ext2 = getFileExtension(path2);

										// 확장자 비교 결과의 부호를 반전시켜 Z-A 순서로 만듭니다.
										int extCompare = ext1.compareToIgnoreCase(ext2);
										if (extCompare != 0) {
											return -extCompare; // 여기가 변경된 부분: Z-A 정렬을 위해 -extCompare 반환
										} else {
											// 확장자가 같으면 경로를 기준으로 Z-A 정렬
											return path2.compareToIgnoreCase(path1); // 여기가 변경된 부분: 경로도 Z-A 정렬
										}
									}
								}

								// 파일 확장자를 추출하는 헬퍼 메서드 (동일)
								private String getFileExtension(String path) {
									int lastDot = path.lastIndexOf('.');
									if (lastDot != -1 && lastDot < path.length() - 1) {
										return path.substring(lastDot + 1).toLowerCase();
									}
									return "";
								}
							};
							break;

						default:
							comparator = String.CASE_INSENSITIVE_ORDER;
							break;
					}

					folders.sort(comparator);
					files.sort(comparator);

					listinstring.clear();
					listinstring.addAll(folders);
					listinstring.addAll(files);

					pos = 0;

					for (int _repeat26 = 0; _repeat26 < listinstring.size(); _repeat26++) {
						HashMap<String, Object> _item = new HashMap<>();
						_item.put("file", listinstring.get((int)pos));
						list.add(_item);
						list_name.add(Uri.parse(listinstring.get((int)pos)).getLastPathSegment());
						selected.put((int)pos, false);
						pos++;
					}

					HashMap<String, Object> _item = new HashMap<>();
					_item.put("file", "-1");
					list.add(_item);
					selected.put(list.size() - 1, false);

					path.setText(folder);
					if (listinstring.isEmpty()) {
						SketchwareUtil.showMessage(getApplicationContext(), "이 폴더는 비었습니다.");
					} else {
						_refresh_list();
					}
				}

				if (listView != null) {
					if (!backPressed && scrollStateMap != null) {
						scrollStateMap.remove(folder);
						listView.scrollToPosition(0);
						if (!sr_refresh) {
							clearCacheForCurrentFolder();
						}
					} else if (folder != null && scrollStateMap.containsKey(folder)) {
						Parcelable scrollState = scrollStateMap.get(folder);
						if (scrollState != null) {
							listView.getLayoutManager().onRestoreInstanceState(scrollState);
						}
					}
				}
			} catch (Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:_refresh");
				SketchwareUtil.showMessage(getApplicationContext(), e.getMessage());
			}
		}
		saioExists();
	}

	private void saioExists() {
		try {
			File folder = new File(Environment.getExternalStorageDirectory() + "/saio");

			if (folder.exists() && folder.isDirectory()) {
				findViewById(R.id.drawer_view).findViewById(R.id.sc_user).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.drawer_view).findViewById(R.id.sc_user).setVisibility(View.GONE);
			}
		} catch (Exception e) {
			ExceptionLogger.log(e, "FilemanagerActivity:saioExists");
		}
	}

	public void _open(final double _position) {
		if (mode == 1) {
			String selectedItem = listinstring.get((int)_position);

			FTPFile selectedFile = getFTPFile((int)_position);
			if (selectedFile != null) {
				if (selectedFile.isDirectory()) {
					new ChangeDirectoryTask(selectedFile.getName()).execute();
				} else {
					downloadFTPFile(selectedFile, true, null);
				}
			}
		} else if (isBrowseZip) { // --- Handle opening within a zip file ---
			String selectedItemName = list_name.get((int)_position); // Get name from list_name for display

			if (selectedItemName.equals("..")) {
				onBackPressed(); // Handle navigating up inside the zip
			} else {
				// It's a file/folder inside the zip
				// Use listinstring which stores the full internal zip path
				String fullZipEntryPath = listinstring.get((int)_position);

				try (ZipFile zipFile = new ZipFile(currentZipFilePath)) {
					ZipEntry entry = zipFile.getEntry(fullZipEntryPath); // Try exact match first

					// If direct entry not found, it might be a directory represented as "dir/" in list_name
					if (entry == null && !fullZipEntryPath.endsWith("/")) {
						entry = zipFile.getEntry(fullZipEntryPath + "/"); // Try with trailing slash for directories
					}

					if (entry != null) {
						if (entry.isDirectory()) {
							// Trim trailing slash for currentZipInnerPath if it's a directory
							String dirPath = fullZipEntryPath.endsWith("/") ? fullZipEntryPath.substring(0, fullZipEntryPath.length() - 1) : fullZipEntryPath;
							currentZipInnerPath = dirPath;
							zipNavigationStack.push(currentZipInnerPath);
							browseZipFile(currentZipFilePath);
						} else {
							openZipEntry(zipFile, entry);
						}
					} else {
						Toast.makeText(this, "ZIP 항목을 찾을 수 없습니다: " + fullZipEntryPath, Toast.LENGTH_LONG).show();
					}
				} catch (IOException e) {
					ExceptionLogger.log(e, "FileManagerActivity:_open-IOException");
					e.printStackTrace();
					Toast.makeText(this, "ZIP 파일 처리 중 오류 발생: " + e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		} else if (FileUtil.isDirectory(listinstring.get((int)(_position)))) {
			Parcelable scrollState = listView.getLayoutManager().onSaveInstanceState();
			scrollStateMap.put(folder, scrollState);
			folder = listinstring.get((int)(_position));
			backPressed = false;
			_refresh();
		} else if (listinstring.get((int)(_position)).toLowerCase().endsWith(".zip")) { // --- Handle opening a zip file ---
			// It's a zip file, enter zip Browse mode
			isBrowseZip = true;
			currentZipFilePath = listinstring.get((int)(_position));
			currentZipInnerPath = ""; // Start at the root of the zip
			zipNavigationStack.clear(); // Clear any previous zip navigation history
			browseZipFile(currentZipFilePath);
		} else if (isAudioFile(listinstring.get((int)(_position)))) { // Assuming isAudioFile exists
			String filePath = listinstring.get((int)(_position));
			playMusicFromPath(filePath); // Assuming playMusicFromPath exists
			FrameLayout mini_player_container = findViewById(R.id.mini_player_container);
			if (mini_player_container != null) { // Null check for safety
				mini_player_container.setVisibility(View.VISIBLE);
			}
		} else if (isVideoFile(listinstring.get((int)(_position))) || isVlcAudio(listinstring.get((int)(_position))) || isVlcVideo(listinstring.get((int)(_position))) || isVlcOther(listinstring.get((int)(_position)))) { // Assuming these methods exist
			_playVideo(_position); // Assuming _playVideo exists
		} else if (isImageFile(listinstring.get((int)(_position)))) { // Assuming isImageFile exists
			_viewImage(_position); // Assuming _viewImage exists
		} else if (listinstring.get((int)(_position)).toLowerCase().endsWith(".txt")) {
			intent.setClass(getApplicationContext(), TxtEditorActivity.class); // Assuming TxtEditorActivity exists
			intent.putExtra("load", listinstring.get((int)(_position)));
			startActivity(intent);
		} else if (listinstring.get((int)(_position)).toLowerCase().endsWith(".pdf")) {
			intent.setClass(getApplicationContext(), PdfViewerActivity.class); // Assuming PdfViewerActivity exists
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("file://" + listinstring.get((int)(_position))));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
			intent.putExtra("f", listinstring.get((int)(_position)));
			startActivity(intent);
		} else {
			File file = new File(listinstring.get((int)(_position)));
			if (!file.exists()) {
				Toast.makeText(this, "파일이 존재하지 않아요", Toast.LENGTH_SHORT).show();
				return;
			}

			new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this)
					.setTitle("이 앱에서 파일을 열 수 없음")
					.setMessage("파일을 열 방법을 선택해주세요.")
					.setPositiveButton("다른 앱에서 열기", (dialog, which) -> {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						try {
							Uri fileUri = FileProvider.getUriForFile(FilemanagerActivity.this, getPackageName() + ".provider", file);
							String mimeType = getMimeTypeOpen(file.getName()); // Assuming getMimeTypeOpen exists
							if (mimeType == null) {
								mimeType = "*/*";
							}
							intent.setDataAndType(fileUri, mimeType);
							intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
							startActivity(intent);
						} catch (Exception e) {
							ExceptionLogger.log(e, "FileManagerActivity:_open");
							new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(FilemanagerActivity.this)
									.setTitle("오류")
									.setMessage("이 파일을 외부 앱으로 열 수 없어요.")
									.setPositiveButton("확인", (dialog1, which1) -> {})
									.setNegativeButton("텍스트로 열기", (dialog1, which1) -> {
										Intent textIntent = new Intent(getApplicationContext(), TxtEditorActivity.class);
										textIntent.putExtra("load", listinstring.get((int)(_position)));
										startActivity(textIntent);
									})
									.show();
							}
					})
					.setNegativeButton("텍스트로 열기", (dialog, which) -> {
						Intent textIntent = new Intent(getApplicationContext(), TxtEditorActivity.class);
						textIntent.putExtra("load", listinstring.get((int)(_position)));
						startActivity(textIntent);
					})
					.setNeutralButton("취소", (dialog, which) -> {})
					.show();
			}
	}

	// FilemanagerActivity.java 파일 내 browseZipFile 메서드
	private void browseZipFile(String zipFilePath) {
		list.clear();
		listinstring.clear();
		list_name.clear();
		selected.clear();
		fileInfoCache.clear();

		try (ZipFile zipFile = new ZipFile(zipFilePath)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			ArrayList<HashMap<String, Object>> zipEntriesToDisplay = new ArrayList<>();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String entryName = entry.getName();

				if (entryName.startsWith(currentZipInnerPath) && !entryName.equals(currentZipInnerPath)) {
					String relativePath = entryName.substring(currentZipInnerPath.length());
					if (relativePath.startsWith("/")) {
						relativePath = relativePath.substring(1);
					}

					if (!relativePath.isEmpty()) {
						String topLevelName = relativePath;
						boolean isDirectory = entry.isDirectory() || entryName.endsWith("/");

						if (relativePath.contains("/")) {
							topLevelName = relativePath.substring(0, relativePath.indexOf("/"));
							isDirectory = true;
						}

						HashMap<String, Object> existingItem = null;
						for (HashMap<String, Object> item : zipEntriesToDisplay) {
							if (item.containsKey("name") && item.get("name").equals(topLevelName)) {
								existingItem = item;
								break;
							}
						}

						if (existingItem != null) {
							if (isDirectory && !(Boolean)existingItem.get("isDirectory")) {
								existingItem.put("isDirectory", true);
								String fullInternalDirPath;
								if (currentZipInnerPath.isEmpty()) {
									fullInternalDirPath = topLevelName + "/";
								} else {
									String base = currentZipInnerPath;
									if (!base.endsWith("/")) {
										base += "/";
									}
									fullInternalDirPath = base + topLevelName + "/";
								}
								existingItem.put("file", fullInternalDirPath);
								existingItem.put("size", "");
								existingItem.put("date", "");
							}
						} else {
							HashMap<String, Object> _item = new HashMap<>();
							_item.put("name", topLevelName);
							_item.put("isDirectory", isDirectory);

							if (isDirectory) {
								String fullInternalDirPath;
								if (currentZipInnerPath.isEmpty()) {
									fullInternalDirPath = topLevelName + "/";
								} else {
									String base = currentZipInnerPath;
									if (!base.endsWith("/")) {
										base += "/";
									}
									fullInternalDirPath = base + topLevelName + "/";
								}
								_item.put("file", fullInternalDirPath);
							} else {
								_item.put("file", entryName);
							}

							if (!isDirectory) {
								_item.put("size", formatFileSize(entry.getSize()));
								// --- 수정된 부분 시작 ---
								_item.put("date", new SimpleDateFormat("yyyy년 M월 d일 a h:m:s", Locale.getDefault()).format(new Date(entry.getTime())));
								// --- 수정된 부분 끝 ---
							} else {
								_item.put("size", "");
								_item.put("date", "");
							}
							zipEntriesToDisplay.add(_item);
						}
					}
				}
			}

			zipEntriesToDisplay.sort((o1, o2) -> {
                boolean isDir1 = (Boolean) o1.get("isDirectory");
                boolean isDir2 = (Boolean) o2.get("isDirectory");
                String name1 = (String) o1.get("name");
                String name2 = (String) o2.get("name");

                if (isDir1 && !isDir2) {
                    return -1;
                } else if (!isDir1 && isDir2) {
                    return 1;
                } else {
                    return name1.compareToIgnoreCase(name2);
                }
            });

			for (HashMap<String, Object> item : zipEntriesToDisplay) {
				listinstring.add((String) item.get("file"));
				list_name.add((String) item.get("name"));
				list.add(item);
				selected.put(list.size() - 1, false);
			}

			HashMap<String, Object> _item = new HashMap<>();
			_item.put("file", "-1");
			_item.put("name", "");
			_item.put("isDirectory", false);
			_item.put("size", "");
			_item.put("date", "");
			list.add(_item);
			selected.put(list.size() - 1, false);


			String displayPath = currentZipFilePath + (currentZipInnerPath.isEmpty() ? "" : "/" + currentZipInnerPath);
			path.setText(displayPath);

			imageAdapter.notifyDataSetChanged();
			listView.scrollToPosition(0);
			if (indexScrollView != null) {
				indexScrollView.setVisibility(View.GONE);
			}
			listView.seslSetFastScrollerEnabled(false);
			_refresh_title();
		} catch (ZipException e) {
			Toast.makeText(this, "손상된 ZIP 파일이거나 ZIP 파일이 아닙니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
			isBrowseZip = false;
			currentZipFilePath = "";
			currentZipInnerPath = "";
			_refresh();
			ExceptionLogger.log(e, "FileManagerActivity:browseZipFile-ZipException");
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(this, "ZIP 파일을 읽는 중 오류 발생: " + e.getMessage(), Toast.LENGTH_LONG).show();
			isBrowseZip = false;
			currentZipFilePath = "";
			currentZipInnerPath = "";
			_refresh();
			ExceptionLogger.log(e, "FileManagerActivity:browseZipFile-IOException");
			e.printStackTrace();
		}
	}

	// Helper method to open a file entry inside a zip
	private void openZipEntry(ZipFile zipFile, ZipEntry entry) {
		try {
			InputStream is = zipFile.getInputStream(entry);
			// Example: Just showing a toast. For actual opening:
			// 1. Create a temporary file.
			// 2. Write content from 'is' to the temporary file.
			// 3. Get Uri for the temporary file using FileProvider.
			// 4. Create an Intent with ACTION_VIEW and appropriate MIME type.
			// 5. Start activity.
			// Remember to handle temporary file cleanup.
			Toast.makeText(this, "ZIP 내부 파일 열기: " + entry.getName() + "\n(실제 파일 열기 구현 필요)", Toast.LENGTH_LONG).show();
			is.close();
		} catch (IOException e) {
			ExceptionLogger.log(e, "FileManagerActivity:openZipEntry");
			e.printStackTrace();
			Toast.makeText(this, "ZIP 내부 파일 열기 중 오류 발생: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private String getParentPath(String path) {
		if (path.equals("/")) {
			return "/";
		}
		File file = new File(path);
		String parent = file.getParent();
		if (parent == null) {
			return "/"; // Should not happen for valid paths unless already at root
		}
		return parent.isEmpty() ? "/" : parent;
	}

	public void _refresh_title() {
		if (title_unit) {
			drawerLayout.setTitle("파일 매니저");
			if (mode == 1) {
				drawerLayout.setSubtitle(ftpHelper.getServerAddress());
			} else if (mode == 2) {
				drawerLayout.setSubtitle("스토리지 서버");
			} else {
				if (folder.equals(FileUtil.getExternalStorageDir())) {
					drawerLayout.setSubtitle("내장 저장공간");
				} else {
					drawerLayout.setSubtitle(Uri.parse(folder).getLastPathSegment());
				}
			}
		} else {
			drawerLayout.setSubtitle("파일 매니저");
			if (mode == 1) {
				drawerLayout.setTitle(ftpHelper.getServerAddress());
			} else if (mode == 2) {
				drawerLayout.setTitle("스토리지 서버");
			} else {
				if (folder.equals(FileUtil.getExternalStorageDir())) {
					drawerLayout.setTitle("내장 저장공간");
				} else {
					drawerLayout.setTitle(Uri.parse(folder).getLastPathSegment());
				}
			}
		}
	}

	public void _playVideo(final double _position) {
		String videoPath = listinstring.get((int) _position);

		Class<?> targetActivity = null;
		Intent intent;

		if (file_video == 1) {
			targetActivity = FilemanagerVideoActivity.class;
		} else if (file_video == 0) {
			targetActivity = FilemanagerVLCActivity.class;
		}

		if (targetActivity != null) {
			intent = new Intent(getApplicationContext(), targetActivity);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("file://" + videoPath));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
			intent.putExtra("path", videoPath);

			startActivity(intent);
		} else {
			new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this)
					.setTitle("비디오 뷰어 방식을 선택하세요")
					.setSingleChoiceItems(
							new String[]{"VLC 플레이어", "기본 플레이어"},
							video_view,
							(dialog, which) -> video_view = which
					)
					.setPositiveButton("확인", (dialog, which) -> {
						Class<?> selectedActivity = (video_view == 0)
								? FilemanagerVLCActivity.class
								: FilemanagerVideoActivity.class;

						Intent chosenIntent = new Intent(getApplicationContext(), selectedActivity);
						chosenIntent.setAction(Intent.ACTION_VIEW);
						chosenIntent.setData(Uri.parse("file://" + videoPath));
						chosenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
						chosenIntent.putExtra("path", videoPath);

						if (video_view == 0) {
							chosenIntent.putExtra("title", new File(videoPath).getName());
						} else {
							chosenIntent.putExtra("sort", sort);
						}

						startActivity(chosenIntent);
						dialog.dismiss();
					})
					.setNegativeButton("취소", (dialog, which) -> {
						dialog.dismiss();
						})
					.show();
			}
	}

	public void _viewImage(final double _position) {
		String imagePath = listinstring.get((int) _position);

		Class<?> targetActivity = null;
		Intent intent;

		if (file_image == 1) {
			targetActivity = FilemanagerMultiImageActivity.class;
		} else if (file_image == 0) {
			targetActivity = FilemanagerSingleImageActivity.class;
		}

		if (targetActivity != null) {
			intent = new Intent(getApplicationContext(), targetActivity);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("file://" + imagePath));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
			intent.putExtra("path", imagePath);

			if (file_image == 1) {
				intent.putExtra("sort", sort);
			} else {
				intent.putExtra("title", new File(imagePath).getName());
			}

			startActivity(intent);
		} else {
			new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this)
					.setTitle("이미지 뷰어 방식을 선택하세요")
					.setSingleChoiceItems(
							new String[]{"하나만 보기", "여러개 보기"},
							image_view,
							(dialog, which) -> image_view = which
					)
					.setPositiveButton("확인", (dialog, which) -> {
						Class<?> selectedActivity = (image_view == 0)
								? FilemanagerSingleImageActivity.class
								: FilemanagerMultiImageActivity.class;

						Intent chosenIntent = new Intent(getApplicationContext(), selectedActivity);
						chosenIntent.setAction(Intent.ACTION_VIEW);
						chosenIntent.setData(Uri.parse("file://" + imagePath));
						chosenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
						chosenIntent.putExtra("path", imagePath);

						if (image_view == 0) {
							chosenIntent.putExtra("title", new File(imagePath).getName());
						} else {
							chosenIntent.putExtra("sort", sort);
						}

						startActivity(chosenIntent);
						dialog.dismiss();
					})
					.setNegativeButton("취소", (dialog, which) -> {
						dialog.dismiss();
						})
					.show();
			}
	}

	public void _refresh_list() {
		if (imageAdapter != null) {
			imageAdapter.notifyDataSetChanged();
		}
	}


	public void _Query(final String _charSeq) {
        if (FileUtil.isDirectory(_charSeq)) {
			folder = _charSeq;
			_refresh();
		} else {
			try{
                int number = Integer.parseInt(_charSeq);
				if (listinstring.size() < number) {
					Toast.makeText(getApplicationContext(), "리스트 항목 수를 초과했습니다", Toast.LENGTH_SHORT).show();
				} else {
					listView.smoothScrollToPosition(number);
				}
			} catch(Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:_Query");
				Toast.makeText(getApplicationContext(), "파일 디렉토리, 혹은 리스트 항목 번호가 아닙니다.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private boolean isNetworkAvailable() {
		android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	// [추가된 코드] - 외부 앱으로 파일 열기
	private void openExternal() {
		ArrayList<Integer> selectedIndices = new ArrayList<>();
		// 선택된 모든 항목의 인덱스를 수집합니다.
		for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
			if (entry.getValue()) {
				// 유효한 파일 인덱스인지 확인합니다. (리스트의 마지막 스페이서 제외)
				if (entry.getKey() < listinstring.size()) {
					selectedIndices.add(entry.getKey());
				}
			}
		}

		// 선택된 파일이 정확히 하나일 때만 실행합니다.
		if (selectedIndices.size() == 1) {
			int selectedIndex = selectedIndices.get(0);
			String filePath = listinstring.get(selectedIndex);
			File file = new File(filePath);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			try {
				Uri fileUri = FileProvider.getUriForFile(FilemanagerActivity.this, getPackageName() + ".provider", file);
				String mimeType = getMimeTypeOpen(file.getName());
				if (mimeType == null) {
					mimeType = "*/*";
				}
				intent.setDataAndType(fileUri, mimeType);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				startActivity(intent);
				} catch (Exception e) {
				ExceptionLogger.log(e, "FileManagerActivity:openExternal");
				new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(FilemanagerActivity.this)
						.setTitle("오류")
						.setMessage("이 파일을 외부 앱으로 열 수 없어요.")
						.setPositiveButton("확인", (dialog1, which1) -> {})
						.setNegativeButton("텍스트로 열기", (dialog1, which1) -> {
							Intent textIntent = new Intent(getApplicationContext(), TxtEditorActivity.class);
							// _position 대신 정확한 파일 경로(filePath)를 사용하도록 수정했습니다.
							textIntent.putExtra("load", filePath);
							startActivity(textIntent);
						})
						.show();
				}
		} else {
			// 선택된 파일이 없거나 2개 이상일 경우
			Toast.makeText(this, "파일을 하나만 선택해주세요.", Toast.LENGTH_SHORT).show();
			}
	}

	private void _vlc() {
		de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder builder = new de.dlyt.yanndroid.oneui.dialog.AlertDialog.Builder(this);
		builder.setTitle("VLC로 열기");
		builder.setMessage("VLC로 열 영상, 혹은 스트리밍(RTSP 등) 링크를 여기에 붙여넣으세요.\n\n예시:\nhttps://example.com/video.mp4\nrtsp://example.com/video.mp4 등");

		final EditText input = new EditText(this);
		input.setHint("");
		input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		builder.setView(input);

// 긍정 버튼 설정
		builder.setPositiveButton("열기", (dialog, which) -> {
			String urlString = input.getText().toString();
			if (urlString.isEmpty()) {
				Toast.makeText(this, "URL을 입력해주세요.", Toast.LENGTH_SHORT).show();
				return;
			}

			try {
				Uri uri = Uri.parse(urlString);

				Intent intent = new Intent(getApplicationContext(), FilemanagerVLCActivity.class);
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(uri);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.putExtra("path", uri.toString());
				startActivity(intent);
				} catch (Exception e) {
				Toast.makeText(this, "잘못된 URL 형식입니다.", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				}
		});

// 부정 버튼 설정
		builder.setNegativeButton("취소", (dialog, which) -> {
			dialog.cancel();
			});

		builder.show();
	}
}
