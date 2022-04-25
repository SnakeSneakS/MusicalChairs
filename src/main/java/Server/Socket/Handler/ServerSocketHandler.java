package Server.Socket.Handler;

import java.net.Socket;

/*
usage: 

ServerSocketManager psm = new ServerSocketManager().new ParrotServerSocketManager(port){};
psm.Listen();
*/
public class ServerSocketHandler extends BaseServerSocketHandler {

    public ServerSocketHandler(Socket socket){
        super(socket);
    }

    @Override
    protected void OnConnect(){
        
    }

    @Override
    protected void OnReceive(String line){
    }

    @Override
    protected void OnClose(){
    }
   
}
