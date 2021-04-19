package tech.mingxi.mediapicker.models;

import lombok.Data;

@Data
public class ResultItem {
	private String uri;
	/**
	 * Information only. Don't use this path to read file! Use uri instead
	 */
	private String path;
}
