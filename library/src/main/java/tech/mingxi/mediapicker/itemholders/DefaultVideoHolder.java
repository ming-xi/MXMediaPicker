package tech.mingxi.mediapicker.itemholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import tech.mingxi.mediapicker.R;
import tech.mingxi.mediapicker.models.VideoItem;

public class DefaultVideoHolder implements VideoHolder {

	@Override
	public int getLayoutId() {
		return R.layout.mxmp_item_video;
	}

	@Override
	public void displayVideo(View rootView, VideoItem videoItem) {
		Glide.with(rootView.getContext()).load(videoItem.getUri()).error(R.drawable.ic_image).into((ImageView) rootView.findViewById(R.id.mxmp_item_video_thumb));
		((TextView) rootView.findViewById(R.id.mxmp_item_video_duration)).setText(milliSecondsToTimer(videoItem.getDuration()));
		rootView.findViewById(R.id.include_selected_mask).setVisibility(videoItem.isSelected() ? View.VISIBLE : View.GONE);
	}

	private String milliSecondsToTimer(long milliseconds) {
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there
		if (hours > 0) {
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		// return timer string
		return finalTimerString;
	}
}
