package Server.Room.User;

import Common.Model.SocketModel.*;
import Server.Socket.Handler.JsonHandler;
import Common.Model.GameModel.User;
import Server.Room.Room;
import Server.Room.RoomManager;
import Server.Socket.Handler.ServerSocketHandler;

public class GameUser {
    private Room room=null;
    public User user=null;
    public ServerSocketHandler socket=null;
    public JsonHandler jsonHandler;

    public GameUser(User user, ServerSocketHandler socket, RoomManager roomManager){
        this.user=user;
        this.socket=socket;
        this.jsonHandler = new JsonHandler(){
            @Override
            public void handleMatchStartReq(MatchStartReq matchStartReq) {
                user_handleMatchStartReq(matchStartReq, roomManager); 
            };

            @Override
            public void handleGameStartReq(GameStartReq gameStartReq) {
                if(room!=null) room.room_handleGameStartReq(gameStartReq, user.UserID);
            };

            @Override
            public void handleMoveReq(MoveReq moveReq) {
                if(room!=null) room.room_handleMoveReq(moveReq, user.UserID);
            };

            @Override
            public void handleSitDownReq(SitDownReq sitDownReq) {
                if(room!=null) room.room_handleSitDownReq(sitDownReq, user.UserID);
            }
        };
    }

    private synchronized void user_handleMatchStartReq(MatchStartReq matchStartReq, RoomManager roomManager){
        this.user.Username = matchStartReq.Username; 
        this.room = roomManager.AddUser(this);
    }

    public void handleJson(String json, RoomManager roomManager){
        if(this.jsonHandler!=null) jsonHandler.handle(json);
    }

    public void OnClose(){
        this.room.DeleteUser(this);
    }
}


