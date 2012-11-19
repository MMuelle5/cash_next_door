package ch.imbApp.cash_next_door.calc;

import android.location.Location;

public class CalcAngle {

	private final static Location NULL_PUNKT = new Location("0");
	{
		NULL_PUNKT.setLatitude(0d);
		NULL_PUNKT.setLongitude(0d);
	}
	public static double calcAngle(double myDirection, Location myLocation, Location targetLocation) {
		
		double distA = myLocation.distanceTo(targetLocation);
		double distB = myLocation.distanceTo(NULL_PUNKT);
		double distC = targetLocation.distanceTo(NULL_PUNKT);
		double alpha;
		
//		if(distA > distB && distA > distC) {
//			alpha = Math.cos(distB/distA);
//		}
//		else if(distB > distA && distB > distC) {
//			alpha = Math.cos(distA/distB);
//		} else {
//			
//		}
		alpha = Math.acos((-distA * distA + distB *distB + distC * distC) / (2.0 * distB * distC));
		
		double totDir = myDirection;

		totDir = totDir < 270 ? 360 -totDir : totDir;
		
//TODO richtig implementieren		
		return alpha;
	}
}
