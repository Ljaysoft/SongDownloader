package main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Downloads songs from a list of song titles to the specified directory
 * @author JFCaron
 *
 */
public class Downloader {

	private static Downloader INSTANCE = new Downloader();
	private static DownloaderListener mListener = null;
	private String[] mSongs;
	private String dir;
	private static String xsongsURL = "http://xsongs.pk/";
	private static String mp3End = ".mp3";
	private static Boolean stop = false;
	private static int progress = 0;

	private Downloader() {

	}

	/**
	 * Get the static instance for callbacks
	 * @return
	 */
	public static Downloader getInstance() {
		return INSTANCE;
	}

	public static void setListener(DownloaderListener listener) {
		if (listener != null) {
			mListener = listener;
		}
	}

	/**
	 * Returns the progress of download
	 * @return [0-100]
	 */
	public static int getProgress() {
		if (INSTANCE.mSongs != null && INSTANCE.mSongs.length > 0) {
			return progress * 100 / INSTANCE.mSongs.length;
		}
		else {
			return 0;
		}
	}

	/**
	 * Stop the download process
	 */
	public static void stop() {
		synchronized (INSTANCE) {
			stop = true;
			progress = 0;
		}
	}

	/**
	 * Download the list of songs to a specific directory
	 * @param list
	 * @param dir
	 */
	public static void downLoad(List<Object> list, String dir) {
		if (list != null && list.size() > 0) {
			INSTANCE.mSongs = new String[list.size()];
			for (int i = 0; i < list.size(); i++) {
				INSTANCE.mSongs[i] = (String) list.get(i);
			}
			INSTANCE.dir = dir;
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			thread.start();
		} else {
			return;
		}
	}

	/**
	 * start the download
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static void start() throws MalformedURLException, IOException {
		stop = false;
		String song;
		for (int i = 0; i < INSTANCE.mSongs.length; i++) {
			synchronized (INSTANCE) {
				if (stop) {
					return;
				}
			}
			song = INSTANCE.mSongs[i];
			String songURL = song.replaceAll("[\\&()]", "").replaceAll("-", "")
					.replaceAll("\\s+", " ").replaceAll("\\s", "-")
					.toLowerCase();
			Document searchPageDoc = null;
			try {
				searchPageDoc = Jsoup.connect(xsongsURL + songURL + ".html").get();
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			}
			if (searchPageDoc != null) {
				String downloadPageURL = getDownloadURL(searchPageDoc,"rack", "(http:\\/\\/xsongs\\.pk\\/download-song\\/)");
				if (downloadPageURL != null) {
					if (saveUrl(INSTANCE.dir + "\\" + song + mp3End, downloadPageURL.replace("download-song", "downloads") + mp3End)) {
						progress++;
						if (mListener != null) {
							mListener.onFileDownloaded();
						}
					}
				}
			}
		}
	}

	/**
	 * Saves mp3 of the name filename that comes from urlString
	 * @param filename
	 * @param urlString
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static Boolean saveUrl(final String filename, final String urlString)
			throws MalformedURLException, IOException {
		File f = new File(filename);
		if(f.exists() && !f.isDirectory())
			return false;
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(urlString).openStream());
			fout = new FileOutputStream(filename);
			final byte data[] = new byte[1024];
			int count;
			synchronized (INSTANCE) {
				while ((count = in.read(data, 0, 1024)) != -1 && !stop) {						
					fout.write(data, 0, count);						
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (fout != null) {
				fout.close();
			}
		}
		return true;
	}

	/**
	 * Returns the url of the download page
	 * @param body
	 * @return
	 */
	private static String getDownloadURL(Document body, String sClass, String regex) {
		String url = null;
		if (body != null) {
			Element eClass = body.getElementsByClass(sClass).first();
			if (eClass != null) {
				Element downloadurlelement = eClass.getElementsByAttributeValueMatching("href", regex).first();
				if (downloadurlelement != null) {
					url = downloadurlelement.attr("href");
				}
			}
		}
		return url;
	}
}