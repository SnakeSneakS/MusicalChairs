// 問題点
// ・画質が悪い (高dpiとの関連性が疑われる)
// ・謎ぬるぽ
// UIDしゅとく

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Color; // https://www.javadrive.jp/tutorial/color/

// 描画担当
public class GameFrame extends JFrame {
    // ウィンドウの内側の高さと幅
    final int height = 700;
    final int width = 700;
    final GameObjectController goc;
    final int fps = 60;
    // 仮想スクリーン http://www.f.waseda.jp/sakas/java/JavaGraphics.html
    final Image img;
    // imgのGraphics2D
    final Graphics2D gr2;
    final int myID = 0;
    final Client client;

    GameFrame () {
        // 詳細は https://www.javadrive.jp/tutorial/jframe/
        super("椅子取りゲーム");
        // デフォルトでは不可視なので window を見えるようにする
        setVisible(true);
        // ウィンドウサイズの指定
        setSize(width + getInsets().left + getInsets().right, height + getInsets().top + getInsets().bottom);
        // サイズ変更の不許可
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // 背景を黒に
        getContentPane().setBackground(Color.black);
        // img取得
        img = this.createImage(width, height);
        gr2 = (Graphics2D) img.getGraphics();
        // アンチエイリアス。https://nompor.com/2017/12/06/post-1572/
		// 図形や線のアンチエイリアシングの有効化
		gr2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// 文字描画のアンチエイリアシングの有効化
		gr2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // カーソル非表示
        // setCursorInvisible();

        // オブジェクト生成
        goc = new GameObjectController(this);

        // Client 起動
        client = new Client(this, 8080);

        // eventListener
        Input m = new Input(this);
        addMouseListener(m);
        addMouseMotionListener(m);
        MyWindowListener mwl = new MyWindowListener(this);
        System.out.println(getWindowListeners());
        addWindowListener(mwl);

        { // テスト用
            goc.addPlayer("一郎", 0);
            goc.addPlayer("二郎", 1);
            goc.addPlayer("三郎", 2);
            goc.addPlayer("四郎", 3);

            goc.getPlayer(0).setPosition(100, 100);
            goc.getPlayer(1).setPosition(200, 100);
            goc.getPlayer(2).setPosition(300, 100);
            goc.getPlayer(3).setPosition(400, 100);
        }

        // アニメーション描画
        run();
    }

    private void run () {
        while (true) {
            // 描画。repaint() が呼ばれると update(), paint() も呼ばれる。
            repaint();
            try {
                // お休み
                Thread.sleep(1000/fps);
            } catch (Exception e) {
                System.out.println("Error: Exception occurred at sleep, run(), GameFrame.");
            }
        }
    }

    // ちらつき防止 http://www.f.waseda.jp/sakas/java/JavaGraphics.html
    @Override
    public void update (Graphics g) {
        paint(g);
    }

    @Override
    public void paint (Graphics g) {
        // http://www.f.waseda.jp/sakas/java/JavaGraphics.html 参照。
        // 上塗り
        gr2.setColor(Color.black);
        gr2.fillRect(0, 0, width, height);

        // オブジェクト描画
        goc.paint(gr2);
        // 仮想スクリーンを実スクリーンに焼く
        g.drawImage(img, getInsets().left, getInsets().top, this);
    }

    // カーソルを非表示に
    private void setCursorInvisible () {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_4BYTE_ABGR);  
        Graphics2D g2 = image.createGraphics();  
        // 黒で透明 black & transparency  
        g2.setColor(new Color(0,0,0,0));     
        g2.fillRect(0,0, 16,16);  
        g2.dispose();  
        getContentPane().setCursor(Toolkit.getDefaultToolkit().createCustomCursor(  
                        image, new Point(0,0), "null_cursor"));  
    }

    public static void main (String[] args) {
        // フレーム作成
        new GameFrame();
    }
}

// 通信担当
class Client {
    GameFrame gf;
    Socket socket;
    BufferedReader in;
    PrintWriter out;

    Client (GameFrame gf, int port) {
        this.gf = gf;
        try {
            InetAddress addr = InetAddress.getByName("localhost");
            System.out.println("addr = " + addr);
            socket = new Socket(addr, port); 
            System.out.println("socket = " + socket);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            /*
            for (int i = 0; i < 10; i++) {
                out.println("howdy " + i); 
                String str = in.readLine();
                System.out.println(str);
            }
            out.println("END"); */
        } catch (Exception e) {
            System.out.println("error: Server との接続に失敗しました。");
            // System.exit(1);
        }
    }

    void sendPosition (int x, int y) {
        out.println(String.format("[Position] %d %d %d", gf.myID, x, y));
    }

    void closeSocket () {
        try {
            if (socket != null) socket.close(); 
        } catch (IOException e) {
            System.out.println("error: socketをcloseできませんでした");
        }
        System.out.println("Socket closed.");
    }
}

// カーソル位置 http://www.maroon.dti.ne.jp/koten-kairo/works/Java3D/mouse3.html
// クリック http://www.maroon.dti.ne.jp/koten-kairo/works/Java3D/mouse3.html
class Input extends MouseAdapter {
    GameFrame gf;

    Input (GameFrame gf) {
        this.gf = gf;
    }

    @Override
    public void mouseClicked (MouseEvent e) {
        gf.client.sendPosition(-100, -100);
    }

    @Override
    public void mouseMoved (MouseEvent e) {
        gf.goc.getPlayer(gf.myID).setPosition(e.getX() - gf.getInsets().left, e.getY() - gf.getInsets().top);
        gf.client.sendPosition(e.getX(), e.getY());
    }
}

class MyWindowListener extends WindowAdapter {
    GameFrame gf;
    MyWindowListener (GameFrame gf) {
        this.gf = gf;
    }

    @Override
    public void windowActivated (WindowEvent e) {
        System.out.println("activated");
    }

    @Override
    public void windowClosed (WindowEvent e) {
        System.out.println("aaa");
        gf.client.closeSocket();
        System.exit(0);
    }
}

// ゲーム内の全オブジェクトを管理する
class GameObjectController {

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
    // フォント https://techhot.hatenablog.com/entry/drawstringcentermiddle_1
    Font nameFont = new Font("SansSerif" , Font.PLAIN, playerRadius/2);
    Color nameColor = Color.white;
    Font HPFont = new Font("SansSerif" , Font.BOLD , playerRadius);
    Color HPColor = Color.black;

    GameObjectController (GameFrame gf) {
        this.gf = gf;
        // 線の太さの変更
        gf.gr2.setStroke(new BasicStroke(lineWidth));
    }

    Player addPlayer (String name, int ID) {
        numberOfPlayers++;
        Player p = new Player(name, ID, this);
        players.add(p);

        // 椅子を配置。人数より１少ない
        if (numberOfPlayers > 1) chairs.add(new Chair(this));
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

// ゲーム内オブジェクトの抽象クラス
abstract class GameObject {
    // オブジェクトの色
    Color c;
    // 淵だけか塗りつぶすか。今のところ使ってない
    boolean isFilled;
    GameObjectController goc;
    int x = 0, y = 0, height = 100, width = 100;
    GameObject (int x, int y, int height, int width, boolean isFilled, GameObjectController goc) {
        this.x = x; this.y = y; this.height = height; this.width = width; this.isFilled = isFilled; this.goc = goc;
    }

    void setPosition (int x, int y) {
        this.x = x; this.y = y;
    }

    // this を描画
    abstract void paint (Graphics g);
}

// プレイヤー
class Player extends GameObject {
    private String name;
    int ID;
    int HP = 100;

    Player (String name, int ID, GameObjectController goc) {
        super(0, 0, 2*goc.playerRadius, 2*goc.playerRadius, true, goc);
        this.name = name; this.ID = ID;
        this.c = getColor(ID);

        // サーバーから座標を受信する
    }

    @Override
    void paint (Graphics g) {
        // 色指定
        g.setColor(c);
        // x, y を真ん中にして描画。getInset はウィンドウ枠の幅。
        g.fillOval(x - width/2, y - width/2, width, height);

        // 文字描画 https://techhot.hatenablog.com/entry/drawstringcentermiddle_1
        // HP
        g.setColor(goc.HPColor);
        g.setFont(goc.HPFont);
        FontMetrics fm = g.getFontMetrics();
        Rectangle rect = fm.getStringBounds(String.valueOf(HP), g).getBounds();
        g.drawString(String.valueOf(HP), x - rect.width/2, y - rect.height/2 + fm.getMaxAscent());
        // name
        g.setColor(goc.nameColor);
        g.setFont(goc.nameFont);
        fm = g.getFontMetrics(); // 二回取得しないとおかしくなる。Font が変わるためと思われる
        Rectangle nameRect = fm.getStringBounds(String.valueOf(name), goc.gf.gr2).getBounds();
        g.drawString(name, x - nameRect.width/2, y - goc.playerRadius - fm.getDescent());
    }

    // ID からユニークな色を返す
    private Color getColor (int ID) {
        Color[] colors = {Color.red, Color.blue, Color.yellow, Color.green, Color.pink, Color.orange};
        if (ID >= colors.length) {
            System.out.println("error: ID >= colors.length です！");
            System.exit(1);
        }
        return colors[ID];
    }
}

// 椅子
class Chair extends GameObject {
    Chair (GameObjectController goc) {
        super(0, 0, 2*goc.chairRadius, 2*goc.chairRadius, false, goc);
        this.c = Color.gray;
    }

    @Override
    void paint (Graphics g) {
        // x, y を中心に描画する。x, y 座標はウィンドウの枠内の左上が起点。
        g.setColor(c);
        g.drawOval(x - width/2, y - width/2, width, height);
    }
}