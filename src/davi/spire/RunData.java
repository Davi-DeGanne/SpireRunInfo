package davi.spire;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class RunData {
	
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
		return map;
	}
	
	public String neowBonus;
	public String neowCost;
	public List<Map<String, Object>> cards;
	public List<String> metricItemsPurchased;
	public List<Number> metricItemPurchaseFloors;
	public List<String> metricItemsPurged;
	public List<Number> metricItemsPurgedFloors;
	public List<String> relics;
	public int ascensionLevel;
	
	private Map<Integer, Map<String, Object>> floors = new TreeMap<Integer, Map<String, Object>>();
	private String[] bossRelicsSkipped = new String[2];
	private String characterName;

	public RunData() {
		for (int floor=0; floor < 55; floor++)
			floors.put(floor, new HashMap<String, Object>());
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
			else if (floor == 51)
				ch = 'B';
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
			if (restAction.equals("Smith"))
				updateStringInFloorMap(floorMap, "cardsUpgraded", map.get("data").toString());
		}
	}
	
	public void setMetricEventChoices(List<Event> mec) {
		for (Event e: mec) {
			Map<String, Object> floorMap = floors.get(e.floor);
			
			floorMap.put("enemies", e.eventName + ": " + e.playerChoice);
			
			if (e.cardsObtained != null)
				updateStringInFloorMap(floorMap, "cardPicked", e.cardsObtained.toString().replaceAll("[\\[\\]]", ""));
			
			List<String> totalCardsRemoved = new ArrayList<String>();
			if (e.cardsRemoved != null)
				totalCardsRemoved.addAll(e.cardsRemoved);
			if (e.cardsTransformed != null)
				totalCardsRemoved.addAll(e.cardsTransformed);
			if (!totalCardsRemoved.isEmpty())
				updateStringInFloorMap(floorMap, "cardsPurged", totalCardsRemoved.toString().replaceAll("[\\[\\](_G)(_B)(_R)]", ""));
			
			if (e.cardsUpgraded != null)
				updateStringInFloorMap(floorMap, "cardsUpgraded", e.cardsUpgraded.toString().replaceAll("[\\[\\](_G)(_B)(_R)]", ""));
			
			if (e.relicsObtained != null)
				updateStringInFloorMap(floorMap, "relics", e.relicsObtained.toString().replaceAll("[\\[\\]]", ""));
		}
	}
	
	public void finalize() {
		int startingMaxHp = 0;
		int startingHp;
		Map<String, Object> floor1 = floors.get(1);
		switch (characterName) {
		case "The Ironclad": startingMaxHp = 80; break;
		case "The Silent"  : startingMaxHp = 70; break;
		case "The Defect"  : startingMaxHp = 75; break;
		}
		
		if (ascensionLevel > 5) {
			if (ascensionLevel > 13)
				startingMaxHp = startingMaxHp - startingMaxHp/16;
			startingHp = startingMaxHp - startingMaxHp/10;
		} else
			startingHp = startingMaxHp;
		
		floor1.put("maxHpChange", ((Number) floor1.get("maxHpOnExit")).intValue() - startingMaxHp);
		floor1.put("hpChange", ((Number) floor1.get("hpOnExit")).intValue() - startingHp);
		floor1.put("goldChange", ((Number) floor1.get("goldOnExit")).intValue() - 100);
		
		
		for (int i=0; i < metricItemsPurchased.size(); i++) {
			String itemPurchased = metricItemsPurchased.get(i);
			Map<String, Object> floorMap = floors.get(metricItemPurchaseFloors.get(i).intValue());
			if (itemPurchased.contains("Potion"))
				updateStringInFloorMap(floorMap, "potionsObtained", itemPurchased);
			else if (relics.contains(itemPurchased))
				updateStringInFloorMap(floorMap, "relics", itemPurchased);
			else
				updateStringInFloorMap(floorMap, "cardPicked", itemPurchased);
		}
		
		for (int i=0; i < metricItemsPurged.size(); i++) {
			String itemPurged = metricItemsPurged.get(i);
			if (itemPurged.contains("_"))
				itemPurged = itemPurged.substring(0, itemPurged.lastIndexOf('_'));
			Map<String, Object> floorMap = floors.get(metricItemsPurgedFloors.get(i).intValue());
			floorMap.put("cardsPurged", itemPurged);
		}
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
		return characterName;
	}

	public void setCharacterName(String characterName) {
		if (!characterName.contains("The"))
			characterName = "The " + characterName;
		this.characterName = characterName;
	}
}
