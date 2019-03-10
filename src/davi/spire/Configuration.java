package davi.spire;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Configuration {
	
	final private static String CONFIG_PATH = "config.json";
	
	private static Configuration instance;
	
	private long updatePeriod;
	private String saveDirectory;
	private String spreadsheetId;
	
	private Configuration() {
	}
	
	public static Configuration get() throws JsonParseException, JsonMappingException, IOException {
		
		if (instance != null)
			return instance;
		
		ObjectMapper objectMapper = new ObjectMapper();
		byte[] jsonData = null;
		jsonData = Files.readAllBytes(Paths.get(CONFIG_PATH));
		instance = objectMapper.readValue(jsonData, Configuration.class);
		return instance;
	}

	public long getUpdatePeriod() {
		return updatePeriod;
	}

	public String getSaveDirectory() {
		return saveDirectory;
	}

	public String getSpreadsheetId() {
		return spreadsheetId;
	}

	public void setUpdatePeriod(long updatePeriod) {
		this.updatePeriod = updatePeriod;
	}

	public void setSaveDirectory(String saveDirectory) {
		this.saveDirectory = saveDirectory;
	}

	public void setSpreadsheetId(String spreadsheetId) {
		this.spreadsheetId = spreadsheetId;
	}
}
