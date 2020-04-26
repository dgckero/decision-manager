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

    DatabaseColumnType(final String simpleNameClass) {
    }

    public static String getDBClassByColumnType(final String columnClassName) {
        final DatabaseColumnType cl = valueOf(columnClassName.toUpperCase());

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

}
