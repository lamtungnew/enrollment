package com.lvt.khvip.validate;


import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.lvt.khvip.util.StringUtils;

@FacesValidator("phoneValidator")
public class PhoneValidator implements Validator {

    @Override
    public void validate(FacesContext fc, UIComponent component, Object value) throws ValidatorException {
        if (StringUtils.isEmpty(value))
            return;
        if (!CommonValidator.isValidMobiNumber(value.toString())) {
            String label = (component.getAttributes().get("msglabel") != null) ? component.getAttributes()
                    .get("msglabel").toString() : "SĐT";
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                    label + " không hợp lệ"));
        }
    }


}