package com.agimatec.validation.jsr303;

import javax.validation.ConstraintDescriptor;
import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * Description: Describe a constraint validation defect<br/>
 * From rootBean and propertyPath, it is possible to rebuild the context of the failure
 * User: roman.stumm <br/>
 * Date: 01.04.2008 <br/>
 * Time: 14:50:12 <br/>
 * Copyright: Agimatec GmbH 2008
 */
class ConstraintViolationImpl<T> implements ConstraintViolation {
    private final String message;
    /** root bean validation was invoked on. */
    private final T rootBean;
    /** last bean validated. */
    private final Object leafBean;
    private final Object value;
    private final String propertyPath;
    private final Set<Class<?>> groups;
    private final ConstraintDescriptor constraintDescriptor;

    public ConstraintViolationImpl(String message, T rootBean, Object leafBean,
                                   String propertyPath, Object value,
                                   Set<Class<?>> groups,
                                   ConstraintDescriptor constraintDescriptor) {
        this.message = message;
        this.rootBean = rootBean;
        this.propertyPath = propertyPath;
        this.leafBean = leafBean;
        this.value = value;
        this.groups = groups;
        this.constraintDescriptor = constraintDescriptor;
    }

    /**
     * @deprecated use getMessage() instead
     */
    public String getInterpolatedMessage() {
        return getMessage();
    }

    /**
     * @deprecated use getMessageTemplate() instead
     * @return
     */
    public String getRawMessage() {
        return getMessage();
    }

    /**
     * former name getInterpolatedMessage()
     * @return The interpolated error message for this constraint violation.
     **/
    public String getMessage() {
        return message;
    }

    public String getMessageTemplate() {
        return message; // TODO RSt - getMessageTemplate nyi
    }

    /** Root bean being validated validated */
    public T getRootBean() {
        return rootBean;
    }

    public Object getLeafBean() {
        return leafBean;
    }

    /** The value failing to pass the constraint */
    public Object getInvalidValue() {
        return value;
    }

    /**
     * the property path to the value from <code>rootBean</code>
     * Null if the value is the rootBean itself
     */
    public String getPropertyPath() {
        return propertyPath;
    }

    /**
     * return the list of groups that the triggered constraint applies on and witch also are
     * within the list of groups requested for validation
     * (directly or through a group sequence)
     */
    public Set<Class<?>> getGroups() {
        return groups;
    }

    public ConstraintDescriptor getConstraintDescriptor() {
        return constraintDescriptor;
    }

    public String toString() {
        return "ConstraintViolationImpl{" + "rootBean=" + rootBean + ", propertyPath='" +
              propertyPath + '\'' + ", message='" + message + '\'' + ", leafBean=" +
              leafBean + ", value=" + value + '}';
    }
}
