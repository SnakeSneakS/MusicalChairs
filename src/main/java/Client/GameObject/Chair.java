package Client.GameObject;

import java.awt.*;

// 椅子
public class Chair extends GameObject {
    public Chair (int chairRadius) {
        super(0, 0, 2*chairRadius, 2*chairRadius, false);
        this.c = Color.gray;
    }

    @Override
    public void paint (Graphics g) {
        // x, y を中心に描画する。x, y 座標はウィンドウの枠内の左上が起点。
        g.setColor(c);
        g.drawOval(x - width/2, y - width/2, width, height);
    }
}