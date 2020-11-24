package analysis;

import org.json.JSONObject;
import org.objectweb.asm.tree.*;
import util.Name;

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

public class LysMain {

  public static void main(String[] args) {

    Map<String, Object> pobj_all_ps = new HashMap<>();
    Map<String, Object> pobj_all_md = new HashMap<>();
    Map<String, Object> pobj_all_df = new HashMap<>();

    HashMap<String, ClassNode> cns = new HashMap<>(); // key:classname
    HashMap<String, ArrayList<SrtVal>> fields = new HashMap<>(); // key:classname,value:fields

    HashMap<String, String> packagenames =
        new HashMap<>(); // key:classname,value:packagename (just for cache)
    HashMap<String, String> onlyclassnames =
        new HashMap<>(); // key:classname,value:classname(short) (just for cache)
    HashMap<String, Map<String, Object>> map_pobj_package_ps =
        new HashMap<>(); // key:packagename,value:pobj_package_ps
    HashMap<String, Map<String, Object>> map_pobj_package_md =
        new HashMap<>(); // key:packagename,value:pobj_package_md
    HashMap<String, Map<String, Object>> map_pobj_package_df =
        new HashMap<>(); // key:packagename,value:pobj_package_df

    /*
     * preparation (get all cns & fields)
     *******************************************/

    String root = "."; // directory to find class files of this analysis program (from the root
    // directory)
    if (args.length > 1) {
      root = args[1];
    }
    // find class files recursively
    Path start = Paths.get(root);
    FileVisitor<Path> visitor = new ClassFileVisitor(cns, root.length());
    try {
      Files.walkFileTree(start, visitor);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // get all fields
    for (Entry<String, ClassNode> cn_entry : cns.entrySet()) {
      ClassNode cn = cn_entry.getValue();

      ArrayList<SrtVal> tmp_fields = new ArrayList<>();
      List<FieldNode> fieldlist = cn.fields;
      for (FieldNode fn : fieldlist) {
        tmp_fields.add(new SrtVal(fn.name, fn.desc, cns.keySet()));
      }

      fields.put(cn.name, tmp_fields);
    }

    /*
     * about package name
     ************************************************************/

    for (Entry<String, ClassNode> cn_entry : cns.entrySet()) {
      ClassNode cn = cn_entry.getValue();

      String packagename, onlyclassname;
      String classname = cn.name;
      int pos = classname.lastIndexOf("/");
      if (pos == -1) {
        packagename = "default";
        onlyclassname = classname;
      } else {
        packagename = classname.substring(0, pos).replace('/', '.');
        onlyclassname = classname.substring(pos + 1);
      }

      packagenames.put(cn.name, packagename);
      onlyclassnames.put(cn.name, onlyclassname);

      // create new pobj_package_xx
      HashMap<String, Object> pobj_package_ps = new HashMap<>();
      HashMap<String, Object> pobj_package_md = new HashMap<>();
      HashMap<String, Object> pobj_package_df = new HashMap<>();
      map_pobj_package_ps.put(packagename, pobj_package_ps);
      map_pobj_package_md.put(packagename, pobj_package_md);
      map_pobj_package_df.put(packagename, pobj_package_df);
      pobj_all_ps.put(packagename, pobj_package_ps);
      pobj_all_md.put(packagename, pobj_package_md);
      pobj_all_df.put(packagename, pobj_package_df);
    }

    /*
     * for each class
     ****************************************************************/

    for (Entry<String, ClassNode> cn_entry : cns.entrySet()) {
      ClassNode cn = cn_entry.getValue();

      Map<String, Object> pobj_class_ps = new HashMap<>();
      Map<String, Object> pobj_methods_md = new HashMap<>();

      /* for each method ********************************************************/
      List<MethodNode> methodlist = cn.methods;
      for (MethodNode mn : methodlist) {

        Map<String, Object> pobj_method_ps = new HashMap<>();
        List<Object> parr_method_md = new ArrayList<>();

        LinkedHashSetEx<Integer> method_lines = new LinkedHashSetEx<>();
        LinkedHashSetEx<String> method_line_labels = new LinkedHashSetEx<>();

        // get all lines
        InsnList insns = mn.instructions;
        if (insns.size() != 0) {
          Iterator<AbstractInsnNode> j = insns.iterator();
          while (j.hasNext()) {
            AbstractInsnNode in = j.next();
            if (in instanceof LineNumberNode) {
              method_lines.add(((LineNumberNode) in).line);
              method_line_labels.add(((LineNumberNode) in).start.getLabel().toString());
            }
          }
        }

        /* for each val(1) ************************************************/
        for (SrtVal sf : fields.get(cn.name)) {
          // if can
          if (sf.typekind == 0) {
            Set<Object> parr_canset = new LinkedHashSet<>();
            Set<Object> parr_recom = new LinkedHashSet<>();
            for (Integer ml : method_lines) {
              parr_canset.add(ml.toString());
              parr_recom.add(ml.toString()); // temporary<<<
            }
            List<Object> parr_val = new ArrayList<>();
            parr_val.add(parr_canset);
            parr_val.add(parr_recom);
            pobj_method_ps.put(sf.name, parr_val);

            // if userclass(sf.typename should be classname in cns)
          } else if (sf.typekind == 1) {
            Map<String, Object> pobj_val = new HashMap<>();
            ArrayList<SrtVal> inside_sfs = fields.get(sf.typename); // should exist
            if (inside_sfs != null) {
              for (SrtVal inside_sf : inside_sfs) {
                if (inside_sf.typekind == 0) { // if can
                  Set<Object> parr_canset = new LinkedHashSet<>();
                  Set<Object> parr_recom = new LinkedHashSet<>();
                  for (Integer ml : method_lines) {
                    parr_canset.add(ml.toString());
                    parr_recom.add(ml.toString()); // temporary<<<
                  }
                  List<Object> parr_field = new ArrayList<>();
                  parr_field.add(parr_canset);
                  parr_field.add(parr_recom);
                  pobj_val.put(inside_sf.name, parr_field);
                }
              }
            }
            pobj_method_ps.put(sf.name, pobj_val);

            // else(cannot)
          } else {
          }
        }

        /* for each val(2) ************************************************/
        List<LocalVariableNode> localvallist = mn.localVariables;
        if (localvallist != null) { // except when the method has no definition but only declaration
          for (LocalVariableNode lvn : localvallist) {
            // if "this"
            if (lvn.name.equals("this")) {
              continue;
            }
            String startlabel = lvn.start.getLabel().toString();
            String endlabel = lvn.end.getLabel().toString();

            SrtVal sl = new SrtVal(lvn.name, lvn.desc, cns.keySet());

            // if can
            if (sl.typekind == 0) {
              Set<Object> parr_canset = new LinkedHashSet<>();
              Set<Object> parr_recom = new LinkedHashSet<>();

              int idx_start = method_line_labels.getIndexOfValue(startlabel);
              int idx_end = method_line_labels.getIndexOfValue(endlabel);
              if (method_lines.size() < idx_start) { // for inner variable declaration
                AbstractInsnNode node = lvn.start;
                while (!(node instanceof LineNumberNode)) {
                  node = node.getNext();
                }
                idx_start = ((LineNumberNode) node).line - method_lines.getFirstValue();
              }
              method_lines.getSubset(idx_start, idx_end, parr_canset);

              parr_recom = parr_canset; // temporary<<<

              addLines(pobj_method_ps, sl.name, parr_canset, parr_recom);

              // if userclass(sl.typename should be classname in cns)
            } else if (sl.typekind == 1) {
              Map<String, Object> pobj_val = new HashMap<>();
              ArrayList<SrtVal> inside_sfs = fields.get(sl.typename); // should exist
              if (inside_sfs != null) {
                for (SrtVal inside_sf : inside_sfs) {
                  if (inside_sf.typekind == 0) { // if can
                    Set<Object> parr_canset = new LinkedHashSet<>();
                    Set<Object> parr_recom = new LinkedHashSet<>();

                    int idx_start = method_line_labels.getIndexOfValue(startlabel);
                    int idx_end = method_line_labels.getIndexOfValue(endlabel);
                    method_lines.getSubset(idx_start, idx_end, parr_canset);
                    parr_recom = parr_canset; // temporary<<<

                    addLines(pobj_val, inside_sf.name, parr_canset, parr_recom);
                  }
                }
              }

              pobj_method_ps.put(sl.name, pobj_val);

              // else(cannot)
            } else {
            }
          }
        }

        /* for parr_method ************************************************/

        String accmodif, rettype, linebeg, lineend;
        ArrayList<String> argtype = new ArrayList<>();
        ArrayList<String> argname = new ArrayList<>();

        accmodif = "";
        switch (mn.access) {
          case 1:
            accmodif = "public";
            break;
          case 2:
            accmodif = "private";
            break;
          case 4:
            accmodif = "protected";
            break;
        }
        parr_method_md.add(accmodif);

        rettype = "";
        if (!mn.name.equals("<init>")) {
          String mdesc = mn.desc;
          int idx_delim = mdesc.indexOf(')');
          String mretdesc = mdesc.substring(idx_delim + 1, mdesc.length());
          SrtVal sv = new SrtVal("dummy", mretdesc, cns.keySet()); // noneed so much<<<
          rettype = sv.typename;
        }
        parr_method_md.add(rettype);

        // (this can be written in "for each val(2)"<<<)
        if (localvallist != null) { // except when the method has no definition but only declaration
          for (LocalVariableNode lvn : localvallist) {
            // if "this"
            if (lvn.name.equals("this")) {
              continue;
            }

            String startlabel = lvn.start.getLabel().toString();
            if (method_line_labels.getFirstValue().equals(startlabel)) {
              SrtVal sl = new SrtVal(lvn.name, lvn.desc, cns.keySet());
              argtype.add(sl.typename);
              argname.add(sl.name);
            }
          }
        }
        parr_method_md.add(argtype);
        parr_method_md.add(argname);

        linebeg = "1";
        lineend = "1";
        if (method_lines.size() > 0) {
          linebeg = method_lines.getFirstValue().toString();
          lineend = method_lines.getLastValue().toString();
        }
        parr_method_md.add(linebeg);
        parr_method_md.add(lineend);

        String mname = mn.name;
        if (mname.equals("<init>")) {
          mname = onlyclassnames.get(cn.name); // constructor
        }

        parr_method_md.add(mname);

        String mfullname = mname + "(" + String.join(", ", argtype) + ")";
        pobj_class_ps.put(mfullname, pobj_method_ps);
        pobj_methods_md.put(mfullname, parr_method_md);
      }
      // for each fields
      HashMap<String, Object> pobj_fields_md = new HashMap<>();
      var fieldVals = fields.get(cn.name);
      fieldVals.forEach(
          val -> {
            pobj_fields_md.put(val.name, val.typename);
          });

      HashMap<String, Object> pobj_class_md = new HashMap<>();
      pobj_class_md.put("methods", pobj_methods_md);
      pobj_class_md.put("fields", pobj_fields_md);
      pobj_class_md.put("supers", Name.toClassNameFromSourcePath(cn.superName));
      ArrayList<String> interfacesStr = new ArrayList<>();
      cn.interfaces.forEach(
          in -> {
            interfacesStr.add(Name.toClassNameFromSourcePath(in));
          });
      pobj_class_md.put("interfaces", interfacesStr);

      // put pobj_class_xx to corresponding pobj_package_xx
      String packagename = packagenames.get(cn.name);
      String onlyclassname = onlyclassnames.get(cn.name);
      map_pobj_package_ps.get(packagename).put(onlyclassname, pobj_class_ps);
      map_pobj_package_md.get(packagename).put(onlyclassname, pobj_class_md);
      map_pobj_package_df
          .get(packagename)
          .put(onlyclassname, getFullSourceFilePath(cn_entry.getKey(), cn.sourceFile));
    }

    /*
     * serialize and write to file
     **************************************************/

    JSONObject result_ps = new JSONObject(pobj_all_ps);
    JSONObject result_md = new JSONObject(pobj_all_md);
    JSONObject result_df = new JSONObject(pobj_all_df);

    String sresult_ps = result_ps.toString();
    String sresult_md = result_md.toString();
    String sresult_df = result_df.toString();

    String output_dir = "data"; // directory to output json files
    if (args.length > 0) {
      output_dir = args[0];
    }
    File output_dir_f = new File(output_dir);
    if (!output_dir_f.exists()) {
      output_dir_f.mkdirs();
    }

    try {
      BufferedWriter bw;
      bw = new BufferedWriter(new FileWriter(output_dir + "/program_structure.json", false));
      bw.write(sresult_ps);
      bw.close();
      bw = new BufferedWriter(new FileWriter(output_dir + "/class_data.json", false));
      bw.write(sresult_md);
      bw.close();
      bw = new BufferedWriter(new FileWriter(output_dir + "/defined_filename.json", false));
      bw.write(sresult_df);
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the map of "cansetline Set and recomline Set". The key is sname. The sets are made from
   * canset_lines and recom_lines. (eg. ssmap = {"sname", [["34", "35", "36", "37"], ["35", "37"]]})
   * <br>
   * When ssmap already contains sname as its key, the specified lines will be added to the existing
   * set.
   *
   * @param ssmap map which will be created
   * @param sname key of the map
   * @param canset_lines set of cansetline
   * @param recom_lines set of recomline
   */
  @SuppressWarnings("unchecked")
  private static void addLines(
      Map<String, Object> ssmap, String sname, Set<Object> canset_lines, Set<Object> recom_lines) {
    List<Object> cur_vallist = (List<Object>) ssmap.get(sname);
    if (cur_vallist == null) {
      List<Object> vallist = new ArrayList<>();
      vallist.add(canset_lines);
      vallist.add(recom_lines);
      ssmap.put(sname, vallist);
    } else {
      ((Set<Object>) cur_vallist.get(0)).addAll(canset_lines);
      ((Set<Object>) cur_vallist.get(1)).addAll(recom_lines);
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
