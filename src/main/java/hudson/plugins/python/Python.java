package hudson.plugins.python;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

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
    public static final class DescriptorImpl extends Descriptor<Builder> {
        public String getDisplayName() {
            return Messages.Python_DisplayName();
        }
    }
}
