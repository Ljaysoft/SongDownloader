package database;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import downloader.Downloader;

public class GetSongFromMP3goear extends SongEngine {

	/**
	 * 
	 * @param doc
	 * @param classStr
	 * @param regex
	 * @return
	 */
	private static String getDownloadPageUrlMP3goear(Document doc,
			String classStr, String regex) {
		String url = null;
		if (doc != null) {
			Elements classes = doc.getElementsByClass(classStr);
			if (classes != null && classes.size() > 1) {
				Element downloadUrlElement = classes.get(1)
						.getElementsByAttributeValueMatching("data-href", regex)
						.first();
				if (downloadUrlElement != null) {
					url = downloadUrlElement.attr("data-href");
				}
			}
		}
		return url;
	}

	public boolean search(String songTitle) {
		searchURL = "http://mp3goear.com/mp3/";
		if (songTitle == null || songTitle.isEmpty())
			return false;

		Boolean isSongFound = false;
		String songURL = songTitle.replaceAll("[\\&()\\.]", "")
				.replaceAll("-", "").replaceAll("\\s+", " ")
				.replaceAll("\\s", "-").toLowerCase();
		Document searchPageDoc = null;
		try {
			searchPageDoc = Jsoup.connect(searchURL + songURL + ".html")
					.userAgent(	"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
					.get();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (searchPageDoc != null) {
			String downloadPageURL = getDownloadPageUrlMP3goear(searchPageDoc,
					"btn btn-inverse PlayerLink", "(http:\\/\\/mp3goear\\.com\\/sc\\/files\\/)");
			if (downloadPageURL != null) {
				try {
					if (Downloader.saveUrl(
							songTitle,
							downloadPageURL)) {
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
