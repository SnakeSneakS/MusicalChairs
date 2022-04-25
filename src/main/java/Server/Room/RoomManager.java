package Server.Room;

import java.util.ArrayList;
import java.util.List;

import Server.Room.User.GameUser;

public class RoomManager {
    public static final int maxRoomNum = 10; 
    public List<Room> rooms;

    public RoomManager(){
        this.rooms = new ArrayList<Room>(0);
    }

    public synchronized Room AddUser(GameUser user){
        int size = rooms.size();
        for(int i=0;i<size;i++){
            Room room = rooms.get(i);
            if(room.canJoin()){
                room.AddUser(user);
                return room;
            }
        }
        Room newRoom = new Room();
        newRoom.AddUser(user);
        rooms.add(newRoom);
        return newRoom;
    }
    
}
