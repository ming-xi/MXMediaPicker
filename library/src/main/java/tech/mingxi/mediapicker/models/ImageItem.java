package tech.mingxi.mediapicker.models;

import lombok.Data;

@Data
public class ImageItem implements Item {
	private long id;
	private String uri;
	private String path;
	private String folderPath;
	private String ext;
	private long date;
	private boolean selected = false;
}
