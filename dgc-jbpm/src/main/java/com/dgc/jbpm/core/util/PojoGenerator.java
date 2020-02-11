package com.dgc.jbpm.core.util;

import com.dgc.jbpm.core.dto.CommonDto;
import javassist.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PojoGenerator {

    public static Class<? extends CommonDto> generate(String className, Map<String, Class<?>> properties)
            throws NotFoundException, CannotCompileException, IOException {

        CtClass cc = getCtcClass(className);

        populateClass(cc, properties.entrySet());

        return cc.toClass();
    }

    private static void populateClass(CtClass cc, Set<Entry<String, Class<?>>> props) throws NotFoundException, CannotCompileException {
        for (Entry<String, Class<?>> entry : props) {
            cc.addField(new CtField(resolveCtClass(entry.getValue()), entry.getKey(), cc));
            // add getter
            cc.addMethod(generateGetter(cc, entry.getKey(), entry.getValue()));
            // add setter
            cc.addMethod(generateSetter(cc, entry.getKey(), entry.getValue()));
        }
    }

    private static CtClass getCtcClass(String className) throws IOException, CannotCompileException, NotFoundException {
        ClassPool pool = ClassPool.getDefault();

        CtClass cc = pool.makeClass(className);
        cc.writeFile();
        cc.defrost();
        cc.setSuperclass(resolveCtClass(CommonDto.class));

        return cc;
    }

    private static CtMethod generateGetter(CtClass declaringClass, String fieldName, Class fieldClass)
            throws CannotCompileException {

        String getterName = "get" + StringUtils.capitalize(fieldName);

        StringBuffer sb = new StringBuffer();
        sb.append("public ").append(fieldClass.getName()).append(" ").append(getterName).append("(){")
                .append("return this.").append(fieldName).append(";").append("}");
        return CtMethod.make(sb.toString(), declaringClass);
    }

    private static CtMethod generateSetter(CtClass declaringClass, String fieldName, Class fieldClass)
            throws CannotCompileException {

        String setterName = "set" + StringUtils.capitalize(fieldName);

        StringBuffer sb = new StringBuffer();
        sb.append("public void ").append(setterName).append("(").append(fieldClass.getName()).append(" ")
                .append(fieldName).append(")").append("{").append("this.").append(fieldName).append("=")
                .append(fieldName).append(";").append("}");
        return CtMethod.make(sb.toString(), declaringClass);
    }

    private static CtClass resolveCtClass(Class clazz) throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        return pool.get(clazz.getName());
    }

}
