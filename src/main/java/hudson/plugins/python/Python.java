package hudson.plugins.python;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Invokes the Python interpreter and invokes the Python script entered on the
 * hudson build configuration.
 * 
 * It is expected that the Python interpreter is available on the system PATH.
 *
 */
public class Python extends CommandInterpreter {
    @DataBoundConstructor
    public Python(String command) {
        super(command);
    }

    @Override
    public String[] buildCommandLine(FilePath script) {
        return new String[]{"python", script.getRemote()};
    }

    @Override
    protected String getContents() {
        return command;
    }

    @Override
    protected String getFileExtension() {
        return ".py";
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public DescriptorImpl() {
            load();
        }

        protected DescriptorImpl(Class<? extends Python> clazz) {
            super(clazz);
        }

        @Override
        public String getDisplayName() {
            return Messages.Python_DisplayName();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return true;
        }
    }
}
