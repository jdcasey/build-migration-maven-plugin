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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

/**
 * Sets the main artifact for the current project to some specified file. This is useful for Maven
 * builds that simply wrap another build system, such that the wrapped system produces the project
 * artifact, and Maven needs a way to capture it for installation and deployment.
 */
@Mojo( name = "main-artifact", defaultPhase = LifecyclePhase.PACKAGE )
public class MainArtifactGoal extends AbstractMojo
{
    /**
     * The file that should be captured as the current project's main artifact output.
     */
    @Parameter
    private File mainArtifact;

    /**
     * The file that should be captured as the current project's main pom output.
     */
    @Parameter
    private File mainPom;

    /**
     * Attach an array of artifacts to the project.
     */
    @Parameter
    private Artifact[] artifacts;

    /**
     * Maven ProjectHelper.
     */
    @Component
    private MavenProjectHelper projectHelper;

    @Parameter (defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter ( defaultValue="${project.build.directory}", readonly = true )
    private File targetDir;

    @Parameter ( defaultValue="${project.build.finalName}", readonly = true )
    private String finalName;

    @Parameter (  property="mainArtifact.copyLocal" , defaultValue = "true" )
    private final boolean copyLocal = true;

    @Parameter( property="mainArtifact.failIfMissing", defaultValue = "false")
    private boolean failIfMissing;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( mainArtifact != null )
        {
            if ( mainArtifact.exists() )
            {
                if ( mainArtifact.isDirectory() )
                {
                    getLog().error( "Main artifact " + mainArtifact + " is a directory." );
                    throw new MojoFailureException(
                                    "You are not allowed to set the main-artifact to a directory! Directories cannot be installed or deployed properly." );
                }

                final File existing = project.getArtifact().getFile();
                if ( existing != null && !existing.isDirectory() )
                {
                    getLog().warn( "NOTE: Discarding pre-existing main-artifact file:\n  " + project.getArtifact().getFile() );
                }
                if ( copyLocal )
                {
                    final String ext = project.getArtifact().getArtifactHandler().getExtension();
                    final File dest = new File( targetDir, finalName + "." + ext );
                    targetDir.mkdirs();

                    getLog().info( "Copying main artifact from: " + mainArtifact + " to: " + dest + " for project: " + project.getId() );

                    copyFile( mainArtifact, dest );

                    project.getArtifact().setFile( dest );
                }
                else
                {
                    getLog().info( "Setting '" + mainArtifact + "' as main artifact file (" + project.getArtifact().getFile()
                                                   + ") for project: " + project.getId() );

                    project.getArtifact().setFile( mainArtifact );
                }
            }
            else if ( failIfMissing )
            {
                throw new MojoFailureException( "Cannot find main-artifact source: '" + mainArtifact + "'" );
            }
            else
            {
                getLog().warn( "CANNOT FIND: " + mainArtifact + ". NOT setting main artifact for project: " + project.getId() );
            }
        }

        if ( mainPom != null && mainPom.exists() )
        {
            getLog().info( "Replacing mainPom " + project.getFile() + " with " + mainPom );
            project.setFile( mainPom );
        }

        if ( artifacts != null )
        {
            for ( Artifact artifact : artifacts )
            {
                getLog().info( "Attaching " + artifact.getFile() + " ( type: " +
                                               artifact.getType() + ", classifier: " +
                                               artifact.getClassifier() + " )" );
                projectHelper.attachArtifact( this.project, artifact.getType(), artifact.getClassifier(),
                                              artifact.getFile() );
            }
        }
    }

    private void copyFile( final File sourceFile, final File destFile )
        throws MojoExecutionException, MojoFailureException
    {
        FileChannel source = null;
        FileChannel dest = null;

        try
        {
            if ( !destFile.exists() )
            {
                destFile.createNewFile();
            }

            source = new FileInputStream( sourceFile ).getChannel();
            dest = new FileOutputStream( destFile ).getChannel();

            dest.transferFrom( source, 0, source.size() );
        }
        catch ( final IOException e )
        {
            throw new MojoFailureException( "Failed to copy artifact from: " + sourceFile + " to: " + destFile
                + ". Reason: " + e, e );
        }
        finally
        {
            try
            {
                if ( source != null )
                {
                    source.close();
                }
                if ( dest != null )
                {
                    dest.close();
                }
            }
            catch ( final IOException e )
            {
                throw new MojoFailureException( "Failed to copy artifact from: " + sourceFile + " to: " + destFile
                    + ". Reason: " + e, e );
            }
        }
    }


    private void validateArtifacts()
                    throws MojoFailureException
    {
        // check unique of types and classifiers
        Set<String> extensionClassifiers = new HashSet<String>();
        for ( Artifact artifact : artifacts )
        {
            String extensionClassifier = artifact.getType() + ":" + artifact.getClassifier();

            if ( !extensionClassifiers.add( extensionClassifier ) )
            {
                throw new MojoFailureException( "The artifact with same type and classifier: " + extensionClassifier
                                                                + " is used more than once." );
            }
        }
    }
}
