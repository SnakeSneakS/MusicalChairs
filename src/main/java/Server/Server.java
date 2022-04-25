package Server;

import Server.Room.RoomManager;

public class Server {
   
    public static void main (String[] args) {
        RoomManager roomManager = new RoomManager();
        new GameManager(roomManager);
    }
}
