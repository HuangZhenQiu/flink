#!/usr/bin/env python3
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
import sys
import ruamel.yaml

if len(sys.argv) != 2:
    print(f'usage: {sys.argv[0]} release_version')
    sys.exit(1)

release_version = sys.argv[1]

yaml_file_name = 'rio.yml'
yaml = ruamel.yaml.YAML()

with open(yaml_file_name) as istream:
    print(f'Loading yaml file: {yaml_file_name}\n')
    ymldoc = yaml.load(istream)

    pipelines = ymldoc['pipelines']

    # Publish pipeline
    print('Searching for publish pipeline')
    publish_pipeline = next(filter(lambda p: p['name'].startswith('publish'), pipelines), None)
    if publish_pipeline is None:
        print('Publish pipeline not found')
        sys.exit(1)
    publish_pipeline['name'] = f'publish-release-{release_version}'
    publish_pipeline['branchName'] = f'release-{release_version}'
    publish_pipeline['package']['release'] = True
    for d in publish_pipeline['package']['dockerfile']:
        d['version'] = release_version
        del d['extraTags']
    print('Publish pipeline updated\n')

    # Binary pipeline
    print('Searching for binary pipeline')
    binary_pipeline = next(filter(lambda p: p['name'].startswith('binary'), pipelines), None)
    if binary_pipeline is None:
        print("Binary pipeline not found")
        sys.exit(1)
    binary_pipeline['name'] = f'binary-release-{release_version}'
    binary_pipeline['branchName'] = f'release-{release_version}'
    binary_pipeline['package']['release'] = True
    print('Binary pipeline updated\n')

    # Pull request pipeline
    print("Searching pull request pipeline")
    pull_request_removed = False
    for k, v in enumerate(pipelines):
        if v['name'].startswith('pull-request'):
            pipelines.pop(k)
            pull_request_removed = True
    if pull_request_removed is False:
        print('Pull request pipeline not found')
        sys.exit(1)
    print('Pull request pipeline removed\n')

with open(yaml_file_name, 'w') as ostream:
    print(f'Saving yaml file: {yaml_file_name}')
    yaml.width = 4096
    yaml.dump(ymldoc, ostream)
