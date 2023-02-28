package Domain;

import java.util.LinkedList;
import java.util.List;

import DataSource.AccessModifiers;
import DataSource.ClassData;
import DataSource.MethodData;
import DataSource.VariableData;

public class CheckSingletonPattern implements Check {

	@Override
	public List<Warning> check(ClassRegistry classRegistry) {
		List<Warning> warnings = new LinkedList<>();
		
		for(ClassData classData : classRegistry.getAllClasses()) {
			boolean privStaticSelfField = false;
			boolean privConstructor = false;
			boolean pubReturnSelf = false;
			
			List<VariableData> fields = classData.getFieldVariables();
			for (VariableData field : fields) {
				if (field.getDataType().equals(classData.getName())
						&& field.getModifiers().contains(AccessModifiers.PRIVATE)
						&& field.getModifiers().contains(AccessModifiers.STATIC)) {
					privStaticSelfField = true;
				}
			}
			
			List<MethodData> methods = classData.getMethods();
			for (MethodData methodData : methods) {
				if (methodData.getMethodName().equals("<init>") && methodData.getModifiers().contains(AccessModifiers.PRIVATE)) {
					privConstructor = true;
				} else if (methodData.getMethodName().equals("<init>") && methodData.getModifiers().contains(AccessModifiers.PUBLIC)) {
					privConstructor = false;
					break;
				}
			}
			
			for (MethodData methodData : methods) {
				if (methodData.getReturnType().equals(classData.getName()) && methodData.getModifiers().contains(AccessModifiers.PUBLIC)) {
					pubReturnSelf = true;
				}
			}
			
			
			if (privStaticSelfField && privConstructor && pubReturnSelf) {
				warnings.add(new Warning(new WarningLocation(classData.getName()), WarningType.SINGLETON_PATTERN_FOUND));
			} else if (privStaticSelfField ? (privConstructor ^ pubReturnSelf) : (privConstructor && pubReturnSelf)) {
				warnings.add(new Warning(new WarningLocation(classData.getName()), WarningType.SINGLETON_PATTERN_INCORRECT_ATTEMPT));
			}
		}
		
		
		
		return warnings;
	}

}
;