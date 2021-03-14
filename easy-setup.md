# 簡単セットアップ
## Dockerを使う場合
### 必要なもの
- docker
- docker-compose

1. `--recursive` オプションをつけてこのリポジトリをクローンする
```sh
git clone --recursive https://github.com/tklab-group/JISDLab.git
```
2. JISDLabディレクトリで以下を実行する．
```sh
docker-compose up --build -d
```
3. http://localhost:21510 にアクセスして初期パスワードを入力する(初期パスワードは `passwd`)
4. [debugspace/DemoForDocker.ipynb](debugspace/DemoForDocker.ipynb)がJupyterクライアントから実行できれば成功

## Dockerを使わない場合
### 必要なもの
- JupyterLab(pip や condaで入手する)
- Java JDK >= 11


1. `--recursive` オプションをつけてこのリポジトリをクローンする
```sh
git clone --recursive https://github.com/tklab-group/JISDLab.git
```
2. 環境変数`JISDLAB_HOME`にJISDLabディレクトリの絶対パスを設定する
3. JISDLabディレクトリで以下を実行する

For Linux, MacOS:
```bash
cd IJava && ./gradlew installKernel --param classpath:$JISDLAB_HOME/jdiscript/jdiscript/build/libs/jdiscript-0.9.0.jar:$JISDLAB_HOME/JISD/build/libs/jisd-all.jar:$JISDLAB_HOME/sample --param startup-scripts-path:$JISDLAB_HOME/JISD/startup.jshell && cd ..
```

For Windows:
```bash
cd IJava ; ./gradlew.bat installKernel --param classpath:"%JISDLAB_HOME%/jdiscript/jdiscript/build/libs/jdiscript-0.9.0.jar;%JISDLAB_HOME%/JISD/build/libs/jisd-all.jar;%JISDLAB_HOME%/sample" --param startup-scripts-path:"%JISDLAB_HOME%/JISD/startup.jshell"; cd ..
```

4. JISDLabディレクトリで以下を実行する

For Linux:
```bash
cd JISD && ./gradlew createKernelJson -Pjsonpath=~/.local/share/jupyter/kernels/java/kernel.json -Pcp=$JISDLAB_HOME/sample && cd ..
```

For Windows:
```bash
cd JISD ; ./gradlew.bat createKernelJson -Pjsonpath="%APPDATA%\jupyter\kernels\java\kernel.json" -Pcp="%JISDLAB_HOME%/sample"; cd ..
```

For MacOS:
```bash
cd JISD && ./gradlew createKernelJson -Pjsonpath=~/Library/Jupyter/kernels/java/kernel.json -Pcp=$JISDLAB_HOME/sample && cd ..
```

5. 以下を実行してJupyterLabを起動する
```sh
jupyter lab
```

6. 起動したJupyterLab にアクセスする

その他詳細は[README.md](README.md)を参照してください．
