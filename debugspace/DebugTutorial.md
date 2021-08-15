# JISD Debug Tutorial

## 目次
[1. Debuggerインスタンスの作成](#1.)  
[2. JPDAを用いたデバッグ](#2.)  
[3. ProbeJを用いたデバッグ](#3.)  
[4. 観測値の利用](#4.)  
[5. 静的情報の取得](#5.)  
[6. 外部プログラムの実行](#6.)  
[7. 観測値のエクスポート](#7.)  

<a id="1."></a>
## 1. Debuggerインスタンスの作成
debug.Debuggerクラスには大きく分けて4種類のデバッグ方法を提供します．
1. デバッグ対象のプログラムを**内部**で起動し，**JPDA**を用いてデバッグする場合(Windows，Linux)
  ```java
  Debugger(String main, String options)
  ```


2. デバッグ対象のプログラムを**外部**で起動し，**JPDA**を用いてデバッグする場合(Windows，Linux)
  ```java
  Debugger(String host, int port)
  ```


3. デバッグ対象のプログラムを**ProbeJ**を付加して**内部**で起動し，デバッグする場合(Windowsのみ)
  ```java
  Debugger(String main, String options, true)
  ```


4. デバッグ対象のプログラムを**ProbeJ**を付加して**外部**で起動し，デバッグする場合(Windows，Linux)
  ```java
  Debugger(String host, int port, true)
  ```


各引数の説明は以下の通りです．
- main: mainメソッドをもつクラス名
  - 例: "jisd.demo.HelloWorld"
- options: javaコマンドにおける起動時のオプション(クラスパス等)
  - 例: "-cp sample"
- host: 接続先ホスト名
  - 例: "127.0.0.1"
- port: 接続先ポート名
  - 例: 8080

3.はWindowsのみで使用可能，それ以外はLinux, Windows両対応となっています．  
ただし，4.はプログラム起動側のOSはWindowsのみという制約がありますが，デバッグ側のOSはLinux, Windows両対応となっています．

<a id="2."></a>
## 2. JPDAを用いたデバッグ
JPDAとはJava Platform Debugger Architectureの略で，Javaプログラムのデバッグを行うための枠組みです．  
JPDAは以下の3つから構成されます．
- JVMTI(Java VM Tool Interface)
  - 実行中のJavaプログラムの内部状態観測・実行制御を行うインタフェース
- JDWP(Java Debug Wire Protocol)
  - 対象プログラムとデバッガのプロセス間で行われる通信プロトコル
- JDI(Java Debug Interface)
  - デバッグアプリケーションを容易に記述することができるインターフェース

JISDはこれらを用いることでブレークポイントの設定やステップ実行等，基本的なデバッグ操作を提供します．  
以下の各セクションでは，JPDAを用いた基本的なデバッグ操作について説明します．  


### 2.1 デバッガの起動と終了
JPDAを用いたデバッグでは，デバッガを起動すると, 内部で起動すべき対象プログラムがあるならばそれが起動されてから，デバッグイベント処理が開始されます．  

2.2以降で説明する`観測ポイントの設定`操作はデバッガ起動の前でも後でもよく，いずれの場合もデバッグ対象のクラスがロードされるまで設定が遅延されます．

たとえば，以下のようにDebuggerを生成したとします．


```Java
var dbg = new Debugger("jisd.demo.HelloWorld", "-cp ../sample")
```

以下のコードによりデバッガを起動します．


```Java
dbg.run(1000)
```

run()の引数には対象プログラムが観測ポイントに到達するまでの大まかな時間をミリ秒で指定します．上記の場合はデバッガ起動から1秒後にスレッドが再開されます．

また，デバッグを終了する場合は以下を実行します．


```Java
dbg.exit()
```

デバッグを終了するとデバッグイベント処理が停止され，対象プログラムが終了します．

### 2.2 観測ポイントの設定
JISDのJPDAを用いたデバッグでは，2種類の観測ポイントを提供します．

- ブレークポイント
- ウォッチポイント  

**ブレークポイント**が設定された場合，その観測ポイントに到達した時点で対象プログラムを一時停止し，その場で変数の値の観測やステップ実行等を行うことができます．同時に，観測ポイントから観測可能な変数の情報(値含む)がDebugResultオブジェクトとして生成されます．  
一方，**ウォッチポイント**が設定された場合，その観測ポイントに到達した時点で対象プログラムを一時停止しますが観測ポイントから観測可能な変数の情報(値含む)の取得とDebugResultオブジェクトの生成後．**直ちに対象プログラムを再開させます**． 

どの変数に対してDebugResultオブジェクトを生成するかはユーザによる指定が可能です．

具体的な設定方法を以下で説明します．

### 2.3 行番号による設定

先ほど作成したDebuggerインスタンス`dbg`に対して`jisd.demo.HelloWorldクラス`の`28行目`に観測ポイントを設定したい場合は，
以下のいずれかのように設定することができます．

- ブレークポイントを設定したい場合


```Java
Optional<Point> bp = dbg.stopAt("jisd.demo.HelloWorld", 28)
```

- ウォッチポイントを設定したい場合


```Java
Optional<Point> wp = dbg.watch("jisd.demo.HelloWorld", 28) 
```

ブレークポイントもウォッチポイントもPointクラスを継承したBreakPointクラスのインスタンスとして扱われることに注意してください．また，ある行に重複して観測ポイントを設定することはできません．

一方，特定の変数のみに注目してDebugResultオブジェクトを生成したい場合があります．  
たとえば，`jisd.demo.HelloWorldクラス`の`28行目`の`変数a`に`ウォッチポイント`を設定したい場合は，


```Java
String[] vars = {"a"};
dbg.watch("jisd.demo.HelloWorld", 28, vars);
```

のように設定します(ブレークポイントも同様)．

### 2.4 メソッド名による設定
行番号ではなくメソッドの先頭に観測ポイントを設定したい場合があります．   
たとえば，`jisd.demo.HelloWorldクラス`の`sayHello()`の先頭にブレークポイントを設定したい場合は，


```Java
dbg.stopAt("jisd.demo.HelloWorld", "sayHello")
```

のように設定します(ウォッチポイントも同様)．

`jisd.demo.HelloWorldクラス`の`sayHello()`の先頭にブレークポイントを設定し，`変数a`と`変数b`を観測したい場合は，


```Java
String[] vars = {"a", "b"};
dbg.stopAt("jisd.demo.HelloWorld", "sayHello", vars);
```

のように設定します(ウォッチポイントも同様)．

### 2.5 デフォルトクラス名
上記のいずれの場合も，クラス名を省略することができ，その場合デフォルトクラス名としてDebuggerの`main`フィールドが参照されます．
`main`フィールドは，Debuggerインスタンスの作成時の第一引数で指定したクラス名で初期化されます.これまでの例に登場した`dbg`では`"jisd.demo.HelloWorld"`がそれに該当します．

第一引数でクラス名を指定しない場合(portを指定した場合)は空のStringで初期化されます．Debugger生成後，setMain()メソッドで後から`main`フィールドを指定でき，デフォルトクラス名として利用できます．  
以下はデフォルトクラス名を"jisd.demo.HelloWorld"に設定した例です．


```Java
dbg.setMain("jisd.demo.HelloWorld");
```

### 2.6 ブレーク時の操作
JISDは実行の再開，ステップ実行等のブレーク時特有の操作を提供します．ブレーク時以外に以下の操作を行った場合，その操作は無視されます．

#### 2.6.1 実行の再開


```Java
dbg.cont()
```

#### 2.6.2 step into


```Java
dbg.step()
```

#### 2.6.3 step over


```Java
dbg.next()
```

#### 2.6.4 step out


```Java
dbg.finish()
```

#### 2.6.5 ソースコード上の実行箇所の表示


```Java
dbg.list()
```

#### 2.6.6 観測可能な変数の一覧表示


```Java
dbg.locals()
```

#### 2.6.7 スタックトレースの表示


```Java
dbg.where()
```

### 2.7 遠隔デバッグ
JPDAを用いた遠隔デバッグは，`1. Debuggerインスタンスの作成`の項で説明したデバッグ方法のうち，`2.`に該当し，ユーザが手元でエージェントを付加して起動したプログラムに対してデバッグを行います．注意として，遠隔デバッグを行う際はデバッグ情報を付加してコンパイルするようにしてください．

以下はデバッグ対象のプログラムの実行例です．
```bash
java -agentlib:jdwp=transport=dt_socket,server=y,address=12345,suspend=n -cp bin jisd.demo.HelloWorld
```

これに対し，ユーザは`1.`の章の`デバッグ方法2.`で説明したようにDebuggerインスタンスを生成します．


```Java
var dbg = new Debugger("host.docker.internal", 12345)
```

"host.docker.internal"はDocker Desktopで使用可能なdockerコンテナ上でのホストのアドレスで，これによりdockerコンテナ上に構築したJISDLab環境からホストで実行されているプログラムに接続することができます．

また，ホスト名を省略した場合，ホスト名に"localhost"が設定されます．


```Java
var dbg = new Debugger(12345) // host = "localhost"
```

<a id="3."></a>
## 3. ProbeJを用いたデバッグ
ProbeJとは実行中のプログラムを観測するツールです．内部でJVMTIを用いており，JVMの起動時にエージェントとして指定することで利用できます．以下はProbeJを付加してJavaプログラムを実行する例です．
```bash
java -agentpath:lib/ProbeJ_ex.dll=options_none:39876 -cp bin jisd.demo.LoopN
```

ProbeJは以下のような特徴があります．
- 利用者が指定した情報だけを収集する
- 観測中にブレークポイントのセットやクリア,観測結果の取得が可能である
- プログラムの実行への影響を最小限にとどめる
- プログラムの実行を継続したまま（短時間の停止だけで）観測を行う

ProbeJを用いたときの最大の利点は**プログラムの実行への影響を最小限にとどめられる**ことです．
また，プログラムの実行を継続したまま観測を行うため，実行中のプログラムのデバッグに向いています．

現状ProbeJはWindowsのみで提供されており，その他のOSではProbeJを付加した起動は不可となっています．  
一方，Windows上でProbeJを付加して起動したプログラムを同一ホスト上またはその他のOSからデバッグすることは可能です．

3.ではProbeJを用いたデバッグについて説明します．

### 3.1 デバッガの起動と終了

ProbeJを用いたデバッグでは，デバッガを起動すると，必要ならばProbeJを付加して対象プログラムを起動し，その後,ProbeJとの通信が開始されます．**内部でProbeJを付加した対象プログラムを起動する場合**，JISDのメインスレッドは対象プログラムを起動し通信を開始するまでの間，**必ず1秒待機**します．なお1秒のうち0.8秒は対象プログラムの起動待機時間としてあらかじめ設定されています．なお，外部でProbeJを付加した対象プログラムを起動している場合は待機せず，直ちに通信が開始されます．

以下のようにDebuggerを生成します．


```Java
var dbg = new Debugger("jisd.demo.HelloWorld", "-cp ../sample", true) // Windows only
```

または，


```Java
var dbg = new Debugger("host.docker.internal", 39876, true) // Host: Windows only, Container: Windows or Linux
```

以下のコードによりデバッガを起動します．


```Java
dbg.run(1000)
```

run()の引数には対象プログラムが観測ポイントに到達するまでの大まかな時間をミリ秒で指定します．上記の場合はrun()開始時から，(ProbeJとの通信開始までの待機時間1秒)+(ユーザ指定の1秒)=2秒後にメインスレッドが再開されます．

また，デバッグを終了する場合は以下を実行します．


```Java
dbg.exit()
```

デバッグを終了するとProbeJとの通信が停止されますが，対象プログラムは終了しないことに注意してください．


### 3.2 観測ポイントの設定
JISDのProbeJを用いたデバッグでは，1種類の観測ポイントである**プルーブポイント**を提供します．

**プルーブポイント**が設定された場合，その観測ポイントに到達すると変数の値の観測のみを行い，最大100件がProbeJ側で保持されます．

ProbeJを用いたデバッグでは，どの変数に対して観測ポイントを設定するかを指定する必要があります．

具体的な設定方法を以下で説明します．

### 3.3 ProbePointの設定

3.1で作成したDebuggerインスタンス`dbg`に対して`jisd.demo.Subクラス`の`20行目`の`変数a`と`変数b`にプルーブポイントを設定したい場合は，以下のように設定することができます．


```Java
String[] vars = {"a", "b"};
Optional<Point> pp = dbg.watch("jisd.demo.HelloWorld", 20, vars);
```

プルーブポイントはPointクラスを継承したProbePointクラスのインスタンスとして扱われます．また，ある行に重複して観測ポイントを設定することはできません．

プルーブポイントはブレークポイントやウォッチポイントとは異なり，後述のgetResults()を呼び出すまでDebugResultオブジェクトが生成されません．getResults()が呼び出されるまではProbeJが観測値を最大100件保持しています．

JPDAを用いたデバッグ同様，デフォルトクラス名が使用できます．(→2.5)

<a id="4."></a>
## 4. 観測値の利用

### 4.1 観測値の取得
観測情報はある行の1つの変数に対して1つのDebugResultオブジェクトとして提供され，
複数の値情報(ValueInfoオブジェクト)を含んでいます．

ある観測ポイント`p`から`変数a`の観測情報を以下のように取得できます．


```Java
Optional<DebugResult> dr = p.getResults("a")
```

また，ある観測ポイント`p`から観測可能なすべての変数の観測情報を取得するには以下のようにします．


```Java
HashMap<String, DebugResult> drs = getResults()
```

DebugResultには複数の値情報が含まれている可能性があり，個々の値はValueInfoオブジェクトに格納されています．

あるDebugResultオブジェクト`result`から直近の観測値を取得するには，以下のようにします．


```Java
ValueInfo vi = result.getLatestValue();
String value = vi.getValue();
```

ValueInfoのgetValue()では観測値はすべてStringで返ることに注意してください．

また，すべての観測値を取得するには，以下のようにします．


```Java
ArrayList<ValueInfo> vis = result.getValues()
```

### 4.2 観測値の保持上限の設定
保持する観測値の数には上限が定められており，デフォルトでは100件となっていますが，以下のように変更可能です(保持上限を200に増やす例).なお，既に設定された観測ポイントには反映されません．


```Java
DebugResult.setDefaultMaxRecordNoOfValue(200)
```

また観測ポイントの変数ごとに保持上限を設定することも可能です(あるPoint `p`の変数`a`の保持上限のみを200に増やす例)．この場合，設定が観測ポイントに即時反映されます．


```Java
p.getResults("a").get().setMaxRecordNoOfValue(200)
```

### 4.3 Location情報の取得
あるDebugResultがどの行番号のどの変数についての情報なのか等を後から知りたい場合は，
以下のようにDebugResultからLocationオブジェクトを取り出します．


```Java
var loc = result.getLocation();
int lineNumber = loc.getLineNumber();
String varName = loc.getVarName();
```

### 4.4 配列・インスタンスの観測

配列やインスタンスを観測した場合，配列の要素やインスタンスの持つフィールド値を展開することができます．

あるAクラスのインスタンス`a`のもつ全フィールドの直近の観測値を取得したい場合は以下のようにします．


```Java
var dr = p.getResults("a").get();
var vi = dr.getLatestValue();
ArrayList<ValueInfo> fields = vi.ch();
```

ある配列`a`の持つ全要素の直近の観測値を取得したい場合も上記と同じコードになります．

現在，これらの機能はJPDAを用いたデバッグのみで提供されます．ProbeJを用いたデバッグの場合は`ch()`で空のArrayListが返ります．

<a id="5."></a>
## 5. 静的情報の取得
JISDの機能の一つとしてクラスやメソッド等の静的情報の提供があります．

まず，ソースディレクトリとクラスディレクトリを指定し静的情報をロードし，その後ユーザはそれらの静的情報にアクセスが可能となります．

以下のようにStaticInfoFactoryクラスを初期化します．このとき，静的情報がロードされ，binDir以下に`.jisd_static_data{number}`ディレクトリが作成されます．{number}には0から始まる連番が付与されます．


```Java
var sif = new StaticInfoFactory(".", "../sample") // set srcDir and binDir
```

この後，たとえば，jisd.demo.HelloWorldクラスに含まれるメソッドやフィールドを表示する場合は以下のようにします．


```Java
ClassInfo ci = sif.createClass("jisd.demo.HelloWorld")
```


```Java
ci.methodNames()
```


```Java
ci.fieldNames()
```

また，`jisd.demo.HelloWorld`クラスの`main`メソッド内のローカル変数`a`を観測するための観測ポイントを設置することができる行を知りたい場合は，


```Java
MethodInfo mi = ci.method("main(java.lang.String[])");
LocalInfo li = mi.local("a");
li.canSet();
```

のようにし，この結果よりユーザは`jisd.demo.HelloWorld`クラスの26行目から32行目のまでに観測ポイントを設置すればよいことが分かります．

<a id="6."></a>
## 6. 外部プログラムの実行

JISDには外部のプログラムを実行するには2種類の方法があります．  
JISDのstaticメソッドである`Utility.exec()`を使う方法とIJavaカーネルの`%exec`マジックを使う方法です．

`Utility.exec()`はstdout，stderr,終了コードを含んだString[]のOptional型が返り，ユーザは結果を再利用することができます．

- Utility.exec()を使う方法


```Java
exec("pwd")
```

- %execマジックを使う方法


```Java
%exec pwd
```

<a id="7."></a>
## 7. 観測値のエクスポート

JISDは観測値をリアルタイムでエクスポートする機能を有しています．現在エクスポート先としてサポートしているのは以下のツールです．

- Elasticsearch
- Prometheus

JISDのエクスポート先は**Elasticsearch**をおすすめします．なぜなら，観測値の更新頻度が高い場合，Prometheusでは値の取りこぼしが発生しやすいためです．Elasticsearchをエクスポート先に設定した場合，観測値の更新頻度が高くても，JISDが観測値をキャッシュしてから一定間隔でデータを送信するので値を取りこぼすことはありません．

ユーザは上記ツールからGrafanaやKibanaで，観測値を可視化することができます．このとき，各可視化ツールの表示間隔に注意してください．値観測の間隔があまりにも短い(一般に，10msより短い)とき，可視化ツールでうまく表示できない場合があります．JISDはあまりにも短い間隔での値観測を検出した場合警告を発しますが，値観測は通常通り行われます．
 
以下の各セクションでは，ElasticsearchとGrafanaを用いた観測値の可視化方法について説明します． 

以下ではElasticsearch環境とGrafana環境が必要となります．
これらの環境のデモは`JISDLab/JISDVis`内にあるdocker-compose.yamlに定義してあります．起動方法は`JISDLab/JISDVis/README.md`に従ってください．

### 7.1 Elasticsearchへのエクスポート

はじめに，観測対象となるプログラムを指定します．今回は`jisd.demo.BinarySearch`を観測対象に指定します．


```Java
var dbg = new Debugger("jisd.demo.BinarySearch", "-cp ../sample");
```

次に，Elasticsearchにデータをエクスポートするクラス`ElasticsearchExporter`のインスタンスを作成します．


```Java
// ElasticsearchExporter(host, port, name, timeLocale = "00:00")
var esExporter = new ElasticsearchExporter("http://elastic01", 9200, "sample");
```

このとき，ホスト名(host)，ポート番号(port)の他に，観測データ群に対して名前(name)をつける必要があります．このnameに指定したものがそのまま観測値の保存場所となるElasticsearchのid名となります．

この`esExporter`を観測対象と紐付けるには以下のようにします．


```Java
dbg.setExporter(esExporter);
```

観測値をエクスポートする準備が整いました．試しに`jisd.demo.BinarySearch`のbinarySearch()内の変数leftとrightの動きをエクスポートしてみましょう．


```Java
// 観測ポイントの設定
dbg.watch(19, new String[]{"left", "right"});
dbg.watch(25, new String[]{"left", "right"});
esExporter.run(10); // esExporterの起動(観測ポイントごとに10msだけsleepする)
dbg.run(3000); // Debuggerの起動(観測対象プログラムの起動)
esExporter.stop(); // esExporterの停止
```

観測ポイント設定後，esExporterを起動すると，停止するまで1秒ごとにElasticsearchにデータを送り続けます．esExporter.run()の引数に指定したmsだけ観測ポイントごとにsleepすることで負荷をかけることができ，値観測の間隔があまりにも短く可視化ツールでうまく表示できない場合に使用します．また，引数に何も指定しない場合は負荷をかけずに値観測を行うことができます．



### 7.2 Grafanaでの可視化

#### 7.2.1 データソースの追加
- HTTP
  - URL
    - ElasticsearchのURL(デモの場合，`http://elastic01:9200/`)を入力します
- Elasticsearch detailsの設定
  - Index name
    - `ElasticsearchExporter`インスタンス作成時に引数nameで指定した文字列を入力します
  - Time field name
    - `@timestamp`を入力します
  - Version
    - `7.0+`を選択します
    
最後に，Save&Testを押下して設定を保存します．

#### 7.2.2 ダッシュボードの追加
1. ダッシュボードの編集画面で新規ダッシュボードのデータソースに先ほど作成したデータソースを入力します
2. `Query`欄で`name="<変数名>"`と入力して表示したい変数名を指定します(デモの場合，`name="left"`と`name="right"`をそれぞれ別々のクエリに入力します)
3. `Metric`の右欄で`Average`，Select Field欄で`value`を選択します
4. ダッシュボードの編集画面の右上の`time ranges`欄で，`Last 5 minutes`を選択します

#### 7.2.3 値観測と可視化
再び，`7.1`に従って，上から実行し，値観測とデータのエクスポートを行います．その後，Grafanaに観測値が反映されます．

![](imgs/7-2-3-1.png "jisd.demo.BinarySearchの可視化")


```Java

```
