# Lightning of Dark
お試し製作Twitterクライアント  
なんかだんだんと常用クライアントにできるくらい使いやすくなってきてる  
開発者すげえなｗ  
使って、どうぞ。  
というかデバッグして落ちたらそこのLogCatのログとか俺に頂戴（王者の風格  
俺のTwitter垢にURLでもなんでも送ってくれればいいから（適当

## 概要
なんとなくTwitter関係に手を出してみたかった。

まだまだ製作・開発途中です。  
自分がどのように変更したかなどがわかりやすいようにGitHubに上げました。

## マルチアカウント
[このCommit](https://github.com/sugtao4423/Lightning-of-Dark/tree/e10f6d80e89f9159e95ba101f2f3a82b506d38ea)から使えるように。  
SQLiteで管理しています。  
基本的にはSharedPreferencesに保存していて、アカウントを変える際などにSQLiteから読みだしてSharedPreferencesに保存してActivityを再起動という方法。

## 使ってみたい方へ
/res/values/strings.xml  
こいつに"CK"と"CS"というnameのStringちゃんを追加してConsumerKeyとConsumerSecretを入力してください  
具体的にソースに追加するのは以下です

    <string name="CK">ConsumerKey</string>
    <string name="CS">ConsumerSecret</string>
