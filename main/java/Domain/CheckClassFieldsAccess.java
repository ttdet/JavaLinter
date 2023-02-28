package Domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DataSource.AccessModifiers;
import DataSource.ClassData;
import DataSource.VariableData;

public class CheckClassFieldsAccess implements Check {

	@Override
	public List<Warning> check(ClassRegistry classRegistry) {
		// TODO Auto-generated method stub
		List<Warning> warnings = new ArrayList<>();
		Map<ClassData, List<VariableData>> errorsInFile = new HashMap<>();
		checkVars(errorsInFile, classRegistry);
		createWarnings(errorsInFile,warnings);
		return warnings;
	}
	
	private void checkVars(Map<ClassData, List<VariableData>> errorsInFile,ClassRegistry classRegistry) {
		for (ClassData f : classRegistry.getAllClasses()) {
			errorsInFile.put(f,new ArrayList<VariableData>());
			for (VariableData v : f.getFieldVariables()) {
				if(!checkIndividualVar(v)) {
					errorsInFile.get(f).add(v);
				}
			}
		}
	}

	private boolean checkIndividualVar(VariableData v) {
		// Boolean return is false for a bad access modifier, and true for a good access
		// modifier.
		if (v.getModifiers().contains(AccessModifiers.PUBLIC)) {
			return false;
		} else if (v.getModifiers().contains(AccessModifiers.DEFAULT)) {
			return false;
		} else {
			return true;
		}
	}

	private void createWarnings(Map<ClassData, List<VariableData>> errorsInFile, List<Warning> warnings) {
		for(ClassData cd : errorsInFile.keySet()) {
			List<VariableData> vars = errorsInFile.get(cd);
			if(vars.size()!=0) {
				String str = "Bad access modifiers on fields:\nClass: " + cd.getName() + "\nFields: ";
				for(int i = 0;i<vars.size();i++) {
					str = str + vars.get(i).getName();
					if(i!=vars.size()-1) {
						str = str + ", ";
					}
				}
				Warning w = new Warning(WarningType.BAD_CLASS_FIELD_ACCESS,str);
				warnings.add(w);
			}
		}
	}
}
