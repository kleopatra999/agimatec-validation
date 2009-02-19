package com.agimatec.validation.constraints;

import com.agimatec.validation.routines.EMailValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Description: pattern for validation taken from hibernate.<br/>
 * User: roman.stumm <br/>
 * Date: 14.10.2008 <br/>
 * Time: 12:38:37 <br/>
 * Copyright: Agimatec GmbH
 */
public class EmailValidator implements ConstraintValidator<Email, String> {
    protected final EMailValidation validation = new EMailValidation();

    public boolean isValid(String value, ConstraintValidatorContext context) {
        return validation.isValid(value);
    }

    public void initialize(Email parameters) {
        // do nothing (as long as Email has no properties)
    }
}
