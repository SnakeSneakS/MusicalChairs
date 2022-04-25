package Server;

import java.net.Socket;

import Common.Model.GameModel.User;
import Server.Room.RoomManager;
import Server.Room.User.GameUser;
import Server.Socket.*;
import Server.Socket.Handler.ServerSocketHandler;

public class GameManager {
    private static final int port = 8080;

    public GameManager(RoomManager roomManager){
        new ServerSocketManager(GameManager.port){
            
            @Override
            public ServerSocketHandler NewServerSockerHandler(Socket socket){
                ServerSocketHandler handler = new ServerSocketHandler(socket){
                    GameUser user; 

                    @Override
                    protected void OnConnect() {
                        //super.OnConnect();
                        this.user = new GameUser(new User(), this, roomManager);
                    }
            
                    @Override
                    protected void OnReceive(String line) {
                        //super.OnReceive(line);
                        this.user.handleJson(line, roomManager); 
                    }
            
                    @Override
                    protected void OnClose() {
                        this.user.OnClose();
                        super.OnClose();
                    }
                };
                return handler;
            };
        };
    }
}
