FROM python:3.8

# JupyterLab install
RUN pip install --upgrade pip && \
pip install jupyterlab

RUN mkdir -p ~/.jupyter && \
echo 'c = get_config()\n\
c.IPKernelApp.pylab = "inline"\n\
c.NotebookApp.ip = "0.0.0.0"\n\
c.NotebookApp.open_browser = False\n\
c.NotebookApp.port = 21510\n\
c.NotebookApp.password = "sha1:c956c5379e61:c10306b11fa6b2d69fe0ad302c1bfe91c140ba92"'\
> ~/.jupyter/jupyter_lab_config.py

# When using a version of Jupyter Notebook earlier than 5.3, 
# the following command must be run after installation to enable 
# the JupyterLab server extension:
#
# RUN jupyter serverextension enable --py jupyterlab

# Java install
RUN apt-get update && \
    apt-get install -y openjdk-11-jdk

# workspace directory
ARG ws_dir=/workspaces

# jisdlab/jisd directory
ENV jisdlab_dir=/JISDLab
ARG jisd_dir=${jisdlab_dir}/JISD

# jdiscript classpath  
ARG cp1=${jisdlab_dir}/jdiscript/jdiscript/build/libs/jdiscript-0.9.0.jar

# JISD classpath
ARG cp2=${jisd_dir}/build/libs/jisd-all.jar

# your application's absolute classpaths in a container(classpath1:classpath2:...)
ARG cp3=${ws_dir}/sample

COPY . $jisdlab_dir
WORKDIR $jisdlab_dir

# IJava install
RUN cd IJava && ./gradlew installKernel --param classpath:${cp1}:${cp2}:${cp3} --param startup-scripts-path:${jisd_dir}/startup.jshell
# For Windows
# RUN cd IJava ; ./gradlew.bat installKernel --param classpath:"%JISDLAB_HOME%/jdiscript/jdiscript/build/libs/jdiscript-0.9.0.jar;%JISDLAB_HOME%/JISD/build/libs/jisd-all.jar;%JISDLAB_HOME%/sample" --param startup-scripts-path:"%JISDLAB_HOME%/JISD/startup.jshell"; cd ..

#Modify kernel.json
RUN cd JISD && ./gradlew createKernelJson -Pjsonpath=/root/.local/share/jupyter/kernels/java/kernel.json -Pcp=${cp3}
# For Windows
# RUN cd JISD ; ./gradlew.bat createKernelJson -Pjsonpath="%APPDATA%\jupyter\kernels\java\kernel.json" -Pcp="%JISDLAB_HOME%\sample"; cd ..

RUN \ 
  curl -fsSL https://deb.nodesource.com/setup_17.x | bash -; \
  apt-get install -y nodejs

RUN \
  ls && pip install --upgrade elyra-code-snippet-extension &&\
  elyra-metadata import code-snippets --directory "${jisdlab_dir}/code-snippets/"
