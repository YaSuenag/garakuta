# できること

* 1 つの Kubernetes Deployment で Elasticsearch と Kibana を立ち上げ、外からのリクエストを受け付けます

# 動かし方

```
podman play kube es-kibana-deployment.yml
```

> [!IMPORTANT]
> 少メモリ環境で動かす場合は [公式ドキュメント](https://www.elastic.co/docs/deploy-manage/deploy/self-managed/vm-max-map-count) に従い、事前にコンテナホストの `vm.max_map_count` を Elasticsearch が要求する値に設定すること。

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

* 利用バージョンは Elasticsearch、Kibana ともに 9.4.2 です
* Elasticsearch に投入したデータは永続化されません
    * 永続化ボリュームは一切指定していません
