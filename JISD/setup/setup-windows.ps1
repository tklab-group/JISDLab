# your classpath required.
# usage   : setup-macos.sh <your classpath>
# example : setup-macos.sh cp1:cp2:cp3parent/cp3child
Param([Parameter(Mandatory=$true)][String]$Arg1)

# Install `IJava`, Jupyter kernel for Java.When installing IJava, please set classpaths and `startup.jshell` by param.There are sample classes in `sample` dir which contains `jisd.demo.HelloWorld` class(which source file is debugspace/HelloWorld.java).  
# 
# cd %JISDLAB_HOME%\IJava ; ./gradlew.bat installKernel --param classpath:"%JISDLAB_HOME%/jdiscript/jdiscript/build/libs/jdiscript-0.9.0.jar;%JISDLAB_HOME%/JISD/build/libs/jisd-all.jar;<your classpaths>" --param startup-scripts-path:"%JISDLAB_HOME%/JISD/startup.jshell"; cd ..
cd %JISDLAB_HOME%\IJava ; ./gradlew.bat installKernel --param classpath:"%JISDLAB_HOME%/jdiscript/jdiscript/build/libs/jdiscript-0.9.0.jar;%JISDLAB_HOME%/JISD/build/libs/jisd-all.jar;$Arg1" --param startup-scripts-path:"%JISDLAB_HOME%/JISD/startup.jshell"; cd ..

# You need to modify `kernel.json` for static analysis.If you want to know the location of `kernel.json`, please see https://jupyter-client.readthedocs.io/en/stable/kernels.html#kernel-specs.
#
# cd %JISDLAB_HOME%\JISD ; ./gradlew.bat createKernelJson -Pjsonpath="%APPDATA%\jupyter\kernels\java\kernel.json" -Pcp=<your classpaths>; cd ..
cd %JISDLAB_HOME%\JISD ; ./gradlew.bat createKernelJson -Pjsonpath="%APPDATA%\jupyter\kernels\java\kernel.json" -Pcp=$Arg1; cd ..

