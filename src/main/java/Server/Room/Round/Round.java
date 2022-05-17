package Server.Room.Round;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import Client.GameObjectController;

import java.util.List;
import java.util.Map;
import java.util.Random;

import Common.Model.GameModel.Position;
import Common.Model.GameModel.User;
import Common.Model.SocketModel.MoveReq;
import Common.Model.SocketModel.MoveRes;
import Common.Model.SocketModel.DamagedRes;
import Common.Model.SocketModel.PlayMusicRes;
import Server.Room.Room;
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
    private List<Position> chairs;

    boolean isEnd=false;

    //衝突したらtrueになる．当たり証明みたいな
    boolean collision=false;

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
        this.chairs = new ArrayList<>();
            
            // 椅子を配置。人数より１少ない
        for(int i=0;i<survivedUserIDs.size() - 1; i++){
            chairs.add(new Position());
        }
        if(survivedUserIDs.size() ==2){
            chairs.get(0).setPosition( GameFrame.width/2, GameFrame.height/2 );
        }else{
            double theta = 2*Math.PI/chairs.size();
            double r = (GameObjectController.chairRadius + GameObjectController.margin)/Math.sin(theta/2);
            for (int i = 0; i < chairs.size(); i++) {
                chairs.get(i).setPosition((int)Math.round(GameFrame.width/2 + r*Math.sin(theta * i)), (int)Math.round(GameFrame.height/2 - r*Math.cos(theta * i)));
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
                        for(RoundUser Member : roundUsers.values()){
                            CollisionDetection(Member);
                        }
                        PlayerDistance();
                        //TODO: ダメージを受けるかどうかみて、ダメージを受けた場合、減ったHPと対象のUserIDに対して「
                            // room.Publish(new DamagedRes(UserID, HP))
                        //」を実行する;
                        // room.Publish(new DamegedRes(UserID, HP));
                    }
                }
            }.start();

            this.canSit=false;
            room.Publish(new PlayMusicRes(true)); 

            Random random = new Random();

            sleep(random.nextInt(20000) + 7000 );
            room.Publish(new PlayMusicRes(false));
            this.canSit=true;
            for(int i=0;i<6;i++){
                System.out.printf("Can Sit Down: %d\n", i*1000);
                sleep(1000);
            }
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


    public void CollisionDetection(RoundUser user){
        double getdistance;
        for(int i = 0; i < chairs.size(); i++){
            double x2 = user.position.x - chairs.get(i).x;
            double y2 = user.position.y - chairs.get(i).y;
            getdistance = Math.sqrt(x2*x2+y2*y2);
            //椅子との当たり判定    
            if (getdistance <= GameObjectController.chairRadius + GameObjectController.playerRadius){
                 //HP下げる？？
                user.HP -= 1;
                room.Publish(new DamagedRes(user.user.UserID, user.HP));
            }
        }
    }
     //プレイヤー同士の当たり判定
    public void PlayerDistance(){
        for(Integer myID : roundUsers.keySet()){
            for(Integer otherID : roundUsers.keySet()){
                if(myID != otherID){
                    double x3 =roundUsers.get(myID).position.x  - roundUsers.get(otherID).position.x;
                    double y3 = roundUsers.get(myID).position.y - roundUsers.get(otherID).position.y;
                    double getdistance = Math.sqrt(x3*x3+y3*y3);
                    if(getdistance <= 2*GameObjectController.playerRadius){
                         //HP下げる？？
                        roundUsers.get(myID).HP -= 1;
                        room.Publish(new DamagedRes(myID, roundUsers.get(myID).HP));
                    }
                }

            }
        }
    }

   public synchronized void handleMoveReq(MoveReq moveReq, int userID){
        // TODO: MoveReqの時の処理を記述する。
        // TODO: 当たり判定を実装するなら当たり判定とか? ダメージを受ける場合はroom.PublishでDamagedResを送信する。(下のMoveResを参照) 
        roundUsers.get(userID).position.MoveTo( moveReq.x, moveReq.y ); 
        MoveRes moveRes = new MoveRes(userID, moveReq.x, moveReq.y);
        room.Publish( moveRes );
        //メソッドを使用する
        /* 当たり判定(ユークリッド距離) */
      

        // room.Publish(new DamagedRes(UserID, HP));

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
