package org.jsonschema2pojo.util;

import java.io.File;
import java.util.Comparator;

/**
 * A comparator that will sort files before directories.
 */
public class FileComparator implements Comparator<File>
{
    @Override
    public int compare(final File fileA, final File fileB)
    {
        if (fileA.isDirectory() && !fileB.isDirectory())
        {
            return 1;
        }

        if (!fileA.isDirectory() && fileB.isDirectory())
        {
            return -1;
        }
        return fileA.compareTo(fileB);
    }
}
