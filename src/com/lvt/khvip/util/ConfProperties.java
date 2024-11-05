package com.lvt.khvip.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ConfProperties {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfProperties.class);
	private static final Properties PROPS = new Properties();

	static {
		String env = System.getProperty("app.env", "");
		if (StringUtils.isNotBlank(env)) {
			env = "-" + env;
		}
		String resource = "/resource/config" + env + ".properties";
		LOGGER.info("Properties path: " + resource);
		try (InputStream input = ConfProperties.class.getResourceAsStream(resource);) {
			PROPS.load(input);
		} catch (Exception ex) {
			LOGGER.error("Failed to load config", ex);
		}
	}

	public static String getProperty(String key) {
		if (key == null || key.isEmpty()) {
			return "";
		}
		return PROPS.getProperty(key);
	}
}
