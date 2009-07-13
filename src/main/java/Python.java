package hudson.plugins.python;

import hudson.FilePath;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Invokes the Python interpreter and invokes the Python script entered on the
 * hudson build configuration.
 * 
 * It is expected that the Python interpreter is available on the system PATH.
 *
 */
public class Python extends CommandInterpreter {

    private Python(String command) {
        super(command);
    }

    protected String[] buildCommandLine(FilePath script) {
        return new String[]{"python", script.getRemote()};
    }

    protected String getContents() {
        return command;
    }

    protected String getFileExtension() {
        return ".py";
    }

    public Descriptor<Builder> getDescriptor() {
        return DESCRIPTOR;
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<Builder> {
        private DescriptorImpl() {
            super(Python.class);
        }

        public Builder newInstance(StaplerRequest req, JSONObject formData) {
            return new Python(formData.getString("python"));
        }

        public String getDisplayName() {
            return "Execute Python script";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/python/help.html";
        }
    }
}
