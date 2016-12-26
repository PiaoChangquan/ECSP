package org.SDS.Sensor;
import java.util.List;

import org.SDS.Loader.PhysicalSensorInformation;

import com.google.common.base.MoreObjects;

public class SensorUnitConfig {

	public static int snsrCount = 0;
//	���о�̬���� sensor��������Ϊ0
	public static List<PhysicalSensorInformation> physicalSnsrInfo = null;
//	����������ϢΪnull
	public static void showSensorUnitConfigValue() {
		System.out.println("Sensor Count: " + snsrCount);
		System.out.println("Physical Sensor List Values:");
		System.out.println(physicalSnsrInfo);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("Sensor Count", snsrCount).add("Sensor List", physicalSnsrInfo).toString();
	}

}
