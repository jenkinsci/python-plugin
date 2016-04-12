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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;

import hudson.DescriptorExtensionList;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.plugins.python.installations.AbstractPythonInstallation;
import hudson.plugins.python.installations.PythonExecutable;
import hudson.plugins.python.installations.PythonLocator;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.VariableResolver;

public abstract class AbstractPythonScript<T extends AbstractPythonInstallation> extends Builder implements Serializable
{
    private static final long serialVersionUID = -3956021436132554560L;

    protected String pythonName;
    protected String scriptArguments;
    protected ScriptSource scriptSource;

    protected boolean quiet;

    protected String options;

    private static Logger logger = Logger.getLogger(AbstractPythonScript.class.getName());


    public AbstractPythonScript(String pythonName, ScriptSource scriptSource, String scriptArgs, String options,
            boolean quiet)
    {
        this.pythonName = pythonName;
        this.scriptArguments = scriptArgs;
        this.scriptSource = scriptSource;
        this.options = options;
        this.quiet = quiet;
    }


    public boolean getQuiet()
    {
        return quiet;
    }


    public String getOptions()
    {
        return options;
    }


    public String getPythonName()
    {
        return pythonName;
    }


    public String getScriptArgs()
    {
        return scriptArguments;
    }


    public ScriptSource getScriptSource()
    {
        return scriptSource;
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException
    {

        if (scriptSource == null)
        {
            listener.fatalError("There is no script configured for this builder");
            return false;
        }

        FilePath ws = build.getWorkspace();
        FilePath script = null;
        try
        {
            script = scriptSource.getScriptFile(ws, build, listener);
        }
        catch (IOException e)
        {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("Unable to produce a script file"));
            return false;
        }

        try
        {
            List<String> cmd = buildCommandLine(build, listener, launcher, script);
            int result;
            try
            {
                Map<String, String> envVars = build.getEnvironment(listener);

                for (Map.Entry<String, String> e : build.getBuildVariables().entrySet())
                {
                    envVars.put(e.getKey(), e.getValue());
                }

                envVars.put("$PATH_SEPARATOR", ":::"); // TODO why??

                result = launcher.launch().cmds(cmd.toArray(new String[] { })).envs(envVars).stdout(listener).pwd(ws)
                        .quiet(quiet).join();
            }
            catch (IOException e)
            {
                Util.displayIOException(e, listener);
                e.printStackTrace(listener.fatalError("command execution failed"));
                result = -1;
            }
            return result == 0;
        }
        finally
        {
            try
            {
                if ((scriptSource instanceof StringSource) && (script != null))
                {
                    script.delete();
                }
            }
            catch (IOException e)
            {
                Util.displayIOException(e, listener);
                e.printStackTrace(listener.fatalError("Unable to delete script file " + script));
            }
        }
    }


    protected abstract T getPython();


    @SuppressWarnings("unchecked")
    protected List<String> buildCommandLine(AbstractBuild<?, ?> build, BuildListener listener, Launcher launcher,
            FilePath script) throws IOException, InterruptedException
    {
        ArrayList<String> list = new ArrayList<String>();

        EnvVars env = build.getEnvironment(listener);
        env.overrideAll(build.getBuildVariables());

        PythonExecutable cmd = null;

        T installation = getPython();
        if (installation != null)
        {
            installation = (T)installation.forNode(Computer.currentComputer().getNode(), listener);
            installation = (T)installation.forEnvironment(env);
            cmd = installation.getExecutable(launcher);
        }

        if (cmd == null)
        {
            PythonLocator locator = new PythonLocator(launcher);
            PythonExecutable pythonExe = locator.findPythonForVersion(getRequiredPythonVersion());
            if (pythonExe == null)
            {
                throw new IOException(
                        "No Python found that matches the requested python version " + getRequiredPythonVersion());
            }
            cmd = pythonExe;
            logger.fine(
                    "[Python WARNING] Python executable is not configured, please check your Python configuration.");
            logger.fine("[Python WARNING] Using python found at " + cmd.getExecutable());
        }

        list.add(cmd.getExecutable());
        list.addAll(cmd.getArguments());

        list.addAll(parseArgumentsAndOptions(options));
        list.add(script.getRemote());

        if (StringUtils.isNotBlank(scriptArguments))
        {
            VariableResolver<String> evr = new VariableResolver.ByMap<String>(env);
            VariableResolver<String> pvr = build.getBuildVariableResolver();
            List<String> params = parseArgumentsAndOptions(scriptArguments);
            for (String param : params)
            {
                String p = Util.replaceMacro(param, evr);
                p = Util.replaceMacro(p, pvr);
                list.add(p);
            }
        }

        return list;
    }


    protected abstract int getRequiredPythonVersion();


    private List<String> parseArgumentsAndOptions(String line)
    {
        CommandLine cmdLine = CommandLine.parse("dummy_executable " + line);
        List<String> args = new ArrayList<String>();
        CollectionUtils.addAll(args, cmdLine.getArguments());
        return args;
    }

    public abstract static class AbstractPythonScriptDescriptor<T> extends BuildStepDescriptor<Builder>
    {

        public abstract T[] getInstallations();


        public AbstractPythonScriptDescriptor(Class<? extends Builder> clazz)
        {
            super(clazz);
        }

        private AtomicInteger instanceCounter = new AtomicInteger(0);


        public int nextInstanceID()
        {
            return instanceCounter.incrementAndGet();
        }


        @Override
        @SuppressWarnings("rawtypes")
        public boolean isApplicable(Class<? extends AbstractProject> jobType)
        {
            return true;
        }


        public static DescriptorExtensionList<ScriptSource, Descriptor<ScriptSource>> getScriptSources()
        {
            return ScriptSource.all();
        }
    }
}
