package it.unibo.tesi.chorol.utils;

public final class Constants {
	public static final String JOLIE_EXTENSION_REGEX = "\\.i?ol$";
	public static final String LINK_TYPE = "LINK";
	public static final String UNDEF_TYPE = "UNDEFINED";
	public static final String STRING_FORMAT_OPERATION = "%s\\n%s@%s:\\n%s";
	static final Integer MAX_VECTOR_SIZE = (int) (Math.pow(2, 31) - 1);

	private Constants() {
	}
}
