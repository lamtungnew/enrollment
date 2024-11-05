package com.lvt.khvip.util;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;

import com.lvt.khvip.client.author.dto.UserPermissionDto;
import com.lvt.khvip.entity.User;

import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SessionUtils {

	public static HttpSession getSession() {
		if (FacesContext.getCurrentInstance() != null) {
			return (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		}
		return null;
	}

	public static HttpServletRequest getRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	}

	public static boolean getPermissions(String moduleCode, String actionCode) {
		try {
			HttpSession session = getSession();
			if (session != null) {
				String key = moduleCode + "_" + actionCode;
				Object obj = session.getAttribute(key);
				if (obj != null) {
					return (boolean) obj;
				}
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return false;
	}

	public static Boolean getFilterPermissions(String url, HttpSession ses) {
		try {
			if (ses != null) {
				Object obj = ses.getAttribute(url);
				if (obj != null) {
					return (boolean) obj;
				}
			}
			return null;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return false;
	}
	
	public static Boolean getFilterPermissions(String url) {
		try {
			HttpSession ses = getSession();
			if (ses != null) {
				Object obj = ses.getAttribute(url);
				if (obj != null) {
					return (boolean) obj;
				}
			}
			return null;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return false;
	}

	public static void setPermissions(List<UserPermissionDto> permissions) {
		try {
			if (!CollectionUtils.isEmpty(permissions)) {
				HttpSession session = getSession();
				for (UserPermissionDto item : permissions) {
					if ("VIEW".equals(item.getModuleActionCode()) && item.getModuleUrl() != null) {
						session.setAttribute(item.getModuleUrl(), item.isAllow());
					} else {
						String key = item.getModuleCode() + "_" + item.getModuleActionCode();
						session.setAttribute(key, item.isAllow());
					}
				}
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	public static String getUserName() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		return session.getAttribute("username").toString();
	}

	public static String getUserId() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		return session.getAttribute("userId").toString();
	}

	public static String getPeopleId() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		return session.getAttribute("peopleId").toString();
	}
}
