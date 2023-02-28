package DataSource;

import java.util.LinkedList;
import java.util.List;

public class VariableData {
	private String varName;
	private List<String> dataType;
	private List<AccessModifiers> modifiers;

	public VariableData(String name, String desc) {
		//this.isClassField = false;
		//this.isLocalVariable = false;
		this.varName = name;
		this.dataType = new LinkedList<String>();
		this.dataType.add(desc);
		this.modifiers = new LinkedList<>();
	}

	public VariableData() {
		//this.isClassField = false;
		//this.isLocalVariable = false;
		this.varName = "";
		this.dataType = new LinkedList<String>();
		this.modifiers = new LinkedList<>();
	}
	
	public VariableData(String name, List<String> desc) {
		//this.isClassField = false;
		//this.isLocalVariable = false;
		this.varName = name;
		this.dataType = desc;
		this.modifiers = new LinkedList<>();
	}

	public void setName(String name) {
		this.varName = name;
	}

	public String getName() {
		return varName;
	}

	public void addModifier(AccessModifiers am) {
		this.modifiers.add(am);
	}

	public void setDataType(String type) {
		this.dataType = new LinkedList<>();
		dataType.add(type);
	}
	
	public void setListDataType(List<String> types) {
		dataType = types;
	}
		
	public void addDataType(String type) {
		dataType.add(type);
	}

	public String toString() {
		return "var name: " + this.varName + ", dataType: " + this.dataType;
	}


	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}
	
	public String getDataType() {
		return dataType.get(0);
	}

	public List<String> getDataTypes() {
		return dataType;
	}

	public List<AccessModifiers> getModifiers() {
		return modifiers;
	}

	public void setModifiers(List<AccessModifiers> modifiers) {
		this.modifiers = modifiers;
	}
}
