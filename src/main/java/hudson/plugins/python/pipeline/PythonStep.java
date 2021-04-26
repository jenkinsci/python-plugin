package hudson.plugins.python.pipeline;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;

/**
 * Created by azikha01 on 14/10/2016.
 */
public class PythonStep extends AbstractStepImpl implements Serializable {
    private static final long serialVersionUID = 1L;

    /* --- Step Properties --- */
    /**
     * Script in form of string.
     */
    @DataBoundSetter
    private String command = null;

    @DataBoundConstructor
    public PythonStep(){}

    public String getCommand(){ return command; }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {
        public static final String STEP_NAME = "py";

        public DescriptorImpl(){
            super(PythonExecution.class);
        }

        @Override
        public String getFunctionName(){ return STEP_NAME; }

        @Override
        public String getDisplayName() { return STEP_NAME; }
    }
}
