import java.io.*;
import java.net.*;

public class Server {
    static int port = 8080;
    public static void main (String[] args) throws IOException {
        ServerSocket s = new ServerSocket(port);
        System.out.println("Started: " + s);
        try {
            Socket socket = s.accept();
            try {
                System.out.println(
                        "Connection accepted: " + socket);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream())); 
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        socket.getOutputStream())),
                        true); 
                while (true) {
                    String str = in.readLine(); 
                    System.out.println(str);
                    if (str.equals("[Position] 0 -100 -100"))
                        break;
                }
            } finally {
                System.out.println("closing...");
                socket.close();
            }
        } finally {
            s.close();
        }
    }
}