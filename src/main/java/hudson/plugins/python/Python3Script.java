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

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.plugins.python.installations.Python3Installation;
import hudson.plugins.python.installations.Python3Installation.Python3InstallationDescriptor;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

public class Python3Script extends AbstractPythonScript<Python3Installation>
{

    private static final long serialVersionUID = -8017952220078123555L;

    @DataBoundConstructor
    public Python3Script(String pythonName, ScriptSource scriptSource, JSONObject python3Options, String scriptArgs,
            String options, boolean quiet)
    {
        super(pythonName, scriptSource, scriptArgs, options, quiet);
    }


    @Override
    protected Python3Installation getPython()
    {
        for (Python3Installation i : Jenkins.getInstance().getDescriptorByType(Python3InstallationDescriptor.class)
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
        return Python3Installation.PYTHON_VERSION;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractPythonScriptDescriptor<Python3Installation>
    {

        public String getPythonVersion()
        {
            return "3";
        }


        public DescriptorImpl()
        {
            super(Python3Script.class);
            load();
        }


        @Override
        public String getDisplayName()
        {
            return "Execute Python 3 script";
        }


        @Override
        public Python3Installation[] getInstallations()
        {
            return Jenkins.getInstance().getDescriptorByType(Python3Installation.Python3InstallationDescriptor.class)
                    .getInstallations();
        }
    }

}
