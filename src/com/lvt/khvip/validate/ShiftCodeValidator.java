package com.lvt.khvip.validate;

import com.lvt.khvip.util.StringUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FacesValidator("shiftCodeValidator")
public class ShiftCodeValidator implements Validator {

	private static final String USERNAME_PATTERN = "^[a-zA-Z0-9]{3,20}$";
	private Pattern pattern;
	private Matcher matcher;

	public ShiftCodeValidator() {
		pattern = Pattern.compile(USERNAME_PATTERN);
	}

	/**
	 * Validate username with regular expression
	 *
	 * @param username username for validation
	 * @return true valid username, false invalid username
	 */
	public boolean validate(final String username) {
		matcher = pattern.matcher(username);
		return matcher.matches();

	}

	@Override
	public void validate(FacesContext fc, UIComponent component, Object value) throws ValidatorException {

		if (StringUtils.isEmpty(value))
			return;
		if (!validate(value.toString())) {
			String label = (component.getAttributes().get("msglabel") != null)
					? component.getAttributes().get("msglabel").toString()
					: "Mã ca";
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "", label
					+ " không hợp lệ (chấp nhận các chữ cái, các số 0-9, và độ dài 6-20 ký tự)"));
		}

	}

}