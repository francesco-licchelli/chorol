package it.unibo.tesi.chorol.utils;

public class OutputSettings {
	private static final boolean saveConditions = false;
	private static boolean fullType = false;
	private static boolean saveStdLib = false;

	public static boolean getFullType() {
		return OutputSettings.fullType;
	}

	public static void setFullType(boolean fullType) {
		OutputSettings.fullType = fullType;
	}

	public static void setSaveStdLib(boolean saveStdLib) {
		OutputSettings.saveStdLib = saveStdLib;
	}

	public static boolean shouldSaveStdLib() {
		return OutputSettings.saveStdLib;
	}

	public static boolean shouldSaveConditions() {
		return OutputSettings.saveConditions;
	}
}
