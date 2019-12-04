OAuth2のResource Owner Password Credentials Grantのサンプル（JavaのSwingで作ったGUIアプリ)

# 注意点
- Swing、 `HttpClient` 、例外などの使い方がかなり雑になっています。真似しないでください。
- あくまで、OAuth2を理解するためのサンプルと捉えてください。

# 動作環境
- AdoptOpenJDK 11.0.5 (HotSpot VM)
    - JDK 12以上では動作確認していません
- Maven 3.6.0以上

# リソースサーバー
1. ソースを[GitHub](https://github.com/MasatoshiTada/oauth2-with-spring-security-51/tree/master/resource-server)から取得
2. `mvn clean spring-boot:run` で起動

# 認可サーバー
1. [Qiita記事](https://qiita.com/suke_masa/items/6b84826df81c083b384c)を参考にKeycloakをインストール・起動
2. src/main/resources/client.propertiesの `client_secret` の値は、適切な値に書き換えてください（[参考記事](https://qiita.com/suke_masa/items/6b84826df81c083b384c#クライアントの設定)）