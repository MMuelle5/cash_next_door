package ch.imbApp.cash_next_door.calc;

import ch.imbApp.cash_next_door.bean.BankOmat;
import android.location.Location;

public class CalcAngle {
	
	public static void uptDateBankOmatInfos(Location myLocation, BankOmat machine) {
		
		machine.setDirection(myLocation.bearingTo(machine.getLocation()));
		machine.setDistance(myLocation.distanceTo(machine.getLocation()));
	}
}
