package DataSource;

import java.util.LinkedList;
import java.util.List;

public class ClassData {

	private String className;
	private String userFriendlyClassName;
	private String superClassName;
	private List<String> interfaces;
	private List<VariableData> variables;
	private List<MethodData> methods;
	private List<AccessModifiers> modifiers;
	private ClassType classType;

	public ClassData(String className) {
		setClassName(className);
		this.variables = new LinkedList<>();
		this.methods = new LinkedList<>();
		this.modifiers = new LinkedList<>();
		this.classType = ClassType.UNKNOWN;
	}

	public ClassData() {
		this("<NoName>");
	}

	public void setName(String name) {
		this.className = name;
	}

	public void addFieldVariable(VariableData vd) {
		this.variables.add(vd);
	}

	public void addMethod(MethodData md) {
		this.methods.add(md);
	}

	public void addModifier(AccessModifiers am) {
		this.modifiers.add(am);
	}

	public void setClassName(String className) {
		this.className = className.replace("/", ".");
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public void setSuperClassName(String superClassName) {
		this.superClassName = superClassName.replace("/", ".");
	}

	public List<String> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
		for(int i = 0; i < interfaces.size(); i++) {
			this.interfaces.set(i, this.interfaces.get(i).replace("/", "."));
		}
	}

	public String getName() {
		return getUserFriendlyClassName();
	}

	public List<VariableData> getFieldVariables() {
		return this.variables;
	}

	public List<MethodData> getMethods() {
		return this.methods;
	}

	public List<AccessModifiers> getModifiers() {
		return this.modifiers;
	}

	public int getMethodParamCount(String methodName) {
		for (MethodData method: this.methods) {
			if (method.getName().equals(methodName)) {
				return method.getParamCount();
			}
		}

		return -1;
	}

	public ClassType getClassType() {
		return classType;
	}

	public void setClassType(ClassType classType) {
		this.classType = classType;
	}

	public String getUserFriendlyClassName() {
		return userFriendlyClassName;
	}

	public void setUserFriendlyClassName(String userFriendlyClassName) {
		this.userFriendlyClassName = userFriendlyClassName;
	}

	public String getShortenedClassName() {
		return userFriendlyClassName.substring(userFriendlyClassName.lastIndexOf('.') + 1);
	}

}
