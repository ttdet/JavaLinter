package DataSource;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class InstructionParser {

    public InstructionData parseInstructionData(AbstractInsnNode insNode) {
        if (insNode instanceof MethodInsnNode) {
            MethodInsnNode methodInsNode = (MethodInsnNode) insNode;
            MethodCallInstruction insData = new MethodCallInstruction(InstructionType.METHOD_CALL, methodInsNode.name, this.getFriendlyName(methodInsNode.owner));

            if(methodInsNode.getPrevious() instanceof VarInsnNode && ((VarInsnNode)methodInsNode.getPrevious()).var == 0) {
                insData.setInstructionType(InstructionType.SELF_METHOD_CALL);
            }

            if(insNode.getPrevious() instanceof FieldInsnNode) {
                insData.setVarName(((FieldInsnNode)insNode.getPrevious()).name);
            }

            return insData;
            
        } else if (insNode instanceof VarInsnNode) {
            VarInsnNode varIns = (VarInsnNode) insNode;
            int opcode = insNode.getOpcode();
            InstructionData insData;

            if (opcode <= 53 && opcode >= 21) {
                insData = new VarInstruction(InstructionType.VAR_INS, varIns.var, "LOAD");
            } else if (opcode <= 86 && opcode >= 22) {
                insData = new VarInstruction(InstructionType.VAR_INS, varIns.var, "STORE");
            } else {
                insData = new VarInstruction(InstructionType.VAR_INS, varIns.var, "unknown");
            }
            //System.out.println(insNode.getOpcode());
            return insData;
        } else if (insNode.getOpcode() > 0 && insNode.getOpcode() <= 20){
        	int opcode = insNode.getOpcode();
        	if (opcode == Opcodes.LDC) {
        		Object cst = ((LdcInsnNode) insNode).cst;
        		if (cst instanceof String) {
        			return new LoadConstInstruction(opcode, (String) cst);
        		}
        	}
            return new LoadConstInstruction(insNode.getOpcode());
        } else {
            return null;
        }
    }

    private String getFriendlyName(String name) {
        return name.replace('/', '.');
    }


}
