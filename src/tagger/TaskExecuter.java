package tagger;

public interface TaskExecuter
{
    // called before taskExecute to check if everything is ok before executing the task!
    public boolean canExecute (String processId);

    // called when the task is launched
    public boolean taskExecute (String processId);

    // called to know if the task has finished!
    public boolean taskDone ();

    // called to stop task execution!
    public void taskStop ();

    int getTaskLength ();
    int getCurrent ();

    // this could be a JComponent to be put in the progressMonitor object!
    Object getMessage ();
}
