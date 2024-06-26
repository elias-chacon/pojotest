package io.github.eliaschacon.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class autogenerated by TestGenerator for POJOS. 
 * Do not modify please!
 */
public class UserTest {

    @Test
    public void shouldSetAndGetId() {
        final var user = new User();
        user.setId(1);

        final var result = user.getId();

        assertNotNull(result);
        assertEquals(1, (java.lang.Integer) result);
    }

    @Test
    public void shouldSetAndGetName() {
        final var user = new User();
        user.setName("a string");

        final var result = user.getName();

        assertNotNull(result);
        assertEquals("a string", (java.lang.String) result);
    }

    @Test
    public void shouldSetAndGetAddress() {
        final var user = new User();
        user.setAddress("a string", 1, "a string", "a string");

        final var result = user.getAddress();

        assertNotNull(result);
    }

}
