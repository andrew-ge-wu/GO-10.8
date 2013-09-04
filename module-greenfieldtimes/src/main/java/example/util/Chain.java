package example.util;

public interface Chain extends Command
{
    void addCommand(Command cmd);
}
