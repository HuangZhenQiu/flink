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
MINOR_VERSION=14
PATCH_VERSION=5
ACS_VERSION=0
```

The script assumes you are currently on the branch `release-$MAJOR_VERSION.$MINOR_VERSION-acs` and the current snapshot version is `$MAJOR_VERSION.$MINOR_VERSION.$PATCH_VERSION-acs-SNAPSHOT`.
