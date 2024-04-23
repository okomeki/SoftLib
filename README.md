# SoftLib
簡単なデータ梱包構造などの一部
Simple Stream Packet

SoftLibシリーズ化してきたのでまとめて1プロジェクトでMaven Centralで公開していく予定みたいなもの
RFCから拾って実装してみたものが多いかもしれません。

- SoftLib BASE64やPacketなどをまとめたものです。
- SoftLibRebind データ変換するもの。
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

Java Module System JDK 11用
~~~
<dependency>
    <groupId>net.siisise</groupId>
    <artifactId>softlib.module</artifactId>
    <version>1.1.14</version>
    <type>jar</type>
</dependency>
~~~
JDK 8用
~~~
<dependency>
    <groupId>net.siisise</groupId>
    <artifactId>softlib</artifactId>
    <version>1.1.14</version>
    <type>jar</type>
</dependency>
~~~
時々変わることがあるので特定バージョンを指定するか、SoftLibJSONなど使用したい機能経由で指定するのがおすすめです。

リリース版 1.1.14 ぐらい。
次版 1.1.15-SNAPSHOT

~~~
<version>[1.1.8,)</version>
~~~
などにするとそれ以降を指定できますね。SNAPSHOT版は安定しないこともあります。

# SoftLibの中身

簡単な機能の実装です

https://developer.jp/softlib-1.1.14-SNAPSHOT-javadoc/

## BASE64, Base32, Base58

 - https://developer.jp/softlib-1.1.14-SNAPSHOT-javadoc/net/siisise/io/BASE64.html

6種類程度のBASE64っぽいフォーマットが読み書きできる割と速いものです

## Packet

- net.siisise.io.Packet https://developer.jp/softlib-1.1.14-SNAPSHOT-javadoc/net/siisise/io/Packet.html
- net.siisise.io.FrontPacket
- net.siisise.io.BackPacket
- net.siisise.io.PacketA

可変長配列、スタック構造、Stream込み込みのようなものを目指してみたらこうなった。

~~~
                   Top                    Last
                   ----------------------------
                   |FrontPacket    BackPacket|
                   |Read 読み  データ          |Write 追加
 逆書き BackWrite  |          ← 逆読みBackRead|
~~~
Packet の構造

FrontPacket, BackPacketが頭と尻のようなもので双方でInputStream,OutputStreamっぽいものが使える。
バイト列でFIFOでもLIFOでもできるような抽象構造。
中身は配列のチェーンだがTEMPファイルなどにすると巨大化も期待できる。
BitStreamも扱えるようにしてみたが、まだ片方しか実装していない。

## Block

基本的には固定サイズ Packet を2つ繋いで読み書きした分読み書き位置が移動する感じの実装

- net.siisise.block.Block 形
- net.siisise.block.ReadableBlock 読み専用
- net.siisise.block.ByteBlock byte[]配列の実装
- net.siisise.block.BufferBlock Bufferの実装
- net.siisise.block.OverBlock 上書き
- net.siisise.block.PacketBlock Packet 2つの実装
- net.siisise.block.SinglePacketBlock Packet 1つの実装

~~~
     Top              読み書き位置       末尾
     |           BackPacket|FrontPacket  |
     |                     |Read/Write→  |
     |  ←BackRead/BackWrite|             |
~~~

Packetから読んだ後に戻りたかったのでjava.nio の Buffer や Channel と互換性など考慮しながら拡張してみたらこうなった。

FrontPacket, BackPacket を byte[], nioのByteBufferなどの固定長ブロックで利用できるようにしたもの。
0 スタート mark なし position あり、 capacity と limit が同一(ReadableBlock, OverBlockは変更不可)。分割(メモリ空間共有)も可能。
ABNF Parser用に作ってみた機能。Packetでは先頭末尾にあった読み書き点がpositionの位置に変わる。
