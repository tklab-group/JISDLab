# JISD
Java scriptable debugger on JupyterLab.

## Requirements
- docker
- docker-compose

## Setup
First of all, you should clone this repository with a `--recursive` option.
```
git clone --recursive https://github.com/tklab-group/JISD.git
```
And then, you can set your Java application's classpaths in `ARG cp3` of [Dockerfile](./Dockerfile).
```bash:Dockerfile
# your application's absolute classpaths in a container(classpath1:classpath2:...)
ARG cp3=${ws_dir}/sample
```
Then, at this project root:
### Build & Start
```bash
docker-compose up --build -d
```
Access http://localhost:17682 and enter a password(an initial password is `passwd`), then you can debug your Java application!

## Demo
Please see `debugspace/debuggerDemo.ipynb`.

## See also
- JISD's javadoc: https://sakupo.github.io/JISD-doc/

## Restart & Stop
### Restart
```bash
docker-compose restart
```
### Stop
```bash
docker-compose down -v
```
