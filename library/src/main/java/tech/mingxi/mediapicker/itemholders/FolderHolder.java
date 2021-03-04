package tech.mingxi.mediapicker.itemholders;

import android.view.View;

import tech.mingxi.mediapicker.models.FolderItem;

public interface FolderHolder extends ItemHolder {
	void displayFolder(View rootView, FolderItem item);
}
