/**
 * Copyright © 2010-2013 Nokia
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

package org.jsonschema2pojo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.apache.commons.io.FilenameUtils.*;
import static org.apache.commons.io.FileUtils.*;

/**
 * Maps files to package names.
 * 
 * @author Christian Trimble
 *
 */
public class PackageMapper {
    private SortedSet<PackageMapping> packageMappings = new TreeSet<PackageMapping>();
    
    public PackageMapper withPackageMapping( File source, String packageName ) throws IOException {
        packageMappings.add(
          (source.isDirectory()?new DirectoryPackageMapping():new FilePackageMapping())
          .withSource(source)
          .withPackageName(packageName));
        
        return this;
    }
    
    public String map( URI systemId ) throws IOException {
        return map(systemId.normalize().toString());
    }
    
  public String map( File file ) throws IOException {
      if( !file.exists() ) { return ""; }
      
      return map(file.getCanonicalFile().toURI().toString());
  }
  
  public String map( String systemId ) throws IOException {
      for( PackageMapping packageMapping : packageMappings ) {
          if( packageMapping.appliesTo(systemId) ) {
              return packageMapping.map(systemId);
          }
      }
      return "";      
  }
  
  public String packageNameForSchemaPath( String basePackage, String path ) {
      String[] segments = getFullPathNoEndSeparator(normalize(path))
        .split(Pattern.quote(File.separator));
      
      StringBuilder sb = new StringBuilder();
      if( basePackage != null && !"".equals(basePackage) ) {
          sb.append(basePackage);
      }
      for( String segment : segments ) {
          if( !segment.equals("") ) {
            sb.append(".").append(replaceIllegalCharacters(segment));
          }
      }
      return sb.toString();
  }
  
  public static String relativePath( String parentPath, String childPath ) {
      return childPath.substring(parentPath.length());
  }
  
  private static final String ILLEGAL_CHARACTER_REGEX = "[^0-9a-zA-Z_$]";

  public static String replaceIllegalCharacters(String name) {
      return name.replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
  }
    
    public abstract class PackageMapping implements Comparable<PackageMapping> {
        String source;
        String packageName;
        
        public PackageMapping withSource( File source ) throws IOException {
            this.source = source.getCanonicalFile().toURI().toString().replaceAll("/$", "");
            if( source.isDirectory() ) {
                this.source = this.source+"/";
            }
            return this;
        }
        
        public PackageMapping withSource( URI source ) throws IOException {
            URI normalized = source.normalize();
            try {
                normalized = new URI(normalized.getScheme(), normalized.getSchemeSpecificPart(), null);
            } catch (URISyntaxException e) {
                throw new IOException("could not remove fragment from "+source.toString());
            }
            this.source = normalized.toString();
            return this;
        }
        
        public PackageMapping withPackageName( String packageName ) {
            this.packageName = packageName;
            return this;
        }
        
        public boolean appliesTo( String path ) throws IOException {
            return directoryContains(source, path);
        }
        
        public String map( String path ) {
            return packageNameForSchemaPath( packageName, relativePath(source, path));
        }
        
        @Override
        public int compareTo(PackageMapping pm) {
            int dirCompare = source.compareTo(pm.source);
            int packageCompare = packageName.compareTo(pm.packageName);
            
            return dirCompare != 0 ? -dirCompare : packageCompare;
        }
        
        @Override
        public String toString() {
            return "{source:"+source+", packageName:"+packageName+"}";
        }
    }
    
    public class DirectoryPackageMapping extends PackageMapping {
        public boolean appliesTo( String path ) throws IOException {
            return path.startsWith(source);
        }
        
        public String map( String path ) {
            return packageNameForSchemaPath( packageName, relativePath(source, path));
        }        
    }
    
    public class FilePackageMapping extends PackageMapping {

        @Override
        public boolean appliesTo(String path) throws IOException {
           return source.equals(path);
        }

        @Override
        public String map(String path) {
            return packageName;
        }
    }
    
    public class UriPackageMapping extends PackageMapping {

        @Override
        public boolean appliesTo(String path) throws IOException {
            return source.equals(path);
        }

        @Override
        public String map(String path) {
            return packageName;
        }
        
    }
}
