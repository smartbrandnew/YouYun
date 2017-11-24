package uyun.bat.datastore.api.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;

public class RelativeTime implements Serializable {
	private static final long serialVersionUID = 1L;
	private int value;
	private TimeUnit unit;
	private Calendar calendar;

	public RelativeTime(int value, TimeUnit unit)
	{
		this.value = value;
		this.unit = unit;
		this.calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}
	/*
	public RelativeTime(int value, String unit){
		this.value = value;
		this.unit=TimeUnit.valueOf(unit);
		this.calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}
*/
	public RelativeTime(){
		
	}
	public int getValue()
	{
		return this.value;
	}

	public String getUnit()
	{
		return this.unit.toString();
	}

	public long getTimeRelativeTo(long time)
	{
		int field = 0;
		if (this.unit == TimeUnit.SECONDS)
		{
			field = 13;
		}
		else if (this.unit == TimeUnit.MINUTES)
		{
			field = 12;
		}
		else if (this.unit == TimeUnit.HOURS)
		{
			field = 10;
		}
		else if (this.unit == TimeUnit.DAYS)
		{
			field = 5;
		}
		else if (this.unit == TimeUnit.WEEKS)
		{
			field = 4;
		}
		else if (this.unit == TimeUnit.MONTHS)
		{
			field = 2;
		}
		else if (this.unit == TimeUnit.YEARS)
		{
			field = 1;
		}

		this.calendar.setTimeInMillis(time);
		this.calendar.add(field, -this.value);

		return this.calendar.getTime().getTime();
	}

	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if ((o == null) || (super.getClass() != o.getClass())) {
			return false;
		}
		RelativeTime that = (RelativeTime) o;
		return ((this.value == that.value) && (this.unit == that.unit));
	}

	public int hashCode()
	{
		int result = this.value;
		result = 31 * result + ((this.unit != null) ? this.unit.hashCode() : 0);
		return result;
	}
}
