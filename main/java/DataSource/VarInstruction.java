package DataSource;

public class VarInstruction extends InstructionData{
    //the operand
    private int var;
    private String type;

    public VarInstruction(InstructionType type, int var, String opcodeType) {
        super(type);
        this.var = var;
        this.type = opcodeType;
    }

    public VarInstruction(InstructionType type, int var) {
        super(type);
        this.var = var;
        this.type = "unknown";
    }

    public void setVar(int var) {
        this.var = var;
    }

    public int getVar() {
        return this.var;
    }

    public String getOpcodeType() {
        return this.type;
    }

    @Override
    public String toString() {
        return super.toString() + ", var index: " + this.var + ", ins type: " + this.type;
    }
}
