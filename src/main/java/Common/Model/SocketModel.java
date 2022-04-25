package Common.Model;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import Common.Model.GameModel.Position;

public class SocketModel {
    //Requestの基本。Client->Serverのリクエスト。 
    @JsonTypeInfo(use = Id.CLASS)
    protected static class Req{
        public Req(){}
    }

    //Responseの基本。Server->Clientのレスポンス。  
    @JsonTypeInfo(use = Id.CLASS)
    protected static class Res{ 
        public boolean isSuccess = true; 
        public Res(){}
        public Res(boolean isSuccess){ this.isSuccess = isSuccess; }
    }

    // For Test 
    public static class TestReq extends Req{
        public String message = "test";
        public TestReq(){}
        public TestReq(String message){ this.message = message; }
    }
    public static class TestRes extends Res{
        public String message = "test";
        public TestRes(){}
        public TestRes(String message){this.message=message;}
    }

    //マッチを開始して、部屋に入る。 
    public static class MatchStartReq extends Req{
        public String Username;
        public MatchStartReq(){} 
        public MatchStartReq(String Username){ this.Username=Username; }
    }
    public static class MatchStartRes extends Res{
        public int UserID;
        public MatchStartRes(){} 
        public MatchStartRes(int UserID){ this.UserID = UserID; }
    }

    //UserInfo
    public static class UserInfo{
        public int UserID;
        public String Username; 
        public UserInfo(){}
        public UserInfo(int UserID, String Username){ this.UserID=UserID; this.Username=Username; }
    }

    //部屋の中にいるユーザーの情報
    public static class RoomUsersInfoRes extends Res{
        public UserInfo[] usersInfo;
        public RoomUsersInfoRes(){}
        public RoomUsersInfoRes(UserInfo[] usersInfo){ this.usersInfo = usersInfo; }
    } 

    //ゲーム開始
    public static class GameStartReq extends Req{
        public GameStartReq(){}
    }
    public static class GameStartRes extends Res{
        public GameStartRes(){}
        public GameStartRes(boolean isSuccess){ this.isSuccess = isSuccess; }
    }

    //移動
    public static class MoveReq extends Req{
        public Integer x;
        public Integer y;
        public MoveReq(){} 
        public MoveReq(Integer x, Integer y){ this.x=x; this.y=y; }
    }
    public static class MoveRes extends Res{
        public int UserID; 
        public Position position;
        public MoveRes(){} 
        public MoveRes(int UserID, Integer x, Integer y){ this.UserID=UserID; this.position=new Position(x,y); }
    }

    //HPにダメージを受ける
    public static class DamagedRes extends Res{
        public int UserID;
        public int HP;
        public DamagedRes(){}
        public DamagedRes(int UserID, int HP){ this.UserID=UserID; this.HP=HP; }
    }

    //音楽の再生・停止
    public static class PlayMusicRes extends Res{
        public boolean isPlay; //if true, play music. else, stop music. 
        public PlayMusicRes(){}
        public PlayMusicRes(boolean isPlay){ this.isPlay=isPlay; }
    }

    //ラウンド終了
    public static class RoundEndRes extends Res{
        public int[] UserIDs; //生き残ったユーザーのid。ここに含まれていないユーザーは敗北扱いとなる。
        public RoundEndRes(){}
        public RoundEndRes(int[] UserIDs){this.UserIDs=UserIDs; }
    }

    //ゲーム終了。ユーザーが1人だけ生き残ったor全員生き残ったとき。
    //RoomEndの時に生き残ったユーザーのIDは全て伝えているためこれで問題ない 
    public static class GameEndRes extends Res{
        public GameEndRes(){} 
    }

    //ユーザーとの通信が切断されたとき
    public static class DisconnectedUserRes extends Res{
        public int UserID;
        public DisconnectedUserRes(){}
        public DisconnectedUserRes(int UserID){ this.UserID=UserID; } 
    }
}
