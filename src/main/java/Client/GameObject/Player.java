package Client.GameObject;

import java.awt.*;
import java.awt.Color; // https://www.javadrive.jp/tutorial/color/
import java.awt.Graphics2D;


// プレイヤー
public class Player extends GameObject {

    public static enum Mode{
        DEFAULT,
        CANSIT,
        ALREADYSIT,
    }

    private static final Color nameColor = Color.white;
    private static final Color HPColor = Color.black;

    

    public String name;
    public int ID;
    public int HP = 100;
    public int playerRadius;
    private Graphics2D gr2;
    private Font nameFont;
    private Font HPFont;

    private Mode mode=Mode.DEFAULT;

    public Player (String name, int ID, int playerRadius, Graphics2D gr2) {
        super(0, 0, 2*playerRadius, 2*playerRadius, true);
        this.name = name; this.ID = ID;
        this.c = getColor(ID);
        this.playerRadius = playerRadius;
        this.gr2 = gr2;

        // フォント https://techhot.hatenablog.com/entry/drawstringcentermiddle_1
        this.nameFont = new Font("SansSerif" , Font.PLAIN, playerRadius/2);
        this.HPFont = new Font("SansSerif" , Font.BOLD , playerRadius);

        // サーバーから座標を受信する
    }

    @Override
    public void paint (Graphics g) {
        // 色指定
        g.setColor(c);
        // x, y を真ん中にして描画。getInset はウィンドウ枠の幅。
        g.fillOval(x - width/2, y - width/2, width, height);

        // 文字描画 https://techhot.hatenablog.com/entry/drawstringcentermiddle_1
        // HP
        g.setColor(HPColor);
        g.setFont(HPFont);
        FontMetrics fm = g.getFontMetrics();
        Rectangle rect = fm.getStringBounds(String.valueOf(HP), g).getBounds();
        g.drawString(String.valueOf(HP), x - rect.width/2, y - rect.height/2 + fm.getMaxAscent());
        // name
        g.setColor(nameColor);
        g.setFont(nameFont);
        fm = g.getFontMetrics(); // 二回取得しないとおかしくなる。Font が変わるためと思われる
        Rectangle nameRect = fm.getStringBounds(String.valueOf(name), gr2).getBounds();
        g.drawString(name, x - nameRect.width/2, y - playerRadius - fm.getDescent());

        switch(mode){
            case DEFAULT:
                //g.setColor(Color.white);
                //g.drawOval(x - width/2, y - width/2, width, height);
                break;
            case CANSIT:
                g.setColor(Color.yellow);
                g.drawOval(x - width/2, y - width/2, width, height);
                break;
            case ALREADYSIT:
                g.setColor(Color.green);
                g.drawOval(x - width/2, y - width/2, width, height);
                break;
            default:
                break;
        }
    }

    //modeを設定する
    public void SetMode(Mode mode){
        this.mode = mode;
    }

    // ID からユニークな色を返す
    private Color getColor (int ID) {
        Color[] colors = {Color.red, Color.blue, Color.yellow, Color.green, Color.pink, Color.orange};
        if (ID >= colors.length) {
            System.err.println("error: ID >= colors.length です！そのため6の剰余をとりあえず取ってみます。");
            //TODO ユニークな色を返す。これだとIDの6の剰余が等しいプレイヤーは同じ色になる。
            ID%=colors.length; 
        }
        return colors[ID];
    }
}
