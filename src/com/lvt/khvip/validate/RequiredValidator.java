package com.lvt.khvip.validate;

import com.lvt.khvip.util.StringUtils;
import org.primefaces.validate.ClientValidator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.Map;

@FacesValidator("requiredValidator")
public class RequiredValidator implements Validator, ClientValidator {

    public RequiredValidator() {

    }

    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null || value.toString().isEmpty()) {
            String label = (component.getAttributes().get("msglabel") != null) ? component.getAttributes()
                    .get("msglabel").toString() : "";
            if (!StringUtils.isEmpty(label)) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                        label + " không được để trống"));
            } else {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                        "Thông tin nhập chưa đầy đủ. Vui lòng nhập lại !"));
            }
        }
    }

    public Map<String, Object> getMetadata() {
        return null;
    }

    public String getValidatorId() {
        return "requiredValidator";
    }

}