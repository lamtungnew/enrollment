/**
 *
 */
package com.lvt.khvip.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import com.lvt.khvip.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.constant.StatusConstant;
import com.lvt.khvip.entity.CapturedImages;
import com.lvt.khvip.entity.CustomerType;
import com.lvt.khvip.entity.People;
import com.lvt.khvip.response.DataResponseFromRegisterFaceSearch;
import com.lvt.khvip.response.DataResponseFromRemoveFacesSearch;
import com.lvt.khvip.util.HttpUtils;
import com.lvt.khvip.util.StringUtils;
import com.lvt.khvip.util.Utility;
import com.lvt.khvip.util.Utils;

/**
 * @author: tuha.lvt
 *
 * @date:
 */

@ManagedBean
@SessionScoped
public class PeopleDao implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(PeopleDao.class);

    public static List<People> getListPeople() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        People people = null;
        List<People> peopleList = new ArrayList<>();
        try {
            String sql = " SELECT  "
            		+ "    p.id, "
            		+ "    people_id, "
            		+ "    CONCAT('" + Constants.URL_IMAGE + "',image_path) AS url, "
            		+ "    full_name, "
            		+ "    ct.name, "
            		+ "    Gender, "
            		+ "    date_of_birth, "
            		+ "    Last_checkin_time, "
            		+ "    g.group_name, "
            		+ "    p.mobile_phone, "
            		+ "    p.group_id, "
            		+ "    p.customer_type, "
            		+ "    p.status "
            		+ "FROM "
            		+ "    People p "
            		+ "        INNER JOIN "
            		+ "    CustomerType ct ON ct.id = p.customer_type "
            		+ "        LEFT JOIN "
            		+ "    groups g ON p.group_id = g.group_id "
            		+ "    where p.status <> 0 "
            		+ "    order by p.status "
            		+ "    limit 1000 ";
            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                people = new People();
                people.setId(resultSet.getInt("id"));
                people.setPeopleId(resultSet.getString("people_id"));
                people.setImagePath(resultSet.getString("url"));
                people.setFullName(resultSet.getString("full_name"));
                people.setCustomerType(resultSet.getString("name"));
                people.setGender(resultSet.getString("Gender"));
                people.setDateOfBirth(resultSet.getString("date_of_birth"));
                people.setLastCheckinTime(resultSet.getString("Last_checkin_time"));
                people.setGroupName(resultSet.getString("group_name"));
                people.setMobilePhone(resultSet.getString("mobile_phone"));
                people.setGroupId(resultSet.getInt("group_id"));
                people.setCustomerTypeId(resultSet.getInt("customer_type"));
                people.setStatus(resultSet.getInt("status"));
                peopleList.add(people);
            }
            String json = new Gson().toJson(peopleList);
            log.info("Total Records Fetched: " + json);
            
            return peopleList;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            Utility.closeObject(resultSet);
            Utility.closeObject(statement);
            Utility.closeObject(connection);
        }
    }

    public static String savePeople(People people, CapturedImages capturedImages) {

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            String result = "";
            connection = ConnectDB.getConnection();
            String sqlInsertToPeople = "insert into People (people_id, full_name, date_of_birth, Gender, image_path ,customer_type) values (?, ?, ?, ?, ?, ?)";
            ps = connection.prepareStatement(sqlInsertToPeople);
            ps.setString(1, people.getPeopleId());
            ps.setString(2, people.getFullName());
            ps.setString(3, people.getDateOfBirth());
            ps.setString(4, people.getGender());
            ps.setString(5, people.getImagePath());
            ps.setString(6, people.getCustomerType());
            ps.executeUpdate();
            Utility.closeObject(ps);

            String sqlInsertToCapturedImages = "update CapturedImages set people_id = ? where people_id = ?";
            ps = connection.prepareStatement(sqlInsertToCapturedImages);
            ps.setString(1, capturedImages.getPeopleId());
            ps.setInt(2, capturedImages.getId());
            ps.executeUpdate();

            result = "listPeople?faces-redirect=true";
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            Utility.closeObject(ps);
            Utility.closeObject(connection);
        }
    }

    public static void updatePeople(People people, boolean registerFace) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectDB.getConnection();
            connection.setAutoCommit(false);
            
            String sql = " update people p set full_name = ?, date_of_birth = ?, customer_type = ?, group_id = ?, image_path = ?, mobile_phone = ?, gender = ?, status = 1 "
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
            
            if (registerFace) {
				HttpUtils httpUtils = new HttpUtils();
				// gọi api xoá khuôn mặt
				DataResponseFromRemoveFacesSearch dataRemoveResponse = httpUtils.callAPIToRemove(people.getPeopleId());
				if (StatusConstant.STATUS_SUCCESS.equals(dataRemoveResponse.getStatus())
						|| StatusConstant.STATUS_PEOPLE_NOT_FOUND_ERROR.equals(dataRemoveResponse.getStatus())
						|| StatusConstant.STATUS_PEOPLE_DO_NOT_EXITS_ERROR.equals(dataRemoveResponse.getStatus())) {
					// gọi api đăng ký khuôn mặt
					DataResponseFromRegisterFaceSearch dataResponse = httpUtils.callAPIToCreate(people.getImagePath(),
							people.getPeopleId());
					if (StatusConstant.STATUS_SUCCESS.equals(dataResponse.getStatus())) {
						connection.commit();
						log.info("[PeopleId={}] Cập nhật SUCCESS", people.getPeopleId());
//						FacesContext.getCurrentInstance().addMessage(null,
//								new FacesMessage(FacesMessage.SEVERITY_INFO, "Cập nhật thành công nhân viên ", ""));
					} else {
						FacesContext.getCurrentInstance().addMessage(null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cập nhật nhân viên thất bại", "Lý do: " + dataResponse.getStatus()));
						throw new Exception("Failed to reg face: " + dataResponse.getStatus());
					}
				} else {
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cập nhật nhân viên thất bại", "Lý do: " + dataRemoveResponse.getStatus()));
					throw new Exception("Failed to remove face: " + dataRemoveResponse.getStatus());
				} 
			} else {
				connection.commit();
			}
            
        } catch (Exception e) {
        	connection.rollback();
            log.error(e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cập nhật nhân viên thất bại", ""));
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
    
    public static boolean isExistPeople(String peopleId) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT "
            		+ "    1 "
            		+ "FROM "
            		+ "    People "
            		+ "WHERE "
            		+ "    People_id = ? "
            		+ "and status = 1 ";
            connection = ConnectDB.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, peopleId);
            rs = ps.executeQuery();
            while (rs.next()) {
            	return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	Utility.closeObject(rs);
            Utility.closeObject(ps);
            Utility.closeObject(connection);
        }
        return false;
    }
    
    public static boolean updateExistPeople(String peopleId) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            String sql = " UPDATE People "
            		+ "SET "
            		+ "    status = 1 "
            		+ "WHERE "
            		+ "    People_id = ? AND status = 2 ";
            connection = ConnectDB.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, peopleId);
            if (ps.executeUpdate() > 0) {
            	return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utility.closeObject(ps);
            Utility.closeObject(connection);
        }
        return false;
    }
    
    public static boolean registerPeople(People people, Connection connection) throws Exception {
        PreparedStatement ps = null;
        boolean result = false;
        try {
            String sql = "insert into People (people_id, full_name, date_of_birth, customer_type, group_id , image_path, mobile_phone, gender) values (?, ?, ?, ?, ?, ?, ?, ?)"
			            + " on duplicate key update "
			    		+ " full_name=values(full_name),"
			    		+ " date_of_birth=values(date_of_birth),"
			    		+ " mobile_phone=values(mobile_phone),"
			    		+ " gender=values(gender),"
			    		+ " status=values(status),"
			    		+ " image_path=values(image_path)";
            ps = connection.prepareStatement(sql);
            ps.setString(1, people.getPeopleId());
            ps.setString(2, people.getFullName());
            ps.setString(3, people.getDateOfBirth());
            ps.setInt(4, people.getCustomerTypeId());
            ps.setInt(5, people.getGroupId());
            ps.setString(6, people.getImagePath());
            ps.setString(7, people.getMobilePhone());
            ps.setString(8, people.getGender());
            ps.executeUpdate();
            
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            connection.rollback();
        } finally {
            Utility.closeObject(ps);
        }
        return result;
    }
    
    public static boolean registerPeopleRegImage(String peopleId, List<List<String>> imageList, Connection connection) throws Exception {
        PreparedStatement ps = null;
        boolean result = false;
		String imagePath;
        try {
            String sql = "insert into people_reg_image (people_id, image) values (?, ?)";
			ps = connection.prepareStatement(sql);
			for (List<String> list : imageList) {
				for (String image : list) {
					imagePath = Utils.convertByte64ToFile(image, Constants.IMAGE_PATH);
					ps.setString(1, peopleId);
					ps.setString(2, imagePath);
					ps.addBatch();
				}
			}
			ps.executeBatch();

            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            connection.rollback();
        } finally {
            Utility.closeObject(ps);
        }
        return result;
    }
    
    public static boolean registerPeopleRegImage(String peopleId, String imagePath, Connection connection) throws Exception {
        PreparedStatement ps = null;
        boolean result = false;
        try {
            String sql = "insert into people_reg_image (people_id, image) values (?, ?)";
            ps = connection.prepareStatement(sql);
			ps.setString(1, peopleId);
            ps.setString(2, imagePath);
            ps.executeUpdate();
            
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            connection.rollback();
        } finally {
            Utility.closeObject(ps);
        }
        return result;
    }
    
	public static boolean registerPeopleWhitelist(String peopleId, int userId, Connection connection) throws Exception {
		PreparedStatement ps = null;
		boolean result = false;
		try {
			String sql = "insert into people_whitelist (user_id, people_id) values (?, ?)";
			ps = connection.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.setString(2, peopleId);
			ps.executeUpdate();

			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			connection.rollback();
		} finally {
			Utility.closeObject(ps);
		}
		return result;
	}
	
	public static List<People> getPeopleList() {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        List<People> peopleList = new ArrayList<>();
        People people = null;
        try {
            String sql = " select people_id , full_name, date_of_birth , Gender , customer_type , group_id ,mobile_phone from people p where status <> 0 ";
            conn = ConnectDB.getConnection();
            statement = conn.createStatement();
            rs = statement.executeQuery(sql);
            while (rs.next()) {
            	people = new People();
            	people.setPeopleId(rs.getString("people_id"));
            	people.setFullName(rs.getString("full_name"));
            	people.setDateOfBirth(rs.getString("date_of_birth"));
            	people.setGender(rs.getString("Gender"));
            	people.setCustomerTypeId(rs.getInt("customer_type"));
            	people.setGroupId(rs.getInt("group_id"));
            	people.setMobilePhone(rs.getString("mobile_phone"));
            	peopleList.add(people);
            }
        } catch (Exception sqlException) {
            sqlException.printStackTrace();
            return null;
        } finally {
            Utility.closeObject(rs);
            Utility.closeObject(statement);
            Utility.closeObject(conn);
        }
        return peopleList;
    }
	
	public static List<People> searchPeopleList(String peopleId, String fullName ,String mobilePhone) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<People> peopleList = new ArrayList<>();
        People people = null;
        try {
        	String sql = " SELECT  "
            		+ "    p.id, "
            		+ "    people_id, "
            		+ "    CONCAT('" + Constants.URL_IMAGE + "',image_path) AS url, "
            		+ "    full_name, "
            		+ "    ct.name, "
            		+ "    Gender, "
            		+ "    date_of_birth, "
            		+ "    Last_checkin_time, "
            		+ "    g.group_name, "
            		+ "    p.mobile_phone, "
            		+ "    p.group_id, "
            		+ "    p.customer_type, "
            		+ "    p.status "
            		+ "FROM "
            		+ "    People p "
            		+ "        INNER JOIN "
            		+ "    CustomerType ct ON ct.id = p.customer_type "
            		+ "        LEFT JOIN "
            		+ "    groups g ON p.group_id = g.group_id "
            		+ "    where p.status <> 0 ";
            if (!StringUtils.isEmpty(peopleId)) {
            	sql += " and people_id like '%' || ? || '%' ";
            	
      
            }
            if (!StringUtils.isEmpty(fullName)) {
            	sql += " and full_name like '%' || ? || '%' ";
            	
      
            }
            if (!StringUtils.isEmpty(mobilePhone)) {
            	sql += " and mobile_phone like '%' || ? || '%' ";
            }
            sql += "    order by p.status ";
            
            conn = ConnectDB.getConnection();
            ps = conn.prepareStatement(sql);
            
            int i = 1;
            if (!StringUtils.isEmpty(peopleId)) {
            	ps.setString(i++, peopleId);
            }
            if (!StringUtils.isEmpty(fullName)) {
            	ps.setString(i++, fullName);
            }
            if (!StringUtils.isEmpty(mobilePhone)) {
            	ps.setString(i++, mobilePhone);
            }
            
            rs = ps.executeQuery();
            while (rs.next()) {
            	people = new People();
            	 people = new People();
                 people.setId(rs.getInt("id"));
                 people.setPeopleId(rs.getString("people_id"));
                 people.setImagePath(rs.getString("url"));
                 people.setFullName(rs.getString("full_name"));
                 people.setCustomerType(rs.getString("name"));
                 people.setGender(rs.getString("Gender"));
                 people.setDateOfBirth(rs.getString("date_of_birth"));
                 people.setLastCheckinTime(rs.getString("Last_checkin_time"));
                 people.setGroupName(rs.getString("group_name"));
                 people.setMobilePhone(rs.getString("mobile_phone"));
                 people.setGroupId(rs.getInt("group_id"));
                 people.setCustomerTypeId(rs.getInt("customer_type"));
                 people.setStatus(rs.getInt("status"));
            	peopleList.add(people);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            Utility.closeObject(rs);
            Utility.closeObject(ps);
            Utility.closeObject(conn);
        }
        return peopleList;
    }
	
	public static List<String> getLast5Detection(String peopleId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> imageList = new ArrayList<>();
        try {
            String sql = " select captured_image_path from detection d where people_id = ? "
            		+ " and liveness_status <> 'FALSE' and liveness_status <> 'false' "
            		+ " order by created_time desc limit 5 ";
            conn = ConnectDB.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, peopleId);
            rs = ps.executeQuery();
            while (rs.next()) {
                imageList.add(Constants.URL_IMAGE + rs.getString("captured_image_path"));
            }
            
            return imageList;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            Utility.closeObject(rs);
            Utility.closeObject(ps);
            Utility.closeObject(conn);
        }
    }
	
	public static int countPeopleByCondition (People condition) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		List<CustomerType> customerTypeList = new ArrayList<CustomerType>();
		CustomerType customerType = null;
		int rs = 0;
		try {
			connection = ConnectDB.getConnection();
			StringBuilder sql = new StringBuilder(" select count(*) as total from People  where 1=1 ");
			
			if (condition!=null) {
				if (!StringUtils.isEmpty(condition.getPeopleId())) {
					sql.append(" and people_id = '"+condition.getPeopleId().toString()+"' ");
				}								
			}
			
			ps = connection.prepareStatement(sql.toString());
			resultSet = ps.executeQuery();
			while (resultSet.next()) {
				rs = resultSet.getInt("total");
			}
		} catch (SQLException ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			Utility.closeObject(resultSet);
			Utility.closeObject(ps);
			Utility.closeObject(connection);
		}
		return rs;
	}
	
	public static List<String> getListPeopleIdByName (String fullName) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		List<CustomerType> customerTypeList = new ArrayList<CustomerType>();
		CustomerType customerType = null;
		List<String> rs = new ArrayList();
		try {
			connection = ConnectDB.getConnection();
			StringBuilder sql = new StringBuilder(" select people_id from People where lower(full_name) ='"+fullName+"'");					
			
			ps = connection.prepareStatement(sql.toString());
			resultSet = ps.executeQuery();
			while (resultSet.next()) {
				rs.add(resultSet.getString("people_id"));				 
			}
		} catch (SQLException ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			Utility.closeObject(resultSet);
			Utility.closeObject(ps);
			Utility.closeObject(connection);
		}
		return rs;
	}

	public static User getApprovalNameByGroupId(Integer groupId, String role){
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String approvalName = null;
        User rs = null;
        try {
            connection = ConnectDB.getConnection();
            StringBuilder sql = new StringBuilder("");

            if (role.equals(Constants.RoleConstants.DEP_LEADER) || role.equals(Constants.RoleConstants.GROUP_LEADER)){
                sql.append("select p.people_id, u.username from groups g inner join people p on g.manager = p.people_id " +
                        "inner join users u on u.people_id = p.people_id " +
                        "where g.group_id="+groupId);
            } else if (role.equals(Constants.RoleConstants.ADMIN)){
                sql.append("select u.people_id, p.full_name from users u inner join user_role ur on u.id = ur.user_id " +
                        "inner join roles r on r.id = ur.role_id and r.name = 'ADMIN'");
            }

            if (StringUtils.isEmpty(sql.toString())){
                return null;
            }

            ps = connection.prepareStatement(sql.toString());
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                rs = new User();
                rs.setPeopleId(resultSet.getString("people_id"));
                rs.setUsername(resultSet.getString("username"));
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            Utility.closeObject(resultSet);
            Utility.closeObject(ps);
            Utility.closeObject(connection);
        }
        return rs;
    }

    public static List<People> getPeopleByPeopleIds(String peopleIds) {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        List<People> peopleList = new ArrayList<>();
        People people = null;
        try {
            String sql = " select people_id , full_name, date_of_birth , Gender , customer_type , group_id ,mobile_phone from people p where status <> 0 and people_id in ("+peopleIds+")";
            conn = ConnectDB.getConnection();
            statement = conn.createStatement();
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                people = new People();
                people.setPeopleId(rs.getString("people_id"));
                people.setFullName(rs.getString("full_name"));
                people.setDateOfBirth(rs.getString("date_of_birth"));
                people.setGender(rs.getString("Gender"));
                people.setCustomerTypeId(rs.getInt("customer_type"));
                people.setGroupId(rs.getInt("group_id"));
                people.setMobilePhone(rs.getString("mobile_phone"));
                peopleList.add(people);
            }
        } catch (Exception sqlException) {
            sqlException.printStackTrace();
            return null;
        } finally {
            Utility.closeObject(rs);
            Utility.closeObject(statement);
            Utility.closeObject(conn);
        }
        return peopleList;
    }

}
