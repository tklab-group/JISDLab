/** */
package probej;

import debug.Location;
import debug.value.ValueInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** @author sugiyama */
class Connector {
  String host;
  int port;
  AsynchronousSocketChannel client;
  Parser parser = new Parser();

  public Connector(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void openConnection() throws TimeoutException {
    try {
      client = AsynchronousSocketChannel.open();
      System.out.println("Try to connect to " + host + ":" + port);
      Future<Void> future = client.connect(new InetSocketAddress(host, port));
      future.get(5, TimeUnit.SECONDS);
      System.out.println("Succeccfully connected to " + host + ":" + port);
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
                sendCommandSync(cmd);
              });
      sender.start();
    }
  }

  void sendCommandSync(String cmd) {
    ByteBuffer inBuf = ByteBuffer.allocate(1024);
    String inputLine = cmd;
    inBuf = ByteBuffer.wrap(inputLine.getBytes());
    Future<Integer> writeResult = client.write(inBuf);
    try {
      writeResult.get(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
    inBuf.clear();
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
                } catch (NumberFormatException | TimeoutException e) {
                  e.printStackTrace();
                  return;
                }
                if (noOfBP < 1) {
                  return;
                }
              } else {
                noOfBP = 1;
              }
              for (int i = 0; i < noOfBP; i++) {
                String locStr = null;
                try {
                  locStr = readLine(client, outBuf);
                } catch (TimeoutException e) {
                  e.printStackTrace();
                }
                // System.out.println(locStr);
                Optional<Location> loc = parser.parseLocation(locStr);
                if (loc.isEmpty()) {
                  // System.out.println("e");
                  continue;
                }
                try {
                  ArrayList<ValueInfo> values = new ArrayList<>();
                  String noOfValueStr = readLine(client, outBuf);
                  // System.out.println(noOfValueStr);
                  int noOfValue = Integer.parseInt(noOfValueStr);
                  if (noOfValue < 1) {
                    continue;
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

                } catch (NumberFormatException | TimeoutException e) {
                  e.printStackTrace();
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

  String readLine(AsynchronousSocketChannel client, ByteBuffer b) throws TimeoutException {
    StringBuilder buf = new StringBuilder(1024);
    while (true) {
      if (!b.hasRemaining()) {
        try {
          b.clear();
          int len = client.read(b).get(5, TimeUnit.SECONDS);
          b.flip();
          if (len == -1) {
            return "0";
          }
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
