# Lightning of Dark
お試し製作Twitterクライアント  
なんかだんだんと常用クライアントにできるくらい使いやすくなってきてる  
開発者すげえなｗ  
頭のおかしい人じゃなくても使って、どうぞ。  
というかデバッグして落ちたらそこのLogCatのログとか俺に頂戴（王者の風格  
俺のTwitter垢にURLでもなんでも送ってくれればいいから（適当

## 概要
なんとなくTwitter関係に手を出してみたかった。  
私自身、色とかデザインとかのセンスが皆無なのでStSとするやつγのスクショから色を抽出して使わせて頂いています。

まだまだ製作・開発途中です。  
自分がどのように変更したかなどがわかりやすいようにGitHubに上げました。

## 使ってみたいとかいう頭のおかしい方へ
/res/values/strings.xml  
こいつに"CK"と"CS"というnameのStringちゃんを追加してConsumerKeyとConsumerSecretを入力してください  
具体的にソースに追加するのは以下です

    <string name="CK">ConsumerKey</string>
    <string name="CS">ConsumerSecret</string>

使ってみると動作がカクカクしてるのがすごいわかるゾ
