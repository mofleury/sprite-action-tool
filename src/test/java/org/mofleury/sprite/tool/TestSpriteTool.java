package org.mofleury.sprite.tool;

import org.junit.Test;

public class TestSpriteTool {

    @Test
    public void shouldRun() throws Exception {
        String[] args = {"src/test/resources/root", "target/out.json"};
        SpriteTool.main(args);
    }
}