package analysis;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

/**
 * For visiting all class files recursively and create ClassNode of each class file. <br>
 * After creating an instance of this class, call {@linkplain Files#walkFileTree} with the instance
 * as one of its arguments.
 */
public class ClassFileVisitor implements FileVisitor<Path> {
  private final int postOffset = 6; // to remove ".class"
  private HashMap<String, ClassNode> classNodes;
  private int offset;

  /**
   * @param classNodes HashMap which will contain ClassNodes of all class files
   * @param offset offset to remove first several letters from the full paths of class files (eg.
   *     when setting 4 offset, "bin\src\Test.class" -> "src\Test.class")
   */
  public ClassFileVisitor(HashMap<String, ClassNode> classNodes, int offset) {
    this.classNodes = classNodes;
    this.offset = offset;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    String fullClassName = file.toString();

    if (fullClassName.endsWith(".class")) {
      String classname =
          fullClassName.substring(
              offset, fullClassName.length() - postOffset); // .replace(File.separator.charAt(0),
      // '.');

      // create ClassNode
      classNodes.put(classname, new ClassNode());
      ClassReader cr = new ClassReader(classname);
      cr.accept(classNodes.get(classname), 0);
    }

    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
    e.printStackTrace();
    return FileVisitResult.CONTINUE;
  }
}
