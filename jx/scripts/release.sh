#!/usr/bin/env bash
set -e

echo $(jx-release-version) > VERSION
echo "Releasing jx-resources-plugin: version: $(cat VERSION)"

# disable excessive logging
export MAVEN_OPTS="-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"

mvn versions:set -DnewVersion=$(cat VERSION)

echo "Pushing new release tag"

jx step tag --version $(cat VERSION)

echo "Now deploying release"

mvn clean deploy -DaltDeploymentRepository=maven.jenkins-ci.org::default::https://repo.jenkins-ci.org/releases/ -P release

# TODO
#updatebot push-version --kind maven ${VERSION}
