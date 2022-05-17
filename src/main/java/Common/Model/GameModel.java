package Common.Model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

public class GameModel {
    //Position
    @JsonTypeInfo(use = Id.CLASS)
    public static class Position{
        public Integer x;
        public Integer y;
        public Position(){}
        public Position(Integer x, Integer y){ this.x=x; this.y=y; }
        public void MoveTo(Integer x, Integer y){ this.x=x; this.y=y; }
        public void setPosition(Integer x, Integer y){ this.x=x; this.y=y; }
        @Override
        public String toString() {
            return "("+x.toString()+","+y.toString()+")";
        }
        public Double Distance(Position position){
            double diffX = x-position.x;
            double diffY = y-position.y;
            return Math.sqrt( diffX*diffX + diffY*diffY );
        }
    }
    
    //User 
    @JsonTypeInfo(use = Id.CLASS)
    public static class User{
        public Integer UserID;
        public String Username;
        public User(){}
        public User(Integer UserID, String Username){ this.UserID=UserID; this.Username=Username; }
    }


}
