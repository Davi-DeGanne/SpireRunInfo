package davi.spire;
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
		map.put(null, "Boss Chest");
		return map;
	}
	
	public String neowBonus;
	public String neowCost;
	public List<Map<String, Object>> cards;
	
	private Map<Integer, Map<String, Object>> floors = new TreeMap<Integer, Map<String, Object>>();

	public RunData() {
		for (int floor=1; floor < 55; floor++)
			floors.put(floor, new HashMap<String, Object>());
	}

	public void setMetricDamageTaken(List<Map<String, Object>> mdt) {
		
		for (Map<String, Object> floorInfo: mdt) {
			int floorNumber = (int) floorInfo.get("floor");
			floorInfo.remove("floor");
			floors.get(floorNumber).putAll(floorInfo);
		}
	}
	
	public void setMetricPathTaken(List<String> mpt) {
		int floor = 1;
		for (String str: mpt) {
			Character ch = null;
			if (str != null)
				ch = str.charAt(0);
			floors.get(floor).put("type", FLOOR_TYPES_BY_CHAR.get(ch));
			floor++;
		}
	}
	
	public void setMetricRelicsObtained(List<Map<String, Object>> mro) {
		
		for (Map<String, Object> relicMap: mro) {
			int floor = (int) relicMap.get("floor");
			String oldRelicString = (String) floors.get(floor).get("relics");
			String newRelicString = (String) relicMap.get("key");
			if (newRelicString.contains("Egg 2"))
				newRelicString = newRelicString.substring(0, newRelicString.length() - 2);
			
			if (oldRelicString == null)
				floors.get(floor).put("relics", newRelicString);
			else
				floors.get(floor).put("relics", oldRelicString + ", " + newRelicString);
		}
	}
	
	public Map<Integer, Map<String, Object>> getFloors() {
		return floors;
	}
}
