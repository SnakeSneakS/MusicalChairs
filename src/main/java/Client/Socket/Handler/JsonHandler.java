package Client.Socket.Handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Common.Model.SocketModel;

public class JsonHandler {
    //test 
    /*
    public static void main(String[] args){
        JsonHandler jsonHandler = new JsonHandler();

        String json=jsonHandler.ToJson(new Model.TestReq("testMessage"));
        System.out.printf( "Json: %s\n", json );

        jsonHandler.handle(json);
    }
    */

    //ObjectをString(jsonデータ)に変換する 
    public String ToJson(Object obj){
        try{
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(obj);
            return json;
        }catch(Exception e){
            System.err.println(e);
            return "";
        }
    }

    //jsonデータに対し、処理を行う。
    public void handle(String json){
        //System.out.printf( "Handle json: %s\n", json ); //for debug
        
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(json);
            String jsonClass = jsonNode.get("@class").asText();
            //System.out.printf("Read Class: %s\n", jsonClass ); //for debug
    
            //Handle Data
            /*
            if(jsonClass.equals(SocketModel.TestRes.class.getName())){
                SocketModel.TestRes testReq = mapper.readValue(json, SocketModel.TestRes.class);
                handleTestReq(testReq);
            }
            else */
            if(jsonClass.equals(SocketModel.RoomUsersInfoRes.class.getName())){
                SocketModel.RoomUsersInfoRes roomUsersInfoRes = mapper.readValue(json, SocketModel.RoomUsersInfoRes.class);
                handleRoomUsersInfoRes(roomUsersInfoRes);
            }
            else if(jsonClass.equals(SocketModel.MatchStartRes.class.getName())){
                SocketModel.MatchStartRes matchStartRes = mapper.readValue(json, SocketModel.MatchStartRes.class);
                handleMatchStartRes(matchStartRes);
            }
            else if(jsonClass.equals(SocketModel.MoveRes.class.getName())){
                SocketModel.MoveRes moveRes = mapper.readValue(json, SocketModel.MoveRes.class);
                handleMoveRes(moveRes);
            }
            else if(jsonClass.equals(SocketModel.GameStartRes.class.getName())){
                SocketModel.GameStartRes gameStartRes = mapper.readValue(json, SocketModel.GameStartRes.class);
                handleGameStartRes(gameStartRes);
            }
            else if(jsonClass.equals(SocketModel.DamagedRes.class.getName())){
                SocketModel.DamagedRes damagedRes = mapper.readValue(json, SocketModel.DamagedRes.class); 
                handleDamagedRes(damagedRes);
            }
            else if(jsonClass.equals(SocketModel.GameEndRes.class.getName())){
                SocketModel.GameEndRes gameEndRes = mapper.readValue(json, SocketModel.GameEndRes.class); 
                handleGameEndRes(gameEndRes);
            }
            else if(jsonClass.equals(SocketModel.RoundEndRes.class.getName())){
                SocketModel.RoundEndRes roundEndRes = mapper.readValue(json, SocketModel.RoundEndRes.class); 
                handleRoundEndRes(roundEndRes);
            }
            else if(jsonClass.equals(SocketModel.PlayMusicRes.class.getName())){
                SocketModel.PlayMusicRes playMusicRes = mapper.readValue(json, SocketModel.PlayMusicRes.class);
                handlePlayMusicRes(playMusicRes);
            }
            else{
                System.err.printf("Unhandling class: %s\n", jsonClass);
            }
        }catch(Exception e){
            System.err.println(e);
        }
    }

    //Handle Request Data Functions
    /*
    public void handleTestReq(SocketModel.TestRes testRes){
        System.out.printf("handle data: %s\n", testRes );
    }
    */
    public void handleMatchStartRes(SocketModel.MatchStartRes matchStartRes){
        System.out.printf("handle data: %s\n", matchStartRes ); 
    }
    public void handleMoveRes(SocketModel.MoveRes moveRes){
        System.out.printf("handle data: %s\n", moveRes ); 
    }
    public void handleGameStartRes(SocketModel.GameStartRes gameStartRes){
        System.out.printf("handle data: %s\n", gameStartRes ); 
    }
    public void handleRoomUsersInfoRes(SocketModel.RoomUsersInfoRes roomUsersInfoRes){
        System.out.printf("handle data: %s\n", roomUsersInfoRes ); 
    }
    public void handleDamagedRes(SocketModel.DamagedRes damagedRes){
        System.out.printf("handle data: %s\n", damagedRes ); 
    }
    public void handleGameEndRes(SocketModel.GameEndRes gameEndRes){
        System.out.printf("handle data: %s\n", gameEndRes ); 
    }
    public void handleRoundEndRes(SocketModel.RoundEndRes roundEndRes){
        System.out.printf("handle data: %s\n", roundEndRes ); 
    }
    public void handlePlayMusicRes(SocketModel.PlayMusicRes playMusicRes){
        System.out.printf("handle data: %s\n", playMusicRes ); 
    }
}

