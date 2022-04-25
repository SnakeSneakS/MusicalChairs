package Server.Socket;

import java.net.ServerSocket;
import java.net.Socket;

import Server.Socket.Handler.*;

/*
Socketを常に待ち受けるようにし、Socketを取得したら、新たなSocketを生成する。
- OnConnectを実行して接続に成功したServerSocketHandlerを渡す部分はOverrideして実装する。
*/
public class ServerSocketManager {
    public int port = 8080;
    public ServerSocket serverSocket=null; 

    public ServerSocketManager(int port){
        this.port = port;
        startHandler(port);
    }

    private void startHandler(int port){
        Thread t = new Thread(){
            @Override
            public void run() {
                try{
                    serverSocket = new ServerSocket(port);
                    while(true){
                        Socket socket = serverSocket.accept();
                        ServerSocketHandler handler = NewServerSockerHandler(socket); 
                        handler.Handle();
                    }
                }catch(Exception e){
                    System.err.println(e);
                }finally{
                    try{
                        if(serverSocket!=null) serverSocket.close();
                    }catch(Exception e){
                        System.err.println(e);
                    }
                }
            }
        };
        t.start();
    }

    public ServerSocketHandler NewServerSockerHandler(Socket socket){
        ServerSocketHandler handler = new ServerSocketHandler(socket){
            @Override
            protected void OnConnect() {
                super.OnConnect();
            }
    
            @Override
            protected void OnReceive(String line) {
                super.OnReceive(line);
            }
    
            @Override
            protected void OnClose() {
                super.OnClose();
            }
        };
        return handler;
    };
}
