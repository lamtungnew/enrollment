/**
 *
 */
package com.lvt.khvip.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt.khvip.client.AuthorClient;
import com.lvt.khvip.client.RoleClient;
import com.lvt.khvip.client.ShiftClient;
import com.lvt.khvip.client.author.dto.ModuleRoleActionDto;
import com.lvt.khvip.client.author.dto.PermissionListDto;
import com.lvt.khvip.client.author.dto.Roles;
import com.lvt.khvip.client.dto.*;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.entity.ExcelResponseMessage;
import com.lvt.khvip.entity.ExcelTemplate;
import com.lvt.khvip.model.ColumnModel;
import com.lvt.khvip.model.ShiftDataModel;
import com.lvt.khvip.util.CommonFaces;
import com.lvt.khvip.util.ConfProperties;
import com.lvt.khvip.util.ExcelUtils;
import com.lvt.khvip.util.MessageProvider;
import com.lvt.khvip.util.SessionUtils;
import com.lvt.khvip.util.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean
@ViewScoped
@Getter
@Setter
@Slf4j
public class PermissionManagerController implements Serializable {
	private RoleClient roleClient;
	private List<Roles> dataModel;
	private Roles searchData;
	private Roles selectedItem;
	private boolean updateMode;

	private AuthorClient authorClient;
	private PermissionListDto permissionList;
	private List<ColumnModel> columns;
	private List<Map<String, Object>> permissions;
	private Map<String, ModuleRoleActionDto> mapUpdatePermissions;
	private final String moduleProperty = "module";

	@PostConstruct
	public void init() {
		roleClient = new RoleClient();
		selectedItem = new Roles();
		searchData = new Roles();

		authorClient = new AuthorClient();
		permissions = new ArrayList<>();
		permissionList = authorClient.getPermissions();
		mapUpdatePermissions = new HashMap<>();
		createDynamicColumns();
		buildPermissionDataTable();
	}

	private void buildPermissionDataTable() {
		try {
			permissions.clear();
			permissionList.getModules().forEach(module -> {
				if (1 == module.getStatus()) {
					Map<String, Object> map = new HashMap<>();
					columns.forEach(col -> {
						if (moduleProperty.equals(col.getProperty())) {
							map.put(moduleProperty, module.getName());
						} else {
							String keyPerm = module.getId() + "_"
									+ getRoleIdByCode(col.getProperty(), permissionList.getRoles());
							permissionList.getPermissions().get(keyPerm).forEach(item -> {
								item.setOldAllow(item.isAllow());
							});
							log.info("col: {} = {}", keyPerm, permissionList.getPermissions().get(keyPerm));
							map.put(col.getProperty(), permissionList.getPermissions().get(keyPerm));
						}
					});
					permissions.add(map);
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public ModuleRoleActionDto getAction(String property, Object celData) {
		try {
			if (moduleProperty.equals(property)) {
				return ModuleRoleActionDto.builder().moduleActionName(celData.toString()).build();
			} else {
				ModuleRoleActionDto moduleRoleActionDto = (ModuleRoleActionDto) celData;
				return moduleRoleActionDto;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public void changeAllow(String property, Object celData) {
		if (!moduleProperty.equals(property)) {
			ModuleRoleActionDto dataTemp = (ModuleRoleActionDto) celData;
			ModuleRoleActionDto data = ModuleRoleActionDto.builder().roleId(dataTemp.getRoleId())
					.moduleActionId(dataTemp.getModuleActionId()).allow(dataTemp.isAllow()).build();
			String key = data.getRoleId() + "_" + data.getModuleActionId();
//			if (data.isAllow() == data.isOldAllow()) {
//				mapUpdatePermissions.remove(key);
//			} else {
			mapUpdatePermissions.put(key, data);
//			}
			log.info("mapUpdatePermissions: {}", mapUpdatePermissions.values());
		}
	}

	public void updatePermissions() {
		if (mapUpdatePermissions != null && mapUpdatePermissions.size() > 0) {
			List<ModuleRoleActionDto> lstData = mapUpdatePermissions.values().stream().collect(Collectors.toList());
			boolean rs = authorClient.updatePermissions(lstData);
			if (rs) {
				buildPermissionDataTable();
				CommonFaces.showGrowlInfo("Cập nhật quyền thành công!");
			} else {
				CommonFaces.showGrowlError("Cập nhật quyền không thành công!");
			}
		} else {
			CommonFaces.showGrowlWarn("Không có quyền nào được thay đổi!");
		}

	}

	private long getRoleIdByCode(String roleCode, List<Roles> roles) {
		for (Roles role : roles) {
			if (roleCode.equals(role.getName())) {
				return role.getId();
			}
		}
		return -1;
	}

	public void goToCreatePage(Roles item) {

	}

	private void createDynamicColumns() {
		try {
			columns = new ArrayList<>();
			columns.add(new ColumnModel("Chức năng", moduleProperty));
			for (Roles role : permissionList.getRoles()) {
				columns.add(new ColumnModel(role.getDescription(), role.getName()));
			}
			log.info("columns: {}", new ObjectMapper().writeValueAsString(columns));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
