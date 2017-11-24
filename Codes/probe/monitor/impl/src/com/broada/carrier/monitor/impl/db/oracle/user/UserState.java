package com.broada.carrier.monitor.impl.db.oracle.user;

public class UserState {
	
	private String username;
	private int status;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(String account_status) {
		if(account_status.equalsIgnoreCase("OPEN"))
			this.status = 0;
		else if(account_status.equalsIgnoreCase("EXPIRED"))
			this.status = 1;
		else if(account_status.equalsIgnoreCase("EXPIRED(GRACE)"))
			this.status = 2;
		else if(account_status.equalsIgnoreCase("LOCKED(TIMED)"))
			this.status = 3;
		else if(account_status.equalsIgnoreCase("LOCKED"))
			this.status = 4;
		else if(account_status.equalsIgnoreCase("EXPIRED & LOCKED(TIMED)"))
			this.status = 5;
		else if(account_status.equalsIgnoreCase("EXPIRED(GRACE) & LOCKED(TIMED)"))
			this.status = 6;
		else if(account_status.equalsIgnoreCase("EXPIRED & LOCKED"))
			this.status = 7;
		else if(account_status.equalsIgnoreCase("EXPIRED(GRACE) & LOCKED"))
			this.status = 8;
	}
	
}
