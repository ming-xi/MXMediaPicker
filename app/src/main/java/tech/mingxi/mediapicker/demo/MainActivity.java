package tech.mingxi.mediapicker.demo;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.tbruyelle.rxpermissions3.RxPermissions;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.rxjava3.functions.Consumer;
import tech.mingxi.mediapicker.MXMediaPicker;
import tech.mingxi.mediapicker.models.PickerConfig;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final int REQ_CODE = 1;
	private TextView tv_selected;
	private CheckBox cb_camera;
	private CheckBox cb_multi_select;
	private CheckBox cb_full_path;
	private ImageView iv_first_image;
	private RadioGroup radioGroup;
	private TextInputLayout til_max;
	private int fileType = MXMediaPicker.FILE_TYPE_VIDEO_AND_IMAGE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv_selected = findViewById(R.id.activity_main_text);
		iv_first_image = findViewById(R.id.activity_main_image);
		cb_camera = findViewById(R.id.activity_main_camera);
		cb_multi_select = findViewById(R.id.activity_main_multi_select);
		cb_full_path = findViewById(R.id.activity_main_full_path);
		radioGroup = findViewById(R.id.activity_main_file_type_group);
		til_max = findViewById(R.id.activity_main_max);
		til_max.getEditText().setText(String.valueOf(MXMediaPicker.DEFAULT_MULTI_SELECT_MAX));
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.activity_main_file_type_all) {
					fileType = MXMediaPicker.FILE_TYPE_VIDEO_AND_IMAGE;
				} else if (checkedId == R.id.activity_main_file_type_image) {
					fileType = MXMediaPicker.FILE_TYPE_IMAGE;
				} else if (checkedId == R.id.activity_main_file_type_video) {
					fileType = MXMediaPicker.FILE_TYPE_VIDEO;
				}
			}
		});
		cb_multi_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				til_max.setEnabled(isChecked);
			}
		});
		findViewById(R.id.activity_main_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
				requestPermissions(permissions);
			}
		});
		MXMediaPicker.init(getApplicationContext());
	}

	private void requestPermissions(String... permission) {
		final RxPermissions rxPermissions = new RxPermissions(MainActivity.this);
		//make sure you have permissions
		rxPermissions.request(permission).subscribe(new Consumer<Boolean>() {
			@Override
			public void accept(Boolean granted) {
				if (granted) {
					//permission granted
					goToPicker();
				} else {
					//Denied. show a dialog or something...
				}
			}
		});
	}

	private void goToPicker() {
		MXMediaPicker picker = MXMediaPicker.getInstance();
		//set your own custom holders. If not set, default holders will be used.
//							picker.setFolderHolder(folderHolder);
//							picker.setImageHolder(imageHolder);
//							picker.setVideoHolder(videoHolder);
		boolean multiSelect = cb_multi_select.isChecked();
		PickerConfig pickerConfig = new PickerConfig();
		pickerConfig.setFileType(fileType);
		pickerConfig.setAllowCamera(cb_camera.isChecked());
		pickerConfig.setFolderMode(cb_full_path.isChecked() ? MXMediaPicker.FOLDER_MODE_FULL_PATH : MXMediaPicker.FOLDER_MODE_ONLY_PARENT);
		pickerConfig.setMultiSelect(multiSelect);
		if (multiSelect) {
			try {
				int max = Integer.parseInt(til_max.getEditText().getText().toString());
				if (max > 0) {
					pickerConfig.setMultiSelectMaxCount(max);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		picker.setPickerConfig(pickerConfig);
		picker.chooseImage(MainActivity.this, REQ_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_CODE) {
			String[] selectedUris = MXMediaPicker.getInstance().getSelectedUris(resultCode, data);
			if (selectedUris != null) {
				StringBuilder sb = new StringBuilder();
				for (String uri : selectedUris) {
					sb.append(uri).append("\n");
				}
				tv_selected.setText(String.format("selected uris:\n%s", sb.toString()));
				if (selectedUris.length > 0) {
					iv_first_image.setImageURI(Uri.parse(selectedUris[0]));
				}
			}
		}
	}
}