/*
 * Copyright 2012 Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commonjava.maven.plugins.migrate;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Sets the main artifact for the current project to some specified file. This is useful for Maven 
 * builds that simply wrap another build system, such that the wrapped system produces the project
 * artifact, and Maven needs a way to capture it for installation and deployment.
 * 
 * @goal main-artifact
 * @phase package
 */
public class MainArtifactGoal
    implements Mojo
{

    /**
     * The file that should be captured as the current project's main artifact output.
     * 
     * @parameter
     * @required
     */
    private File mainArtifact;

    /**
     * @parameter default-value="${project}"
     */
    private MavenProject project;

    private Log log;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( mainArtifact.exists() )
        {
            if ( mainArtifact.isDirectory() )
            {
                throw new MojoFailureException( "You are not allowed to set the main-artifact to a directory! Directories cannot be installed or deployed properly." );
            }

            getLog().info( "Setting '" + mainArtifact + "' as main artifact file for project: " + project.getId() );

            final File existing = project.getArtifact().getFile();
            if ( existing != null && !existing.isDirectory() )
            {
                getLog().warn( "NOTE: Discarding pre-existing main-artifact file:\n  "
                    + project.getArtifact().getFile() );
            }

            project.getArtifact().setFile( mainArtifact );
        }
    }

    public synchronized Log getLog()
    {
        if ( log == null )
        {
            log = new SystemStreamLog();
        }

        return log;
    }

    public void setLog( final Log log )
    {
        this.log = log;
    }

}
