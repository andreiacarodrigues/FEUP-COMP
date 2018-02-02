import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Register
{
    public final static Comparator<Register> regComparator = (o1, o2) ->
    {
        int o1Val = o1.interferences.size() + o1.interferenceFromCoalesce.size() - o1.removed;
        int o2Val = o2.interferences.size() + o2.interferenceFromCoalesce.size() - o2.removed;

        if(o1Val < o2Val)
        {
            return -1;
        }
        else if(o1Val > o2Val)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    };

    public final String name;
    public int removed = 0;
    public int color = -1;
    public final Set<Register> interferences = new HashSet<>();
    public final Set<Register> interferenceFromCoalesce = new HashSet<>();
    public final Set<Register> move = new HashSet<>();
    public final Set<Register> coalesced = new HashSet();

    public Register(String name)
    {
        this.name = name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Register register = (Register) o;

        return name.equals(register.name);
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
