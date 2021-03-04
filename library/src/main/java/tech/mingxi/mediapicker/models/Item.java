package tech.mingxi.mediapicker.models;

public interface Item {
	long getDate();

	String getFolderPath();

	boolean isSelected();

	void setSelected(boolean selected);
}
