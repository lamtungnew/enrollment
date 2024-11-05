package com.lvt.khvip.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("checkboxConverter")
public class CheckboxConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null) {
            return null;
        }
        switch (value) {
            case "0":
                return Boolean.FALSE;
            case "1":
                return Boolean.TRUE;
            default:
                throw new ConverterException();
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Boolean bool = (Boolean) value;
        if (bool == null) {
            return null;
        }
        return bool ? "1" : "0";
    }

}
