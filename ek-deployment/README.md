# できること

* 1 つの Kubernetes Deployment で Elasticsearch と Kibana を立ち上げ、外からのリクエストを受け付けます

# 動かし方

1. コンテナホストの `vm.max_map_count` を `262144` にする
    * Elastic 推奨値
    * これがないと Elasticsearch の起動に失敗します

```
$ su -
# echo 262144 > /proc/sys/vm/max_map_count
```

2. Podman で Deployment を動かす

```
$ podman play kube es-kibana-deployment.yml
```

# Deployment 終了方法

```
$ podman play kube --down es-kibana-deployment.yml
```

# 外からのアクセス方法

Elasticsearch と Kibana の HTTP ポートは `hostPort` でホスト側ポートに紐づけられています

* 9200: Elasticsearch
* 5601: Kibana

# 各コンテナのログを見る

* `podman ps` でコンテナ ID を確認後、 `podman logs -f <コンテナ ID>` します
* 出力が JSON なのに注意

# 注意点

* 利用バージョンは Elasticsearch、Kibana ともに 7.13.2 です
* Kibana は `--allow-root` をつけて動いています
    * 試した環境（Fedora 34、podman-3.2.1-1）では Kibana 起動時にエラーが発生したため
    * es-kibana-deployment.yml の中でコンテナ内の Kibana ラッパーと `--allow-root` を直接指定しています
* Elasticsearch に投入したデータは永続化されません
    * 永続化ボリュームは一切指定していません
