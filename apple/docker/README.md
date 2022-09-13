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
wget https://artifacts.apple.com/artifactory/oss-patched-binaries-local/org/apache/hadoop/3.3.3.2-apple/hadoop-3.3.3.2-apple.tar.gz
```

## Build

Create the base image as follows, optionally replace it with your own version tag.

```bash
DOCKER_BUILDKIT=1 docker build . -f Dockerfile --target base -t docker.apple.com/acs-flink/flink:1.15.2.0-apple
```

Create the Hadoop image as follows:

```bash
DOCKER_BUILDKIT=1 docker build . -f Dockerfile --target hadoop -t docker.apple.com/acs-flink/flink:1.15.2.0-apple-hadoop
```

These images are regularly published via our Rio CI pipeline.

## Verification

You may verify your image being able to start a jobmanager:
```bash
docker run -it docker.apple.com/acs-flink/flink:1.14.3.1-apple jobmanager
```
