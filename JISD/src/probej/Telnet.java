package probej;

import java.net.*;
import java.io.*;

/**
 * @author sugiyama
 *
 */
//Telnet.java 指定されたアドレスのポートに標準入出力を接続します
//ポートが23番の場合ネゴシエーションを行います。
//使い方：java Telnet サーバーアドレス ポート番号 例：java Telnet 192.168.0.1 23
//ポート番号を省略すると23番を仮定します 終了はCtrl+c

//Telnetクラス 
//Telnetクラスはネットワーク接続の管理を行います。
public class Telnet {
  public Socket serverSocket;// 接続用ソケット
  public OutputStream serverOutput;// ネットワーク出力用ストリーム
  public BufferedInputStream serverInput;// ネットワーク入力用ストリーム
  public String host;// 接続用サーバーアドレス
  public int port; // 接続先サーバーポート番号
  static final int DEFAULT_TELNET_PORT = 23;// telnetのポート番号23番

  // コンストラクタアドレスとポートの指定がある場合
  public Telnet(String host, int port) {
    this.host = host;
    this.port = port;
  }

  // コンストラクタアドレスのみ指定の場合
  public Telnet(String host) {
    this(host, DEFAULT_TELNET_PORT);
  }

  // openConnectionメソッド
  // アドレスとポート番号からソケットを作りストリームを作成ます。
  public void openConnection() {
    try {
      serverSocket = new Socket(host, port);
      serverOutput = serverSocket.getOutputStream();
      serverInput = new BufferedInputStream(serverSocket.getInputStream());
      if (port == DEFAULT_TELNET_PORT) {
        negotiation(serverInput, serverOutput);
      }
    } catch (UnknownHostException e) {
      System.err.println("UnknownHostException Error : " + e);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("IOException Error : " + e);
      System.exit(1);
    }
  }

  // main_procメソッド
  // ネットワークとのやりとりをするスレッドをスタート
  public void main_proc() throws IOException {
    try // スレッド用クラスStreamConnectorのオブジェクトを生成
    {
      StreamConnector stdin_to_socket = new StreamConnector(System.in, serverOutput);
      StreamConnector socket_to_stdout = new StreamConnector(serverInput, System.out);
      Thread input_thread = new Thread(stdin_to_socket);// スレッドを生成
      Thread output_thread = new Thread(socket_to_stdout);
      input_thread.start();// スレッドを起動
      output_thread.start();
    } catch (Exception e) {
      System.err.print(e);
      System.exit(1);
    }
  }

  // ネゴシエーションに用いるコマンドの定義
  static final byte IAC = (byte) 255;
  static final byte DONT = (byte) 254;
  static final byte DO = (byte) 253;
  static final byte WONT = (byte) 252;
  static final byte WILL = (byte) 251;

  // negotiationメソッド
  // NVTによる通信をネゴシエートします
  static void negotiation(BufferedInputStream in, OutputStream out) throws IOException {
    byte[] buff = new byte[3];// コマンド受信用配列
    while (true) {
      in.mark(buff.length);
      if (in.available() >= buff.length) {
        in.read(buff);
        if (buff[0] != IAC)// ネゴシエーション終了
        {
          in.reset();
          return;
        } else if (buff[1] == DO)// DOコマンドに対しては
        {
          buff[1] = WONT;// WON'Tで返答
          out.write(buff);
        }
      }
    }
  }

  // mainメソッド
  // TCPコネクションを開いて処理を開始します。
  public static void main(String[] arg) {
    try {
      Telnet t = null;
      switch (arg.length) {
      case 1: // サーバーアドレスのみの指定
        t = new Telnet(arg[0]);
        break;
      case 2: // アドレスとポートの指定
        t = new Telnet(arg[0], Integer.parseInt(arg[1]));
        break;
      default:// 使い方が間違っている場合
        System.out.println("usage:java Telnet<host name> {<port number>}");
        return;
      }
      System.out.println("Listening...");
      t.openConnection();
      t.main_proc();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}

//StreamConnectorクラス
//ストリームを受け取り両者を結合してデータを受け渡します。
//スレッドを構成するためのクラス
class StreamConnector implements Runnable {
  InputStream src = null;
  OutputStream dist = null;

  // コンストラクタ 入出力ストリームを受け取ります。
  public StreamConnector(InputStream in, OutputStream out) {
    src = in;
    dist = out;
  }

  // 処理の本体 ストリームの読み書きを無限に繰り返します。
  @Override
  public void run() {
    byte[] buff = new byte[1024];
    while (true) {
      try {
        int n = src.read(buff);
        if (n > 0) {
          dist.write(buff, 0, n);
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.err.print("StreamConnector Error : " + e);
        System.exit(1);
      }
    }
  }
}
