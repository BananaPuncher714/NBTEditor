package io.github.bananapuncher714.nbteditor;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class PluginTest {

    @BeforeAll
    static void initiate() {
        MockBukkit.mock();
        MockBukkit.load(Plugin.class);
    }

    @AfterAll
    static void finish() {
        MockBukkit.unmock();
    }

    @Test
    void test() {
        System.out.println("test");
    }

}
