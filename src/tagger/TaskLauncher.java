package tagger;

public class TaskLauncher
{
    private String processId;
    private TaskExecuter task=null;
    
    TaskLauncher (TaskExecuter obj,String process)
    {
	task=obj;
	processId=process;
    }
    
    /*
     * Called from ProgressBarDemo to start the task.
     */
    void go()
    {
	final SwingWorker tagTask = new SwingWorker()
	    {
		public Object construct()
		{
		    try
			{
			    if (task.canExecute(processId))
				task.taskExecute(processId);
			    else
				task.taskStop();
			}
		    catch (Exception e)
			{
			    task.taskStop();
			    e.printStackTrace();
			    return new Integer(1);
			}
		    return new Integer(1);
		}
	    };
	tagTask.start();
    }
}

