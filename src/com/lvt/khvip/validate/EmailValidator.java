package com.lvt.khvip.validate;

import org.primefaces.validate.ClientValidator;

import com.lvt.khvip.util.StringUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.Map;
import java.util.regex.Pattern;

@FacesValidator("emailValidator")
public class EmailValidator implements Validator, ClientValidator {

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private Pattern pattern;

    public EmailValidator() {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    @Override
    public void validate(FacesContext fc, UIComponent component, Object value) throws ValidatorException {
        if (StringUtils.isEmpty(value))
            return;
        if (!pattern.matcher(value.toString()).matches()) {
            String label = (component.getAttributes().get("msglabel") != null) ? component.getAttributes()
                    .get("msglabel").toString() : "Email";
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                    label + " không hợp lệ"));
        }
    }

    public boolean matcher(String email){
        if (StringUtils.isEmpty(email))
            return true;
        if (!pattern.matcher(email).matches()) {
            return false;
        }
        return true;
    }
    @Override
    public Map<String, Object> getMetadata() {
        return null;
    }


    @Override
    public String getValidatorId() {
        return "emailValidator";
    }
}