package Server.Room.Round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import Client.GameObjectController;
import Client.GameObject.Player;

import java.util.List;
import java.util.Map;
import java.util.Random;

import Common.Model.GameModel.Position;
import Common.Model.GameModel.User;
import Common.Model.SocketModel.MoveReq;
import Common.Model.SocketModel.MoveRes;
import Common.Model.SocketModel.DamagedRes;
import Common.Model.SocketModel.PlayMusicRes;
import Common.Model.SocketModel.SitDownReq;
import Common.Model.SocketModel.SitDownRes;
import Server.Room.Room;
import Server.Room.Round.RoundManager.RoundCommonStatus;
import Server.Room.User.GameUser;
import Client.GameFrame;

// 説明: 
// roundUsersにはroundで生き残っているユーザーたちが入る
// 座るのに成功した時には、roomSurvivedUserIDsにuserIDをputする。
// ユーザーが移動した時にはhandleMoveReqが実行される。それを部屋の中のユーザー全体に通知するにはroom.Publishをする。
// run内にゲームの実装を記述する。一定時間毎に音楽を流し...など。
public class Round extends Thread { 
    private Room room;
    private Map<Integer, RoundUser> roundUsers; //roundで生きているユーザー達 
    private List<Integer> roomSurvivedUserIDs;  //roundで生き残ったユーザー達 
    private List<RoundChairs> chairs;

    public class RoundChairs{
        public Position position;
        boolean cannotSit;
        public RoundChairs(){
            position = new Position();
            cannotSit = false;
        }
    }

    boolean isEnd=false;

    //衝突したらtrueになる．当たり証明みたいな
    boolean collision=false;

    private int initialRoundUsers = 0;
    private boolean canSit=false;



    public Round(Room room, Map<Integer,RoundCommonStatus> statusMap, List<Integer> survivedUserIDs){
        this.room = room;
        this.roundUsers = new ConcurrentHashMap<>();
        for(GameUser user: room.users.values()){
            if(statusMap.containsKey(user.user.UserID)){
                roundUsers.put(user.user.UserID, new RoundUser(user, statusMap.get(user.user.UserID)));
            }else{
                roundUsers.put(user.user.UserID, new RoundUser(user, new RoundCommonStatus()));
                System.err.printf("Failed to fetch status of userid %d, so create new status.\n", user.user.UserID);
            }
        }
        this.initialRoundUsers = survivedUserIDs.size();
        this.roomSurvivedUserIDs = new ArrayList<>();
        this.chairs = new ArrayList<>();
            
            // 椅子を配置。人数より１少ない
        for(int i=0;i<survivedUserIDs.size() - 1; i++){
            chairs.add(new RoundChairs());
        }
        if(survivedUserIDs.size() ==2){
            chairs.get(0).position.setPosition( GameFrame.width/2, GameFrame.height/2 );
        }else{
            double theta = 2*Math.PI/chairs.size();
            double r = (GameObjectController.chairRadius + GameObjectController.margin)/Math.sin(theta/2);
            for (int i = 0; i < chairs.size(); i++) {
                chairs.get(i).position.setPosition((int)Math.round(GameFrame.width/2 + r*Math.sin(theta * i)), (int)Math.round(GameFrame.height/2 - r*Math.cos(theta * i)));
            }
        }
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
                        //椅子に座ることができない間、当たり判定をとりダメージを減らす． 
                        if(!canSit){
                            for(Integer myID : roundUsers.keySet()){
                                CollisionDetection(roundUsers.get(myID));
                            }
                            PlayerDistance();
                        }
                    }
                }
            }.start();

            this.canSit=false;
            for(Integer myID : roundUsers.keySet()){
                roundUsers.get(myID).isdamaged = true; 
            }
            sleep(4000);;
            for(Integer myID : roundUsers.keySet()){
                roundUsers.get(myID).isdamaged = false; 
            }
            room.Publish(new PlayMusicRes(true)); 

            Random random = new Random(); 

            sleep(random.nextInt(20000) + 12000 );
            room.Publish(new PlayMusicRes(false));
            this.canSit=true;
            for(int i=0;i<6;i++){
                System.out.printf("Can Sit Down: %d\n", i*1000);
                sleep(1000);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public List<Integer> getSurvivedUserIDs(){
        return this.roomSurvivedUserIDs;
    }

    private synchronized boolean SitDown(int userID, int chairID){
        if( roundUsers.containsKey(userID) && roomSurvivedUserIDs.size()<(initialRoundUsers-1) ){
            if(!roomSurvivedUserIDs.contains(userID)){
                roomSurvivedUserIDs.add(userID); 
                room.Publish(
                    new SitDownRes(userID, chairID)
                );
            }
            return true; //座るのに成功
        }else{
            return false; //座るのに失敗 
        }
    }

    public void CollisionDetection(RoundUser user){
        if(user.isdamaged == true) return;
        double getdistance;
        for(int i = 0; i < chairs.size(); i++){
            double x2 = user.position.x - chairs.get(i).position.x;
            double y2 = user.position.y - chairs.get(i).position.y;
            getdistance = Math.sqrt(x2*x2+y2*y2);
            //椅子との当たり判定    
            if (getdistance <= GameObjectController.chairRadius + GameObjectController.playerRadius){
                //HP下げる？？
                //2秒ごとに発動
                if(user.status.HP > 0) user.status.HP -= 10;
                room.Publish(new DamagedRes(user.user.UserID, user.status.HP));
                user.isdamaged = true;
                new Thread(){
                    @Override
                    public void run() {
                        try{
                            Thread.sleep(2000); // 2秒間だけ処理を止める
                            user.isdamaged = false;
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }.start();
            }    
        }
        
    }
     //プレイヤー同士の当たり判定
    public void PlayerDistance(){
        for(Integer myID : roundUsers.keySet()){
            if(roundUsers.get(myID).isdamaged == true) continue; 
            for(Integer otherID : roundUsers.keySet()){
                if(myID != otherID){
                    double x3 =roundUsers.get(myID).position.x  - roundUsers.get(otherID).position.x;
                    double y3 = roundUsers.get(myID).position.y - roundUsers.get(otherID).position.y;
                    double getdistance = Math.sqrt(x3*x3+y3*y3);
                    if(getdistance <= 2*GameObjectController.playerRadius){
                        //HP下げる？？
                        //2秒ごとに発動
                        if(roundUsers.get(myID).status.HP > 0) roundUsers.get(myID).status.HP -= 10;
                        room.Publish(new DamagedRes(myID, roundUsers.get(myID).status.HP));
                        roundUsers.get(myID).isdamaged = true;
                        new Thread(){
                            @Override
                            public void run() {
                                try{
                                    Thread.sleep(2000); // 2秒間だけ処理を止める
                                    roundUsers.get(myID).isdamaged = false;
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }.start();                        
                    }
                }

            }
        }
    }

    // TODO: MoveReqの時の処理を記述する。
    // TODO: 当たり判定を実装するなら当たり判定とか? ダメージを受ける場合はroom.PublishでDamagedResを送信する。(下のMoveResを参照) 
    //移動リクエストを受け取った時に実行される。
    public synchronized void handleMoveReq(MoveReq moveReq, int userID){
        // TODO: 当たり判定を実装するなら当たり判定とか? ダメージを受ける場合はroom.PublishでDamagedResを送信する。56行目付近
        roundUsers.get(userID).position.MoveTo( moveReq.x, moveReq.y ); 
        MoveRes moveRes = new MoveRes(userID, moveReq.x, moveReq.y);
        room.Publish( moveRes );

    }
    
    //椅子に座るかどうかを判定する
    //クリックを押した時に実行される。
    public synchronized void handleSitDownReq(SitDownReq sitDownReq, int userID){
        if(roundUsers.containsKey(userID)){
            System.out.printf("SitDownReq: {userID: %d, position: %s}\n", userID, roundUsers.get(userID).position);

            //ユーザーが生きているか判定
            if(roundUsers.get(userID).status.HP<=0){
                return;
            }

            //例えばこんな感じ? 本当は椅子とのあれこれをしたい。
            //座れたかどうかを判定
            //メソッドを使用する
            /* 当たり判定(ユークリッド距離) */
            // room.Publish(new DamagedRes(UserID, HP));
            for(int i = 0; i < chairs.size(); i++){
                if(chairs.get(i).cannotSit == true) continue;
                double chairSumDistance = GameObjectController.chairRadius + GameObjectController.playerRadius;
                double chairdistance = new Position(chairs.get(i).position.x, chairs.get(i).position.y).Distance(new Position(roundUsers.get(userID).position.x, roundUsers.get(userID).position.y));
                if(canSit && chairdistance<chairSumDistance){
                    Boolean isSitDown = SitDown(userID, i);
                    if(isSitDown){
                        System.out.printf("座るのに成功した: UserID=%d\n", userID);
                        chairs.get(i).cannotSit = true;
                    }else{
                        System.out.printf("座るのに失敗した or 既に座っている: UserID=%d\n", userID);
                    }
                }
            }
        }
    }

    public void End(){
        this.isEnd=true;
    }

    public class RoundUser{
        public boolean survived = false; 
        public boolean isdamaged = false;
        public User user;
        public Position position;
        public RoundCommonStatus status;

        public RoundUser(GameUser gameUser, RoundCommonStatus status){
            this.position = new Position(0, 0);
            this.status = status; 
            this.user = gameUser.user;
        }
    }
}
