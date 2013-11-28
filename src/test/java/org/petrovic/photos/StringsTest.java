package org.petrovic.photos;

import org.junit.Assert;
import org.junit.Test;

public class StringsTest {

    private String fileName = "a.png";

    @Test
    public void testExtension() throws Exception {
        Assert.assertEquals(Strings.extension(fileName), "png");
    }

    @Test
    public void testNameLessExtension() throws Exception {
        Assert.assertEquals(Strings.nameLessExtension(fileName), "a");
    }
}
