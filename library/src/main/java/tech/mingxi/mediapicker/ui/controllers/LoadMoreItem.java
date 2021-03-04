package tech.mingxi.mediapicker.ui.controllers;

public class LoadMoreItem {
	static class Singleton {
		static LoadMoreItem INSTANCE;

		static {
			INSTANCE = new LoadMoreItem();
		}
	}

	public static LoadMoreItem getInstance() {
		return Singleton.INSTANCE;
	}

	private LoadMoreItem() {

	}
}
