package DataSource;

public class LoadConstInstruction extends InstructionData{

    private int type;
    private String constVal;

    public LoadConstInstruction(int type) {
        super(InstructionType.LOAD_CONST);
        this.type = type;
    }

    public LoadConstInstruction(int type, String constVal) {
        super(InstructionType.LOAD_CONST);
        this.type = type;
        this.constVal = constVal;
    }

    public String toString() {
        return super.toString() + " " + this.type;
    }
    
    public String getConstVal() {
    	return this.constVal;
    }

}
