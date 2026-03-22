package com.boma.banksim.customer;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private CustomerProfile profile() {
        return new CustomerProfile(60_000, 0.6, 700, 0.3, 0.4);
    }

    private Customer valid() {
        return new Customer("C1", "Alice", CustomerType.RETAIL, profile());
    }

    @Test
    void constructor_setsAllFields() {
        Customer c = valid();
        assertEquals("C1", c.getCustomerId());
        assertEquals("Alice", c.getName());
        assertEquals(CustomerType.RETAIL, c.getType());
        assertNotNull(c.getProfile());
    }

    @Test
    void constructor_blankId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer("", "Alice", CustomerType.RETAIL, profile()));
    }

    @Test
    void constructor_nullId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(null, "Alice", CustomerType.RETAIL, profile()));
    }

    @Test
    void constructor_blankName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer("C1", "  ", CustomerType.RETAIL, profile()));
    }

    @Test
    void constructor_nullType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer("C1", "Alice", null, profile()));
    }

    @Test
    void constructor_nullProfile_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer("C1", "Alice", CustomerType.RETAIL, null));
    }

    @Test
    void addAccountId_stored() {
        Customer c = valid();
        c.addAccountId("ACC1");
        assertTrue(c.getAccountIds().contains("ACC1"));
    }

    @Test
    void addAccountId_duplicate_ignoredSilently() {
        Customer c = valid();
        c.addAccountId("ACC1");
        c.addAccountId("ACC1");
        assertEquals(1, c.getAccountIds().size());
    }

    @Test
    void addAccountId_blank_throws() {
        assertThrows(IllegalArgumentException.class, () -> valid().addAccountId(""));
    }

    @Test
    void removeAccountId_removesFromList() {
        Customer c = valid();
        c.addAccountId("ACC1");
        c.removeAccountId("ACC1");
        assertFalse(c.getAccountIds().contains("ACC1"));
    }

    @Test
    void getAccountIds_returnsUnmodifiableList() {
        Customer c = valid();
        c.addAccountId("ACC1");
        List<String> ids = c.getAccountIds();
        assertThrows(UnsupportedOperationException.class, () -> ids.add("ACC2"));
    }

    @Test
    void hasLoans_noLoans_false() {
        assertFalse(valid().hasLoans());
    }

    @Test
    void hasLoans_afterAddingLoanId_true() {
        Customer c = valid();
        c.addLoanId("LN1");
        assertTrue(c.hasLoans());
    }

    @Test
    void addLoanId_duplicate_ignoredSilently() {
        Customer c = valid();
        c.addLoanId("LN1");
        c.addLoanId("LN1");
        assertEquals(1, c.getLoanIds().size());
    }
}
