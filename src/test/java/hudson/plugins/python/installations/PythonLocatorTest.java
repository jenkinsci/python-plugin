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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.powermock.api.easymock.PowerMock.expectNew;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.remoting.LocalChannel;
import hudson.remoting.VirtualChannel;
import hudson.util.ArgumentListBuilder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ VirtualChannel.class, PythonLocator.class, Launcher.class, Launcher.ProcStarter.class,
    EnvVars.class })
public class PythonLocatorTest extends EasyMockSupport
{

    @Rule
    private TemporaryFolder tmpRule = new TemporaryFolder();

    FilePath tmpPython;
    File windows;
    File python2Folder;
    File python3Folder;
    Launcher launcher;
    Launcher.ProcStarter proc;
    VirtualChannel channel;
    File py;
    File python2;
    File python3;
    File python2ux;
    File python3ux;


    @Before
    public void setup() throws Exception
    {

        python2Folder = tmpRule.newFolder("Python2");
        python3Folder = tmpRule.newFolder("Python3");
        windows = tmpRule.newFolder("Windows");
        File tmpFile = tmpRule.newFile("python.exe");
        tmpPython = new FilePath(tmpFile);
        python2 = new File(python2Folder, "python.exe");
        python3 = new File(python3Folder, "python.exe");
        python2ux = new File(python2Folder, "python2");
        python3ux = new File(python3Folder, "python3");
        py = new File(windows, "py.exe");
        FileUtils.writeStringToFile(python2, "");
        FileUtils.writeStringToFile(python3, "");
        FileUtils.writeStringToFile(python2ux, "");
        FileUtils.writeStringToFile(python3ux, "");
        FileUtils.writeStringToFile(py, "");
        FileUtils.writeStringToFile(tmpFile, "");

        launcher = PowerMock.createMock(Launcher.class);
        proc = PowerMock.createMock(Launcher.ProcStarter.class);
        channel = PowerMock.createMock(LocalChannel.class);
        expect(launcher.getChannel()).andReturn(channel);
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
    public void testFileInPathWithExeExtensionIsFound() throws IOException, InterruptedException
    {
        EnvVars envvars = PowerMock.createMock(EnvVars.class);
        PowerMock.mockStatic(EnvVars.class);
        defineExpectsForFindFileInPath(python2Folder, envvars, false);

        PowerMock.replayAll();

        PythonLocator locator = new PythonLocator(launcher);
        FilePath python = locator.findFileInPath("python.exe");
        assertThat(python, is(notNullValue()));
    }


    @Test
    public void testGetPython2Version() throws Exception
    {

        defineExpectsForGetPythonMajorVersion('2');
        PowerMock.replayAll();

        PythonLocator locator = new PythonLocator(launcher);
        int version = locator.getPythonMajorVersion(tmpPython, Python2Installation.PYTHON_VERSION);
        assertThat(version, is(Python2Installation.PYTHON_VERSION));
        PowerMock.verifyAll();
    }


    @Test
    public void testGetPython3Version() throws Exception
    {
        defineExpectsForGetPythonMajorVersion('3');

        PowerMock.replayAll();

        PythonLocator locator = new PythonLocator(launcher);
        int version = locator.getPythonMajorVersion(tmpPython, Python3Installation.PYTHON_VERSION);
        assertThat(version, is(Python3Installation.PYTHON_VERSION));
        PowerMock.verifyAll();
    }


    private String unixLikePath(String path)
    {
        path = path.replaceAll("\\\\", "/");
        path = path.replaceFirst("^.:", "");
        return path;
    }


    private void defineExpectsForFindFileInPath(File folder, EnvVars envvars, boolean isUnix)
            throws IOException, InterruptedException
    {
        expect(EnvVars.getRemote(channel)).andReturn(envvars);
        String path = folder.getPath();
        if (isUnix)
        {
            path = unixLikePath(path);
        }

        expect(envvars.get("PATH")).andReturn(path);
        expect(launcher.isUnix()).andReturn(isUnix);
    }


    @Test
    public void testFindPython2ForWindowsViaPy() throws Exception
    {
        EnvVars envvars = PowerMock.createMock(EnvVars.class);
        PowerMock.mockStatic(EnvVars.class);
        defineExpectsForFindFileInPath(windows, envvars, false);
        defineExpectsForGetPythonMajorVersion('2');

        PowerMock.replayAll();

        PythonLocator locator = new PythonLocator(launcher);

        PythonExecutable f = locator.findPythonVersionForWindows(2);
        assertThat(f.getExecutable(), is(py.getAbsolutePath()));
        PowerMock.verifyAll();
    }


    @Test
    public void testFindPython2ForWindowsViaPython() throws Exception
    {
        EnvVars envvars = PowerMock.createMock(EnvVars.class);
        PowerMock.mockStatic(EnvVars.class);
        defineExpectsForFindFileInPath(python2Folder, envvars, false);
        defineExpectsForFindFileInPath(python2Folder, envvars, false);
        defineExpectsForGetPythonMajorVersion('2');

        PowerMock.replayAll();

        PythonLocator locator = new PythonLocator(launcher);

        PythonExecutable f = locator.findPythonVersionForWindows(2);
        assertThat(f.getExecutable().toLowerCase(), is(python2.getAbsolutePath().toLowerCase()));
        PowerMock.verifyAll();
    }


    @Test
    public void testFindPython3ForWindowsViaPy() throws Exception
    {
        EnvVars envvars = PowerMock.createMock(EnvVars.class);
        PowerMock.mockStatic(EnvVars.class);
        defineExpectsForFindFileInPath(windows, envvars, false);
        defineExpectsForGetPythonMajorVersion('3');

        PowerMock.replayAll();

        PythonLocator locator = new PythonLocator(launcher);

        PythonExecutable f = locator.findPythonVersionForWindows(Python3Installation.PYTHON_VERSION);
        assertThat(f.getExecutable(), is(py.getAbsolutePath()));
        PowerMock.verifyAll();

    }


    @Test
    public void testFindPython3ForWindowsViaPython() throws Exception
    {
        EnvVars envvars = PowerMock.createMock(EnvVars.class);
        PowerMock.mockStatic(EnvVars.class);
        defineExpectsForFindFileInPath(python3Folder, envvars, false);
        defineExpectsForFindFileInPath(python3Folder, envvars, false);
        defineExpectsForGetPythonMajorVersion('3');

        PowerMock.replayAll();

        PythonLocator locator = new PythonLocator(launcher);

        PythonExecutable f = locator.findPythonVersionForWindows(Python3Installation.PYTHON_VERSION);
        assertThat(f.getExecutable().toLowerCase(), is(python3.getAbsolutePath().toLowerCase()));
        PowerMock.verifyAll();
    }


    // This test runs on windows only when the tmp folder is on the same drive
    // where the test is executed
    @Test
    public void testFindPython2ForUnix() throws Exception
    {
        EnvVars envvars = PowerMock.createMock(EnvVars.class);
        PowerMock.mockStatic(EnvVars.class);
        defineExpectsForFindFileInPath(python2Folder, envvars, true);
        defineExpectsForGetPythonMajorVersion('2');

        PowerMock.replayAll();

        PythonLocator locator = new PythonLocator(launcher);

        PythonExecutable f = locator.findPythonVersionForUnix(Python2Installation.PYTHON_VERSION);
        String expectedPath = unixLikePath(python2ux.getAbsolutePath().toLowerCase());
        String foundPath = unixLikePath(f.getExecutable().toLowerCase());
        assertThat(foundPath, is(expectedPath));
        PowerMock.verifyAll();
    }


    // This test runs on windows only when the tmp folder is on the same drive
    // where the test is executed
    @Test
    public void testFindPython3ForUnix() throws Exception
    {
        EnvVars envvars = PowerMock.createMock(EnvVars.class);
        PowerMock.mockStatic(EnvVars.class);
        defineExpectsForFindFileInPath(python3Folder, envvars, true);
        defineExpectsForGetPythonMajorVersion('3');

        PowerMock.replayAll();

        PythonLocator locator = new PythonLocator(launcher);

        PythonExecutable f = locator.findPythonVersionForUnix(Python3Installation.PYTHON_VERSION);
        String expectedPath = unixLikePath(python3ux.getAbsolutePath().toLowerCase());
        String foundPath = unixLikePath(f.getExecutable().toLowerCase());
        assertThat(foundPath, is(expectedPath));
        PowerMock.verifyAll();
    }
}
