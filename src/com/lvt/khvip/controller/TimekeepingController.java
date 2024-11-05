package com.lvt.khvip.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.dao.GroupsDao;
import com.lvt.khvip.dao.PeopleDao;
import com.lvt.khvip.dao.TimeKeepingDao;
import com.lvt.khvip.entity.Groups;
import com.lvt.khvip.entity.People;
import com.lvt.khvip.entity.Timekeeping;
import com.lvt.khvip.util.StringUtils;
import com.lvt.khvip.util.Utils;

@SessionScoped
@ManagedBean
public class TimekeepingController {
	private static final Logger log = LoggerFactory.getLogger(TimekeepingController.class);

    private Timekeeping timekeeping;
    private Date fromDate;
    private Date toDate;
    private String peopleIdSearch;
    private StreamedContent content;
    
    private int groupSelected;
	private List<Groups> groups;
	private People peopleSelected;
	private List<People> peopleList;
	private List<Timekeeping> timekeepingList;
	private boolean flagLate;
	private boolean flagNoSignUp;
    
    public TimekeepingController() {
    	GroupsDao groupsDao = new GroupsDao();
    	groups = groupsDao.getGroupList();
    	timekeepingList = TimeKeepingDao.getListTimekeeping();
    }

    public List<Timekeeping> getListTimekeepingOfPeople() {
    	if (flagLate && flagNoSignUp) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Vui lòng chỉ chọn một loại", ""));
			return timekeepingList;
		}
    	
		String strFromDate = null;
		String strToDate = null;
		try {
			strFromDate = Utils.convertDateToString(fromDate, "yyyy-MM-dd");
			strToDate = Utils.convertDateToString(toDate, "yyyy-MM-dd");
			if (StringUtils.isEmpty(strFromDate) && StringUtils.isEmpty(strToDate)) {
				strFromDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-01"));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		timekeepingList = TimeKeepingDao.getListTimekeepingOfPeople(peopleIdSearch, groupSelected, strFromDate, strToDate, flagLate);
		
		// xử lí những ngày không chấm công
		if (flagNoSignUp) {
			
			// tạo list từ ngày đến ngày bỏ t7 và chủ nhật
			LocalDate dtFrom;
			LocalDate dtTo;
			if (fromDate == null) {
				dtFrom = YearMonth.now().atDay(1);
			} else {
				dtFrom = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}
			if (toDate == null) {
				dtTo = LocalDate.now();
			} else {
				dtTo = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}
	    	List<LocalDate> lsDate = new ArrayList<>();
			while (dtTo.isAfter(dtFrom) || dtTo.isEqual(dtFrom)) {
				if (!(dtTo.getDayOfWeek() == DayOfWeek.SATURDAY || dtTo.getDayOfWeek() == DayOfWeek.SUNDAY)) {
					lsDate.add(dtTo);
				}
				dtTo = dtTo.minusDays(1);
			}
	    	
			// add list danh sách chấm công vào map với key là peopleId + ngày châm công
	    	Map<String, Timekeeping> timekeepingMap = new TreeMap<>();
	    	for (Timekeeping item : timekeepingList) {
	    		timekeepingMap.put(item.getPeopleId() + "_" + item.getDateTimeKeeping(), item);
	    	}
	    	
	    	// lấy tất cả nhân viên
	    	List<People> allPeopleId = TimeKeepingDao.getPeopeIdList(peopleIdSearch);
	    	
	    	// lấy những ngày không chấm công của từng mã nv
	    	timekeepingList = new ArrayList<>();
	    	for (People people : allPeopleId) {
	    		for (LocalDate cd : lsDate) {
	    			String key = people.getPeopleId() + "_" + cd.toString();
	    			if (!timekeepingMap.containsKey(key)) {
	    				Timekeeping noSignUp = new Timekeeping();
	    				noSignUp.setPeopleId(people.getPeopleId());
	    				noSignUp.setDateTimeKeeping(cd.toString());
	    				noSignUp.setDecription("Không chấm công");
	    				noSignUp.setImagePath(people.getImagePath());
	    				noSignUp.setFullName(people.getFullName());
	    				noSignUp.setGroupName(people.getGroupName());
	    				timekeepingList.add(noSignUp);
	    			}
	    		}
	    	}
		}
		
		return timekeepingList;
	}

    public Timekeeping getTimekeeping() {
        if (this.timekeeping == null) {
            this.timekeeping = new Timekeeping();
        }
        return timekeeping;
    }

	public void exportExcell() {
		File file = new File("formulas_output.xlsx");
		log.info(file.getAbsolutePath());
		try (InputStream is = new FileInputStream(Constants.EXCELL_TEMPLATE_PATH);
				OutputStream os = new FileOutputStream(file)) {
			Context context = new Context();
			context.putVar("list", timekeepingList);
			JxlsHelper.getInstance().processTemplate(is, os, context);
			InputStream downloadFileInputStream = new FileInputStream(file);
			DefaultStreamedContent content = DefaultStreamedContent.builder()
					.name("time_checking.xlsx")
					.stream(() -> downloadFileInputStream).build();
			this.content = content;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
    
    public void getPeopleListByPeopleId() {
		if (StringUtils.isEmpty(peopleIdSearch)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Vui lòng nhập mã nhân viên", ""));
			return;
		}
		peopleList = PeopleDao.searchPeopleList(peopleIdSearch, null, null);	
		showDialog("peoplePicker");
	}
    
	public void showDialog(String id) {
		PrimeFaces.current().executeScript("PF('" + id + "').show();");
	}
    
    public void onRowSelect() {
    	peopleIdSearch = peopleSelected.getPeopleId();
    }

    public void setTimekeeping(Timekeeping timekeeping) {
        this.timekeeping = timekeeping;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getPeopleIdSearch() {
        return peopleIdSearch;
    }

    public void setPeopleIdSearch(String peopleIdSearch) {
        this.peopleIdSearch = peopleIdSearch;
    }

    public StreamedContent getContent() {
        return content;
    }

    public void setContent(StreamedContent content) {
        this.content = content;
    }

	public int getGroupSelected() {
		return groupSelected;
	}

	public void setGroupSelected(int groupSelected) {
		this.groupSelected = groupSelected;
	}

	public List<Groups> getGroups() {
		return groups;
	}

	public void setGroups(List<Groups> groups) {
		this.groups = groups;
	}

	public People getPeopleSelected() {
		return peopleSelected;
	}

	public void setPeopleSelected(People peopleSelected) {
		this.peopleSelected = peopleSelected;
	}

	public List<People> getPeopleList() {
		return peopleList;
	}

	public void setPeopleList(List<People> peopleList) {
		this.peopleList = peopleList;
	}

	public List<Timekeeping> getTimekeepingList() {
		return timekeepingList;
	}

	public void setTimekeepingList(List<Timekeeping> timekeepingList) {
		this.timekeepingList = timekeepingList;
	}

	public boolean isFlagLate() {
		return flagLate;
	}

	public void setFlagLate(boolean flagLate) {
		this.flagLate = flagLate;
	}

	public boolean isFlagNoSignUp() {
		return flagNoSignUp;
	}

	public void setFlagNoSignUp(boolean flagNoSignUp) {
		this.flagNoSignUp = flagNoSignUp;
	}
}
