
package EzSearchClient;

import java.util.*;
import java.util.Timer;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.text.Document;
import java.io.*;
import java.net.InetAddress;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
//import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

class WinRegistry {
  public static final int HKEY_CURRENT_USER = 0x80000001;
  public static final int HKEY_LOCAL_MACHINE = 0x80000002;
  public static final int REG_SUCCESS = 0;
  public static final int REG_NOTFOUND = 2;
  public static final int REG_ACCESSDENIED = 5;

  private static final int KEY_ALL_ACCESS = 0xf003f;
  private static final int KEY_READ = 0x20019;
  private static Preferences userRoot = Preferences.userRoot();
  private static Preferences systemRoot = Preferences.systemRoot();
  private static Class<? extends Preferences> userClass = userRoot.getClass();
  private static Method regOpenKey = null;
  private static Method regCloseKey = null;
  private static Method regQueryValueEx = null;
  private static Method regEnumValue = null;
  private static Method regQueryInfoKey = null;
  private static Method regEnumKeyEx = null;
  private static Method regCreateKeyEx = null;
  private static Method regSetValueEx = null;
  private static Method regDeleteKey = null;
  private static Method regDeleteValue = null;

  static {
    try {
      regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey",
          new Class[] { int.class, byte[].class, int.class });
      regOpenKey.setAccessible(true);
      regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey",
          new Class[] { int.class });
      regCloseKey.setAccessible(true);
      regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx",
          new Class[] { int.class, byte[].class });
      regQueryValueEx.setAccessible(true);
      regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue",
          new Class[] { int.class, int.class, int.class });
      regEnumValue.setAccessible(true);
      regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1",
          new Class[] { int.class });
      regQueryInfoKey.setAccessible(true);
      regEnumKeyEx = userClass.getDeclaredMethod(
          "WindowsRegEnumKeyEx", new Class[] { int.class, int.class,
              int.class });
      regEnumKeyEx.setAccessible(true);
      regCreateKeyEx = userClass.getDeclaredMethod(
          "WindowsRegCreateKeyEx", new Class[] { int.class,
              byte[].class });
      regCreateKeyEx.setAccessible(true);
      regSetValueEx = userClass.getDeclaredMethod(
          "WindowsRegSetValueEx", new Class[] { int.class,
              byte[].class, byte[].class });
      regSetValueEx.setAccessible(true);
      regDeleteValue = userClass.getDeclaredMethod(
          "WindowsRegDeleteValue", new Class[] { int.class,
              byte[].class });
      regDeleteValue.setAccessible(true);
      regDeleteKey = userClass.getDeclaredMethod(
          "WindowsRegDeleteKey", new Class[] { int.class,
              byte[].class });
      regDeleteKey.setAccessible(true);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private WinRegistry() {  }

  /**
   * Read a value from key and value name
   * @param hkey   HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
   * @param key
   * @param valueName
   * @return the value
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static String readString(int hkey, String key, String valueName)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    if (hkey == HKEY_LOCAL_MACHINE) {
      return readString(systemRoot, hkey, key, valueName);
    }
    else if (hkey == HKEY_CURRENT_USER) {
      return readString(userRoot, hkey, key, valueName);
    }
    else {
      throw new IllegalArgumentException("hkey=" + hkey);
    }
  }

  /**
   * Read value(s) and value name(s) form given key
   * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
   * @param key
   * @return the value name(s) plus the value(s)
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static Map<String, String> readStringValues(int hkey, String key)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    if (hkey == HKEY_LOCAL_MACHINE) {
      return readStringValues(systemRoot, hkey, key);
    }
    else if (hkey == HKEY_CURRENT_USER) {
      return readStringValues(userRoot, hkey, key);
    }
    else {
      throw new IllegalArgumentException("hkey=" + hkey);
    }
  }

  /**
   * Read the value name(s) from a given key
   * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
   * @param key
   * @return the value name(s)
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static List<String> readStringSubKeys(int hkey, String key)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    if (hkey == HKEY_LOCAL_MACHINE) {
      return readStringSubKeys(systemRoot, hkey, key);
    }
    else if (hkey == HKEY_CURRENT_USER) {
      return readStringSubKeys(userRoot, hkey, key);
    }
    else {
      throw new IllegalArgumentException("hkey=" + hkey);
    }
  }

  /**
   * Create a key
   * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
   * @param key
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static void createKey(int hkey, String key)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    int [] ret;
    if (hkey == HKEY_LOCAL_MACHINE) {
      ret = createKey(systemRoot, hkey, key);
      regCloseKey.invoke(systemRoot, new Object[] { new Integer(ret[0]) });
    }
    else if (hkey == HKEY_CURRENT_USER) {
      ret = createKey(userRoot, hkey, key);
      regCloseKey.invoke(userRoot, new Object[] { new Integer(ret[0]) });
    }
    else {
      throw new IllegalArgumentException("hkey=" + hkey);
    }
    if (ret[1] != REG_SUCCESS) {
      throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
    }
  }

  /**
   * Write a value in a given key/value name
   * @param hkey
   * @param key
   * @param valueName
   * @param value
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static void writeStringValue
    (int hkey, String key, String valueName, String value)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    if (hkey == HKEY_LOCAL_MACHINE) {
      writeStringValue(systemRoot, hkey, key, valueName, value);
    }
    else if (hkey == HKEY_CURRENT_USER) {
      writeStringValue(userRoot, hkey, key, valueName, value);
    }
    else {
      throw new IllegalArgumentException("hkey=" + hkey);
    }
  }

  /**
   * Delete a given key
   * @param hkey
   * @param key
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static void deleteKey(int hkey, String key)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    int rc = -1;
    if (hkey == HKEY_LOCAL_MACHINE) {
      rc = deleteKey(systemRoot, hkey, key);
    }
    else if (hkey == HKEY_CURRENT_USER) {
      rc = deleteKey(userRoot, hkey, key);
    }
    if (rc != REG_SUCCESS) {
      throw new IllegalArgumentException("rc=" + rc + "  key=" + key);
    }
  }

  /**
   * delete a value from a given key/value name
   * @param hkey
   * @param key
   * @param value
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static void deleteValue(int hkey, String key, String value)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    int rc = -1;
    if (hkey == HKEY_LOCAL_MACHINE) {
      rc = deleteValue(systemRoot, hkey, key, value);
    }
    else if (hkey == HKEY_CURRENT_USER) {
      rc = deleteValue(userRoot, hkey, key, value);
    }
    if (rc != REG_SUCCESS) {
      throw new IllegalArgumentException("rc=" + rc + "  key=" + key + "  value=" + value);
    }
  }

  // =====================

  private static int deleteValue
    (Preferences root, int hkey, String key, String value)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {
        new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) });
    if (handles[1] != REG_SUCCESS) {
      return handles[1];  // can be REG_NOTFOUND, REG_ACCESSDENIED
    }
    int rc =((Integer) regDeleteValue.invoke(root,
        new Object[] {
          new Integer(handles[0]), toCstr(value)
          })).intValue();
    regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
    return rc;
  }

  private static int deleteKey(Preferences root, int hkey, String key)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    int rc =((Integer) regDeleteKey.invoke(root,
        new Object[] { new Integer(hkey), toCstr(key) })).intValue();
    return rc;  // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
  }

  private static String readString(Preferences root, int hkey, String key, String value)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {
        new Integer(hkey), toCstr(key), new Integer(KEY_READ) });
    if (handles[1] != REG_SUCCESS) {
      return null;
    }
    byte[] valb = (byte[]) regQueryValueEx.invoke(root, new Object[] {
        new Integer(handles[0]), toCstr(value) });
    regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
    return (valb != null ? new String(valb).trim() : null);
  }

  private static Map<String,String> readStringValues
    (Preferences root, int hkey, String key)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    HashMap<String, String> results = new HashMap<String,String>();
    int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {
        new Integer(hkey), toCstr(key), new Integer(KEY_READ) });
    if (handles[1] != REG_SUCCESS) {
      return null;
    }
    int[] info = (int[]) regQueryInfoKey.invoke(root,
        new Object[] { new Integer(handles[0]) });

    int count = info[0]; // count
    int maxlen = info[3]; // value length max
    for(int index=0; index<count; index++)  {
      byte[] name = (byte[]) regEnumValue.invoke(root, new Object[] {
          new Integer
            (handles[0]), new Integer(index), new Integer(maxlen + 1)});
      String value = readString(hkey, key, new String(name));
      results.put(new String(name).trim(), value);
    }
    regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
    return results;
  }

  private static List<String> readStringSubKeys
    (Preferences root, int hkey, String key)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    List<String> results = new ArrayList<String>();
    int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {
        new Integer(hkey), toCstr(key), new Integer(KEY_READ)
        });
    if (handles[1] != REG_SUCCESS) {
      return null;
    }
    int[] info = (int[]) regQueryInfoKey.invoke(root,
        new Object[] { new Integer(handles[0]) });

    int count  = info[0]; // Fix: info[2] was being used here with wrong results. Suggested by davenpcj, confirmed by Petrucio
    int maxlen = info[3]; // value length max
    for(int index=0; index<count; index++)  {
      byte[] name = (byte[]) regEnumKeyEx.invoke(root, new Object[] {
          new Integer
            (handles[0]), new Integer(index), new Integer(maxlen + 1)
          });
      results.add(new String(name).trim());
    }
    regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
    return results;
  }

  private static int [] createKey(Preferences root, int hkey, String key)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    return  (int[]) regCreateKeyEx.invoke(root,
        new Object[] { new Integer(hkey), toCstr(key) });
  }

  private static void writeStringValue
    (Preferences root, int hkey, String key, String valueName, String value)
    throws IllegalArgumentException, IllegalAccessException,
    InvocationTargetException
  {
    int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {
        new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) });

    regSetValueEx.invoke(root,
        new Object[] {
          new Integer(handles[0]), toCstr(valueName), toCstr(value)
          });
    regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
  }

  // utility
  private static byte[] toCstr(String str) {
    byte[] result = new byte[str.length() + 1];

    for (int i = 0; i < str.length(); i++) {
      result[i] = (byte) str.charAt(i);
    }
    result[str.length()] = 0;
    return result;
  }
}

class xml_convert {
  public static String ENDL = System.getProperty("line.separator");

//  String xml_base_file   = "default_original.xml";
//  String xml_source_file = "default_source.xml";
//  String xml_output_file = "default_output.xml";

  StringBuffer xml_base_buffer = null;
  String xml_pdk_info  = "";
  String xml_pdk_table = "";

  BufferedReader br = null;

  public boolean compare(String buffer) {
    try {
      int s_pos = buffer.indexOf("<project");
      if(s_pos > 0)
      {
        s_pos = buffer.indexOf("name=\"") + 6;
        if(s_pos > 6)
        {
          int e_pos = buffer.indexOf("\"", s_pos);
          String name = buffer.substring(s_pos, e_pos);
          if(xml_base_buffer.indexOf(name) < 0)
          {
            //System.out.println(name + " : s_pos " + s_pos + " e_pos" + e_pos + " : " +buffer);
            return false;
          }
        }
      }
    } catch (Exception e) {
      System.out.println(buffer);
      //e.printStackTrace();
      return true;
    } finally {
    }
    return true;
  }

  public xml_convert(String xml_base_file, String xml_source_file) {

    if(xml_base_file == null || xml_base_file == "")
      return;

    if(xml_source_file == null || xml_source_file == "")
      return;

    // Read and display the file contents. Since we're reading text, we
    // use a FileReader instead of a FileInputStream.
    try {
      //Make Base Format Buffer
      br = new BufferedReader(new FileReader(new File(xml_base_file)));

      xml_base_buffer = new StringBuffer();

      while (true)
      {
        String readLineData = br.readLine();
        if(readLineData == null)
          break;

        if(readLineData.indexOf("<remote  name=\"pdk\"") >= 0) //<remote  name="pdk"
        {
          while (true)
          {
            xml_pdk_info = xml_pdk_info + ENDL + readLineData;
            if(readLineData == null || readLineData.indexOf("/>") >= 0)
              break;
            readLineData = br.readLine();
          }
        }
        else if(readLineData.indexOf("<project path=\"android/vendor/pdk\"") >= 0) //<project path="android/vendor/pdk"
        {
          while (true)
          {
            xml_pdk_table = xml_pdk_table + ENDL + readLineData;
            if(readLineData == null || readLineData.indexOf("/>") >= 0)
              break;
            readLineData = br.readLine();
          }
        }
        else if(readLineData.indexOf("revision=\"gh16_mm_6.0.0-pdk\"") >= 0) //revision="gh16_mm_6.0.0-pdk"
        {
          while (true)
          {
            xml_pdk_table = xml_pdk_table + ENDL + readLineData;
            if(readLineData == null || readLineData.indexOf("/>") >= 0)
              break;
            readLineData = br.readLine();
          }
        }
        else
        {
          xml_base_buffer.append(readLineData);
        }
      }
      br.close();

      br = new BufferedReader(new FileReader(new File(xml_source_file)));

      String xml_output_file = xml_source_file + "_output.xml";
      //Make Out File
      FileWriter fw = new FileWriter(xml_output_file, false); //the true will append the new data

      int count = 0;
      while (true)
      {
        String readLineData = br.readLine();

        if(readLineData == null)
          break;

        count++;
        if(compare(readLineData) == false) {
          continue;
        }
        fw.write(readLineData + ENDL);//appends the string to the file

        if(readLineData.indexOf("<default remote=\"origin\"") >= 0)
        {
          fw.write(xml_pdk_info  + ENDL);//appends the string to the file
          fw.write(xml_pdk_table + ENDL);//appends the string to the file
        }
      }
      //System.out.println("count : " + count);
      br.close();
      fw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

class DOSOpen {
  public static String FSEPARATOR = System.getProperty("file.separator");
  public DOSOpen(String dir, String csearchindex){
    try{
      String osName = System.getProperty("os.name").toLowerCase();
      ProcessBuilder pb = new ProcessBuilder();
      List<String> cmdList = new ArrayList<String>();

      if(osName.indexOf("windwos 9") > -1){
        cmdList.add("command.com");
        cmdList.add("/c");
        cmdList.add("start");
      } else if(
        (osName.indexOf("nt") > -1) ||
        (osName.indexOf("windows 2000") > -1) ||
        (osName.indexOf("windows 7") > -1) ||
        (osName.indexOf("windows xp") > -1)) {
        cmdList.add("cmd.exe");
        cmdList.add("/c");
        cmdList.add("start");
      } else if((osName.indexOf("linux") > -1)) {
        cmdList.add("/usr/bin/xterm");
      }

      System.out.println("OS : " + osName + " / CMD : " + cmdList);

      pb = pb.directory(new File(dir));

      Map<String, String> env = pb.environment();
      if(csearchindex != "")
      {
        File fh = new File(csearchindex);
        if(fh.exists())
        {
          env.put("CSEARCHINDEX", csearchindex);
        }
      }

      pb.command(cmdList);
      pb.start();

      cmdList.clear();
      cmdList = null;
      pb = null;
    }catch (IOException e) {
      e.printStackTrace();
    } finally {
    }
  }

  public DOSOpen(String dir, String cmd, String param){
    try{
      String osName = System.getProperty("os.name").toLowerCase();
      ProcessBuilder pb = new ProcessBuilder();
      List<String> cmdList = new ArrayList<String>();

      cmdList.add(dir + FSEPARATOR + cmd);
      cmdList.add(param);

      System.out.println("OS : " + osName + " / CMD : " + cmdList);

      pb = pb.directory(new File(dir));
      pb.command(cmdList);
      pb.start();

      cmdList.clear();
      cmdList = null;
      pb = null;
    }catch (IOException e) {
      e.printStackTrace();
    }
  }
}

class SearchFileView extends JDialog implements ActionListener {
  public static String ENDL = System.getProperty("line.separator");
  // TextPane 생성
  JTextPane textPane = new JTextPane();
  JScrollPane jsp = new JScrollPane(textPane);
  JButton btFind = new JButton("          Find          ");
  JButton btExit = new JButton("          Exit          ");
  private JTextField find;
  private int toffset=0;

  public static String word;
  public static Highlighter highlighter = new UnderlineHighlighter(null);
  final WordSearcher searcher = new WordSearcher(textPane);

  public SearchFileView(String pattern, String filePath, int line, Dimension size, Point pos) {

    addWindowListener( new WindowAdapter() {

      public void windowOpened( WindowEvent e ){
        if(EzSearchClient.IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }

      @Override
      public void windowClosing(WindowEvent e)
      {
          dispose();
          Runtime.getRuntime().gc();
          //System.gc();
      }

/*
      public void windowIconified( WindowEvent e ){
        pattern.setText(paste().toString());
      }

      public void windowDeiconified( WindowEvent e ){
        pattern.setText(paste().toString());
      }
*/
      public void windowActivated( WindowEvent e ){
        if(EzSearchClient.IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }

      public void windowDeactivated( WindowEvent e ){
        if(EzSearchClient.IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }
    });

    if ((filePath == null) || (filePath.length() == 0))
      return;

    JPanel northP = new JPanel();
    northP.setLayout(new BoxLayout(northP,BoxLayout.X_AXIS));

    find = new JTextField(20);
    find.setText(pattern);
    northP.add( find, BorderLayout.WEST);
    northP.add(btFind, BorderLayout.CENTER);
    northP.add(btExit, BorderLayout.EAST);
    this.getContentPane().add(northP, BorderLayout.NORTH);

    this.getContentPane().add(jsp, BorderLayout.CENTER);
    textPane.setContentType("text/html");

    int condition = JComponent.WHEN_FOCUSED;
    InputMap iMap = textPane.getInputMap(condition);
    ActionMap aMap = textPane.getActionMap();

    String enter = "enter";
    iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enter);
    aMap.put(enter, new AbstractAction() {

       @Override
       public void actionPerformed(ActionEvent arg0) {
          btFind.doClick();
       }
    });

    String escape = "escape";
    iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escape);
    aMap.put(escape, new AbstractAction() {

       @Override
       public void actionPerformed(ActionEvent arg0) {
          dispose();
       }
    });

    InputMap ifMap = find.getInputMap(condition);
    ActionMap afMap = find.getActionMap();

    ifMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escape);
    afMap.put(escape, new AbstractAction() {

       @Override
       public void actionPerformed(ActionEvent arg0) {
          dispose();
       }
    });


    btFind.addActionListener(this);
    btExit.addActionListener(this);

    textPane.setEditorKit(new NumberedEditorKit());
    textPane.setEditable(false);

    // 생성한 Textpane의 스타일을가져와서 스타일문서생성
    StyledDocument doc = textPane.getStyledDocument();

    // Style 생성 TextColor 빨강 녹색 생성 다른 인수도가능
    Style c = doc.addStyle("Highlight", null);
    StyleConstants.setForeground(c, Color.RED);
    StyleConstants.setBackground(c, Color.YELLOW);
    StyleConstants.setBold(c, true);
    StyleConstants.setFontSize(c, 14);

    c = doc.addStyle("Default", c);
    StyleConstants.setForeground(c, Color.DARK_GRAY);
    StyleConstants.setBackground(c, Color.WHITE);
    StyleConstants.setBold(c, false);
    StyleConstants.setFontSize(c, 12);

    setTitle(filePath.toString());
    getContentPane().add(textPane);

    System.out.println("File Open : " + filePath);

    FileReader fr = null;
    StringBuffer strbuf = null;
    BufferedReader br = null;

    // Read and display the file contents. Since we're reading text, we
    // use a FileReader instead of a FileInputStream.
    try {
      fr = new FileReader(new File(filePath)); // And a char stream to read it
      br = new BufferedReader(new FileReader(new File(filePath)));

      int cnt = 0;
      int HighlightPos = 0;
      int HighlightLength = 0;

      strbuf = new StringBuffer();

      while (true)
      {
        String readLineData = br.readLine();
        if(readLineData == null)
          break;

        cnt++;
        if(cnt == line)
        {
          doc.insertString(doc.getLength(), new String(strbuf), doc.getStyle("Default"));
          HighlightPos = doc.getLength();
          HighlightLength = readLineData.length();
          doc.insertString(doc.getLength(), readLineData + ENDL, doc.getStyle("Highlight"));
          strbuf.delete(0, strbuf.length());
        }
        else
        {
          strbuf.append(new String(readLineData) + ENDL);
        }
      }
      doc.insertString(doc.getLength(), new String(strbuf), doc.getStyle("Default"));
/*
      while ((readLineData = r.readLine()) != null)
      {
        cnt++;
        if(cnt == line)
        {
          HighlightPos = doc.getLength();
          HighlightLength = readLineData.length();
          doc.insertString(doc.getLength(), readLineData + ENDL, doc.getStyle("Highlight"));
        }
        else
        {
          doc.insertString(doc.getLength(), readLineData + ENDL, doc.getStyle("Default"));
        }
      }
*/
      br.close();
      fr.close();

      textPane.select(HighlightPos,HighlightPos + HighlightLength); //toffset 부터 문자열 선택
      textPane.setCaretPosition(HighlightPos);
      textPane.requestFocus();

    }
    // Display messages if something goes wrong
    catch (Exception e) {
      textPane.setText(e.getClass().getName() + ": " + e.getMessage());
    }
    // Always be sure to close the input stream!
    finally {
      try {
        if (br != null)
          br.close();

        if (fr != null)
          fr.close();
      } catch (IOException e) {
      }

      strbuf = null;
      br = null;
      fr = null;
    }

    JScrollPane scrollingArea = new JScrollPane(textPane);
    getContentPane().add(scrollingArea, BorderLayout.CENTER);

    find.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        word = find.getText().trim();
        int offset = searcher.search(word);
        if (offset != -1) {
          try {
            textPane.scrollRectToVisible(textPane.modelToView(offset));
          } catch (BadLocationException e) {
          }
        }
      }
    });

    textPane.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent evt) {
//        searcher.search(word);
      }

      public void removeUpdate(DocumentEvent evt) {
//        searcher.search(word);
      }

      public void changedUpdate(DocumentEvent evt) {
      }
    });

    word = find.getText().trim();

    searcher.search(word);

    this.setSize(800,600);

    // Determine the new location of the window
    int w = this.getSize().width;
    int h = this.getSize().height;
    this.setLocation(pos.x + size.width - w, pos.y + size.height - h);

    this.setModal(true);
    this.setVisible(true);
  }

  public void actionPerformed(ActionEvent e){
    if(e.getSource()==btFind)
    {
      word = find.getText().trim();
      searcher.search(word);

      toffset = textPane.getCaretPosition();
      String tempfind = find.getText().toUpperCase();
      textPane.selectAll();
      String tempString = textPane.getSelectedText().toUpperCase();

      // 문자열들의 길이
      int tfGetLength = tempfind.length();
      int taGetLength = tempString.length();

      for(;toffset < taGetLength;toffset++){
        // 문자 비교
        if(tempString.regionMatches(toffset, tempfind, 0,tfGetLength))
          break;
      }

      if(toffset + tfGetLength >  taGetLength)
      {
        toffset = 0;
        for(;toffset < taGetLength;toffset++){
          // 문자 비교
          if(tempString.regionMatches(toffset, tempfind, 0,tfGetLength))
            break;
        }
      }

      if(toffset + tfGetLength >  taGetLength)
        return;

      textPane.setCaretPosition(toffset + tfGetLength);
      textPane.select(toffset,toffset + tfGetLength); //toffset 부터 문자열 선택
      textPane.requestFocus();
    }
    else if(e.getSource()==btExit)
    {
      dispose();
    }

  }
}

class NumberedEditorKit extends StyledEditorKit {
    public ViewFactory getViewFactory() {
        return new NumberedViewFactory();
    }
}

class NumberedViewFactory implements ViewFactory {
    public View create(Element elem) {
        String kind = elem.getName();
        if (kind != null)
            if (kind.equals(AbstractDocument.ContentElementName)) {
                return new LabelView(elem);
            }
            else if (kind.equals(AbstractDocument.
                             ParagraphElementName)) {
//              return new ParagraphView(elem);
                return new NumberedParagraphView(elem);
            }
            else if (kind.equals(AbstractDocument.
                     SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);
            }
            else if (kind.equals(StyleConstants.
                     ComponentElementName)) {
                return new ComponentView(elem);
            }
            else if (kind.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            }
        // default to text display
        return new LabelView(elem);
    }
}

class NumberedParagraphView extends ParagraphView {
    public short NUMBERS_WIDTH = 35;

    public NumberedParagraphView(Element e) {
        super(e);
        short top = 0;
        short left = 0;
        short bottom = 0;
        short right = 0;
        this.setInsets(top, left, bottom, right);
    }

    protected void setInsets(short top, short left, short bottom,
                             short right) {super.setInsets
                             (top,(short)(left+NUMBERS_WIDTH),
                             bottom,right);
    }

    public void paintChild(Graphics g, Rectangle r, int n) {
        super.paintChild(g, r, n);
        int previousLineCount = getPreviousLineCount();
        int numberX = r.x - getLeftInset();
        int numberY = r.y + r.height - 5;
        g.drawString(Integer.toString(previousLineCount + n + 1),
                                      numberX, numberY);
    }

    public int getPreviousLineCount() {
        int lineCount = 0;
        View parent = this.getParent();
        int count = parent.getViewCount();
        for (int i = 0; i < count; i++) {
            if (parent.getView(i) == this) {
                break;
            }
            else {
                lineCount += parent.getView(i).getViewCount();
            }
        }
        return lineCount;
    }
  }

class UnderlineHighlighter extends DefaultHighlighter {
  public UnderlineHighlighter(Color c) {
    painter = (c == null ? sharedPainter : new UnderlineHighlightPainter(c));
  }

  // Convenience method to add a highlight with
  // the default painter.
  public Object addHighlight(int p0, int p1) throws BadLocationException {
    return addHighlight(p0, p1, painter);
  }

  public void setDrawsLayeredHighlights(boolean newValue) {
    // Illegal if false - we only support layered highlights
    if (newValue == false) {
      throw new IllegalArgumentException(
          "UnderlineHighlighter only draws layered highlights");
    }
    super.setDrawsLayeredHighlights(true);
  }

  // Painter for underlined highlights
  public static class UnderlineHighlightPainter extends
      LayeredHighlighter.LayerPainter {
    public UnderlineHighlightPainter(Color c) {
      color = c;
    }

    public void paint(Graphics g, int offs0, int offs1, Shape bounds,
        JTextComponent c) {
      // Do nothing: this method will never be called
    }

    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
        JTextComponent c, View view) {
      g.setColor(color == null ? c.getSelectionColor() : color);

      Rectangle alloc = null;
      if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
        if (bounds instanceof Rectangle) {
          alloc = (Rectangle) bounds;
        } else {
          alloc = bounds.getBounds();
        }
      } else {
        try {
          Shape shape = view.modelToView(offs0,
              Position.Bias.Forward, offs1,
              Position.Bias.Backward, bounds);
          alloc = (shape instanceof Rectangle) ? (Rectangle) shape
              : shape.getBounds();
        } catch (BadLocationException e) {
          return null;
        }
      }

      FontMetrics fm = c.getFontMetrics(c.getFont());
      int baseline = alloc.y + alloc.height - fm.getDescent() + 1;
      g.drawLine(alloc.x, baseline, alloc.x + alloc.width, baseline);
      g.drawLine(alloc.x, baseline + 1, alloc.x + alloc.width, baseline + 1);

      return alloc;
    }

    protected Color color; // The color for the underline
  }

  // Shared painter used for default highlighting
  protected static final Highlighter.HighlightPainter sharedPainter = new UnderlineHighlightPainter(
      null);

  // Painter used for this highlighter
  protected Highlighter.HighlightPainter painter;
}

// A simple class that searches for a word in
// a document and highlights occurrences of that word

class WordSearcher {
  public WordSearcher(JTextComponent comp) {
    this.comp = comp;
    this.painter = new UnderlineHighlighter.UnderlineHighlightPainter(
        Color.red);
  }

  // Search for a word and return the offset of the
  // first occurrence. Highlights are added for all
  // occurrences found.
  public int search(String word) {
    int firstOffset = -1;
    Highlighter highlighter = comp.getHighlighter();

    // Remove any existing highlights for last word
    Highlighter.Highlight[] highlights = highlighter.getHighlights();
    for (int i = 0; i < highlights.length; i++) {
      Highlighter.Highlight h = highlights[i];
      if (h.getPainter() instanceof UnderlineHighlighter.UnderlineHighlightPainter) {
        highlighter.removeHighlight(h);
      }
    }

    if (word == null || word.equals("")) {
      return -1;
    }

    // Look for the word we are given - insensitive search
    String content = null;
    try {
      Document d = comp.getDocument();
      content = d.getText(0, d.getLength()).toLowerCase();
    } catch (BadLocationException e) {
      // Cannot happen
      return -1;
    }

    word = word.toLowerCase();
    int lastIndex = 0;
    int wordSize = word.length();

    while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
      int endIndex = lastIndex + wordSize;
      try {
        highlighter.addHighlight(lastIndex, endIndex, painter);
      } catch (BadLocationException e) {
        // Nothing to do
      }
      if (firstOffset == -1) {
        firstOffset = lastIndex;
      }
      lastIndex = endIndex;
    }

    return firstOffset;
  }

  protected JTextComponent comp;

  protected Highlighter.HighlightPainter painter;

}

class ColorRenderer extends JLabel implements TableCellRenderer
{
//  private String columnName;
  public ColorRenderer(String column)
  {
//    this.columnName = column;
    setOpaque(true);
  }

  public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column)
  {
//    Object columnValue=table.getValueAt(row,table.getColumnModel().getColumnIndex(columnName));

    if (value != null) setText(value.toString());

    if(isSelected)
    {
      setBackground(table.getSelectionBackground());
      setForeground(table.getSelectionForeground());
    }
    else
    {
      if(row%2==0){
        setBackground(new Color(255,255,160));
        setForeground(table.getForeground());
      }
      else
      {
        setBackground(table.getBackground());
        setForeground(table.getForeground());
      }
/*
      if (columnValue.equals("1")) setBackground(java.awt.Color.pink);
      if (columnValue.equals("2")) setBackground(java.awt.Color.green);
      if (columnValue.equals("3")) setBackground(java.awt.Color.red);
      if (columnValue.equals("4")) setBackground(java.awt.Color.blue);
*/
    }

    return this;
  }
}

class SearchReport extends JFrame implements MouseListener{

  private static final int LIST_FORM_CSEARCH   = 0;
  private static final int LIST_FORM_CSCOPE    = 1;
  private static final int LIST_FORM_FILELIST  = 2;

  private static final int COL_FILE_NAME = 0;
  private static final int COL_FILE_EXT  = 1;
  private static final int COL_FILE_LINE = 2;
  private static final int COL_FULL_PATH = 3;
  private static final int COL_FILE_CODE = 4;

  public static String FSEPARATOR = System.getProperty("file.separator");
  public static String ENDL       = System.getProperty("line.separator");

  final String[] columnNames = {"File Name", //COL_FILE_NAME
                                "Ext",       //COL_FILE_EXT
                                "Line",      //COL_FILE_LINE
                                "Full Path", //COL_FULL_PATH
                                "Code"};     //COL_FILE_CODE

  String data[][] = {};
  String Searchpattern = null;

  final Class[] classes = new Class[]{String.class, String.class, Integer.class, String.class, String.class};
  DefaultTableModel model = new DefaultTableModel(data, columnNames){
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex < classes.length)
            return classes[columnIndex];
        return super.getColumnClass(columnIndex);
    }

//    @Override
//    public String getColumnName(int column) {
//        return getColumnClass(column).getSimpleName();
//    }
  };

  final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
  //ArrayList <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();

  JTable SearchResultsTable = new JTable(model){
    public boolean isCellEditable(int rowIndex, int colIndex) {
        return false; //Disallow the editing of any cell
      }
  };

  ListSelectionModel listSelectionModel;
  JTextArea listInfoTxt;
  JTextPane contextWindowTxt;
  StyledDocument doc;
  JTextField ftfCode, ftfFileName, ftfFullPath; //Filter Text Field
  Timer tableSetTimer;

  private JCheckBox CaseSensitive,AutoRefresh;
  private JCheckBox fcbCode, fcbFileName, fcbFullPath; //Filter Check Box
//  private JCheckBox ViewContextWindows;
  private boolean setValueIsAdjusting = false;

  JInternalFrame fr = new JInternalFrame("SearchReport", true, true, true, true);
  String filter_remove_key = "ReMoVeRoWkEy";
  String filter_remove     = "^((?!ReMoVeRoWkEy).)*$";
  public static String dbrootpath = "";
  public static String prefixPath = "";

  public void runRowFilter()
  {
      try {
        setValueIsAdjusting = true;

        String filter_code = "";
        String filter_fname = "";
        String filter_fpath = "";

        if(fcbCode.isSelected())
          filter_code = ftfCode.getText();
        if(fcbFileName.isSelected())
          filter_fname = ftfFileName.getText();
        if(fcbFullPath.isSelected())
          filter_fpath = ftfFullPath.getText();

        String regex = filter_code;
        if((filter_code != "") && (CaseSensitive.isSelected() == false))
        {
          regex = "(?i)" + filter_code;
        }

        List<RowFilter<Object,Object>> rfs = new ArrayList<RowFilter<Object,Object>>(3);
        rfs.add(RowFilter.regexFilter(regex,         COL_FILE_CODE));
        rfs.add(RowFilter.regexFilter(filter_fname,  COL_FILE_NAME));
        rfs.add(RowFilter.regexFilter(filter_remove, COL_FILE_NAME));
        rfs.add(RowFilter.regexFilter(filter_fpath,  COL_FULL_PATH));
        RowFilter<Object,Object> af = RowFilter.andFilter(rfs);
        sorter.setRowFilter(af);

        rfs.clear();
        rfs = null;

        setValueIsAdjusting = false;
      } catch (Exception e) {
        e.printStackTrace();
//        JOptionPane.showMessageDialog(null,e);
      }
  }

  public void runRowInvert()
  {
    //listSelectionModel.setValueIsAdjusting(true);
    setValueIsAdjusting = true;
    int[] rows = SearchResultsTable.getSelectedRows();
    SearchResultsTable.selectAll();
    for (int prevSel : rows) {
      SearchResultsTable.removeRowSelectionInterval(prevSel, prevSel);
    }
    //listSelectionModel.setValueIsAdjusting(false);
    setValueIsAdjusting = false;
  }

  public SearchReport(String type, String pattern, BufferedReader buffer, Dimension size, Point pos, int ListFormat, String PreFixPath, String DBRootPath)
  {
    String[] array={};
    String FName, FPath, FExt;

    dbrootpath = DBRootPath;
    prefixPath = PreFixPath;

    addWindowListener( new WindowAdapter() {

      public void windowOpened( WindowEvent e ){
        if(EzSearchClient.IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }

      @Override
      public void windowClosing(WindowEvent e)
      {
          dispose();
          Runtime.getRuntime().gc();
          //System.gc();
      }

/*
      public void windowIconified( WindowEvent e ){
        pattern.setText(paste().toString());
      }

      public void windowDeiconified( WindowEvent e ){
        pattern.setText(paste().toString());
      }
*/
      public void windowActivated( WindowEvent e ){
        if(EzSearchClient.IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }

      public void windowDeactivated( WindowEvent e ){
        if(EzSearchClient.IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }
    });

    Searchpattern = pattern;

    setTitle(type.toString() + Searchpattern.toString());

    getContentPane().add(SearchResultsTable);

    ColorRenderer cr=new ColorRenderer("A");

    long start = System.currentTimeMillis();
    setValueIsAdjusting = true;
    try {
      //System.out.println("Received Report : " + pattern);
      if(ListFormat == LIST_FORM_FILELIST) // File List
      {
        while (true)
        {
          String readLineData = buffer.readLine();
          if(readLineData == null)
            break;

          if(readLineData.length() > 1)
          {
            FPath = readLineData;
//            FPath = dbrootpath + readLineData.substring(1, readLineData.length());
            //FPath = readLineData.replace(prefixPath, dbrootpath);
            FName = getFileName(FPath);
            FExt  = getExtension(FName);
            // COL_FILE_NAME : COL_FILE_EXT : COL_FILE_LINE : COL_FULL_PATH : COL_FILE_CODE
//            model.insertRow(0, new Object[]{FName, ext, "0", FPath, ""});
            model.addRow(new Object[]{FName, FExt, 0, FPath, ""});
          }
        }
      }
      else if(ListFormat == LIST_FORM_CSEARCH)
      {
        while (true)
        {
          String readLineData = buffer.readLine();
          if(readLineData == null)
            break;

          array = readLineData.split(":");
          //        array[0]; //Full Path
          //        array[1]; //File Line
          //        array[2]; //Detail code

          int i, index = 0;
          for(i=0;i<2;i++)
            index = readLineData.indexOf(":", index+1);
          if(index < 0)
          {
            continue;
          }

          if(array[0].charAt(0) == '/')
          {
            // File Path : Line : Code
            FPath = array[0];
            //FPath = array[0].replace(prefixPath, dbrootpath);
            FName = getFileName(FPath);
            FExt  = getExtension(FName);

            // COL_FILE_NAME : COL_FILE_EXT : COL_FILE_LINE : COL_FULL_PATH : COL_FILE_CODE
            model.insertRow(0, new Object[]{FName, FExt, Integer.parseInt(array[1]), FPath, array[2]});
          }
          else
          {
            // Driver : File Path : Line : Code
            FPath = array[0] + ":" + array[1];
            //FPath = array[0].replace(prefixPath, dbrootpath);
            FName = getFileName(FPath);
            FExt  = getExtension(FName);

            // COL_FILE_NAME : COL_FILE_EXT : COL_FILE_LINE : COL_FULL_PATH : COL_FILE_CODE
            model.insertRow(0, new Object[]{FName, FExt, Integer.parseInt(array[2]), FPath, array[3]});
          }
        }
      }
      else if(ListFormat == LIST_FORM_CSCOPE)
      {
        while (true)
        {
          String readLineData = buffer.readLine();
          if(readLineData == null)
            break;

          array = readLineData.split(" ");
          //        array[0]; //Full Path
          //        array[1]; //Pattern
          //        array[2]; //File Line
          //        array[3]; //Detail code

          FPath = array[0];
          FName = getFileName(FPath);
          FExt  = getExtension(FName);

          int index = 0;
          for(int i=0;i<3;i++)
            index = readLineData.indexOf(" ", index+1);

          if(index < 0)
          {
            System.out.println("SearchResults Err");
            continue;
          }

          String FCode = readLineData.substring(index);

          // COL_FILE_NAME : COL_FILE_EXT : COL_FILE_LINE : COL_FULL_PATH : COL_FILE_CODE
          model.insertRow(0, new Object[]{FName, FExt, Integer.parseInt(array[2]), FPath, FCode});
        }
      }
      buffer.close();

      SearchResultsTable.getColumn(SearchResultsTable.getColumnName(COL_FILE_NAME)).setCellRenderer(cr);
      SearchResultsTable.getColumn(SearchResultsTable.getColumnName(COL_FILE_EXT)).setCellRenderer(cr);
      SearchResultsTable.getColumn(SearchResultsTable.getColumnName(COL_FILE_LINE)).setCellRenderer(cr);
      SearchResultsTable.getColumn(SearchResultsTable.getColumnName(COL_FULL_PATH)).setCellRenderer(cr);
      SearchResultsTable.getColumn(SearchResultsTable.getColumnName(COL_FILE_CODE)).setCellRenderer(cr);
    } catch (Exception e) { // 에러 처리
      System.out.println("SearchReport Exception : " + e);
      e.printStackTrace();
    }
    //listSelectionModel.setValueIsAdjusting(false);
    setValueIsAdjusting = false;
//    long end = System.currentTimeMillis();
//    System.out.println(pattern + " [ " + SearchResultsTable.getRowCount() + " ], [ " + ( end - start )/1000.0 + " Sec ]");

    if(model.getRowCount() > 0)
    {
      setDesign(size, pos, ListFormat);
    }
    else
    {
      JOptionPane.showMessageDialog(this, "검색 결과가 없습니다", "Message", JOptionPane.WARNING_MESSAGE);
    }
  }

  private void setDesign(Dimension size, Point pos, int ListFormat)
  {
    // Disable auto resizing
    SearchResultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    // Set the first visible column to 800 pixels wide
    if(ListFormat == LIST_FORM_FILELIST)
    {
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_NAME).setPreferredWidth(150); //File Name
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_NAME).setMaxWidth(300);
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_EXT).setPreferredWidth(10); //File Extension
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_EXT).setMaxWidth(100);
      SearchResultsTable.getColumnModel().getColumn(COL_FULL_PATH).setPreferredWidth(620); //Full Path

      SearchResultsTable.getColumnModel().getColumn(COL_FILE_LINE).setMinWidth(0);
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_LINE).setMaxWidth(0);
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_CODE).setMinWidth(0);
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_CODE).setMaxWidth(0);
    }
    else
    {
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_NAME).setPreferredWidth(150); //File Name
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_NAME).setMaxWidth(300);
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_EXT).setPreferredWidth(10);  //File Extension
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_EXT).setMaxWidth(100);
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_LINE).setPreferredWidth(50);  //Line
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_LINE).setMaxWidth(50);
      SearchResultsTable.getColumnModel().getColumn(COL_FULL_PATH).setPreferredWidth(150); //Full Path
      SearchResultsTable.getColumnModel().getColumn(COL_FILE_CODE).setPreferredWidth(420); //Code
    }

    SearchResultsTable.setFillsViewportHeight(true);
    SearchResultsTable.setAutoCreateRowSorter(true);

    SearchResultsTable.addMouseListener(this);
    addMouseListener(this);

    listSelectionModel = SearchResultsTable.getSelectionModel();
    //listSelectionModel.setValueIsAdjusting(true);

    listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());
    SearchResultsTable.setSelectionModel(listSelectionModel);
    SearchResultsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

//    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
//    rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
//    SearchResultsTable.getColumnModel().getColumn(2).setCellRenderer( rightRenderer );

    //Build output area.
    listInfoTxt = new JTextArea(2, 72);
    listInfoTxt.setEditable(false);

//    contextWindowTxt = new JTextArea(5, 100);
    contextWindowTxt = new JTextPane();
    contextWindowTxt.setEditable(false);
    contextWindowTxt.setContentType("text/html");
    contextWindowTxt.setEditorKit(new NumberedEditorKit());
//    contextWindowTxt.setPreferredSize(new Dimension(1000, 250));
//    contextWindowTxt.setMinimumSize(new Dimension(10, 10));

    // 생성한 Textpane의 스타일을가져와서 스타일문서생성
    doc = contextWindowTxt.getStyledDocument();

    // Style 생성 TextColor 빨강 녹색 생성 다른 인수도가능
    Style c = doc.addStyle("Highlight", null);
    StyleConstants.setForeground(c, Color.RED);
    StyleConstants.setBackground(c, Color.YELLOW);
    StyleConstants.setBold(c, true);
    StyleConstants.setFontSize(c, 14);

    c = doc.addStyle("Default", c);
    StyleConstants.setForeground(c, Color.DARK_GRAY);
    StyleConstants.setBackground(c, Color.WHITE);
    StyleConstants.setBold(c, false);
    StyleConstants.setFontSize(c, 12);

    JScrollPane scrollingArea = new JScrollPane(SearchResultsTable);
    getContentPane().add(scrollingArea, BorderLayout.CENTER);
    scrollingArea.setPreferredSize(new Dimension(250, 160));
    scrollingArea.setMinimumSize(new Dimension(10, 10));

    SearchResultsTable.setRowSorter(sorter);
    SearchResultsTable.setBackground(new Color(255,255,204));

//    if(ListFormat)
//    {
//      sortKeys.add(new RowSorter.SortKey(COL_FILE_NAME, SortOrder.ASCENDING));
//      sortKeys.add(new RowSorter.SortKey(COL_FULL_PATH, SortOrder.ASCENDING));
//    }
//    else
//    {
//      sortKeys.add(new RowSorter.SortKey(COL_FILE_CODE, SortOrder.ASCENDING));
//      sortKeys.add(new RowSorter.SortKey(COL_FULL_PATH, SortOrder.ASCENDING));
//      sortKeys.add(new RowSorter.SortKey(COL_FILE_NAME, SortOrder.ASCENDING));
//      sortKeys.add(new RowSorter.SortKey(COL_FILE_LINE, SortOrder.ASCENDING));
//    }
//    sorter.setSortKeys(sortKeys);

    JPanel menuPanel = new JPanel(new BorderLayout());

    //Filter
    JPanel FilterPanel = new JPanel();
    FilterPanel.setLayout(new BoxLayout(FilterPanel,BoxLayout.Y_AXIS));
    menuPanel.add(FilterPanel, BorderLayout.WEST);

    //Option
    JPanel OptionPanel = new JPanel();
    OptionPanel.setLayout(new BoxLayout(OptionPanel,BoxLayout.Y_AXIS));
    menuPanel.add(OptionPanel, BorderLayout.CENTER);

    //Button
    JPanel buttonPanel = new JPanel(new BorderLayout());
    menuPanel.add(buttonPanel, BorderLayout.EAST);

    JButton btFilter = new JButton(" Filter ");
    btFilter.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        runRowFilter();
      }
    });
    buttonPanel.add(btFilter, BorderLayout.NORTH);

    JButton btInvert = new JButton(" Invert ");
    btInvert.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        runRowInvert();
      }
    });
    buttonPanel.add(btInvert, BorderLayout.CENTER);

    JButton btExit = new JButton(" Exit ");
    btExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    buttonPanel.add(btExit, BorderLayout.SOUTH);

    CaseSensitive = new JCheckBox("Case Sensitive",false);
//    CaseSensitive.setAlignmentX(Component.RIGHT_ALIGNMENT);
    CaseSensitive.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(AutoRefresh.isSelected())
          runRowFilter();
      }
    });
    OptionPanel.add(CaseSensitive);

    AutoRefresh = new JCheckBox("Auto refresh",true);
    AutoRefresh.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(AutoRefresh.isSelected())
          runRowFilter();
      }
    });
    OptionPanel.add(AutoRefresh);

    fcbCode     = new JCheckBox("Code      ",false);
    fcbFullPath = new JCheckBox("Full Path  ",false);
    fcbFileName = new JCheckBox("File Name",false);

    //Filter
    JPanel Opt0Panel = new JPanel();
    Opt0Panel.setLayout(new BoxLayout(Opt0Panel,BoxLayout.X_AXIS));


//    CaseSensitive.setAlignmentX(Component.RIGHT_ALIGNMENT);
    fcbCode.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//          if(AutoRefresh.isSelected() && fcbCode.isSelected())
        if(AutoRefresh.isSelected())
          runRowFilter();
      }
    });

    ftfCode = new JTextField(34);
    ftfCode.setText(Searchpattern.toString());
    ftfCode.setCaretPosition(ftfCode.getText().length());

    ftfCode.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//          if(AutoRefresh.isSelected() && fcbCode.isSelected())
        if(AutoRefresh.isSelected())
          runRowFilter();
      }
    });

    // Listen for changes in the text
    ftfCode.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        chkRefresh();
      }
      public void removeUpdate(DocumentEvent e) {
        chkRefresh();
      }
      public void insertUpdate(DocumentEvent e) {
        chkRefresh();
      }

      public void chkRefresh() {
//          if(AutoRefresh.isSelected() && fcbCode.isSelected())
        if(AutoRefresh.isSelected())
        {
            runRowFilter();
        }
      }
    });

    if(ListFormat != LIST_FORM_FILELIST)
    {
      FilterPanel.add(Opt0Panel);
      Opt0Panel.add(fcbCode);
      Opt0Panel.add(ftfCode, BorderLayout.CENTER);
      fcbCode.setSelected(true);
    }

    JPanel Opt1Panel = new JPanel();
    Opt1Panel.setLayout(new BoxLayout(Opt1Panel,BoxLayout.X_AXIS));
    FilterPanel.add(Opt1Panel);


//    CaseSensitive.setAlignmentX(Component.RIGHT_ALIGNMENT);
    fcbFileName.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//        if(AutoRefresh.isSelected() && fcbFileName.isSelected())
        if(AutoRefresh.isSelected())
          runRowFilter();
      }
    });
    Opt1Panel.add(fcbFileName);

    ftfFileName = new JTextField(34);
    ftfFileName.setCaretPosition(ftfFileName.getText().length());

    ftfFileName.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//        if(AutoRefresh.isSelected() && fcbFileName.isSelected())
        if(AutoRefresh.isSelected())
          runRowFilter();
      }
    });

    // Listen for changes in the text
    ftfFileName.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        chkRefresh();
      }
      public void removeUpdate(DocumentEvent e) {
        chkRefresh();
      }
      public void insertUpdate(DocumentEvent e) {
        chkRefresh();
      }

      public void chkRefresh() {
//        if(AutoRefresh.isSelected() && fcbFileName.isSelected())
        if(AutoRefresh.isSelected())
        {
            runRowFilter();
        }
      }
    });
    Opt1Panel.add(ftfFileName, BorderLayout.CENTER);

    if(ListFormat == LIST_FORM_FILELIST)
    {
      fcbFileName.setSelected(true);
      ftfFileName.setText(Searchpattern.toString());
      ftfFileName.setCaretPosition(ftfFileName.getText().length());
    }

    JPanel Opt2Panel = new JPanel();
    Opt2Panel.setLayout(new BoxLayout(Opt2Panel,BoxLayout.X_AXIS));
    FilterPanel.add(Opt2Panel);

//    CaseSensitive.setAlignmentX(Component.RIGHT_ALIGNMENT);
    fcbFullPath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//        if(AutoRefresh.isSelected() && fcbFullPath.isSelected())
        if(AutoRefresh.isSelected())
          runRowFilter();
      }
    });
    Opt2Panel.add(fcbFullPath);

    ftfFullPath = new JTextField(34);
    ftfFullPath.setCaretPosition(ftfFullPath.getText().length());

    ftfFullPath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//        if(AutoRefresh.isSelected() && fcbFullPath.isSelected())
        if(AutoRefresh.isSelected())
          runRowFilter();
      }
    });

    // Listen for changes in the text
    ftfFullPath.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        chkRefresh();
      }
      public void removeUpdate(DocumentEvent e) {
        chkRefresh();
      }
      public void insertUpdate(DocumentEvent e) {
        chkRefresh();
      }

      public void chkRefresh() {
//        if(AutoRefresh.isSelected() && fcbFullPath.isSelected())
        if(AutoRefresh.isSelected())
        {
            runRowFilter();
        }
      }
    });
    Opt2Panel.add(ftfFullPath, BorderLayout.CENTER);

    getContentPane().add(menuPanel, BorderLayout.NORTH);

    JPanel contextWindowPanel = new JPanel(new BorderLayout());
    contextWindowPanel.setBackground(Color.orange);
    listInfoTxt.setBackground(Color.orange);
    listInfoTxt.setForeground(Color.blue);

//    Font font = new Font("Verdana", Font.BOLD, 14);
//    Font font = new Font("Fixedsys", Font.BOLD, 14);
//    listInfoTxt.setFont(font);

//Put the editor pane in a scroll pane.
    JScrollPane editorScrollPane = new JScrollPane(contextWindowTxt);
    editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    editorScrollPane.setPreferredSize(new Dimension(250, 250));
    editorScrollPane.setMinimumSize(new Dimension(10, 10));

//    contextWindowPanel.add(editorScrollPane, BorderLayout.SOUTH);
    contextWindowPanel.add(editorScrollPane, BorderLayout.NORTH);
    contextWindowPanel.add(listInfoTxt, BorderLayout.SOUTH);
    getContentPane().add(contextWindowPanel, BorderLayout.SOUTH);

    int condition = JComponent.WHEN_FOCUSED;
    InputMap iftMap = ftfCode.getInputMap(condition);
    ActionMap aftMap = ftfCode.getActionMap();
    String escape = "escape";

    iftMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escape);
    aftMap.put(escape, new AbstractAction() {

       @Override
       public void actionPerformed(ActionEvent arg0) {
          dispose();
       }
    });

    InputMap ilstMap = SearchResultsTable.getInputMap(condition);
    ActionMap alstMap = SearchResultsTable.getActionMap();

    ilstMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escape);
    alstMap.put(escape, new AbstractAction() {

       @Override
       public void actionPerformed(ActionEvent arg0) {
          dispose();
       }
    });

    InputMap icwtMap = contextWindowTxt.getInputMap(condition);
    ActionMap acwtMap = contextWindowTxt.getActionMap();

    icwtMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escape);
    acwtMap.put(escape, new AbstractAction() {

       @Override
       public void actionPerformed(ActionEvent arg0) {
          dispose();
       }
    });

    InputMap iliMap = listInfoTxt.getInputMap(condition);
    ActionMap aliMap = listInfoTxt.getActionMap();

    iliMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escape);
    aliMap.put(escape, new AbstractAction() {

       @Override
       public void actionPerformed(ActionEvent arg0) {
          dispose();
       }
    });

    this.setSize(800,500);

    // Determine the new location of the window
    int w = this.getSize().width;
    int h = this.getSize().height;
    this.setLocation(pos.x + size.width - w, pos.y + size.height - h);

//    this.setModal(true);
    this.pack();
    this.setVisible(true);


    TableSetTimerTask tableInit = new TableSetTimerTask();

    tableSetTimer = new Timer(); // 타이머 생성
    tableSetTimer.scheduleAtFixedRate(tableInit, new Date(), 100 );  // 현재시간부터 500ms마다 timerTask 실행
  }

    public class TableSetTimerTask extends TimerTask {
      public void run() {
//SearchResultsTable
        tableSetTimer.cancel();  //타이머 중지
        ListSelectionModel selectionModel =  SearchResultsTable.getSelectionModel();
        selectionModel.setSelectionInterval(0, 0);
      }
  }

  class SharedListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();

            if (lsm.isSelectionEmpty()) {
              listInfoTxt.setText("");
              listInfoTxt.append(" <none> ");
            } else if(e.getValueIsAdjusting() == false){
                if(setValueIsAdjusting == true) return;

                // Find out which indexes are selected.
                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
//                for (int i = minIndex; i <= maxIndex; i++) {
                    int i = lsm.getAnchorSelectionIndex();
                    if (lsm.isSelectedIndex(i)) {
                      int rdx = SearchResultsTable.convertRowIndexToModel(i);
                      int total = SearchResultsTable.getRowCount();
                      String fpath = (String)model.getValueAt(rdx,COL_FULL_PATH);

                      if(prefixPath != "")
                        fpath = fpath.replace(prefixPath, dbrootpath);

                      listInfoTxt.setText("");
                      listInfoTxt.append("---- "+ ftfCode.getText() + " Matches ( " + (i+1) + " / " + total + " ) ----");
                      listInfoTxt.append(ENDL);
//                      listInfoTxt.append((String)tm.getValueAt(rdx,0) + " : " + (String)tm.getValueAt(rdx,1) + ENDL); // File name + line
                      listInfoTxt.append(fpath); // Full path
//                      listInfoTxt.append(ENDL);
//                      listInfoTxt.append((String)tm.getValueAt(rdx,COL_FILE_CODE)); // Code

                      File oFile = new File(fpath);
                      if (oFile.exists()) {
                        long len = oFile.length();
                        listInfoTxt.append(" : " + len + " Byte ( " + len/1024 + " KB)");
                      } else {
                        listInfoTxt.append(" Can't Find");
                      }

                      listInfoTxt.setCaretPosition(listInfoTxt.getDocument().getLength());

                      //fullCodeText(fpath, Integer.parseInt((String)model.getValueAt(rdx,COL_FILE_LINE)));
                      fullCodeText(fpath, (int)model.getValueAt(rdx, COL_FILE_LINE));
                      return;
                    }
//                }
            }
        }
  }

  public void fullCodeText(String filePath, int line)
  {
    if ((filePath == null) || (filePath.length() == 0)) {
      contextWindowTxt.setText("");
      return;
    }

//    if(ViewContextWindows.isSelected() == false) {
//      contextWindowTxt.setText("");
//      return;
//    }

    FileReader fr = null;
    BufferedReader br = null;
    StringBuffer strbuf;

    // Read and display the file contents. Since we're reading text, we
    // use a FileReader instead of a FileInputStream.
    try {
      if(prefixPath != "")
        filePath = filePath.replace(prefixPath, dbrootpath);
      fr = new FileReader(new File(filePath)); // And a char stream to read it
      br = new BufferedReader(fr);

      int cnt = 0;
      int HighlightPos = 0;
      int HighlightLength = 0;

      strbuf = new StringBuffer();

      doc.remove(0,doc.getLength());
      while (true)
      {
        String readLineData = br.readLine();
        if(readLineData == null)
          break;
        cnt++;
        if(cnt == line)
        {
          doc.insertString(doc.getLength(), new String(strbuf), doc.getStyle("Default"));
          HighlightPos = doc.getLength();
          HighlightLength = readLineData.length();
          doc.insertString(doc.getLength(), readLineData + ENDL, doc.getStyle("Highlight"));
          strbuf.delete(0, strbuf.length());
        }
        else
        {
          strbuf.append(new String(readLineData) + ENDL);
        }
      }
      doc.insertString(doc.getLength(), new String(strbuf), doc.getStyle("Default"));

      br.close();
      fr.close();

      contextWindowTxt.select(HighlightPos,HighlightPos + HighlightLength);
      contextWindowTxt.setCaretPosition(HighlightPos);
    }
    // Display messages if something goes wrong
    catch (Exception e) {
      contextWindowTxt.setText(e.getClass().getName() + ": " + e.getMessage());
    }
    // Always be sure to close the input stream!
    finally {
      try {
        if (br != null)
          br.close();

        if (fr != null)
          fr.close();
      } catch (IOException e) {
      }
      br = null;
      fr = null;
      strbuf = null;
    }
  }


  public String getFileName(String filename)
  {
    int index = filename.lastIndexOf("/");

    if (index != -1) { //for linux
      return filename.substring(index+1);
    } else { //for window
      index = filename.lastIndexOf("\\");
      if (index != -1) {
        return filename.substring(index+1);
      }
    }
    return "";
  }

  public String getExtension(String filename)
  {
    int i = filename.lastIndexOf('.');
    if (i > 0) {
        return filename.substring(i+1);
    }
    return "";
  }

  void eventOutput(String eventDescription, MouseEvent e) {
    //        textArea.append(eventDescription + " detected on "
    //                + e.getComponent().getClass().getName()
    //                + "." + ENDL);
    //        textArea.setCaretPosition(textArea.getDocument().getLength());
//    System.out.println("Mouse event: " + eventDescription);
  }

  public void mousePressed(MouseEvent e) {
    eventOutput("Mouse pressed (# of clicks: " + e.getClickCount() + ")", e);
  }

  public void mouseReleased(MouseEvent e) {
    eventOutput("Mouse released (# of clicks: " + e.getClickCount() + ")", e);
  }

  public void mouseEntered(MouseEvent e) {
    eventOutput("Mouse entered", e);
  }

  public void mouseExited(MouseEvent e) {
    eventOutput("Mouse exited", e);
  }

  public void mouseClicked(MouseEvent e) {
    if(e.getClickCount() < 1)
      return;

    //Mouse Left
    if((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
    {
      //Double click
      if(e.getClickCount() >= 2)
      {
        try
        {
          JTable target = (JTable)e.getSource();
          int selRow, selCol;
          Object value;
          selRow = target.convertRowIndexToModel(target.getSelectedRow());
          selCol = target.convertColumnIndexToModel(target.getSelectedColumn());
          if(selRow != -1)
          {
            value = model.getValueAt(selRow,COL_FULL_PATH);
            String FullPath = (String)value;
            if(prefixPath != "")
              FullPath = FullPath.replace(prefixPath, dbrootpath);

            File file = new File(FullPath);

            if(file.exists())      // First, make sure the path exists
            {
              if(file.isFile())      // Similarly, this will tell you if it's a file
              {
                //System.err.println("File : " + FullPath);
                if(selCol == COL_FILE_CODE) //File View
                {
                  try
                  {
                    Desktop.getDesktop().open(file);
                  } catch (Exception eee) {
                    //int line = Integer.parseInt((String)(model.getValueAt(selRow,COL_FILE_LINE)));
                    int line = (int)model.getValueAt(selRow, COL_FILE_LINE);
                    new SearchFileView(Searchpattern, FullPath, line, this.getSize(), this.getLocation());
                  }
                }
                else if((selCol == COL_FULL_PATH) && (e.getClickCount() >= 3))
                {
                  Desktop.getDesktop().open(file.getParentFile());

//                  StringSelection stringSelection = new StringSelection(FullPath);
//                  Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
//                  clpbrd.setContents(stringSelection, null);
                }
                else //Open Source Insight
                {
                  String SI3Path = WinRegistry.readString (
                    WinRegistry.HKEY_LOCAL_MACHINE,                                           //HKEY
                    "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\insight3.exe",  //Key
                    "Path");                                                                  //ValueName

                  FullPath = FullPath.replace("/","\\");
                  System.err.println(SI3Path + "\\Insight3.exe" + " -i" + " +" + model.getValueAt(selRow,COL_FILE_LINE) + " " + FullPath);
                  new ProcessBuilder(SI3Path + "\\Insight3.exe", "-i", "+"+model.getValueAt(selRow,COL_FILE_LINE), FullPath).start();
      //            dispose();
                }
              }
              else if(file.isDirectory()) // This will tell you if it is a directory
              {
                Desktop.getDesktop().open(file);
              }
            }
            else
            {
              JOptionPane.showMessageDialog(null,"경로를 찾을 수 없습니다");
            }
            file = null;
          }
          else
          {
            System.err.println("선택된 셀이 없음");
          }
        } catch (Exception ee) { // 에러 처리
          JOptionPane.showMessageDialog(null,"Source Insight를 찾을 수 없습니다");
          System.err.println("에러! 외부 명령 실행에 실패했습니다.\n" + ee.getMessage());
          ee.printStackTrace();
//          System.exit(-1);
//          System.gc();
        }
      }
    }
    //Mouse Right
    else if((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
    {
      int[] rows = SearchResultsTable.getSelectedRows();
      if(rows.length==0)
        return;

      AskingQuestionDialog askMsg = new AskingQuestionDialog("선택 항목 삭제", this.getSize(), this.getLocation());
      if(askMsg.getResult() != 0) return;

      try
      {
        setValueIsAdjusting = true;
        for(int i = 0; i < rows.length; i++) {
          rows[i]=SearchResultsTable.convertRowIndexToModel(rows[i]);
        }
        Arrays.sort(rows);

        for(int i = rows.length - 1; i >= 0; i--) {
          try{
            model.setValueAt(filter_remove_key, rows[i], 0);
            model.fireTableRowsUpdated(rows[i], rows[i]);
          } catch (Exception ex) {
            //System.out.println("setValueAt Fail! : " + rows[i]);
          }
        }
        setValueIsAdjusting = false;
      } catch (Exception ee) { // 에러 처리
        System.out.println("removeRow Fail! : " + ee.getMessage());
        ee.printStackTrace();
      } finally {
        SearchResultsTable.revalidate();
        //SearchResultsTable.clearSelection();
        //SearchResultsTable.setRowSelectionInterval(0, 0);
        runRowFilter();
      }
    }
  }
}

class CustomDialog extends JDialog implements ActionListener {
  public CustomDialog(JFrame parent, Dimension size, Point pos, String title, String message) {

    super(parent, title, true);
    if (size != null && pos != null) {
      setLocation(pos.x + size.width / 4, pos.y + size.height / 4);
    }
    JPanel messagePane = new JPanel();
    messagePane.add(new JLabel(message));
    getContentPane().add(messagePane);
    JPanel buttonPane = new JPanel();
    JButton button = new JButton("Cancel");
    buttonPane.add(button);
    button.addActionListener(this);
    getContentPane().add(buttonPane, BorderLayout.SOUTH);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    pack();
    setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    setVisible(false);
    dispose();
  }
}

class AskingQuestionDialog {
  public static int result = -1;
  public AskingQuestionDialog(String msg, Dimension size, Point pos) {
    JOptionPane pane = new JOptionPane(msg);
    Object[] options = new String[] { "YES", "NO" };
    pane.setOptions(options);
    JDialog dialog = pane.createDialog(new JFrame(), "EzSearch");
    dialog.setAlwaysOnTop( true );
    dialog.setLocation(pos.x + size.width / 4, pos.y + size.height / 4);
    dialog.show();

    Object obj = pane.getValue();
    for (int k = 0; k < options.length; k++)
      if (options[k].equals(obj))
        result = k;
//    System.out.println("User's choice: " + result);
  }

  public static int getResult()
  {
    return result;
  }
}

class Help extends JDialog implements ActionListener {
  public static String ENDL = System.getProperty("line.separator");
  JTextArea helpTxt = new JTextArea();;
  JButton btExit = new JButton("          Exit          ");

  public Help(Dimension size, Point pos) {
    setTitle("Help");

    addWindowListener( new WindowAdapter() {

      public void windowOpened( WindowEvent e ){
        if(EzSearchClient.IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }

      @Override
      public void windowClosing(WindowEvent e)
      {
        dispose();
        Runtime.getRuntime().gc();
        //System.gc();
      }

/*
      public void windowIconified( WindowEvent e ){
        pattern.setText(paste().toString());
      }

      public void windowDeiconified( WindowEvent e ){
        pattern.setText(paste().toString());
      }
*/
      public void windowActivated( WindowEvent e ){
//        pattern.requestFocus();
        if(EzSearchClient.IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }

      public void windowDeactivated( WindowEvent e ){
        if(EzSearchClient.IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }
    });

    JPanel helpPanel = new JPanel();
    helpPanel.setLayout(new BoxLayout(helpPanel,BoxLayout.Y_AXIS));
    helpPanel.setBackground(Color.orange);

    helpTxt.setEditable(false);
    helpTxt.setBackground(Color.orange);
    helpTxt.setForeground(Color.blue);

    JScrollPane helpScrollPane = new JScrollPane(helpTxt);
    helpScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//    helpScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    helpScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    helpScrollPane.setPreferredSize(new Dimension(500, 500));
//    helpScrollPane.setMinimumSize(new Dimension(10, 10));

    helpPanel.add(helpScrollPane, BorderLayout.SOUTH);

    btExit.addActionListener(this);
    helpPanel.add(btExit, BorderLayout.NORTH);

    getContentPane().add(helpPanel, BorderLayout.SOUTH);

    this.setSize(500,500);

    // Determine the new location of the window
    int w = this.getSize().width;
//    int h = this.getSize().height;
//    this.setLocation(pos.x + size.width - w, pos.y + size.height - h);
    this.setLocation(pos.x-w, pos.y);

//    this.setModal(true);
    this.pack();
    this.setVisible(true);

//===========================================================================

    helpTxt.setText("");

    helpTxt.append(ENDL + " < SHELL CMD > " + ENDL);
    helpTxt.append("GREP" + ENDL);

    helpTxt.append("-i : ignore case distinctions" + ENDL);
    helpTxt.append("-w : force PATTERN to match only whole words" + ENDL);
    helpTxt.append("-v : select non-matching lines" + ENDL);
    helpTxt.append("-l : print only names of FILEs containing matches" + ENDL);
    helpTxt.append("-I : equivalent to --binary-files=without-match" + ENDL);
    helpTxt.append("-n : 줄 번호를 함께 출력한다." + ENDL);

    helpTxt.append("grep : 파일 전체를 뒤져 정규표현식에 대응하는 모든 행들을 출력한다. " + ENDL);
    helpTxt.append("egrep : grep의 확장판으로, 추가 정규표현식 메타문자들을 지원한다. " + ENDL);
    helpTxt.append("fgrep : fixed grep 이나 fast grep으로 불리며, 모든 문자를 문자 그래도 취급한다. 즉, 정규표현식의 메타문자도 일반 문자로 취급한다. " + ENDL);

    helpTxt.append("정규표현식을 사용하는 grep의 예제 " + ENDL);
    helpTxt.append("grep NW d* (d로 시작하는 모든 파일에서 NW를 포함하는 모든 행을 찾는다.) " + ENDL);
    helpTxt.append("grep '^n' [file] (n으로 시작하는 모든 행을 출력한다.) " + ENDL);
    helpTxt.append("grep '4$' [file] (4로 끝나는 모든 행을 출력한다.) " + ENDL);
    helpTxt.append("grep TB Savage [file] (TB만 인자이고 Savage와 [file]은 파일 이름이다.) " + ENDL);
    helpTxt.append("grep 'TB Savage' [file] (TB Savage를 포함하는 모든 행을 출력한다.) " + ENDL);
    helpTxt.append("grep '5.' [file] (숫자 5, 마침표, 임의의 한 문자가 순서대로 나타나는 문자열이 포함된 행을 출력한다.) " + ENDL);
    helpTxt.append("grep '.5' [file] (.5가 나오는 모든 행을 출력한다.) " + ENDL);
    helpTxt.append("grep '^[we]' [file] (w나 e로 시작하는 모든 행을 출력한다.) " + ENDL);
    helpTxt.append("grep '[^0-9]' [file] (숫자가 아닌 문자를 하나라도 포함하는 모든 행을 출력한다.) " + ENDL);
    helpTxt.append("grep '[A-Z][A-Z] [A-Z]' [file] (대문자 2개와 공백 1개, 그리고 대문자 하나가 연이어 나오는 문자열이 포함된 행을 출력한다.) " + ENDL);
    helpTxt.append("grep 'ss* ' [file] (s가 한 번 나오고, 다시 s가 0번 또는 여러번 나온 후에 공백이 연이어 등장하는 문자열을 포함한 모든 행을 출력한다.) " + ENDL);
    helpTxt.append("grep '[a-z]{9}' [file] (소문자가 9번 이상 반복되는 문자열을 포함하는 모든 행을 출력한다.) " + ENDL);
    helpTxt.append("grep '(3).[0-9].*1 *1' [file] (숫자 3,마침표,임의의 한 숫자,임의 개수의 문자,숫자 3(태그),임의 개수의 탭 문자,숫자 3의 순서를 갖는 문자열이 포한된 모든 행을 출력한다.) " + ENDL);
    helpTxt.append("grep '(north로 시작하는 단어가 포함된 모든 행을 출력한다.) " + ENDL);
    helpTxt.append("grep '' [file] (north라는 단어가 포함된 모든 행을 출력한다.) " + ENDL);
    helpTxt.append("grep '<[a-z].*n>' [file] (소문자 하나로 시작하고, 이어서 임의 개수의 여러 문자가 나오며, n으로 끝나는 단어가 포함된 모든 행을 출력한다. 여기서 .*는 공백을 포함한 임의의 문자들을 의미한다.) " + ENDL);

    helpTxt.append("grep에 옵션 사용 " + ENDL);
    helpTxt.append("grep -n '^south' [file] (행번호를 함께 출력한다.) " + ENDL);
    helpTxt.append("grep -i 'pat' [file] (대소문자를 구별하지 않게 한다.) " + ENDL);
    helpTxt.append("grep -v 'Suan Chin' [file] (문자열 Suan Chin이 포함되지 않은 모든 행을 출력하게 한다. 이 옵션은 입력 파일에서 특정 내용의 입력을 삭제하는데 쓰인다. " + ENDL);
    helpTxt.append("grep -v 'Suan Chin' [file] > black " + ENDL);
    helpTxt.append("mv black [file] " + ENDL);
    helpTxt.append("grep -l 'SE' * (패턴이 찾아진 파일의 행 번호 대신 단지 파일이름만 출력한다.) " + ENDL);
    helpTxt.append("grep -w 'north' [file] (패턴이 다른 단어의 일부가 아닌 하나의 단어가 되는 경우만 찾는다. northwest나 northeast 등의 단어가 아니라, north라는 단어가 포함된 행만 출력한다.) " + ENDL);
    helpTxt.append("grep -i \"$LOGNAME\" [file] (환경변수인 LOGNAME의 값을 가진 모든 행을 출력한다. 변수가 큰따옴표로 둘러싸여 있는 경우, 쉘은 변수의 값으로 치환한다. 작은따옴표로 둘러싸여 있으면 변수 치환이 일어나지 않고 그냥 $LOGNAME 이라는 문자로 출력된다.) " + ENDL);

    helpTxt.append("EGREP" + ENDL);
    helpTxt.append("egrep(extended grep) : grep에서 제공하지 않는 확장된 정규표현식 메타문자를 지원  한다. grep와 동일한 명령행 옵션을 지원한다. " + ENDL);
    helpTxt.append("egrep 'NW|EA' [file] (NW나 EA가 포함된 행을 출력한다.) " + ENDL);
    helpTxt.append("egrep '3+' [file] (숫자 3이 한 번 이상 등장하는 행을 출력한다.) " + ENDL);
    helpTxt.append("egrep '2.?[0-9]' [file] (숫자 2 다음에 마침표가 없거나 한 번 나오고, 다시 숫자가 오는 행을 출력한다.) " + ENDL);
    helpTxt.append("egrep ' (no)+' [file] (패턴 no가 한 번 이상 연속해서 나오는 행을 출력한다.) " + ENDL);
    helpTxt.append("egrep 'S(h|u)' [file] (문자 S 다음에 h나 u가 나오는 행을 출력한다.) " + ENDL);
    helpTxt.append("egrep 'Sh|u' [file] (패턴 Sh나 u를 포함한 행을 출력한다.) " + ENDL);

    helpTxt.append("고정 grep 과 빠른 grep " + ENDL);
    helpTxt.append("fgrep : grep 명령어와 동일하게 동작한다. 다만 정규표현식 메타문자들을 특별하게 취급하지 않는다. " + ENDL);
    helpTxt.append("fgrep '[A-Z]****[0-9]..$5.00' file ([A-Z]****[0-9]..$5.00 이 포함된 행을 출력한다. 모든 문자들을 문자 자체로만 취급한다.) " + ENDL);

    helpTxt.append(ENDL + "FIND" + ENDL);
    helpTxt.append("find . -name [pattern] : pattern형식으로 되어 있는 파일" + ENDL);
    helpTxt.append("find . -iname [pattern] : 대소문자 구분하지 않음" + ENDL);
    helpTxt.append("find -amin n : n분전에 최종 접근된 파일" + ENDL);
    helpTxt.append("find -cmin n : n 분전에 마지막으로 변경된 파일" + ENDL);

    helpTxt.append(ENDL + "WC" + ENDL);
    helpTxt.append("wc -c [file] : 전체 문자의 수를 출력" + ENDL);
    helpTxt.append("wc -l [file] : 전체 라인의 수를 출력" + ENDL);
    helpTxt.append("wc -w [file] : 전체 단어의 수를 출력" + ENDL);

    helpTxt.append(ENDL + "HEAD" + ENDL);
    helpTxt.append("head -n [file] : 앞에서부터 n만큼 라인수 출력" + ENDL);

    helpTxt.append(ENDL + "TAIL" + ENDL);
    helpTxt.append("tail -n [file] : 끝에서부터 n만큼 라인수 출력" + ENDL);
    helpTxt.append("tail +n [file] : 파일의 처음 라인수 부분부터 끝까지 출력" + ENDL);
    helpTxt.append("tail -f [file] : 파일의 끝부분에 추가되는 부분을 계속해서 표시" + ENDL);

    helpTxt.append(ENDL + "< Regex Filter >" + ENDL);
    helpTxt.append(".     : 아무 문자나 1개" + ENDL);
    helpTxt.append("*     : 바로 앞에 있는 문자가 0개 또는 그 이상" + ENDL);
    helpTxt.append("+     : 바로 앞에 있는 문자가 1개 또는 그 이상" + ENDL);
    helpTxt.append("?     : 바로 앞에 있는 문자가 0개 또는 1개" + ENDL);

    helpTxt.append("{n}   : 바로 앞에 있는 문자가 n개" + ENDL);
    helpTxt.append("{n,}  : 바로 앞에 있는 문자가 n개 이상" + ENDL);
    helpTxt.append("{n,m} : 바로 앞에 있는 문자가 n개 이상, m개 이하 " + ENDL);

    helpTxt.append("^     : 줄의 제일 처음" + ENDL);
    helpTxt.append("$     : 줄의 제일 마지막" + ENDL);
    helpTxt.append("^.*$  : 한줄 전체" + ENDL);

    helpTxt.append("[ ]   : 안에 있는 문자중 아무것 1개" + ENDL);
    helpTxt.append("[abc] : a,b,c 중 아무것이나 1개" + ENDL);
    helpTxt.append("[a-z] : 알파벳 소문자 중 아무것이나 1개" + ENDL);
    helpTxt.append("[a-zA-Z0-9_] : 알파벳, 숫자, 밑줄문자 중 아무것이나 1개" + ENDL);

    helpTxt.append("[^ ]   : 안에 있는 문자를 제외한 문자중 아무것 1개" + ENDL);
    helpTxt.append("[^0-9] : 숫자 이외의 문자 중 아무것이나 1개" + ENDL);
    helpTxt.append("[^0-9] : 숫자 이외의 문자 중 아무것이나 1개" + ENDL);
    helpTxt.append("[^a-zA-Z0-9_] : 알파벳, 숫자, 밑줄문자 이외의 문자 중 아무것이나 1개" + ENDL);

    helpTxt.append("http://www.nextree.co.kr/p4327/" + ENDL);
    helpTxt.append("http://www.ylabs.co.kr/index.php?document_srl=5950&mid=board_centos" + ENDL);

    helpTxt.append(ENDL + " 문의 사항은 hagueljh@gmail.com " + ENDL);
//===========================================================================
  }

  public void actionPerformed(ActionEvent e){
    if(e.getSource()==btExit)
    {
      dispose();
    }
  }
}

public class EzSearchClient extends Frame implements ActionListener, FocusListener
{
  public static String FSEPARATOR = System.getProperty("file.separator");
  public static String ENDL       = System.getProperty("line.separator");
  public static String WINENDL = "\r\n";
  public static String LNXENDL = "\n";

  public static String DBRootPath = "";

  private static boolean LocalMode=true;

  private static final int LIST_FORM_CSEARCH   = 0;
  private static final int LIST_FORM_CSCOPE    = 1;
  private static final int LIST_FORM_FILELIST  = 2;

  private static final int CMD_SYMBOL       = 0;
  private static final int CMD_GLOBAL       = 1;
  private static final int CMD_CALL_BY_FUNC = 2;
  private static final int CMD_CALLING_FUNC = 3;
//  private static final int CMD_TEXT_SEARCH  = 4;
//  private static final int CMD_CHANGE_STR   = 5;
//  private static final int CMD_EGREP        = 5;
  private static final int CMD_FILE_SEARCH  = 4;
  private static final int CMD_INC_SEARCH   = 5;
  private static final int CMD_ASSIGNMENT   = 6;

  private static final int CMD_CSEARCH      = 7;
  private static final int CMD_PJT_FILE     = 8;

  private static final int CMD_SEND_CMD     = 9;
  private static final int CMD_DOSCMD       = 10;
  private static final int MAX_PATTERN      = 11;

  private static final int DEFAULT_FIND = CMD_GLOBAL; //CMD_CSEARCH; //Find this global definition
  private static final String ExecuteCase[] = {"Find this C symbol: ",
                                               "Find this global definition: ",
                                               "Find functions called by this function: ",
                                               "Find functions calling this function: ",
//                                               "Find this text string: ",
//                                               "Change this text string: ",
//                                               "Find this egrep pattern: ",
                                               "Find this file: ",
                                               "Find files #including this file: ",
                                               "Find assignments to this symbol: ",

                                               "Code CSearch : ",
                                               "File CSearch : ",

                                               "Send Command : ",
                                               "Terminal ",
                                               ""
                                              };

  JButton btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btn10,btn11,btn12,btn13,btn14,btn15,btn16,btn17,btn18,btn19,btn20;
  JButton FndButton[] = {btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btn10,btn11,btn12,btn13,btn14,btn15,btn16,btn17,btn18,btn19,btn20};
  JButton helpButton = new JButton("Help");;

  static int SendArgPatternMode = 0;
  static String argPattern = "";
  static String argIPAddress = "127.0.0.1";
  static String prefixPath = "";
  static String csearchindex = "";

//  static String argIPAddress = "192.168.56.101";
  static int ServerPortNum = 1029;
  static int LocalCmd0PortNum = 7770;
  static int LocalCmd1PortNum = 7870;

  int mPatternType;

  SearchReport findReportFrame = null;
  public static boolean isAlwaysOnTop = true;

  TcpIpClient TcpIpClient = null;
  TcpIpServer TcpIpServer0 = null;
  TcpIpServer TcpIpServer1 = null;

  InputStream app_exec_input_stream = null;
  BufferedReader app_exec_buf_read = null;
  String app_exec_send_msg = null;
  InputStreamReader app_exec_stream = null;

  Label lblOut;     //라벨 만들기
  JTextField pattern_txt;
  JCheckBox CaseSensitive;
  JCheckBox AlwaysOnTop;
  TcpIpClient TcpIp = new TcpIpClient();

  public EzSearchClient(String title)
  {
    super(title);

    if(SendArgPatternMode != 0)
    {
      System.out.println("TcpIpPatternSendService:" + argPattern);
      TcpIpPatternSendService();
      //System.gc();
      System.exit(0);
      return;
    }

    this.setDesign(); //생성자에 호출
    pattern_txt.setText(argPattern);

    addWindowListener( new WindowAdapter() {

      public void windowOpened( WindowEvent e ){
        pattern_txt.requestFocus();
        if(IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }

      @Override
      public void windowClosing(WindowEvent e)
      {
        Runtime.getRuntime().gc();
        //System.gc();
        System.exit(0);
      }

/*
      public void windowIconified( WindowEvent e ){
        pattern_txt.setText(paste().toString());
      }

      public void windowDeiconified( WindowEvent e ){
        pattern_txt.setText(paste().toString());
      }
*/
      public void windowActivated( WindowEvent e ){
        pattern_txt.requestFocus();
        if(IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }

      public void windowDeactivated( WindowEvent e ){
        if(IsAlwaysOnTop())
          setAlwaysOnTop( true );
        else
          setAlwaysOnTop( false );
      }
    });

//    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    this.setSize(300, 160 + 30 * (MAX_PATTERN + 1));
    this.setSize(300, 160 + 30 * (MAX_PATTERN));
//    this.setLocation(300,100);
    // Get the size of the screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

    // Determine the new location of the window
    int w = this.getSize().width;
    int h = this.getSize().height;
//    int x = (dim.width-w)/2;
//    int y = (dim.height-h)/2;
    int x = dim.width-w-15;
    int y = dim.height-h-70;

    // Move the window
    this.setLocation(x, y);

    this.setVisible(true);

    TcpIpPatternReadServiceThread TcpIpReadSvr = new TcpIpPatternReadServiceThread();
    TcpIpReadSvr.start();

  }

  //  SYMBOL       = 0;
  //  GLOBAL       = 1;
  //  CALL_BY_FUNC = 2;
  //  CALLING_FUNC = 3;
  //  TEXT_SEARCH  = 4;
  //  CHANGE_STR   = 5;
  //  EGREP        = 6;
  //  FILE_SEARCH  = 7;
  //  INC_SEARCH   = 8;
  //  ASSIGNMENT   = 9;
  public int getCScopeFieldNum(int cmd_id) {
    switch(cmd_id)
    {
     case CMD_SYMBOL:       return 0;
     case CMD_GLOBAL:       return 1;
     case CMD_CALL_BY_FUNC: return 2;
     case CMD_CALLING_FUNC: return 3;
//     case CMD_TEXT_SEARCH:  return 4;
//     case CMD_CHANGE_STR:   return 5;
//     case CMD_EGREP:        return 6;
     case CMD_FILE_SEARCH:  return 7;
     case CMD_INC_SEARCH:   return 8;
     case CMD_ASSIGNMENT:   return 9;
    }

    System.out.println("Invalid id : " + cmd_id);
    return 0xFF;
  }

  public void SearchAppExec()
  {
    int num_pattern = mPatternType;
    int ListFormat = LIST_FORM_CSEARCH;
    String report_file = DBRootPath + FSEPARATOR + ".ezSeArChRePoRt.temp";

    app_exec_send_msg     = null;
    app_exec_input_stream = null;
    app_exec_stream       = null;
    app_exec_buf_read     = null;
    findReportFrame       = null;

    try {
      StringBuffer message = new StringBuffer();
      switch(num_pattern)
      {
        case CMD_CSEARCH:
          if(CaseSensitive.isSelected())
            message.append("csearch^^-i^^-n^^" + pattern_txt.getText());
          else
            message.append("csearch^^-n^^" + pattern_txt.getText());
          ListFormat = LIST_FORM_CSEARCH;
          break;

        case CMD_PJT_FILE:
          //message.append("/EzSearchPjtFileList" + "grep " + pattern_txt.getText() + " .project_files");
          if(CaseSensitive.isSelected())
            message.append("csearch^^-i^^-l^^-f^^" + pattern_txt.getText() + "^^.");
          else
            message.append("csearch^^-l^^-f^^" + pattern_txt.getText() + "^^.");
          ListFormat = LIST_FORM_FILELIST;
          break;

        case CMD_SYMBOL:
        case CMD_GLOBAL:
        case CMD_CALL_BY_FUNC:
        case CMD_CALLING_FUNC:
//        case CMD_TEXT_SEARCH:
        //case CMD_CHANGE_STR:
//        case CMD_EGREP:
        case CMD_FILE_SEARCH:
        case CMD_INC_SEARCH:
        case CMD_ASSIGNMENT:
          if(CaseSensitive.isSelected())
            message.append("cscope^^-d^^-C^^-L" + getCScopeFieldNum(num_pattern) + "^^" + pattern_txt.getText());
          else
            message.append("cscope^^-d^^-L" + getCScopeFieldNum(num_pattern) + "^^" + pattern_txt.getText());
          ListFormat = LIST_FORM_CSCOPE;
          break;

        case CMD_SEND_CMD:
          message.append("/bin/bash^^EzSearchSrvShell.sh^^/ClientCmd^^" + pattern_txt.getText());
          break;

        default: //for test
          message.append("dir");
          break;
      }

      long start = System.currentTimeMillis();

      if(LocalMode)
      {
        app_exec_send_msg = new String(message);
        //app_exec_send_msg = new String(pattern_txt.getText());

        List<String> cmd = new ArrayList(Arrays.asList(app_exec_send_msg.split("\\^\\^"))); //^^

        ProcessBuilder pb = new ProcessBuilder();
        Map<String, String> env = pb.environment();

        File fh = new File(report_file);
        if(fh.exists())
          fh.delete();

        pb = pb.directory(new File(DBRootPath));

        if(csearchindex != "")
        {
          fh = new File(csearchindex);
          if(fh.exists())
          {
            env.put("CSEARCHINDEX", csearchindex);
          }
          else
          {
            JOptionPane.showMessageDialog(this, "CSEARCHINDEX 경로를 확인 하세요", "Message", JOptionPane.WARNING_MESSAGE);
          }
        }
//        System.out.println("cmd : " + message);

        pb.command(cmd);
        pb.redirectOutput(new File(report_file));
        //pb.redirectError(new File(report_file + "_err"));

        Process p = pb.start();
        p.waitFor();
        env.clear();

        cmd.clear();
        cmd = null;

        //System.out.println("CSEARCHINDEX : " + env.get("CSEARCHINDEX"));

        app_exec_buf_read = new BufferedReader(new FileReader(report_file));
      }
      else
      {
        message.append(ENDL);
        app_exec_send_msg = new String(message);

        TcpIp.connect(argIPAddress, ServerPortNum);
        TcpIp.sendMessage(app_exec_send_msg);

        System.out.println("Sending : " + new Date().toString());
        System.out.println(app_exec_send_msg);
        app_exec_input_stream = new ByteArrayInputStream(TcpIp.receiveMessage().getBytes());
        app_exec_buf_read     = new BufferedReader(new InputStreamReader(app_exec_input_stream));
        TcpIp.disconnect();
      }

      switch(num_pattern)
      {
        case CMD_SEND_CMD:
          while (true)
          {
            String readLineData = app_exec_buf_read.readLine();
            if(readLineData == null)
              break;
            System.out.println(readLineData);
          }
          System.out.println("Interval : " + (System.currentTimeMillis() - start )/1000.0 + " Sec" + ENDL);
          this.show();
          JOptionPane.showMessageDialog(this, "처리 완료 : " + pattern_txt.getText());
          break;

        default:
          System.out.println("Interval : " + (System.currentTimeMillis() - start )/1000.0 + " Sec" + ENDL);
          findReportFrame = new SearchReport(ExecuteCase[num_pattern], pattern_txt.getText(),
                                            app_exec_buf_read, this.getSize(), this.getLocation(),
                                             ListFormat, prefixPath, DBRootPath);
          break;
      }

      if(app_exec_stream != null)
        app_exec_stream.close();

      if(app_exec_input_stream != null)
        app_exec_input_stream.close();

      if(app_exec_buf_read != null)
        app_exec_buf_read.close();
    } catch (Exception e) {
      System.out.println("SearchAppExec : " + e);
      e.printStackTrace();
      if(LocalMode)
      {

      }
      else
      {
        JOptionPane.showMessageDialog(this, "서버 상태를 확인 하세요", "Message", JOptionPane.WARNING_MESSAGE);
      }
    } finally {
    }
    lblOut.setText("Button Click!!!");
  }

  class CSearchExecThread extends Thread{  //Thread 클래스를 상속받는 클래스를 작성
    public void run()   //run() 메서드를 오버라이딩
    {
      Runtime.getRuntime().gc();
      SearchAppExec();
    }
  }

  public void focusGained(FocusEvent ee) {
//      if(bClipPaste == true)
//        pattern_txt.setText(paste().toString());
//      bClipPaste = false;
  }

  public void focusLost(FocusEvent ee) {

  }

  //버튼 액션 구현
  @Override
  public void actionPerformed(ActionEvent e)
  {
    Object ob = e.getSource();
    int num_pattern = 0;

    Runtime.getRuntime().gc();

    if(ob == helpButton) {
      Help help = new Help(this.getSize(), this.getLocation());
      help = null;
    } else if(ob == AlwaysOnTop) {
      isAlwaysOnTop = AlwaysOnTop.isSelected();
      if(IsAlwaysOnTop())
        setAlwaysOnTop( true );
      else
        setAlwaysOnTop( false );
    } else {
      for(num_pattern = 0; num_pattern < MAX_PATTERN; num_pattern++)
      {
        if(ob == FndButton[num_pattern])
        {
          switch(num_pattern)
          {
            case CMD_DOSCMD:
              if(pattern_txt.getText().indexOf("/xml") >= 0)
              {
                String[] cmd={};
                cmd = pattern_txt.getText().split(" ");

                new xml_convert(cmd[1], cmd[2]);
                ob = null;
                return;
              }

              DOSOpen dos = null;
              File fh = new File(pattern_txt.getText());
              if(fh.exists())
              {
                dos = new DOSOpen(pattern_txt.getText(), csearchindex);
              }
              else
              {
                dos = new DOSOpen(DBRootPath, csearchindex);
              }

              dos = null;
              ob = null;
              System.gc();
              return;

            default:
              if(pattern_txt.getText().length() <= 0)
              {
                JOptionPane.showMessageDialog(this, "검색어가 없습니다", "Message", JOptionPane.WARNING_MESSAGE);
                ob = null;
                return;
              }

              if(pattern_txt.getText().matches("/EzSearchExIt.*"))
              {
                System.exit(0);
                ob = null;
                return;
              }
              break;
          }

          lblOut.setText("Waitting!!! " + ExecuteCase[num_pattern] + pattern_txt.getText());
          mPatternType = num_pattern;

//          CSearchExecThread csThread = new CSearchExecThread();
//          csThread.start();
          SearchAppExec();
          break;
        }
      }
    }
    ob = null;
    //System.gc();
  }

  private void setDesign()
  {
    lblOut = new Label("Button Click!!!");

    JPanel pTop = new JPanel();
//    pTop.setBackground(Color.orange);
    this.add("North",pTop);
    pTop.setLayout(new BoxLayout(pTop,BoxLayout.Y_AXIS));

    pattern_txt = new JTextField(128);
    pattern_txt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getSource()==pattern_txt)
        {
//          JOptionPane.showMessageDialog(null, ExecuteCase[DEFAULT_FIND] + pattern_txt.getText() + "\n검색을 시작 합니다", "Message", JOptionPane.INFORMATION_MESSAGE);
          FndButton[0].doClick();
        }
      }
    });
    pattern_txt.addFocusListener(this);

    pTop.add(pattern_txt, BorderLayout.NORTH);

    JPanel pCheckBox=new JPanel();
//    pCheckBox.setLayout(new BoxLayout(pCheckBox,BoxLayout.Y_AXIS));

    CaseSensitive = new JCheckBox("Ignore case distinctions",false);
    CaseSensitive.setAlignmentX(Component.LEFT_ALIGNMENT);
    pCheckBox.add(CaseSensitive);

    AlwaysOnTop = new JCheckBox("Always On Top",true);
    AlwaysOnTop.setAlignmentX(Component.LEFT_ALIGNMENT);
    pCheckBox.add(AlwaysOnTop);
    AlwaysOnTop.addActionListener(this);
    isAlwaysOnTop = AlwaysOnTop.isSelected();

    pTop.add(pCheckBox,BorderLayout.SOUTH);

    JPanel pCenter=new JPanel();
    pCenter.setLayout(new BoxLayout(pCenter,BoxLayout.Y_AXIS));

//    pCenter.setBackground(Color.orange);
    this.add("Center",pCenter);

    int num_pattern = 0;
    for(num_pattern = 0; num_pattern < MAX_PATTERN ; num_pattern++) {
      FndButton[num_pattern]=new JButton(num_pattern + ". " + ExecuteCase[num_pattern]);
      pCenter.add(FndButton[num_pattern]);
      FndButton[num_pattern].addActionListener(this);
      FndButton[num_pattern].addFocusListener(this);
    }
    //Disable Button : Change this text string
//    FndButton[CMD_CHANGE_STR].setEnabled(false);

    if(LocalMode)
      FndButton[CMD_SEND_CMD].setEnabled(false);

    helpButton.addActionListener(this);
    pCenter.add(helpButton);

    JPanel pBottom=new JPanel(new BorderLayout());
    pBottom.setBackground(new Color(200,255,255));
    pBottom.add(lblOut, BorderLayout.SOUTH);
    lblOut.setBackground(Color.GREEN);

    Label lblIPAddr;
    if(LocalMode)
    {
      lblIPAddr=new Label("Local Mode = " + csearchindex);
    }
    else
    {
      lblIPAddr=new Label("Search Server = " + argIPAddress + ":" + ServerPortNum);
    }
    pBottom.add(lblIPAddr, BorderLayout.NORTH);

    this.add("South",pBottom);
  }

  public void copy(TextField tf)  {
    StringSelection data = new StringSelection(tf.getText());
    Clipboard clipboard = getToolkit().getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(data, data);
  }

  public String paste()  {
    Clipboard clipboard = getToolkit().getDefaultToolkit().getSystemClipboard();
    Transferable data = clipboard.getContents(this);
    String s;
    try {
      s = (String) (data.getTransferData(DataFlavor.stringFlavor));
    } catch (Exception e) {
      s = data.toString();
    }

   return s;
  }

  class TcpIpPatternReadServiceThread extends Thread{
    public void run()
    {
      int loop = 0;
      boolean success = false;

      success = false;
      TcpIpServer0 = new TcpIpServer();
      for(loop = 0; loop < 10; loop++) {
        try {
          if(TcpIpServer0.connect(LocalCmd0PortNum) == true)
          {
            TcpIpServer0.receiveMsgToBuffer(pattern_txt, FndButton[DEFAULT_FIND]);
            success = true;
            break;
          }
          else
          {
  //          System.exit(0);
          }
        } catch (Exception e) {
          e.printStackTrace();
  //        System.exit(0);
        } finally {
        }
        LocalCmd0PortNum++;
      }
      if(success == false)
        System.exit(0);

      success = false;
      TcpIpServer1 = new TcpIpServer();
      for(loop = 0; loop < 10; loop++) {
        try {
          if(TcpIpServer1.connect(LocalCmd1PortNum) == true)
          {
            TcpIpServer1.receiveMsgToBuffer(pattern_txt, FndButton[CMD_SEND_CMD]);
            success = true;
            break;
          }
          else
          {
//            System.exit(0);
          }
        } catch (Exception e) {
          e.printStackTrace();
//          System.exit(0);
        } finally {
        }
        LocalCmd1PortNum++;
      }
      if(success == false)
        System.exit(0);

      //System.gc();
    }
  }

  public void TcpIpPatternSendService()
  {
    try {
      TcpIpClient = new TcpIpClient();
      if(SendArgPatternMode == 1) {
        TcpIpClient.connect(InetAddress.getLocalHost().getHostAddress(), LocalCmd0PortNum);
      } else if(SendArgPatternMode == 2) {
        TcpIpClient.connect(InetAddress.getLocalHost().getHostAddress(), LocalCmd1PortNum);
      }
      TcpIpClient.sendMessage(argPattern);
//      TimeUnit.SECONDS.sleep(2);
      TcpIpClient.disconnect();
      System.exit(0);
    } catch (Exception e) {
      System.out.println("TcpIpPatternSendServiceThread : " + e);
    } finally {
    }
    //System.gc();
  }

  public static boolean IsAlwaysOnTop(){
    return isAlwaysOnTop;
  }

  public static void main(String[] args)
  {
    if((args.length > 0) && (args[0].length() > 0))
    {
      argPattern = args[0];
    }

    try {
      String localPath;
      localPath = new java.io.File(".").getCanonicalPath();

      if(DBRootPath.length() <= 0)
        DBRootPath = localPath;

      } catch (IOException ee) {
    }

    if((args.length >= 4) && argPattern.equals("/EzSearchServer"))
    {
      argIPAddress  = args[1];
      ServerPortNum = Integer.parseInt(args[2]);
      prefixPath  = args[3];

      if(args[4].length() > 0)
        DBRootPath = args[4];

      LocalMode = false;
      argPattern = "";
      script.makeClientBatch(argIPAddress, ServerPortNum, prefixPath);
    }
    else if(argPattern.equals("/EzSearchLocal"))
    {
      argPattern = "";
      if(args[1].length() > 0)
        csearchindex = args[1];
      script.makeClientBatch(DBRootPath);
    }
    else if((args.length == 2) && argPattern.equals("/ClientCmd"))
    {
        argPattern = args[1];
        SendArgPatternMode = 2;
    }
    else if((args.length == 1) && (argPattern.length() > 0))
    {
      SendArgPatternMode = 1;
    }
    else
    {
      script.makeClientBatch(DBRootPath);
//        System.out.println("ex) java -jar EzScope.jar /EzScopeServer 127.0.0.1 1029");
//        System.out.println("ex) D:\\Pjt>ezscopeClient.bat");
    }

    new EzSearchClient(DBRootPath + " : Ez Search+SourceInsight - hagueljh@gmail.com");
  }
}



