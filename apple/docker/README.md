# Apple Flink docker images

These image builds follow the upstream [flink-docker](https://github.com/apache/flink-docker) repository, 
but use Apple internal base images. 

## Prerequisites

Build Flink, then compress the distro and copy it here:

```bash
pushd ../../flink-dist/target/flink-*-bin
tar czf flink.tgz flink-*-SNAPSHOT
cp flink.tgz ../../../apple/docker
popd
```

For the Hadoop image download the Hadoop distro:

```bash
wget https://archive.apache.org/dist/hadoop/common/hadoop-3.3.1/hadoop-3.3.1.tar.gz
```

## Build

Create the base image as follows, optionally replace it with your own version tag.

```bash
docker build . -f Dockerfile.base -t docker.apple.com/acs-flink/flink:1.14.3.1-apple
```

Create the Hadoop image as follows:

```bash
docker build . -f Dockerfile.hadoop -t docker.apple.com/acs-flink/flink:1.14.3.1-apple-hadoop
```

These images are regularly published via our Rio CI pipeline.

## Verification

You may verify your image being able to start a jobmanager:
```bash
docker run -it docker.apple.com/acs-flink/flink:1.14.3.1-apple jobmanager
```
