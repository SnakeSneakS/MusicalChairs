package Client.GameObject;  

import java.awt.*;
import java.awt.Color;


// ゲーム内オブジェクトの抽象クラス
abstract class GameObject {
    // オブジェクトの色
    Color c;
    // 淵だけか塗りつぶすか。今のところ使ってない
    boolean isFilled;
    int x = 0, y = 0, height = 100, width = 100;
    protected GameObject (int x, int y, int height, int width, boolean isFilled) {
        this.x = x; this.y = y; this.height = height; this.width = width; this.isFilled = isFilled; 
    }

    public void setPosition (int x, int y) {
        this.x = x; this.y = y;
    }

    // this を描画
    abstract void paint (Graphics g);
}
