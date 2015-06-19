package settings;

import java.util.prefs.Preferences;

public class Settings  {
	private static final Settings INSTANCE = new Settings();	
	private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
	
	public Settings() {
	}
	
	public static void setInPutDir(String inDir) {
		INSTANCE.prefs.put("LAST_INPUT_DIR", inDir);
	}
	
	public static String getInPutDir() {
		return INSTANCE.prefs.get("LAST_INPUT_DIR", "");
	}
	
	public static void setOutPutDir(String outDir) {
		INSTANCE.prefs.put("LAST_OUTPUT_DIR", outDir);
	}
	
	public static String getOutPutDir() {
		return INSTANCE.prefs.get("LAST_OUTPUT_DIR", "");
	}
}
