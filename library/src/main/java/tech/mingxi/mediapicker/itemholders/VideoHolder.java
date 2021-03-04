package tech.mingxi.mediapicker.itemholders;

import android.view.View;

import tech.mingxi.mediapicker.models.VideoItem;

public interface VideoHolder extends ItemHolder {
	void displayVideo(View rootView, VideoItem videoItem);
}
