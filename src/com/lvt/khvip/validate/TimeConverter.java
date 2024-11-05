package com.lvt.khvip.validate;

import com.lvt.khvip.util.StringUtils;
import com.lvt.khvip.util.Utils;
import lombok.extern.slf4j.Slf4j;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.Date;

@FacesConverter("timeConverter")
@Slf4j
public class TimeConverter implements Converter {


    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        try {
            if (StringUtils.isEmpty(s))
                return null;
            return Utils.convertStringToDate(s, "HH:mm");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        try {
            if (o == null)
                return null;
            if (o instanceof Date) {
                return Utils.convertDateToString((Date) o, "HH:mm");
            } else {
                if (StringUtils.isEmpty(o.toString()))
                    return null;
                return Utils.convertDateToString(Utils.convertStringToDate(o.toString(), "EEE MMM dd HH:mm:ss z yyyy"), "HH:mm");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
