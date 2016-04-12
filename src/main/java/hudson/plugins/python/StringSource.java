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

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.util.FormValidation;

public class StringSource extends ScriptSource
{

    private String scriptContent;


    @DataBoundConstructor
    public StringSource(String scriptContent)
    {
        this.scriptContent = scriptContent;
    }


    public String getScriptContent()
    {
        return scriptContent;
    }


    @Override
    public FilePath getScriptFile(FilePath workspace, AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException
    {
        return workspace.createTextTempFile("hudson", ".py", scriptContent, true);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ScriptSource>
    {

        @Override
        public String getDisplayName()
        {
            return "Python script";
        }


        public FormValidation doCheckScript(@QueryParameter String scriptContent)
        {
            if (StringUtils.isBlank(scriptContent))
            {
                return FormValidation.error("Script seems to be empty string!");
            }
            return FormValidation.ok();
        }
    }
}
