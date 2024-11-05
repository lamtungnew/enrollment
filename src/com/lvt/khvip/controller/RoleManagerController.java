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
public class RoleManagerController implements Serializable {
	private RoleClient roleClient;
	private List<Roles> dataModel;
	private Roles searchData;
	private Roles selectedItem;
	private boolean updateMode;

	@PostConstruct
	public void init() {
		roleClient = new RoleClient();
		selectedItem = new Roles();
		searchData = new Roles();

		changeMode(null);
		reloadListData();
	}

	private void reloadListData() {
		dataModel = roleClient.listRole();
	}

	public void search() {
		reloadListData();
	}

	public void goToCreatePage(Roles item) {
		if (item != null) {
			selectedItem = item;
		} else {
			updateMode = false;
			selectedItem = new Roles();
		}
	}

	public void add() {
		try {
			Date current = new Date();
			selectedItem.setId(null);
			selectedItem.setStatus(1);
			selectedItem.setCreatedTime(current);
			ResponseData<Roles> responseData = roleClient.create(selectedItem);
			if (responseData != null && StringUtils.isEmpty(responseData.getError())) {
				selectedItem = responseData.getData();
				CommonFaces.hideDialog("dlgDetailVar");
				CommonFaces.showGrowlInfo("Thêm mới thành công!");
			} else {
				CommonFaces.showMessageError("Thêm mới không thành công", responseData.getMessage());
			}
			reloadListData();
		} catch (Exception e) {
			e.printStackTrace();
			CommonFaces.showMessageError(e.getMessage());
		}
	}

	public void update() {
		try {
			ResponseData<Roles> responseData = roleClient.update(selectedItem);
			if (responseData != null && StringUtils.isEmpty(responseData.getError())) {
				selectedItem = responseData.getData();
				CommonFaces.hideDialog("dlgDetailVar");
				CommonFaces.showGrowlInfo("Sửa thông tin thành công!");
			} else {
				CommonFaces.showMessageError("Sửa thông tin không thành công", responseData.getMessage());
			}
			reloadListData();
		} catch (Exception e) {
			e.printStackTrace();
			CommonFaces.showMessageError(e.getMessage());
		}
	}

	public void delete(Roles item) {
		try {
			boolean rs = roleClient.delete(item.getId());
			if (rs) {
				CommonFaces.showMessage("Xóa thành công!");
				reloadListData();
				selectedItem = null;
			} else {
				CommonFaces.showMessageError("Xóa không thành công!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			CommonFaces.showMessageError(e.getMessage());
		}
	}

	public void onRowSelect(SelectEvent event) {
		selectedItem = (Roles) event.getObject();
	}

	public void onSelect(Roles item) {
		selectedItem = item;
	}

	public void changeMode(Roles item) {
		if (item != null) {
			updateMode = true;
			selectedItem = item;
		} else {
			updateMode = false;
			selectedItem = new Roles();
		}
	}

	public boolean isViewDetail() {
		return false;
	}

}
