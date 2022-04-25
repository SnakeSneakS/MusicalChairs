package Server.Room.Round;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

import Common.Model.GameModel.Position;
import Common.Model.GameModel.User;
import Common.Model.SocketModel.MoveReq;
import Common.Model.SocketModel.MoveRes;
import Common.Model.SocketModel.DamagedRes;
import Common.Model.SocketModel.PlayMusicRes;
import Server.Room.Room;
import Server.Room.User.GameUser;

// 説明: 
// roundUsersにはroundで生き残っているユーザーたちが入る
// 座るのに成功した時には、roomSurvivedUserIDsにuserIDをputする。
// ユーザーが移動した時にはhandleMoveReqが実行される。それを部屋の中のユーザー全体に通知するにはroom.Publishをする。
// run内にゲームの実装を記述する。一定時間毎に音楽を流し...など。
public class Round extends Thread { 
    private Room room;
    private Map<Integer, RoundUser> roundUsers; //roundで生きているユーザー達 
    private List<Integer> roomSurvivedUserIDs;  //roundで生き残ったユーザー達 

    boolean isEnd=false;

    private int initialRoundUsers = 0;
    private boolean canSit=false;

    public Round(Room room, List<Integer> survivedUserIDs){
        this.room = room;
        this.roundUsers = new ConcurrentHashMap<>();
        for(GameUser user: room.users.values()){
            roundUsers.put(user.user.UserID, new RoundUser(user));
        }
        this.initialRoundUsers = survivedUserIDs.size();
        this.roomSurvivedUserIDs = new ArrayList<>();
    }

    @Override
    public void run(){
        //例 
        try{
            //ダメージを見るやつ
            new Thread(){
                @Override
                public void run() {
                    while(!isEnd){
                        //TODO: ダメージを受けるかどうかみて、ダメージを受けた場合、減ったHPと対象のUserIDに対して「
                            //room.Publish(new DamagedRes(UserID, HP))
                        //」を実行する;
                    }
                }
            }.start();

            for(int i=0;i<20;i++){
                System.out.printf( "Playing music: %d\n", (i++)*500 );
                sleep(500);
            }
            room.Publish(new PlayMusicRes(false));
            this.canSit=true;
            for(int i=0;i<6;i++){
                System.out.printf("Can Sit Down: %d\n", i*1000);
                sleep(1000);
            }
            this.canSit=false;
            room.Publish(new PlayMusicRes(true)); 
        }catch(Exception e){
            System.err.println(e);
        }
    }

    public List<Integer> getSurvivedUserIDs(){
        return this.roomSurvivedUserIDs;
    }

    private synchronized boolean SitDown(int userID){
        if( roundUsers.containsKey(userID) && roomSurvivedUserIDs.size()<(initialRoundUsers-1) ){
            if(!roomSurvivedUserIDs.contains(userID)) roomSurvivedUserIDs.add(userID); 
            return true; //座るのに成功
        }else{
            return false; //座るのに失敗 
        }
    }

    public synchronized void handleMoveReq(MoveReq moveReq, int userID){
        // TODO: MoveReqの時の処理を記述する。
        // TODO: 当たり判定を実装するなら当たり判定とか? ダメージを受ける場合はroom.PublishでDamagedResを送信する。(下のMoveResを参照) 
        roundUsers.get(userID).position.MoveTo( moveReq.x, moveReq.y ); 
        MoveRes moveRes = new MoveRes(userID, moveReq.x, moveReq.y);
        room.Publish( moveRes );

        //TODO: 座るかどうか判定し、座る場合、SitDown(userIDを実行する) 
        //とりあえず(350)との距離が30以下ならば座るようにしているが、「椅子に座る」ように修正が必要。
        if(canSit && new Position(350,350).Distance(new Position(moveReq.x, moveReq.y))<100){
            Boolean isSitDown = SitDown(userID);
            if(isSitDown){
                System.out.printf("座るのに成功した: UserID=%d\n", userID);
            }else{
                System.out.printf("座るのに失敗した: UserID=%d\n", userID);
            }
        }
    }

    public void End(){
        this.isEnd=true;
    }

    public class RoundUser{
        public boolean survived = false; 
        public User user;
        public Position position;
        public Integer HP;

        public RoundUser(GameUser gameUsers){
            this.position=new Position(0, 0);
            this.HP = 100; 
        }
    }
}