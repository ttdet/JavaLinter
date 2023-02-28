package DataSource;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ClassParser {

	private MethodParser methodParser;

	public ClassParser() {
		this.methodParser = new MethodParser();
	}


	public ClassData parseClassData(File f){
		byte[] arr = getBytes(f);

		if(arr == null) {
			throw new RuntimeException("Failed to open file for parsing " + f);
		}

		ClassReader cr = new ClassReader(arr);
		ClassNode cn = new ClassNode();
		cr.accept(cn, ClassReader.EXPAND_FRAMES);

		return parseClassDataFromNode(cn);

		

	}

	public ClassData parseClassDataFromNode(ClassNode cn) {
		ClassData classData = new ClassData();

		classData.setClassName(cn.name);
		classData.setUserFriendlyClassName(Type.getObjectType(cn.name).getClassName());
		classData.setSuperClassName(cn.superName);
		classData.setInterfaces(cn.interfaces);

		for(MethodNode methodNode : cn.methods) {
			classData.addMethod(this.methodParser.parseMethodData(methodNode));
		}

		for(FieldNode fieldNode : cn.fields) {
			classData.addFieldVariable(parseFieldVariable(fieldNode));
		}

		if((cn.access & Opcodes.ACC_PUBLIC) != 0)
			classData.addModifier(AccessModifiers.PUBLIC);
		if((cn.access & Opcodes.ACC_PRIVATE) != 0)
			classData.addModifier(AccessModifiers.PRIVATE);
		if((cn.access & Opcodes.ACC_PROTECTED) != 0)
			classData.addModifier(AccessModifiers.PROTECTED);

		if((cn.access & Opcodes.ACC_INTERFACE) != 0)
			classData.setClassType(ClassType.INTERFACE);
		else if((cn.access & Opcodes.ACC_ABSTRACT) != 0)
			classData.setClassType(ClassType.ABSTRACT);
		else
			classData.setClassType(ClassType.CONCRETE);

		return classData;
	}

	

	private byte[] getBytes(File file) {
		FileInputStream fl;
		try {
			fl = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		byte[] arr = new byte[(int)file.length()];
		try {
			fl.read(arr);
			fl.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return arr;
	}

	private VariableData parseFieldVariable(FieldNode fieldNode) {
		VariableData variableData = new VariableData();
		
		List<String> dataTypes = getVariableDataTypes(fieldNode);
		variableData.setListDataType(dataTypes);
		variableData.setName(fieldNode.name);
		boolean hasAccessModifier = false;
		if((fieldNode.access & Opcodes.ACC_PUBLIC) != 0) {
			variableData.addModifier(AccessModifiers.PUBLIC);
			hasAccessModifier = true;
		}
		if((fieldNode.access & Opcodes.ACC_PRIVATE) != 0) {
			variableData.addModifier(AccessModifiers.PRIVATE);
			hasAccessModifier = true;
		}
		if((fieldNode.access & Opcodes.ACC_PROTECTED) != 0) {
			variableData.addModifier(AccessModifiers.PROTECTED);
			hasAccessModifier = true;
		}
		if((fieldNode.access & Opcodes.ACC_STATIC) != 0)
			variableData.addModifier(AccessModifiers.STATIC);
		if((fieldNode.access & Opcodes.ACC_FINAL) != 0)
			variableData.addModifier(AccessModifiers.FINAL);
		if(!hasAccessModifier) {
			variableData.addModifier(AccessModifiers.DEFAULT);
		}

		return variableData;
	}


	private List<String> getVariableDataTypes(FieldNode fieldNode) {
		// TODO Auto-generated method stub
		List<String> dataTypes = new LinkedList<>();
		dataTypes.add(Type.getType(fieldNode.desc).getClassName());
		if(fieldNode.signature!=null) {
			String sig = fieldNode.signature;
			String[] classes = sig.split("[<,;>]");
			for(int i = 1;i<classes.length;i++) {
				dataTypes.add(Type.getType(classes[i] + ";").getClassName());
			}
		}
		return dataTypes;
	}


	public List<ClassData> recursivelyParseAllClassFilesInDirectory(String directory) {
		List<ClassData> classDataList = new LinkedList<>();
		recursivelyParseAllClassFilesInDirectory(directory, classDataList);
		return classDataList;
	}

	public void recursivelyParseAllClassFilesInDirectory(String directory, List<ClassData> fileList) {
			File root = new File(directory);
			File[] list = root.listFiles();

			if (list == null) return;

			for (File f : list) {
				if ( f.isDirectory() ) {
					recursivelyParseAllClassFilesInDirectory(f.getAbsolutePath(), fileList);
				}
				else if(f.getName().endsWith(".class")) {
					fileList.add(parseClassData(f));
				}
			}

	}
}
