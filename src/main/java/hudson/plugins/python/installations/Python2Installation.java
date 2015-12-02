/*
 *  The MIT License
 *
 *  Copyright (c) 2016 Markus Winter. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package hudson.plugins.python.installations;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

public class Python2Installation extends AbstractPythonInstallation
{

    private static final long serialVersionUID = 1386145821598783034L;

    public static final transient String DEFAULT_PYTHON2 = "Default python 2";

    public static final transient int PYTHON_VERSION = 2;


    @DataBoundConstructor
    public Python2Installation(String name, String home, List<? extends ToolProperty<?>> properties)
    {
        super(name, home, properties);
    }


    @Override
    public Python2Installation forEnvironment(EnvVars environment)
    {
        return new Python2Installation(getName(), environment.expand(getHome()), getProperties().toList());
    }


    @Override
    public Python2Installation forNode(Node node, TaskListener log) throws IOException, InterruptedException
    {
        return new Python2Installation(getName(), translateFor(node, log), getProperties().toList());
    }


    @Override
    protected int getRequiredPythonVersion()
    {
        return 2;
    }

    @Extension
    public static class Python2InstallationDescriptor extends ToolDescriptor<Python2Installation>
    {

        public Python2InstallationDescriptor()
        {
            super();
            load();
        }


        @Override
        public String getDisplayName()
        {
            return "Python 2";
        }


        @Override
        public FormValidation doCheckHome(@QueryParameter File value)
        {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            String path = value.getPath();

            return FormValidation.validateExecutable(path, new PythonValidator(PYTHON_VERSION));
        }


        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws hudson.model.Descriptor.FormException
        {
            setInstallations(req.bindJSONToList(clazz, json.get("tool")).toArray(new Python2Installation[0]));
            save();
            return true;
        }

    }

}
