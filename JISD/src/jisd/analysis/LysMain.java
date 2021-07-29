package jisd.analysis;

import jisd.util.Name;
import jisd.util.Print;
import org.json.JSONObject;
import org.objectweb.asm.tree.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

/**
 * A main class which analyzes Java bytecodes and generates static analysis data.
 *
 * @author khiranouchi
 * @author sugiyama
 */
public class LysMain {

  /**
   * Analyzes Java bytecodes and generates static analysis data.
   *
   * @param args <br>
   *     args[0]: an output dir name <br>
   *     args[1 or later]: a binDir name or a jar file name.
   */
  public static void main(String[] args) {

    Map<String, Object> packageStructure = new HashMap<>();
    Map<String, Object> classData = new HashMap<>();
    Map<String, Object> definedFileNames = new HashMap<>();

    HashMap<String, ClassNode> cns = new HashMap<>(); // key:classname
    HashMap<String, ArrayList<SrtVal>> fields = new HashMap<>(); // key:classname,value:fields

    HashMap<String, String> packageNames =
        new HashMap<>(); // key:classname,value:packageName (just for cache)
    HashMap<String, String> onlyClassNames =
        new HashMap<>(); // key:classname,value:classname(short) (just for cache)
    HashMap<String, Map<String, Object>> packageNameAndStructure =
        new HashMap<>(); // key:packageName,value:pobj_package_ps
    HashMap<String, Map<String, Object>> packageNameAndClassData =
        new HashMap<>(); // key:packageName,value:pobj_package_md
    HashMap<String, Map<String, Object>> packageNameAndDefinedFileNames =
        new HashMap<>(); // key:packageName,value:pobj_package_df

    /*
     * preparation (get all cns & fields)
     *******************************************/
    if (args.length < 2) {
      Print.out("No class file is set.");
      return;
    }
    for (int i = 1; i < args.length; i++) {
      String root = args[i];
      // find class files from jar.
      if (root.endsWith(".jar")) {
        JarFileLoader jfl = new JarFileLoader();
        File jarFile = new File(root);
        try {
          jfl.loadClasses(cns, jarFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      // find class files from binDir
      Path start = Paths.get(root);
      FileVisitor<Path> visitor = new ClassFileVisitor(cns, root.length());
      try {
        Files.walkFileTree(start, visitor);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // get all fields
    for (Entry<String, ClassNode> cn_entry : cns.entrySet()) {
      ClassNode cn = cn_entry.getValue();

      ArrayList<SrtVal> tmp_fields = new ArrayList<>();
      List<FieldNode> fieldList = cn.fields;
      for (FieldNode fn : fieldList) {
        tmp_fields.add(new SrtVal(fn.name, fn.desc, cns.keySet()));
      }

      fields.put(cn.name, tmp_fields);
    }

    /*
     * about package name
     ************************************************************/

    for (Entry<String, ClassNode> cn_entry : cns.entrySet()) {
      ClassNode cn = cn_entry.getValue();

      String packageName, onlyClassName;
      String className = cn.name;
      if (className == null) {
        className = "Not Found";
      }
      int pos = className.lastIndexOf("/");
      if (pos == -1) {
        packageName = "default";
        onlyClassName = className;
      } else {
        packageName = className.substring(0, pos).replace('/', '.');
        onlyClassName = className.substring(pos + 1);
      }

      packageNames.put(cn.name, packageName);
      onlyClassNames.put(cn.name, onlyClassName);

      // create new pobj_package_xx
      HashMap<String, Object> ps = new HashMap<>();
      HashMap<String, Object> cd = new HashMap<>();
      HashMap<String, Object> df = new HashMap<>();
      packageNameAndStructure.put(packageName, ps);
      packageNameAndClassData.put(packageName, cd);
      packageNameAndDefinedFileNames.put(packageName, df);
      packageStructure.put(packageName, ps);
      classData.put(packageName, cd);
      definedFileNames.put(packageName, df);
    }

    /*
     * for each class
     ****************************************************************/

    for (Entry<String, ClassNode> cnEntry : cns.entrySet()) {
      ClassNode classNode = cnEntry.getValue();

      Map<String, Object> classPs = new HashMap<>();
      Map<String, Object> methodsCd = new HashMap<>();

      /* for each method ********************************************************/
      List<MethodNode> methodList = classNode.methods;
      for (MethodNode methodNode : methodList) {

        Map<String, Object> methodPs = new HashMap<>();
        List<Object> ArrayMethodCd = new ArrayList<>();

        LinkedHashSetEx<Integer> methodLines = new LinkedHashSetEx<>();
        LinkedHashSetEx<String> methodLineLabels = new LinkedHashSetEx<>();

        // get all lines
        InsnList insns = methodNode.instructions;
        if (insns.size() != 0) {
          Iterator<AbstractInsnNode> j = insns.iterator();
          while (j.hasNext()) {
            AbstractInsnNode in = j.next();
            if (in instanceof LineNumberNode) {
              methodLines.add(((LineNumberNode) in).line);
              methodLineLabels.add(((LineNumberNode) in).start.getLabel().toString());
            }
          }
        }

        /* for each val(1) ************************************************/
        for (SrtVal sf : fields.get(classNode.name)) {
          // if can
          Set<Object> ArrayCanSet = new LinkedHashSet<>();
          Set<Object> ArrayRecom = new LinkedHashSet<>();
          for (Integer ml : methodLines) {
            ArrayCanSet.add(ml.toString());
            // ArrayRecom.add(ml.toString()); // temporary<<<
          }
          List<Object> ArrayVal = new ArrayList<>();
          ArrayVal.add(ArrayCanSet);
          methodPs.put("this." + sf.name, ArrayVal);
        }

        /* for each val(2) ************************************************/
        List<LocalVariableNode> localVarList = methodNode.localVariables;
        if (localVarList != null) { // except when the method has no definition but only declaration
          for (LocalVariableNode lvn : localVarList) {
            // if "this"
            if (lvn.name.equals("this")) {
              continue;
            }
            String startLabel = lvn.start.getLabel().toString();
            String endLabel = lvn.end.getLabel().toString();

            SrtVal sl = new SrtVal(lvn.name, lvn.desc, cns.keySet());

            // if can
            Set<Object> ArrayCanSet = new LinkedHashSet<>();
            Set<Object> ArrayRecom = new LinkedHashSet<>();

            int idxStart = methodLineLabels.getIndexOfValue(startLabel);
            int idxEnd = methodLineLabels.getIndexOfValue(endLabel);
            if (methodLines.size() < idxStart) { // for inner variable declaration
              AbstractInsnNode node = lvn.start;
              while (!(node instanceof LineNumberNode)) {
                node = node.getNext();
              }
              idxStart = methodLines.getIndexOfValue(((LineNumberNode) node).line);
            }
            methodLines.getSubset(idxStart, idxEnd, ArrayCanSet);

            ArrayRecom = ArrayCanSet; // temporary<<<
            List<Object> ArrayVal = new ArrayList<>();
            ArrayVal.add(ArrayCanSet);
            // ArrayVal.add(ArrayRecom);
            methodPs.put(sl.name, ArrayVal);
          }
        }

        /* for parr_method ************************************************/

        String accessModifier, returnType, lineBegin, lineEnd;
        ArrayList<String> argType = new ArrayList<>();
        ArrayList<String> argName = new ArrayList<>();

        accessModifier = "";
        switch (methodNode.access) {
          case 1:
            accessModifier = "public";
            break;
          case 2:
            accessModifier = "private";
            break;
          case 4:
            accessModifier = "protected";
            break;
        }
        ArrayMethodCd.add(accessModifier);

        returnType = "";
        if (!methodNode.name.equals("<init>")) {
          String methodDesc = methodNode.desc;
          int idxDelimiter = methodDesc.indexOf(')');
          String methodReturnDesc = methodDesc.substring(idxDelimiter + 1, methodDesc.length());
          SrtVal sv = new SrtVal("dummy", methodReturnDesc, cns.keySet()); // noneed so much<<<
          returnType = sv.typeName;
        }
        ArrayMethodCd.add(returnType);

        // (this can be written in "for each val(2)"<<<)
        if (localVarList != null) { // except when the method has no definition but only declaration
          for (LocalVariableNode lvn : localVarList) {
            // if "this"
            if (lvn.name.equals("this")) {
              continue;
            }

            String startLabel = lvn.start.getLabel().toString();
            if (methodLineLabels.getFirstValue().equals(startLabel)) {
              SrtVal sl = new SrtVal(lvn.name, lvn.desc, cns.keySet());
              argType.add(sl.typeName);
              argName.add(sl.name);
            }
          }
        }
        ArrayMethodCd.add(argType);
        ArrayMethodCd.add(argName);

        lineBegin = "1";
        lineEnd = "1";
        if (methodLines.size() > 0) {
          lineBegin = methodLines.getFirstValue().toString();
          lineEnd = methodLines.getLastValue().toString();
        }
        ArrayMethodCd.add(lineBegin);
        ArrayMethodCd.add(lineEnd);

        String methodName = methodNode.name;
        if (methodName.equals("<init>")) {
          methodName = onlyClassNames.get(classNode.name); // constructor
        }

        ArrayMethodCd.add(methodName);

        String methodFullName = methodName + "(" + String.join(", ", argType) + ")";
        classPs.put(methodFullName, methodPs);
        methodsCd.put(methodFullName, ArrayMethodCd);
      }
      // for each fields
      HashMap<String, Object> fieldsCd = new HashMap<>();
      var fieldVals = fields.get(classNode.name);
      fieldVals.forEach(
          val -> {
            fieldsCd.put(val.name, val.typeName);
          });

      HashMap<String, Object> classCd = new HashMap<>();
      classCd.put("methods", methodsCd);
      classCd.put("fields", fieldsCd);
      classCd.put("super", Name.toClassNameFromSourcePath(classNode.superName));
      ArrayList<String> interfacesStr = new ArrayList<>();
      classNode.interfaces.forEach(
          in -> {
            interfacesStr.add(Name.toClassNameFromSourcePath(in));
          });
      classCd.put("interfaces", interfacesStr);

      // put pobj_class_xx to corresponding pobj_package_xx
      String packageName = packageNames.get(classNode.name);
      String onlyClassName = onlyClassNames.get(classNode.name);

      packageNameAndStructure.get(packageName).put(onlyClassName, classPs);
      packageNameAndClassData.get(packageName).put(onlyClassName, classCd);
      packageNameAndDefinedFileNames
          .get(packageName)
          .put(onlyClassName, getFullSourceFilePath(cnEntry.getKey(), classNode.sourceFile));
    }

    /*
     * serialize and write to file
     **************************************************/

    JSONObject resultPs = new JSONObject(packageStructure);
    JSONObject resultCd = new JSONObject(classData);
    JSONObject resultDf = new JSONObject(definedFileNames);

    String resultPsStr = resultPs.toString();
    String resultCdStr = resultCd.toString();
    String resultDfStr = resultDf.toString();

    String outputDir = "data"; // directory to output json files
    if (args.length > 0) {
      outputDir = args[0];
    }
    File outputDirFile = new File(outputDir);
    if (!outputDirFile.exists()) {
      outputDirFile.mkdirs();
    }

    try {
      BufferedWriter bw;
      bw = new BufferedWriter(new FileWriter(outputDir + "/program_structure.json", false));
      bw.write(resultPsStr);
      bw.close();
      bw = new BufferedWriter(new FileWriter(outputDir + "/class_data.json", false));
      bw.write(resultCdStr);
      bw.close();
      bw = new BufferedWriter(new FileWriter(outputDir + "/defined_filename.json", false));
      bw.write(resultDfStr);
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the map of "cansetline Set and recomline Set". The key is name. The sets are made from
   * canSetLines and recomLines. (eg. base = {"name", [["34", "35", "36", "37"], ["35", "37"]]})
   * <br>
   * When base already contains name as its key, the specified lines will be added to the existing
   * set.
   *
   * @param base map which will be created
   * @param name key of the map
   * @param canSetLines set of cansetline
   * @param recomLines set of recomline
   */
  @SuppressWarnings("unchecked")
  private static void addLines(
      Map<String, Object> base, String name, Set<Object> canSetLines, Set<Object> recomLines) {
    List<Object> curValList = (List<Object>) base.get(name);
    if (curValList == null) {
      List<Object> valList = new ArrayList<>();
      valList.add(canSetLines);
      valList.add(recomLines);
      base.put(name, valList);
    } else {
      ((Set<Object>) curValList.get(0)).addAll(canSetLines);
      ((Set<Object>) curValList.get(1)).addAll(recomLines);
    }
  }

  /**
   * Create full path of source file which define class file. <br>
   * eg. "src\package1\TestClass.class" & "TestFile.java" -> "package1\TestFile.java"
   *
   * @param fullClassFilePath full path of class file
   * @param sourceFileName only name of defined source file
   * @return full path of defined source-file name
   */
  private static String getFullSourceFilePath(String fullClassFilePath, String sourceFileName) {
    int idxFirstSep = fullClassFilePath.indexOf(File.separator);
    int idxLastSep = fullClassFilePath.lastIndexOf(File.separator);
    if (idxFirstSep < idxLastSep) {
      String tmp =
          fullClassFilePath
              .replace(File.separatorChar, '/')
              .substring(idxFirstSep + 1, idxLastSep + 1);
      return tmp + sourceFileName;
    } else {
      return sourceFileName;
    }
  }
}
