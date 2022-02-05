# SoftLib
簡単なデータ梱包構造などの一部
Simple Stream Packet

SoftLibシリーズ化してきたのでまとめて1プロジェクトでMaven Centralで公開していく予定みたいなもの
RFCから拾って実装してみたものが多いかもしれません。

- SoftLib BASE64やPacketなどをまとめたものです。
- SoltLibABNF BNF,ABNF,EBNFのparser, CC。
- SoftLibRFC RFCのABNFを抽出したもの。HTTPなどの拡張にも対応。
- SoftLibJSON JSONのparser, Object Mapping, JSON PやJSON Bの実装。
- SoftLibREST RESTの周辺を組んでみる予定。
- SoftLibMIME mail系のエンコード、デコード
- SoftLibXML XML APIをオブジェクト指向っぽくまとめる方向
- SoftLibCrypt DES,AES,MessageDigest,SHA-1,SHA-2,SHA-3,HMACなど暗号系の実装 PKI系もあったかな
- SoftLibNature Nature RemoをつついてみるSoftLibRESTの実験。(未公開)
- SoftLibJ3BIF Mapping を利用してDBを自動生成してみたりする実験。PostgreSQLで。

JSONまではほどほどに使えますが、他は実験感覚で作っているもので足りない部分もあります。

# LICENSE

シリーズ通して Apache License 2.0 的な方向です。時々ヘッダに違うものが混ざっているのは古いだけですので両方だと思ってもいいです。

# Maven

~~~
<dependency>
    <groupId>net.siisise</groupId>
    <artifactId>softlib</artifactId>
    <version>1.1.4</version>
    <type>jar</type>
</dependency>
~~~
時々変わることがあるので特定バージョンを指定するか、SoftLibJSONなど使用したい機能経由で指定するのがおすすめです。

~~~
<version>[1.1.4,)</version>
~~~
などにするとそれ以降を指定できますね。SNAPSHOT版は安定しないこともあります。

# SoftLibの中身

簡単な機能の実装です

## BASE64

3種類のBASE64っぽいフォーマットが読み書きできる割と速いものです

## Packet

可変長配列、のようなものを目指してみたらこうなった。

FrontPacket, BackPacketが頭と尻のようなもので双方でInputStream,OutputStreamっぽいものが使える。
FIFOでもLIFOでもできるような抽象構造。
中身は配列のチェーンだがTEMPファイルなどにすると巨大化も期待できる。
BitStreamも扱えるようにしてみたが、まだ片方しか実装していない。
