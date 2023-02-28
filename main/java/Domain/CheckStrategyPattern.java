package Domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import DataSource.ClassData;
import DataSource.ClassType;
import DataSource.MethodData;
import DataSource.MethodType;
import DataSource.VariableData;

public class CheckStrategyPattern implements Check {

	private List<Warning> warnings;
	private ClassRegistry classRegistry;
	private HashMap<String, List<String>> interfaces;
	private HashMap<String, List<String>> abstractandSuperClasses;

	@Override
	public List<Warning> check(ClassRegistry classRegistry) {
		// TODO Auto-generated method stub
		this.classRegistry = classRegistry;
		this.warnings = new ArrayList<>();
		checkFields();

		interfaces = getInterfaces();
		checkInterfaceAndMethods();
		abstractandSuperClasses = getAbstractAndSuperClasses();
		checkAbstractClasses();
		return warnings;
	}

	private void checkAbstractClasses() {
		// TODO Auto-generated method stub
		if (abstractandSuperClasses.size() == 0) {
			return;
		}
		checkInterfacesorAbstractClasses(abstractandSuperClasses, "superclass");
		for (String s : abstractandSuperClasses.keySet()) {
			if (s.equals("java.lang.Object"))
				break;
			List<MethodData> commonMethods = new ArrayList<>();
			for (MethodData md : classRegistry.get(s).getMethods()) {
				if (!md.getMethodType().equals(MethodType.ABSTRACT) && !md.getName().equals("<init>")) {
					commonMethods.add(md);
				}
			}
			if (commonMethods.size() > 0) {

				String str = "Good use of polymorphism moving methods with identical behavior: ";
				for (MethodData md : commonMethods) {
					str = str + md.getName() + ", ";
				}
				str = str.substring(0, str.length() - 2) + " into the superclass";
				createWarning(WarningType.STRATEGY_PATTERN_FOUND, classRegistry.get(s), str);
			}
		}
	}

	private void checkInterfacesorAbstractClasses(HashMap<String, List<String>> lsit, String str) {
		// TODO Auto-generated method stub
		for (String topClass : lsit.keySet()) {
			if (topClass.equals("java.lang.Object"))
				break;
			List<String> subclasses = lsit.get(topClass);
			List<MethodData> sharedMethods = new ArrayList<>();
			sharedMethods.addAll(classRegistry.get(topClass).getMethods());
			for (String subclass : subclasses) {
				ClassData cd = classRegistry.get(subclass);
				for (MethodData m : sharedMethods) {
					if (!methodInList(cd.getMethods(), m)) {
						sharedMethods.remove(m);
					}
				}
			}
			if (sharedMethods.size() == classRegistry.get(topClass).getMethods().size()) {
				createWarning(WarningType.STRATEGY_PATTERN_FOUND, classRegistry.get(topClass),
						"Well implemented polymorphism, all same methods are in " + str);
			} else {
				createWarning(WarningType.STRATEGY_PATTERN_WARNING, classRegistry.get(topClass),
						"Consider Moving methods that are implemented in each subclass into the " + str);
			}

		}
	}

	private void checkInterfaceAndMethods() {
		// TODO checks to see if each class implementing an interface has a method
		// called X that is not in the interface. if so, give a warning, if not give a
		// Checkmark
		if (interfaces.size() == 0) {
			return;
		}
		checkInterfacesorAbstractClasses(interfaces, "interface");
	}

	private boolean methodInList(List<MethodData> list, MethodData md) {
		for (MethodData m : list) {
			if (m.getName().equals(md.getName()) && m.getParams().equals(md.getParams())) {
				return true;
			}
		}
		return false;
	}

	private void checkFields() {
		for (ClassData cd : classRegistry.getAllClasses()) {
			List<VariableData> vars = cd.getFieldVariables();
			for (VariableData v : vars) {
				if (!isPrimitive(v.getDataType()) && v.getDataTypes().size() != 1) {
					for (String s : v.getDataTypes()) {
						if (!isPrimitive(s)) {
							try {
								ClassData c = classRegistry.get(s);
								if (c.getClassType() == ClassType.INTERFACE) {
									createWarning(WarningType.STRATEGY_PATTERN_FOUND, cd,
											"coding to an interface not an implementation seen in field "
													+ v.getName());
								} else if (c.getInterfaces().size() != 0 || c.getSuperClassName() != null) {
									createWarning(WarningType.STRATEGY_PATTERN_WARNING, cd,
											"Consider Storing fields as an interface for increased polymorphism in field "
													+ v.getName());
								}
							} catch (Exception e) {
								// do nothing
							}
						}
					}
				}
			}
		}

	}

	private void createWarning(WarningType wt, ClassData cd, String str) {
		// TODO Auto-generated method stub
		List<WarningLocation> places = new ArrayList<>();
		places.add(new WarningLocation(cd.getName()));
		Warning w = new Warning(places, wt, str);
		this.warnings.add(w);
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

	private HashMap<String, List<String>> getInterfaces() {
		HashMap<String, List<String>> interfaces = new HashMap<>();
		for (ClassData cd : classRegistry.getAllClasses()) {
			if (cd.getClassType() == ClassType.INTERFACE) {
				interfaces.put(cd.getName(), new ArrayList<String>());
			}
		}
		for (ClassData cd : classRegistry.getAllClasses()) {
			List<String> ints = cd.getInterfaces();
			if (ints.size() != 0) {
				for (String s : ints) {
					interfaces.get(s).add(cd.getName());
				}
			}
		}
		return interfaces;
	}

	private HashMap<String, List<String>> getAbstractAndSuperClasses() {
		HashMap<String, List<String>> ac = new HashMap<>();
//		for (ClassData cd : classRegistry.getAllClasses()) {
//			if (cd.getClassType() == ClassType.ABSTRACT) {
//				ac.put(cd.getName(), new ArrayList<String>());
//			}
//		}
		for (ClassData cd : classRegistry.getAllClasses()) {
			String superclass = cd.getSuperClassName();
			if (superclass != null && ac.get(superclass) != null) {
				ac.get(superclass).add(cd.getName());
			} else if (superclass != null && ac.get(superclass) == null) {
				ArrayList<String> x = new ArrayList<>();
				x.add(cd.getName());
				ac.put(superclass, x);
			}
		}
		return ac;
	}
}