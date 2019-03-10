package davi.spire;

import java.util.List;

public class CardChoice {
	
	public String picked;
	public int floor;
	public List<String> notPicked;
	
	
	public String toString() {
		return "{floor: " + floor + ", picked: " + picked + ", notPicked: " + notPicked + "}";
	}
}
