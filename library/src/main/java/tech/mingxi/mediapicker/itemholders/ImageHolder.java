package tech.mingxi.mediapicker.itemholders;

import android.view.View;

import tech.mingxi.mediapicker.models.ImageItem;

public interface ImageHolder extends ItemHolder {
	void displayImage(View rootView, ImageItem imageItem);
}
