# terratest-maven-plugin

This is a Maven plugin for running [Terratest](https://terratest.gruntwork.io)s or any go test in your repository.
Terratest is used to create infrastructure tests with popular languange go. With this plugin you can run these tests, creat HTML reports
out of the box with your beloved maven build pipeline.
You can use this plugin to run all your go tests, it has no explicit dependency on terratest.


[![CircleCI](https://circleci.com/gh/circleci/circleci-docs.svg?style=shield)](https://circleci.com/github/terratest-maven-plugin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Usage
Include in your <code>pom.xml</code>

    <build>
        ...
            <plugins>
                ...
                <plugin>
                    <groupId>io.jinfra</groupId>
                    <artifactId>terratest-maven-plugin</artifactId>
                    <version>1.0.0</version>
                    <configuration>
                        <terraTestPath>{[ABSOLUTE-PATH-TO-TERRATESTS]}</terraTestPath>
                    </configuration>
                    <executions>
                        <execution>
                            <goals>
                                <goal>run-tests</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
                ...
            </plugins>
            ...
    </build>

## Goals

| Goal                                   | Description                                                        | Default Lifecycle Phase |
| ---------------------------------------| ------------------------------------------------------------------ | ----------------------- |
| terratest:compile                      | Compile Terratests inside the <code>terratestPath</code> directory |           compile       |                       
| terratest:run-tests                    | Run go tests inside <code>terratestPath</code>                     |           test          |

## Required parameters
    <configuration>
       <terraTestPath>{[ABSOLUTE-PATH-TO-TERRATESTS]}</terraTestPath>
    </configuration>
Sets the working directory for the goal <i>compile</i> or <i>run-tests</i>

## Optional parameters

### Generating logs
You can generate log files into your <code>terraTestPath</code> directory from the stdOut and stdErr of the go test results.
Use the following parameter:

    <configuration>
        ...
        <createLogFile>true</createLogFile>
        ...
    </configuration>
This will generate an <code>terratest-output.log</code> and an <code>terratest-error-output.log</code> inside your <code>terraTestPath</code>

### Generating HTML report
<i>Only used in run-tests goal</i><br>
You can have an HTML report about your test results into your <code>terraTestPath</code> directory as <code>index.html</code>.

![HTML report example](https://media.giphy.com/media/QbvoQSpDIZgR76DNyz/giphy.gif)
<br>Use the following parameter:

    <configuration>
    ...
     <generateHtmlReport>true</generateHtmlReport>
    ...
    </configuration>

### Use json output
You can set the useJsonOutput parameter so go test results will be logged as json. This combined with <code>createLogFile</code> parameter eases further processing.

    <configuration>
    ...
     <useJsonOutput>true</useJsonOutput>
    ...
    </configuration>

Example output:

    {"Time":"2021-03-24T09:06:19.39012+01:00","Action":"run","Package":"io/jinfra/terratest/maven/plugin/tests/m/v2","Test":"TestValidDockerTestMavenProject"}
    {"Time":"2021-03-24T09:06:19.390426+01:00","Action":"output","Package":"io/jinfra/terratest/maven/plugin/tests/m/v2","Test":"TestValidDockerTestMavenProject","Output":"=== RUN   TestValidDockerTestMavenProject\n"}

### Skip tests
<i>Only used in run-tests goal</i><br>
terratest-maven-plugin integrates with the widely used <code>-DskipTests</code>, so if you run this for example:

    mvn clean install -DskipTests
your terratests won't run.
Of course you can set it directly in the plugin/pluginManagement section:

    <configuration>
    ...
    <skipTests>true</skipTests>
    ...
    </configuration>
The effect will be the same. If you want to compile your go tests even if tests are disabled use the <code>terratest:compile</code> goal, for example like this:

    <plugin>
        <groupId>io.jinfra</groupId>
        <artifactId>terratest-maven-plugin</artifactId>
        <configuration>
            <terraTestPath>${project.basedir}/docker-test</terraTestPath>
            </configuration>
        <executions>
            <execution>
                <goals>
                    <goal>compile</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
This will bound to compile [lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html) by default.
If you want to change this behaviour, use the <code>phase</code> parameter in execution.

### Disable test caching
<i>Only used in run-tests goal</i><br>
Go can use a mechanics called test caching. This is enabled by default but you can switch it off by using the following parameter:

    <configuration>
    ...
    <skipTests>true</skipTests>
    ...
    </configuration>

### Add further arguments
You can include further arguments with the go test or compiling.
use the following parameter:

    <configuration>
    ...
        <arguments>
            <param>arg1</param>
            <param>arg1</param>
        </arguments>
    ...
    </configuration>
For full reference of the arguments you can use with go test, see the [Go test reference](https://golang.org/pkg/cmd/go/internal/test/)

## Contribution
If you'd like to contribute, please feel free. This is an MIT licenced application so you can use it, extends it however you want. :)

### Build the project
Clone this project.
Use maven 3.0.0 or newer for building.
Once you done with your changes and sufficient amount of tests have been done, create a pull request.


