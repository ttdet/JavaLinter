package DataSource;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodParser {

	private InstructionParser instParser;

	public MethodParser() {
		this.instParser = new InstructionParser();
	}


	public MethodData parseMethodData(MethodNode mn) {
		MethodData methodData = new MethodData(mn.name);
		String desc = mn.desc;
		Type[] paramTypes = Type.getArgumentTypes(desc);
		for (Type paramType: paramTypes) {
			methodData.addParameter(paramType.getClassName());
		}

		methodData.setReturnType(Type.getReturnType(mn.desc).getClassName());

		if((mn.access & Opcodes.ACC_PUBLIC) != 0)
			methodData.addModifier(AccessModifiers.PUBLIC);
		if((mn.access & Opcodes.ACC_PRIVATE) != 0)
			methodData.addModifier(AccessModifiers.PRIVATE);
		if((mn.access & Opcodes.ACC_PROTECTED) != 0)
			methodData.addModifier(AccessModifiers.PROTECTED);
		if((mn.access & Opcodes.ACC_STATIC) != 0)
			methodData.addModifier(AccessModifiers.STATIC);

		if((mn.access & Opcodes.ACC_ABSTRACT) != 0) {
			methodData.setMethodType(MethodType.ABSTRACT);
			return methodData;
		}

		for (LocalVariableNode localVarNode: mn.localVariables) {
//			System.out.println(localVarNode.name + " " + localVarNode.index);
			VariableData var = new VariableData(localVarNode.name, localVarNode.desc);
			methodData.addLocalVariable(localVarNode.index, var);
		}
		for (int i = 0; i < mn.instructions.size(); i++) {
			AbstractInsnNode insNode = mn.instructions.get(i);
			InstructionData insData = this.instParser.parseInstructionData(insNode);
			if (insData != null) {
				methodData.addInstruction(insData);
			}
		}

		methodData.setMethodType(MethodType.IMPLEMENTED);

		return methodData;
		
	}
}
