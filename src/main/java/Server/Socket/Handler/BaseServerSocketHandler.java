package Server.Socket.Handler;

import java.net.Socket;
import java.net.SocketException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;

// Base Socket Server
// create socket manager by extending this class.
// parameters: 
//  - int port
// functions: 
//  - abstract function OnReceive(String message). when receive message from client.
//  - abstract function OnClose(). when connection has been closed.
//  - Handle() to handle socket messages.
//  - Close() to close connection.
//  - Send(String message): send message to connected client.
public abstract class BaseServerSocketHandler {
    public boolean IsConnected = false;
    public boolean IsClosed = false;

    private boolean shouldClose=false;

    private Socket socket = null;
    private BufferedReader reader = null;   //client message reader
    private PrintWriter writer = null;  //writer

    public BaseServerSocketHandler(Socket socket){
        this.shouldClose = false;
        this.IsClosed = false;
        this.socket=socket;
    }

    //When connect success
    protected abstract void OnConnect();
    //When received message 
    protected abstract void OnReceive(String line); 
    //when connection closed
    protected abstract void OnClose();

    //send message
    public void Send(String message) throws Exception{
        if(writer != null){
            writer.println(message);
        }
        else{
            throw new NullPointerException("writer is null.");
        } 
    }

    //start handling 
    public void Handle(){
        Thread t = new Thread(){
            @Override
            public void run() {
                try{
                    handle();
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
        this.shouldClose = false;
        if(writer != null) reader.close();
        if (reader != null) reader.close();
        if(socket != null) socket.close(); //shutdownInputStreamとshutdownOutputStream
        System.out.println("Connection Closed.");
        this.IsClosed = true;

        OnClose();
    }

    //Server: using socket
    private void handle() throws Exception{
        System.out.println("Server Socket handler started!");

        try{
            //initialization
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
        }catch(SocketException se){
            throw se;
        }catch(Exception e){
            throw e;
        }finally{
            close();
        }
    }
}

