package com.agimatec.validation.jsr303;

import com.agimatec.validation.BeanValidator;
import com.agimatec.validation.MetaBeanManager;

import javax.validation.MessageResolver;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Description: a factory is a complete configurated object that can create validators<br/>
 * User: roman.stumm <br/>
 * Date: 29.10.2008 <br/>
 * Time: 17:06:20 <br/>
 * Copyright: Agimatec GmbH
 */
public class AgimatecValidatorFactory implements ValidatorFactory {
    private static AgimatecValidatorFactory DEFAULT_FACTORY;

    private MetaBeanManager metaBeanManager;
    private MessageResolver messageResolver;
    private BeanValidator beanValidator;

    /** convenience to retrieve a default global ValidatorFactory */
    public static AgimatecValidatorFactory getDefault() {
        if (DEFAULT_FACTORY == null) {
            AgimatecValidationProvider provider = new AgimatecValidationProvider();
            DEFAULT_FACTORY =
                    provider.buildValidatorFactory(new ValidatorBuilderImpl(null, provider));
        }
        return DEFAULT_FACTORY;
    }

    public AgimatecValidatorFactory() {
    }

    public <T> Validator<T> getValidator(Class<T> clazz) {
        return new ClassValidator(this, metaBeanManager.findForClass(clazz));
    }

    public void setMetaBeanManager(MetaBeanManager metaBeanManager) {
        this.metaBeanManager = metaBeanManager;
    }

    public void setMessageResolver(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    public MessageResolver getMessageResolver() {
        return messageResolver;
    }

    public MetaBeanManager getMetaBeanManager() {
        return metaBeanManager;
    }

    public void setBeanValidator(BeanValidator beanValidator) {
        this.beanValidator = beanValidator;
    }

    public BeanValidator getBeanValidator() {
        return beanValidator;
    }
}