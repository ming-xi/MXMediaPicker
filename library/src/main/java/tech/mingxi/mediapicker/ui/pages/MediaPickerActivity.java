package tech.mingxi.mediapicker.ui.pages;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import tech.mingxi.mediapicker.MXMediaPicker;
import tech.mingxi.mediapicker.R;
import tech.mingxi.mediapicker.models.FolderItem;
import tech.mingxi.mediapicker.models.ImageItem;
import tech.mingxi.mediapicker.models.Item;
import tech.mingxi.mediapicker.models.VideoItem;
import tech.mingxi.mediapicker.ui.controllers.ItemAdapter;

public class MediaPickerActivity extends AppCompatActivity {
	private static final String TAG = MediaPickerActivity.class.getSimpleName();
	private static final int REQ_CODE_CAMERA = 1;
	private static final int REQ_CODE_RECORDER = 2;
	public static final String KEY_FOLDER_MODE = "KEY_FOLDER_MODE";
	public static final String KEY_FILE_TYPE = "KEY_FILE_TYPE";
	public static final String KEY_MULTI_SELECT = "KEY_MULTI_SELECT";
	public static final String KEY_IS_CAMERA_ENABLED = "KEY_IS_CAMERA_ENABLED";
	public static final String KEY_MULTI_SELECT_MAX_COUNT = "KEY_MULTI_SELECT_MAX_COUNT";
	private RecyclerView recyclerView;
	private View progressBar;

	private int mode = MXMediaPicker.FOLDER_MODE_ONLY_PARENT;
	private int fileType = MXMediaPicker.FILE_TYPE_VIDEO_AND_IMAGE;
	private boolean isMultiSelect = false;
	private boolean isCameraEnabled = false;
	private int multiSelectMaxCount = MXMediaPicker.DEFAULT_MULTI_SELECT_MAX;
	private List<Item> items = new ArrayList<>();
	private ItemAdapter adapter;
	private Stack<FolderItem> stack = new Stack<>();
	private List<Item> selectedItems = new ArrayList<>();
	private FolderItem root;
	//for camera to write file
	private Uri fileUri;
	private String filePath;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mxmp_activity_picker);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		recyclerView = findViewById(R.id.mxmp_activity_picker_recycler);
		progressBar = findViewById(R.id.mxmp_activity_picker_progressbar);
		Intent it = getIntent();
		mode = it.getIntExtra(KEY_FOLDER_MODE, mode);
		fileType = it.getIntExtra(KEY_FILE_TYPE, fileType);
		isMultiSelect = it.getBooleanExtra(KEY_MULTI_SELECT, isMultiSelect);
		isCameraEnabled = it.getBooleanExtra(KEY_IS_CAMERA_ENABLED, isCameraEnabled);
		multiSelectMaxCount = it.getIntExtra(KEY_MULTI_SELECT_MAX_COUNT, multiSelectMaxCount);
		setupRecycler();
		MXMediaPicker.getInstance().loadData(new MXMediaPicker.OnDataLoadCompleteListener() {
			@Override
			public void onSuccess(List<Item> list) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						items.addAll(list);
						adapter.notifyItemRangeInserted(0, items.size());
						progressBar.setVisibility(View.GONE);
						root = new FolderItem();
						root.setItems(list);
						root.setName(getResources().getString(R.string.root));
						stack.push(root);
						changeTitle(root.getName());
					}
				});
			}

			@Override
			public void onFailure(Throwable throwable) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MediaPickerActivity.this, getResources().getString(R.string.load_data_error), Toast.LENGTH_LONG).show();
						progressBar.setVisibility(View.GONE);
					}
				});
			}
		});
	}

	private void setupRecycler() {
		adapter = new ItemAdapter(this, new ItemAdapter.OnClickListener() {
			@Override
			public void onClick(Item item) {
				if (item instanceof ImageItem || item instanceof VideoItem) {
					if (isMultiSelect) {
						if (selectedItems.contains(item)) {
							selectedItems.remove(item);
							item.setSelected(false);
						} else {
							if (selectedItems.size() >= multiSelectMaxCount) {
								Toast.makeText(MediaPickerActivity.this, getResources().getString(R.string.reach_selection_max, multiSelectMaxCount), Toast.LENGTH_LONG).show();
								return;
							}
							selectedItems.add(item);
							item.setSelected(true);
						}
						invalidateOptionsMenu();
						adapter.notifyItemChanged(items.indexOf(item));
					} else {
						selectedItems.add(item);
						setResultAndFinish();
					}
				} else if (item instanceof FolderItem) {
					stack.push((FolderItem) item);
					changeTitle(((FolderItem) item).getName());
					changeFolder(((FolderItem) item));
				}
			}
		});
		adapter.submitList(items);
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

	}

	private void changeFolder(FolderItem newItem) {
		int size = items.size();
		items.clear();
		adapter.notifyItemRangeRemoved(0, size);
		items.addAll(newItem.getItems());
		size = items.size();
		adapter.notifyItemRangeInserted(0, size);
	}

	@Override
	public void onBackPressed() {
		if (stack.peek() != root) {
			stack.pop();
			FolderItem folderItem = stack.peek();
			changeTitle(folderItem.getName());
			changeFolder(folderItem);
			return;
		}
		super.onBackPressed();
	}

	private void changeTitle(String title) {
		getSupportActionBar().setTitle(title);
	}

	private void setResultAndFinish() {
		List<String> uris = new ArrayList<>();
		List<String> paths = new ArrayList<>();
		for (Item selectedItem : selectedItems) {
			if (selectedItem instanceof ImageItem) {
				uris.add(((ImageItem) selectedItem).getUri());
				paths.add(((ImageItem) selectedItem).getPath());
			} else if (selectedItem instanceof VideoItem) {
				uris.add(((VideoItem) selectedItem).getUri());
				paths.add(((VideoItem) selectedItem).getPath());
			}
		}
		Intent data = new Intent();
		data.putExtra(MXMediaPicker.KEY_SELECTED_URIS, uris.toArray(new String[0]));
		data.putExtra(MXMediaPicker.KEY_SELECTED_PATHS, paths.toArray(new String[0]));
		setResult(RESULT_OK, data);
		finish();
	}


	private void goToVideoRecorder() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		// Create the File where the photo should go
		File file = null;
		try {
			file = createVideoFile();
		} catch (IOException ex) {
			// Error occurred while creating the File
			Toast.makeText(MediaPickerActivity.this, getResources().getString(R.string.start_camera_failed), Toast.LENGTH_LONG).show();
		}
		// Continue only if the File was successfully created
		if (file != null) {
			filePath = file.getAbsolutePath();
			fileUri = FileProvider.getUriForFile(this,
					getPackageName() + ".mxmediapicker.fileprovider",
					file);
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
				takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
			}
			startActivityForResult(takePictureIntent, REQ_CODE_RECORDER);
		}
	}

	private void goToCamera() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Create the File where the photo should go
		File file = null;
		try {
			file = createImageFile();
		} catch (IOException ex) {
			// Error occurred while creating the File
			Toast.makeText(MediaPickerActivity.this, getResources().getString(R.string.start_camera_failed), Toast.LENGTH_LONG).show();
		}
		// Continue only if the File was successfully created
		if (file != null) {
			filePath = file.getAbsolutePath();
			fileUri = FileProvider.getUriForFile(this,
					getPackageName() + ".mxmediapicker.fileprovider",
					file);
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
				takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
			}
			startActivityForResult(takePictureIntent, REQ_CODE_CAMERA);
		}
	}

	private File createImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = getFilesDir();
		File image = File.createTempFile(
				imageFileName,
				".jpg",
				storageDir
		);
		return image;
	}

	private File createVideoFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "MP4" + timeStamp + "_";
		File storageDir = getFilesDir();
		File image = File.createTempFile(
				imageFileName,
				".mp4",
				storageDir
		);
		return image;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_CODE_CAMERA && resultCode == RESULT_OK) {
			ImageItem imageItem = new ImageItem();
			imageItem.setUri(fileUri.toString());
			imageItem.setPath(filePath);
			selectedItems.add(imageItem);
			setResultAndFinish();
		} else if (requestCode == REQ_CODE_RECORDER && resultCode == RESULT_OK) {
			VideoItem videoItem = new VideoItem();
			videoItem.setUri(fileUri.toString());
			videoItem.setPath(filePath);
			selectedItems.add(videoItem);
			setResultAndFinish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_picker, menu);
		tintMenuIcons(menu, getResources().getColor(R.color.MXColorOnPrimary), R.id.menu_picker_camera);
		return true;
	}

	private void tintMenuIcons(Menu menu, int color, int... ids) {
		for (int id : ids) {
			Drawable drawable = menu.findItem(id).getIcon();
			drawable = DrawableCompat.wrap(drawable);
			DrawableCompat.setTint(drawable, color);
			menu.findItem(id).setIcon(drawable);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		//check if any camera is available
		boolean hasCameraFeature = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
		menu.findItem(R.id.menu_picker_camera).setVisible(isCameraEnabled && hasCameraFeature);
		MenuItem menuDone = menu.findItem(R.id.menu_picker_done);
		menuDone.setVisible(isMultiSelect);
		menuDone.setEnabled(!selectedItems.isEmpty());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.menu_picker_camera) {
			if (fileType == MXMediaPicker.FILE_TYPE_VIDEO) {
				goToVideoRecorder();
			} else {
				goToCamera();
			}
			return true;
		} else if (itemId == R.id.menu_picker_done) {
			setResultAndFinish();
			return true;
		} else if (itemId == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
