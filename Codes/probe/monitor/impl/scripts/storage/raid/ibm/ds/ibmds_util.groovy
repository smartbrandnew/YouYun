/*!Action
 action.name=IBMDS存储监测工具类
 action.descr=IBMDS存储监测工具类
 action.protocols=smis
*/

import com.broada.carrier.monitor.impl.storage.IBMDSUtils;
import java.util.*;
import java.lang.*;

def cimInstances = IBMDSUtils.findStorageSystem($smis);

def getCIMInstances() {
	return cimInstances;
}

def double b2GB(String totalBlocks, String blockSize) {
	    double result = 0.0D;
	    try
	    {
	      int blkInBytes = Integer.parseInt(blockSize.trim());
	      double noBlocks = Double.parseDouble(totalBlocks.trim());
	      double totSizeInBytes = noBlocks * blkInBytes;

	      long totSizeGbLong = (totSizeInBytes / 1073741824.0D * 1000.0D);
	      result = totSizeGbLong / 1000.0D;
	    }
	    catch (Exception ex)
	    {
	      ex.printStackTrace();
	    }
	    return result;
}

def String getVolumeGroupType(String type) throws Exception
{
  String result = "Not Available";
  switch (Integer.parseInt(type))
  {
  case 0:
    result = "Unknown";
    break;
  case 1:
    result = "SCSIMap256";
    break;
  case 2:
    result = "SCSIMask";
    break;
  case 3:
    result = "SCSI520Map256";
    break;
  case 4:
    result = "SCSI520Mask";
  }

  return result;
}

def getFCPortType(def portTypeIndex) {
	def portType = null;
	switch(portTypeIndex) {
		case 0:
			portType = 'Unknown';
		case 1:
			portType = 'Other';
			break;
		case 10:
			portType = 'N';
			break;
		case 11:
			portType = 'NL';
			break;
		case 12:
			portType = 'F/NL';
			break;
		case 13:
			portType = 'Nx';
			break;
		case 14:
			portType = 'E';
			break;
		case 15:
			portType = 'F';
			break;
		case 16:
			portType = 'FL';
			break;
		case 17:
			portType = 'B';
			break;
		case 18:
			portType = 'G';
			break;
		case 16000..65535:
			portType = 'Vendor Reserved';
			break;
		
	}
}

def String getHealthState(int healthState) throws Exception
{
  String response = "Not Available";
  switch (Integer.parseInt(healthState)) {
  case 0:
    response = "Unknown"; break;
  case 5:
    response = "OK"; break;
  case 10:
    response = "Degraded/Warning"; break;
  case 15:
    response = "Minor failure"; break;
  case 20:
    response = "Major failure"; break;
  case 25:
    response = "Critical failure"; break;
  case 30:
    response = "Non-recoverable error"; break;
  case 32768:
    response = "Removed"; break;
  case 32769:
    response = "Online"; break;
  case 32770:
    response = "Offline"; break;
  case 32771:
    response = "Rebooting"; break;
  case 32772:
    response = "Success"; break;
  case 32773:
    response = "Failure"; break;
  case 32774:
    response = "Write Disabled"; break;
  case 32775:
    response = "Write Protected"; break;
  case 32776:
    response = "Not Ready"; break;
  case 32777:
    response = "Power Saving Mode";
  }
  return response;
}

def String getOperationalStatus(int index) {
	String operationalStatus = null;
	switch (index) {
	case 0:
		operationalStatus = "Unknown";
		break;
	case 1:
		operationalStatus = "Other";
		break;
	case 2:
		operationalStatus = "OK";
		break;
	case 3:
		operationalStatus = "Degraded or Predicted Failure";
		break;
	case 4:
		operationalStatus = "Stressed";
		break;
	case 5:
		operationalStatus = "Predictive Failure";
		break;
	case 6:
		operationalStatus = "Error";
		break;
	case 7:
		operationalStatus = "Non-Recoverable Error";
		break;
	case 8:
		operationalStatus = "Starting";
		break;
	case 9:
		operationalStatus = "Stopping";
		break;
	case 10:
		operationalStatus = "Stopped";
		break;
	case 11:
		operationalStatus = "In Service";
		break;
	case 12:
		operationalStatus = "No Contact";
		break;
	case 13:
		operationalStatus = "Lost Communication";
		break;
	case 14:
		operationalStatus = "Aborted";
		break;
	case 15:
		operationalStatus = "Dormant";
		break;
	case 16:
		operationalStatus = "Supporting Entity In Error";
		break;
	case 17:
		operationalStatus = "Completed";
		break;
	case 18:
		operationalStatus = "Power Mode";
		break;
	case 19:
		operationalStatus = "Flushing";
		break;
	case 32768:
		operationalStatus = "Removed";
		break;
	case 32769:
		operationalStatus = "Online";
		break;
	case 32770:
		operationalStatus = "Offline";
		break;
	case 32771:
		operationalStatus = "Rebooting";
		break;
	case 32772:
		operationalStatus = "Success";
		break;
	case 32773:
		operationalStatus = "Failure";
		break;
	case 32774:
		operationalStatus = "Write Disabled";
		break;
	case 32775:
		operationalStatus = "Write Protected";
		break;
	case 32776:
		operationalStatus = "Not Ready";
		break;
	case 32777:
		operationalStatus = "Power Saving Mode";
		break;
	default:
		operationalStatus = "Not Available";
	}
	return operationalStatus;
}


def String getUsageRestriction(int usageRest) throws Exception
{
  String response = "Not Available";
  switch (usageRest) {
  case 0:
    response = "Unknown"; break;
  case 2:
    response = "Front-end only"; break;
  case 3:
    response = "Back-end only"; break;
  case 4:
    response = "Not restricted";
  case 1:
	response = "Not restricted";
  }
  return response;
}
