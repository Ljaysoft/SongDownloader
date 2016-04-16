package database;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import downloader.Downloader;

public class GetSongFromSourceOne extends SongEngine {

	/**
	 * 
	 * @param doc
	 * @param classStr
	 * @param regex
	 * @return
	 */
	private static String getDownloadPageUrlSourceOne(Document doc,
			String classStr, String regex) {
		String url = null;
		if (doc != null) {
			Elements classes = doc.getElementsByClass(classStr);
			if (classes != null && classes.size() > 1) {
				Element downloadUrlElement = classes.get(1)
						.getElementsByAttributeValueMatching("onclick", regex)
						.first();
				if (downloadUrlElement != null) {
					url = downloadUrlElement.attr("onclick");
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
	private static String getDownloadUrlSourceOne(Document doc, String tag,
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

	public boolean search(String songTitle) {
		Boolean isSongFound = false;
		searchURL = "http://www.myfreemp3.space/mp3/";
		String songURL = songTitle.replaceAll("[\\&()-]", "")
				.replaceAll("\\s+", " ").replaceAll("\\s", "+");
		Document searchPageDoc = null;
		try {
			// handle status 403
			searchPageDoc = Jsoup
					.connect(searchURL + songURL)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
					.timeout(0)
					.get();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (searchPageDoc != null) {
			String downloadPageURL = getDownloadPageUrlSourceOne(searchPageDoc,
					"dw", "(http:\\/\\/unref\\.eu\\/)");
			if (downloadPageURL != null) {
				downloadPageURL = downloadPageURL.substring(13, downloadPageURL.length()-12);
				Document downloadPageDoc = null;
				try {
					// handle status 403 again
					downloadPageDoc = Jsoup
							.connect(downloadPageURL)
							.userAgent(
									"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
							.get();
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (downloadPageDoc != null) {
					String downloadURL = getDownloadUrlSourceOne(downloadPageDoc,
							"script",
							"((?:http|https)(?::\\/{2}[\\w]+)(?:[\\/|\\.]?)(?:[^\\s\"]*))");
					try {
						if (Downloader.saveUrl(songTitle, downloadURL)) {
							isSongFound = true;
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return isSongFound;
	}

}
