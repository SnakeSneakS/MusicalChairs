package Server.Socket.Handler;

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
            if(jsonClass.equals(SocketModel.TestReq.class.getName())){
                SocketModel.TestReq testReq = mapper.readValue(json, SocketModel.TestReq.class);
                handleTestReq(testReq);
            }
            else if(jsonClass.equals(SocketModel.MatchStartReq.class.getName())){
                SocketModel.MatchStartReq matchStartReq = mapper.readValue(json, SocketModel.MatchStartReq.class);
                handleMatchStartReq(matchStartReq);
            }
            else if(jsonClass.equals(SocketModel.MoveReq.class.getName())){
                SocketModel.MoveReq moveReq = mapper.readValue(json, SocketModel.MoveReq.class);
                handleMoveReq(moveReq);
            }
            else if(jsonClass.equals(SocketModel.GameStartReq.class.getName())){
                SocketModel.GameStartReq gameStartReq = mapper.readValue(json, SocketModel.GameStartReq.class);
                handleGameStartReq(gameStartReq);
            }
            else{
                System.err.printf("Unhandling class: %s\n", jsonClass);
            }
        }catch(Exception e){
            System.err.println(e);
        }
    }

    //Handle Request Data Functions
    public void handleTestReq(SocketModel.TestReq testReq){
        //System.out.printf("handle data: %s\n", testReq );
    }
    public void handleMatchStartReq(SocketModel.MatchStartReq matchStartReq){
        //System.out.printf("handle data: %s\n", matchStartReq ); 
    }
    public void handleMoveReq(SocketModel.MoveReq moveReq){
        //System.out.printf("handle data: %s\n", moveReq ); 
    }
    public void handleGameStartReq(SocketModel.GameStartReq gameStartReq){
        //System.out.printf("handle data: %s\n", gameStartReq ); 
    }
}

