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

And then, install `IJava`, Jupyter kernel for Java. When installing IJava, you need to set your application's classpath. Plaese run the shell script below(JupyterLab required).

For Linux:

```bash
JISD/setup/setup-linux.sh <your classpath>
```

For Windows:

```bash
JISD\setup\setup-windows.ps1 <your classpath>
```

For MacOS:

```bash
JISD/setup/setup-macos.sh <your classpath>
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
