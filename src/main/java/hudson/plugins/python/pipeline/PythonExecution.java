package hudson.plugins.python.pipeline;

import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Inject;
import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.ListView;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.tasks.CommandInterpreter;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import java.util.ArrayList;

/**
 * Created by azikha01 on 14/10/2016.
 */
public class PythonExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

    @Inject
    private transient PythonStep config;

    @StepContextParameter
    private transient Launcher launcher;

    @StepContextParameter
    private transient TaskListener listener;

    @StepContextParameter
    private transient FilePath ws;

    @StepContextParameter
    private transient Run<?,?> build;

    @Override
    protected Void run() throws Exception {
        FilePath script = ws.createTextTempFile("hudson", ".py", config.getCommand(), false);
        try {
            ArrayList<String> cmd = new ArrayList<String>();
            cmd.add("python");
            cmd.add(script.getRemote());
            int result = launcher.launch().cmds(cmd).envs(build.getEnvironment(listener)).stdout(listener).pwd(ws).join();
            if (result != 0){
                throw new AbortException("Python script exited with error code: " + result);
            }
        } finally {
            script.delete();
        }
        return null;
    }
}
