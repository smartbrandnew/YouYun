package uyun.bat.datastore.api.entity;

import java.io.Serializable;

public class ResourceOrderBy implements Serializable {
	private static final long serialVersionUID = 1L;
	private Order order;
	private SortBy sortBy;

	public ResourceOrderBy(Order order, SortBy sortBy) {
		this.order = order;
		this.sortBy = sortBy;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public SortBy getSortBy() {
		return sortBy;
	}

	public void setSortBy(SortBy sortBy) {
		this.sortBy = sortBy;
	}

	public enum Order {
		ASCENDING("asc"), DESCENDING("desc");

		private String text;

		Order(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return this.text;
		}

		public static Order checkByName(String name) {
			for (Order type : Order.values()) {
				if (type.text.equals(name)) {
					return type;
				}
			}
			return null;
		}
	}

	public enum SortBy {
		HOSTNAME("hostname"), IPADDR("ipaddr"), RESOURCETYPE("type"), ONLINESTATUS("online_status"), ALERTSTATUS(
				"alert_status");
		private String text;

		SortBy(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return this.text;
		}

		public static SortBy checkByName(String name) {
			for (SortBy type : SortBy.values()) {
				if (type.text.equals(name)) {
					return type;
				}
			}
			return null;
		}
	}
}
