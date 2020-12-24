package jisd.analysis;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
/** @author sugiyama */
class JarFileLoader {
  Map<String, ClassNode> loadClasses(Map<String, ClassNode> cns, File jarFile) throws IOException {
    JarFile jar = new JarFile(jarFile);
    Stream<JarEntry> str = jar.stream();
    str.forEach(z -> readJar(jar, z, cns));
    jar.close();
    return cns;
  }

  Map<String, ClassNode> readJar(JarFile jar, JarEntry entry, Map<String, ClassNode> cns) {
    String name = entry.getName();
    if (name.endsWith(".class")) {
      try (InputStream jis = jar.getInputStream(entry)) {
        byte[] bytes = IOUtils.toByteArray(jis);
        String cafebabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
        if (!cafebabe.toLowerCase().equals("cafebabe")) {
          // This class doesn't have a valid magic
          return cns;
        }
        try {
          ClassNode cn = getNode(bytes);
          cns.put(cn.name, cn);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return cns;
  }

  ClassNode getNode(byte[] bytes) {
    ClassReader cr = new ClassReader(bytes);
    ClassNode cn = new ClassNode();
    try {
      cr.accept(cn, ClassReader.EXPAND_FRAMES);
    } catch (Exception e) {
      e.printStackTrace();
    }
    cr = null;
    return cn;
  }
}
