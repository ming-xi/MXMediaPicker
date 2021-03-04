package tech.mingxi.mediapicker.ui.controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import tech.mingxi.mediapicker.MXMediaPicker;
import tech.mingxi.mediapicker.R;
import tech.mingxi.mediapicker.itemholders.FolderHolder;
import tech.mingxi.mediapicker.itemholders.ImageHolder;
import tech.mingxi.mediapicker.itemholders.ItemHolder;
import tech.mingxi.mediapicker.itemholders.VideoHolder;
import tech.mingxi.mediapicker.models.FolderItem;
import tech.mingxi.mediapicker.models.ImageItem;
import tech.mingxi.mediapicker.models.Item;
import tech.mingxi.mediapicker.models.VideoItem;

public class ItemAdapter extends ListAdapter<Item, RecyclerView.ViewHolder> {
	private static final String TAG = ItemAdapter.class.getSimpleName();

	private static final int TYPE_UNKNOWN = -1;
	private static final int TYPE_FOLDER = 0;
	private static final int TYPE_IMAGE = 1;
	private static final int TYPE_VIDEO = 2;
	private final LayoutInflater layoutInflater;
	private final FolderHolder folderHolder;
	private final ImageHolder imageHolder;
	private final VideoHolder videoHolder;
	private final OnClickListener onClickListener;

	public ItemAdapter(Context context, OnClickListener onClickListener) {
		super(new DiffUtil.ItemCallback<Item>() {
			@Override
			public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
				return false;
			}

			@Override
			public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
				return false;
			}
		});
		this.onClickListener = onClickListener;
		layoutInflater = LayoutInflater.from(context);
		folderHolder = MXMediaPicker.getInstance().getFolderHolder();
		imageHolder = MXMediaPicker.getInstance().getImageHolder();
		videoHolder = MXMediaPicker.getInstance().getVideoHolder();
	}

	@Override
	public int getItemViewType(int position) {
		Item item = getItem(position);
		if (item instanceof FolderItem) {
			return TYPE_FOLDER;
		} else if (item instanceof ImageItem) {
			return TYPE_IMAGE;
		} else if (item instanceof VideoItem) {
			return TYPE_VIDEO;
		}
		return -1;
	}

	private ItemHolder getHolder(int type) {
		switch (type) {
			case TYPE_FOLDER: {
				return folderHolder;
			}
			case TYPE_IMAGE: {
				return imageHolder;
			}
			case TYPE_VIDEO: {
				return videoHolder;
			}
			case TYPE_UNKNOWN:
			default: {
				return null;
			}
		}
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ItemHolder holder = getHolder(viewType);
		View view;
		if (holder == null) {
			view = layoutInflater.inflate(R.layout.mxmp_item_unknown, parent, false);
		} else {
			view = layoutInflater.inflate(holder.getLayoutId(), parent, false);
		}
		return new ViewHolder(view, onClickListener);
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		((ViewHolder) holder).setItem(getItem(position));
		ItemHolder itemHolder = getHolder(getItemViewType(position));
		if (itemHolder != null) {
			Item item = getItem(position);
			if (itemHolder instanceof FolderHolder) {
				((FolderHolder) itemHolder).displayFolder(holder.itemView, ((FolderItem) item));
			} else if (itemHolder instanceof ImageHolder) {
				((ImageHolder) itemHolder).displayImage(holder.itemView, ((ImageItem) item));
			} else if (itemHolder instanceof VideoHolder) {
				((VideoHolder) itemHolder).displayVideo(holder.itemView, ((VideoItem) item));
			}
		}
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		Item item;

		ViewHolder(View itemView, OnClickListener onClickListener) {
			super(itemView);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickListener.onClick(item);
				}
			});
		}

		public void setItem(Item item) {
			this.item = item;
		}
	}

	public interface OnClickListener {
		void onClick(Item item);
	}
}
