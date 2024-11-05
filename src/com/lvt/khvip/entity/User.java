
package com.lvt.khvip.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import com.lvt.khvip.client.AuthorClient;
import com.lvt.khvip.client.author.dto.UserPermissionDto;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.dao.LoginDAO;
import com.lvt.khvip.util.SessionUtils;

import lombok.Data;

@ManagedBean
@SessionScoped
@Data
public class User implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(User.class);
	private static final long serialVersionUID = 1094801825228386363L;
	private String peopleId;
	private int id;
	private String password;
	private String username;
	private Role role;
	private String fullName;
	private Integer manageGroupId;
	private Integer groupId;
	private Integer depId;
//	public static User user = new User();

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	// validate login
	public String validateUsernamePassword() {

		User valid = LoginDAO.validate(username, password);
		if (valid != null) {
			User user = new User();
			user.setId(valid.getId());
			user.setRole(valid.getRole());
			user.setPeopleId(valid.getPeopleId());
			user.setManageGroupId(valid.getManageGroupId());
			user.setUsername(valid.getUsername());
			user.setGroupId(valid.getGroupId());
			user.setDepId(valid.getDepId());
			HttpSession session = SessionUtils.getSession();
			session.setAttribute("username", username);
			session.setAttribute("peopleId", valid.getPeopleId());
			session.setAttribute("userId", valid.getId());
			session.setAttribute("user-info", user);

			AuthorClient authorClient = new AuthorClient();
			List<UserPermissionDto> permissionDtos = authorClient.getUserPermissions(username);
			log.info("permissions: {} > {}", user, permissionDtos);
			SessionUtils.setPermissions(permissionDtos);
		
			return "index.xhtml?faces-redirect=true";
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Sai tài khoản hoặc mật khẩu", "Vui lòng nhập lại tài khoản hoặc mật khẩu"));
			return "";
		}
	}

	// logout event, invalidate session
	public String logout() {
		HttpSession session = SessionUtils.getSession();
		session.invalidate();
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		return "login.xhtml?faces-redirect=true";
	}

	public static String isUserInRoles() {
		User user = getSessionUser();
		if (user == null) {
			return null;
		}
		return user.getRole().getName();
	}

	public static User getSessionUser() {
		User user = null;
		try {
			HttpSession session = SessionUtils.getSession();
			user = (User) session.getAttribute("user-info");
		} catch (Exception ex) {
			return null;
		}

//		if (user == null) {
//			FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
//			try {
//				FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml?faces-redirect=true");
//			} catch (IOException ex) {
//				log.error("Failed to redirect to login page", ex);
//			}
//		}

		return user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
