package eu.eyan.idakonyvtar.oszk;

import java.util.List;

public class MarcHelper {

	public static String findMarc(List<Marc> marcs, MarcCodes marcCode) {
		for (Marc marc : marcs) {
			if (marcCode.getMarc1().equals(marc.marc1())
					&& marcCode.getMarc2().equals(marc.marc2())
					&& marcCode.getMarc3().equals(marc.marc3())) {
				return marc.value();
			}
		}
		return null;
	}
}