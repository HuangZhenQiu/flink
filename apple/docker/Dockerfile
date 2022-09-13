FROM docker.apple.com/base-images/ubi8-minimal/java11-runtime as base

USER root
# Install dependencies
RUN set -ex; \
  microdnf install -y wget gnupg2 curl tar hostname gettext shadow-utils findutils gzip bzip2

# Suboptimal fix for missing libjemalloc.so from LD_PRELOAD
RUN rpm -ivh https://download-ib01.fedoraproject.org/pub/epel/8/Everything/x86_64/Packages/j/jemalloc-5.2.1-2.el8.x86_64.rpm
RUN mkdir -p /usr/lib/x86_64-linux-gnu/
RUN ln -s /usr/lib64/libjemalloc.so.2 /usr/lib/x86_64-linux-gnu/libjemalloc.so

# Grab gosu for easy step-down from root
ENV GOSU_VERSION 1.11
RUN set -ex; \
  wget -nv -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-amd64"; \
  wget -nv -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-amd64.asc"; \
  export GNUPGHOME="$(mktemp -d)"; \
  for server in ha.pool.sks-keyservers.net $(shuf -e \
                          hkp://p80.pool.sks-keyservers.net:80 \
                          keyserver.ubuntu.com \
                          hkp://keyserver.ubuntu.com:80 \
                          pgp.mit.edu) ; do \
      gpg --batch --keyserver "$server" --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 && break || : ; \
  done && \
  gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu; \
  gpgconf --kill all; \
  rm -rf "$GNUPGHOME" /usr/local/bin/gosu.asc; \
  chmod +x /usr/local/bin/gosu; \
  gosu nobody true

# Prepare environment
ENV FLINK_HOME=/opt/flink
ENV PATH=$FLINK_HOME/bin:$PATH
RUN groupadd --system --gid=9999 flink && \
    /usr/sbin/useradd --system --home-dir $FLINK_HOME --uid=9999 --gid=flink flink
WORKDIR $FLINK_HOME

# Install Flink
COPY flink.tgz $FLINK_HOME
RUN set -ex; \
  tar -xf flink.tgz --strip-components=1; \
  rm flink.tgz; \
  \
  chown -R flink:flink .;

# Configure container
COPY docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]
EXPOSE 6123 8081
CMD ["help"]

FROM base as hadoop

ENV HADOOP_VERSION 3.3.3
ENV HADOOP_APPLE_VERSION 2
ENV HADOOP_TGZ_DIRECTORY hadoop-$HADOOP_VERSION.$HADOOP_APPLE_VERSION-apple
ENV HADOOP_TGZ_FILE $HADOOP_TGZ_DIRECTORY.tar.gz
ENV HADOOP_USER hdfs
ENV HADOOP_PREFIX /usr/local/hadoop-$HADOOP_VERSION
ENV HADOOP_COMMON_HOME /usr/local/hadoop-$HADOOP_VERSION
ENV HADOOP_HDFS_HOME /usr/local/hadoop-$HADOOP_VERSION
ENV HADOOP_CONF_DIR /usr/local/hadoop-$HADOOP_VERSION/etc/hadoop

# Download hadoop
COPY $HADOOP_TGZ_FILE /usr/local/
RUN set -ex; \
      cd /usr/local/; \
      tar -xvf $HADOOP_TGZ_FILE; \
      rm $HADOOP_TGZ_FILE

RUN ln -s /usr/local/$HADOOP_TGZ_DIRECTORY /usr/local/hadoop \
&& groupadd -r hadoop \
&& groupadd -r $HADOOP_USER && useradd -r -g $HADOOP_USER -G hadoop $HADOOP_USER

# RUN mkdir -p $HADOOP_CONF_DIR

# Setup permissions and ownership
RUN chown -R $HADOOP_USER:hadoop /usr/local/$HADOOP_TGZ_DIRECTORY # && chmod -R 775 $HADOOP_CONF_DIR

# Set up hadoop user and bin path
ENV HADOOP_USER_NAME $HADOOP_USER
ENV PATH="${HADOOP_PREFIX}/bin:${PATH}"
ENV HADOOP_CLASSPATH="/usr/local/hadoop/etc/hadoop:/usr/local/hadoop/share/hadoop/common/lib/*:/usr/local/hadoop/share/hadoop/common/*:/usr/local/hadoop/share/hadoop/hdfs:/usr/local/hadoop/share/hadoop/hdfs/lib/*:/usr/local/hadoop/share/hadoop/hdfs/*:/usr/local/hadoop/share/hadoop/mapreduce/*:/usr/local/hadoop/share/hadoop/yarn:/usr/local/hadoop/share/hadoop/yarn/lib/*:/usr/local/hadoop/share/hadoop/yarn/*"

# Configure container
COPY docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]
EXPOSE 6123 8081
CMD ["help"]
