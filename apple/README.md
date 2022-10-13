# ACS Flink Release Process

## Bumping major / minor version on dev branch

For this you can simply use the utility provided under `projectRoot/tools/change-version.sh`
Simply change to the tools directory, edit the `change-version.sh` script to match the current and target versions and run the script.

After this make sure to also update the `rio.yml` dockerfile versions by hand.

## Creating a new ACS Flink release

The `projectRoot/create_release_branch.sh` script contains the logic necessary to create a new ACS release, including new branch, version and rio updates.
To use it, change to the `apple` directory, edit the `create_release_branch.sh` file so that the variables match the current and target verions, then execute the script.

What you need to edit:
```
MAJOR_VERSION=1
MINOR_VERSION=15
PATCH_VERSION=1
ACS_VERSION=0
# BCC = binary compatibility check
# BCC version must be the previous patch version of a release
BCC_MAJOR_VERSION=1
BCC_MINOR_VERSION=15
BCC_PATCH_VERSION=0
BCC_ACS_VERSION=0
```

The script assumes you are currently on the branch `release-$MAJOR_VERSION.$MINOR_VERSION-acs` and the current snapshot version is `$MAJOR_VERSION.$MINOR_VERSION.$PATCH_VERSION-acs-SNAPSHOT`.

Binary compatibility is enforced between patch releases from version `1.15.2.2-acs`.
This effectively means binary compatibility will be checked between the following versions:
* $MAJOR_VERSION.$MINOR_VERSION.$PATCH_VERSION.$ACS_VERSION-acs
* $BCC_MAJOR_VERSION.$BCC_MINOR_VERSION.$BCC_PATCH_VERSION.$BCC_ACS_VERSION-acs
