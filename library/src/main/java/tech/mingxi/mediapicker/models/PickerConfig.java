package tech.mingxi.mediapicker.models;

import lombok.Data;
import tech.mingxi.mediapicker.MXMediaPicker;

@Data
public class PickerConfig {
	private int folderMode = MXMediaPicker.FOLDER_MODE_ONLY_PARENT;
	private boolean allowCamera = false;
	private boolean multiSelect = false;
	private int multiSelectMaxCount = MXMediaPicker.DEFAULT_MULTI_SELECT_MAX;
	private int fileType = MXMediaPicker.FILE_TYPE_VIDEO_AND_IMAGE;
}
