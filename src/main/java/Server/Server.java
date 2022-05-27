package Server;

import Server.Room.RoomManager;

public class Server {
   
    public static void main (String[] args) {
        System.out.println("Start server!!"); 
        RoomManager roomManager = new RoomManager();
        new GameManager(roomManager);
    }
}
