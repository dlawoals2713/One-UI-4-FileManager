package com.dlawoals2713.oui4.file;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SeslSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;

public class DeveloperMenuActivity extends BaseThemeActivity {
    private LinearLayout group1;
    private LinearLayout group2;
    private EditText edittext2;
	private ImageView imageview1;
	
	private Intent intent = new Intent();
	private SharedPreferences sp;
	private SharedPreferences spp;
	private SharedPreferences sppp;
	private SharedPreferences spppp;
    private SeslSpinner spinner;
    private EditText edittext4;
    private EditText edittext5;

    @Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.developer_menu);
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
		} else {
			initializeLogic();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_view);
        toolbarLayout.setNavigationButtonOnClickListener(view -> onBackPressed());
        group1 = findViewById(R.id.group1);
        group2 = findViewById(R.id.group2);
        TextView textview24 = findViewById(R.id.textview24);
        TextView textview147 = findViewById(R.id.textview147);
        TextView textview118 = findViewById(R.id.textview118);
        TextView textview119 = findViewById(R.id.textview119);
        TextView textview124 = findViewById(R.id.textview124);
        TextView textview132 = findViewById(R.id.textview132);
        TextView textview136 = findViewById(R.id.textview136);
        TextView textview137 = findViewById(R.id.textview137);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
		edittext2 = findViewById(R.id.edittext2);
		imageview1 = findViewById(R.id.imageview1);
        spinner = findViewById(R.id.spinner);
        edittext4 = findViewById(R.id.edittext4);
        edittext5 = findViewById(R.id.edittext5);
        Button button4 = findViewById(R.id.button4);
		sp = getSharedPreferences("insight1", Activity.MODE_PRIVATE);
		spp = getSharedPreferences("FileUploadActivity", Activity.MODE_PRIVATE);
		sppp = getSharedPreferences("HomeActivity", Activity.MODE_PRIVATE);
		spppp = getSharedPreferences("SaioagentActivity", Activity.MODE_PRIVATE);
		
		textview118.setOnClickListener(_view -> {
            FileUtil.writeFile("/data/user/0/com.dlawoals2713.oui4.file/files/data/DebugMode.txt", "1");
            SketchwareUtil.showMessage(getApplicationContext(), "user 0 assigned DebugMode value");
        });
		
		textview119.setOnClickListener(_view -> {
            FileUtil.writeFile("/data/user/0/com.dlawoals2713.oui4.file/files/data/DebugMode.txt", "0");
            SketchwareUtil.showMessage(getApplicationContext(), "off");
        });
		
		edittext2.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				String resourceName = edittext2.getText().toString().trim();
				if (!resourceName.isEmpty()) {
					@SuppressLint("DiscouragedApi") int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());

					if (resId != 0) {
						imageview1.setImageResource(resId);
					} else {
						Toast.makeText(DeveloperMenuActivity.this, "Image not found", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(DeveloperMenuActivity.this, "Please enter a resource name", Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				
			}
			
			@Override
			public void afterTextChanged(Editable _param1) {
				
			}
		});

        List<String> activityClassNames = getAllActivities();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, activityClassNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        button4.setOnClickListener(v -> {
            String selectedActivityName = (String) spinner.getSelectedItem();
            String extraKey = edittext4.getText().toString().trim();
            String extraValue = edittext5.getText().toString().trim();

            try {
                Class<?> activityClass = Class.forName(selectedActivityName);
                Intent intent = new Intent(DeveloperMenuActivity.this, activityClass);

                if (!extraKey.isEmpty()) {
                    if (!extraValue.isEmpty()) {
                        intent.putExtra(extraKey, extraValue);
                    } else {
                        intent.putExtra(extraKey, "");
                    }
                }

                startActivity(intent);

            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "클래스를 찾을 수 없습니다: " + selectedActivityName, Toast.LENGTH_SHORT).show();
            }
        });
	}
	
	private void initializeLogic() {
        group1.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
        group2.setBackground(new GradientDrawable() {
            public GradientDrawable getIns(int a, int b) {
                this.setCornerRadius(a);
                this.setColor(b);
                return this;
            }
        }.getIns((int)60, ContextCompat.getColor(getApplicationContext(), R.color.groupColor)));
	}

    private List<String> getAllActivities() {
        List<String> activityList = new ArrayList<>();
        try {
            PackageManager pm = getPackageManager();
            String packageName = getPackageName();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

            if (packageInfo.activities != null) {
                for (ActivityInfo activityInfo : packageInfo.activities) {
                    activityList.add(activityInfo.name);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "액티비티 정보를 가져오는 데 실패했어요.", Toast.LENGTH_SHORT).show();
        }
        return activityList;
    }
}
