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

    private final String simpleNameClass;

    DatabaseColumnType(final String simpleNameClass) {
        this.simpleNameClass = simpleNameClass;
    }

    /**
     * Parse java types to database types
     *
     * @param columnClassName java type to be parsed
     * @return database type
     */
    public static String getDBClassByColumnType(final String columnClassName) {
        final DatabaseColumnType cl = valueOf(columnClassName.toUpperCase());

        switch (cl) {
            case INTEGER:
                return Constants.INTEGER_DB_CLASS;
            case DOUBLE:
                return Constants.REAL_DB_CLASS;
            case DATE:
            case STRING:
            case EMAIL:
            default:
                return Constants.TEXT_DB_CLASS;
        }
    }

    public static class Constants {
        public static final String EMAIL_TYPE = "Email";
        public static final String INTEGER_DB_CLASS = "INTEGER";
        public static final String TEXT_DB_CLASS = "TEXT";
        public static final String REAL_DB_CLASS = "REAL";
    }

}
