package ch.imbApp.cash_next_door.calc;

import ch.imbApp.cash_next_door.bean.BankOmat;
import android.location.Location;

public class CalcAngle {

	private final static Location NULL_PUNKT = new Location("0");
	{
		NULL_PUNKT.setLatitude(0d);
		NULL_PUNKT.setLongitude(0d);
	}
	@Deprecated
	public static double calcAngle(double myDirection, Location myLocation, Location targetLocation) {
		
//		Location loc = new Location("north");
//		System.out.println("bearing: "+myLocation.bearingTo(targetLocation));
////		loc.setLatitude();
//		loc.setLongitude(myLocation.getLongitude());
//		double distA = myLocation.distanceTo(targetLocation);
//		double distB = myLocation.distanceTo(NULL_PUNKT);
//		double distC = targetLocation.distanceTo(NULL_PUNKT);
//		double alpha;
//		
////		if(distA > distB && distA > distC) {
////			alpha = Math.cos(distB/distA);
////		}
////		else if(distB > distA && distB > distC) {
////			alpha = Math.cos(distA/distB);
////		} else {
////			
////		}
//		alpha = Math.acos((-distA * distA + distB *distB + distC * distC) / (2.0 * distB * distC));
//		
//		double totDir = myDirection;
//
//		totDir = totDir < 270 ? 360 -totDir : totDir;
//		
		return myLocation.bearingTo(targetLocation);
	}
	
	public static void uptDateBankOmatInfos(Location myLocation, BankOmat machine) {
		
		machine.setDirection(myLocation.bearingTo(machine.getLocation()));
		machine.setDistance(myLocation.distanceTo(machine.getLocation()));
	}
}
