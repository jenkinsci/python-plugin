package hudson.plugins.python;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Entry point for Python plugin
 *
 */
public class PluginImpl extends Plugin {
    public void start() throws Exception {
        BuildStep.BUILDERS.add(Python.DESCRIPTOR);
    }
}
