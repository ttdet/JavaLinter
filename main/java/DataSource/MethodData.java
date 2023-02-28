package DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MethodData {

	private String methodName;
	private List<AccessModifiers> modifiers;
	private Map<Integer,VariableData> localVariables;
	private List<InstructionData> instructions;
	private List<String> params;
	private String returnType;
	private MethodType methodType;

	public MethodData(String name) {
		this.methodName = name;
		this.modifiers = new LinkedList<>();
		this.localVariables = new HashMap<>();
		this.instructions = new ArrayList<>();
		this.params = new LinkedList<>();
		this.returnType = "";
		this.methodType = MethodType.UNKNOWN;
	}

	public MethodData() {
		this("<NoName>");
	}

	public void setName(String name) {
		this.methodName = name;
	}

	public void setReturnType(String type) {
		this.returnType = type;
	}

	public void addModifier(AccessModifiers am) {
		this.modifiers.add(am);
	}

	public void addLocalVariable(int index, VariableData varData) {
		this.localVariables.put(index, varData);
	}
	

	public void addInstruction(InstructionData insData) {
		this.instructions.add(insData);
	}

	public void addParameter(String paramType) {
		this.params.add(paramType);
	}

	public String getName() {
		return this.methodName;
	}

	public List<AccessModifiers> getModifiers() {
		return this.modifiers;
	}

	public Map<Integer,VariableData> getLocalVariables() {
		return this.localVariables;
	}

	public List<InstructionData> getInstructions() {
		return this.instructions;
	}

	public List<String> getParams() {
		return this.params;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getReturnType() {
		return returnType;
  }
  
	public int getParamCount() {
		return this.params.size();
	}

	public void setInstructions(List<InstructionData> instList) {
		this.instructions = instList;
	}

	public String getSignature() {

		StringBuilder sb = new StringBuilder();

		sb.append(returnType).append(" ");
		sb.append(methodName).append("(");

		if(params.size() == 0)
			return sb.append(")").toString();

		for(String type : params) {
			sb.append(type).append(", ");
		}

		return sb.replace(sb.length() - 2, sb.length(), ")").toString();

	}

	public MethodType getMethodType() {
		return methodType;
	}

	public void setMethodType(MethodType methodType) {
		this.methodType = methodType;
	}

	public boolean isStatic() {
		return this.modifiers.contains(AccessModifiers.STATIC);
	}
}
