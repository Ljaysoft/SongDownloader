package settings;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Saves and fetch application settings
 * 
 * @author JFCaron
 *
 */
public class Settings {
	private static final Settings INSTANCE = new Settings();
	private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());

	private Settings() {
		// do not instanciate
	}

	/**
	 * Get Default input list directory
	 * 
	 * @return
	 */
	public static String getInPutDir() {
		return INSTANCE.prefs.get("LAST_INPUT_DIR", "");
	}

	/**
	 * Get Default output directory
	 * 
	 * @return
	 */
	public static String getOutPutDir() {
		return INSTANCE.prefs.get("LAST_OUTPUT_DIR", "");
	}

	/**
	 * Resets preferences
	 */
	public static void reset() {
		try {
			INSTANCE.prefs.clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set Default input list directory
	 * 
	 * @param inDir
	 */
	public static void setInPutDir(String inDir) {
		INSTANCE.prefs.put("LAST_INPUT_DIR", inDir);
	}

	/**
	 * Set Default output directory
	 * 
	 * @param outDir
	 */
	public static void setOutPutDir(String outDir) {
		INSTANCE.prefs.put("LAST_OUTPUT_DIR", outDir);
	}
}
