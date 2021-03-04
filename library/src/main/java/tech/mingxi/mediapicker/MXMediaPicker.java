package tech.mingxi.mediapicker;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import tech.mingxi.mediapicker.itemholders.DefaultFolderHolder;
import tech.mingxi.mediapicker.itemholders.DefaultImageHolder;
import tech.mingxi.mediapicker.itemholders.DefaultVideoHolder;
import tech.mingxi.mediapicker.itemholders.FolderHolder;
import tech.mingxi.mediapicker.itemholders.ImageHolder;
import tech.mingxi.mediapicker.itemholders.VideoHolder;
import tech.mingxi.mediapicker.models.FolderItem;
import tech.mingxi.mediapicker.models.ImageItem;
import tech.mingxi.mediapicker.models.Item;
import tech.mingxi.mediapicker.models.PickerConfig;
import tech.mingxi.mediapicker.models.VideoItem;
import tech.mingxi.mediapicker.ui.pages.MediaPickerActivity;

public class MXMediaPicker {
	private static final String TAG = MXMediaPicker.class.getSimpleName();

	public static final int FOLDER_MODE_ONLY_PARENT = 0;
	public static final int FOLDER_MODE_FULL_PATH = 1;
	public static final int FILE_TYPE_VIDEO_AND_IMAGE = 0;
	public static final int FILE_TYPE_VIDEO = 1;
	public static final int FILE_TYPE_IMAGE = 2;
	public static final int DEFAULT_MULTI_SELECT_MAX = 9;
	public static final String KEY_SELECTED_URIS = "KEY_SELECTED_URIS";
	private static Context appContext;
	private static final FolderHolder DEFAULT_FOLDER_HOLDER = new DefaultFolderHolder();
	private static final ImageHolder DEFAULT_IMAGE_HOLDER = new DefaultImageHolder();
	private static final VideoHolder DEFAULT_VIDEO_HOLDER = new DefaultVideoHolder();
	private List<Item> items;
	private ImageHolder imageHolder;
	private VideoHolder videoHolder;
	private FolderHolder folderHolder;
	private PickerConfig pickerConfig;
	private final ExecutorService executorService;

	private static class Singleton {
		static MXMediaPicker INSTANCE;

		static void init(Context context) {
			INSTANCE = new MXMediaPicker(context);
		}
	}

	public static MXMediaPicker getInstance() {
		return Singleton.INSTANCE;
	}

	public static void init(Context context) {
		appContext = context;
		Singleton.init(appContext);
	}


	private MXMediaPicker(Context context) {
		items = Collections.synchronizedList(new ArrayList<>());
		executorService = Executors.newFixedThreadPool(3);

	}

	public void loadData(OnDataLoadCompleteListener listener) {
		List<Item> list = Collections.synchronizedList(new ArrayList<>());
		ArrayList<Callable<Void>> tasks = new ArrayList<>();


		Callable<Void> videoTask = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				loadVideoData(list);
				Log.i(TAG, "loadVideoData end");
				return null;
			}
		};
		Callable<Void> imageTask = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				loadImageData(list);
				Log.i(TAG, "loadImageData end");
				return null;
			}
		};
		if (pickerConfig.getFileType() == FILE_TYPE_VIDEO_AND_IMAGE) {
			tasks.add(videoTask);
			tasks.add(imageTask);
		} else if (pickerConfig.getFileType() == FILE_TYPE_VIDEO) {
			tasks.add(videoTask);
		} else if (pickerConfig.getFileType() == FILE_TYPE_IMAGE) {
			tasks.add(imageTask);
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					executorService.invokeAll(tasks);
					Log.i(TAG, "loadData end");
					generateFileTree(list);
					listener.onSuccess(items);
				} catch (Exception e) {
					listener.onFailure(e);
				}
			}
		}).start();
	}

	public List<Item> getItems() {
		return items;
	}

	private void generateFileTree(List<Item> list) {
		items.clear();
		HashMap<String, String> nameMap = new HashMap<>();
		HashMap<String, List<Item>> map = new HashMap<>();
		for (Item item : list) {
			String folderPath = item.getFolderPath();
			List<Item> itemsInFolder = map.get(folderPath.toLowerCase());
			if (itemsInFolder == null) {
				nameMap.put(folderPath.toLowerCase(), folderPath);
				itemsInFolder = new ArrayList<>();
				map.put(folderPath.toLowerCase(), itemsInFolder);
			}
			itemsInFolder.add(item);
		}
		int mode = pickerConfig.getFolderMode();
		if (mode == FOLDER_MODE_FULL_PATH) {
			HashMap<String, FolderItem> folderMap = new HashMap<>();
			Set<Map.Entry<String, List<Item>>> entries = map.entrySet();
			FolderItem folderItem = null;
			FolderItem lastFolderItem = null;
			for (Map.Entry<String, List<Item>> entry : entries) {
				List<Item> itemList = entry.getValue();
				if (itemList.isEmpty()) {
					continue;
				}
				String path = entry.getKey();
				path = nameMap.get(path);
				Log.i(TAG, "folder = " + path);
				String[] segments = path.split("/", 0);

				String currentPath = path;
				folderItem = null;
				lastFolderItem = null;
				for (int i = segments.length - 1; i >= 0; i--) {
					String segment = segments[i];
					if (segment.trim().isEmpty()) {
						continue;
					}
					folderItem = folderMap.get(currentPath);
					if (folderItem == null) {
						folderItem = new FolderItem();
						folderItem.setPath(currentPath);
						folderItem.setName(segment);
						folderItem.setFolderPath(getFolderPathFromPath(currentPath));
						folderItem.setItems(new ArrayList<>());
						folderMap.put(currentPath, folderItem);
					}
					currentPath = currentPath.replace(segment, "");
					if (currentPath.endsWith("/")) {
						currentPath = currentPath.substring(0, currentPath.length() - 1);
					}
					if (i == segments.length - 1) {
						folderItem.getItems().addAll(entry.getValue());
					} else {
						if (!folderItem.getItems().contains(lastFolderItem)) {
							folderItem.getItems().add(lastFolderItem);
						}
					}
					lastFolderItem = folderItem;
				}
				Log.i(TAG, String.format("items name= %s size = %d", folderItem.getName(), folderItem.getItems().size()));
			}
			while (folderItem.getItems().size() == 1 && folderItem.getItems().get(0) instanceof FolderItem) {
				folderItem = (FolderItem) folderItem.getItems().get(0);
			}
			items.addAll(folderItem.getItems());
			sortByDate(items);
		} else if (mode == FOLDER_MODE_ONLY_PARENT) {
			Set<Map.Entry<String, List<Item>>> entries = map.entrySet();
			for (Map.Entry<String, List<Item>> entry : entries) {
				List<Item> itemList = entry.getValue();
				if (itemList.isEmpty()) {
					continue;
				}
				String path = entry.getKey();
				path = nameMap.get(path);
				Log.i(TAG, "folder = " + path);
				FolderItem folderItem = new FolderItem();
				folderItem.setPath(path);
				String name = path;
				if (name.contains("/") && !name.endsWith("/")) {
					name = name.substring(name.lastIndexOf("/") + 1);
				}
				folderItem.setName(name);
				sortByDate(itemList);
				folderItem.setDate(itemList.get(0).getDate());
				folderItem.setItems(itemList);
				folderItem.setFolderPath(getFolderPathFromPath(path));
				items.add(folderItem);
			}
			sortByDate(items);
		}
	}

	private void sortByDate(List<Item> itemList) {
		for (Item item : itemList) {
			if (item instanceof FolderItem) {
				Log.i(TAG, "sortByDate  " + ((FolderItem) item).getPath());
				sortByDate(((FolderItem) item).getItems());
				((FolderItem) item).setDate(((FolderItem) item).getItems().get(0).getDate());
			}
		}
		Collections.sort(itemList, new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				if (o1 instanceof FolderItem ^ o2 instanceof FolderItem) {
					return (o1 instanceof FolderItem && !(o2 instanceof FolderItem)) ? -1 : 1;
				} else {
					return Long.compare(o2.getDate(), o1.getDate());
				}
			}
		});
	}

	private String getFolderPathFromPath(String folderPath) {
		if (folderPath.contains("/")) {
			folderPath = folderPath.substring(0, folderPath.lastIndexOf("/"));
		} else {
			folderPath = appContext.getResources().getString(R.string.root);
		}
		return folderPath;
	}

	private void loadVideoData(List<Item> list) {
		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] projection = {
				MediaStore.Video.VideoColumns._ID,
				MediaStore.Video.VideoColumns.DATA,
				MediaStore.Video.VideoColumns.DATE_TAKEN,
				MediaStore.Video.VideoColumns.DURATION,

		};
		Cursor c = appContext.getContentResolver().query(uri, projection, null, null, MediaStore.Video.VideoColumns.DATE_TAKEN + " desc");
		if (c != null) {
			while (c.moveToNext()) {
				VideoItem item = new VideoItem();
				item.setId(c.getLong(c.getColumnIndex(MediaStore.Video.VideoColumns._ID)));
				item.setUri(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, c.getInt(c.getColumnIndex(MediaStore.Video.VideoColumns._ID))).toString());
				item.setPath(c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns.DATA)));
				if (!item.getPath().contains(".")) {
					continue;
				}
				String ext = item.getPath().substring(item.getPath().lastIndexOf(".")).replace(".", "").toLowerCase();
				item.setExt(ext);
				item.setFolderPath(item.getPath().substring(0, item.getPath().lastIndexOf("/")));
				item.setDate(c.getLong(c.getColumnIndex(MediaStore.Video.VideoColumns.DATE_TAKEN)));
				item.setDuration(c.getLong(c.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)));
				list.add(item);
//				Log.i(TAG, "item=" + item);
			}
			Log.i(TAG, "load video end");
			c.close();
		}
	}

	private void loadImageData(List<Item> list) {
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] projection = {
				MediaStore.Images.ImageColumns._ID,
				MediaStore.Images.ImageColumns.DATA,
				MediaStore.Images.ImageColumns.DATE_TAKEN,
				MediaStore.Images.ImageColumns.DURATION,
		};
		List<String> allowedFormats = Arrays.asList("jpg", "jpeg", "png", "gif");
		Cursor c = appContext.getContentResolver().query(uri, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " desc");
		if (c != null) {
			while (c.moveToNext()) {
				ImageItem item = new ImageItem();
				item.setId(c.getLong(c.getColumnIndex(MediaStore.Images.ImageColumns._ID)));
				item.setUri(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, c.getInt(c.getColumnIndex(MediaStore.Images.ImageColumns._ID))).toString());
				item.setPath(c.getString(c.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
				if (!item.getPath().contains(".")) {
					continue;
				}
				String ext = item.getPath().substring(item.getPath().lastIndexOf(".")).replace(".", "").toLowerCase();
				if (!allowedFormats.contains(ext)) {
					continue;
				}
				item.setExt(ext);
				item.setFolderPath(item.getPath().substring(0, item.getPath().lastIndexOf("/")));
				item.setDate(c.getLong(c.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)));
				list.add(item);
//				Log.i(TAG, "item=" + item);
			}
			Log.i(TAG, "load image end");
			c.close();
		}
	}

	public void chooseImage(AppCompatActivity activity, int requestCode) {
		Intent it = new Intent(activity, MediaPickerActivity.class);
		prepareIntentFromConfig(it);
		activity.startActivityForResult(it, requestCode);
	}

	public void chooseImage(Fragment fragment, int requestCode) {
		Intent it = new Intent(fragment.getContext(), MediaPickerActivity.class);
		prepareIntentFromConfig(it);
		fragment.startActivityForResult(it, requestCode);
	}

	public String[] getSelectedUris(int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			return data.getStringArrayExtra(KEY_SELECTED_URIS);
		} else {
			return null;
		}
	}

	private void prepareIntentFromConfig(Intent it) {
		if (pickerConfig != null) {
			if (pickerConfig.isMultiSelect() && pickerConfig.isAllowCamera()) {
				throw new IllegalArgumentException("cannot use camera in multi-selection mode");
			}
			it.putExtra(MediaPickerActivity.KEY_FOLDER_MODE, pickerConfig.getFolderMode());
			it.putExtra(MediaPickerActivity.KEY_FILE_TYPE, pickerConfig.getFileType());
			it.putExtra(MediaPickerActivity.KEY_MULTI_SELECT, pickerConfig.isMultiSelect());
			it.putExtra(MediaPickerActivity.KEY_IS_CAMERA_ENABLED, pickerConfig.isAllowCamera());
			it.putExtra(MediaPickerActivity.KEY_MULTI_SELECT_MAX_COUNT, pickerConfig.getMultiSelectMaxCount());
		}
	}

	public void setPickerConfig(PickerConfig pickerConfig) {
		this.pickerConfig = pickerConfig;
	}

	public void setImageHolder(ImageHolder imageHolder) {
		this.imageHolder = imageHolder;
	}

	public void setVideoHolder(VideoHolder videoHolder) {
		this.videoHolder = videoHolder;
	}

	public void setFolderHolder(FolderHolder folderHolder) {
		this.folderHolder = folderHolder;
	}

	public ImageHolder getImageHolder() {
		return imageHolder == null ? DEFAULT_IMAGE_HOLDER : imageHolder;
	}

	public VideoHolder getVideoHolder() {
		return videoHolder == null ? DEFAULT_VIDEO_HOLDER : videoHolder;
	}

	public FolderHolder getFolderHolder() {
		return folderHolder == null ? DEFAULT_FOLDER_HOLDER : folderHolder;
	}

	public interface OnDataLoadCompleteListener {
		void onSuccess(List<Item> items);

		void onFailure(Throwable throwable);
	}
}
