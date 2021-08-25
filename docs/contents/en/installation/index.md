# Installation
## Using Docker
### Requirements
- docker
- docker-compose

1. Clone [JISDLab repository](https://github.com/tklab-group/JISDLab) with a `--recursive` option.
```bash
git clone --recursive https://github.com/tklab-group/JISDLab.git
```
2. Run the following in the JISDLab directory．
```bash
docker-compose up --build -d
```
3. And then, you can set your Java application's classpaths in `ARG cp3` of [Dockerfile](./Dockerfile).Concerning local and container path relations, see [docker-compose.yaml](./docker-compose.yaml).
```bash:Dockerfile
# your application's absolute classpaths in a container(classpath1:classpath2:...)
ARG cp3=${ws_dir}/sample
```
4. Access http://localhost:21510 and enter a password(an initial password is `passwd`)
5. Success if [debugspace/DemoForDocker.ipynb](debugspace/DemoForDocker.ipynb) can be executed from the Jupyter client.

## Not using Docker
### Requirements
- JupyterLab
- Java JDK >= 11


1. Clone [JISDLab repository](https://github.com/tklab-group/JISDLab) with a `--recursive` option.
```bash
git clone --recursive https://github.com/tklab-group/JISDLab.git
```
2. Set the environment variable `JISDLAB_HOME` to the absolute path of the JISDLab directory.
3. Run the following in the JISDLab directory．
For Linux:
```bash
JISD/setup/setup-linux.sh <your classpath>
```
For MacOS:
```bash
JISD/setup/setup-macos.sh <your classpath>
```
For Windows:
```bash
JISD\setup\setup-windows.ps1 <your classpath>
```
4. Run the following to launch JupyterLab
```sh
jupyter lab
```
5. Access the launched JupyterLab

For other details, please refer to the [JISDLab repository](https://github.com/tklab-group/JISDLab).
