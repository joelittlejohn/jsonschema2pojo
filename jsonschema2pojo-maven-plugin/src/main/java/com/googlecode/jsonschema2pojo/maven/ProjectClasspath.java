/**
 * Copyright © 2010-2011 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.jsonschema2pojo.maven;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.logging.Log;

/**
 * Represents the classpath built from a maven project's dependencies.
 */
public class ProjectClasspath {

    private final Set<Artifact> artifacts;
    private final ArtifactResolver artifactResolver;
    private final List<ArtifactRepository> remoteRepositories;
    private final ArtifactRepository localRepository;

    /**
     * Create a new project classpath with the given artifacts and helpers.
     * 
     * @param artifacts
     *            a list of artifacts gathered from a maven project's
     *            dependencies
     * @param artifactResolver
     *            an artifact resolver that can be used to resolve artifact
     *            references to file handles
     * @param remoteRepositories
     *            the remote repositories in scope for gathering artifacts
     * @param localRepository
     *            the local repository that can be used to gather artifacts
     */
    public ProjectClasspath(Set<Artifact> artifacts, ArtifactResolver artifactResolver, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository) {
        this.artifacts = artifacts;
        this.artifactResolver = artifactResolver;
        this.remoteRepositories = remoteRepositories;
        this.localRepository = localRepository;
    }

    /**
     * Provides a class loader that can be used to load classes from this
     * project classpath.
     * 
     * @param parent
     *            a classloader which should be used as the parent of the newly
     *            created classloader.
     * @param log
     *            object to which details of the found/loaded classpath elements
     *            can be logged.
     * 
     * @return a classloader that can be used to load any class that is
     *         contained in the set of artifacts that this project classpath is
     *         based on.
     * 
     * @throws ArtifactResolutionException
     *             if one of the artifacts in this classpath cannot be resolved
     *             due to a unexpected problem in the resolution process, e.g.
     *             error attempting to download from a remote repository
     * @throws ArtifactNotFoundException
     *             if an artifact in this classpath resolves to a file path that
     *             cannot be found
     */
    public ClassLoader getClassLoader(ClassLoader parent, Log log) throws ArtifactResolutionException, ArtifactNotFoundException {

        List<URL> classpathUrls = new ArrayList<URL>(artifacts.size());

        for (Artifact artifact : artifacts) {

            artifactResolver.resolve(artifact, remoteRepositories, localRepository);

            try {
                log.debug("Adding project dependency: " + artifact.getGroupId() + ":" + artifact.getArtifactId());
                classpathUrls.add(artifact.getFile().toURI().toURL());
            } catch (MalformedURLException e) {
                log.debug("Unable to use classpath entry as it could not be understood as a valid URL: " + artifact.getFile().getAbsolutePath(), e);
            }

        }

        return new URLClassLoader(classpathUrls.toArray(new URL[classpathUrls.size()]), parent);

    }

}
