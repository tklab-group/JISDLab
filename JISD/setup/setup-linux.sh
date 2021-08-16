#!/bin/sh

if [ $# -ne 1 ]; then
  echo "your classpath required." 1>&2
  echo "usage   : setup-linux.sh <your classpath>" 1>&2
  echo "example : setup-linux.sh cp1:cp2:cp3parent/cp3child" 1>&2
  exit 1
fi

# Install `IJava`, Jupyter kernel for Java.When installing IJava, please set classpaths and `startup.jshell` by param.There are sample classes in `sample` dir which contains `jisd.demo.HelloWorld` class(which source file is debugspace/HelloWorld.java).  
# 
# cd IJava && ./gradlew installKernel --param classpath:$JISDLAB_HOME/jdiscript/jdiscript/build/libs/jdiscript-0.9.0.jar:$JISDLAB_HOME/JISD/build/libs/jisd-all.jar:<your classpaths> --param startup-scripts-path:$JISDLAB_HOME/JISD/startup.jshell && cd ..
cd $JISDLAB_HOME/IJava && ./gradlew installKernel --param classpath:$JISDLAB_HOME/jdiscript/jdiscript/build/libs/jdiscript-0.9.0.jar:$JISDLAB_HOME/JISD/build/libs/jisd-all.jar:$1 --param startup-scripts-path:$JISDLAB_HOME/JISD/startup.jshell && cd ..

# You need to modify `kernel.json` for static analysis.If you want to know the location of `kernel.json`, please see https://jupyter-client.readthedocs.io/en/stable/kernels.html#kernel-specs.
# 
# cd JISD && ./gradlew createKernelJson -Pjsonpath=~/.local/share/jupyter/kernels/java/kernel.json -Pcp=<your classpaths> && cd ..
cd $JISDLAB_HOME/JISD && ./gradlew createKernelJson -Pjsonpath=$HOME/.local/share/jupyter/kernels/java/kernel.json -Pcp=$1 && cd ..
