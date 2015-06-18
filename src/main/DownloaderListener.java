package main;

public interface DownloaderListener {
	void onFileDownloaded();

	void onUpdateCurrentDownload(String title);

	void onUpdateSpeed();
}
