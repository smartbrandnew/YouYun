package uyun.bat.syndatabase.entity;

public class ResourceIdTransform {
	
	private String resId;   // 资源Id
	private String tenantId;   // 租户Id
	private String unitId;        // 统一资源库Id
	
	public ResourceIdTransform() {
		//
	}
	
	public ResourceIdTransform(String resId, String tenantId) {
		this.resId = resId;
		this.tenantId = tenantId;
	}
	
	public String getResId() {
		return resId;
	}
	public void setResId(String resId) {
		this.resId = resId;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getUnitId() {
		return unitId;
	}
	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	@Override
	public String toString() {
		return "ResourceIdTransform [resId=" + resId + ", tenantId=" + tenantId + ", unitId=" + unitId
				+ "]";
	}
	
}
