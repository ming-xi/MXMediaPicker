package tech.mingxi.mediapicker.models;

import java.util.List;

import lombok.Data;

@Data
public class FolderItem implements Item {
	private String name;
	private String path;
	private String folderPath;
	private long date;
	private List<Item> items;

	@Override
	public boolean isSelected() {
		return false;
	}

	@Override
	public void setSelected(boolean selected) {

	}
}
