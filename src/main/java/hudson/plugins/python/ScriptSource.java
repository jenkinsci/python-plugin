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

import hudson.DescriptorExtensionList;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public abstract class ScriptSource implements Describable<ScriptSource>
{

    /**
     * Able to load script when script path contains parameters
     *
     * @param projectWorkspace
     *            Project workspace to create tmp file
     * @param build
     *            - needed to obtain environment variables
     * @param listener
     *            - build listener needed by Environment
     * @return Path to the executed script file
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract FilePath getScriptFile(FilePath workspace, AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException;


    @Override
    @SuppressWarnings("unchecked")
    public Descriptor<ScriptSource> getDescriptor()
    {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }


    public static final DescriptorExtensionList<ScriptSource, Descriptor<ScriptSource>> all()
    {
        return Jenkins.getInstance().getDescriptorList(ScriptSource.class);
    }

}
