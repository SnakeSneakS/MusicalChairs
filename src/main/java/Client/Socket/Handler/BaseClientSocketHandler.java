package Client.Socket.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

// Base Socket Client
// create socket manager by extending this class.
// parameters: 
//  - int port
//  - String hostname
// functions: 
//  - abstract function OnReceive(String message). when receive message from client.
//  - abstract function OnClose(). when connection has been closed.
//  - Listen() to connect and listen messages.
//  - Close() to close connection.
//  - Send(String message): send message to connected client.
public abstract class BaseClientSocketHandler{
    public boolean IsClosed = false;
    public boolean IsConnected = false;

    private int port = 80;
    private String hostname = "localhost";

    private boolean shouldClose=false;

    private Socket socket = null;
    private BufferedReader reader = null;   //client message reader
    private PrintWriter writer = null;  //writer

    public BaseClientSocketHandler(String hostname, int port){
        this.port = port;
        this.hostname = hostname;
        this.shouldClose = false;
        this.IsClosed = false;
    }

    //When connect success
    protected abstract void OnConnect();
    //When received message 
    protected abstract void OnReceive(String line); 
    //when connection closed
    protected abstract void OnClose();

    //send message
    protected void Send(String message){
        if(writer != null) writer.println(message);
        else System.err.println("writer is null.");
    }

    //start connect 
    public void Connect(){
        Thread t = new Thread(){
            @Override
            public void run() {
                try{
                    connect();
                }catch(Exception e){
                    throw new RuntimeException(e);
                }
            }
        };
        t.setUncaughtExceptionHandler(
            new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread thread, Throwable throwable) {
                    System.out.println("Exception happened in thread: " + thread.getName());
                    throwable.printStackTrace();
                }
            }
        );
        t.start();
    }

    public void Close(){
        this.shouldClose=true;
    }

    private void onConnect(){
        System.out.printf("Connection success!!\nlocalAddr: %s, remoteAddr: %s\n", this.socket.getLocalSocketAddress(), this.socket.getRemoteSocketAddress() );
        this.IsConnected = true;
        OnConnect();
    }

    private void close() throws IOException{
        //close connection. 
        if(writer != null) reader.close();
        if(reader != null) reader.close();
        if(socket != null) socket.close(); //shutdownInputStreamとshutdownOutputStream
        System.out.println("Connection Closed.");
        this.IsClosed=true;
    }

    //Server: using socket
    private void connect() throws Exception{
        System.out.println("Try to connect to, hostname: "+this.hostname+", port: "+this.port);

        try{
            //initialization
            socket = new Socket(hostname, port);

            if(!socket.isConnected()) return;

            this.reader = new BufferedReader( 
                new InputStreamReader( 
                    socket.getInputStream() 
                )
            );
            this.writer = new PrintWriter( 
                socket.getOutputStream(), 
                true
            );
            
            onConnect();

            String line;
            while((line = reader.readLine())!=null){
                synchronized(this){
                    OnReceive(line);
                    if(this.shouldClose) break;
                    if(this.socket.isInputShutdown()) break;
                    if(this.socket.isOutputShutdown()) break;
                }
            }

        }catch(ConnectException ce){
            System.err.printf("Connect Exception happened! \n%s\n", ce);
            ce.printStackTrace();
            return;
        }catch(UnknownHostException uhe){
            System.err.printf("Unknown Host Exception!\nhost: %s\n%s\n", this.hostname, uhe);
            uhe.printStackTrace();
            throw uhe;
        }catch(Exception e){
            System.err.printf("Exception happened!\nhost: %s\n%s\n", this.hostname, e);
            e.printStackTrace();
            throw e;
        }finally{
            close();

            OnClose();
        }
    }
}