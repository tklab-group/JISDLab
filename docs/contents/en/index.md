# JISDLab
Java scriptable debugger on JupyterLab

**JISDLab** is a Java debugging environment that runs according to a script that you write.

JISDLab uses the Java debugging library **JISD**, which is intended to be used on JupyterLab, but can also be used as a Java library in your own environment.

For more information, please see [JISDLab repository](https://github.com/tklab-group/JISDLab).

## Features
- Debugging by breakpoints
- Debugging operations such as step execution, method execution, and stack trace display
- Remote debugging with scripts from the Web
- Document insertion in Text, HTML/Markdown format
- Drawing and saving graphs
- Asynchronous access to observation results
- Retention of multiple observation results at the same location
- Debugging with minimal impact on the target program
- Provision of static information
- Easy integration with external programs
- Variable data export and visualization to Elasticsearch, Prometheus, etc

## Features of JISDLab
## Scriptable debugging
Scriptable debugging is a debugging method using a debugger that operates according to the written script. JISDLab is a scriptable debugging method. JISDLab reduces the burden of such debugging work by using scriptable debugging. Once a script is written, it can be executed at any time, and because it is in Notebook format, debugging can be easily shared and reproduced by simply sharing the file.

### Literary Debugging
Literary debugging is a debugging method that applies literary programming to debugging. Literary programming is a style of computer programming proposed by Donald Knuth, in which formatted natural language text, executable code snippets, and computational results are interwoven to support program comprehension. In recent years, interactive literary programming tools such as Jupyter Notebook have been widely used in the field of data science. JISDLab can easily document, share, and reproduce debugging work by practicing this literary programming style during debugging. JISDLab allows you to easily document, share, and reproduce your debugging work by practicing this literary programming.

### Program Visualization
JISDLab can export data to tools such as Elasticsearch in order to visualize the collected observations. The exported data can be visualized by tools such as Grafana and Kibana to detect anomalous values in the presence of a large amount of variable data due to iterations, or to perform fixed-point observation of values, etc. Using JISDLab, these can be observed from outside the program to be debugged. By using JISDLab, these can be observed from the outside of the program to be debugged, making it possible to operate the program separately from the production code and avoid mistakes such as leaving unnecessary debugging code in the production.
