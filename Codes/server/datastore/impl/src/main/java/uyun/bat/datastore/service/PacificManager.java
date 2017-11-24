package uyun.bat.datastore.service;

import uyun.pacific.model.api.service.ModelService;
import uyun.pacific.model.api.service.ResAttributeService;
import uyun.pacific.model.api.service.ResClassService;
import uyun.pacific.model.api.service.ResInterfaceService;
import uyun.pacific.model.api.service.ResRelationMetaService;
import uyun.pacific.model.api.service.ResRelationTypeService;
import uyun.pacific.model.api.service.ResUniqueKeyService;
import uyun.pacific.resource.api.service.AuditService;
import uyun.pacific.resource.api.service.InlineObjectService;
import uyun.pacific.resource.api.service.ResObjectService;
import uyun.pacific.resource.api.service.ResRelationService;
import uyun.pacific.resource.api.service.ResZoneService;

public abstract class PacificManager {
	private static PacificManager instance = new PacificManager(){};

	public static PacificManager getInstance() {
		return instance;
	}

	private ResObjectService pacificResObjectService;
	private AuditService pacificAuditService;
	private ResRelationService pacificResRelationService;
	private ResZoneService pacificResZoneService;
	private ModelService pacificModelService;
	private ResAttributeService pacificResAttributeService;
	private ResClassService pacificResClassService;
	private ResInterfaceService pacificInterfaceService;
	private ResRelationMetaService pacificResRelationMetaService;
	private ResRelationTypeService pacificResRelationTypeService;
	private ResUniqueKeyService pacificResUniqueKeyService;
	private InlineObjectService pacificInlineObjectService;

	public ResObjectService getPacificResObjectService() {
		return pacificResObjectService;
	}

	public void setPacificResObjectService(ResObjectService pacificResObjectService) {
		this.pacificResObjectService = pacificResObjectService;
	}

	public AuditService getPacificAuditService() {
		return pacificAuditService;
	}

	public void setPacificAuditService(AuditService pacificAuditService) {
		this.pacificAuditService = pacificAuditService;
	}

	public ResRelationService getPacificResRelationService() {
		return pacificResRelationService;
	}

	public void setPacificResRelationService(ResRelationService pacificResRelationService) {
		this.pacificResRelationService = pacificResRelationService;
	}

	public ResZoneService getPacificResZoneService() {
		return pacificResZoneService;
	}

	public void setPacificResZoneService(ResZoneService pacificResZoneService) {
		this.pacificResZoneService = pacificResZoneService;
	}

	public ModelService getPacificModelService() {
		return pacificModelService;
	}

	public void setPacificModelService(ModelService pacificModelService) {
		this.pacificModelService = pacificModelService;
	}

	public ResAttributeService getPacificResAttributeService() {
		return pacificResAttributeService;
	}

	public void setPacificResAttributeService(ResAttributeService pacificResAttributeService) {
		this.pacificResAttributeService = pacificResAttributeService;
	}

	public ResClassService getPacificResClassService() {
		return pacificResClassService;
	}

	public void setPacificResClassService(ResClassService pacificResClassService) {
		this.pacificResClassService = pacificResClassService;
	}

	public ResInterfaceService getPacificInterfaceService() {
		return pacificInterfaceService;
	}

	public void setPacificInterfaceService(ResInterfaceService pacificInterfaceService) {
		this.pacificInterfaceService = pacificInterfaceService;
	}

	public ResRelationMetaService getPacificResRelationMetaService() {
		return pacificResRelationMetaService;
	}

	public void setPacificResRelationMetaService(ResRelationMetaService pacificResRelationMetaService) {
		this.pacificResRelationMetaService = pacificResRelationMetaService;
	}

	public ResRelationTypeService getPacificResRelationTypeService() {
		return pacificResRelationTypeService;
	}

	public void setPacificResRelationTypeService(ResRelationTypeService pacificResRelationTypeService) {
		this.pacificResRelationTypeService = pacificResRelationTypeService;
	}

	public ResUniqueKeyService getPacificResUniqueKeyService() {
		return pacificResUniqueKeyService;
	}

	public void setPacificResUniqueKeyService(ResUniqueKeyService pacificResUniqueKeyService) {
		this.pacificResUniqueKeyService = pacificResUniqueKeyService;
	}

	public InlineObjectService getPacificInlineObjectService() {
		return pacificInlineObjectService;
	}

	public void setPacificInlineObjectService(InlineObjectService pacificInlineObjectService) {
		this.pacificInlineObjectService = pacificInlineObjectService;
	}
}
