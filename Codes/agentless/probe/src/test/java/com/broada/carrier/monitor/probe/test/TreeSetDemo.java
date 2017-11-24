package com.broada.carrier.monitor.probe.test;

import java.util.TreeSet;

public class TreeSetDemo {
	
	public static void main(String[] args) {
		
		TreeSet<TaskItem> sets = new TreeSet<TaskItem>();
		
		TaskItem item1 = new TaskItem("1", 1);
		TaskItem item2 = new TaskItem("2", 2);
		TaskItem item3 = new TaskItem("3", 3);
		TaskItem item4 = new TaskItem("1", 1);
		
		sets.add(item3);
		sets.add(item2);
		sets.add(item1);
		sets.add(item4);
		// 取出来一个，再看第一个
		System.out.println(sets.remove(item1));
		System.out.println(sets.first());
		
		sets.add(item1);
		System.out.println(sets.first());
		
		

	}
	
	

}

class TaskItem implements Comparable<TaskItem>{
	
	private String ID;
	private int value;
	
	public TaskItem() {
		// TODO Auto-generated constructor stub
	}
	public TaskItem(String ID, int value) {
		this.ID = ID;
		this.value = value;
	}
	
	
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "TaskItem [ID=" + ID + ", value=" + value + "]";
	}
	
	@Override
	public int compareTo(TaskItem item) {
		return this.getValue() > item.getValue() ? 1:(this.getValue() == item.getValue()? 0:-1);
	}
	
}