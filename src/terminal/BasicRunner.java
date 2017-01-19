package terminal;

import com.sixtyfour.Basic;
import com.sixtyfour.DelayTracer;

/**
 * Proxy class to instantiate an run the BASIC system
 */
public class BasicRunner implements Runnable
{
    private static volatile boolean running = false;
    private Basic olsenBasic;
    private C64Screen shellFrame;

    public BasicRunner (String[] program, boolean slow, C64Screen shellFrame)
    {
        if (running)
        {
            return;
        }
        this.shellFrame = shellFrame;
        olsenBasic = new Basic(program);
        if (slow)
        {
            DelayTracer t = new DelayTracer(1000);
            olsenBasic.setTracer(t);
        }
        olsenBasic.getMachine().setMemoryListener(new PeekPokeHandler(shellFrame));
        olsenBasic.setOutputChannel(new ShellOutputChannel(shellFrame));
        olsenBasic.setInputProvider(new ShellInputProvider(shellFrame));
    }

    /**
     * Compile an run a single line
     *
     * @param in the BASIC line
     * @param sf reference to shell main window
     * @return textual representation of success/error
     */
    public static String runSingleLine (String in, C64Screen sf)
    {
        try
        {
            Basic b = new Basic("0 " + in.toUpperCase());
            b.getMachine().setMemoryListener(new PeekPokeHandler(sf));
            b.compile();
            b.setOutputChannel(new ShellOutputChannel(sf));
            b.setInputProvider(new ShellInputProvider(sf));
            b.start();
            return "";
        }
        catch (Exception ex)
        {
            return ex.getMessage().toUpperCase()+"\n";
        }
    }

    /**
     * Start BASIC task
     *
     * @param synchronous if true the caller is blocked
     */
    public void start (boolean synchronous)
    {
        if (running)
        {
            System.out.println("already running ...");
            return;
        }
        Thread t = new Thread (this);
        t.start();
        if (!synchronous)
        {
            return;
        }
        try
        {
            t.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean isRunning ()
    {
        return running;
    }

    public Basic getOlsenBasic ()
    {
        return olsenBasic;
    }

    @Override
    public void run ()
    {
        running = true;
        try
        {
//            SidRunner.reset();
//            SwingUtilities.invokeAndWait(() ->
//                    shellFrame.runButton.setEnabled(false));
            olsenBasic.run();
            //SidRunner.reset();
//            SwingUtilities.invokeAndWait(() ->
//                    shellFrame.runButton.setEnabled(true)
//            );
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            running = false;
        }
    }
}