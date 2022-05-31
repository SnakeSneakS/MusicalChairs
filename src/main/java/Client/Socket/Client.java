package Client.Socket;
/*
// example usage: 
public class Example{

    ClientSocketManager csm =new ClientSocketManager(serverIP, port);
    csm.Connect();

    //connect (か その失敗)待ち
    while(!(csm.IsConnected || csm.IsClosed)) continue;

    //is connect failed, end.
    if(!csm.IsConnected) return;

    csm.Send("test");
}
*/

import Client.Socket.Handler.BaseClientSocketHandler;
import Client.Socket.Handler.JsonHandler;

public class Client extends BaseClientSocketHandler{
    public JsonHandler jsonHandler;

    public Client(String hostname, int port){
        super(hostname, port);
        this.jsonHandler=new JsonHandler(){};
    }

    @Override
    protected void OnConnect(){

    }

    @Override
    protected void OnReceive(String line) {
        jsonHandler.handle(line);
    }

    @Override
    protected void OnClose() {
        System.err.println("Connection disconnected!");
        System.exit(1);
    }

    public void Send(Object obj){
        String message = jsonHandler.ToJson(obj); 
        super.Send(message);
    }
}

