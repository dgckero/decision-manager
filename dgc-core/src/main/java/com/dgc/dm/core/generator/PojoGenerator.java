/*
  @author david
 */
package com.dgc.dm.core.generator;

import com.dgc.dm.core.dto.CommonDto;
import javassist.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class PojoGenerator {

    public static Class generate(final String className, final Map<String, Class<?>> properties)
            throws NotFoundException, CannotCompileException, IOException {

        final CtClass cc = PojoGenerator.getCtcClass(className);

        PojoGenerator.populateClass(cc, properties.entrySet());

        return cc.toClass();
    }

    private static void populateClass(final CtClass cc, final Set<Map.Entry<String, Class<?>>> props) throws CannotCompileException {
        for (final Map.Entry<String, Class<?>> entry : props) {
            final Class<?> clazz = (entry.getValue().getSimpleName().equals("Email")) ? String.class : entry.getValue();

            cc.addField(new CtField(PojoGenerator.resolveCtClass(clazz), entry.getKey(), cc));
            // add getter
            cc.addMethod(PojoGenerator.generateGetter(cc, entry.getKey(), clazz));
            // add setter
            cc.addMethod(PojoGenerator.generateSetter(cc, entry.getKey(), clazz));
        }
        // add toString
        cc.addMethod(PojoGenerator.generateToString(cc, props));
    }

    private static CtClass getCtcClass(final String className) throws IOException, CannotCompileException, NotFoundException {
        final ClassPool pool = ClassPool.getDefault();

        pool.importPackage("com.dgc.dm.core");
        pool.appendClassPath(new LoaderClassPath(CommonDto.class.getClassLoader()));

        final CtClass cc = pool.makeClass(className);
        cc.writeFile();
        if (cc.isFrozen()) {
            cc.defrost();
        }

        cc.setSuperclass(pool.get(CommonDto.class.getName()));

        return cc;
    }

    private static CtMethod generateGetter(final CtClass declaringClass, final String fieldName, final Class fieldClass)
            throws CannotCompileException {

        final String getterName = "get" + StringUtils.capitalize(fieldName);

        final String sb = "public " + fieldClass.getName() + " " + getterName + "(){" +
                "return this." + fieldName + ";" + "}";
        return CtMethod.make(sb, declaringClass);
    }

    private static CtMethod generateSetter(final CtClass declaringClass, final String fieldName, final Class fieldClass)
            throws CannotCompileException {

        final String setterName = "set" + StringUtils.capitalize(fieldName);

        final String sb = "public void " + setterName + "(" + fieldClass.getName() + " " +
                fieldName + ")" + "{" + "this." + fieldName + "=" +
                fieldName + ";" + "}";
        return CtMethod.make(sb, declaringClass);
    }

    private static CtMethod generateToString(final CtClass declaringClass, final Set<Map.Entry<String, Class<?>>> props) throws CannotCompileException {

        String sb = "public String toString() { return ";

        for (final Map.Entry<String, Class<?>> entry : props) {
            sb += "+\"" + entry.getKey() + ":\"+" + entry.getKey() + "\n ";
        }
        sb += ";}";
        sb = sb.replaceFirst("\\+", "");
        return CtMethod.make(sb, declaringClass);
    }

    private static CtClass resolveCtClass(final Class clazz) {
        final ClassPool pool = ClassPool.getDefault();
        return pool.getOrNull(clazz.getName());
    }

}
