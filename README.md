## Jenkins X Resources Plugin

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/jx-resources.svg)](https://plugins.jenkins.io/jx-resources) [![Build Status](https://ci.jenkins.io/job/Plugins/job/jx-resources-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/jx-resources-plugin/job/master/) ![Apache 2](http://img.shields.io/badge/license-Apache%202-red.svg)

This plugin adds support for modifying `PipelineActivity` resources as pipelines run. It exposes Pipeline run status to the Jenkins X `PipelineActivity` Custom Resources in Kubernetes

### Documentation

- [Plugin Javadoc](https://javadoc.jenkins.io/plugin/jx-resources/)
- [Changelog](https://github.com/jenkinsci/jx-resources-plugin/releases)

  > Older versions of this plugin may not be safe to use. Please review the following warnings before using an older version: [CSRF vulnerability and missing permission check](https://jenkins.io/security/advisory/2019-06-11/#SECURITY-1379)