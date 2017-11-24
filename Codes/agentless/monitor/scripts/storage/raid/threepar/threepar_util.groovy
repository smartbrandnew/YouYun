/*!Action
 action.name=THREEPAR存储监测工具类
 action.descr=THREEPAR存储监测工具类
 action.protocols=smis
*/

import com.broada.carrier.monitor.impl.storage.ThreePARUtils;
import java.util.*;
import java.lang.*;

def cimInstances = ThreePARUtils.findArrayInstances($smis);

def getCIMInstances() {
	return cimInstances;
}

def double convertBytesIntoGB(String totalBlocks, String blockSize) throws Exception
{
  double result = 0.0D;
  try
  {
    int blkInBytes = Integer.parseInt(blockSize.trim());
    double noBlocks = Double.parseDouble(totalBlocks.trim());
    double totSizeInBytes = noBlocks * blkInBytes;

    long totSizeGbLong = ()(totSizeInBytes / 1073741824.0D * 1000.0D);
    result = totSizeGbLong / 1000.0D;
  }
  catch (Exception ex)
  {
    ex.printStackTrace();
  }
  return result;
}

def long getGbFromBits(String speedBits) throws Exception
{
  long result = 0L;
  try
  {
    long portSpeedInLong = ()(Double.parseDouble(speedBits.trim()) / 1073741824.0D * 100.0D);
    double portSpeedInDouble = portSpeedInLong / 100.0D;
    result = Math.round(portSpeedInDouble);
  }
  catch (Exception ex)
  {
    ex.printStackTrace();
  }
  return result;
}

def String getLinkTechnology(int link)
{
  String[] linkTechnology = { "Unknown", "Other", "Ethernet", "IB", "Fiber Channel", "FDDI", "ATM", "Token Ring", "Frame Relay", "Infrared", "BlueTooth", "Wireless LAN" };

  if ((link >= 0) && (link <= 11))
  {
    return linkTechnology[link];
  }
  return linkTechnology[0];
}

def String getPortType(int type)
{
  String[] portTypes = { "Unknown", "Other", "N", "NL", "F/NL", "Nx", "E", "F", "FL", "B", "G", "Vendor Reserved" };

  if ((type == 0) || (type == 1))
  {
    return portTypes[type];
  }
  if ((type >= 10) && (type <= 18))
  {
    return portTypes[(type - 8)];
  }
  if ((type >= 16000) && (type <= 65535))
  {
    return portTypes[11];
  }

  return portTypes[0];
}

def String getBatteryStatus(String input) throws Exception
{
  String batteryStatus = "Not Available";
  try
  {
    int index = Integer.parseInt(input);
    switch (index)
    {
    case 1:
      batteryStatus = "Other";
      break;
    case 2:
      batteryStatus = "Unknown";
      break;
    case 3:
      batteryStatus = "Fully Charged";
      break;
    case 4:
      batteryStatus = "Low";
      break;
    case 5:
      batteryStatus = "Crtical";
      break;
    case 6:
      batteryStatus = "Charging";
      break;
    case 7:
      batteryStatus = "Charging and High";
      break;
    case 8:
      batteryStatus = "Charging and Low";
      break;
    case 9:
      batteryStatus = "Charging and Critical";
      break;
    case 10:
      batteryStatus = "Undefined";
      break;
    case 11:
      batteryStatus = "Partially Charged";
      break;
    case 12:
      batteryStatus = "Learning";
      break;
    case 13:
      batteryStatus = "Overcharged";
    }

  }
  catch (Exception e)
  {
    e.printStackTrace();
  }
  return batteryStatus;
}

