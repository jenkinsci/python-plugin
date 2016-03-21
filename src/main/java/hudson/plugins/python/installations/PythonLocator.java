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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.remoting.VirtualChannel;
import hudson.util.ArgumentListBuilder;

public class PythonLocator
{

    private Launcher launcher;
    private VirtualChannel channel;


    public PythonLocator(Launcher launcher)
    {
        this.launcher = launcher;
        channel = launcher.getChannel();
    }

    private static Logger logger = Logger.getLogger(PythonLocator.class.getName());


    @CheckForNull
    public PythonExecutable findPythonForVersion(int version)
            throws IOException, InterruptedException
    {
        if (launcher.isUnix())
        {
            return findPythonVersionForUnix(version);
        }
        else
        {
            return findPythonVersionForWindows(version);
        }
    }


    /*
     * Looks in the PATH for a given file
     */
    @CheckForNull
    public FilePath findFileInPath(String exe) throws IOException, InterruptedException
    {
        EnvVars envvars = EnvVars.getRemote(channel);
        String path = envvars.get("PATH");
        logger.fine("PATH is : " + path);
        if (path != null)
        {
            for (String dir : Util.tokenize(path.replace("\\", "\\\\"), launcher.isUnix() ? ":" : ";"))
            {
                logger.fine("Scanning directory " + dir);
                FilePath dirPath = new FilePath(channel, dir);

                FilePath f = dirPath.child(exe);
                logger.fine("Looking for file " + f.getRemote());
                if (f.exists())
                {
                    return f;
                }
            }
        }

        return null;
    }


    @CheckForNull
    public PythonExecutable findPythonVersionForWindows(int wantedVersion) throws IOException, InterruptedException
    {
        FilePath pythonExe = findFileInPath("py.exe");
        logger.fine("PythonExe is " + pythonExe);
        int version;
        if (pythonExe != null)
        {
            version = getPythonMajorVersion(pythonExe, wantedVersion);
            if (version == wantedVersion)
            {
                return new PythonExecutable(pythonExe, "-" + wantedVersion);
            }
        }

        pythonExe = findFileInPath("python.exe");
        logger.fine("PythonExe is " + pythonExe);
        version = 0;
        if (pythonExe != null)
        {
            version = getPythonMajorVersion(pythonExe, 0);
            if (version == wantedVersion)
            {
                return new PythonExecutable(pythonExe);
            }
        }

        FilePath rootC = new FilePath(channel, "c:\\");
        for (FilePath dir : rootC.listDirectories())
        {
            logger.fine("searching for python in " + dir.getName());
            if (dir.getName().toLowerCase().startsWith("python" + wantedVersion))
            {
                logger.fine("Directory starts with python " + dir);
                pythonExe = dir.child("python.exe");
                if (!pythonExe.isDirectory())
                {
                    logger.fine("PythonExe exists " + pythonExe);
                    version = getPythonMajorVersion(pythonExe, 0);
                    if (version == wantedVersion)
                    {
                        return new PythonExecutable(pythonExe);
                    }
                }
            }
        }
        return null;
    }


    @CheckForNull
    public PythonExecutable findPythonVersionForUnix(int wantedVersion) throws IOException, InterruptedException
    {
        logger.fine("Searching for python" + wantedVersion);

        FilePath pythonExe = findFileInPath("python" + wantedVersion);
        int version;
        if (pythonExe != null)
        {
            logger.fine("Found: " + pythonExe);
            version = getPythonMajorVersion(pythonExe, 0);
            if (version == wantedVersion)
            {
                logger.fine("Found python for Unix: " + pythonExe.getRemote());
                return new PythonExecutable(pythonExe);
            }
        }

        logger.fine("Searching for python");
        pythonExe = findFileInPath("python");
        if (pythonExe != null)
        {
            version = getPythonMajorVersion(pythonExe, 0);
            if (version == wantedVersion)
            {
                logger.fine("Found python: " + pythonExe.getRemote());
                return new PythonExecutable(pythonExe);
            }
        }

        return null;
    }


    /**
     * Starts python and retrieves the python major version. Return 0 if the file is not python or some other
     * error
     * occured.
     *
     * @param file
     * @param python3Option
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public int getPythonMajorVersion(FilePath file, int wantedVersion) throws InterruptedException, IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ArgumentListBuilder command = new ArgumentListBuilder();
        command.add(file.getRemote());
        if (wantedVersion > 0)
        {
            command.addTokenized("-" + wantedVersion);
        }
        command.add("-c").add("import sys; print(sys.version_info.major)");
        logger.fine("Checking python Version of " + file.getRemote());
        logger.fine("Command: " + command.toString());

        final Launcher.ProcStarter proc = launcher.launch().cmds(command).stdout(output).quiet(true);

        if (proc.join() == 0)
        {
            try
            {
                logger.fine("Python Version is " + output.toString().trim());
                return Integer.parseInt(output.toString().trim());
            }
            catch (NumberFormatException e)
            {
                return 0;
            }
        }

        return 0;
    }
}
