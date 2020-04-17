/*
  @author david
 */
package com.dgc.dm.core.db.dao;

public enum DatabaseColumnType {
    DATE(java.util.Date.class.getSimpleName()),
    STRING(java.lang.String.class.getSimpleName()),
    DOUBLE(java.lang.Double.class.getSimpleName()),
    INTEGER(java.lang.Integer.class.getSimpleName()),
    EMAIL("Email");

    private final String simpleNameClass;

    DatabaseColumnType(final String simpleNameClass) {
        this.simpleNameClass = simpleNameClass;
    }

    public static String getDBClassByColumnType(final String columnClassName) {
        final DatabaseColumnType cl = valueOf(columnClassName.toUpperCase());

        switch (cl) {
            case INTEGER:
                return "INTEGER";
            case DATE:
            case DOUBLE:
                return "REAL";
            case STRING:
            case EMAIL:
            default:
                return "TEXT";
        }
    }

}
