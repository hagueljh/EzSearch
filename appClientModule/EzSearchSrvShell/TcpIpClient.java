package EzSearchSrvShell;

import java.net.Socket;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TcpIpClient {

    private Socket socket;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;

    /** Creates a new instance of TcpIpClient */
    public TcpIpClient() {
        socket = null;
        bis = null;
        bos = null;
    }

    public void connect(String host, int port) throws UnknownHostException, IOException {
        socket = new Socket(InetAddress.getByName(host), port);

        bis = new BufferedInputStream(socket.getInputStream());
        bos = new BufferedOutputStream(socket.getOutputStream());
    }

    public void connect(String host, int port, int localPort) throws UnknownHostException, IOException {
        socket = new Socket(InetAddress.getByName(host), port, InetAddress.getLocalHost(), localPort);
    }

    public void sendMessage(String message) throws IOException {
//        System.out.println("sendMessage : " + message);
        bos.write(message.getBytes());
        bos.write(0);
        bos.flush();
    }

    public String receiveMessage() throws IOException {
        byte[] buf = new byte[4096];
        StringBuffer strbuf = new StringBuffer();

        int read = 0;
        while((read = bis.read(buf)) > 0) {
            strbuf.append(new String(buf, 0, read));
        }

        buf = null;
        return new String(strbuf);
    }
    
    public void disconnect() throws IOException {
        bis.close();
        bos.close();
        socket.close();

        socket = null;
        bos = null;
        bis = null;
    }

}


