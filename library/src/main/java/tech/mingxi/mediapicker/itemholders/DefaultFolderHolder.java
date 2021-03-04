package tech.mingxi.mediapicker.itemholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import tech.mingxi.mediapicker.R;
import tech.mingxi.mediapicker.models.FolderItem;
import tech.mingxi.mediapicker.models.ImageItem;
import tech.mingxi.mediapicker.models.Item;
import tech.mingxi.mediapicker.models.VideoItem;

public class DefaultFolderHolder implements FolderHolder {
	@Override
	public int getLayoutId() {
		return R.layout.mxmp_item_folder;
	}

	@Override
	public void displayFolder(View rootView, FolderItem item) {
		((TextView) rootView.findViewById(R.id.mxmp_item_folder_title)).setText(String.format("%s\n(%d)", item.getName(), item.getItems().size()));
		ImageView iv1 = rootView.findViewById(R.id.mxmp_item_folder_preview1);
		ImageView iv2 = rootView.findViewById(R.id.mxmp_item_folder_preview2);
		ImageView iv3 = rootView.findViewById(R.id.mxmp_item_folder_preview3);
		ImageView iv4 = rootView.findViewById(R.id.mxmp_item_folder_preview4);
		List<Item> items = item.getItems();
		Context context = rootView.getContext();
		if (items.size() > 0) {
			load(context, items.get(0), iv1);
			if (items.size() > 1) {
				load(context, items.get(1), iv2);
				if (items.size() > 2) {
					load(context, items.get(2), iv3);
					if (items.size() > 3) {
						load(context, items.get(3), iv4);
					} else {
						clear(context, iv4);
					}
				} else {
					clear(context, iv3);
					clear(context, iv4);
				}
			} else {
				clear(context, iv2);
				clear(context, iv3);
				clear(context, iv4);
			}
		} else {
			clear(context, iv1);
			clear(context, iv2);
			clear(context, iv3);
			clear(context, iv4);
		}
	}

	void clear(Context context, ImageView iv) {
		Glide.with(context).clear(iv);
		iv.setVisibility(View.INVISIBLE);
	}

	void load(Context context, Item subItem, ImageView iv) {
		iv.setVisibility(View.VISIBLE);
		if (subItem instanceof ImageItem) {
			Glide.with(context).load(((ImageItem) subItem).getUri()).error(R.drawable.ic_image).into(iv);
		} else if (subItem instanceof VideoItem) {
			Glide.with(context).load(((VideoItem) subItem).getUri()).error(R.drawable.ic_image).into(iv);
		} else if (subItem instanceof FolderItem) {
			Glide.with(context).clear(iv);
			iv.setImageResource(R.drawable.ic_folder);
		}
	}
}
