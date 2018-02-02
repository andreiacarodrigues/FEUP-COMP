import java.util.HashSet;
import java.util.Set;

public class MyInstruction
{
    public final int id;
    public final String label;
    Set<String> use = new HashSet<>();
    Set<String> def = new HashSet<>();
    Set<String> in = new HashSet<>();
    Set<String> out = new HashSet<>();
    public final String[] move;
    public final boolean controlFlow;
    public final NormalOpData normalData;
    public final ControlFlowOpData flowData;
    private int[] navigateTo = {-1, -1};

    public MyInstruction(int id, String label, NormalOpData data)
    {
        this.id = id;
        this.label = label;
        this.controlFlow = false;
        this.normalData = data;
        this.flowData = null;

        for(Token token : data.left)
        {
            if(token.kind == ILOCParserConstants.REGISTER)
            {
                use.add(token.image);
            }
        }

        boolean check1 = data.instruction.image.equals("store");
        boolean check2 = data.instruction.image.equals("storeAI");
        boolean check3 = data.instruction.image.equals("storeA0");
        boolean check4 = data.instruction.image.equals("cstore");
        boolean check5 = data.instruction.image.equals("cstoreAI");
        boolean check6 = data.instruction.image.equals("cstoreA0");


        if(check1 || check2 || check3 || check4 || check5 || check6)
        {
            for(Token token : data.right)
            {
                if(token.kind == ILOCParserConstants.REGISTER)
                {
                    use.add(token.image);
                }
            }
        }
        else
        {
            for(Token token : data.right)
            {
                if(token.kind == ILOCParserConstants.REGISTER)
                {
                    def.add(token.image);
                }
            }
        }

        check1 = data.instruction.image.equals("i2i");
        check2 = data.instruction.image.equals("c2c");
        check3 = data.instruction.image.equals("c2i");
        check4 = data.instruction.image.equals("i2c");

        if(check1 || check2 || check3 || check4)
        {
            move = new String[2];
            move[0] = data.left[0].image;
            move[1] = data.right[0].image;
        }
        else
        {
            move = null;
        }
    }

    public MyInstruction(int id, NormalOpData data)
    {
        this(id, null, data);
    }

    public MyInstruction(int id, String label, ControlFlowOpData data)
    {
        this.id = id;
        this.label = label;
        this.controlFlow = true;
        this.normalData = null;
        this.flowData = data;
        if(data.instruction.equals("cbr"))
        {
            this.use.add(data.register);
        }
        this.move = null;
    }

    public MyInstruction(int id, ControlFlowOpData data)
    {
        this(id, null, data);
    }

    public void setNavigateTo(int blockID) throws Exception
    {
        for(int i = 0; i < navigateTo.length; i++)
        {
            if(navigateTo[i] == -1)
            {
                navigateTo[i] = blockID;
                return;
            }
        }

        throw new Exception();
    }

    public int[] getNavigateTo()
    {
        return this.navigateTo;
    }
}
