/*!Action
 action.name=通过smi-s协议监测IBMDS存储池基本信息
 action.descr=通过smi-s协议监测IBMDS存储池基本信息
 action.protocols=smis
 monitor.output=IBMDS-STORAGEPOOL-INFO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.IBMDSUtils;
import java.lang.*;
import com.broada.cid.action.impl.action.context.NumberContext;

executeMonitor();
return $result;

def executeMonitor() {
	def dsUtil = $script.use('storage/raid/ibm/ds/ibmds_util');
	def arrayInst = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageSystem');
	def spInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_StoragePool');
	def spcInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_StoragePoolCapabilities');
	def spceInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_StoragePoolCompositeExtent');
	arrayInst.each{systemInst->
	def arrayCOP = systemInst.getObjectPath();
	def name = $smis.getProperty(systemInst, 'Name');
	spInstances.each{spInst->
		def sysname =  $smis.getProperty(spInst, 'StorageSystem_Name');
		if(sysname==name){
		def poolId = $smis.getProperty(spInst,"PoolID");
		def instaceId = $smis.getProperty(spInst,"InstanceID");
		def poolName = $smis.getProperty(spInst,"ElementName");
		def totalCapacity = $unit.b2GB($smis.getProperty(spInst,"TotalManagedSpace"));
		def remainingManagedSpace = $unit.b2GB($smis.getProperty(spInst,"RemainingManagedSpace"));
		def poolOpStatus = $smis.getProperty(spInst,"OperationalStatus")[0];
        def poolPrimordial = $smis.getProperty(spInst,"Primordial");
        
/*		
		def DataRedundancyMax = null;
		def DataRedundancyMin = null;
		def PackageRedundancyMax = null;
		def PackageRedundancyMin = null;
		def DeltaReservationMax = null;
		def DeltaReservationMin = null;
		def DeltaReservationDefault = null;
		def ExtentStripeLengthDefault = null;

			spcInstances.each{spcInst->
			def spcid = $smis.getProperty(spcInst, 'StoragePool_InstanceID');
			if(spcid==instaceId){
			 DataRedundancyMax = $smis.getProperty(spcInst, 'DataRedundancyMax');
			 DataRedundancyMin = $smis.getProperty(spcInst, 'DataRedundancyMin');
			 PackageRedundancyMax = $smis.getProperty(spcInst, 'PackageRedundancyMax');
			 PackageRedundancyMin = $smis.getProperty(spcInst, 'PackageRedundancyMin');
			 DeltaReservationMax = $smis.getProperty(spcInst, 'DeltaReservationMax');
			 DeltaReservationMin = $smis.getProperty(spcInst, 'DeltaReservationMin');
			 DeltaReservationDefault = $smis.getProperty(spcInst, 'DeltaReservationDefault');
			 ExtentStripeLengthDefault = $smis.getProperty(spcInst, 'ExtentStripeLengthDefault');
			
			 }
			}
		def ConsumableBlocks = null;
		def BlockSize = null;
		def NumberOfBlocks = null;

			spceInstances.each{spceInst->
			def spceid = $smis.getProperty(spceInst, 'StoragePool_InstanceID');
			if(spceid==instaceId){
			 ConsumableBlocks = $smis.getProperty(spceInst, 'ConsumableBlocks');
			 BlockSize = $smis.getProperty(spceInst, 'BlockSize');
			 NumberOfBlocks = $smis.getProperty(spceInst, 'NumberOfBlocks');

			 }
			}
*/			
			
		def result = $result.create(poolName);
		result.clazz = 'StoragePool';
		result.attr.capacity = Double.toString(totalCapacity);
		result.attr.storResCode =poolId;
		result.attr.primordial = poolPrimordial;
		result.perf.stor_manage.space_allocated = totalCapacity - remainingManagedSpace;
		result.perf.stor_manage.space_unallocated = remainingManagedSpace;
		result.perf.stor_manage.space_useage = NumberContext.round((totalCapacity - remainingManagedSpace) * 100 / totalCapacity, 2);
		result.state.available_status = (poolOpStatus == '2' ? 1 : 0);
	}	
	}
	}
	
}