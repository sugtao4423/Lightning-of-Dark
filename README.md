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

## ユーザーストリーム
TwitterのUserStream廃止により[疑似機能を実装](https://github.com/sugtao4423/Lightning-of-Dark/commit/8974dda0559c2db9b6b9f0a27aa856cf8ff3340d)  
ホームTLをそのまま使う場合、自動取得間隔は60秒以上限定  
リストをホームTLとして使う場合は自動取得間隔を1秒から設定可能だが、TweetDeckと同じ4秒程度が望ましい

## 使ってみたい方へ
/res/values/strings.xml  
こいつに"CK"と"CS"というnameのStringちゃんを追加してConsumerKeyとConsumerSecretを入力してください  
具体的にソースに追加するのは以下です

    <string name="CK">ConsumerKey</string>
    <string name="CS">ConsumerSecret</string>
