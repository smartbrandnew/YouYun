package com.broada.carrier.monitor.common.swing.table;

public class BeanTable <T> extends BaseTable {
	private static final long serialVersionUID = 1L;
	
	public BeanTable() {
		this(new BeanTableModel<T>());
	}

	public BeanTable(BeanTableModel<T> model) {
		super(model);		
	}

	public T getSelectedObject() {
		return getModel().getSelectedRow(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public BeanTableModel<T> getModel() {		
		return (BeanTableModel<T>) super.getModel();
	}

	public void setSelectedObject(T object) {
		getModel().getRow(object);
	}	
}
