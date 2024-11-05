package com.lvt.khvip.validate;

import org.primefaces.validate.ClientValidator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.Map;

@FacesValidator("compareComponentValidator")
public class CompareComponentValidator implements Validator, ClientValidator {

    public CompareComponentValidator() {

    }

    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value != null && !value.toString().isEmpty()) {
            String label = (component.getAttributes().get("msglabel") != null) ? component.getAttributes()
                    .get("msglabel").toString() : "";

            Object compValue = (component.getAttributes().get("compare_value") != null) ? component.getAttributes()
                    .get("compare_value").toString() : "";

            if (!value.equals(compValue)) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                        label + " không khớp"));
            }
        }
    }

    public Map<String, Object> getMetadata() {
        return null;
    }

    public String getValidatorId() {
        return "compareComponentValidator";
    }

}