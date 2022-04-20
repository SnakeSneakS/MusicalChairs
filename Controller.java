import java.io.*;
import java.net.*;

// Client と Server の main を実行するだけのクラス。
public class Controller implements Runnable {
    static String[] port = {"8080"};
    public static void main (String[] args) {
        // ポート番号を入れる配列

        // 配列を渡して main を呼ぶ。
        Thread server = new Thread(new Controller());
        server.start();
        GameFrame.main(port);
    }

    @Override
    public void run() {
        try {
            Server.main(port);
        } catch (Exception e) {

        }
    }
}