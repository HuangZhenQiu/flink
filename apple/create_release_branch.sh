#!/usr/bin/env bash
################################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
################################################################################
set -e

MAJOR_VERSION=1
MINOR_VERSION=15
PATCH_VERSION=1
ACS_VERSION=0

OLD="$MAJOR_VERSION.$MINOR_VERSION.$PATCH_VERSION-acs-SNAPSHOT"
RELEASE_VERSION="$MAJOR_VERSION.$MINOR_VERSION.$PATCH_VERSION.$ACS_VERSION-acs"
BRANCH_NAME="release-$MAJOR_VERSION.$MINOR_VERSION-acs"

HERE=` basename "$PWD"`
if [[ "$HERE" != "apple" ]]; then
    echo "Please only execute in the apple/ directory";
    exit 1;
fi

git checkout -b release-$RELEASE_VERSION

# change version in all pom files
find .. -name 'pom.xml' -type f -exec perl -pi -e 's#<version>'"$OLD"'</version>#<version>'"$RELEASE_VERSION"'</version>#' {} \;

# change version of the quickstart property
find .. -name 'pom.xml' -type f -exec perl -pi -e 's#<flink.version>'"$OLD"'</flink.version>#<flink.version>'"$RELEASE_VERSION"'</flink.version>#' {} \;

git commit -am"[apple][release] Set release version to $RELEASE_VERSION"

cd ..
sed -e "s/\${MINOR_VERSION}/${MINOR_VERSION}/" -e "s/\${PATCH_VERSION}/${PATCH_VERSION}/" -e "s/\${ACS_VERSION}/${ACS_VERSION}/" -e "s/\${RELEASE_VERSION}/${RELEASE_VERSION}/" -e "s/\${BRANCH_NAME}/${BRANCH_NAME}/"  apple/release_rio_bump.patch.template | git apply

git commit -am"[apple][release] Update rio.yaml for $RELEASE_VERSION"
cd apple
