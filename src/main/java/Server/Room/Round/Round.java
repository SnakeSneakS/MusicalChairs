package Server.Room.Round;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
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
                            // room.Publish(new DamagedRes(UserID, HP))
                        //」を実行する;
                        // room.Publish(new DamegedRes(UserID, HP));
                    }
                }
            }.start();

            this.canSit=false;
            room.Publish(new PlayMusicRes(true)); 

            Random random = new Random(); 

            sleep(random.nextInt(20000) + 9000 );
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

    // public static class UserPosition{
    //     public Integer x1, x2;
    //     public UserPosition(){}
    //     public UserPosition(Integer x1, Integer y1){this.x1=x1; this.y1=y1;}
    //     //以下で椅子の座標を取得したい．．．とりあえず350
    //     //椅子との当たり判定
    //     public int x2, y2 = 350;
    //         public double Distance(UserPosition position){
    //             double getdistance;
    //             for(int i = 0; i < chairs.size(); i++){
    //                 double x2 = x1 - chairs.get(i).setPosition.x;
    //                 double y2 = y1 - chairs.get(i).setPosition.y;
    //                 getdistance = Math.sqrt(x2*x2+y2*y2);
    //                 return getdistance;
    //                 if (getdistance <= 40){
    //                     collision = true;
    //                      //HP下げる？？
    //                     this.HP -= 20;
    //                     room.Publish( moveRes );
    //                 }
    //             }
    //         }
    //          //プレイヤー同士の当たり判定
    //         public Double Distance(UserPosition position){
    //             double x3 = x1-roundUsers.get(userID).position.x;
    //             double y3 = y1-roundUsers.get(userID).position.y;
    //             double getdistance = Math.sqrt(x3*x3+y3*y3);
    //             return getdistance;
    //             if(getdistance <= 40){
    //                 collision = true;
    //                  //HP下げる？？
    //                 this.HP -= 20;
    //                 room.Publish( moveRes );
    //             }
    //         }
    // } 

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
