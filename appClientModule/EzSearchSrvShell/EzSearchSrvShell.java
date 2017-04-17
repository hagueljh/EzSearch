package EzSearchSrvShell;

public class EzSearchSrvShell
{
  public static String ENDL = System.getProperty("line.separator");

  static int PortNum = 1029;
  static String argIPAddress;
  static String localPath;
  TcpIpServiceThread TcpIpThread;
  TcpIpServer TcpIp;

  static String csearchindex = "";
  
  public EzSearchSrvShell(String title)
  {
    TcpIpThread = new TcpIpServiceThread();
    TcpIpThread.start();
  }

  class TcpIpServiceThread extends Thread{
    public void run()
    {
      TcpIp = new TcpIpServer();
      TcpIp.startServer(PortNum, localPath, csearchindex);
    }
  }
    
  public static void main(String[] args)
  {
    try {
      localPath = new java.io.File(".").getCanonicalPath();      
      } catch (Exception ee) {
    }

    argIPAddress = srv_script.getLocalServerIp();
    if((args.length > 0) && (args[0].length() > 0))
    {
      PortNum = Integer.parseInt(args[0]);
      srv_script.makeServerBatch("EzSearchSrvShell" , PortNum);
      srv_script.makeClientBatch(argIPAddress, PortNum, localPath);

      if(args[1].length() > 0)
        csearchindex = args[1];
    }
    else
    {
      srv_script.makeMkEzSearchDB();
      System.out.println("Please.. Insert Port num");
      System.out.println("ex)java -jar EzSearchSrvShell.jar " + PortNum);
      System.exit(0);
      return;      
    }

    System.out.println("==  EzSearch Server  ==");
    System.out.println("Path : " + localPath);
    System.out.println("IP   : " + argIPAddress);
    System.out.println("Port : " + PortNum);

    new EzSearchSrvShell(localPath + " : Ez Search+SourceInsight - hagueljh@gmail.com");
  }
}



