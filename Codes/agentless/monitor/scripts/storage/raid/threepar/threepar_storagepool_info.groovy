/*!Action
 action.name=通过smi-s协议监测THREEPAR存储池信息
 action.descr=通过smi-s协议THREEPAR存储池信息
 action.protocols=smis
 monitor.output=THREEPAR-STORAGEPOOL-INFO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.ThreePARUtils;
import java.lang.*;
import com.broada.cid.action.impl.action.context.NumberContext;

executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/threepar/threepar_util');
	def arrayInst = svcUtil.getCIMInstances();
	arrayInst.each{cimInst->
		def cop = cimInst.getObjectPath();
		def spInstances = ThreePARUtils.findSPInstances($smis, cop);
		spInstances.each{spInst->
	         
	         String poolDescription = spInst.getProperty("Description");
	         String caption = spInst.getProperty("Caption");
             String spElementName = spInst.getProperty("ElementName");
             String spName = spInst.getProperty("Name");
             String spInstanceId = spInst.getProperty("InstanceID");
             String spPoolId = spInst.getProperty("PoolID");
             String spPrimordial = spInst.getProperty("Primordial");

             String spTotalManagedSpace = spInst.getProperty("TotalManagedSpace");
             double spTotalCapacity = 0.0D;
             try
             {
               spTotalCapacity = svcUtil.convertBytesIntoGB(spTotalManagedSpace, "1");
             }
             catch (Exception e)
             {
               e.printStackTrace();
             }

             String spRemainingManagedSpace = spInst.getProperty("RemainingManagedSpace") ;
             double spFreeCapacity = 0.0D;
             try
             {
               spFreeCapacity = svcUtil.convertBytesIntoGB(spRemainingManagedSpace, "1");
             }
             catch (Exception e)
             {
               e.printStackTrace();
             }

             double spUsedCapacity = spTotalCapacity - spFreeCapacity;
             String spSpaceLimit = spInst.getProperty("SpaceLimit");
             double spaceLimit = 0.0D;
             try
             {
               spaceLimit = svcUtil.convertBytesIntoGB(spSpaceLimit, "1");
             }
             catch (Exception e)
             {
               e.printStackTrace();
             }

             String spUsage = spInst.getProperty("Usage");
             String spSpaceLimitDetermination = spInst.getProperty("SpaceLimitDetermination");
             String spDiskDeviceType = "Not Available";
             try
             {
               spDiskDeviceType = ThreePARUtils.getDiskDeviceType(spInst.getProperty("DiskDeviceType"));
             }
             catch (Exception e)
             {
               e.printStackTrace();
             }
             
             
            def result = $result.create(spElementName);
     		result.clazz = 'StoragePool';
     		result.attr.capacity = spTotalCapacity;
     		result.attr.storResCode = spPoolId;
     		result.perf.stor_manage.space_allocated = spUsedCapacity;
     		result.perf.stor_manage.space_unallocated = spFreeCapacity;
     		result.perf.stor_manage.space_useage = NumberContext.round(spUsedCapacity * 100 / spTotalCapacity, 2);

		}
	}
}