FROM python:3.8

# JupyterLab install
RUN pip install --upgrade pip
RUN pip install jupyterlab
RUN mkdir -p ~/.jupyter &&\
echo 'c = get_config()\n\
c.IPKernelApp.pylab = "inline"\n\
c.NotebookApp.ip = "0.0.0.0"\n\
c.NotebookApp.open_browser = False\n\
c.NotebookApp.port = 21510\n\
c.NotebookApp.password = "sha1:c956c5379e61:c10306b11fa6b2d69fe0ad302c1bfe91c140ba92"'\
> ~/.jupyter/jupyter_notebook_config.py

# When using a version of Jupyter Notebook earlier than 5.3, 
# the following command must be run after installation to enable 
# the JupyterLab server extension:
#
# RUN jupyter serverextension enable --py jupyterlab

# Java install
RUN apt-get update
RUN apt-get install -y openjdk-11-jdk

# workspace directory
ARG ws_dir=/workspaces

# project directory
ARG project_dir=${ws_dir}/JISDLab

COPY . $project_dir
WORKDIR $project_dir

# jdiscript classpath  
ARG cp1=jdiscript/jdiscript/build/libs/jdiscript-0.9.0.jar

# JISD classpath
ARG cp2=JISD/lib/JISD.jar

# your application's absolute classpaths in a container(classpath1:classpath2:...)
ARG cp3=${ws_dir}/sample

# IJava install
RUN cd IJava && ./gradlew installKernel --param classpath:${project_dir}/${cp1}:${project_dir}/${cp2}:${cp3}
