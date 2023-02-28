package DataSource;

public class MethodCallInstruction extends InstructionData{
    private String methodName;
    private String methodOwner;
    private String varName;

    public MethodCallInstruction(InstructionType type, String name, String owner) {
        super(type);
        this.methodName = name;
        this.methodOwner = owner;
        this.varName = "";
    }

    public void setName(String name) {
        this.methodName = name;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public void setOwner(String owner) {
        this.methodOwner = owner;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String getMethodOwner() {
        return this.methodOwner;
    }

    @Override
    public String toString() {
        return super.toString() + ", method name: "+ this.methodName + ", method owner: " + this.methodOwner;
    }

    public String getVarName() {
        return varName;
    }
}
