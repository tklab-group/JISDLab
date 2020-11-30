/** */
package probej;

import debug.Location;
import debug.value.ValueInfo;
import util.Print;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/** @author sugiyama */
class Connector {
  String host;
  int port;
  AsynchronousSocketChannel client;
  Future<AsynchronousSocketChannel> acceptFuture;
  Parser parser = new Parser();

  public Connector(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void openConnection() {
    try {
      client = AsynchronousSocketChannel.open();
      Future<Void> future = client.connect(new InetSocketAddress(host, port));
      future.get();
      Print.out("Successflly connected to " + host + ":" + port);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }

  void sendCommand(String cmd) {
    if ((client != null) && (client.isOpen())) {
      Thread sender =
          new Thread(
              () -> {
                ByteBuffer inBuf = ByteBuffer.allocate(1024);
                String inputLine = cmd;
                inBuf = ByteBuffer.wrap(inputLine.getBytes());
                Future<Integer> writeResult = client.write(inBuf);
                try {
                  writeResult.get();
                  // System.out.println("sended");
                } catch (InterruptedException e) {
                  e.printStackTrace();
                } catch (ExecutionException e) {
                  e.printStackTrace();
                }
                inBuf.clear();
              });

      sender.start();
    }
  }

  HashMap<Location, ArrayList<ValueInfo>> getResults(
      String className, String varName, int lineNumber) {
    HashMap<Location, ArrayList<ValueInfo>> results = new HashMap<>();
    Thread receiver =
        new Thread(
            () -> {
              ByteBuffer outBuf = ByteBuffer.allocate(1024);
              outBuf.clear();
              outBuf.flip();
              int noOfBP = 0;
              if (lineNumber == 0) {
                try {
                  String noOfBPStr = readLine(client, outBuf);
                  noOfBP = Integer.parseInt(noOfBPStr);
                } catch (NumberFormatException e) {
                  // System.out.println("NAN");
                  return;
                }
                if (noOfBP < 1) {
                  return;
                }
              } else {
                noOfBP = 1;
              }
              for (int i = 0; i < noOfBP; i++) {
                String locStr = readLine(client, outBuf);
                //System.out.println(locStr);
                Optional<Location> loc = parser.parseLocation(locStr);
                if (loc.isEmpty()) {
                  // System.out.println("e");
                  continue;
                }
                try {
                  ArrayList<ValueInfo> values = new ArrayList<>();
                  String noOfValueStr = readLine(client, outBuf);
                  // System.out.println(noOfValueStr);
                  int noOfValue = Integer.parseInt(noOfc
                  }
                  for (int j = 0; j < noOfValue; j++) {
                    String valueStr = readLine(client, outBuf);
                    // System.out.println(valueStr);
                    Optional<ValueInfo> value = parser.parseValue(valueStr);
                    if (value.isPresent()) {
                      values.add(value.get());
                    }
                  }
                  results.put(loc.get(), values);

                } catch (NumberFormatException e) {
                  // System.out.println("noOfValueNAN");
                  continue;
                }
              }
            });
    receiver.start();

    // Generate Print command.
    String cmd = "Print";
    if (lineNumber > 0) {
      cmd = "Print " + className + ".java " + varName + " " + lineNumber;
    }
    sendCommand(cmd);

    // Wait until the receiver get results.
    try {
      receiver.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return results;
  }

  String readLine(AsynchronousSocketChannel client, ByteBuffer b) {
    StringBuilder buf = new StringBuilder(1024);
    while (true) {
      if (!b.hasRemaining()) {
        try {
          b.clear();
          client.read(b).get();
          b.flip();
        } catch (InterruptedException e) {
          e.printStackTrace();
          return "";
        } catch (ExecutionException e) {
          return "";
        }
      }
      while (b.hasRemaining()) {
        char c = (char) b.get();
        if (c == '\n') {
          if (buf.length() == 0) {
            return "0";
          }
          return buf.toString();
        }
        buf.append(c);
      }
    }
  }

  void close() {
    try {
      client.close();
      // System.out.println("close");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
