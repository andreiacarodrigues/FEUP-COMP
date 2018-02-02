import java.util.Arrays;

public class NormalOpData
{
    public Token[] left;
    public Token[] right;
    public Token instruction;

    public NormalOpData(Token[] left, Token[] right, Token instruction)
    {
        this.left = left;
        this.right = right;
        this.instruction = instruction;
    }

    public NormalOpData(NormalOpData old)
    {
        this.left = Arrays.copyOf(old.left, old.left.length);
        this.right = Arrays.copyOf(old.right, old.right.length);
        this.instruction = old.instruction;
    }
}
