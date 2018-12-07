#!/usr/bin/env bash
set -e

echo $(jx-release-version) > VERSION
echo "Releasing jx-resources-plugin version $(cat VERSION)"

mvn versions:set -DnewVersion=$(cat VERSION)
    
git tag -fa v$(cat VERSION) -m "Release version $(cat VERSION)"
git push origin v$(cat VERSION)

mvn clean deploy -DaltDeploymentRepository=maven.jenkins-ci.org::default::https://repo.jenkins-ci.org/releases/ -P release

# TODO
#updatebot push-version --kind maven ${VERSION}
