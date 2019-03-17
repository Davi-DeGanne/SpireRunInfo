package davi.spire;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class RunData {
	
	private static final List<String> STARTING_RELICS = Arrays.asList("Cracked Core", "Burning Blood", "Ring of the Snake");
	private static final Map<Character, String> FLOOR_TYPES_BY_CHAR = createFloorTypeMap();
	private static Map<Character, String> createFloorTypeMap() {
		Map<Character, String> map = new HashMap<Character, String>();
		map.put('M', "Enemy");
		map.put('E', "Elite");
		map.put('B', "Boss");
		map.put('$', "Merchant");
		map.put('R', "Rest");
		map.put('T', "Treasure");
		map.put('?', "Event");
		map.put('C', "Boss Chest");
		map.put('D', "The Door");
		return map;
	}
	/*
	private static final String[] IRONCLAD_STARTING_DECK = 
		{"Strike_R", "Strike_R", "Strike_R", "Strike_R", "Strike_R", "Defend_R", "Defend_R", "Defend_R", "Defend_R", "Bash"};
	private static final String[] SILENT_STARTING_DECK = 
		{"Strike_G", "Strike_G", "Strike_G", "Strike_G", "Strike_G", "Defend_G", "Defend_G", "Defend_G", "Defend_G", "Defend_G", "Survivor", "Neutralize"};
	private static final String[] DEFECT_STARTING_DECK = 
		{"Strike_B", "Strike_B", "Strike_B", "Strike_B", "Defend_B", "Defend_B", "Defend_B", "Defend_B", "Zap", "Dualcast"};
	*/
	
	public String neowBonus = "";
	public String neowCost = "";
	public List<String> metricItemsPurchased;
	public List<Number> metricItemPurchaseFloors;
	public List<String> metricItemsPurged;
	public List<Number> metricItemsPurgedFloors;
	public List<String> relics;
	public int ascensionLevel;
	public long seed;

	private LinkedList<Object> cards;
//	private LinkedList<Object> cardsInferred;
	private Map<Integer, Map<String, Object>> floors = new TreeMap<Integer, Map<String, Object>>();
	private String[] bossRelicsSkipped = {"", ""};
	private String characterName;

	public RunData() {
		for (int floor=0; floor < 56; floor++)
			floors.put(floor, new HashMap<String, Object>());
		/*
		cardsInferred = new LinkedList<Object>();
		cardsInferred.addAll(Arrays.asList(IRONCLAD_STARTING_DECK));
		cardsInferred.addAll(Arrays.asList(SILENT_STARTING_DECK));
		cardsInferred.addAll(Arrays.asList(DEFECT_STARTING_DECK));
		*/
	}

	public void setMetricDamageTaken(List<Map<String, Object>> mdt) {
		
		for (Map<String, Object> floorInfo: mdt) {
			int floorNumber = ((Number) floorInfo.get("floor")).intValue();
			floorInfo.remove("floor");
			floors.get(floorNumber).putAll(floorInfo);
		}
	}
	
	public void setMetricPathPerFloor(List<String> mppf) {
		int floor = 1;
		for (String str: mppf) {
			Character ch = null;
			if (str != null)
				ch = str.charAt(0);
			else if (floor == 17 || floor == 34)
				ch = 'C';
			else if (floor == 51 || floor == 52)
				ch = 'D';
			else
				System.out.println("Why are you still here?");
			floors.get(floor).put("type", FLOOR_TYPES_BY_CHAR.get(ch));
			floor++;
		}
	}
	
	public void setMetricRelicsObtained(List<Map<String, Object>> mro) {
		
		for (Map<String, Object> relicMap: mro) {
			String newRelicString = relicMap.get("key").toString();
			if (newRelicString.contains("Egg 2"))
				newRelicString = newRelicString.substring(0, newRelicString.length() - 2);
			
			updateStringInFloorMap(floors.get(((Number) relicMap.get("floor")).intValue()), "relics", newRelicString);
		}
	}
	
	private void intValuePerFloor(List<Number> list, String intName) {
		
		if (list.size() == 0)
			return;
		
		int valueOnExit = list.get(0).intValue();
		floors.get(1).put(intName + "OnExit", valueOnExit);
		
		for (int i=1; i < list.size(); i++) {
			valueOnExit = list.get(i).intValue();
			floors.get(i+1).put(intName + "Change", valueOnExit - list.get(i-1).intValue());
			floors.get(i+1).put(intName + "OnExit", valueOnExit);
		}
	}
	
	public void setMetricCurrentHpPerFloor(List<Number> mchpf) {
		intValuePerFloor(mchpf, "hp");
	}
	
	public void setMetricMaxHpPerFloor(List<Number> mmhpf) {
		intValuePerFloor(mmhpf, "maxHp");
	}
	
	public void setMetricGoldPerFloor(List<Number> mgpf) {
		intValuePerFloor(mgpf, "gold");
	}
	
	public void setMetricCardChoices(List<CardChoice> mcc) {
		for (CardChoice cc: mcc) {
			
			Map<String, Object> floorMap = floors.get(cc.floor);
			
//			cardsInferred.add(cc.picked);
			updateStringInFloorMap(floorMap, "cardPicked", cc.picked.replace("1", ""), ';');
			updateStringInFloorMap(floorMap, "cardsNotPicked", cc.notPicked.toString().replaceAll("[1\\[\\]]", ""), ';');
				
		}
	}
	
	public void setMetricPotionsObtained(List<Map<String, Object>> mpo) {
		for (Map<String, Object> map: mpo)
			updateStringInFloorMap(floors.get(((Number) map.get("floor")).intValue()), "potionsObtained", map.get("key").toString());
	}
	
	public void setMetricBossRelics(List<Map<String, Object>> mbr) {
		int i = 0;
		for (Map<String, Object> map: mbr) {
			floors.get((i+1)*17).put("relics", map.get("picked"));
			bossRelicsSkipped[i] = map.get("not_picked").toString().replaceAll("[\\[\\]]", "");
			i++;
		}
	}
	
	public void setMetricCampfireChoices(List<Map<String, Object>> mcc) {
		for (Map<String, Object> map: mcc) {
			Map<String, Object> floorMap = floors.get(((Number) map.get("floor")).intValue());
			String restAction = SheetUpdater.underscoreToCapitalCase(map.get("key").toString());
			floorMap.put("enemies", restAction);
			if (restAction.equals("Smith")) {
				String cardUpgraded = map.get("data").toString();
				updateStringInFloorMap(floorMap, "cardsUpgraded", cardUpgraded);
//				cardsInferred.remove(cardUpgraded);
//				cardsInferred.add(cardUpgraded + "+");
			}
		}
	}
	
	public void setMetricEventChoices(List<Event> mec) {
		for (Event e: mec) {
			Map<String, Object> floorMap = floors.get(e.floor);
			
			floorMap.put("enemies", e.eventName + ": " + e.playerChoice);
			
			if (e.cardsObtained != null) {
				updateStringInFloorMap(floorMap, "cardPicked", e.cardsObtained.toString().replaceAll("[\\[\\]]", ""));
//				for (String cardObtained: e.cardsObtained)
//					cardsInferred.add(cardObtained);
			}
			
			List<String> totalCardsRemoved = new ArrayList<String>();
			if (e.cardsRemoved != null)
				totalCardsRemoved.addAll(e.cardsRemoved);
			if (e.cardsTransformed != null)
				totalCardsRemoved.addAll(e.cardsTransformed);
			if (!totalCardsRemoved.isEmpty()) {
				updateStringInFloorMap(floorMap, "cardsPurged", totalCardsRemoved.toString().replaceAll("[\\[\\]]|_G|_B|_R", ""));
//				for (String cardRemoved: totalCardsRemoved)
//					cardsInferred.remove(cardRemoved);
			}
			if (e.cardsUpgraded != null) {
				updateStringInFloorMap(floorMap, "cardsUpgraded", e.cardsUpgraded.toString().replaceAll("[\\[\\]]|_G|_B|_R", ""));
//				for (String cardUpgraded: e.cardsUpgraded) {
//					cardsInferred.remove(cardUpgraded);
//					cardsInferred.add(cardUpgraded + "+");
//				}
			}
			
			if (e.relicsObtained != null)
				updateStringInFloorMap(floorMap, "relics", e.relicsObtained.toString().replaceAll("[\\[\\]]", ""));
		}
	}
	
	public void setCards(List<Map<String, Object>> cards) {
		this.cards = new LinkedList<Object>();
		for (Map<String, Object> card: cards) {
			
			String cardName = (String) card.get("id");
			
			int upgrades = ((Number) card.get("upgrades")).intValue();
			if (upgrades > 0)
				cardName = cardName + "+";
			if (upgrades > 1)
				cardName = cardName + upgrades;
			
			this.cards.add(cardName);
		}
	}
	
	public void finalize(String characterName) {
		// Set character name
		if (!characterName.contains("The"))
			characterName = "The " + characterName;
		this.characterName = characterName;
		
		// Initialize local variables
		int startingMaxHp = 0;
		int startingHp;
		Map<String, Object> floor1 = floors.get(1);
		switch (this.characterName) {
		case "The Ironclad":
			startingMaxHp = 80;
//			for (String str: SILENT_STARTING_DECK) cardsInferred.remove(str);
//			for (String str: DEFECT_STARTING_DECK) cardsInferred.remove(str);
			break;
		case "The Silent"  : 
			startingMaxHp = 70; 
//			for (String str: IRONCLAD_STARTING_DECK) cardsInferred.remove(str);
//			for (String str: DEFECT_STARTING_DECK) cardsInferred.remove(str);
			break;
		case "The Defect"  : 
			startingMaxHp = 75; 
//			for (String str: IRONCLAD_STARTING_DECK) cardsInferred.remove(str);
//			for (String str: SILENT_STARTING_DECK) cardsInferred.remove(str);
			break;
		}
		
		// Figure out actual starting HP and Max HP based on ascension level
		if (ascensionLevel > 5) {
			if (ascensionLevel > 13)
				startingMaxHp = startingMaxHp - startingMaxHp/16;
			startingHp = startingMaxHp - startingMaxHp/10;
		} else
			startingHp = startingMaxHp;
		
		// Calculate and store how much HP and Max HP was gained/lost on floor one, using values defined above
		if (floor1.containsKey("maxHpOnExit")) 
			floor1.put("maxHpChange", ((Number) floor1.get("maxHpOnExit")).intValue() - startingMaxHp);
		if (floor1.containsKey("hpOnExit")) 
			floor1.put("hpChange", ((Number) floor1.get("hpOnExit")).intValue() - startingHp);
		if (floor1.containsKey("goldOnExit")) 
			floor1.put("goldChange", ((Number) floor1.get("goldOnExit")).intValue() - 100);
		
		// Figure out what items have been purchased, and sort them into the correct lists
		for (int i=0; i < metricItemsPurchased.size(); i++) {
			String itemPurchased = metricItemsPurchased.get(i);
			Map<String, Object> floorMap = floors.get(metricItemPurchaseFloors.get(i).intValue());
			if (relics.contains(itemPurchased))
				updateStringInFloorMap(floorMap, "relics", itemPurchased);
			else if (itemPurchased.matches("Potion|Brew|LiquidBronze|SneckoOil|EssenceOfSteel"))
				updateStringInFloorMap(floorMap, "potionsObtained", itemPurchased);
			else {
				updateStringInFloorMap(floorMap, "cardPicked", itemPurchased);
//				cardsInferred.add(itemPurchased);
			}
		}
		
		// Figure out which cards have been removed and where
		for (int i=0; i < metricItemsPurged.size(); i++) {
			String itemPurged = metricItemsPurged.get(i);
		//	cardsInferred.remove(itemPurged);
			itemPurged.replaceAll("_G|_B|_R", "");
			Map<String, Object> floorMap = floors.get(metricItemsPurgedFloors.get(i).intValue());
			floorMap.put("cardsPurged", itemPurged);
		}
		
		// Check if Neow bonus provided a relic, and if so, save that relic in the data structure
		if (neowBonus.contains("RELIC")) 
			if (STARTING_RELICS.contains(relics.get(0)) && floors.containsKey(0))
				floors.get(0).put("relics", relics.get(1));
			else if (floors.containsKey(0))
				if (floors.get(0).get("relics") == null)
					floors.get(0).put("relics", relics.get(0));
				else
					floors.get(0).put("relics", relics.get(0) + ", " + floors.get(0).get("relics"));
		
		/*
		if (neowBonus.contains("REMOVE") || neowBonus.contains("TRANSFORM") || neowBonus.equals("UPGRADE_CARD")) {
			List<Object> cardsNotPresent = new LinkedList<Object>(cardsInferred);
			for (Object o: cards)
				cardsNotPresent.remove(o);
			String floorMapKey = "cardsPurged";
			if (neowBonus.equals("UPGRADE_CARD")) floorMapKey = "cardsUpgraded";
			updateStringInFloorMap(floors.get(0), floorMapKey, cardsNotPresent.toString().replaceAll("[\\[\\]]|_G|_B|_R", ""));
		}

		if (neowBonus.equals("ONE_RANDOM_RARE_CARD") || neowBonus.equals("RANDOM_COLORLESS") || neowBonus.contains("TRANSFORM") || neowCost.equals("CURSE")) {
			List<Object> extraCardsPresent = new LinkedList<Object>(cards);
			for (Object o: cardsInferred)
				extraCardsPresent.remove(o);
			updateStringInFloorMap(floors.get(0), "cardPicked", extraCardsPresent.toString().replaceAll("[\\[\\]]|_G|_B|_R", ""));
		}
		*/
		
		// Pad decklist so that old data in the spreadsheet is overwritten with blank cells
		for (int i=this.cards.size(); i < 100; i++)
			this.cards.add("");
	}

	private void updateStringInFloorMap(Map<String, Object> floorMap, String floorMapKey, String appendStr) {
		updateStringInFloorMap(floorMap, floorMapKey, appendStr, ',');
	}
	private void updateStringInFloorMap(Map<String, Object> floorMap, String floorMapKey, String appendStr, char ch) {
		Object floorMapEntry = floorMap.get(floorMapKey);
		if (floorMapEntry == null)
			floorMap.put(floorMapKey, appendStr);
		else
			floorMap.put(floorMapKey, floorMapEntry.toString() + ch + ' ' + appendStr);
	}
	
	
	public Map<Integer, Map<String, Object>> getFloors() {
		return floors;
	}
	
	public String[] getBossRelicsSkipped() {
		return bossRelicsSkipped;
	}
	
	public String getCharacterName() {
		return characterName == null? "": characterName;
	}
	
	public LinkedList<Object> getCards() {
		
		if (cards == null) {
			cards = new LinkedList<Object>();
			for (int i=0; i < 100; i++)
				cards.add("");
		}
		
		return cards;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RunData other = (RunData) obj;
		if (seed != other.seed)
			return false;
		return true;
	}
}
