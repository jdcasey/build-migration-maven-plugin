Build Migration Maven Plugin
============================

This plugin is meant to be a small collection of helpers to smooth the migration path from another build system, like Ant, to Maven. The plugins will probably all be very simple, yet they will address use cases that are currently not supported by any plugin, or are excessively complicated to configure.

The `main-artifact` Goal
------------------------

The `main-artifact` goal simply sets the file of the project's main artifact.

This can be useful if you're wrapping another build system's output with Maven pom.xml files, and need to get Maven to recognize the build output for purposes of installation or deployment. Its use is very simple:

    <project>
      [...]
      <build>
        <plugins>
          <plugin>
            <artifactId>maven-antrun-plugin</artifactId>
            <executions>
              <execution>
                <id>build-ant</id>
                <goals>
                  <goal>run</goal>
                </goals>
                <configuration>
                  <target>
                      <ant antfile="build.xml" inheritAll="true" inheritRefs="true" target="all"/>
                  </target>
                </configuration>
              </execution>
            </executions>
          </plugin>
          
          <plugin>
            <groupId>org.commonjava.maven.plugins</groupId>
            <artifactId>build-migration-maven-plugin</artifactId>
            <version>0.1</version>
            <executions>
              <execution>
                <id>main-artifact</id>
                <goals>
                  <goal>main-artifact</goal>
                </goals>
                <configuration>
                  <mainArtifact>build/project-1.1.jar</mainArtifact>
                </configuration>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </project>

By default, the `main-artifact` goal will run in the `package` phase, which means it will normally run after the `jar:jar` goal has run. If you want to disable the `jar:jar` goal, one way to do that is with something like the following:

    <plugin>
      <artifactId>maven-jar-plugin</artifactId>
      <configuration>
        <excludes>
          <exclude>**/*</exclude>
        </excludes>
        <skipIfEmpty>true</skipIfEmpty>
      </configuration>
    </plugin>

This tells the jar plugin to skip all classes, and then skip building the jar (since it would be empty).

                
                  