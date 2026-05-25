package com.dlawoals2713.oui4.file;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dlawoals2713.oui4.file.base.BaseThemeActivity;
import com.dlawoals2713.oui4.file.databinding.ActivityUpdatelogBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.dlyt.yanndroid.oneui.view.Snackbar;

public class UpdatelogActivity extends BaseThemeActivity {
	private ActivityUpdatelogBinding binding;
    private String lastUpdate = "";
	private double app = 0;

    private ArrayList<HashMap<String, Object>> list_map = new ArrayList<>();


    @Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = ActivityUpdatelogBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initializeLogic();
	}

	private void initializeLogic() {
		binding.toolbarView.setNavigationButtonOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
		app = Double.parseDouble(getIntent().getStringExtra("app"));
		lastUpdate =
				"• 6.5(1)\n- Smart All in one 6.5 Test 51 AS 에서 클론\n- One UI 3과 One UI 4 전환 기능 추가\n- 스토리지 서버 삭제\n\n" +
						"• 6.5(6559)\n- Smart All in one 6.5 Test 59 AS 에서 파일 최적화 코드 클론\n- /saio 폴더가 없을 때 Drawer의 saio 사용자 저장소를 숨기도록 변경\n- 레이아웃 로드 방식 변경\n- 음악 플레이어에 가로 레이아웃이 없는 문제 수정\n- 설정에서 '큰 화면에서 유닛 나눠 보기' 삭제";
		_log_dataset();
		Snackbar.make(binding.recyclerview1, "최신 업데이트 정보를 확인할까요?", Snackbar.LENGTH_SHORT)
		    .setAction("확인", v -> {
				if (binding.recyclerview1.getAdapter() != null) {
					int lastPosition = binding.recyclerview1.getAdapter().getItemCount() - 1;
					binding.recyclerview1.smoothScrollToPosition(lastPosition);
				}
			}).show();
	}

	public void _log_dataset() {
		List<String> versions = new ArrayList<>();
		List<String> contents = new ArrayList<>();
		switch((int)app) {
			case ((int)0): {
				versions.add("6.5");
				contents.add(lastUpdate);
				break;
			}
			case ((int)1): {
                HashMap<String, Object> camone_log = new Gson().fromJson(FileUtil.readFile(FileUtil.getPackageDataDir(getApplicationContext()).concat("/Smart all in one/data/ex_app/camone_updatelog.txt")), new TypeToken<HashMap<String, Object>>() {
                }.getType());
				for (Map.Entry<String, Object> entry : camone_log.entrySet()) {
					versions.add(entry.getKey());
					contents.add(String.valueOf(entry.getValue()));
				}
				break;
			}
		}
		new JSONArray(versions);
		new JSONArray(contents);
		
		// list_map에 데이터 추가
		for (int i = 0; i < versions.size(); i++) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("version", versions.get(i));
			map.put("content", (i < contents.size()) ? contents.get(i) : "[내용 없음]");
			list_map.add(map);
		}
		binding.recyclerview1.setAdapter(new Recyclerview1Adapter(list_map));
		binding.recyclerview1.setLayoutManager(new LinearLayoutManager(this));
	}
	
	public class Recyclerview1Adapter extends RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder> {
		ArrayList<HashMap<String, Object>> _data;
		
		public Recyclerview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getLayoutInflater();
			View _v = _inflater.inflate(R.layout.update_log, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new ViewHolder(_v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, final int _position) {
			View _view = _holder.itemView;
			
			final TextView version = _view.findViewById(R.id.version);
			final TextView content = _view.findViewById(R.id.content);
			
			version.setText(Objects.requireNonNull(_data.get(_position).get("version")).toString());
            Objects.requireNonNull(_data.get(_position).get("content")).toString();
            content.setText(Objects.requireNonNull(_data.get(_position).get("content")).toString());
        }
		
		@Override
		public int getItemCount() {
			return _data.size();
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder {
			public ViewHolder(View v) {
				super(v);
			}
		}
	}
}
