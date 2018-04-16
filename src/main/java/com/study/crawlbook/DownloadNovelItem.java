package com.study.crawlbook;

public class DownloadNovelItem {
	private String number;
	private String title;
	private String url;
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String toString() {
		return "DownloadNovelItem [number=" + number + ", title=" + title + ", url=" + url + "]";
	}
}
