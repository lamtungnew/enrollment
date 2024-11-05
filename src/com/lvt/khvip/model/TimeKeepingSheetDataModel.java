package com.lvt.khvip.model;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.lvt.khvip.dao.PeopleDao;
import com.lvt.khvip.entity.People;
import com.lvt.khvip.entity.TimekeepingDate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.primefaces.model.Visibility;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lvt.khvip.client.TimeKeepingSheetClient;
import com.lvt.khvip.client.dto.ResponseListData;
import com.lvt.khvip.client.dto.TimekeepingSheetData;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.util.BackendUtils;

public class TimeKeepingSheetDataModel extends LazyDataModel<TimekeepingSheetData> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private boolean firstLoad = false;
    // private boolean search;
    private List<TimekeepingSheetData> datasource;
    private TimekeepingSheetData condition;
    private String sortColumn;
    private boolean ascSorted;
    private int firstRowIndex = 0;
    private int rowIndexAsc = 0;

    private String tableId;
    private List<Boolean> visibleColumns = new ArrayList<Boolean>();


    private TimeKeepingSheetClient timeKeepingSheetClient;

    public TimeKeepingSheetDataModel(List<TimekeepingSheetData> datasource) {
        this.datasource = datasource;
    }


    public TimeKeepingSheetDataModel(String tableId, TimekeepingSheetData condition, TimeKeepingSheetClient shiftClient) {
        this.tableId = tableId;
        this.condition = condition;
        this.timeKeepingSheetClient = shiftClient;
    }


    public void setCondition(TimekeepingSheetData condition, String sortedColumn, boolean ascSorted, boolean firstLoad) {
        this.condition = condition;
        this.sortColumn = sortedColumn;
        this.ascSorted = ascSorted;
        this.firstLoad = firstLoad;
    }


    @Override
    public TimekeepingSheetData getRowData(String rowKey) {
        if (datasource != null) {
            for (TimekeepingSheetData row : datasource) {
                if (String.valueOf(row.getAutoid()).equals(rowKey))
                    return row;
            }
        }
        return null;
    }

    @Override
    public String getRowKey(TimekeepingSheetData row) {
        return String.valueOf(row.getAutoid());
    }

    @Override
    public List<TimekeepingSheetData> load(int first, int pageSize, Map<String, SortMeta> sortField, Map<String, FilterMeta> filters) {
        List<TimekeepingSheetData> data = new ArrayList<>();

        try {

            if (firstLoad) {
                first = 0;
                firstLoad = false;
            }
            first = first / pageSize;
            firstRowIndex = first;
                       
            condition.setPageNo(first);
            condition.setLimit(pageSize);
            ResponseListData<TimekeepingSheetData> rs = timeKeepingSheetClient.getAllListTimeKeepingSheetCustom(condition);
            datasource = BackendUtils.stringToArray( new Gson().toJson(rs.getData()), TimekeepingSheetData[].class) ;

            StringBuilder peopleIds = new StringBuilder();
            for (TimekeepingSheetData row : datasource){
                if (!StringUtils.isEmpty(row.getApprovedBy())){
                    if (StringUtils.isEmpty(peopleIds.toString())) {
                        peopleIds.append("'"+row.getApprovedBy()+"'");
                    } else {
                        peopleIds.append(",'"+row.getApprovedBy()+"'");
                    }
                }

                if (!StringUtils.isEmpty(row.getApprovalByLevel2())){
                    if (StringUtils.isEmpty(peopleIds.toString())) {
                        peopleIds.append("'"+row.getApprovalByLevel2()+"'");
                    } else {
                        peopleIds.append(",'"+row.getApprovalByLevel2()+"'");
                    }
                }
            }

            Map<String,String> peopleNameByPeopleId = new HashMap();
            if (!StringUtils.isEmpty(peopleIds.toString())){
                List<People> listApprovePeople = PeopleDao.getPeopleByPeopleIds(peopleIds.toString());

                peopleNameByPeopleId = listApprovePeople.stream().collect(Collectors.toMap(people -> people.getPeopleId(), People::getFullName,
                        (existing, replacement) -> existing));
            }

            for (TimekeepingSheetData row : datasource) {            	
            	if (row.getState().equals("1")) {
            	    if (row.getApprovalLevel().equals(1)) {
                        row.setStateValue(Constants.TimeKeepingSheetConstants.APPROVED_STATUS);
                    } else {
                        row.setStateValue(Constants.TimeKeepingSheetConstants.NEXT_APPROVED_STATUS);
                    }
            	} else if (row.getState().equals("2")) {
            		row.setStateValue(Constants.TimeKeepingSheetConstants.APPROVED_STATUS);
            	} else if (row.getState().equals("3")) {
            		row.setStateValue(Constants.TimeKeepingSheetConstants.DENY_STATUS);
            	} else {
                    row.setStateValue(Constants.TimeKeepingSheetConstants.NEW_STATUS);
                }

                row.setApprovedByName(peopleNameByPeopleId.get(row.getApprovedBy()));
                row.setApprovalByLevel2Name(peopleNameByPeopleId.get(row.getApprovalByLevel2()));

                if (!StringUtils.isEmpty(row.getDateOfBirth())) {
            		String[] dateOfBirthItem = row.getDateOfBirth().split("-");
                	String newDateOfBirth = dateOfBirthItem[2]+"-"+dateOfBirthItem[1]+"-"+dateOfBirthItem[0];
                	row.setDateOfBirth(newDateOfBirth);
            	}

            	TimekeepingSheetData condition = new TimekeepingSheetData();
            	condition.setAutoid(row.getAutoid());
            	TimekeepingSheetData timekeepingSheetData = timeKeepingSheetClient.getTimeKeepingSheet(condition);

            	if (ObjectUtils.isEmpty(timekeepingSheetData)){
            	    continue;
                }

            	if (!ObjectUtils.isEmpty(timekeepingSheetData.getTimeKeepingDate())){
                    StringBuilder mistake = new StringBuilder();

                    for(TimekeepingDate timekeepingDate : timekeepingSheetData.getTimeKeepingDate()){
                        if (StringUtils.isEmpty(mistake.toString())){
                            mistake.append(timekeepingDate.getDate());
                        }else{
                            mistake.append(", " +timekeepingDate.getDate());
                        }
                    }

                    row.setMistake(mistake.toString());
                }


                boolean match = true;

                if (filters != null) {
                    for (Iterator<String> it = filters.keySet().iterator(); it.hasNext(); ) {
                        try {
                            String filterProperty = it.next();
                            Object filterValue = filters.get(filterProperty);
                            String fieldValue = String.valueOf(row.getClass().getField(filterProperty).get(row));

                            if (filterValue == null || fieldValue.startsWith(filterValue.toString())) {
                                match = true;
                            } else {
                                match = false;
                                break;
                            }
                        } catch (Exception e) {
                            match = false;
                        }
                    }
                }

                if (match) {
                    data.add(row);
                }
            }

            long count = rs.getTotalCount();
            this.setRowCount((int) count);
            return data;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return data;
    }
    
	public static <T> List<T> getObjectList(String jsonString, Class<T> cls) {
		List<T> list = new ArrayList<T>();
		try {
			Gson gson = new Gson();
			JsonArray arry = new JsonParser().parse(jsonString).getAsJsonArray();
			for (JsonElement jsonElement : arry) {
				list.add(gson.fromJson(jsonElement, cls));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

    public boolean isFirstLoad() {
        return firstLoad;
    }

    public void setFirstLoad(boolean firstLoad) {
        this.firstLoad = firstLoad;
    }

    public List<TimekeepingSheetData> getDatasource() {
        return datasource;
    }

    public void setDatasource(List<TimekeepingSheetData> datasource) {
        this.datasource = datasource;
    }

    public TimekeepingSheetData getCondition() {
        return condition;
    }

    public void setCondition(TimekeepingSheetData condition) {
        this.condition = condition;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public boolean isAscSorted() {
        return ascSorted;
    }

    public void setAscSorted(boolean ascSorted) {
        this.ascSorted = ascSorted;
    }

    public int getRowIndexAsc() {
        rowIndexAsc = (getRowIndex() + 1) + firstRowIndex;
        return rowIndexAsc;
    }

    public void setRowIndexAsc(int rowIndexAsc) {
        this.rowIndexAsc = rowIndexAsc;
    }

    public String getInvStatusColor(String invStatus) {
        switch (invStatus) {
            case "1":
                return "text-decoration: underline;";
            case "2":
                return "color:#898EBC; text-decoration: underline;";
            case "3":
                return "color:#0066FF; text-decoration: underline;";
            case "4":
                return "color:#CE5D43; text-decoration: underline;";
            case "5":
                return "color:red; text-decoration: underline;";
            default:
                break;
        }
        return null;
    }

    public void onToggle(ToggleEvent e) {
        visibleColumns.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

}

class LazySorter implements Comparator<TimekeepingSheetData> {

    private String sortField;

    private SortOrder sortOrder;

    public LazySorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    public int compare(TimekeepingSheetData obj1, TimekeepingSheetData obj2) {
        try {
            Object value1 = TimekeepingSheetData.class.getField(this.sortField).get(obj1);
            Object value2 = TimekeepingSheetData.class.getField(this.sortField).get(obj2);

            int value = ((Comparable) value1).compareTo(value2);

            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
