import java.util.Arrays;

public class ControlFlowOpData
{
    public String[] labels;
    public String register;
    public String instruction;

    public ControlFlowOpData(String label)
    {
        labels = new String[1];
        labels[0] = label;
        this.register = null;
        this.instruction = "jumpl";
    }

    public ControlFlowOpData(String label1, String label2, String register)
    {
        labels = new String[2];
        labels[0] = label1;
        labels[1] = label2;
        this.register = register;
        this.instruction = "cbr";
    }

    public ControlFlowOpData(ControlFlowOpData old)
    {
        this.labels = Arrays.copyOf(old.labels, old.labels.length);
        this.register = old.register;
        this.instruction = old.instruction;
    }
}
