package ch.imbApp.cash_next_door.bean;

import android.location.Location;
import android.widget.TextView;

public class BankOmat {

	private Location location;
	private String bank;

	private TextView displayedView;
	private double direction;
	private double distance;
	
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public Double getLatitude() {
		if(location != null) {
			return location.getLatitude();
		}
		return null;
	}
	public Double getLongitude() {
		if(location != null) {
			return location.getLongitude();
		}
		return null;
	}
	public String getBank() {
		return bank;
	}
	public void setBank(String bank) {
		this.bank = bank;
	}
	public TextView getDisplayedView() {
		return displayedView;
	}
	public void setDisplayedView(TextView displayedView) {
		this.displayedView = displayedView;
	}
	public double getDirection() {
		return direction;
	}
	public void setDirection(double direction) {
		this.direction = direction;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
}
