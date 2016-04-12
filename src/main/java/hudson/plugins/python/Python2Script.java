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
package hudson.plugins.python;

import static hudson.init.InitMilestone.PLUGINS_STARTED;

import java.io.File;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.init.Initializer;
import hudson.model.Items;
import hudson.plugins.python.installations.Python2Installation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

public class Python2Script extends AbstractPythonScript<Python2Installation>
{

    private static final long serialVersionUID = 7102426938537145769L;

    protected transient String command;


    @DataBoundConstructor
    public Python2Script(String pythonName, ScriptSource scriptSource, JSONObject python2Options, String scriptArgs,
            String options, boolean quiet)
    {
        super(pythonName, scriptSource, scriptArgs, options, quiet);
    }


    @Override
    protected Python2Installation getPython()
    {
        for (Python2Installation i : Jenkins.getInstance()
                .getDescriptorByType(Python2Installation.Python2InstallationDescriptor.class)
                .getInstallations())
        {
            if (pythonName != null && pythonName.equals(i.getName()))
            {
                return i;
            }
        }

        return null;
    }


    @Override
    protected int getRequiredPythonVersion()
    {
        return Python2Installation.PYTHON_VERSION;
    }


    public Object readResolve()
    {
        if (command != null)
        {
            scriptSource = new StringSource(command);
            command = null;
        }
        return this;
    }


    @Initializer(before = PLUGINS_STARTED)
    public static void addAliases()
    {
        File python3CompytibilityMarker = new File(Jenkins.getInstance().getRootDir(), "python3CompatibilityMarker");
        if (!python3CompytibilityMarker.exists())
        {
            Items.XSTREAM2.addCompatibilityAlias("hudson.plugins.python.Python", Python2Script.class);
        }

    }

    @Extension
    public static final class DescriptorImpl extends AbstractPythonScriptDescriptor<Python2Installation>
    {

        public String getPythonVersion()
        {
            return "2";
        }


        public DescriptorImpl()
        {
            super(Python2Script.class);
            load();
        }


        @Override
        public String getDisplayName()
        {
            return "Execute Python 2 script";
        }


        @Override
        public Python2Installation[] getInstallations()
        {
            return Jenkins.getInstance().getDescriptorByType(Python2Installation.Python2InstallationDescriptor.class)
                    .getInstallations();
        }
    }
}
