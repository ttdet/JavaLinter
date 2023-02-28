package Domain;

import java.util.LinkedList;
import java.util.List;

import DataSource.ClassData;
import DataSource.MethodData;

public class CheckMethodName implements Check {
	
	private static final String CAMEL_CASE_REGEX = "[a-z]([A-Z0-9]*[a-z][a-z0-9]*[A-Z]|[a-z0-9]*[A-Z][A-Z0-9]*[a-z])[A-Za-z0-9]*";
	private static final String CAMEL_CASE_NO_UPPER_REGEX = "[a-z]+";

	@Override
	public List<Warning> check(ClassRegistry classRegistry) {
		List<Warning> warnings = new LinkedList<>();
		
		for (ClassData classData : classRegistry.getAllClasses()) {
			for (MethodData methodData : classData.getMethods()) {
				if (!methodData.getMethodName().equals("<init>") && !methodData.getMethodName().equals("<clinit>") && !hasProperCapitalization(methodData.getMethodName())) {
					warnings.add(new Warning(
							new WarningLocation(classData.getUserFriendlyClassName(), methodData.getMethodName()),
							WarningType.METHOD_NAME_BAD_CAPITALIZATION,
							"Method does not use correct capitalization"));
				}
			}
		}
		
		return warnings;
	}
	
	private boolean hasProperCapitalization(String methodName) {
		return methodName.matches(CAMEL_CASE_REGEX) || methodName.matches(CAMEL_CASE_NO_UPPER_REGEX);
	}

}
