package uyun.bat.agent.impl.autosync.entity;

import java.util.ArrayList;
import java.util.List;

public class Action {
	private String event;
	private String type;
	private String arg;
	private List<Condition> conditions;

	public Action() {
		this(null, null, null, null);
	}

	public Action(Event event, String type, String arg, Condition[] conditions) {
		if (event == null)
			event = Event.UPGRADE_SUCCESSFUL;
		this.event = event.getId();
		this.type = type;
		this.arg = arg;
		this.conditions = new ArrayList<Condition>();
		if (conditions != null) {
			for (Condition cond : conditions)
				this.conditions.add(cond);
		}
	}

	public Event retEvent() {
		return Event.getById(event);
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getEvent() {
		return this.event;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getArg() {
		return arg;
	}

	public void setArg(String arg) {
		this.arg = arg;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void addCondition(Condition cond) {
		this.conditions.add(cond);
	}

	@Override
	public String toString() {
		return String.format("%s[%s %s %s]", getClass().getSimpleName(), event, type, arg);
	}

	public Condition[] getConditions(String name) {
		if (conditions.size() == 0)
			return null;

		List<Condition> result = new ArrayList<Condition>();
		for (Condition cond : conditions)
			if (cond.getName().equalsIgnoreCase(name))
				result.add(cond);
		return result.toArray(new Condition[0]);
	}
}
