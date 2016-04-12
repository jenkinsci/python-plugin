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

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.FormValidation.FileValidator;
import jenkins.model.Jenkins;

public class PythonValidator extends FileValidator
{
    private final int version;


    public PythonValidator(int version)
    {
        this.version = version;
    }


    @Override
    public FormValidation validate(File f)
    {
        try
        {
            Launcher launcher = Jenkins.getInstance().createLauncher(TaskListener.NULL);
            PythonLocator locator = new PythonLocator(launcher);
            int effectiveVersion = locator.getPythonMajorVersion(new FilePath(f), 0);
            if (effectiveVersion != version)
            {
                return FormValidation.error(
                        "The python found at " + f.getPath() + " has major version " + effectiveVersion
                                + " but expected major version is " + version + ".");
            }
            return FormValidation.ok();
        }
        catch (IOException e)
        {
            return FormValidation.error("Unable to check python version.");
        }
        catch (InterruptedException e)
        {
            return FormValidation.error("Unable to check python version.");
        }
    }
}
