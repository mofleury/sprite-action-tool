package org.mofleury.sprite.tool;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.File;

public class TestSpriteTool {

    @Test
    public void shouldRun() throws Exception {
        String[] args = {"src/test/resources/root", "target/out.json"};
        SpriteTool.main(args);

        Assertions.assertThat(new File("target/out.json")).hasSameContentAs(new File("src/test/resources/expected.json"));
    }
}