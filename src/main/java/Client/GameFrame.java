package Client;

// 問題点
// ・画質が悪い (高dpiとの関連性が疑われる)
// ・謎ぬるぽ
// UIDしゅとく

import javax.swing.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.Color; // https://www.javadrive.jp/tutorial/color/

import Client.GameObject.Player;
import Client.Socket.Client;
import Client.Socket.Handler.JsonHandler;
import Common.Model.SocketModel.DamagedRes;
import Common.Model.SocketModel.GameEndRes;
import Common.Model.SocketModel.GameStartReq;
import Common.Model.SocketModel.GameStartRes;
import Common.Model.SocketModel.MatchStartReq;
import Common.Model.SocketModel.MatchStartRes;
import Common.Model.SocketModel.MoveRes;
import Common.Model.SocketModel.PlayMusicRes;
import Common.Model.SocketModel.RoomUsersInfoRes;
import Common.Model.SocketModel.RoundEndRes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
 
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

// 描画担当
public class GameFrame extends JFrame {
    final String hostname = "localhost";
    final int port = 8080;

    // ウィンドウの内側の高さと幅
    public static final int height = 700;
    public static final int width = 700;
    final GameObjectController goc;
    final int fps = 30; //60
    // 仮想スクリーン http://www.f.waseda.jp/sakas/java/JavaGraphics.html
    final Image img;
    // imgのGraphics2D
    final Graphics2D gr2;

    //my info
    public int myID = 0;
    public String myUsername = "my-name";
    public boolean isInRoom = false;

    final Client client;

    Clip clip = createClip(new File("resources/futta-festa.wav"));

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
        //setCursorInvisible();

        // オブジェクト生成
        goc = new GameObjectController(this);

        // Client 起動
        client = new Client(hostname, port){
            JsonHandler jsonHandler = new JsonHandler(){
                //マッチ開始した時に受信するメッセージに対する処理
                @Override 
                public void handleMatchStartRes(MatchStartRes matchStartRes) {
                    if(matchStartRes.isSuccess){
                        isInRoom=true;
                        myID=matchStartRes.UserID; 
                        addPlayer(myUsername, myID);
                        System.out.println("handleMatchStartRes: match succeeded!!\n");
                    }else{
                        System.err.println("handleMatchStartRes: match failed!!\n");
                    }
                }
                //ゲームが始まった時に受信するメッセージに対する処理
                @Override 
                public void handleGameStartRes(GameStartRes gameStartRes) {
                    //TODO 
                    if(!gameStartRes.isSuccess){
                        System.err.printf("handleGameStartRes: Game couldn't start!!\n");
                    }else{
                        System.out.printf("handleGameStartRes: GameStarted!!\n");
                    }
                }
                //ユーザーが移動した時に受信するメッセージに対する処理。 
                @Override 
                public void handleMoveRes(MoveRes moveRes) {
                    //TODO 移動した時のレスポンス 
                    Player player = goc.getPlayer(moveRes.UserID);
                    if(player!=null && moveRes.UserID!=myID) player.setPosition(moveRes.position.x- getInsets().left, moveRes.position.y-getInsets().top); 
                    //System.out.printf("handleMoveRes: UserID=%s, Position=%s\n", moveRes.UserID, moveRes.position);
                }
                //他のユーザーが減ったor増えた時に受信するメッセージ
                @Override 
                public void handleRoomUsersInfoRes(RoomUsersInfoRes roomUsersInfoRes) {
                    //TODO
                    int size=roomUsersInfoRes.usersInfo.length;
                    System.out.printf("handleRoomUsersInfoRes: size=%d\n", size); 
                    for(int i=0;i<size;i++){
                        addPlayer(roomUsersInfoRes.usersInfo[i].Username, roomUsersInfoRes.usersInfo[i].UserID);
                        System.out.printf("- UserID=%d, Username=%s\n", roomUsersInfoRes.usersInfo[i].UserID, roomUsersInfoRes.usersInfo[i].Username);
                    }
                }
                @Override
                public void handleDamagedRes(DamagedRes damagedRes) {
                    Player player = goc.getPlayer(damagedRes.UserID);
                    player.HP = damagedRes.HP; 
                    System.out.printf("handleDamagedRes: UserID=%s, HP=%s\n", damagedRes.UserID, damagedRes.HP);
                }
                @Override
                public synchronized void handleRoundEndRes(RoundEndRes roundEndRes) {
                    JFrame jFrame = new JFrame();

                    for(int j=0; j<goc.players.size();j++){
                        int checkID=goc.players.get(j).ID;
                        boolean isSurvived=false;
                        for(int i=0;i<roundEndRes.UserIDs.length;i++){
                            if(checkID==roundEndRes.UserIDs[i]){
                                isSurvived=true;
                                break;
                            }
                        }
                        if(!isSurvived){
                            goc.deletePlayer(checkID);
                            if(checkID==myID) JOptionPane.showMessageDialog(jFrame, "I'm loser");
                        }
                    }
                    // System.out.printf("]\n");
                }
                @Override
                public synchronized void handleGameEndRes(GameEndRes gameEndRes) {
                    JFrame jFrame = new JFrame();
                    if(goc.players.size()==1){
                        JOptionPane.showMessageDialog(jFrame, "Game END!! Winner is:"+goc.players.get(0).name);
                    }
                    else{
                        JOptionPane.showMessageDialog(jFrame, "Game END!! No Winner");
                    }
                }
             
                @Override
                public void handlePlayMusicRes(PlayMusicRes playMusicRes) {
                    if(playMusicRes.isPlay==true){
                        clip.start();
                        FloatControl ctrl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                        ctrl.setValue((float)Math.log10((float)0.6 / 20)*20);
                    }
                    else{
                        System.out.println("stopped");
                        clip.stop();
                    }
                    // System.out.printf("play music? %s\n", playMusicRes.isPlay);
                }
            };

            @Override
            protected void OnConnect() {
                super.OnConnect();
            }

            @Override
            protected void OnReceive(String line) {
                this.jsonHandler.handle(line);
                super.OnReceive(line);
            }

            @Override
            protected void OnClose() {
                super.OnClose();
            }
        };
        client.Connect();

        myUsername = getUsername();

        //matchStart
        //TODO: 名前を入力した後にmatchStartを呼び出すようにしたい。
        client.Send( new MatchStartReq(this.myUsername) ); //名前を送ってその名前でマッチするようにサーバーに申請する。 
        
        gamestartreq();

        // eventListener
        Input m = new Input(this);
        addMouseListener(m);
        addMouseMotionListener(m);
        MyWindowListener mwl = new MyWindowListener(this);
        System.out.println(getWindowListeners());
        addWindowListener(mwl);

        /*{ // テスト用
            goc.addPlayer("一郎", 0);
            goc.addPlayer("二郎", 1);
            goc.addPlayer("三郎", 2);
            goc.addPlayer("四郎", 3);

            goc.getPlayer(0).setPosition(100, 100);
            goc.getPlayer(1).setPosition(200, 100);
            goc.getPlayer(2).setPosition(300, 100);
            goc.getPlayer(3).setPosition(400, 100);
        }*/

        // アニメーション描画
        run();
    }
    
    //wavファイル用意
    public Clip createClip(File path){
        try (AudioInputStream wav = AudioSystem.getAudioInputStream(path)){

            //ファイルの形式取得
            AudioFormat format = wav.getFormat();
            
            //単一のオーディオ形式を含む指定した情報からデータラインの情報オブジェクトを構築
            DataLine.Info dataLine = new DataLine.Info(Clip.class,format);
            
            //指定された Line.Info オブジェクトの記述に一致するラインを取得
            Clip c = (Clip)AudioSystem.getLine(dataLine);
            
            //再生準備完了
            c.open(wav);
            
            return c;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    } 
        

    private void addPlayer(String username, int id){
        if(goc.getPlayer(id)!=null){
            return;
        }else{
            goc.addPlayer(username, id);
        }
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
    
    //ユーザー名を入力させるポップアップ
    public static String getUsername(){
        JFrame jFrame = new JFrame();
        return JOptionPane.showInputDialog(jFrame, "Enter your username");
    }

    public void gamestartreq(){
        JFrame jFrame = new JFrame();
        int result = JOptionPane.showConfirmDialog(jFrame, "Do you want to start a game?");

        if (result == 0){
            client.Send( new GameStartReq() );
            System.out.println("You pressed Yes");
        }
        else if (result == 1){
            System.out.println("You pressed NO");
            System.exit(1);
        }
        else{
            System.out.println("You pressed Cancel");
            System.exit(1);
        }
    }
       

    public static void main (String[] args) {
        final int clientNum = 2; //幾つのクライアントを動作させるか 
        Thread[] t = new Thread[clientNum];
        for(int i=0;i<clientNum;i++){
            t[i] = new Thread(){
                @Override
                public void run() {
                    // フレーム作成
                    new GameFrame();

                }
            };
            t[i].start();
        }

        try{
            for(int i=0;i<clientNum;i++){
                t[i].join();
            }
        }catch(Exception e){
            System.err.println(e);
        }finally{
            System.out.println("Execution end.");
        }
    }
}

