package davi.spire;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.model.ValueRange;

public class SheetUpdater {
	
	private static final String[] COLUMNS = {"type", "relics", "enemies", "turns", "damage"};
	
	private static SheetUpdater singletonInstance;
	
	private File saveFolder;
	private ObjectMapper objectMapper;
	
	private SheetUpdater() {
		saveFolder = new File(Configuration.get().getSaveDirectory());
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		
		final java.util.logging.Logger buggyLogger = java.util.logging.Logger.getLogger(FileDataStoreFactory.class.getName());
		buggyLogger.setLevel(java.util.logging.Level.SEVERE);
	}
	
	public static SheetUpdater get() {
		if (singletonInstance != null)
			return singletonInstance;
		
		singletonInstance = new SheetUpdater();
		return singletonInstance;
	}

	public void update() {
		
		long lastModified = Long.MIN_VALUE;
		File newestSave = null;
		for (File f: saveFolder.listFiles()) 
			if (f.lastModified() > lastModified && f.getName().contains("BETA")) {
				lastModified = f.lastModified();
				newestSave = f;
			}
		
		byte[] jsonData = null;
		if (newestSave != null)
			try {
				jsonData = Files.readAllBytes(newestSave.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		
		RunData runData = null;
		try {
			runData = objectMapper.readValue(jsonData, RunData.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<Integer, Map<String, Object>> floorsMap = runData.getFloors();
		
		LinkedList<List<Object>> values = new LinkedList<List<Object>>();
		for (int i=1; i < 55; i++) {
			LinkedList<Object> row = new LinkedList<Object>();
			for (String column : COLUMNS) {
				Object cell = floorsMap.get(i).get(column);
				if (cell != null)
					row.add(cell.toString());
				else
					row.add("");
			}
			values.add(row);
		}
		
		LinkedList<Object> cardNameList = new LinkedList<Object>();
		for (Map<String, Object> card: runData.cards) {
			
			String cardName = (String) card.get("id");
			if (cardName.charAt(cardName.length() - 2) == '_')
				cardName = cardName.substring(0, cardName.length() - 2);
			
			int upgrades = (int) card.get("upgrades");
			if (upgrades > 0)
				cardName = cardName + "+";
			if (upgrades > 1)
				cardName = cardName + upgrades;
			
			cardNameList.add(cardName);
		}
			for (int i=cardNameList.size(); i < 100; i++)
				cardNameList.add("");
		
		ValueRange floorTable = new ValueRange().setValues(values).setRange("Floor Stats!B7:60");
		ValueRange neowBonus = new ValueRange().setValues(Arrays.asList(Arrays.asList(
						underscoreToCapitalCase(newestSave.getName().substring(0, newestSave.getName().indexOf('.'))),
						underscoreToCapitalCase(runData.neowBonus), 
						underscoreToCapitalCase(runData.neowCost))
				)).setRange("Floor Stats!D2:D4").setMajorDimension("COLUMNS");
		ValueRange deckList = new ValueRange().setValues(Arrays.asList(cardNameList)).setRange("Deck List!A1:A100").setMajorDimension("COLUMNS");
		
		try {
			SheetWriter.writeToSheet(Arrays.asList(floorTable, neowBonus, deckList));
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
	}
	
	private static String underscoreToCapitalCase(String underscoreString) {
		String output = "";
		for (int i=0; i < underscoreString.length(); i++) {
			char ch = underscoreString.charAt(i);
			if (i == 0 || (Character.isLetter(ch) && underscoreString.charAt(i - 1) == '_'))
				output = output + Character.toUpperCase(ch);
			else if (Character.isLetter(ch))
				output = output + Character.toLowerCase(ch);
			else if (ch == '_')
				output = output + ' ';
		}
		
		
		return output;
	}
}
