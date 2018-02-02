import java.util.HashMap;
import java.util.Map;

public class Validator
{
    public enum Format
    {
        RR_R,
        RC_R,
        C_R,
        R_R,
        R_RC,
        R_RR;
    }

    private static Map<String, Format> opToFormat = new HashMap<>();
    static
    {
        opToFormat.put("add", Format.RR_R);
        opToFormat.put("sub", Format.RR_R);
        opToFormat.put("mult", Format.RR_R);
        opToFormat.put("div", Format.RR_R);
        opToFormat.put("lshift", Format.RR_R);
        opToFormat.put("rshift", Format.RR_R);
        opToFormat.put("and", Format.RR_R);
        opToFormat.put("or", Format.RR_R);
        opToFormat.put("xor", Format.RR_R);
        opToFormat.put("loadA0", Format.RR_R);
        opToFormat.put("cstoreA0", Format.RR_R);
        opToFormat.put("cmp_LI", Format.RR_R);
        opToFormat.put("cmp_LE", Format.RR_R);
        opToFormat.put("cmp_EQ", Format.RR_R);
        opToFormat.put("cmp_GE", Format.RR_R);
        opToFormat.put("cmp_GT", Format.RR_R);
        opToFormat.put("cmp_NE", Format.RR_R);

        opToFormat.put("addI", Format.RC_R);
        opToFormat.put("subI", Format.RC_R);
        opToFormat.put("rsubI", Format.RC_R);
        opToFormat.put("multI", Format.RC_R);
        opToFormat.put("divI", Format.RC_R);
        opToFormat.put("rdivI", Format.RC_R);
        opToFormat.put("lshiftI", Format.RC_R);
        opToFormat.put("rshiftI", Format.RC_R);
        opToFormat.put("andI", Format.RC_R);
        opToFormat.put("orI", Format.RC_R);
        opToFormat.put("xorI", Format.RC_R);
        opToFormat.put("loadAI", Format.RC_R);
        opToFormat.put("cloadAI", Format.RC_R);

        opToFormat.put("loadI", Format.C_R);

        opToFormat.put("load", Format.R_R);
        opToFormat.put("cload", Format.R_R);
        opToFormat.put("store", Format.R_R);
        opToFormat.put("cstore", Format.R_R);
        opToFormat.put("i2i", Format.R_R);
        opToFormat.put("c2c", Format.R_R);
        opToFormat.put("c2i", Format.R_R);
        opToFormat.put("i2c", Format.R_R);

        opToFormat.put("storeAI", Format.R_RC);
        opToFormat.put("cstoreAI", Format.R_RC);

        opToFormat.put("storeA0", Format.R_RR);
        opToFormat.put("cstoreA0", Format.R_RR);
    }

    public static boolean validate(NormalOpData data)
    {
        Format format = opToFormat.get(data.instruction.image);

        Token[] left = data.left;
        Token[] right = data.right;

        if(left.length == 0 || left.length > 2 || right.length == 0 || right.length > 2)
        {
            return false;
        }

        if(format == Format.RR_R)
        {
            if(left.length != 2 || right.length != 1)
            {
                return false;
            }

            Token firstLeft = left[0];
            Token secondLeft = left[1];

            Token rightToken = right[0];

            boolean check1 = firstLeft.kind == ILOCParserConstants.REGISTER;
            boolean check2 = secondLeft.kind == ILOCParserConstants.REGISTER;

            boolean check3 = rightToken.kind == ILOCParserConstants.REGISTER;

            return check1 && check2 && check3;
        }
        else if(format == Format.RC_R)
        {
            if(left.length != 2 || right.length != 1)
            {
                return false;
            }

            Token firstLeft = left[0];
            Token secondLeft = left[1];

            Token rightToken = right[0];

            boolean check1 = firstLeft.kind == ILOCParserConstants.REGISTER;
            boolean check2 = secondLeft.kind == ILOCParserConstants.INTEGER;

            boolean check3 = rightToken.kind == ILOCParserConstants.REGISTER;

            return check1 && check2 && check3;
        }
        else if(format == Format.C_R)
        {
            if(left.length != 1 || right.length != 1)
            {
                return false;
            }

            Token leftToken = left[0];

            Token rightToken = right[0];

            boolean check1 = leftToken.kind == ILOCParserConstants.INTEGER;

            boolean check2 = rightToken.kind == ILOCParserConstants.REGISTER;

            return check1 && check2;
        }
        else if(format == Format.R_R)
        {
            if(left.length != 1 || right.length != 1)
            {
                return false;
            }

            Token leftToken = left[0];

            Token rightToken = right[0];

            boolean check1 = leftToken.kind == ILOCParserConstants.REGISTER;

            boolean check2 = rightToken.kind == ILOCParserConstants.REGISTER;

            return check1 && check2;
        }
        else if(format == Format.R_RC)
        {
            if(left.length != 1 || right.length != 2)
            {
                return false;
            }

            Token leftToken = left[0];

            Token firstRight = right[0];
            Token secondRight = right[1];

            boolean check1 = leftToken.kind == ILOCParserConstants.REGISTER;

            boolean check2 = firstRight.kind == ILOCParserConstants.REGISTER;
            boolean check3 = secondRight.kind == ILOCParserConstants.INTEGER;

            return check1 && check2 && check3;
        }
        else if(format == Format.R_RR)
        {
            if(left.length != 1 || right.length != 2)
            {
                return false;
            }

            Token leftToken = left[0];

            Token firstRight = right[0];
            Token secondRight = right[1];

            boolean check1 = leftToken.kind == ILOCParserConstants.REGISTER;

            boolean check2 = firstRight.kind == ILOCParserConstants.REGISTER;
            boolean check3 = secondRight.kind == ILOCParserConstants.REGISTER;

            return check1 && check2 && check3;
        }
        else
        {
            return false;
        }
    }
}
