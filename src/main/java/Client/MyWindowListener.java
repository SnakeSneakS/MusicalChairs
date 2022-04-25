package Client;

import java.awt.event.*;

public class MyWindowListener extends WindowAdapter {
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
        gf.client.Close();
        System.exit(0);
    }
}
