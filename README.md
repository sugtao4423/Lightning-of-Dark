# Lightning of Dark
お試し製作Twitterクライアント  
なんかだんだんと常用クライアントにできるくらい使いやすくなってきてる  
開発者すげえなｗ  
使って、どうぞ。  
というかデバッグして落ちたらそこのLogCatのログとか俺に頂戴（王者の風格  
俺のTwitter垢にURLでもなんでも送ってくれればいいから（適当

## 概要
なんとなくTwitter関係に手を出してみたかった。  
私自身、色とかデザインとかのセンスが皆無なのでStSとするやつγのスクショから色を抽出して使わせて頂いています。  
[このCommit](https://github.com/sugtao4423/Lightning-of-Dark/commit/220aae4bfacf883f823890bdea8dd3aa98dd0396)からするやつγの画像データまで使ってしまっています。（ユーザーページのツイート数が表示されている場所（これはそのうち変更する予定。さすがにするやつの作者様に申し訳ない）  
apkの公開などはしていませんが、Githubに上げてしまっているのでもし作者様がお気づきになり、使用しないで欲しいということや、お説教などありましたら[私のTwitterアカウント](https://twitter.com/sugtao4423)のほうにご連絡頂けると幸いです。

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
