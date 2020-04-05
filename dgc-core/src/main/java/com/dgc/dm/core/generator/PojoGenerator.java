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

    public static Class generate(String className, Map<String, Class<?>> properties)
            throws NotFoundException, CannotCompileException, IOException {

        CtClass cc = getCtcClass(className);

        populateClass(cc, properties.entrySet());

        return cc.toClass();
    }

    private static void populateClass(CtClass cc, Set<Map.Entry<String, Class<?>>> props) throws CannotCompileException {
        for (Map.Entry<String, Class<?>> entry : props) {
            Class<?> clazz = (entry.getValue().getSimpleName().equals("Email")) ? String.class : entry.getValue();

            cc.addField(new CtField(resolveCtClass(clazz), entry.getKey(), cc));
            // add getter
            cc.addMethod(generateGetter(cc, entry.getKey(), clazz));
            // add setter
            cc.addMethod(generateSetter(cc, entry.getKey(), clazz));
        }
        // add toString
        cc.addMethod(generateToString(cc, props));
    }

    private static CtClass getCtcClass(String className) throws IOException, CannotCompileException, NotFoundException {
        ClassPool pool = ClassPool.getDefault();

        pool.importPackage("com.dgc.dm.core");
        pool.appendClassPath(new LoaderClassPath(CommonDto.class.getClassLoader()));

        CtClass cc = pool.makeClass(className);
        cc.writeFile();
        if (cc.isFrozen()) {
            cc.defrost();
        }

        cc.setSuperclass(pool.get(CommonDto.class.getName()));

        return cc;
    }

    private static CtMethod generateGetter(CtClass declaringClass, String fieldName, Class fieldClass)
            throws CannotCompileException {

        String getterName = "get" + StringUtils.capitalize(fieldName);

        String sb = "public " + fieldClass.getName() + " " + getterName + "(){" +
                "return this." + fieldName + ";" + "}";
        return CtMethod.make(sb, declaringClass);
    }

    private static CtMethod generateSetter(CtClass declaringClass, String fieldName, Class fieldClass)
            throws CannotCompileException {

        String setterName = "set" + StringUtils.capitalize(fieldName);

        String sb = "public void " + setterName + "(" + fieldClass.getName() + " " +
                fieldName + ")" + "{" + "this." + fieldName + "=" +
                fieldName + ";" + "}";
        return CtMethod.make(sb, declaringClass);
    }

    private static CtMethod generateToString(CtClass declaringClass, Set<Map.Entry<String, Class<?>>> props) throws CannotCompileException {

        String sb = "public String toString() { return ";

        for (Map.Entry<String, Class<?>> entry : props) {
            sb += "+\"" + entry.getKey() + ":\"+" + entry.getKey() + "\n ";
        }
        sb += ";}";
        sb = sb.replaceFirst("\\+", "");
        return CtMethod.make(sb, declaringClass);
    }

    private static CtClass resolveCtClass(Class clazz) {
        ClassPool pool = ClassPool.getDefault();
        return pool.getOrNull(clazz.getName());
    }

}
