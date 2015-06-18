package main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Downloads songs from a list of song titles to the specified directory
 * 
 * @author JFCaron
 *
 */
public class Downloader {

	private static Downloader INSTANCE = new Downloader();
	private static DownloaderListener mListener = null;
	private static String xsongsURL = "http://xsongs.pk/";
	private static String mp3marsURL = "http://www.mp3mars.com/mp3/";
	private static String mp3End = ".mp3";
	private static Boolean stop = false;
	private static int progress = 0;
	private static boolean sHasStarted;
	/**
	 * Download the list of songs to a specific directory
	 * 
	 * @param list
	 */
	public static void downLoad(List<Object> list) {
		if (list != null && list.size() > 0) {
			INSTANCE.mSongs = new String[list.size()];
			for (int i = 0; i < list.size(); i++) {
				INSTANCE.mSongs[i] = (String) list.get(i);
			}
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
	public static long getCurrentFileSize() {
		return INSTANCE.currentFileSize;
	}
	public static int getDownloadSpeed() {
		return INSTANCE.mDownloadSpeed;
	}
	public static int getFailedNumber() {
		return INSTANCE.mSongsNotFoundArray.size();
	}
	public static ArrayList<String> getFailedSongs() {
		return INSTANCE.mSongsNotFoundArray;
	}
	/**
	 * Get the static instance for callbacks
	 * 
	 * @return
	 */
	public static Downloader getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns the progress of download
	 * 
	 * @return [0-100]
	 */
	public static int getProgress() {
		if (INSTANCE.mSongs != null && INSTANCE.mSongs.length > 0) {
			return progress * 100 / INSTANCE.mSongs.length;
		} else {
			return 0;
		}
	}

	public static long getTotalSize() {
		return INSTANCE.totalDownloaded;
	}

	public static boolean hasStarted() {
		return sHasStarted;
	}

	public static void setDir(String dir) {
		INSTANCE.dir = dir;
	}

	/**
	 * Set listener for the downloader
	 * 
	 * @param listener
	 */
	public static void setListener(DownloaderListener listener) {
		if (listener != null) {
			mListener = listener;
		}
	}

	/**
	 * Stop the download process
	 */
	public static void stop() {
		synchronized (INSTANCE) {
			stop = true;
			sHasStarted = false;
			progress = 0;
		}
	}

	/**
	 * 
	 * @param doc
	 * @param classStr
	 * @param regex
	 * @return
	 */
	private static String getDownloadPageUrlMp3Mars(Document doc,
			String classStr, String regex) {
		String url = null;
		if (doc != null) {
			Elements classes = doc.getElementsByClass(classStr);
			if (classes != null && classes.size() > 1) {
				Element downloadUrlElement = classes.get(1)
						.getElementsByAttributeValueMatching("href", regex)
						.first();
				if (downloadUrlElement != null) {
					url = downloadUrlElement.attr("href");
				}
			}
		}
		return url;
	}

	/**
	 * Returns the url of the download page
	 * 
	 * @param body
	 * @return
	 */
	private static String getDownloadPageUrlXSong(Document doc,
			String classStr, String regex) {
		String url = null;
		if (doc != null) {
			Element eClass = doc.getElementsByClass(classStr).first();
			if (eClass != null) {
				Element downloadUrlElement = eClass
						.getElementsByAttributeValueMatching("href", regex)
						.first();
				if (downloadUrlElement != null) {
					url = downloadUrlElement.attr("href");
				}
			}
		}
		return url;
	}

	/**
	 * 
	 * @param doc
	 * @param tag
	 * @param regex
	 * @return
	 */
	private static String getDownloadUrlMp3Mars(Document doc, String tag,
			String regex) {
		String url = null;
		if (doc != null) {
			Element div = doc.select(tag).first();
			if (div != null) {
				String tagBlock = div.toString();
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(tagBlock);
				if (matcher.find()) {
					url = matcher.group(1);
				}
			}
		}
		return url;
	}

	/**
	 * Saves mp3 of the name filename that comes from urlString
	 * 
	 * @param filename
	 * @param urlString
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static Boolean saveUrl(final String filename, final String urlString)
			throws MalformedURLException, IOException {
		File f = new File(filename);
		if (f.exists() && !f.isDirectory())
			return false;
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection httpcon = (HttpURLConnection) url
					.openConnection();
			httpcon.addRequestProperty("User-Agent", "Mozilla/4.76");
			in = new BufferedInputStream(httpcon.getInputStream());
			fout = new FileOutputStream(filename);
			final byte data[] = new byte[4096];
			int count;
			long estimatedTime;

			if (mListener != null) {
				mListener.onUpdateCurrentDownload(filename.substring(
						INSTANCE.dir.length() + 1, filename.length()));
			}

			INSTANCE.currentFileSize = 0;

			long startTime = System.nanoTime();

			while ((count = in.read(data, 0, 4096)) != -1 && !stop) {
				fout.write(data, 0, count);
				synchronized (INSTANCE) {
					INSTANCE.currentFileSize += count;
					INSTANCE.totalDownloaded += count;
					estimatedTime = System.nanoTime() - startTime;
					INSTANCE.mDownloadSpeed = (int) (INSTANCE.currentFileSize
							* 1000000000 / estimatedTime / 1024);
					if (mListener != null) {
						mListener.onUpdateSpeed();
					}
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
	 * start the download
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static void start() throws MalformedURLException, IOException {
		stop = false;
		INSTANCE.mSongsNotFoundArray.clear();
		INSTANCE.totalDownloaded = 0;
		String songTitle;
		Boolean isSongFound;
		synchronized (INSTANCE) {
			sHasStarted = true;
		}
		for (int i = 0; i < INSTANCE.mSongs.length; i++) {
			isSongFound = false;
			synchronized (INSTANCE) {
				if (stop) {
					sHasStarted = false;
					return;
				}
			}
			songTitle = INSTANCE.mSongs[i];

			// search in xsongs
			String songURL = songTitle.replaceAll("[\\&()]", "")
					.replaceAll("-", "").replaceAll("\\s+", " ")
					.replaceAll("\\s", "-").toLowerCase();
			Document searchPageDoc = null;
			try {
				searchPageDoc = Jsoup.parse(new URL(xsongsURL + songURL
						+ ".html"), 10000);
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			}
			if (searchPageDoc != null) {
				String downloadPageURL = getDownloadPageUrlXSong(searchPageDoc,
						"rack", "(http:\\/\\/xsongs\\.pk\\/download-song\\/)");
				if (downloadPageURL != null) {
					/*
					 * if (saveUrl(INSTANCE.dir + "\\" + songTitle + mp3End,
					 * downloadPageURL.replace("download-song", "downloads") +
					 * mp3End)) { isSongFound = true; }
					 */
				}
			}
			// search in mp3mars
			if (!isSongFound) {
				songURL = songTitle.replaceAll("[\\&()-]", "")
						.replaceAll("\\s+", " ").replaceAll("\\s", "+");
				searchPageDoc = null;
				try {
					// handle status 403
					searchPageDoc = Jsoup
							.connect(mp3marsURL + songURL)
							.userAgent(
									"mozilla/5.0 (macintosh; intel mac os x 10_9_2) applewebkit/537.36 (khtml, like gecko) chrome/33.0.1750.152 safari/537.36")
							.get();
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
				}
				if (searchPageDoc != null) {
					String downloadPageURL = getDownloadPageUrlMp3Mars(
							searchPageDoc, "dl", "(http:\\/\\/refs\\.pm\\/)");
					if (downloadPageURL != null) {
						Document downloadPageDoc = null;
						try {
							// handle status 403 again
							downloadPageDoc = Jsoup
									.connect(downloadPageURL)
									.userAgent(
											"mozilla/5.0 (macintosh; intel mac os x 10_9_2) applewebkit/537.36 (khtml, like gecko) chrome/33.0.1750.152 safari/537.36")
									.get();
						} catch (SocketTimeoutException e) {
							e.printStackTrace();
						}
						if (downloadPageDoc != null) {
							String downloadURL = getDownloadUrlMp3Mars(
									downloadPageDoc, "script",
									"((?:http|https)(?::\\/{2}[\\w]+)(?:[\\/|\\.]?)(?:[^\\s\"]*))");
							if (saveUrl(INSTANCE.dir + "\\" + songTitle
									+ mp3End, downloadURL)) {
								isSongFound = true;
							}
						}
					}
				}
			}
			// not found, add to the list
			if (!isSongFound) {
				INSTANCE.mSongsNotFoundArray.add(songTitle);
			}
			if (mListener != null) {
				progress++;
				mListener.onFileDownloaded();
			}
		}
	}

	private String[] mSongs;

	private final ArrayList<String> mSongsNotFoundArray = new ArrayList<String>();

	private String dir;

	private int mDownloadSpeed = 0;

	private long currentFileSize = 0;

	private long totalDownloaded = 0;

	private Downloader() {

	}
}