/**
 *
 */
package com.lvt.khvip.controller;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import com.lvt.khvip.util.CommonFaces;
import com.lvt.khvip.util.SessionUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@ManagedBean
@SessionScoped
@Getter
@Setter
@Slf4j
public class SessionController implements Serializable {

	public String presentFormTitle;

	@PostConstruct
	public void init() {

	}

	public boolean isAllow(String moduleCode, String actionCode) {
		Boolean rs = SessionUtils.getPermissions(moduleCode, actionCode);
		return rs == null ? false : rs;
	}
	
	public static Boolean isAllowMenu(String url) {
		Boolean rs = SessionUtils.getFilterPermissions(url);
		return rs == null ? false : rs;
	}

	public void setPresentFormTitle(String title) {
		presentFormTitle = title;
	}

	public void profileDetail() {
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			ServletContext servletContext = (ServletContext) context.getCurrentInstance().getExternalContext()
					.getContext();
			String path = servletContext.getContextPath() + "/faces";
			FacesContext.getCurrentInstance().getExternalContext().redirect(
					path + "/profile.xhtml?faces-redirect=true");
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			CommonFaces.showMessage(e.getMessage());
		}
	}
}
