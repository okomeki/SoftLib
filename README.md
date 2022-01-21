# SoftLib
簡単なデータ梱包構造などの一部
Simple Stream Packet

SoftLibABNF、SoftLibJSONで利用する部分程度の公開

## BASE64

3種類のBASE64っぽいフォーマットが読み書きできる割と速いものです

## Packet

可変長配列、のようなものを目指してみたらこうなった。

FrontPacket, BackPacketが頭と尻のようなもので双方でInputStream,OutputStreamっぽいものが使える。
FIFOでもLIFOでもできるような抽象構造。
中身は配列のチェーンだがTEMPファイルなどにすると巨大化も期待できる。
