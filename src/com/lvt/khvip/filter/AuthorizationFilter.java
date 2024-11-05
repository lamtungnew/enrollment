package com.lvt.khvip.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lvt.khvip.constant.RoleConstant;
import com.lvt.khvip.entity.User;
import com.lvt.khvip.util.SessionUtils;

import lombok.extern.slf4j.Slf4j;

@WebFilter(filterName = "AuthFilter", urlPatterns = { "*.xhtml" })
@Slf4j
public class AuthorizationFilter implements Filter {

	public AuthorizationFilter() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {

			HttpServletRequest reqt = (HttpServletRequest) request;
			HttpServletResponse resp = (HttpServletResponse) response;
			HttpSession ses = reqt.getSession(false);

			String reqURI = reqt.getRequestURI();
			// nếu người dùng đã đăng nhập
			boolean pubUri = reqURI.indexOf("/login.xhtml") >= 0 || reqURI.indexOf("/public/") >= 0
				    || reqURI.indexOf("/403.xhtml") >= 0
					|| reqURI.contains("javax.faces.resource") || reqURI.indexOf("/register.xhtml") >= 0
					|| reqURI.indexOf("/index.xhtml") >= 0
					|| reqURI.indexOf("/profile.xhtml") >= 0
					|| reqURI.indexOf("/profile.xhtml") >= 0
					|| reqURI.indexOf("/egister-face.xhtml") >= 0 // bo
					|| reqURI.indexOf("/listPeople.xhtml") >= 0; // bo
			if (ses != null && ses.getAttribute("username") != null) {
				if (pubUri) {
					chain.doFilter(request, response);
				} else {
					String path = reqt.getPathInfo().replaceFirst("/", "");
					path = path.substring(0, (path.indexOf(".xhtml") + 6));

					if(path.indexOf("employee-detail.xhtml") >= 0){
						path = "employee-manager.xhtml";
					}

					if(path.indexOf("shift-manager-detail.xhtml") >= 0){
						path = "shift-manager.xhtml";
					}

					Boolean isAllow = SessionUtils.getFilterPermissions(path, ses);
					log.info("filter: {} - {}", path, isAllow);
//					if (isAllow == null || isAllow) {
					if (isAllow != null && isAllow) {
						chain.doFilter(request, response);
					} else {
						resp.sendRedirect(reqt.getContextPath() + "/faces/403.xhtml");
					}
				}
			} else {
				if (pubUri) {
					chain.doFilter(request, response);
				} else {
					resp.sendRedirect(reqt.getContextPath() + "/faces/login.xhtml");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {

	}
}