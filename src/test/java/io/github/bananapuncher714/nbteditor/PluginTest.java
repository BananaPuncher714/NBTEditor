package io.github.bananapuncher714.nbteditor;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

final class PluginTest {

    @BeforeAll
    static void initiate() {
        MockBukkit.mock();
        MockBukkit.load(Plugin.class);
    }

    @Test
    void test() {

    }

    @AfterAll
    static void finish() {
        MockBukkit.unmock();
    }

}
