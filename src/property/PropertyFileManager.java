package property;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import logger.Logger;

public class PropertyFileManager {

	private String TAG = "PropertyFileManager";

	private String fileName = "null";

	public PropertyFileManager(String fileName) {
		Logger.logInfo(TAG, "Restoring properties from " + fileName + ".");

		this.fileName = fileName;
	}

	public void savePropertyValue(String key, String value) {
		Logger.logInfo(TAG, "Saving property " + key + " with value " + value + ".");

		createFileIfDoesntExist();

		PropertiesConfiguration config;
		try {
			config = new PropertiesConfiguration(fileName);
			config.setProperty(key, value);
			config.save();
		} catch (ConfigurationException e) {
			Logger.logError(TAG, "Error while getting property " + key + " from " + fileName + ".", e);
			e.printStackTrace();
		}
	}

	public String getPropertyValue(String key, String defaultValue) {
		Logger.logInfo(TAG, "Getting property " + key + ".");

		createFileIfDoesntExist();

		try {
			PropertiesConfiguration config = new PropertiesConfiguration(fileName);
			String value = (String) config.getProperty(key);

			if (value == null) { //file might be empty
				return defaultValue;
			} else {
				return value;
			}

		} catch (ConfigurationException e) {
			Logger.logError(TAG, "Could not get property value for " + key + ", returning default value. ", e);
			e.printStackTrace();
		}
		return defaultValue;
	}

	private void createFileIfDoesntExist() {
		try {
			// if file already exists will do nothing 
			new File(fileName).createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}