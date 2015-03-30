# Lightning of Dark
お試し製作Twitterクライアント

## 概要
なんとなくTwitter関係に手を出してみたかった。  
私自身、色とかデザインとかのセンスが皆無なのでStSのスクショから色を抽出して使わせて頂いています。

まだまだ製作・開発途中です。  
自分がどのように変更したかなどがわかりやすいようにGitHubに上げました。

## 使ってみたいとかいう頭のおかしい方へ
/res/values/strings.xml  
こいつに"CK"と"CS"というnameのStringちゃんを追加してConsumerKeyとConsumerSecretを入力してください  
具体的にソースに追加するのは以下です

    <string name="CK">ConsumerKey</string>
    <string name="CS">ConsumerSecret</string>

使ってみると動作がカクカクしてるのがすごいわかるゾ
