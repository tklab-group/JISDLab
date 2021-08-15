# JISDLab

Java scriptable debugger on JupyterLab

## Installation using Docker

### Requirements

- docker
- docker-compose

### Setup

First of all, you should clone this repository with a `--recursive` option.

```
git clone --recursive https://github.com/tklab-group/JISDLab.git
```

And then, you can set your Java application's classpaths in `ARG cp3` of [Dockerfile](./Dockerfile).Concerning local and container path relations, see [docker-compose.yaml](./docker-compose.yaml).

```bash:Dockerfile
# your application's absolute classpaths in a container(classpath1:classpath2:...)
ARG cp3=${ws_dir}/sample
```

Then, at this project root:

### Build & Start

```bash
docker-compose up --build -d
```

Access http://localhost:21510 and enter a password(an initial password is `passwd`), then you can debug your Java application!

## Manual Installation

You can also set up JISDLab without Docker.

### Requirements

- JupyterLab
- Java JDK >= 11

### Setup

First of all, you should clone this repository with a `--recursive` option.

```bash
git clone --recursive https://github.com/tklab-group/JISDLab.git
```

Next, please set **the absolute path of JISDLab root directory** to your environment variable `JISDLAB_HOME`.

---

And then, install `IJava`, Jupyter kernel for Java.When installing IJava, please set classpaths and `startup.jshell` by param.There are sample classes in `sample` dir which contains `jisd.demo.HelloWorld` class(which source file is debugspace/HelloWorld.java).  
For Linux, MacOS:

```bash
cd IJava && ./gradlew installKernel --param classpath:$JISDLAB_HOME/jdiscript/jdiscript/build/libs/jdiscript-0.9.0.jar:$JISDLAB_HOME/JISD/build/libs/jisd-all.jar:<your classpaths> --param startup-scripts-path:$JISDLAB_HOME/JISD/startup.jshell && cd ..
```

For Windows:

```bash
cd IJava ; ./gradlew.bat installKernel --param classpath:"%JISDLAB_HOME%/jdiscript/jdiscript/build/libs/jdiscript-0.9.0.jar;%JISDLAB_HOME%/JISD/build/libs/jisd-all.jar;<your classpaths>" --param startup-scripts-path:"%JISDLAB_HOME%/JISD/startup.jshell"; cd ..
```

---

And, you need to modify `kernel.json` for static analysis.If you want to know the location of `kernel.json`, please see https://jupyter-client.readthedocs.io/en/stable/kernels.html#kernel-specs.

For Linux:

```bash
cd JISD && ./gradlew createKernelJson -Pjsonpath=~/.local/share/jupyter/kernels/java/kernel.json -Pcp=<your classpaths> && cd ..
```

For Windows:

```bash
cd JISD ; ./gradlew.bat createKernelJson -Pjsonpath="%APPDATA%\jupyter\kernels\java\kernel.json" -Pcp=<your classpaths>; cd ..
```

For MacOS:

```bash
cd JISD && ./gradlew createKernelJson -Pjsonpath=~/Library/Jupyter/kernels/java/kernel.json -Pcp=<your classpaths> && cd ..
```

---

Then, start the Jupyter server

```bash
jupyter lab
```

Please access the server from your web browser, or IDE which support Jupyter (ex. Vidual Studio Code).

## Tutorial

Please see [debugspace/DebugTutorial.ipynb](debugspace/DebugTutorial.ipynb).

## Demo

Please see

- Docker: [debugspace/DemoForDocker.ipynb](debugspace/DemoForDocker.ipynb)
- VSCode: [debugspace/DemoForVSCode.ipynb](debugspace/DemoForVSCode.ipynb)

## See also

- JISD's javadoc: https://sakupo.github.io/JISD-doc/

## Trouble Shooting

Please see https://github.com/tklab-group/JISDLab/wiki/Trouble-Shooting (Japanese only)

## Copyright

see ./LICENSE

Java scriptable debugger on JupyterLab  
Copyright 2021 tklab-group
