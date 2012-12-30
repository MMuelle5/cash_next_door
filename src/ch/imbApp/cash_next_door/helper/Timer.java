package ch.imbApp.cash_next_door.helper;

import java.util.Date;

public class Timer {

	private Date date = new Date();
	private int sleepingDuration;
	
	
	public Timer(int sleepingDuration) {
		super();
		this.sleepingDuration = sleepingDuration;
	}

	public boolean isTimeReached() {

		if (date.before(new Date())) {
			date = new Date();
			date = new Date(date.getTime() + sleepingDuration);
			return true;
		}
		else {
			return false;
		}
	}
	
	public void setSleepingDuration(int sleepingDuration) {
		this.sleepingDuration =  sleepingDuration;
	}

}
