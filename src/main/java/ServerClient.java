import Client.GameFrame;
import Server.Server;

// Client と Server の main を実行するだけのクラス。
public class ServerClient implements Runnable {
    static String[] port = {"8080"};
    public static void main (String[] args) {
        // ポート番号を入れる配列
        Thread server = new Thread(new ServerClient());
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