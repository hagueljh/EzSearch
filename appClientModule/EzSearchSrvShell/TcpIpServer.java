package EzSearchSrvShell;
//http://warmz.tistory.com/598

//TcpIpServer.java
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.*;

public class TcpIpServer {
    public static String FSEPARATOR = System.getProperty("file.separator");
    public static String ENDL = System.getProperty("line.separator");
    public static String WINENDL = "\r\n";
    public static String LNXENDL = "\n";

    private BufferedWriter bw       = null;
    private BufferedReader br       = null;
    private BufferedInputStream in  = null;
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private JTextField outbuf = null;
    private JButton actionBtn = null;
    byte[] temp_buf;

    /** Creates a new instance of TcpIpServer */
    public TcpIpServer() {
        socket = null;
        serverSocket = null;

        bw = null;
        br = null;
        in = null;
    }

    public boolean connect(int port) throws UnknownHostException, IOException {
      try{
        serverSocket = new ServerSocket(port);
        return true;
      }catch(Exception e){
        e.printStackTrace();
      }
      return false;
    }

/*
    class receiveMsgToBufThread extends Thread{
      public void run()
      {
        try {
          String line = null;
          while ((line = br.readLine()) != null) {
            outbuf.setText(line.toString());
            System.out.println("receiveMessageThread : " + line.toString());
          }
        } catch (Exception e) {
          System.out.println("receiveMessageThread : " + e);
        }
        System.out.println("receiveMessageThread : Finish");
      }
    }
*/

    class receiveMsgToBufThread extends Thread{
      public void run()
      {
        try {
          while(true) {
            try {
              socket = serverSocket.accept();

              in = new BufferedInputStream(socket.getInputStream());
              temp_buf = new byte[4096];

              int read = 0;
              while((read = in.read(temp_buf)) > 0) {
                outbuf.setText(new String(temp_buf, 0, read));
              }

              String str = outbuf.getText();

              //if(Character.isWhitespace(str.charAt(str.length() - 1)))
                outbuf.setText(str.substring(0, str.length()-1));
              //System.out.println("stream : [ " + outbuf.getText() + " ], len = " + outbuf.getText().length());

              actionBtn.doClick();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                in.close();
                socket.close();
                in = null;
                socket = null;
                temp_buf = null;
            }
          }
        } catch (Exception ee) {
        }
      }
    }

    public void receiveMsgToBuffer(JTextField buffer, JButton button) throws IOException {
        outbuf = buffer;
        actionBtn = button;
        receiveMsgToBufThread rcvThread = new receiveMsgToBufThread();
        rcvThread.start();
    }

    public void disconnect() throws IOException {
            br.close();
            bw.close();
            in.close();
            socket.close();
            serverSocket.close();

            br = null;
            bw = null;
            in = null;
            socket = null;
            serverSocket = null;
    }

    class execAppThread extends Thread{
      public void run()
      {
        try {
        } catch (Exception ee) {
          System.out.println("execAppThread : " + ee);
        } finally {
        }
      }
    }

    public void startServer(int port, String path, String csearchindex) {
      try {
        serverSocket = new ServerSocket(port);
        //serverSocket.setSoTimeout(2000);
      } catch (SocketTimeoutException ste) {
        System.out.println(ste);
        ste.printStackTrace();
        System.exit(0);
      } catch (Exception e) {
        System.out.println(e);
        //e.printStackTrace();
        System.exit(0);
      }

      Date date = null;
      while(true)
      {
        try {
          Runtime.getRuntime().gc();

          socket = serverSocket.accept();

          date = new Date();
          System.out.println("Starting : " + date.toString());

          //bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); //보내는 데이타
          br = new BufferedReader(new InputStreamReader(socket.getInputStream())); //받는 데이타

          try{
            long start = System.currentTimeMillis();

            execApp(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), path, br.readLine(), csearchindex);
            //bw.write(execApp(path, br.readLine()));
            //bw.newLine();   //아주 중요..생략시 서버로 부터 데이타 수신이 안되는 경우 발생 됨.
            //bw.flush();

            System.out.println("Interval : " + ( System.currentTimeMillis() - start )/1000.0 + " Sec");
          }catch(SocketTimeoutException ste){
            System.out.println(ste);
            //ste.printStackTrace();
            System.exit(0);
          }catch(Exception e){
            System.out.println(e);
            //e.printStackTrace();
            System.exit(0);
          }finally{
            br.close();
            //bw.close();
//            socket.close();
//            serverSocket.close();
            System.gc();
          }
          }catch(SocketTimeoutException ste){
            System.out.println(ste);
            //ste.printStackTrace();
            System.exit(0);
          } catch (Exception e) {
            System.out.println(e);
            //e.printStackTrace();
            System.exit(0);
        }
      }
    }

    static public void execApp(BufferedWriter bw, String path, String param, String csearchindex)  throws InterruptedException  {
        try {
          System.out.println(param);

          //Runtime oRuntime = Runtime.getRuntime();
          //Process oProcess = oRuntime.exec(param);
          //BufferedReader br  = new BufferedReader(new InputStreamReader(oProcess.getInputStream()));

          String report_file = path + FSEPARATOR + ".ezSeArChRePoRt.temp";
          String report_err_file = path + FSEPARATOR + ".ezSeArChRePoRtErr.temp";
          File fh = new File(report_file);
          if(fh.exists())
            fh.delete();

          fh = new File(report_err_file);
          if(fh.exists())
            fh.delete();

          List<String> cmd = new ArrayList(Arrays.asList(param.split("\\^\\^")));//^^

          ProcessBuilder pb = new ProcessBuilder();
          Map<String, String> env = pb.environment();

          pb = pb.directory(new File(path));

          if(csearchindex != "")
          {
            fh = new File(csearchindex);
            if(fh.exists())
            {
              env.put("CSEARCHINDEX", csearchindex);
            }
            else
            {
            }
          }

          pb.command(cmd);
          pb.redirectOutput(new File(report_file));
          pb.redirectError(new File(report_err_file));
          Process p = pb.start();
          p.waitFor();

          try (BufferedReader br = new BufferedReader(new FileReader(report_file)))
          {
            String line = null;
            while ((line = br.readLine()) != null) {
              StringBuffer sb = new StringBuffer();
              sb.append(line);
              sb.append(System.getProperty("line.separator"));
              bw.write(new String(sb));
            }
            bw.newLine();   //아주 중요..생략시 서버로 부터 데이타 수신이 안되는 경우 발생 됨.
            bw.flush();
            bw.close();
          } catch (IOException e) {
            e.printStackTrace();
          }

          return;
        } catch (IOException e) { // 에러 처리
          System.out.println("execApp Exception : " + e);
          e.printStackTrace();
        } finally {
        }
        return;
    }
}

class srv_script {
  public static String FSEPARATOR = System.getProperty("file.separator");
  public static String ENDL = System.getProperty("line.separator");
  public static String WINENDL = "\r\n";
  public static String LNXENDL = "\n";

//  public static String ezsearch_path = System.getenv("EZSEARCH");
//  if(ezsearch_path == null || ezsearch_path.isEmpty())
//    ezsearch_path = "";

  public static void setExecuteFile(String name)
  {
    try {
    	String command= "chmod a+x " + name;
    	Runtime rt = Runtime.getRuntime();
    	Process pr = rt.exec(command);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void makeServerBatch(String name, int PortNum)
  {
    try {
      File f = null;

      if(ENDL.equals(WINENDL)){
        f = new File(name + ".bat");
        if(f.exists() == false)
        {
          FileWriter fw = new FileWriter(f);
          fw.write("@ECHO OFF");
          fw.write(WINENDL);
          fw.write("IF \"%EZSEARCH%\"==\"\" SET EZSEARCH=\".\"" + WINENDL);
          fw.write("java -Xms128m -Xmx512m -jar " + "%EZSEARCH%\\" + name + ".jar ");
          fw.write(Integer.toString(PortNum));
          fw.write(WINENDL);
          fw.flush();
          fw.close();
          fw = null;
        }
        f = null;
      }

      if(ENDL.equals(LNXENDL)){
        f = new File(name + ".sh");
        if(f.exists() == false)
        {
          FileWriter fw = new FileWriter(f);
          fw.write("#!/bin/sh");
          fw.write(LNXENDL);
          fw.write("if [ -z \"$EZSEARCH\" ]; then EZSEARCH=\".\"; fi" + LNXENDL);
          fw.write("java -Xms128m -Xmx512m -jar " + "$EZSEARCH/" + name + ".jar ");
          fw.write(Integer.toString(PortNum));
          fw.write(LNXENDL);
          fw.flush();
          fw.close();
          setExecuteFile(name + ".sh");
          fw = null;
        }
        f = null;
      }
    } catch (Exception e) {

    }
  }

  public static void makeClientBatch(String argIPAddress, int PortNum, String prefixPath)
  {
    try {
      File f = null;

      f = new File("ezsearch_svc.bat");
//      if(f.exists() == false)
      {
        FileWriter fw = new FileWriter(f);
        fw.write("@ECHO OFF");
        fw.write(WINENDL);
        fw.write("IF \"%EZSEARCH%\"==\"\" SET EZSEARCH=\".\"" + WINENDL);
        fw.write("java -Xms128m -Xmx1024m -jar " + "%EZSEARCH%\\" + "EzSearch.jar /EzSearchServer ");
        fw.write(argIPAddress);
        fw.write(" ");
        fw.write(Integer.toString(PortNum));
        fw.write(" ");
        fw.write(prefixPath);
        fw.write(WINENDL);
        fw.flush();
        fw.close();
        fw = null;
      }
      f = null;

      f = new File("ezsearch_svc.sh");
//      if(f.exists() == false)
      {
        FileWriter fw = new FileWriter(f);
        fw.write("#!/bin/sh");
        fw.write(LNXENDL);
        fw.write("if [ -z \"$EZSEARCH\" ]; then EZSEARCH=\".\"; fi" + LNXENDL);
        fw.write("java -Xms128m -Xmx1024m -jar " + "$EZSEARCH/" + "EzSearch.jar /EzSearchServer ");
        fw.write(argIPAddress);
        fw.write(" ");
        fw.write(Integer.toString(PortNum));
        fw.write(" ");
        fw.write(prefixPath);
        fw.write(LNXENDL);
        fw.flush();
        fw.close();
        setExecuteFile("ezsearch.sh");
        fw = null;
      }
      f = null;
    } catch (Exception e) {
    }
  }

  public static void makeClientBatch(String localPath)
  {
    String FSEPARATOR = System.getProperty("file.separator");
    try {
      File f = null;
      String csearchindex = System.getenv("CSEARCHINDEX");
      if(ENDL.equals(WINENDL)){
        f = new File("ezsearch_loc.bat");
        if(f.exists() == false)
        {
          FileWriter fw = new FileWriter(f);
          fw.write("@ECHO OFF");
          fw.write(WINENDL);
          fw.write("IF \"%EZSEARCH%\"==\"\" SET EZSEARCH=\".\"" + WINENDL);
          fw.write("java -Xms128m -Xmx1024m -jar " + "%EZSEARCH%\\" + "EzSearch.jar /EzSearchLocal ");
          if(csearchindex != null && !csearchindex.isEmpty())
          {
            fw.write(csearchindex);
          }
          else
          {
//            fw.write(localPath);
            fw.write(" %CD%");
            fw.write(FSEPARATOR);
            fw.write(".csearchindex");
          }
          fw.write(WINENDL);
          fw.flush();
          fw.close();
          fw = null;
        }
        f = null;
      }

      if(ENDL.equals(LNXENDL)){
        f = new File("ezsearch_loc.sh");
        if(f.exists() == false)
        {
          FileWriter fw = new FileWriter(f);
          fw.write("#!/bin/sh");
          fw.write(LNXENDL);
          fw.write("if [ -z \"$EZSEARCH\" ]; then EZSEARCH=\".\"; fi" + LNXENDL);
          fw.write("java -Xms128m -Xmx1024m -jar " + "$EZSEARCH/" + "EzSearch.jar /EzSearchLocal ");
          if(csearchindex != null && !csearchindex.isEmpty())
          {
            fw.write(csearchindex);
          }
          else
          {
//            fw.write(localPath);
            fw.write(" `pwd`");
            fw.write(FSEPARATOR);
            fw.write(".csearchindex");
          }
          fw.write(LNXENDL);
          fw.flush();
          fw.close();
          setExecuteFile("ezsearch.sh");
          fw = null;
        }
        f = null;
      }
    } catch (Exception e) {
    }
  }

  public static void makeMkEzSearchDB()
  {
    try {
      File f = null;
      if(ENDL.equals(WINENDL)){
        f = new File("mkEzSearchDB.bat");
        if(f.exists() == false)
        {
          FileWriter fw = new FileWriter(f);
          fw.write("@ECHO OFF");
          fw.write(WINENDL);
          fw.write(WINENDL);
          fw.write("SET CSF=cscope.files");
          fw.write(WINENDL);
          fw.write("SET CSO=cscope.out");
          fw.write(WINENDL);
          fw.write("SET CST=cscope.temp");
          fw.write(WINENDL);
          fw.write(WINENDL);

          fw.write("if \"%1\"==\"/^.^SyncHronIze^.^\" goto SYNC");
          fw.write(WINENDL);
          fw.write(WINENDL);

          fw.write("del %CSF% %CSO% %CST%");
          fw.write(WINENDL);
          fw.write("dir /B /S *.c *.h *.l *.y *.s *.cpp *.java *.cc *.aidl *.xml *.txt *.cxx *.hh  *akefile* *.m* *.scl* *.min* *.cfg*  *.def* *.con* *.k* *.rc  *.bat *.pl *.rh *.prop *.qml *.pro > %CST%");
          fw.write(WINENDL);
          fw.write("for /f \"delims=\" %%i in (%CST%) do echo \"%%i\">> %CSF%");
          fw.write(WINENDL);
          fw.write("del %CST%");
          fw.write(WINENDL);
          fw.write("echo Build the cross-reference");
          fw.write(WINENDL);
//          fw.write("mlcscope -b -q -i %CSF%");
//          fw.write("cscope -b -i %CSF%");
          fw.write("cscope -b -q -i %CSF%");
          fw.write(WINENDL);
          fw.write("goto EXIT");
          fw.write(WINENDL);
          fw.write(WINENDL);

          fw.write(":SYNC");
          fw.write(WINENDL);
          fw.write("echo Update the cross-reference");
          fw.write(WINENDL);
//          fw.write("cscope -L");
          fw.write("cscope -q -L");
          fw.write(WINENDL);
          fw.write("exit");
          fw.write(WINENDL);
          fw.write(WINENDL);

          fw.write(":EXIT");
          fw.write(WINENDL);

          fw.flush();
          fw.close();
        }
      }

      if(ENDL.equals(LNXENDL)){
        f = new File("mkEzSearchDB.sh");
        if(f.exists() == false)
        {
          FileWriter fw = new FileWriter(f);
          fw.write("#!/bin/sh");
          fw.write(LNXENDL);

          fw.write("CSF=cscope.files" + LNXENDL);
          fw.write("CSO=cscope.out"   + LNXENDL);
          fw.write("CST=cscope.temp"  + LNXENDL);
          fw.write("SPATH=."          + LNXENDL);
          fw.write(LNXENDL);

          fw.write("if [ -z \"$CSEARCHINDEX\" ]; then"  + LNXENDL);
          fw.write(" `pwd`"                             + LNXENDL);
          fw.write(".csearchindex"                      + LNXENDL);
          fw.write("fi"                                 + LNXENDL);
          
          fw.write("if [ $# = 0 ] ; then"         + LNXENDL);
          fw.write("  echo \"$0 [Source Path]\""  + LNXENDL);
          fw.write("  exit"                       + LNXENDL);
          fw.write("fi"                           + LNXENDL);
          fw.write(LNXENDL);

          fw.write("if [ $1 = \"/^.^SyncHronIze^.^\" ]  ;	then" + LNXENDL);
          fw.write("  echo \"Synchronize\""         + LNXENDL);
          fw.write("  cscope -q -L"                 + LNXENDL);
          fw.write("else"                           + LNXENDL);
          fw.write("  if [ ! -d $1 ] ; then"        + LNXENDL);
          fw.write("    echo \"Invalid Directory\"" + LNXENDL);          
          fw.write("  fi"                           + LNXENDL);
          fw.write(LNXENDL);

          fw.write("  SPATH=$1"                     + LNXENDL);

          ///////////////////////////////////////////////////
          //     make cscope DB
          ///////////////////////////////////////////////////
          fw.write("  echo \"Build the cross-reference\"" + LNXENDL);
          fw.write("  rm -rf $CSF $CSO $CST"              + LNXENDL);
          fw.write("  find $SPATH ");
          //fw.write("\\( -iname '*.[chlys]' -o -iname '*.cpp' -o -iname '*.java' -o -iname '*.cc' -o -iname '*.aidl' -o -iname '*.xml' -o -iname '*.txt' -o -iname '*.cxx' -o -iname '*.hh'  -o -iname '*akefile*' -o -iname '*.m*' -o -iname '*.scl*' -o -iname '*.min*' -o -iname '*.cfg*'  -o -iname '*.def*' -o -iname '*.con*' -o -iname '*.k*' -o -iname '*.rc' -o -iname '*.prop' \\) -print > $CST");
          fw.write("\\( -iname '*.[chlys]' -o -iname '*.cpp' -o -iname '*.java' -o -iname '*.cc' -o -iname '*.aidl' -o -iname '*.xml' -o -iname '*.dtsi' -o -iname '*.qml' -o -iname '*.pro' \\) -print > $CST");
          fw.write(LNXENDL);
          fw.write("  while read line"            + LNXENDL);
          fw.write("  do"                         + LNXENDL);
          fw.write("  echo \\\"$line\\\">>  $CSF" + LNXENDL);
          fw.write("  done < $CST"                + LNXENDL);
          fw.write("  rm $CST"                    + LNXENDL);
//          fw.write("cscope -b -i $CSF"          + LNXENDL);
          fw.write("  cscope -b -q -i $CSF"       + LNXENDL);

          ///////////////////////////////////////////////////
          //     make Csearch DB
          ///////////////////////////////////////////////////
          fw.write("cindex $SPATH");
          fw.write(LNXENDL);
          
          fw.write("fi"                           + LNXENDL);
          
          fw.flush();
          fw.close();
          setExecuteFile("mkEzSearchDB.sh");
        }
      }
    } catch (Exception e) {
    }
  }
  
  public static String getLocalServerIp()
  {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
      {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
        {
          InetAddress inetAddress = enumIpAddr.nextElement();
          if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress())
          {
            return inetAddress.getHostAddress().toString();
          }
        }
      }
    } catch (SocketException ex) {}
    return null;
  }
}
