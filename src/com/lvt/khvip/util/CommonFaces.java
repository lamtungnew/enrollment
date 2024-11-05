package com.lvt.khvip.util;

import org.primefaces.PrimeFaces;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlBody;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommonFaces {
    public static String getRemoteAddr() {
        String ipAddress = null;
        if (FacesContext.getCurrentInstance() != null) {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                    .getRequest();
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
        }
        return ipAddress;
    }

    public static HttpServletRequest getRequest() {
        if (FacesContext.getCurrentInstance() != null) {
            return (HttpServletRequest) getExternalContext().getRequest();
        } else {
            return null;
        }

    }

    public static ExternalContext getExternalContext() {
        if (FacesContext.getCurrentInstance() != null) {
            return FacesContext.getCurrentInstance().getExternalContext();
        } else {
            return null;
        }
    }

    public static FacesContext getCurrentInstance() {
        return FacesContext.getCurrentInstance();
    }

    public static String getViewAttributeByKey(String key) {
        if (getCurrentInstance() != null) {
            List<UIComponent> lstComs = getCurrentInstance().getViewRoot().getChildren();
            for (UIComponent uiComponent : lstComs) {
                if (uiComponent instanceof HtmlBody) {
                    if (uiComponent.getAttributes().containsKey(key)) {
                        return uiComponent.getAttributes().get(key).toString();
                    }
                }
            }
        }
        return null;
    }

    public static String getCompAttributeByKey(UIComponent component, String key) {
        if (component.getAttributes().containsKey(key)) {
            return component.getAttributes().get(key).toString();
        }
        return null;
    }

    public static HttpServletResponse getHttpServletResponse() {
        return (HttpServletResponse) getExternalContext().getResponse();
    }

    public static HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) getExternalContext().getRequest();
    }

    public static void showLoading() {
        PrimeFaces.current().executeScript("PF('loadingVar').show();");
    }

    public static void hideLoading() {
        PrimeFaces.current().executeScript("PF('loadingVar').hide();");
    }
    public static void showDialog(String dialogVar) {
        PrimeFaces.current().executeScript("PF('" + dialogVar + "').show();");
    }

    public static void hideDialog(String dialogVar) {
        PrimeFaces.current().executeScript("PF('" + dialogVar + "').hide();");
    }

    public static String getRequestParams(String name) {
        Map<String, String> params =
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Object val = params.get(name);
        return val != null ? val.toString() : null;
    }

    public static void clearMessages() {
        Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
        while (msgIterator.hasNext()) {
            msgIterator.remove();
        }
    }

    public static void showMessage(FacesMessage.Severity severity, String title, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, title, message));
    }

    public static void showMessage(String title, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(title, message));
    }

    public static void showMessage(String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(message, ""));
    }

    public static void showMessageErrorId(String id, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(id, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, ""));
    }

    public static void showMessage(FacesMessage.Severity severity, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, message, ""));
    }

    public static void showMessageForId(String id, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(id, new FacesMessage(message, ""));
    }

    public static void showMessageError(String title, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message));
    }

    public static void showMessageError(String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, ""));
    }

    public static void showMessageWarn(String title, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, title, message));
    }

    public static void showMessageWarn(String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, message, ""));
    }


    public static void putSessionMap(String key, Object value) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getSessionMap().put(key, value);
    }

    public static Object getSessionMap(String key) {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getExternalContext().getSessionMap().get(key);
    }

    public static void removeSessionMap(String key) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getSessionMap().remove(key);
    }

    public static String getCurrentURL() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String url = ctx.getViewRoot().getViewId();

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
                .getRequest();
        url = url + "?";
        for (String paramKey : request.getParameterMap().keySet()) {
            url += paramKey + "=" + request.getParameter(paramKey) + "&";
        }
        return url;
    }
    
    
    private static void addGrowlMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().
                addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public static void showGrowlInfo(String message) {
    	addGrowlMessage(FacesMessage.SEVERITY_INFO, "Thông báo", message);
        PrimeFaces.current().ajax().update("growl");
    }

    public static void showGrowlWarn(String message) {
    	addGrowlMessage(FacesMessage.SEVERITY_WARN, "Cảnh báo", message);
        PrimeFaces.current().ajax().update("growl");
    }

    public static void showGrowlError(String message) {
    	addGrowlMessage(FacesMessage.SEVERITY_ERROR, "Thông báo lỗi", message);
        PrimeFaces.current().ajax().update("growl");
    }
}
