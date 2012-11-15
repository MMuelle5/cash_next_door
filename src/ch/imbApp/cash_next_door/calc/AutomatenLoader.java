package ch.imbApp.cash_next_door.calc;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import ch.imbApp.cash_next_door.bean.BankOmat;

public class AutomatenLoader {

	public static List<BankOmat> getBankomaten(Location loc, int rad) {
		List<BankOmat> retList = new ArrayList<BankOmat>();
		
		BankOmat val = new BankOmat();
		val.setLocation(new Location("dummy"));
		val.getLocation().setLatitude(47.377847d);
		val.getLocation().setLongitude(8.532292d);
		retList.add(val);
		
//		val = new BankOmat();
//		val.setLocation(new Location("dummy"));
//		val.getLocation().setLatitude(42.22d);
//		val.getLocation().setLongitude(42.221d);
//		retList.add(val);
		
		return retList;
	}
}
