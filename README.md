# 椅子取りゲーム
これはJavaで作成したオンラインリアルタイム椅子取りゲームのプロジェクトファイルです。 

## Usage
### Java 
1. [Install gradle.](https://dev.classmethod.jp/articles/gradle-step-by-step/). 
    - どうしてgradleが必要なんですか <- 外部パッケージを使うのに便利そうだったから。 
2. ~~build: ```gradle build```~~ 開発時にはbuildせずに直接runで大丈夫なはず... 
3. run: ```gradle run``` 
4. if you want to clean, run ```gradle clean```. 
5. check [settings](#settings)

### Docker (If Server Only)
1. ```docker compose up``` 
2. ```localhost:8080```以外を使う場合は、```.env```ファイルの作成 (テンプレート: [.env.template](.env.template) )


## Settings
- プログラムファイルの場所: src/main/java
    - 以下、${program}と略記する。
- 環境変数として指定するHOSTNAMEとPORTの値が接続先のサーバになります。デフォルトではlocalhostの8080番ポートです．
- クライアントの実行数は[GameFrame.java](./src/main/java/Client/GameFrame.java)のclientNumの値を変えて設定してください． 
- クライアントのみ起動する、サーバのみ起動する、クライアントとサーバを両方とも起動する、などの設定変更はbuild.gradleのmainClassを変更してください。 
