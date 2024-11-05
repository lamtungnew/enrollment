package com.lvt.khvip.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.constant.StatusConstant;
import com.lvt.khvip.entity.Detection;
import com.lvt.khvip.entity.People;
import com.lvt.khvip.response.DataResponseFromRegisterFaceSearch;
import com.lvt.khvip.response.DataResponseFromRemoveFacesSearch;
import com.lvt.khvip.util.HttpUtils;
import com.lvt.khvip.util.Utility;
import com.lvt.khvip.util.Utils;

@ManagedBean
@SessionScoped
public class HistoryPeopleSignedUpDao {
	private static final Logger log = LoggerFactory.getLogger(HistoryPeopleSignedUpDao.class);

	public static List<Detection> getPeopleSignedUp(String peopleId, String status, String fromTime, String toTime) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Detection detection = null;
		try {
			List<Detection> detectionList = new ArrayList<>();
			String sql = " SELECT  "
					+ "    d.id, "
					+ "    d.people_id, "
					+ "    CONCAT('" + Constants.URL_IMAGE + "', d.captured_image_path) AS url, "
					+ "    p.full_name, "
					+ "    ct.name, "
					+ "    p.Gender, "
					+ "    p.date_of_birth, "
					+ "    d.created_time, "
					+ "    g.group_name, "
					+ "    ca.camera_name, "
            		+ "    p.customer_type, "
            		+ "    p.mobile_phone, "
            		+ "    p.group_id, "
            		+ "    d.liveness_quick, "
            		+ "    d.day_first_time, "
            		+ "    d.day_noon_time "
					+ "FROM "
					+ "    People p "
					+ "        RIGHT JOIN "
					+ "    Detection d ON p.people_id = d.people_id "
					+ "        LEFT JOIN "
					+ "    CustomerType ct ON p.customer_type = ct.id "
					+ "        LEFT JOIN "
					+ "    groups g ON p.group_id = g.group_id "
					+ "        LEFT JOIN "
					+ "    camera ca ON ca.camera_id = d.camera_id "
					+ "where (liveness_status <> 'FALSE' and liveness_status <> 'false') ";
			if (!Utils.isEmpty(peopleId)) {
				sql += "AND d.people_id like '%' || ? || '%' ";
			}
			if (!Utils.isEmpty(status)) {
				if (StatusConstant.Detection.STATUS_TRUE.equals(status)) {
					sql += "AND (d.liveness_quick = ? OR d.liveness_quick is null) ";
				} else {
					sql += "AND d.liveness_quick = ? ";
				}
			}
			if (!Utils.isEmpty(fromTime)) {
				sql += "AND DATE(d.created_time)  >= ? ";
			}
			if (!Utils.isEmpty(toTime)) {
				sql += "AND DATE(d.created_time) <= ? ";
			}
			sql += "ORDER BY d.created_time desc limit 1000 ";
			
			connection = ConnectDB.getConnection();
            ps = connection.prepareStatement(sql);
            int i = 1;
            if (!Utils.isEmpty(peopleId)) {
            	ps.setString(i++, peopleId);
            }
            if (!Utils.isEmpty(status)) {
            	ps.setString(i++, status);
            }
            if (!Utils.isEmpty(fromTime)) {
            	ps.setString(i++, fromTime);
            }
            if (!Utils.isEmpty(toTime)) {
            	ps.setString(i++, toTime);
            }
            
			rs = ps.executeQuery();
			while (rs.next()) {
				detection = new Detection();
				detection.setId(rs.getInt("id"));
				detection.setPeopleId(rs.getString("people_id"));
				detection.setCapturedImagePath(rs.getString("url"));
				detection.setFullName(rs.getString("full_name"));
				detection.setCustomerType(rs.getString("name"));
				detection.setGender(rs.getString("Gender"));
				detection.setDateOfBirth(rs.getString("date_of_birth"));
				detection.setCreatedTime(rs.getString("created_time"));
				detection.setCameraName(rs.getString("camera_name"));
				detection.setGroupName(rs.getString("group_name"));
				detection.setCustomerTypeId(rs.getInt("customer_type"));
				detection.setMobilePhone(rs.getString("mobile_phone"));
				detection.setGroupId(rs.getInt("group_id"));
				detection.setLivenessStatus(rs.getString("liveness_quick"));
				detection.setDayFirstTime(rs.getString("day_first_time"));
				detection.setDayNoonTime(rs.getString("day_noon_time"));
				detectionList.add(detection);
			}
			log.info("Total Records Fetched: " + detectionList.size());
			
			return detectionList;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new ArrayList<>();
		} finally {
			Utility.closeObject(rs);
			Utility.closeObject(ps);
			Utility.closeObject(connection);
		}
	}

	public static String updatePeople(People people, Detection detection) {

		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = ConnectDB.getConnection();
			connection.setAutoCommit(false);
			
			String sql = " update people p set full_name = ?, date_of_birth = ?, customer_type = ?, group_id = ?, image_path = ?, mobile_phone = ?, gender = ? "
            		+ " where people_id = ? ";
            ps = connection.prepareStatement(sql);
            ps.setString(1, people.getFullName());
            ps.setString(2, people.getBirthday().toString());
            ps.setInt(3, people.getCustomerTypeId());
            ps.setInt(4, people.getGroupId());
			ps.setString(5, people.getImagePathNoHost());
            ps.setString(6, people.getMobilePhone());
            ps.setString(7, people.getGender());
            ps.setString(8, people.getPeopleId());
            ps.executeUpdate();
            Utility.closeObject(ps);

			String sqlUpdateDetection = " update Detection set people_id = ?, created_time = ? where id = ? ";
			ps = connection.prepareStatement(sqlUpdateDetection);
			ps.setString(1, detection.getPeopleId());
			ps.setString(2, detection.getCreatedTime());
			ps.setInt(3, detection.getId());
			ps.executeUpdate();
			Utility.closeObject(ps);
			
			HttpUtils httpUtils = new HttpUtils();
			// gọi api xoá khuôn mặt
			DataResponseFromRemoveFacesSearch dataRemoveResponse = httpUtils.callAPIToRemove(detection.getPeopleId());
			if (StatusConstant.STATUS_SUCCESS.equals(dataRemoveResponse.getStatus()) 
					|| StatusConstant.STATUS_PEOPLE_NOT_FOUND_ERROR.equals(dataRemoveResponse.getStatus())
					|| StatusConstant.STATUS_PEOPLE_DO_NOT_EXITS_ERROR.equals(dataRemoveResponse.getStatus())) {
				// gọi api đăng ký khuôn mặt
				DataResponseFromRegisterFaceSearch dataResponse = httpUtils.callAPIToCreate(detection.getCapturedImagePath(), detection.getPeopleId());
				if (StatusConstant.STATUS_SUCCESS.equals(dataResponse.getStatus())) {
					connection.commit();
					log.info("Cập nhật SUCCESS");
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Cập nhật thành công nhân viên ", ""));
				} else {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cập nhật nhân viên thất bại", ""));
					throw new Exception("Failed to reg face: " + dataResponse.getStatus());
				}
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cập nhật nhân viên thất bại", ""));
				throw new Exception("Failed to remove face: " + dataRemoveResponse.getStatus());
			}
			
			return "";
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return "";
		} finally {
			Utility.closeObject(ps);
			Utility.closeObject(connection);
		}
	}

	public static int deletePeople(int id) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			String sql = "delete from People where people_id = " + id;
			ps = ConnectDB.getConnection().prepareStatement(sql);
			ps.executeUpdate();
			log.info("Delete PeopleId: " + id);

			return id;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			Utility.closeObject(ps);
			Utility.closeObject(connection);
		}
	}
	
	public static Detection getPeopleSignedUpById(int id) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Detection detection = null;
		try {
			String sql = " SELECT  "
					+ "    d.id, "
					+ "    d.people_id, "
					+ "    CONCAT('" + Constants.URL_IMAGE + "', d.captured_image_path) AS url, "
					+ "    p.full_name, "
					+ "    ct.name, "
					+ "    p.Gender, "
					+ "    p.date_of_birth, "
					+ "    d.created_time, "
					+ "    g.group_name, "
					+ "    ca.camera_name, "
            		+ "    p.customer_type, "
            		+ "    p.mobile_phone, "
            		+ "    p.group_id, "
            		+ "    d.liveness_quick, "
            		+ "    d.day_first_time, "
            		+ "    d.day_noon_time "
					+ "FROM "
					+ "    People p "
					+ "        RIGHT JOIN "
					+ "    Detection d ON p.people_id = d.people_id "
					+ "        LEFT JOIN "
					+ "    CustomerType ct ON p.customer_type = ct.id "
					+ "        LEFT JOIN "
					+ "    groups g ON p.group_id = g.group_id "
					+ "        LEFT JOIN "
					+ "    camera ca ON ca.camera_id = d.camera_id "
					+ "where (liveness_status <> 'FALSE' and liveness_status <> 'false') "
					+ "    and d.id = ? ";
			
			connection = ConnectDB.getConnection();
            ps = connection.prepareStatement(sql);
        	ps.setInt(1, id);
            
			rs = ps.executeQuery();
			if (rs.next()) {
				detection = new Detection();
				detection.setId(rs.getInt("id"));
				detection.setPeopleId(rs.getString("people_id"));
				detection.setCapturedImagePath(rs.getString("url"));
				detection.setFullName(rs.getString("full_name"));
				detection.setCustomerType(rs.getString("name"));
				detection.setGender(rs.getString("Gender"));
				detection.setDateOfBirth(rs.getString("date_of_birth"));
				detection.setCreatedTime(rs.getString("created_time"));
				detection.setCameraName(rs.getString("camera_name"));
				detection.setGroupName(rs.getString("group_name"));
				detection.setCustomerTypeId(rs.getInt("customer_type"));
				detection.setMobilePhone(rs.getString("mobile_phone"));
				detection.setGroupId(rs.getInt("group_id"));
				detection.setLivenessStatus(rs.getString("liveness_quick"));
				detection.setDayFirstTime(rs.getString("day_first_time"));
				detection.setDayNoonTime(rs.getString("day_noon_time"));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			Utility.closeObject(rs);
			Utility.closeObject(ps);
			Utility.closeObject(connection);
		}
		return detection;
	}
}
