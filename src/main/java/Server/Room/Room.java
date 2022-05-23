package Server.Room;

import java.util.Map; 
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import Server.Room.Round.RoundManager;
import Server.Room.User.GameUser; 

import Common.Model.SocketModel.GameStartReq; 
import Common.Model.SocketModel.GameStartRes; 
import Common.Model.SocketModel.MatchStartRes; 
import Common.Model.SocketModel.MoveReq; 
import Common.Model.SocketModel.RoomUsersInfoRes;
import Common.Model.SocketModel.SitDownReq;
import Common.Model.SocketModel.UserInfo; 

//人を集めてゲームを開始するまでを扱う。 
public class Room {
    public static final int minUserNum = 2; 
    public static final int maxUserNum = 5;

    private RoundManager roundManager=null; //roundGameを司る。

    public Map<Integer, GameUser> users;  

    public Room(){
        this.users = new ConcurrentHashMap<Integer, GameUser>();
    }

    //部屋に入室できるかどうか
    public synchronized boolean canJoin(){
        return (roundManager==null || !roundManager.isPlaying) && (this.users.size()<maxUserNum);
    }
    //ゲームを開始できるかどうか(必要人数を満たしているかどうか)
    private synchronized boolean canPlay(){
        int size = this.users.size();
        boolean b = (roundManager==null || roundManager.isPlaying!=true) && (minUserNum <= size) && (size <= maxUserNum); 
        System.out.printf("canPlay?: size=%d, bool=%s\n", users.size(), b); //for debug
        return b;
    }

    //部屋にユーザーを追加する
    public synchronized void AddUser(GameUser user){
        if(canJoin()){
            user.user.UserID = CalcUniqueUserID(); 
            this.users.put(user.user.UserID, user); 
            System.out.printf("New User has entered room: %s, roomUserNum: %d\n", user, this.users.size());

            MatchStartRes res = new MatchStartRes(user.user.UserID);
            String resJson = user.jsonHandler.ToJson(res);
            try{
                user.socket.Send(resJson);
            }catch(Exception e){
                e.printStackTrace();
            }

            //ユーザーが増えたことを知らせる
            UserInfo[] userInfo = new UserInfo[users.size()];
            int i=0;
            for(GameUser userInRoom: users.values()){
                userInfo[i++] = new UserInfo(userInRoom.user.UserID, userInRoom.user.Username);
            }
            RoomUsersInfoRes infoRes = new RoomUsersInfoRes( userInfo );
            Publish( infoRes );
        }
    }

    //Delete User from room: when connection with user has been disconnected.  
    public synchronized void DeleteUser(GameUser user){
        if(users.containsKey(user.user.UserID)){
            users.remove(user.user.UserID);
            System.out.println("User removed room!!"); 
        }else{
            System.err.println("Failed to delete user!!");
        }

        if(users.size()==0){
            roundManager.end();
            this.roundManager=null; 
        }
    }

    //Send Message to all users in room 
    public void Publish(Object sendObj){
        for(GameUser user: users.values()){
            SendMessage(sendObj, user);
        }
    }
    //Send message to 1 user 
    public void SendMessage(Object sendObj, GameUser user){
        try{
            String json = user.jsonHandler.ToJson(sendObj);
            user.socket.Send(json);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //calculate unique user id
    public synchronized int CalcUniqueUserID(){
        Random random = new Random();
        int uniqueID = random.nextInt(Integer.MAX_VALUE);
        while(users.containsKey(uniqueID)){
            uniqueID =  random.nextInt(Integer.MAX_VALUE);
        }
        return uniqueID;
    }

    //ゲームを開始する。
    private synchronized void StartGame(){
        if(roundManager!=null && roundManager.isPlaying==true){
            System.err.println("RoomGame Has already started and not finished!!");
            return; 
        }
        roundManager = new RoundManager(this); 
        roundManager.start();
    }

    //ゲームスタートを知らせて
    public synchronized void room_handleGameStartReq(GameStartReq gameStartReq, int userID){
        if(canPlay()){
            Publish(new GameStartRes(true) );
            StartGame();
        }else{
            SendMessage(new GameStartRes(false), users.get(userID)); 
        }
    }    

    public synchronized void room_handleMoveReq(MoveReq moveReq, int userID){
        if(this.roundManager!=null) roundManager.handleMoveReq(moveReq, userID);
    }

    public synchronized void room_handleSitDownReq(SitDownReq sitDownReq, int userID){
        if(this.roundManager!=null) roundManager.handleSitDownReq(sitDownReq, userID);
    }

}
