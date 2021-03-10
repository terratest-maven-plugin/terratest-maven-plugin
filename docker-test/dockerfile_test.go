package test

import (
	"testing"

	"github.com/gruntwork-io/terratest/modules/docker"
	"github.com/stretchr/testify/assert"
)

func TestRoadmappioDockerfile(t *testing.T) {
	// Configure the tag to use on the Docker image.
	tag := "roadmappio"
	buildOptions := &docker.BuildOptions{
		Tags: []string{tag},
	}

	// Build the Docker image.
	docker.Build(t, "../", buildOptions)

	//Test if app directory present
	optsForAppDir := &docker.RunOptions{Command: []string{"ls", "/"}}
	outputDorAppDir := docker.Run(t, tag, optsForAppDir)
	assert.Contains(t, outputDorAppDir, "app")

	//Test jar file presence
	optsForJar := &docker.RunOptions{Command: []string{"ls", "/app"}}
	outputForJar := docker.Run(t, tag, optsForJar)
	assert.Contains(t, outputForJar, "roadmappio-build-1.0.0-SNAPSHOT.jar")

	//Test jdk version
	optsForJavaVersion := &docker.RunOptions{Command: []string{"java", "-version"}}
	outputForJavaVersion := docker.Run(t, tag, optsForJavaVersion)
	assert.Contains(t, outputForJavaVersion, "openjdk version \"11.0.10\"")

	//Test for app dir rights
	optsForRights := &docker.RunOptions{Command: []string{"stat", "-c", "%U %G", "/app"}}
	outputForRights := docker.Run(t, tag, optsForRights)
	assert.Equal(t, "roadmappio roadmappio", outputForRights)

	//Test for jar rights
	optsForJarRights := &docker.RunOptions{Command: []string{"stat", "-c", "%U %G", "/app/roadmappio-build-1.0.0-SNAPSHOT.jar"}}
	outputForJarRights := docker.Run(t, tag, optsForJarRights)
	assert.Equal(t, "roadmappio roadmappio", outputForJarRights)

}
