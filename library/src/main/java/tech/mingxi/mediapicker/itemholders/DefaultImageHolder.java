package tech.mingxi.mediapicker.itemholders;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import tech.mingxi.mediapicker.R;
import tech.mingxi.mediapicker.models.ImageItem;

public class DefaultImageHolder implements ImageHolder {
	@Override
	public int getLayoutId() {
		return R.layout.mxmp_item_image;
	}

	@Override
	public void displayImage(View rootView, ImageItem imageItem) {
		Glide.with(rootView.getContext()).load(imageItem.getUri()).error(R.drawable.ic_image).into((ImageView) rootView.findViewById(R.id.mxmp_item_image));
		rootView.findViewById(R.id.include_selected_mask).setVisibility(imageItem.isSelected() ? View.VISIBLE : View.GONE);
	}
}
