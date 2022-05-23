package Server.Room.Round;

import java.util.ArrayList;
import java.util.List;

import Common.Model.SocketModel.GameEndRes;
import Common.Model.SocketModel.MoveReq;
import Common.Model.SocketModel.RoundEndRes;
import Common.Model.SocketModel.SitDownReq;
import Server.Room.Room;
import Server.Room.User.GameUser;

//Room内のゲームを司る
//GameStartした時に生成され、GameEndした時に失われる。
public class RoundManager extends Thread {
    public boolean isPlaying=false;
    public Round round=null;
    Room room;

    private List<Integer> survivedUserIDs;

    public RoundManager(Room room){
        this.room = room; 
        survivedUserIDs = new ArrayList<Integer>();
        for(GameUser user: room.users.values()){
            survivedUserIDs.add(user.user.UserID);
        }
    }

    @Override
    public void run() {
        isPlaying=true;
        while(isPlaying){
            try{
                try{
                    round = new Round(this.room, this.survivedUserIDs);
                    round.start();

                    round.join(); //roundが終わるのを待つ

                    this.survivedUserIDs = round.getSurvivedUserIDs();
                    
                    int[] ids=this.survivedUserIDs.stream().mapToInt(Integer::intValue).toArray() ;
                    room.Publish( 
                        new RoundEndRes(ids) 
                    ); 
                    //for(int i: ids){ System.out.printf("survived user: %d\n", i); }

                    if(this.survivedUserIDs.size() < 2){
                        break;
                    }
                }catch(Exception e){
                    System.err.println(e);
                }
            }catch(Exception e){
                System.err.println(e);
            }
            
        }
        end();
    }

    public void end(){
        if(round!=null) round.End();

        this.isPlaying=false;
        this.round = null; 

        //ゲーム終了を伝える。 
        GameEndRes gameEndRes = new GameEndRes(); 
        room.Publish( gameEndRes );

        System.out.println("Game Ended!!"); 
    }

    public void handleMoveReq(MoveReq moveReq, int userID){
        if(this.round!=null) round.handleMoveReq(moveReq, userID);
    }

    public void handleSitDownReq(SitDownReq sitDownReq, int userID){
        if(this.round!=null) round.handleSitDownReq(sitDownReq, userID);
    }
}
