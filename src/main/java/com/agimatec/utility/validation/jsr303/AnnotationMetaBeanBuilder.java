package com.agimatec.utility.validation.jsr303;

import com.agimatec.utility.ReflectUtils;
import com.agimatec.utility.validation.MetaBeanBuilder;
import com.agimatec.utility.validation.model.Features;
import com.agimatec.utility.validation.model.MetaBean;
import com.agimatec.utility.validation.model.MetaProperty;

import javax.validation.*;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Description: process the class annotations for constraint validations
 * to build the MetaBean<br/>
 * User: roman.stumm <br/>
 * Date: 01.04.2008 <br/>
 * Time: 14:12:51 <br/>
 *
 */
public class AnnotationMetaBeanBuilder extends MetaBeanBuilder {
    private final Provider provider;

    public AnnotationMetaBeanBuilder(Provider provider) {
        this.provider = provider;
    }

    @Override
    protected MetaBean buildMetaBean(BeanInfo info) {
        MetaBean metabean = super.buildMetaBean(info);   // call super!
        try {
            applyAnnotations(metabean, info);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e.getTargetException());
        }
        return metabean;
    }

    /**
     * add the validation features to the metabean that come from jsr303
     * annotations in the beanClass
     */
    protected void applyAnnotations(MetaBean metabean, BeanInfo beanInfo)
            throws IllegalAccessException, InvocationTargetException {
        final Class<?> beanClass = beanInfo.getBeanDescriptor().getBeanClass();
        for (Class interfaceClass : beanClass.getInterfaces()) {
            processClass(interfaceClass, metabean);
        }

        // process class, superclasses and interfaces
        List<Class> classSequence = new ArrayList<Class>();
        Class theClass = beanClass;
        while (theClass != null) {
            classSequence.add(theClass);
            theClass = theClass.getSuperclass();
        }
        // start with superclasses and go down the hierarchy so that
        // the child classes are processed last to have the chance to overwrite some declarations
        // of their superclasses and that they see what they inherit at the time of processing
        for (int i = classSequence.size() - 1; i >= 0; i--) {
            Class eachClass = classSequence.get(i);
            processClass(eachClass, metabean);
        }
    }

    /**
     * process class annotations, field and method annotations
     *
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void processClass(Class<?> beanClass, MetaBean metabean)
            throws IllegalAccessException, InvocationTargetException {
        processAnnotations(metabean, null, beanClass);

        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            MetaProperty metaProperty = metabean.getProperty(field.getName());
            // create a property for those fields for which there is not yet a MetaProperty
            if (metaProperty == null) {
                metaProperty = new MetaProperty();
                metaProperty.setAccess(MetaProperty.ACCESS.FIELD);
                metaProperty.setName(field.getName());
                metaProperty.setType(field.getType());
                metabean.putProperty(metaProperty.getName(), metaProperty);
            }
            processAnnotations(metabean, metaProperty, field);
        }
        Method[] methods = beanClass.getDeclaredMethods();
        for (Method method : methods) {
            String propName = null;
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                propName = Introspector.decapitalize(method.getName().substring(3));
            } else
            if (method.getName().startsWith("is") && method.getParameterTypes().length == 0) {
                propName = Introspector.decapitalize(method.getName().substring(2));
            } else
            if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                propName = Introspector.decapitalize(method.getName().substring(3));
            }
            if (propName != null) {
                MetaProperty metaProperty = metabean.getProperty(propName);
                // only those methods, for which we have a MetaProperty
                if (metaProperty != null) {
                    processAnnotations(metabean, metaProperty, method);
                }
            }
        }
    }

    private void processAnnotations(MetaBean metabean, MetaProperty prop, AnnotatedElement element)
            throws IllegalAccessException, InvocationTargetException {
        for (Annotation annotation : element.getDeclaredAnnotations()) {
            processAnnotation(annotation, prop, metabean, element);
        }
    }

    private void processAnnotation(Annotation annotation, MetaProperty prop, MetaBean metabean,
                                   AnnotatedElement element)
            throws IllegalAccessException, InvocationTargetException {
        if (annotation instanceof Valid) {
            processValid(element, metabean, prop);
        } else if (annotation instanceof GroupSequence) {
            processGroupSequence((GroupSequence) annotation, metabean);
        } else if (annotation instanceof GroupSequences) {
            for (GroupSequence each : ((GroupSequences) annotation).value()) {
                processGroupSequence(each, metabean);
            }
        } else {
            /*
            * An annotation is considered a constraint
            * definition if its retention policy contains RUNTIME and if
            * the annotation itself is annotated with javax.validation.ConstraintValidator.
            */
            ConstraintValidator vcAnno =
                    annotation.annotationType().getAnnotation(ConstraintValidator.class);
            if (vcAnno != null) {
                applyConstraint(annotation, vcAnno.value(), metabean, prop);
            } else {
                /**
                 * Multi-valued constraints:
                 * To support this, the bean validation provider treats annotations
                 * with a value annotation element
                 * with a return type of an array of constraint annotations
                 * and whose retention is RUNTIME as a list of
                 * annotations that are processed by the Bean Validation implementation.
                 * This means that each constraint specified in
                 * the value element is applied to the target.
                 */
                Object result = getAnnotationValue(annotation, "value");
                if (result != null && result instanceof Annotation[]) {
                    for (Annotation each : (Annotation[]) result) {
                        processAnnotation(each, prop, metabean, element);
                    }
                }
            }
        }
    }

    private void processValid(AnnotatedElement element, MetaBean metabean, MetaProperty prop) {
        if (prop != null && prop.getMetaBean() == null && prop.getType() != null) {
            prop.putFeature(Features.Property.REF_CASCADE, Boolean.TRUE);
            if (Collection.class.isAssignableFrom(prop.getType())) { // determine beanType
                Class clazz;
                clazz = findBeanType(element, metabean, prop);
                if (clazz != null) {
                    prop.putFeature(Features.Property.REF_BEAN_TYPE, clazz);
                }
            }
        }
    }

    private void processGroupSequence(GroupSequence each, MetaBean metabean) {
        Map<String, String[]> groupSeqMap =
                metabean.getFeature(Jsr303Features.Bean.GROUP_SEQ);
        if (groupSeqMap == null) {
            groupSeqMap = new HashMap();
            metabean.putFeature(Jsr303Features.Bean.GROUP_SEQ, groupSeqMap);
        }
        groupSeqMap.put(each.name(), each.sequence());
    }

    private Class findBeanType(AnnotatedElement element, MetaBean metabean, MetaProperty prop) {
        Class clazz;
        if (element instanceof Field) {
            clazz = ReflectUtils.getBeanTypeFromField((Field) element);
        } else if (element instanceof Method) {
            Method m = (Method) element;
            if (m.getParameterTypes().length == 0) {
                clazz = ReflectUtils.getBeanTypeFromGetter(m);
            } else {
                clazz = ReflectUtils.getBeanTypeFromSetter(m);
            }
        } else {
            clazz = ReflectUtils.getBeanType(metabean.getBeanClass(), prop.getName());
        }
        return clazz;
    }

    private Object getAnnotationValue(Annotation annotation, String name)
            throws IllegalAccessException, InvocationTargetException {
        Method valueMethod = null;
        try {
            valueMethod = annotation.annotationType().getDeclaredMethod(name);
        } catch (NoSuchMethodException ex) { /* do nothing */ }
        if (null != valueMethod) {
            return valueMethod.invoke(annotation);
        }
        return null;
    }

    private void applyConstraint(Annotation annotation, Class<? extends Constraint> constraintClass,
                                 MetaBean metabean, MetaProperty prop)
            throws IllegalAccessException, InvocationTargetException {
        // The lifetime of a constraint validation implementation instance is undefined.
        Constraint constraint = provider.getConstraintFactory().getInstance(constraintClass);
        constraint.initialize(annotation);
        Object msg = getAnnotationValue(annotation, "message");
        Object groups = getAnnotationValue(annotation, "groups");
        if (!(msg instanceof String)) {
            msg = null;
        }
        if (groups instanceof String) {
            groups = new String[]{(String) groups};
        }
        if (!(groups instanceof String[])) {
            groups = null;
        }
        ConstraintValidation validation =
                new ConstraintValidation(constraint, (String) msg, (String[]) groups, annotation);
        if (prop != null) {
            prop.addValidation(validation);
        } else {
            metabean.addValidation(validation);
        }
    }
}
