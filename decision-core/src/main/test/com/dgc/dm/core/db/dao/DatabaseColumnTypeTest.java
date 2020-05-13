package com.dgc.dm.core.db.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseColumnTypeTest {

    @Test
    void testGetDBClassByColumnType_Double() {
        assertEquals(DatabaseColumnType.Constants.REAL_DB_CLASS, DatabaseColumnType.getDBClassByColumnType(Double.class.getSimpleName()));
    }

    @Test
    void testGetDBClassByColumnType_Date() {
        assertEquals(DatabaseColumnType.Constants.TEXT_DB_CLASS, DatabaseColumnType.getDBClassByColumnType(java.util.Date.class.getSimpleName()));
    }

    @Test
    void testGetDBClassByColumnType_String() {
        assertEquals(DatabaseColumnType.Constants.TEXT_DB_CLASS, DatabaseColumnType.getDBClassByColumnType(String.class.getSimpleName()));
    }

    @Test
    void testGetDBClassByColumnType_Email() {
        assertEquals(DatabaseColumnType.Constants.TEXT_DB_CLASS, DatabaseColumnType.getDBClassByColumnType(DatabaseColumnType.Constants.EMAIL_TYPE));
    }

    @Test
    void testGetDBClassByColumnType_Integer() {
        assertEquals(DatabaseColumnType.Constants.INTEGER_DB_CLASS, DatabaseColumnType.getDBClassByColumnType(Integer.class.getSimpleName()));
    }
}
