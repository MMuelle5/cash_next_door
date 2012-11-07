package ch.imbApp.cash_next_door.bean;

import android.location.Location;

public class BankOmat {

	private Location location;
	private String bank;
	
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
	
}
