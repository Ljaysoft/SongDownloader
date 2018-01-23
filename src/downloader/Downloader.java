package downloader;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import settings.Settings;
import database.*;

/**
 * Downloads songs from a list of song titles to the specified directory
 * 
 * @author JFCaron
 *
 */
public class Downloader {

	private static Downloader INSTANCE = new Downloader();
	private static DownloaderListener mListener = null;
	private static int progress = 0;
	private static boolean sHasStarted;
	private static Boolean stop = false;
	private long currentFileSize = 0;
	private int mDownloadSpeed = 0;
	private String[] mSongs;
	private final ArrayList<String> mSongsNotFoundArray = new ArrayList<String>();
	private String outputDir = Settings.getOutPutDir();
	private long totalBytesDownloaded = 0;
	private long totalSongsDownloaded = 0;
	private int alreadyOwn = 0;

	private Downloader() {

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
		INSTANCE.totalBytesDownloaded = 0;
		INSTANCE.totalSongsDownloaded = 0;
		String songTitle;
		Boolean isSongFound = false;
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

			// search in mp3mars
			isSongFound = new GetSongFromSourceOne().search(songTitle);

			// search in MP3goer broken
			// if (!isSongFound) {
			//isSongFound = new GetSongFromMP3goear().search(songTitle);

			// search in xsongs broken
			// if (!isSongFound) {
			// isSongFound = new GetSongFromXSongs().search(songTitle);
			// }			
			progress++;
			// not found, add to the list
			if (!isSongFound) {
				INSTANCE.mSongsNotFoundArray.add(songTitle);
			}

			if (mListener != null) {
				mListener.onFileDownloaded();
			}
		}
		// write failed songs into logfile
		if (!INSTANCE.mSongsNotFoundArray.isEmpty()) {
			try {
				String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
				File failedSongFile = new File(INSTANCE.outputDir + "\\" + "failed_song_" + timeLog + ".log");
				BufferedWriter writer = new BufferedWriter(new FileWriter(failedSongFile));
				for (String song : INSTANCE.mSongsNotFoundArray) {
					writer.write(song);
					writer.newLine();
				}
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void clearFails() {
		synchronized (INSTANCE) {
			INSTANCE.mSongsNotFoundArray.clear();
		}
	}

	/**
	 * Download the list of songs to a specific directory
	 * 
	 * @param list
	 */
	public static void downLoadFromList(List<Object> list) {
		if (list != null && list.size() > 0) {
			INSTANCE.mSongsNotFoundArray.clear();
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

	public static int getNumberOwned() {
		return INSTANCE.alreadyOwn;
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
	 * Get the output directory path
	 * 
	 * @return
	 */
	public static String getOutputDir() {
		return INSTANCE.outputDir;
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
		return INSTANCE.totalBytesDownloaded;
	}

	public static long getDownloadCount() {
		return INSTANCE.totalSongsDownloaded;
	}

	public static boolean hasStarted() {
		return sHasStarted;
	}

	public static boolean isReady() {
		return !INSTANCE.outputDir.isEmpty();
	}

	/**
	 * Saves mp3 of the name filename that comes from urlString
	 * 
	 * @param filename
	 * @param urlString
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static Boolean saveUrl(final String filename, final String urlString)
			throws MalformedURLException, IOException {
		String fullName = INSTANCE.outputDir + "\\" + filename + ".mp3";
		File f = new File(fullName);
		if (f.exists() && f.isFile()) {
			INSTANCE.alreadyOwn++;
			return true;
		} else {
			INSTANCE.totalSongsDownloaded++;
		}
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			URL url = new URL(urlString);
			HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
			httpcon.addRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0");
			httpcon.setReadTimeout(60 * 1000);
			httpcon.connect();
			in = new BufferedInputStream(httpcon.getInputStream());
			fout = new FileOutputStream(fullName);
			final byte data[] = new byte[4096];
			int count;
			long estimatedTime;

			if (mListener != null) {
				mListener.onUpdateCurrentDownload(filename);
			}

			INSTANCE.currentFileSize = 0;

			long startTime = System.nanoTime();

			while ((count = in.read(data, 0, 4096)) != -1 && !stop) {
				fout.write(data, 0, count);
				synchronized (INSTANCE) {
					INSTANCE.currentFileSize += count;
					INSTANCE.totalBytesDownloaded += count;
					estimatedTime = System.nanoTime() - startTime;
					INSTANCE.mDownloadSpeed = (int) (INSTANCE.currentFileSize * 1000000000 / estimatedTime / 1024);
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
	 * Set listener for the downloader
	 * 
	 * @param listener
	 */
	public static void setListener(DownloaderListener listener) {
		if (listener != null) {
			mListener = listener;
		}
	}

	public static void setOutputDir(String outputDir) {
		if (outputDir == null)
			return;
		Settings.setOutPutDir(outputDir);
		INSTANCE.outputDir = outputDir;
	}

	/**
	 * Stop the download process
	 */
	public static void stop() {
		synchronized (INSTANCE) {
			stop = true;
			sHasStarted = false;
			progress = 0;
			INSTANCE.mDownloadSpeed = 0;
			INSTANCE.totalBytesDownloaded = 0;
			INSTANCE.currentFileSize = 0;
			INSTANCE.alreadyOwn = 0;
		}
	}
}