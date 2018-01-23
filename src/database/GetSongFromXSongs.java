package database;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import downloader.Downloader;

public class GetSongFromXSongs extends SongEngine {

	/**
	 * Returns the url of the download page
	 * 
	 * @param body
	 * @return
	 */
	private static String getDownloadPageUrlXSong(Document doc, String classStr, String regex) {
		String url = null;
		if (doc != null) {
			Element eClass = doc.getElementsByClass(classStr).first();
			if (eClass != null) {
				Element downloadUrlElement = eClass.getElementsByAttributeValueMatching("href", regex).first();
				if (downloadUrlElement != null) {
					url = downloadUrlElement.attr("href");
				}
			}
		}
		return url;
	}

	public boolean search(String songTitle) {
		searchURL = "http://xsongs.pk/";
		if (songTitle == null || songTitle.isEmpty())
			return false;
		String foo = new String();

		Boolean isSongFound = false;
		String songURL = songTitle.replaceAll("[\\&()]", "").replaceAll("-", "").replaceAll("\\s+", " ")
				.replaceAll("\\s", "-").toLowerCase();
		Document searchPageDoc = null;
		try {
			searchPageDoc = Jsoup.parse(new URL(searchURL + songURL + ".html"), 10000);
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (searchPageDoc != null) {
			String downloadPageURL = getDownloadPageUrlXSong(searchPageDoc, "rack",
					"(http:\\/\\/xsongs\\.pk\\/download-song\\/)");
			if (downloadPageURL != null) {
				try {
					if (Downloader.saveUrl(songTitle, downloadPageURL.replace("download-song", "downloads") + ".mp3")) {
						isSongFound = true;
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isSongFound;
	}

}
