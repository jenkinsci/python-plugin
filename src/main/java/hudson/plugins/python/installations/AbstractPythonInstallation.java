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

import java.io.IOException;
import java.util.List;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.EnvironmentSpecific;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;

public abstract class AbstractPythonInstallation extends ToolInstallation
implements EnvironmentSpecific<AbstractPythonInstallation>, NodeSpecific<AbstractPythonInstallation>
{

    private static final long serialVersionUID = 1L;


    public AbstractPythonInstallation(String name, String home, List<? extends ToolProperty<?>> properties)
    {
        super(name, home, properties);
    }


    public PythonExecutable getExecutable(Launcher launcher) throws IOException, InterruptedException
    {
        PythonLocator locator = new PythonLocator(launcher);

        FilePath pythonExe = new FilePath(launcher.getChannel(), getHome());
        if (locator.getPythonMajorVersion(pythonExe, 0) == getRequiredPythonVersion())
        {
            if (pythonExe.getName().equals("py.exe"))
            {
                return new PythonExecutable(pythonExe, "-" + getRequiredPythonVersion());
            }
            return new PythonExecutable(pythonExe);
        }
        return null;

    }


    protected abstract int getRequiredPythonVersion();

}
