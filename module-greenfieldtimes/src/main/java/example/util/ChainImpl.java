package example.util;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChainImpl implements Chain
{
    private final ConcurrentLinkedQueue<Command> cmds = new ConcurrentLinkedQueue<Command>();

    public void addCommand(Command cmd)
    {
        cmds.add(cmd);
    }

    public boolean execute(Context ctx)
    {
        Iterator<Command> it = cmds.iterator();

        while (it.hasNext())
        {
            if (!it.next().execute(ctx))
            {
                return false;
            }
        }

        return true;
    }
}
