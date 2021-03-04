package tech.mingxi.mediapicker.ui.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import lombok.Setter;
import tech.mingxi.mediapicker.util.NetworkUtil;

public class ListRefreshController {
	private static final String TAG = ListRefreshController.class.getSimpleName();
	private static final int MODE_RECYCLER = 0;
	private static final int MODE_SCROLLVIEW = 1;

	private View noNetworkPlaceholder;
	private View emptyListPlaceholder;
	private int initialPage;
	private int page;
	private boolean canLoadMore = true;
	private RecyclerView recycler;
	private NestedScrollView scroller;
	private SwipeRefreshLayout refresher;
	private OnRefreshListener onRefreshListener;
	private OnLoadMoreListener onLoadMoreListener;
	private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
	private final int mode;
	@Setter
	private int headerItemCount = 0;

	public ListRefreshController(int initialPage, RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout, View noNetworkPlaceholder, View emptyListPlaceholder) {
		this.initialPage = initialPage;
		this.recycler = recyclerView;
		this.refresher = swipeRefreshLayout;
		this.noNetworkPlaceholder = noNetworkPlaceholder;
		this.emptyListPlaceholder = emptyListPlaceholder;
		page = initialPage;
		mode = MODE_RECYCLER;
		setup();
	}

	public ListRefreshController(int initialPage, NestedScrollView scrollView, SwipeRefreshLayout swipeRefreshLayout, View noNetworkPlaceholder, View emptyListPlaceholder) {
		this.initialPage = initialPage;
		this.scroller = scrollView;
		this.refresher = swipeRefreshLayout;
		this.noNetworkPlaceholder = noNetworkPlaceholder;
		this.emptyListPlaceholder = emptyListPlaceholder;
		page = initialPage;
		mode = MODE_SCROLLVIEW;
		setup();
	}

	private void setup() {
		if (mode == MODE_RECYCLER) {
			RecyclerView.LayoutManager layoutManager = recycler.getLayoutManager();
			if (layoutManager instanceof GridLayoutManager) {
				endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener((GridLayoutManager) layoutManager, 1) {
					@Override
					public void onLoadMore(int currentPage, int totalItemsCount, RecyclerView view) {
						loadMore();
					}
				};
			} else if (layoutManager instanceof LinearLayoutManager) {
				endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) layoutManager, 1) {
					@Override
					public void onLoadMore(int currentPage, int totalItemsCount, RecyclerView view) {
						loadMore();
					}
				};
			} else if (layoutManager instanceof StaggeredGridLayoutManager) {
				endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener((StaggeredGridLayoutManager) layoutManager, 1) {
					@Override
					public void onLoadMore(int currentPage, int totalItemsCount, RecyclerView view) {
						loadMore();
					}
				};
			}
			endlessRecyclerViewScrollListener.setupLoadMore(recycler);
			recycler.addOnScrollListener(endlessRecyclerViewScrollListener);
		} else if (mode == MODE_SCROLLVIEW) {
			//不能load more
		}
		SwipeRefreshLayout.OnRefreshListener internalOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshInternal(true);
			}
		};
		refresher.setOnRefreshListener(internalOnRefreshListener);
	}

	public void finishRefreshingEffect() {
		updateEmptyListPlaceholderVisibility(isListEmpty());
		refresher.setRefreshing(false);
	}

	private boolean isListEmpty() {
		boolean empty = false;
		if (mode == MODE_RECYCLER) {
			empty = recycler.getAdapter().getItemCount() == 0 + headerItemCount;
		} else if (mode == MODE_SCROLLVIEW) {
			empty = ((ViewGroup) scroller.getChildAt(0)).getChildCount() == 0;
		}
		return empty;
	}

	public void finishLoadingMoreEffect() {
		if (mode != MODE_RECYCLER) {
			return;
		}
		RecyclerView.Adapter recyclerAdapter = recycler.getAdapter();
		if (recyclerAdapter instanceof ListAdapter) {
			ListAdapter adapter = (ListAdapter) recyclerAdapter;
			List<?> items = adapter.getCurrentList();
			int index = items.indexOf(LoadMoreItem.getInstance());
			if (index != -1) {
				items.remove(index);
				adapter.notifyItemRemoved(index);
				endlessRecyclerViewScrollListener.setLoadMoreItemDisplaying(false);
			}
		}
	}

	public void refresh() {
		refresher.setRefreshing(true);
		refreshInternal(false);
	}

	private void refreshInternal(boolean triggeredByUser) {
		resetPage();
		if (endlessRecyclerViewScrollListener != null) {
			endlessRecyclerViewScrollListener.resetToInitialState();
		}
		if (onRefreshListener != null) {
			onRefreshListener.onRefresh(triggeredByUser);
		}
		if (!NetworkUtil.isNetworkAvailable() && isListEmpty()) {
			updateNoNetworkPlaceholderVisibility(true);
		}
	}

	public void loadMore() {
		if (onLoadMoreListener != null) {
			onLoadMoreListener.onLoadMore(page);
		}
	}

	public boolean canLoadMore() {
		return canLoadMore;
	}

	public void setCanLoadMore(boolean canLoadMore) {
		this.canLoadMore = canLoadMore;
		if (mode != MODE_RECYCLER) {
			throw new IllegalStateException("not in recycler mode!");
		}
		recycler.removeOnScrollListener(endlessRecyclerViewScrollListener);
		if (canLoadMore) {
			recycler.addOnScrollListener(endlessRecyclerViewScrollListener);
		}
	}

	public void resetPage() {
		page = initialPage;
	}

	public void increasePage() {
		page += 1;
	}

	public int getInitialPage() {
		return initialPage;
	}

	public View getNoNetworkPlaceholder() {
		return noNetworkPlaceholder;
	}

	public View getEmptyListPlaceholder() {
		return emptyListPlaceholder;
	}

	public void setNoNetworkPlaceholder(View noNetworkPlaceholder) {
		this.noNetworkPlaceholder = noNetworkPlaceholder;
	}

	public void setEmptyListPlaceholder(View emptyListPlaceholder) {
		this.emptyListPlaceholder = emptyListPlaceholder;
	}

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;
	}

	public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
		this.onLoadMoreListener = onLoadMoreListener;
	}

	public void updateNoNetworkPlaceholderVisibility(boolean visible) {
		if (visible) {
			if (noNetworkPlaceholder.getVisibility() == View.VISIBLE) {
				return;
			}
			noNetworkPlaceholder.animate().alpha(1).setDuration(250).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					super.onAnimationStart(animation);
					noNetworkPlaceholder.setVisibility(View.VISIBLE);
				}
			}).start();
		} else {
			if (noNetworkPlaceholder.getVisibility() == View.GONE) {
				return;
			}
			noNetworkPlaceholder.animate().alpha(0).setDuration(250).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					noNetworkPlaceholder.setVisibility(View.GONE);
				}
			}).start();
		}
	}

	public void updateEmptyListPlaceholderVisibility(boolean visible) {
		if (emptyListPlaceholder == null) {
			return;
		}
		if (visible) {
			if (emptyListPlaceholder.getVisibility() == View.VISIBLE) {
				return;
			}
			emptyListPlaceholder.animate().alpha(1).setDuration(250).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					super.onAnimationStart(animation);
					emptyListPlaceholder.setVisibility(View.VISIBLE);
				}
			}).start();
		} else {
			if (emptyListPlaceholder.getVisibility() == View.GONE) {
				return;
			}
			emptyListPlaceholder.animate().alpha(0).setDuration(250).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					emptyListPlaceholder.setVisibility(View.GONE);
				}
			}).start();
		}
	}

	public void fixItemCount(int deltaCount) {
		if (mode != MODE_RECYCLER) {
			throw new IllegalStateException("not in recycler mode!");
		}
		endlessRecyclerViewScrollListener.fixItemCount(deltaCount);
	}

	public interface OnRefreshListener {
		void onRefresh(boolean triggeredByUser);
	}

	public interface OnLoadMoreListener {
		void onLoadMore(int page);
	}

	public static abstract class SpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

		private final RecyclerView recycler;

		public SpanSizeLookup(RecyclerView recycler) {
			this.recycler = recycler;
		}

		@Override
		public int getSpanSize(int position) {
			RecyclerView.LayoutManager layoutManager = recycler.getLayoutManager();
			if (layoutManager instanceof GridLayoutManager) {
				RecyclerView.Adapter recyclerAdapter = recycler.getAdapter();
				if (recyclerAdapter instanceof ListAdapter) {
					ListAdapter adapter = (ListAdapter) recyclerAdapter;
					List<?> items = adapter.getCurrentList();
					if (position > 0 && position < items.size()) {
						if (items.get(position) instanceof LoadMoreItem) {
							return ((GridLayoutManager) layoutManager).getSpanCount();
						}
					}
				}
			}
			return getItemSpanSize(position);
		}

		public abstract int getItemSpanSize(int position);
	}
}
