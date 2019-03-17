package davi.spire;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.model.ValueRange;

public class SheetUpdater {
	
	private static final String[] COLUMNS = {"type", "enemies", "relics", "turns", "hpChange", "maxHpChange", "hpOnExit", "maxHpOnExit", 
											 "cardsUpgraded", "cardsPurged", "cardPicked", "cardsNotPicked", "potionsObtained", "goldChange", "goldOnExit"};
	
	private static SheetUpdater singletonInstance;

	private RunData currentRun = null;
	private RunData prevRun = null;
	private RunData prevPrevRun = null;
	private File saveFolder;
	private ObjectMapper objectMapper;
	private long lastUpdateTimeModified = Long.MIN_VALUE;
	
	private SheetUpdater() throws JsonParseException, JsonMappingException, IOException {
		saveFolder = new File(Configuration.get().getSaveDirectory());
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		
		final java.util.logging.Logger buggyLogger = java.util.logging.Logger.getLogger(FileDataStoreFactory.class.getName());
		buggyLogger.setLevel(java.util.logging.Level.SEVERE);
	}
	
	public static SheetUpdater get() throws JsonParseException, JsonMappingException, IOException {
		if (singletonInstance != null)
			return singletonInstance;
		
		singletonInstance = new SheetUpdater();
		return singletonInstance;
	}

	public void update() throws IOException, GeneralSecurityException {
		
		long lastModified = Long.MIN_VALUE + 1;
		File newestSave = null;
		for (File f: saveFolder.listFiles()) 
			if (f.lastModified() > lastModified && f.getName().contains("BETA")) {
				lastModified = f.lastModified();
				newestSave = f;
			}
		
		// This makes sure that a full update is only made if the save file is different from last time.
		if (lastUpdateTimeModified == lastModified)
			return;
		else if (lastUpdateTimeModified < lastModified)
			lastUpdateTimeModified = lastModified;
		
		byte[] jsonData = null;
		if (newestSave != null)
			jsonData = Files.readAllBytes(newestSave.toPath());
		
		
		List<RunData> runDataList = new LinkedList<RunData>();
		{
			RunData runData = null;
			if (jsonData != null) {
				runData = objectMapper.readValue(jsonData, RunData.class);
				runData.finalize(underscoreToCapitalCase(newestSave.getName().substring(0, newestSave.getName().indexOf('.'))));
			}

			if (currentRun != null && !runData.equals(currentRun)) {
				prevPrevRun = prevRun;
				prevRun = currentRun;
			}
			currentRun = runData;
			
			if (currentRun != null)
				runDataList.add(currentRun);
			if (prevRun != null) runDataList.add(prevRun);
			if (prevPrevRun != null) runDataList.add(prevPrevRun);
		}
		

		
		for (int runIndex = 0; runIndex < 3; runIndex++) {
			String sheetName = "Floor Stats";
			if (runIndex == 1)
				sheetName = sheetName + " (Previous Run)";
			else if (runIndex > 1)
				sheetName = sheetName + " (" + runIndex + " Runs Previous)";
			
			RunData runData;
			if (runDataList.size() > runIndex)
				runData = runDataList.get(runIndex);
			else
				runData = new RunData();
			

			LinkedList<List<Object>> values = new LinkedList<List<Object>>();
			for (int i=0; i < 56; i++) {
				LinkedList<Object> row = new LinkedList<Object>();
				for (String column : COLUMNS) {
					Object cell = runData.getFloors().get(i).get(column);
					if (cell != null)
						row.add(cell.toString());
					else
						row.add("");
				}
				values.add(row);
			}


			ValueRange floorTable = new ValueRange().setValues(values).setRange(sheetName + "!B7:62");
			ValueRange neowBonus = new ValueRange().setValues(Arrays.asList(Arrays.asList(
					runData.getCharacterName(),
					underscoreToCapitalCase(runData.neowBonus), 
					underscoreToCapitalCase(runData.neowCost))
					)).setRange(sheetName + "!D2:D4").setMajorDimension("COLUMNS");
			ValueRange deckList = new ValueRange().setValues(Arrays.asList(runData.getCards())).setRange(sheetName + "!C69:C168").setMajorDimension("COLUMNS");
			ValueRange bossRelics = new ValueRange().setValues(Arrays.asList(Arrays.asList(runData.getBossRelicsSkipped()))).setRange(sheetName + "!G3:G4").setMajorDimension("COLUMNS");

			SheetWriter.writeToSheet(Arrays.asList(floorTable, neowBonus, deckList, bossRelics));
		}
		
		
		
	}
	
	public static String underscoreToCapitalCase(String underscoreString) {
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
