package com.lvt.khvip.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

/**
 * @author haitx3
 */

public class MessageProvider {
	private static ResourceBundle bundle;

	static {
		getBundle();
	}

	public static ResourceBundle getBundle() {
		if (bundle == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			bundle = context.getApplication().getResourceBundle(context, "msgs");
		}
		return bundle;
	}

	public static String getValue(String key) {
		String result = "N/A";
		try {
			result = getBundle().getString(key);
		} catch (MissingResourceException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getValue(String key, Object... arguments) {
		String result = "N/A";
		try {
			result = getBundle().getString(key);
			result = MessageFormat.format(result, arguments);
		} catch (MissingResourceException e) {
			e.printStackTrace();
		}
		return result;
	}

}
