package com.agimatec.validation.jsr303.extensions;

import com.agimatec.validation.jsr303.AgimatecFactoryContext;
import com.agimatec.validation.jsr303.Jsr303MetaBeanFactory;
import com.agimatec.validation.jsr303.ValidationCollector;
import com.agimatec.validation.jsr303.util.SecureActions;
import com.agimatec.validation.model.Validation;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.Valid;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * Description: <br/>
 * User: roman <br/>
 * Date: 01.02.2010 <br/>
 * Time: 12:07:24 <br/>
 * Copyright: Agimatec GmbH
 */
public class MethodValidatorMetaBeanFactory extends Jsr303MetaBeanFactory {
    public MethodValidatorMetaBeanFactory(AgimatecFactoryContext factoryContext) {
        super(factoryContext);
    }

    public void buildMethodDescriptor(MethodBeanDescriptorImpl descriptor) {
        try {
            buildMethodConstraints(descriptor);
            buildConstructorConstraints(descriptor);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private void buildConstructorConstraints(MethodBeanDescriptorImpl beanDesc)
          throws InvocationTargetException, IllegalAccessException {
        beanDesc.setConstructorConstraints(new HashMap());

        for (Constructor cons : beanDesc.getMetaBean().getBeanClass()
              .getDeclaredConstructors()) {
            if (!factoryContext.getFactory().getAnnotationIgnores()
                  .isIgnoreAnnotations(cons)) {

                ConstructorDescriptorImpl consDesc =
                      new ConstructorDescriptorImpl(beanDesc.getMetaBean(), new Validation[0]);
                beanDesc.putConstructorDescriptor(cons, consDesc);

                Annotation[][] paramsAnnos = cons.getParameterAnnotations();
                int idx = 0;
                for (Annotation[] paramAnnos : paramsAnnos) {
                    processAnnotations(consDesc, paramAnnos, idx);
                    idx++;
                }
            }
        }
    }

    private void buildMethodConstraints(MethodBeanDescriptorImpl beanDesc)
          throws InvocationTargetException, IllegalAccessException {
        beanDesc.setMethodConstraints(new HashMap());

        for (Method method : beanDesc.getMetaBean().getBeanClass().getDeclaredMethods()) {
            if (!factoryContext.getFactory().getAnnotationIgnores()
                  .isIgnoreAnnotations(method)) {


                MethodDescriptorImpl methodDesc = new MethodDescriptorImpl(
                      beanDesc.getMetaBean(), new Validation[0]);
                beanDesc.putMethodDescriptor(method, methodDesc);

                // return value validations
                ValidationCollectorGeneric validations = new ValidationCollectorGeneric();
                for (Annotation anno : method.getAnnotations()) {
                    processAnnotation(anno, methodDesc, validations);
                }
                methodDesc.getConstraintDescriptors().addAll(
                      (List)validations.getValidations());

                // parameter validations
                Annotation[][] paramsAnnos = method.getParameterAnnotations();
                int idx = 0;
                for (Annotation[] paramAnnos : paramsAnnos) {
                    processAnnotations(methodDesc, paramAnnos, idx);
                    idx++;
                }
            }
        }
    }

    private void processAnnotations(ProcedureDescriptor methodDesc, Annotation[] paramAnnos,
                                    int idx)
          throws InvocationTargetException, IllegalAccessException {
        ValidationCollectorGeneric validations = new ValidationCollectorGeneric();
        for (Annotation anno : paramAnnos) {
            processAnnotation(anno, methodDesc, validations);
        }
        ParameterDescriptorImpl paramDesc = new ParameterDescriptorImpl(
              methodDesc.getMetaBean(), validations.getValidations().toArray(
              new Validation[validations.getValidations().size()]));
        paramDesc.setIndex(idx);
        methodDesc.getParameterDescriptors().add(paramDesc);
    }

    private void processAnnotation(Annotation annotation, ProcedureDescriptor desc,
                                   ValidationCollector validations)
          throws InvocationTargetException, IllegalAccessException {

        if (annotation instanceof Valid) {
            desc.setCascaded(true);
        } else {
            Constraint vcAnno = annotation.annotationType().getAnnotation(Constraint.class);
            if (vcAnno != null) {
                Class<? extends ConstraintValidator<?, ?>>[] validatorClasses;
                validatorClasses = findConstraintValidatorClasses(annotation, vcAnno);
                applyConstraint(annotation, validatorClasses, null,
                      desc.getMetaBean().getBeanClass(), null, validations);
            } else {
                /**
                 * Multi-valued constraints
                 */
                Object result = SecureActions.getAnnotationValue(annotation, ANNOTATION_VALUE);
                if (result != null && result instanceof Annotation[]) {
                    for (Annotation each : (Annotation[]) result) {
                        processAnnotation(each, desc, validations); // recursion
                    }
                }
            }
        }
    }
}