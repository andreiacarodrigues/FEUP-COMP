import java.util.*;

public class Main
{
    static public void main(String[] args)
    {
        int registersN = Integer.parseInt(args[0]);
        //region Parser
        java.io.FileInputStream fis = null;
        SimpleNode node = null;
        try
        {
            fis = new java.io.FileInputStream(args[1]);
            ILOCParser myParser = new ILOCParser(fis);
            node = myParser.ILOCProgram();
            fis.close();
        }
        catch (java.io.IOException e)
        {
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if(node == null)
        {
            throw new NullPointerException();
        }
        //endregion

        //region CFG
        Map<Integer, MyInstruction> blocks = new HashMap<>();
        Map<String, Integer> labelToBlock = new HashMap<>();
        Map<MyInstruction, ArrayList<String>> test = new HashMap<>();
        int blockCounter = 0;
        String labelBuffer = null;
        for(Node child : node.children)
        {
            if(child.getId() == ILOCParserTreeConstants.JJTLABEL && child instanceof SimpleNode)
            {
                String labelText = ((LabelData)(((SimpleNode) child).jjtGetValue())).text;
                labelToBlock.put(labelText, blockCounter);
                labelBuffer = labelText;
            }
            else
            {
                MyInstruction block = null;
                SimpleNode simpleChild = (SimpleNode)child;
                if (child.toString().equals("ControlFlowOp"))
                {
                    ControlFlowOpData data = (ControlFlowOpData)simpleChild.jjtGetValue();
                    block = new MyInstruction(blockCounter, labelBuffer, data);
                    String[] labels = data.labels;
                    for (String label : labels)
                    {
                        test.computeIfAbsent(block, f -> new ArrayList<>()).add(label);
                    }
                }
                else if (child.toString().equals("NormalOp"))
                {
                    NormalOpData data = (NormalOpData)simpleChild.jjtGetValue();
                    if(!Validator.validate(data))
                    {
                        System.err.println("Semantic error in code.");
                        return;
                    }
                    block = new MyInstruction(blockCounter, labelBuffer, data);
                    try
                    {
                        block.setNavigateTo(blockCounter+1);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                labelBuffer = null;
                blocks.put(blockCounter, block);
                blockCounter++;
            }
        }

        for(Map.Entry<MyInstruction, ArrayList<String>> entry : test.entrySet())
        {
            for(String label : entry.getValue())
            {
                int id = labelToBlock.get(label);
                try
                {
                    entry.getKey().setNavigateTo(id);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        try
        {
            java.io.FileWriter dot = new java.io.FileWriter("cfg.gv");
            dot.write("digraph program { ");
            dot.write(" 0 [shape=Mdiamond]; ");
            int val = 0;
            for(MyInstruction codeBlock : blocks.values())
            {
                for(int target : codeBlock.getNavigateTo())
                {
                    if(target != -1)
                    {
                        dot.write(codeBlock.id + " -> " + target + ';');
                    }
                }
            }
            dot.write(val + " [shape=Msquare]; ");
            dot.write('}');
            dot.close();
        }
        catch (java.io.IOException e)
        {
            e.printStackTrace();
        }
        //endregion

        //region Dataflow analysis
        int lastInstruction = blockCounter - 1;
        boolean done;
        do
        {
            done = true;
            for(blockCounter = lastInstruction; blockCounter >= 0; blockCounter--)
            {
                MyInstruction instruction = blocks.get(blockCounter);
                int[] succ = instruction.getNavigateTo();
                Set<String> outPrime = new HashSet<>();
                Set<String> inPrime = new HashSet<>();
                for(Integer successor : succ)
                {
                    if(successor != -1 && successor <= lastInstruction)
                    {
                        MyInstruction successorInstruction = blocks.get(successor);
                        outPrime.addAll(successorInstruction.in);
                    }
                }
                inPrime.addAll(instruction.use);
                Set<String> temp = new HashSet<>(outPrime);
                temp.removeAll(instruction.def);
                inPrime.addAll(temp);
                if(!(instruction.in.equals(inPrime)) || !(instruction.out.equals(outPrime)))
                {
                    done = false;
                }
                instruction.in = inPrime;
                instruction.out = outPrime;
            }
        } while(!done);
        Set<String> registers = new HashSet<>();
        for(MyInstruction instruction : blocks.values())
        {
            registers.addAll(instruction.in);
            registers.addAll(instruction.out);
        }
        //endregion

        //region Building interference graph
        Map<String, Register> trueRegisters = new HashMap<>();
        for(String register : registers)
        {
            trueRegisters.put(register, new Register(register));
        }
        for(MyInstruction instruction : blocks.values())
        {
            if(instruction.move != null)
            {
                Register register1 = trueRegisters.get(instruction.move[0]);
                Register register2 = trueRegisters.get(instruction.move[1]);

                register1.move.add(register2);
                register2.move.add(register1);
            }
        }
        for(Register register : trueRegisters.values())
        {
            for(MyInstruction instruction : blocks.values())
            {
                if(instruction.in.contains(register.name))
                {
                    for(String registerName : instruction.in)
                    {
                        if(!registerName.equals(register.name))
                        {
                            register.interferences.add(trueRegisters.get(registerName));
                        }
                    }
                }
                if(instruction.out.contains(register.name))
                {
                    for(String registerName : instruction.out)
                    {
                        if(!registerName.equals(register.name))
                        {
                            register.interferences.add(trueRegisters.get(registerName));
                        }
                    }
                }
            }
        }
        //endregion

        //region Graph simplification and coalescing
        ArrayList<Register> registersQueue = new ArrayList<>(trueRegisters.values());
        Stack<Register> stack = new Stack<>();

        outerQueue:
        while(!registersQueue.isEmpty())
        {
            registersQueue.sort(Register.regComparator);
            Register register = registersQueue.get(0);
            registersQueue.remove(0);
            if(register.interferences.size() + register.interferenceFromCoalesce.size() - register.removed >= registersN)
            {
                System.err.println("Not enough registers to allocate the code's variables.");
                return;
            }
            for(Register otherRegister : register.interferences)
            {
                otherRegister.removed++;
            }
            for(Register moveRegister : register.move)
            {
                if(moveRegister.coalesced.contains(register))
                {
                    continue;
                }
                if(register.interferences.contains(moveRegister))
                {
                    continue;
                }
                for(Register moveRegisterInterference : moveRegister.interferences)
                {
                    if(moveRegisterInterference.interferences.size() < registersN || register.interferences.contains(moveRegisterInterference))
                    {
                        register.coalesced.add(moveRegister);
                        moveRegister.coalesced.add(register);

                        Set<Register> temp = new HashSet<>(register.interferences);
                        temp.removeAll(moveRegister.interferences);
                        moveRegister.interferenceFromCoalesce.addAll(temp);

                        for(Register otherRegister : moveRegister.interferenceFromCoalesce)
                        {
                            otherRegister.removed--;
                        }

                        continue outerQueue;
                    }
                }
            }
            stack.add(register);
        }
        //endregion

        //region Graph coloring
        Map<Integer, Set<Register>> registerForColor = new HashMap<>();
        stackLoop:
        while(!stack.isEmpty())
        {
            Register register = stack.pop();
            Set<Register> temp = new HashSet<>();
            temp.addAll(register.interferences);
            temp.addAll(register.interferenceFromCoalesce);
            int i;
            outer:
            for(i = 0; i < registersN; i++)
            {
                for(Register neighbour : temp)
                {
                    if(neighbour.color == i)
                    {
                        continue outer;
                    }
                }
                register.color = i;
                for(Register coalesced : register.coalesced)
                {
                    coalesced.color = i;
                }
                Set<Register> colorSet = registerForColor.computeIfAbsent(i, f -> new HashSet<>());
                colorSet.add(register);
                continue stackLoop;
            }
        }
        //endregion

        //region Redefining instructions with new register allocation
        Map<Integer, MyInstruction> finalInstruction = new HashMap<>();
        int instructionCounter = 0;
        for(int i = 0; i <= lastInstruction; i++)
        {
            MyInstruction current = blocks.get(i);
            if(current.move != null)
            {
                Register register1 = trueRegisters.get(current.move[0]);
                Register register2 = trueRegisters.get(current.move[1]);

                if(register1.coalesced.contains(register2))
                {
                    continue;
                }
            }
            if(current.controlFlow)
            {
                ControlFlowOpData data = current.flowData;
                ControlFlowOpData newData = new ControlFlowOpData(data);
                if(newData.register != null)
                {
                    newData.register = "r" + trueRegisters.get(newData.register).color;
                }
                finalInstruction.put(instructionCounter, new MyInstruction(instructionCounter++, current.label, newData));
            }
            else
            {
                NormalOpData data = current.normalData;
                NormalOpData newData = new NormalOpData(data);
                for(int j = 0; j < newData.left.length; j++)
                {
                    Token token = newData.left[j];
                    if(token.kind == ILOCParserConstants.REGISTER)
                    {
                        newData.left[j] = new Token(token.kind, "r" + trueRegisters.get(token.image).color);
                    }
                }
                for(int j = 0; j < newData.right.length; j++)
                {
                    Token token = newData.right[j];
                    if(token.kind == ILOCParserConstants.REGISTER)
                    {
                        newData.right[j] = new Token(token.kind, "r" + trueRegisters.get(token.image).color);
                    }
                }
                finalInstruction.put(instructionCounter, new MyInstruction(instructionCounter++, current.label, newData));
            }
        }
        //endregion

        //region Print output ILOC code
        try
        {
            java.io.FileWriter outCode = new java.io.FileWriter("outCode");
            for(int i = 0; i < finalInstruction.size(); i++)
            {
                MyInstruction instruction = finalInstruction.get(i);
                if(instruction.label != null)
                {
                    outCode.write(instruction.label + ":\n");
                }
                if(instruction.controlFlow)
                {
                    ControlFlowOpData data = instruction.flowData;
                    outCode.write(data.instruction + ' ');
                    if(data.instruction.equals("cbr"))
                    {
                        outCode.write(data.register + " -> " + data.labels[0] + ", " + data.labels[1]);
                    }
                    else
                    {
                        outCode.write(" -> " + data.labels[0]);
                    }
                }
                else
                {
                    NormalOpData data = instruction.normalData;
                    outCode.write(data.instruction.image + ' ');
                    for(int j = 0; j < data.left.length; j++)
                    {
                        if(j != 0)
                        {
                            outCode.write(", ");
                        }
                        Token token = data.left[j];
                        outCode.write(token.image);
                    }
                    outCode.write(" => ");
                    for(int j = 0; j < data.right.length; j++)
                    {
                        if(j != 0)
                        {
                            outCode.write(", ");
                        }
                        Token token = data.right[j];
                        outCode.write(token.image);
                    }
                }
                outCode.write('\n');
            }
            outCode.close();
        }
        catch (java.io.IOException e)
        {
            e.printStackTrace();
        }
        //endregion

        //region Output original and new ILOC code to C
        try
        {
            java.io.FileWriter outCode = new java.io.FileWriter("original.c");

            for(String register : registers)
            {
                outCode.write("int " + register + ";\n");
            }
            for(int i = 0; i <= lastInstruction; i++)
            {
                MyInstruction instruction = blocks.get(i);
                if(instruction.label != null)
                {
                    outCode.write(instruction.label + ":\n");
                }
                outCode.write(ILOCtoC.ILOCtoC(instruction));
                outCode.write('\n');
            }
            outCode.close();
            java.io.FileWriter outCodeAfter = new java.io.FileWriter("outCode.c");
            for(int i = 0; i < registerForColor.size(); i++)
            {
                outCodeAfter.write("int r" + i + ";\n");
            }
            for(int i = 0; i < finalInstruction.size(); i++)
            {
                MyInstruction instruction = finalInstruction.get(i);
                if(instruction.label != null)
                {
                    outCodeAfter.write(instruction.label + ":\n");
                }
                outCodeAfter.write(ILOCtoC.ILOCtoC(instruction));
                outCodeAfter.write('\n');
            }
            outCodeAfter.close();
        }
        catch (java.io.IOException e)
        {
            e.printStackTrace();
        }
        //endregion

        //region Variables statistics
        System.out.println("Registers interferences:");
        for(Register register : trueRegisters.values())
        {
            System.out.print("Register " + register.name + " interferes with: ");
            boolean first = true;
            for(Register other : register.interferences)
            {
                if(first)
                {
                    System.out.print(other.name);
                    first = false;
                }
                else
                {
                    System.out.print(", " + other.name);
                }

            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Register allocation:");
        for(int i = 0; i < registerForColor.size(); i++)
        {
            Set<Register> colorRegisters = registerForColor.get(i);
            System.out.print("r" + i + " <- {");
            boolean first = true;
            for(Register register : colorRegisters)
            {
                if(first)
                {
                    System.out.print(register.name);
                    first = false;
                }
                else
                {
                    System.out.print(", " + register.name);
                }
            }
            System.out.print("}\n");
        }
        //endregion
    }
}
