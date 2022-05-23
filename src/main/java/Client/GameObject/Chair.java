package Client.GameObject;

import java.awt.*;

// 椅子
public class Chair extends GameObject {
    public static enum Mode{
        DEFAULT,
        CANSIT,
        ALREADYSIT,
    }

    private Mode mode = Mode.DEFAULT;

    public Chair (int chairRadius) {
        super(0, 0, 2*chairRadius, 2*chairRadius, false);
        SetMode(Mode.DEFAULT);
    }

    public void SetMode(Mode mode){
        this.mode = mode;
    }

    @Override
    public void paint (Graphics g) {
        // x, y を中心に描画する。x, y 座標はウィンドウの枠内の左上が起点。
        switch(mode){
            case DEFAULT:
                g.setColor(Color.gray);
                g.drawOval(x - width/2, y - width/2, width, height);
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
}


