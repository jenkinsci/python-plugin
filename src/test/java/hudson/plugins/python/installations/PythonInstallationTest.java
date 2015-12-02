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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.powermock.api.easymock.PowerMock.expectNew;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hudson.Launcher;
import hudson.remoting.LocalChannel;
import hudson.remoting.VirtualChannel;
import hudson.util.ArgumentListBuilder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PythonLocator.class, Launcher.ProcStarter.class, Launcher.class })

public class PythonInstallationTest
{
    @Rule
    private TemporaryFolder tmpRule = new TemporaryFolder();

    File python2Folder;
    File python3Folder;
    File python2;
    File python3;
    File python3ux;
    File python3win;

    Launcher launcher;
    Launcher.ProcStarter proc;
    VirtualChannel channel;


    @Before
    public void setup() throws Exception
    {
        python2Folder = tmpRule.newFolder("Python2");
        python3Folder = tmpRule.newFolder("Python3");
        python2 = new File(python2Folder, "py.exe");
        python3 = new File(python3Folder, "py.exe");
        python3win = new File(python3Folder, "python.exe");
        python3ux = new File(python3Folder, "python3");
        FileUtils.writeStringToFile(python2, "");
        FileUtils.writeStringToFile(python3, "");
        FileUtils.writeStringToFile(python3ux, "");
        launcher = PowerMock.createMock(Launcher.class);
        proc = PowerMock.createMock(Launcher.ProcStarter.class);
        channel = PowerMock.createMock(LocalChannel.class);
        expect(launcher.getChannel()).andReturn(channel);
    }


    @Test
    public void testPython2InstallationReturns2AsRequired() throws Exception
    {
        Python2Installation python2Installation = new Python2Installation("python2", python2.getAbsolutePath(), null);
        assertEquals(python2Installation.getRequiredPythonVersion(), 2);
    }


    private void defineExpectsForGetPythonMajorVersion(char version) throws Exception
    {
        expect(launcher.launch()).andReturn(proc);
        expect(proc.cmds(anyObject(ArgumentListBuilder.class))).andReturn(proc);
        expect(proc.stdout(anyObject(ByteArrayOutputStream.class))).andReturn(proc);
        expect(proc.quiet(true)).andReturn(proc);
        expect(proc.join()).andReturn(0);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(version);

        expectNew(ByteArrayOutputStream.class).andReturn(output);
    }


    @Test
    public void testPython2InstallationViaPy() throws Exception
    {
        defineExpectsForGetPythonMajorVersion('2');
        expect(launcher.getChannel()).andReturn(channel);

        PowerMock.replayAll();

        Python2Installation python2Installation = new Python2Installation("python2", python2.getAbsolutePath(), null);
        PythonExecutable executable = python2Installation.getExecutable(launcher);
        assertThat(executable.getArguments(), contains("-2"));
    }


    @Test
    public void testPython3InstallationViaPy() throws Exception
    {
        defineExpectsForGetPythonMajorVersion('3');
        expect(launcher.getChannel()).andReturn(channel);

        PowerMock.replayAll();

        Python3Installation python2Installation = new Python3Installation("python3", python3.getAbsolutePath(), null);
        PythonExecutable executable = python2Installation.getExecutable(launcher);
        assertThat(executable.getArguments(), contains("-3"));
    }


    @Test
    public void testPython3InstallationUnix() throws Exception
    {
        defineExpectsForGetPythonMajorVersion('3');
        expect(launcher.getChannel()).andReturn(channel);

        PowerMock.replayAll();

        Python3Installation python3Installation = new Python3Installation("python3", python3ux.getAbsolutePath(), null);
        PythonExecutable executable = python3Installation.getExecutable(launcher);
        assertThat(executable.getArguments(), empty());
        assertThat(executable.getExecutable(), is(python3ux.getAbsolutePath()));
    }


    @Test
    public void testPython3InstallationWindows() throws Exception
    {
        defineExpectsForGetPythonMajorVersion('3');
        expect(launcher.getChannel()).andReturn(channel);

        PowerMock.replayAll();

        Python3Installation python3Installation = new Python3Installation("python3", python3win.getAbsolutePath(),
                null);
        PythonExecutable executable = python3Installation.getExecutable(launcher);
        assertThat(executable.getArguments(), empty());
        assertThat(executable.getExecutable(), is(python3win.getAbsolutePath()));
    }
}
