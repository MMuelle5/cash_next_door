package ch.imbApp.cash_next_door.bean;

import android.location.Location;
import android.widget.TextView;

public class BankOmat {

	private Location location;
	private String bankName;
	private String bankAddress;

	private TextBean displayedView;
	private double direction;
	private double distance;

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Double getLatitude() {
		if (location != null) {
			return location.getLatitude();
		}
		return null;
	}

	public Double getLongitude() {
		if (location != null) {
			return location.getLongitude();
		}
		return null;
	}

	public TextBean getDisplayedView() {
		return displayedView;
	}

	public void setDisplayedView(TextBean displayedView) {
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

	public String getBankAddress() {
		return bankAddress;
	}

	public void setBankAddress(String bankAddress) {
		this.bankAddress = bankAddress;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

}
