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

public class GetSongFromMP3Mars extends SongEngine {

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

	public boolean search(String songTitle) {
		Boolean isSongFound = false;
		searchURL = "http://www.mp3mars.com/mp3/";
		String songURL = songTitle.replaceAll("[\\&()-]", "")
				.replaceAll("\\s+", " ").replaceAll("\\s", "+");
		Document searchPageDoc = null;
		try {
			// handle status 403
			searchPageDoc = Jsoup
					.connect(searchURL + songURL)
					.userAgent(
							"mozilla/5.0 (macintosh; intel mac os x 10_9_2) applewebkit/537.36 (khtml, like gecko) chrome/33.0.1750.152 safari/537.36")
					.get();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (searchPageDoc != null) {
			String downloadPageURL = getDownloadPageUrlMp3Mars(searchPageDoc,
					"dl", "(http:\\/\\/refs\\.pm\\/)");
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
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (downloadPageDoc != null) {
					String downloadURL = getDownloadUrlMp3Mars(downloadPageDoc,
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
