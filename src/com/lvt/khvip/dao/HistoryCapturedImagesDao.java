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
import com.lvt.khvip.entity.CapturedImages;
import com.lvt.khvip.entity.People;
import com.lvt.khvip.response.DataResponseFromRegisterFaceSearch;
import com.lvt.khvip.response.DataResponseFromRemoveFacesSearch;
import com.lvt.khvip.util.HttpUtils;
import com.lvt.khvip.util.Utility;
import com.lvt.khvip.util.Utils;

@ManagedBean
@SessionScoped
public class HistoryCapturedImagesDao {
	private static final Logger log = LoggerFactory.getLogger(HistoryCapturedImagesDao.class);

    public static List<CapturedImages> getPeopleCapturedImages(String peopleId, String status, String fromTime, String toTime) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        CapturedImages images = null;
        List<CapturedImages> imageList = new ArrayList<>();
        try {
            String sql = " SELECT  "
            		+ "    c.id, "
            		+ "    c.people_id, "
            		+ "    CONCAT('" + Constants.URL_IMAGE + "', c.path_image) AS url, "
            		+ "    p.full_name, "
            		+ "    ct.name, "
            		+ "    g.group_name, "
            		+ "    p.Gender, "
            		+ "    p.date_of_birth, "
            		+ "    c.captured_time, "
            		+ "    ca.camera_name, "
            		+ "    p.status, "
            		+ "    p.customer_type, "
            		+ "    p.mobile_phone, "
            		+ "    c.liveness_status "
            		+ "FROM "
            		+ "    People p "
            		+ "        RIGHT JOIN "
            		+ "    CapturedImages c ON p.people_id = c.people_id "
            		+ "        LEFT JOIN "
            		+ "    CustomerType ct ON p.customer_type = ct.id "
            		+ "        LEFT JOIN "
            		+ "    groups g ON p.group_id = g.group_id "
            		+ "        LEFT JOIN "
            		+ "    camera ca ON ca.camera_id = c.camera_id "
            		+ " WHERE 1=1 ";
            if (!Utils.isEmpty(peopleId)) {
            	sql += "AND c.people_id like '%' || ? || '%' ";
            }
            if (!Utils.isEmpty(status)) {
            	if (StatusConstant.CapturedImages.STATUS_TRUE.equals(status)) {
            		sql += "AND (c.liveness_status = ? OR c.liveness_status is null) ";
            	} else {
            		sql += "AND c.liveness_status = ? ";
            	}
            }
            if (!Utils.isEmpty(fromTime)) {
                sql += "AND DATE(c.captured_time) >= ? " ;
            }
            if (!Utils.isEmpty(toTime)) {
                sql += "AND DATE(c.captured_time) <= ? " ;
            }
            sql += "ORDER BY c.captured_time desc limit 1000 ";

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
                images = new CapturedImages();
                images.setId(rs.getInt("id"));
                images.setPeopleId(rs.getString("people_id"));
                images.setCapturedImagePath(rs.getString("url"));
                images.setFullName(rs.getString("full_name"));
                images.setCustomerType(rs.getString("name"));
                images.setGender(rs.getString("Gender"));
                images.setDateOfBirth(rs.getString("date_of_birth"));
                images.setCreatedTime(rs.getString("captured_time"));
                images.setCameraName(rs.getString("camera_name"));
                images.setGroupName(rs.getString("group_name"));
                images.setStatus(rs.getInt("status"));
                images.setCustomerTypeId(rs.getInt("customer_type"));
                images.setMobilePhone(rs.getString("mobile_phone"));
                images.setLivenessStatus(rs.getString("liveness_status"));
                imageList.add(images);
            }

            log.info("Total Records Fetched: " + imageList.size());
            return imageList;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            return null;
        } finally {
            Utility.closeObject(rs);
            Utility.closeObject(ps);
            Utility.closeObject(connection);
        }
    }

    public static String updatePeople(People people, CapturedImages capturedImages) {

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectDB.getConnection();
            connection.setAutoCommit(false);

            String sql = " update people p set full_name = ?, date_of_birth = ?, customer_type = ?, group_id = ?, image_path = ?, mobile_phone = ?, gender = ? "
            		+ " where people_id = ? ";
            ps = connection.prepareStatement(sql);
            ps.setString(1, people.getFullName());
            ps.setString(2, people.getDateOfBirth());
            ps.setInt(3, people.getCustomerTypeId());
            ps.setInt(4, people.getGroupId());
			ps.setString(5, people.getImagePathNoHost());
            ps.setString(6, people.getMobilePhone());
            ps.setString(7, people.getGender());
            ps.setString(8, people.getPeopleId());
            ps.executeUpdate();
            Utility.closeObject(ps);

            String sqlUpdateCapturedImages = "update CapturedImages set people_id = ? where id = ?";
            ps = connection.prepareStatement(sqlUpdateCapturedImages);
            ps.setString(1, people.getPeopleId());
            ps.setInt(2, capturedImages.getId());
            ps.executeUpdate();
            Utility.closeObject(ps);

			HttpUtils httpUtils = new HttpUtils();
			// gọi api xoá khuôn mặt
			DataResponseFromRemoveFacesSearch dataRemoveResponse = httpUtils.callAPIToRemove(capturedImages.getPeopleId());
			if (StatusConstant.STATUS_SUCCESS.equals(dataRemoveResponse.getStatus())
					|| StatusConstant.STATUS_PEOPLE_NOT_FOUND_ERROR.equals(dataRemoveResponse.getStatus())
					|| StatusConstant.STATUS_PEOPLE_DO_NOT_EXITS_ERROR.equals(dataRemoveResponse.getStatus())) {
				// gọi api đăng ký khuôn mặt
				DataResponseFromRegisterFaceSearch dataResponse = httpUtils.callAPIToCreate(capturedImages.getCapturedImagePath(), capturedImages.getPeopleId());
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
            e.printStackTrace();
            return "";
        } finally {
            Utility.closeObject(ps);
            Utility.closeObject(connection);
        }
    }

    public static String saveNewPeople(People people, CapturedImages capturedImages) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectDB.getConnection();
            connection.setAutoCommit(false);

            String sqlInsertToPeople = "insert into People (people_id, full_name, date_of_birth, customer_type, group_id , image_path, mobile_phone, gender) values (?, ?, ?, ?, ?, ?, ?, ?)"
            		+ " on duplicate key update "
            		+ " full_name=values(full_name),"
            		+ " date_of_birth=values(date_of_birth),"
            		+ " mobile_phone=values(mobile_phone),"
            		+ " gender=values(gender),"
            		+ " status=values(status),"
            		+ " image_path=values(image_path)";
            ps = connection.prepareStatement(sqlInsertToPeople);
            ps.setString(1, people.getPeopleId());
            ps.setString(2, people.getFullName());
            ps.setString(3, people.getDateOfBirth());
            ps.setInt(4, people.getCustomerTypeId());
            ps.setInt(5, people.getGroupId());
			ps.setString(6, people.getImagePathNoHost());
            ps.setString(7, people.getMobilePhone());
            ps.setString(8, people.getGender());
            ps.executeUpdate();
            Utility.closeObject(ps);

            String sqlInsertToCapturedImages = "update CapturedImages set people_id = ? where id = ?";
            ps = connection.prepareStatement(sqlInsertToCapturedImages);
            ps.setString(1, capturedImages.getPeopleId());
            ps.setInt(2, capturedImages.getId());
            ps.executeUpdate();
            Utility.closeObject(ps);

            String sqlInsertDetection = " INSERT INTO detection "
					+ " (captured_image_path, camera_id, people_id, response_time, recognization_status, response_raw, created_time, captured_time, day_first_time, day_noon_time, liveness_status) "
					+ " VALUES(?, ?, ?, ?, ?, ?, current_timestamp(), ?, ?, ?, ?) ";
			ps = connection.prepareStatement(sqlInsertDetection);
			ps.setString(1, people.getImagePathNoHost());
			ps.setInt(2, capturedImages.getCameraId());
			ps.setString(3, people.getPeopleId());
			ps.setString(4, null);
			ps.setString(5, null);
			ps.setString(6, null);
			ps.setString(7, capturedImages.getCreatedTime());
			ps.setString(8, capturedImages.getCreatedTime());
			ps.setString(9, null);
			ps.setString(10, "TRUE");
			ps.executeUpdate();
			Utility.closeObject(ps);

			String sqlInsertPeopleRegImage = "insert into people_reg_image (people_id, image) values (?, ?)";
			ps = connection.prepareStatement(sqlInsertPeopleRegImage);
			ps.setString(1, people.getPeopleId());
			ps.setString(2, people.getImagePathNoHost());
			ps.executeUpdate();

            HttpUtils httpUtils = new HttpUtils();
            DataResponseFromRegisterFaceSearch dataResponse = httpUtils.callAPIToCreate(capturedImages.getCapturedImagePath(), capturedImages.getPeopleId());
            if (!StatusConstant.STATUS_SUCCESS.equals(dataResponse.getStatus())) {
            	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Kiểm tra khuôn mặt thất bại", ""));
            	throw new Exception("Failed to reg face: " + dataResponse.getStatus());
            }

            connection.commit();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Đăng ký nhân viên thành công " + people.getFullName(), ""));

            return "";
        } catch (Exception e) {
        	connection.rollback();
        	log.error(e.getMessage(), e);
            return "";
        } finally {
            Utility.closeObject(ps);
            Utility.closeObject(connection);
        }
    }

    public static String deletePeople(String id) {
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
            return "0";
        } finally {
            Utility.closeObject(ps);
            Utility.closeObject(connection);
        }
    }

    public static boolean reRegisterPeople(People people, CapturedImages ci, Connection connection) throws Exception {
        PreparedStatement ps = null;
        boolean result = false;
        String capturedTime = ci.getCreatedTime();
        int capturedImagesId = ci.getId();
        try {
            String sql = " update people p set full_name = ?, date_of_birth = ?, customer_type = ?, group_id = ?, image_path = ?, mobile_phone = ?, gender = ? "
            		+ " where people_id = ? ";
            ps = connection.prepareStatement(sql);
            ps.setString(1, people.getFullName());
            ps.setString(2, people.getDateOfBirth());
            ps.setInt(3, people.getCustomerTypeId());
            ps.setInt(4, people.getGroupId());
			ps.setString(5, people.getImagePathNoHost());
            ps.setString(6, people.getMobilePhone());
            ps.setString(7, people.getGender());
            ps.setString(8, people.getPeopleId());
            ps.executeUpdate();
            Utility.closeObject(ps);

            String sqlUpdateCapturedImages = "update CapturedImages set people_id = ? where id = ?";
            ps = connection.prepareStatement(sqlUpdateCapturedImages);
            ps.setString(1, people.getPeopleId());
            ps.setInt(2, capturedImagesId);
            ps.executeUpdate();
            Utility.closeObject(ps);

            String sqlInsertDetection = " INSERT INTO detection "
					+ " (captured_image_path, camera_id, people_id, response_time, recognization_status, response_raw, created_time, captured_time, day_first_time, day_noon_time, liveness_status) "
					+ " VALUES(?, ?, ?, ?, ?, ?, current_timestamp(), ?, ?, ?, ?) ";
			ps = connection.prepareStatement(sqlInsertDetection);
			ps.setString(1, people.getImagePathNoHost());
			ps.setInt(2, ci.getCameraId());
			ps.setString(3, people.getPeopleId());
			ps.setString(4, null);
			ps.setString(5, null);
			ps.setString(6, null);
			ps.setString(7, capturedTime);
			ps.setString(8, capturedTime);
			ps.setString(9, null);
			ps.setString(10, "TRUE");
			ps.executeUpdate();
			Utility.closeObject(ps);

			String sqlInsertPeopleRegImage = "insert into people_reg_image (people_id, image) values (?, ?)";
			ps = connection.prepareStatement(sqlInsertPeopleRegImage);
			ps.setString(1, people.getPeopleId());
			ps.setString(2, people.getImagePathNoHost());
			ps.executeUpdate();

            result = true;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            connection.rollback();
        } finally {
            Utility.closeObject(ps);
        }
        return result;
    }

    public static CapturedImages getCapturedImagesById(int id) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        CapturedImages images = null;
        try {
            String sql = " select "
	            		+ "	c.id, "
	            		+ "	p.group_id, "
	            		+ "	c.people_id, "
	            		+ "	p.full_name, "
	            		+ "	p.mobile_phone, "
	            		+ "	p.date_of_birth, "
	            		+ "	p.Gender, "
	            		+ "	p.customer_type, "
	            		+ " CONCAT('" + Constants.URL_IMAGE + "', c.path_image) AS url, "
	            		+ "	c.captured_time, "
	            		+ " p.status "
	            		+ "from "
	            		+ "	People p "
	            		+ "right join CapturedImages c on "
	            		+ "	p.people_id = c.people_id "
	            		+ "where "
	            		+ "	c.id = ? ";
            connection = ConnectDB.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                images = new CapturedImages();
                images.setGroupId(resultSet.getInt("group_id"));
                images.setPeopleId(resultSet.getString("people_id"));
                images.setFullName(resultSet.getString("full_name"));
                images.setMobilePhone(resultSet.getString("mobile_phone"));
                images.setDateOfBirth(resultSet.getString("date_of_birth"));
                images.setGender(resultSet.getString("Gender"));
                images.setCustomerTypeId(resultSet.getInt("customer_type"));
                images.setCapturedImagePath(resultSet.getString("url"));
                images.setCreatedTime(resultSet.getString("captured_time"));
                images.setStatus(resultSet.getInt("status"));
            }

            return images;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            return null;
        } finally {
            Utility.closeObject(resultSet);
            Utility.closeObject(ps);
            Utility.closeObject(connection);
        }
    }
}
