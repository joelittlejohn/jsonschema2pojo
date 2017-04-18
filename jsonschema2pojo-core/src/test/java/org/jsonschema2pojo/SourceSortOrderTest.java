/**
 * Copyright © 2017 Dell Inc. or its subsidiaries. All Rights Reserved.
 * VCE Confidential/Proprietary Information
 */
package org.jsonschema2pojo;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class SourceSortOrderTest {
    @Test
    public void testTwoFilesAreCompared_FILES_FIRST() throws IOException
    {
        testTwoFilesAreCompared(SourceSortOrder.FILES_FIRST.getComparator());
    }

    @Test
    public void twoDirectoriesAreCompared_FILES_FIRST() throws IOException
    {
        testTwoDirectoriesAreCompared(SourceSortOrder.FILES_FIRST.getComparator());
    }

    @Test
    public void testTwoFilesAreCompared_SUBDIRS_FIRST() throws IOException
    {
        testTwoFilesAreCompared(SourceSortOrder.SUBDIRS_FIRST.getComparator());
    }

    @Test
    public void twoDirectoriesAreCompared_SUBDIRS_FIRST() throws IOException
    {
        testTwoDirectoriesAreCompared(SourceSortOrder.SUBDIRS_FIRST.getComparator());
    }

    private void testTwoFilesAreCompared(Comparator<File> fileComparator ) throws IOException
    {
        final File mockFileA = mockFile();
        final File mockFileB = mockFile();

        fileComparator.compare(mockFileA, mockFileB);
        verify(mockFileA, atLeast(1)).compareTo(mockFileB);
    }

    private void testTwoDirectoriesAreCompared(Comparator<File> fileComparator ) throws IOException
    {
        final File mockDirA = mockDirectory();
        final File mockDirB = mockDirectory();

        fileComparator.compare(mockDirA, mockDirB);
        verify(mockDirA, atLeast(1)).compareTo(mockDirB);
    }

    @Test
    public void filesBeforeDirectories_FILES_FIRST() throws IOException
    {
        final Comparator<File> fileComparator = SourceSortOrder.FILES_FIRST.getComparator();
        final File mockFile = mockFile();
        final File mockDir = mockDirectory();

        assertThat(fileComparator.compare(mockFile, mockDir), lessThan(0));
        assertThat(fileComparator.compare(mockDir, mockFile), greaterThan(0));

        verify(mockFile, never()).compareTo(any(File.class));
        verify(mockDir, never()).compareTo(any(File.class));
    }

    @Test
    public void filesBeforeDirectories_SUBDIRS_FIRST() throws IOException
    {
        final Comparator<File> fileComparator = SourceSortOrder.SUBDIRS_FIRST.getComparator();
        final File mockFile = mockFile();
        final File mockDir = mockDirectory();

        assertThat(fileComparator.compare(mockFile, mockDir), greaterThan(0));
        assertThat(fileComparator.compare(mockDir, mockFile), lessThan(0));

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
