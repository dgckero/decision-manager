/*
  @author david
 */
package com.dgc.dm.core.db.dao;

/**
 * Enum to parse java types to database types
 */
public enum DatabaseColumnType {
    DATE(java.util.Date.class.getSimpleName()),
    DOUBLE(Double.class.getSimpleName()),
    EMAIL(Constants.EMAIL_TYPE),
    INTEGER(Integer.class.getSimpleName()),
    STRING(String.class.getSimpleName());

    DatabaseColumnType(String simpleNameClass) {
    }

    /**
     * Parse java types to database types
     *
     * @param columnClassName java type to be parsed
     * @return database type
     */
    public static String getDBClassByColumnType(String columnClassName) {
        DatabaseColumnType cl = valueOf(columnClassName.toUpperCase());

        switch (cl) {
            case INTEGER:
                return "INTEGER";
            case DOUBLE:
                return "REAL";
            case DATE:
            case STRING:
            case EMAIL:
            default:
                return "TEXT";
        }
    }

    public static class Constants {
        public static final String EMAIL_TYPE = "Email";
    }

}
