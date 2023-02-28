package Domain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import DataSource.ClassData;
import DataSource.InstructionData;
import DataSource.InstructionType;
import DataSource.MethodCallInstruction;
import DataSource.MethodData;
import DataSource.VarInstruction;

/**
 * @author Qingyuan Jiao
 * Detects train wreck. 
 * IMPLICIT_TRAIN_WRECK is a train wreck in form of:
 * ClassA a = b.getClassA();
 * a.foo();
 * 
 * CALL_CHAIN is a train wreck in form of :
 * a.foo().bar();
 * 
 * This check has the following limitations:
 * 1. It can only detect train wrecks caused by a local variable within a method.
 * 2. Only classes in the to-check directory (i.e. the classes in the classRegistry argument) will be considered. 
 *      Train wrecks caused by imported libraries that are not in the directory will be skipped. 
 * 3. The check might fail when the result of a method call is passed in as an argument (e.g. a.methodA(b.getA()))
 * 4. It's not intelligent enough to tell if the class is a mere data structure of not. It records all train wrecks. 
 *      
 */
public class CheckTrainWreck implements Check {

    public CheckTrainWreck() {

    }

    @Override
    public List<Warning> check(ClassRegistry classRegistry) {
        List<Warning> warnings = new LinkedList<>();

        for(ClassData classData : classRegistry.getAllClasses()) {
            List<MethodData> methods = classData.getMethods();
            for (MethodData methodData: methods) {
                warnings.addAll(checkMethod(classRegistry, classData.getName(), classData.getUserFriendlyClassName(), methodData));
            }
        }

        return warnings;
    }

    /**
     * Checks a method for train wrecks.
     * @param classRegistry
     * @param className
     * @param userFClassName
     * @param methodData
     * @return
     */
    private List<Warning> checkMethod(ClassRegistry classRegistry, String className, String userFClassName, MethodData methodData) {
        List<Warning> warnings = new LinkedList<>();
        List<InstructionData> instList = methodData.getInstructions();
        
        for (int i = 0; i < instList.size(); i++) {
            InstructionData currInst = instList.get(i);
            if (currInst.getType() == InstructionType.METHOD_CALL) {
                MethodCallInstruction methodCallInst = (MethodCallInstruction) currInst;
                if (!methodCallInst.getMethodName().equals("<init>") && classInScope(classRegistry, methodCallInst.getMethodOwner())) {
                    String methodOwner = methodCallInst.getMethodOwner();
                    String methodName = methodCallInst.getMethodName();
                    int paramCount = classRegistry.get(methodOwner).getMethodParamCount(methodName);
                    int callerIndex = i - paramCount - 1;
                    if (callerIndex < 0) continue;
                    InstructionData loadCallerSlot = instList.get(callerIndex);
                    if (loadCallerSlot.getType() == InstructionType.METHOD_CALL) {
                        MethodCallInstruction callerInitSlot = (MethodCallInstruction) loadCallerSlot;
                        if (callerInitSlot.getMethodName().equals("<init>") || callerInitSlot.getMethodName().equals("<clinit>")) {
                            //in form of (new A()).methodA();
                            continue;
                        } else {
                            WarningLocation location = new WarningLocation(userFClassName, methodData.getSignature());
                            Warning newWarning = new Warning(location, WarningType.CALL_CHAIN);
                            String text = "call chain when calling " + methodName + "()";
                            newWarning.setWarningText(text);
                            warnings.add(newWarning);
                        }
                    } else if (loadCallerSlot.getType() == InstructionType.VAR_INS) {
                        VarInstruction loadCallerInst = (VarInstruction) loadCallerSlot;
                        int varIndex = loadCallerInst.getVar();
                        if (!isVarInstantiated(varIndex, i, instList)) {
                            //caller is a variable declared in the method's body, not an argument
                            //AND caller object is derived from calling another method
                            WarningLocation location = new WarningLocation(userFClassName, methodData.getSignature());
                            Warning newWarning = new Warning(location, WarningType.IMPLICIT_TRAIN_WRECK);
                            String text = "train wreck when calling " + methodName + "()";
                            newWarning.setWarningText(text);
                            warnings.add(newWarning);
                        }     
                    }
                  
                }
            }
        }

        return warnings;

    }

    /**
     * Checks if the class is in the to-check list.
     * @param classRegistry
     * @param className
     * @return
     */
    private boolean classInScope(ClassRegistry classRegistry, String className) {
        return classRegistry.get(className) != null;
    }


    /**
     * Checks whether an object variable is derived from instantiation or through return value of another method call. 
     * @param varIndex
     * @param lineNo
     * @param instList
     * @return true if the object variable is instantiated. 
     */
    private boolean isVarInstantiated(int varIndex, int lineNo, List<InstructionData> instList) {
        for (int j = lineNo - 1; j >= 0; j--) { //loop back the find when was the caller was stored
            InstructionData inst = instList.get(j);
            if (inst.getType() == InstructionType.VAR_INS) {
                VarInstruction varInst = (VarInstruction) inst;
                if (varInst.getVar() == varIndex && varInst.getOpcodeType().equals("STORE")) {
                    InstructionData prevInst = instList.get(j - 1);
                    if (prevInst.getType() == InstructionType.VAR_INS) {
                        VarInstruction prevAssignment = (VarInstruction) prevInst;
                        int assignedVar = prevAssignment.getVar();
                        return isVarInstantiated(assignedVar, j, instList);
                    } else {
                        MethodCallInstruction methodCallInst = (MethodCallInstruction) prevInst;
                        if (methodCallInst.getMethodName().equals("<init>") || methodCallInst.getMethodName().equals("<clinit>")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

} 
