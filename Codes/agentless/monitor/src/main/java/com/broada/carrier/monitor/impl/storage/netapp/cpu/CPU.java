package com.broada.carrier.monitor.impl.storage.netapp.cpu;

public class CPU {
	private Boolean isWacthed = Boolean.FALSE;
	private String expression = null;// 表达式
	
	private  String instance;
	private  String label;   

	private Double useRateValue;
	private Double useRateVavel;

	private Double vacancyRateValue;
	private Double vacancyRateVavel;

	private Double interruptRateValue;
	private Double interruptRateVavel;

	private String cpuuptime;
 

	public Double getUseRateValue() {
		return useRateValue;
	}

	public void setUseRateValue(Double useRateValue) {
		this.useRateValue = useRateValue;
	}

	public Double getUseRateVavel() {
		return useRateVavel;
	}

	public void setUseRateVavel(Double useRateVavel) {
		this.useRateVavel = useRateVavel;
	}

	public Double getVacancyRateValue() {
		return vacancyRateValue;
	}

	public void setVacancyRateValue(Double vacancyRateValue) {
		this.vacancyRateValue = vacancyRateValue;
	}

	public Double getVacancyRateVavel() {
		return vacancyRateVavel;
	}

	public void setVacancyRateVavel(Double vacancyRateVavel) {
		this.vacancyRateVavel = vacancyRateVavel;
	}

	public Double getInterruptRateValue() {
		return interruptRateValue;
	}

	public void setInterruptRateValue(Double interruptRateValue) {
		this.interruptRateValue = interruptRateValue;
	}

	public Double getInterruptRateVavel() {
		return interruptRateVavel;
	}

	public void setInterruptRateVavel(Double interruptRateVavel) {
		this.interruptRateVavel = interruptRateVavel;
	}

	public String getCpuuptime() {
		return cpuuptime;
	}

	public void setCpuuptime(String cpuuptime) {
		this.cpuuptime = cpuuptime;
	}

	 

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getInstance() {
		return instance;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
