package Client;

import java.awt.event.*;

import Client.GameObject.Player;
import Common.Model.GameModel.Position;
import Common.Model.SocketModel.MoveReq;

// カーソル位置 http://www.maroon.dti.ne.jp/koten-kairo/works/Java3D/mouse3.html
// クリック http://www.maroon.dti.ne.jp/koten-kairo/works/Java3D/mouse3.html
public class Input extends MouseAdapter {
    GameFrame gf;

    private Position mousePosition;

    Input (GameFrame gf) {
        this.gf = gf;
        this.mousePosition = new Position(0, 0);

        //clientが接続を確保している間、mouseMovedを送り続ける。
        Thread t = new Thread(){
            @Override
            public void run() {
                try{
                    while(true){
                        if(gf.client.IsConnected && gf.isInRoom){
                            MoveReq req = new MoveReq( mousePosition.x, mousePosition.y );
                            gf.client.Send( req );
                        }
                        if(gf.client.IsClosed){
                            break;  
                        }
                        sleep(1000/gf.fps);
                    }
                }catch(Exception e){
                    System.err.println(e);
                }
            }
        };
        t.start();
    }

    @Override
    public void mouseClicked (MouseEvent e) {
        //MoveReq req = new MoveReq( -100, -100 ); gf.client.Send( req );
    }

    @Override
    public void mouseMoved (MouseEvent e) {
        Player player = gf.goc.getPlayer(gf.myID);
        if(player!=null) player.setPosition(e.getX() - gf.getInsets().left, e.getY() - gf.getInsets().top);
        mousePosition.MoveTo(e.getX(), e.getY()); 
    }
}
