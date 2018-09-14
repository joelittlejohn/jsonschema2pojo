package org.jsonschema2pojo;

/**
 * The strategy used for generating type names.
 * Embedding {@code javaType} attribute into the schema is always given precedence.
 */
public enum TypeNameStrategy {
    /**
     * Use the file name for top-level types and property name for sub-types.
     */
    FILE_AND_PROPERTY_NAME,

    /**
     * Use the title attribute. Falls back to file/property names
     * if no title is given.
     */
    TITLE_ATTRIBUTE
}
