/*
  @author david
 */
package com.dgc.dm.core.generator;

import com.dgc.dm.core.dto.RowDataDto;
import javassist.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.dgc.dm.core.db.dao.DatabaseColumnType.Constants.EMAIL_TYPE;
import static org.apache.commons.lang3.StringUtils.stripAccents;

@Slf4j
public class PojoGenerator {

    private static final String P_ALPHA_P_DIGIT = "[^\\p{Alpha}\\p{Digit}]+";
    private static final Pattern LAST_PLUS = Pattern.compile("\\+");

    /**
     * Generate generates a Class object for the given className and a map, containing required properties
     *
     * @param className  name of the Class
     * @param properties map containing required properties
     * @return
     * @throws NotFoundException
     * @throws CannotCompileException
     * @throws IOException
     */
    public static Class generate(final String className, final Map<String, Class<?>> properties)
            throws NotFoundException, CannotCompileException, IOException {
        log.debug("[INIT] generate className: {}, properties: {}", className, properties);
        final CtClass cc = getCtcClass(className);
        populateClass(cc, properties.entrySet());
        log.debug("[END] generated class {}", className);
        return cc.toClass();
    }

    /**
     * Creates a new class (or interface) from the given className
     * Add package com.dgc.cm.core to new class
     * Appends RowDataDto to the end of the search path.
     *
     * @param className
     * @return
     * @throws IOException
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    private static CtClass getCtcClass(final String className) throws IOException, CannotCompileException, NotFoundException {
        log.debug("[INIT] getCtcClass className: {}", className);
        final ClassPool pool = ClassPool.getDefault();

        pool.importPackage("com.dgc.dm.core");
        pool.appendClassPath(new LoaderClassPath(RowDataDto.class.getClassLoader()));

        final CtClass cc = pool.makeClass(className);
        cc.writeFile();
        if (cc.isFrozen()) {
            cc.defrost();
        }
        cc.setSuperclass(pool.get(RowDataDto.class.getName()));
        log.debug("[END] generated CtClass: {}", cc);
        return cc;
    }

    /**
     * Add parameters from props Map to new class
     * Generate getter, setter and toString methods
     *
     * @param cc    new class to be generated
     * @param props map containing required properties
     * @throws CannotCompileException
     */
    private static void populateClass(final CtClass cc, final Set<Map.Entry<String, Class<?>>> props) throws CannotCompileException {
        log.debug("[INIT] populateClass cc: {}, props: {}", cc, props);
        for (final Map.Entry<String, Class<?>> entry : props) {
            final Class<?> clazz = getPropertyClass(entry.getValue());

            String propertyName = getPropertyNameByColumnName(entry.getKey());

            cc.addField(new CtField(resolveCtClass(clazz), propertyName, cc));
            // add getter
            cc.addMethod(generateGetter(cc, propertyName, clazz));
            // add setter
            cc.addMethod(generateSetter(cc, propertyName, clazz));
        }
        // add toString
        cc.addMethod(generateToString(cc, props));
        log.debug("[END] populateClass");
    }

    /**
     * Remove special characters in className
     *
     * @param className
     * @return className without special characters
     */
    public static String getPropertyNameByColumnName(String className) {
        log.debug("[INIT] getPropertyNameByColumnName className: {}", className);
        className = stripAccents(className);
        String propertyName = className.replaceAll(P_ALPHA_P_DIGIT, "");
        log.debug("[END] getPropertyNameByColumnName propertyName: {}", propertyName);
        return propertyName;
    }

    /**
     * Map Excel type to Java type
     * Mapping Email and Date to String
     *
     * @param columnClass
     * @return Java type mapped from columnClass
     */
    private static Class<?> getPropertyClass(final Class<?> columnClass) {
        log.debug("[INIT] getPropertyClass columnClass: {}", columnClass);
        Class<?> result = String.class;
        if (!EMAIL_TYPE.equals(columnClass.getSimpleName())) if (!Date.class.equals(columnClass)) {
            result = columnClass;
        }
        log.debug("[END] getPropertyClass result: {}", result);
        return result;
    }

    /**
     * Generate getter method for fieldName
     *
     * @param declaringClass new class generated
     * @param fieldName      property name
     * @param fieldClass     class of new property name
     * @return CMethod generated
     * @throws CannotCompileException
     */
    private static CtMethod generateGetter(final CtClass declaringClass, final String fieldName, final Class fieldClass)
            throws CannotCompileException {
        log.debug("[INIT] generateGetter declaringClass: {}, fieldName: {}, fieldClass: {}", declaringClass, fieldName, fieldClass);
        final String getterName = "get" + StringUtils.capitalize(fieldName);
        String sb = String.format("public %s %s(){return this.%s;}", fieldClass.getName(), getterName, fieldName);
        log.debug("[END] generateGetter");
        return CtMethod.make(sb, declaringClass);
    }

    /**
     * Generate getter method for fieldName
     *
     * @param declaringClass new class generated
     * @param fieldName      property name
     * @param fieldClass     class of new property name
     * @return CMethod generated
     * @throws CannotCompileException
     */
    private static CtMethod generateSetter(final CtClass declaringClass, final String fieldName, final Class fieldClass)
            throws CannotCompileException {
        log.debug("[INIT] generateSetter declaringClass: {}, fieldName: {}, fieldClass: {}", declaringClass, fieldName, fieldClass);
        final String setterName = "set" + StringUtils.capitalize(fieldName);
        final String sb = String.format("public void %s(%s %s){this.%s=%s;}", setterName, fieldClass.getName(), fieldName, fieldName, fieldName);
        log.debug("[END] generateSetter");
        return CtMethod.make(sb, declaringClass);
    }

    /**
     * Generate toString method based on property
     *
     * @param declaringClass new class generated
     * @param props          map containing property
     * @return CMethod generated
     * @throws CannotCompileException
     */
    private static CtMethod generateToString(final CtClass declaringClass, final Set<? extends Map.Entry<String, Class<?>>> props) throws CannotCompileException {
        log.debug("[INIT] generateToString declaringClass: {}, props: {}", declaringClass, props);
        StringBuilder sb = new StringBuilder("public String toString() { return ");

        for (final Map.Entry<String, Class<?>> entry : props) {
            sb.append("+\"").append(getPropertyNameByColumnName(entry.getKey())).append(":\"+").append(getPropertyNameByColumnName(entry.getKey())).append("\n ");
        }
        sb.append(";}");
        sb = new StringBuilder(LAST_PLUS.matcher(sb.toString()).replaceFirst(""));
        log.debug("[END] generateToString");
        return CtMethod.make(sb.toString(), declaringClass);
    }

    /**
     * Returns a reference to the CtClass object representing clazz
     *
     * @param clazz
     * @return reference to CtClass
     */
    private static CtClass resolveCtClass(final Class clazz) {
        log.debug("[INIT] resolveCtClass by clazz: {}", clazz);
        final ClassPool pool = ClassPool.getDefault();
        log.debug("[END] resolveCtClass by clazz: {}", clazz);
        return pool.getOrNull(clazz.getName());
    }

}
