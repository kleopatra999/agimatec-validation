package com.agimatec.utility.validation.xml;

import com.agimatec.utility.validation.model.FeaturesCapable;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 10.07.2007 <br/>
 * Time: 13:11:56 <br/>
 * Copyright: Agimatec GmbH 2008
 */
public class XMLFeaturesCapable implements Serializable {
    @XStreamImplicit
    private List<XMLMetaFeature> features;
    @XStreamImplicit(itemFieldName = "validator")
    private List<XMLMetaValidatorReference> validators;

    public List<XMLMetaFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<XMLMetaFeature> features) {
        this.features = features;
    }

    public void putFeature(String key, Object value) {
        XMLMetaFeature anno = findFeature(key);
        if (features == null) features = new ArrayList<XMLMetaFeature>();
        if (anno == null) {
            features.add(new XMLMetaFeature(key, value));
        } else {
            anno.setValue(value);
        }
    }

    public void removeFeature(String key) {
        XMLMetaFeature anno = findFeature(key);
        if(anno != null) {
            getFeatures().remove(anno);
        }
    }

    public Object getFeature(String key) {
        XMLMetaFeature anno = findFeature(key);
        return anno == null ? null : anno.getValue();
    }

    private XMLMetaFeature findFeature(String key) {
        if (features == null) return null;
        for (XMLMetaFeature anno : features) {
            if (key.equals(anno.getKey())) return anno;
        }
        return null;
    }
    
    public List<XMLMetaValidatorReference> getValidators() {
        return validators;
    }

    public void setValidators(List<XMLMetaValidatorReference> validators) {
        this.validators = validators;
    }

    public void addValidator(String validatorId) {
        if (validators == null) validators = new ArrayList<XMLMetaValidatorReference>();
        validators.add(new XMLMetaValidatorReference(validatorId));
    }

    public void mergeFeaturesInto(FeaturesCapable fc) {
        if (getFeatures() != null) {
            fc.optimizeRead(false);
            for (XMLMetaFeature each : getFeatures()) {
                fc.putFeature(each.getKey(), each.getValue());
            }
            fc.optimizeRead(true);
        }
    }
}
