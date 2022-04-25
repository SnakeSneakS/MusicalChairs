package Client;

import java.util.ArrayList;

import java.awt.*;

import Client.GameObject.Player;
import Client.GameObject.Chair;


// ゲーム内の全オブジェクトを管理する
public class GameObjectController {

    ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Chair> chairs = new ArrayList<>();

    int numberOfPlayers = 0;
    GameFrame gf;

    // 椅子の円の半径
    int chairRadius = 50;
    // 椅子同士のマージン (2倍空く)
    int margin = 10;
    // プレイヤーの円の半径
    int playerRadius = 40;
    // 線の太さ
    int lineWidth = 2;


    GameObjectController (GameFrame gf) {
        this.gf = gf;
        // 線の太さの変更
        gf.gr2.setStroke(new BasicStroke(lineWidth));
    }

    Player addPlayer (String name, int ID) {
        numberOfPlayers++;
        Player p = new Player(name, ID, playerRadius, gf.gr2);
        players.add(p);

        // 椅子を配置。人数より１少ない
        if (numberOfPlayers > 1) chairs.add(new Chair(chairRadius));
        double theta = 2*Math.PI/chairs.size();
        double r = (chairRadius + margin)/Math.sin(theta/2);
        for (int i = 0; i < chairs.size(); i++) {
            chairs.get(i).setPosition((int)Math.round(gf.width/2 + r*Math.sin(theta * i)), (int)Math.round(gf.height/2 - r*Math.cos(theta * i)));
        }
        return p;
    }

    // ID でプレイヤーを返す。見つからなければ null を返す
    Player getPlayer (int ID) {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.ID == ID) return p;
        }
        return null;
    }

    // 全オブジェクトを描画
    void paint (Graphics g) {
        for (int i = 0; i < players.size(); i++) {
            players.get(i).paint(g);
        }
        for (int i = 0; i < chairs.size(); i++) {
            chairs.get(i).paint(g);
        }
    }
}
