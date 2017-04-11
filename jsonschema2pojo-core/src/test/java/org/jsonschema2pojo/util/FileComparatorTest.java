package org.jsonschema2pojo.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileComparatorTest
{
    private final FileComparator fileComparator = new FileComparator();

    @Test
    public void testTwoFilesAreCompared() throws IOException
    {
        final File mockFileA = mockFile();
        final File mockFileB = mockFile();

        fileComparator.compare(mockFileA, mockFileB);
        verify(mockFileA, atLeast(1)).compareTo(mockFileB);
    }

    @Test
    public void testTwoDirectoriesAreCompared() throws IOException
    {
        final File mockDirA = mockDirectory();
        final File mockDirB = mockDirectory();

        fileComparator.compare(mockDirA, mockDirB);
        verify(mockDirA, atLeast(1)).compareTo(mockDirB);
    }

    @Test
    public void filesBeforeDirectories() throws IOException
    {
        final File mockFile = mockFile();
        final File mockDir = mockDirectory();

        assertThat(fileComparator.compare(mockFile, mockDir), lessThan(0));
        assertThat(fileComparator.compare(mockDir, mockFile), greaterThan(0));

        verify(mockFile, never()).compareTo(any(File.class));
        verify(mockDir, never()).compareTo(any(File.class));
    }

    private File mockFile()
    {
        return mockFile(false);
    }

    private File mockDirectory()
    {
        return mockFile(true);
    }

    private File mockFile(final boolean isDirectory)
    {
        final File mockFile = mock(File.class);
        when(mockFile.isDirectory()).thenReturn(isDirectory);
        return mockFile;
    }
}
