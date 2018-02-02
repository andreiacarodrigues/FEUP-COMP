import java.util.Map;

public class ILOCtoC
{
    static public String ILOCtoC(MyInstruction instruction)
    {
        if(instruction.controlFlow)
        {
            ControlFlowOpData data = instruction.flowData;
            if(data.instruction.equals("cbr"))
            {
                return "if(" + data.register + "){goto " + data.labels[0] + ";} else {goto " + data.labels[1] + ";}";
            }
            else
            {
                return "goto " + data.labels[0] + ";";
            }
        }
        else
        {
            NormalOpData data = instruction.normalData;
            switch(data.instruction.image)
            {
                case "add":
                case "addI":
                    return data.right[0].image + " = " + data.left[0].image + " + " + data.left[1].image + ";";
                case "subI":
                case "sub":
                    return data.right[0].image + " = " + data.left[0].image + " - " + data.left[1].image + ";";
                case "multI":
                case "mult":
                    return data.right[0].image + " = " + data.left[0].image + " * " + data.left[1].image + ";";
                case "divI":
                case "div":
                    return data.right[0].image + " = " + data.left[0].image + " / " + data.left[1].image + ";";
                case "rsubI":
                    return data.right[0].image + " = " + data.left[1].image + " - " + data.left[0].image + ";";
                case "rdivI":
                    return data.right[0].image + " = " + data.left[1].image + " / " + data.left[0].image + ";";
                case "lshift":
                case "lshiftI":
                    return data.right[0].image + " = " + data.left[0].image + " << " + data.left[1].image + ";";
                case "rshift":
                case "rshiftI":
                    return data.right[0].image + " = " + data.left[0].image + " >> " + data.left[1].image + ";";
                case "and":
                case "andI":
                    return data.right[0].image + " = " + data.left[0].image + " & " + data.left[1].image + ";";
                case "or":
                case "orI":
                    return data.right[0].image + " = " + data.left[0].image + " | " + data.left[1].image + ";";
                case "xor":
                case "xorI":
                    return data.right[0].image + " = " + data.left[0].image + " ^ " + data.left[1].image + ";";
                case "loadI":
                case "i2i":
                    return data.right[0].image + " = " + data.left[0] + ";";
                case "load":
                    return data.right[0].image + " = *(int*)" + data.left[0] + ";";
                case "loadAI":
                case "loadA0":
                    return data.right[0].image + " = *(int*)(" + data.left[0] + " + " + data.left[1] + ");";
                case "cload":
                    return data.right[0].image + " = *(char*)" + data.left[0] + ";";
                case "cloadAI":
                case "cloadA0":
                    return data.right[0].image + " = *(char*)(" + data.left[0] + " + " + data.left[1] + ");";
                case "store":
                return "*(int*)" + data.right[0].image + " = " + data.left[0] + ";";
                case "storeAI":
                case "storeA0":
                    return "*(int*)(" + data.right[0].image + " + " + data.right[1].image + ") = " + data.left[0] + ";";
                case "cstore":
                    return "*(char*)" + data.right[0].image + " = " + data.left[0] + ";";
                case "cstoreAI":
                case "cstoreA0":
                    return "*(char*)(" + data.right[0].image + " + " + data.right[1].image + ") = " + data.left[0] + ";";
                case "c2c":
                case "i2c":
                    return data.right[0].image + " = (char)" + data.left[0] + ";";
                case "c2i":
                    return data.right[0].image + " = (int)" + data.left[0] + ";";
                case "cmp_LI":
                    return data.right[0].image + " = " + data.left[0].image + " < " + data.left[1].image + ";";
                case "cmp_LE":
                    return data.right[0].image + " = " + data.left[0].image + " <= " + data.left[1].image + ";";
                case "cmp_EQ":
                    return data.right[0].image + " = " + data.left[0].image + " == " + data.left[1].image + ";";
                case "cmp_GE":
                    return data.right[0].image + " = " + data.left[0].image + " >= " + data.left[1].image + ";";
                case "cmp_GT":
                    return data.right[0].image + " = " + data.left[0].image + " > " + data.left[1].image + ";";
                case "cmp_NE":
                    return data.right[0].image + " = " + data.left[0].image + " != " + data.left[1].image + ";";
                default:
                    return null;
            }
        }
    }
}
