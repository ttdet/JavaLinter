package Domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import DataSource.ClassData;
import DataSource.InstructionData;
import DataSource.InstructionType;
import DataSource.MethodCallInstruction;
import DataSource.MethodData;
import DataSource.VariableData;

public class CheckHollywoodPrinciple implements Check {

	private ClassRegistry cr;
	private List<Warning> warnings;
	private Map<String, List<String>> dependencies;

	@Override
	public List<Warning> check(ClassRegistry classRegistry) {
		cr = classRegistry;
		warnings = new ArrayList<Warning>();
		dependencies = new HashMap<>();
		// create dependency chart

		for (ClassData cd : classRegistry.getAllClasses()) {
			this.dependencies.put(cd.getName(), new ArrayList<String>());
		}
		for (ClassData cd : classRegistry.getAllClasses()) {
			generateDependencies(cd);
		}
		cleanDependencies();

		// check each class for circular dependency
		for (String key : dependencies.keySet()) {
			checkCallings(key);
		}
//		for (String s : dependencies.keySet()) {
//			System.out.println(s + " has dependencies on : ");
//			for (String str : dependencies.get(s)) {
//				System.out.println(str);
//			}
//		}

		// check each class for improper super

		return this.warnings;
	}

	private void checkCallings(String key) {
		// TODO Auto-generated method stub
		for (String d : dependencies.get(key)) {
			if (dependencies.get(d).contains(key)) {
				createCircleWarning(key, d);
			}
		}
	}

	private void createCircleWarning(String c1, String c2) {
		List<WarningLocation> locations = new LinkedList<>();
		locations.add(new WarningLocation(c1));
		locations.add(new WarningLocation(c2));
		String str = c1 + " Depends on " + c2 + " and vice versa";
		if (checkCircleWarningNotExist(c1, c2)) {
			Warning w = new Warning(locations, WarningType.HOLLYWOOD_PRINCIPLE_VIOLATION, str);
			warnings.add(w);
		}
	}

	private boolean checkCircleWarningNotExist(String c1, String c2) {
		String str = c2 + " Depends on " + c1 + " and vice versa";
		for (Warning w : warnings) {
			if (w.getWarningText().equals(str)) {
				return false;
			}
		}
		return true;
	}

	private void generateDependencies(ClassData cd) {
		// TODO Auto-generated method stub
//		List<ClassData> dependents = new ArrayList<>();
		interfaceArrows(cd);
		List<VariableData> fields = cd.getFieldVariables();
		HashMap<Integer, VariableData> fieldss = new HashMap<>();
		for (int i = 0; i < fields.size(); i++) {
			fieldss.put(i, fields.get(i));
		}
		checkVars(fieldss, cd);
		for (MethodData md : cd.getMethods()) {
			dependenciesFromMethod(md, cd);
		}
	}

	private void dependenciesFromMethod(MethodData md, ClassData cd) {
		checkVars(md.getLocalVariables(), cd);
//		String s = md.getLocalVariables().get(0).getDataType();
		checkInstructions(md.getInstructions(), md, cd);
		checkTypes(md.getParams(), cd);
		checkType(md.getReturnType(), cd);
	}

	private void interfaceArrows(ClassData cd) {
		List<String> inter = cd.getInterfaces();
		for (String str : inter) {
//			dependencies.get(str).add(cd.getName());
			addDependency(str, cd.getName());
		}
	}

	private void checkVars(Map<Integer, VariableData> map, ClassData cd) {
		for (Integer i : map.keySet()) {
			VariableData v = map.get(i);
			if (!isPrimitive(v.getDataType())) {
				addDependency(cd.getName(), v.getDataType());
//				this.dependencies.get(cd.getName()).add(v.getDataType());
			}
		}
	}

	private void checkTypes(List<String> params, ClassData cd) {
		// TODO Auto-generated method stub
		for (String v : params) {
			if (!isPrimitive(v)) {
//				this.dependencies.get(cd.getName()).add(v);
				addDependency(cd.getName(), v);
			}
		}
	}

	private void checkType(String type, ClassData cd) {
		if (!isPrimitive(type)) {
//			this.dependencies.get(cd.getName()).add(type);
			addDependency(cd.getName(), type);
		}
	}

	private void checkInstructions(List<InstructionData> instr, MethodData md, ClassData cd) {
		for (InstructionData id : instr) {
			if (id.getType().equals(InstructionType.METHOD_CALL)
					|| id.getType().equals(InstructionType.SELF_METHOD_CALL)) {
//				System.out.println(id.toString());
				MethodCallInstruction i = (MethodCallInstruction) id;
//				dependencies.get(cd.getName()).add(i.getMethodOwner());
				addDependency(cd.getName(), i.getMethodOwner());

				if (i.getMethodOwner().equals(cd.getSuperClassName()) && !i.getMethodName().equals(md.getName())) {
					generateBadSuperWarning(i, md, cd);
				}
			}
		}
	}

	private void generateBadSuperWarning(MethodCallInstruction i, MethodData md, ClassData cd) {
		// TODO Auto-generated method stub
		List<WarningLocation> locations = new LinkedList<>();
		locations.add(new WarningLocation(cd.getName()));
		String str = cd.getName() + " is calling a super method in a non decorator way in method " + md.getName()
				+ "\nCalls super method " + i.getMethodName();
		if (checkSuperWarningNotExist(i, md, cd)) {
			Warning w = new Warning(locations, WarningType.HOLLYWOOD_PRINCIPLE_VIOLATION, str);
			warnings.add(w);
		}
	}

	private boolean checkSuperWarningNotExist(MethodCallInstruction i, MethodData md, ClassData cd) {
		// TODO Auto-generated method stub
		String str = cd.getName() + " is calling a super method in a non decorator way in method " + md.getName()
				+ "\nCalls super method " + i.getMethodName();
		for (Warning w : warnings) {
			if (str.equals(w.getWarningText())) {
				return false;
			}
		}
		return true;
	}

	private void addDependency(String main, String dependsOn) {
		if (!dependencies.get(main).contains(dependsOn)) {
			dependencies.get(main).add(dependsOn);
		}
	}

	private boolean isPrimitive(String str) {
		switch (str.toLowerCase()) {
		case "int":
		case "integer":
		case "double":
		case "boolean":
		case "byte":
		case "char":
		case "short":
		case "float":
		case "long":
		case "java.lang.string":
			return true;
		default:
			return false;
		}
	}

	// removes the "dependency" on List, other Java classes. While they are real,
	// they don't really matter
	private void cleanDependencies() {
		for (String s : this.dependencies.keySet()) {
			for (int i = 0; i < dependencies.get(s).size(); i++) {
				if (!this.cr.getAllClasses().contains(cr.get(dependencies.get(s).get(i)))
						|| s.equals(dependencies.get(s).get(i))) {
					dependencies.get(s).remove(i);
					i--;
				}
			}
		}
	}

}
