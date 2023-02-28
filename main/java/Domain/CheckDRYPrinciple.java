package Domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import DataSource.ClassData;
import DataSource.InstructionData;
import DataSource.InstructionType;
import DataSource.LoadConstInstruction;
import DataSource.MethodData;

public class CheckDRYPrinciple implements Check {

	@Override
	public List<Warning> check(ClassRegistry classRegistry) {
		List<Warning> warnings = new LinkedList<>();
		
		HashMap<String, HashSet<String>> constsWithClassLocs = new HashMap<>();

		for (ClassData classData : classRegistry.getAllClasses()) {
			for (MethodData methodData : classData.getMethods()) {
				if (methodData.getMethodName().equals("<init>")) {
					continue;
				}

				List<InstructionData> instructions = methodData.getInstructions();
				LinkedList<LoadConstInstruction> constInsts = new LinkedList<>();
				for (InstructionData inst : instructions) {
					if (inst.getType().equals(InstructionType.LOAD_CONST)) {
						LoadConstInstruction ins = (LoadConstInstruction) inst;
						if (ins.getConstVal() != null) {
							constInsts.add(ins);
						} else {
							continue;
						}
						if (constsWithClassLocs.containsKey(ins.getConstVal())) {
							HashSet<String> locations = constsWithClassLocs.get(ins.getConstVal());
							locations.add(classData.getName());
							constsWithClassLocs.put(ins.getConstVal(), locations);
						} else {
							HashSet<String> locations = new HashSet<>();
							locations.add(classData.getName());
							constsWithClassLocs.put(ins.getConstVal(), locations);
						}
					}
				}

				// Check for duplicate instructions within the current method
				if (checkDuplicateInstructions(constInsts)) {
					Warning inFileWarning = new Warning(new WarningLocation(classData.getName()),
							WarningType.DONT_REPEAT_YOURSELF, "Don't Repeat Yourself violation found in method "
									+ methodData.getMethodName() + " of " + classData.getName());
					if (isUniqueLocation(warnings, inFileWarning)) {
						warnings.add(inFileWarning);
					}
					continue;
				}

				// Check for duplicate instructions across other methods in the same class
				for (MethodData otherMethodData : classData.getMethods()) {
					if (methodData.equals(otherMethodData)) {
						continue;
					}

					LinkedList<LoadConstInstruction> otherConstInsts = new LinkedList<>();
					for (InstructionData inst : otherMethodData.getInstructions()) {
						if (inst.getType().equals(InstructionType.LOAD_CONST)) {
							otherConstInsts.add((LoadConstInstruction) inst);
						}
					}
				}
			}
		}
		
		
		for (Map.Entry<String, HashSet<String>> constLocs : constsWithClassLocs.entrySet()) {
			if (constLocs.getValue().size() > 1) {
				LinkedList<WarningLocation> warningLocs = new LinkedList<>();
				for (String loc : constLocs.getValue()) {
					warningLocs.add(new WarningLocation(loc));
				}
				warnings.add(new Warning(warningLocs,
						WarningType.DONT_REPEAT_YOURSELF_CROSS,
						warningBuilder(constLocs.getKey(), warningLocs)));
			}
		}

		return warnings;
	}
	
	private String warningBuilder(String constFound, LinkedList<WarningLocation> locs) {
		String warningText = "Don't Repeat Yourself Cross Violation of constant: " + constFound + " found at: ";
		for (WarningLocation location : locs) {
			warningText += location.locationToString();
		}
		return warningText;
	}

	private boolean isUniqueLocation(List<Warning> warnings, Warning proposedWarning) {
		for (Warning warning : warnings) {
			if (warning.sameLocations(proposedWarning))
				return false;
		}
		return true;
	}

	private boolean checkDuplicateInstructions(List<LoadConstInstruction> insts) {
	    HashSet<String> distinctVals = new HashSet<>();
	    HashSet<LoadConstInstruction> seenInsts = new HashSet<>();
	    
	    for (LoadConstInstruction inst : insts) {
	        if (seenInsts.contains(inst)) {
	            continue;
	        }
	        
	        if (inst.getConstVal() != null && !distinctVals.add(inst.getConstVal())) {
	            return true;
	        }
	        
	        seenInsts.add(inst);
	    }
	    
	    return false;
	}

}
