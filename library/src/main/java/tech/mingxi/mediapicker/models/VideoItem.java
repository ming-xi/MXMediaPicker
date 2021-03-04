package tech.mingxi.mediapicker.models;

import lombok.Data;

@Data
public class VideoItem implements Item {
	private long id;
	private String uri;
	private String path;
	private String folderPath;
	private String ext;
	private long date;
	private long duration;
	private boolean selected = false;
}
