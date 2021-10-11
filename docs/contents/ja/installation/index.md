# インストール
## Dockerを使う場合
### 必要なもの
- docker
- docker-compose

1. `--recursive` オプションをつけて[JISDLabリポジトリ](https://github.com/tklab-group/JISDLab)をクローンする
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


1. `--recursive` オプションをつけて[JISDLabリポジトリ](https://github.com/tklab-group/JISDLab)をクローンする
```sh
git clone --recursive https://github.com/tklab-group/JISDLab.git
```
2. 環境変数`JISDLAB_HOME`にJISDLabディレクトリの絶対パスを設定する
3. JISDLabディレクトリで以下を実行する

For Linux:

```bash
JISD/setup/setup-linux.sh <指定したいクラスパス>
```

For MacOS:

```bash
JISD/setup/setup-macos.sh <指定したいクラスパス>
```

For Windows:

```bash
JISD\setup\setup-windows.ps1 <指定したいクラスパス>
```
4. 以下を実行してJupyterLabを起動する
```sh
jupyter lab
```
5. 起動したJupyterLab にアクセスする

その他詳細は[JISDLabリポジトリ](https://github.com/tklab-group/JISDLab)を参照してください．
