package com.agimatec.validation.routines;

import com.agimatec.validation.model.Validation;
import com.agimatec.validation.model.ValidationContext;

/**
 * Description: DO NOTHING VALIDATION (can be used to turn off standard validation)<br/>
 * User: roman.stumm <br/>
 * Date: 06.07.2007 <br/>
 * Time: 16:51:28 <br/>
 * Copyright: Agimatec GmbH 2008
 */
public class NOPValidation implements Validation {

    public void validate(ValidationContext context) {
        // do nothing
    }
}
