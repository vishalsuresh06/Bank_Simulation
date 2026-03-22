package com.boma.banksim.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTest {

    @Test
    void generate_returnsNonBlankString() {
        String id = IdGenerator.generate();
        assertNotNull(id);
        assertFalse(id.isBlank());
    }

    @Test
    void generate_returnsUuidFormat() {
        // UUID format: 8-4-4-4-12 = 36 chars with hyphens
        String id = IdGenerator.generate();
        assertEquals(36, id.length());
    }

    @Test
    void generate_twoCallsReturnDifferentIds() {
        String id1 = IdGenerator.generate();
        String id2 = IdGenerator.generate();
        assertNotEquals(id1, id2);
    }

    @Test
    void generateWithPrefix_startsWithPrefix() {
        String id = IdGenerator.generate("LN");
        assertTrue(id.startsWith("LN-"));
    }

    @Test
    void generateWithPrefix_totalLengthIsCorrect() {
        // prefix + "-" + 8 uppercase hex chars
        String id = IdGenerator.generate("CHK");
        assertEquals("CHK-".length() + 8, id.length());
    }

    @Test
    void generateWithPrefix_twoCallsReturnDifferentIds() {
        String id1 = IdGenerator.generate("DEP");
        String id2 = IdGenerator.generate("DEP");
        assertNotEquals(id1, id2);
    }

    @Test
    void generateWithPrefix_suffixIsUppercase() {
        String id = IdGenerator.generate("ACC");
        String suffix = id.substring("ACC-".length());
        assertEquals(suffix.toUpperCase(), suffix);
    }
}
