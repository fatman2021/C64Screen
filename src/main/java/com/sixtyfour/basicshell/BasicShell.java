package com.sixtyfour.basicshell;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

import com.sixtyfour.Basic;


/**
 * A simple shell for loading/editing and running BASIC programs.
 * 
 * @author nietoperz809
 */
public class BasicShell
{
  static final ExecutorService executor = Executors.newFixedThreadPool(10);
  private final ArrayBlockingQueue<String> fromTextArea = new ArrayBlockingQueue<>(20);
  private final ArrayBlockingQueue<String> toTextArea = new ArrayBlockingQueue<>(20);
  private JTextArea mainTextArea;
  private JPanel panel1;
  private JButton stopButton;
  private JButton clsButton;
  private Runner runner = null;
  private int[] lastStrLen = new int[2]; // Length of last output chunk
  private int lineNum; // line number set by caret listener


  /**
   * Main thread entry point
   */
  public static void main(String[] unused)
  {
    JFrame frame = new JFrame("BasicShell");
    BasicShell shellFrame = new BasicShell();
    frame.setContentPane(shellFrame.panel1);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
    shellFrame.commandLoop();
  }


  /**
   * Returns length of the output string before the last one Needed by some input statements
   * 
   * @return Lengh of penultimate output
   */
  int getPenultimateOutputSize()
  {
    return lastStrLen[0];
  }


  private BasicShell()
  {
    setupUI();
    mainTextArea.addCaretListener(new CaretListener()
    {
      @Override
      public void caretUpdate(CaretEvent e)
      {
        JTextArea editArea = (JTextArea) e.getSource();
        try
        {
          int caretpos = editArea.getCaretPosition();
          lineNum = editArea.getLineOfOffset(caretpos);
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    });
    mainTextArea.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyReleased(KeyEvent e)
      {
        if (e.getKeyChar() == '\n')
        {
          try
          {
            fromTextArea.put(getLineAt(lineNum - 1));
          }
          catch (InterruptedException e1)
          {
            e1.printStackTrace();
          }
        }
        super.keyReleased(e);
      }
    });

    executor.execute(new Runnable()
    {
      @Override
      public void run()
      {
        while (true)
        {
          try
          {
            String s = toTextArea.take();
            mainTextArea.append(s);
            mainTextArea.setCaretPosition(mainTextArea.getDocument().getLength());
            if (runner != null && runner.getOlsenBasic() != null && runner.getOlsenBasic().isRunnning())
            {
              Thread.sleep(1);
            }
            else
            {
              Thread.yield();
            }
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
      }
    });
    stopButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (runner != null)
        {
          Basic i = runner.getOlsenBasic();
          i.runStop();
        }
      }
    });
    clsButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cls();
      }
    });
  }


  /**
   * Method generated by IntelliJ IDEA GUI Designer
   */
  private void setupUI()
  {
    panel1 = new JPanel();
    panel1.setLayout(new BorderLayout(0, 0));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
    panel2.setBackground(Color.BLACK);
    panel2.setPreferredSize(new Dimension(800, 34));
    panel1.add(panel2, BorderLayout.SOUTH);
    stopButton = new JButton();
    stopButton.setText("Stop");
    stopButton.setPreferredSize(new Dimension(82, 30));
    stopButton.setText("Stop");
    panel2.add(stopButton);
    clsButton = new JButton();
    clsButton.setPreferredSize(new Dimension(82, 30));
    clsButton.setText("Cls");
    panel2.add(clsButton);
    mainTextArea = new JTextArea();
    mainTextArea.setBackground(new Color(0x352879));
    mainTextArea.setDoubleBuffered(true);
    mainTextArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
    mainTextArea.setForeground(new Color(0x6C5EB5));
    mainTextArea.setCaretColor(new Color(0x6C5EB5));
    mainTextArea.setToolTipText("<html>Typ one of:<br>" + "- cls<br>- list<br>- run<br>- new<br>"
        + "- save[file]<br>- load[file]<br>- dir<br>" + "or edit your BASIC code here</html>");
    final JScrollPane scrollPane1 = new JScrollPane(mainTextArea);

    BlockCaret mc = new BlockCaret();
    mainTextArea.setCaret(mc);
    mc.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

    // DefaultCaret caret = (DefaultCaret) mainTextArea.getCaret();
    // caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    panel1.add(scrollPane1, BorderLayout.CENTER);
    panel1.setPreferredSize(new Dimension(800, 600));
  }


  /**
   * Return line at specified position
   * 
   * @param linenum
   *          Line number
   * @return Line as String
   */
  private String getLineAt(int linenum)
  {
    try
    {
      int start = mainTextArea.getLineStartOffset(linenum);
      int end = mainTextArea.getLineEndOffset(linenum);
      return mainTextArea.getText(start, end - start);
    }
    catch (BadLocationException e)
    {
      return ("");
    }
  }


  /**
   * Wipe text area
   */
  private void cls()
  {
    mainTextArea.setText("");
  }


  /**
   * 
   */
  private void dir()
  {
    File[] filesInFolder = new File(".").listFiles();
    for (File fileEntry : filesInFolder)
    {
      if (fileEntry.isFile())
      {
        putString(fileEntry.getName() + " -- " + fileEntry.length() + '\n');
      }
    }
  }


  /**
   * Command loop that runs in main thread
   */
  private void commandLoop()
  {
    ProgramStore store = new ProgramStore();
    while (true)
    {
      String s = getString();
      String sl = s.toLowerCase();
      if (sl.startsWith("load") || sl.startsWith("save"))
      {
        s = s.replace("\"", " ").trim();
      }
      String[] split = s.split(" ");
      s = s.toLowerCase();
      if (s.equals("list"))
      {
        putString(store.toString());
      }
      else if (s.equals("new"))
      {
        store.clear();
      }
      else if (s.equals("cls"))
      {
        cls();
      }
      else if (s.equals("dir"))
      {
        dir();
      }
      else if (s.equals("run"))
      {
        runner = new Runner(store.toArray(), this);
        runner.synchronousStart();
      }
      else if (split[0].toLowerCase().equals("save"))
      {
        String msg = store.save(split[1]);
        putString(msg);
      }
      else if (split[0].toLowerCase().equals("load"))
      {
        String msg = store.load(split[1]);
        putString(msg);
      }
      else
      {
        if (!store.insert(s))
        {
          if (runner == null)
          {
            runner = new Runner(new String[] {}, this);
          }
          try
          {

            runner.executeDirectCommand(s);
          }
          catch (Throwable t)
          {
            putString("?ERROR\n");
          }
        }
      }
    }
  }


  /**
   * Get input from text area. Blocks the caller if there is none
   * 
   * @return
   */
  public String getString()
  {
    try
    {
      return fromTextArea.take().trim();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
      return null;
    }
  }


  public boolean peek()
  {
    return fromTextArea.peek() != null;
  }


  /**
   * Send text to text area. Blocks thd caller if buffer is full
   * 
   * @param s
   */
  public void putString(String outText)
  {
    try
    {
      toTextArea.put(outText);
      lastStrLen[0] = lastStrLen[1];
      lastStrLen[1] = outText.length();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }
}
