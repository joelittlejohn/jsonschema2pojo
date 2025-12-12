/**
 * Copyright © 2010-2014 Nokia
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
package org.jsonschema2pojo.gradle

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.jupiter.api.Test

class GradleBuildIT {

  @Test
  void java() {
    build("example/java");
  }

  void build(String projectDir) {
    GradleConnector connector = GradleConnector.newConnector()
    connector.useGradleVersion("8.9")
    connector.forProjectDirectory(new File(projectDir))
    ProjectConnection connection = connector.connect()
    try {
      BuildLauncher launcher = connection.newBuild()
      launcher.setStandardOutput(System.out);
      launcher.setStandardError(System.err);
      launcher.forTasks("build")
      launcher.addArguments("--stacktrace")
      launcher.run()
    } finally {
      connection.close()
    }
  }

}
