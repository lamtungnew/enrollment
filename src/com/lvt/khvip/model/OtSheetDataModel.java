package com.lvt.khvip.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lvt.khvip.client.OtSheetClient;
import com.lvt.khvip.client.dto.ResponseListData;
import com.lvt.khvip.client.dto.OtSheetData;
import com.lvt.khvip.client.dto.TimekeepingSheetData;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.dao.PeopleDao;
import com.lvt.khvip.entity.OtSheetDate;
import com.lvt.khvip.entity.People;
import com.lvt.khvip.entity.TimekeepingDate;
import com.lvt.khvip.util.BackendUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class OtSheetDataModel extends LazyDataModel<OtSheetData> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private boolean firstLoad = false;
    // private boolean search;
    private List<OtSheetData> datasource;
    private OtSheetData condition;
    private String sortColumn;
    private boolean ascSorted;
    private int firstRowIndex = 0;
    private int rowIndexAsc = 0;

    private String tableId;
    private List<Boolean> visibleColumns = new ArrayList<Boolean>();


    private OtSheetClient OtSheetClient;

    public OtSheetDataModel(List<OtSheetData> datasource) {
        this.datasource = datasource;
    }


    public OtSheetDataModel(String tableId, OtSheetData condition, OtSheetClient otSheetClient) {
        this.tableId = tableId;
        this.condition = condition;
        this.OtSheetClient = otSheetClient;
    }


    public void setCondition(OtSheetData condition, String sortedColumn, boolean ascSorted, boolean firstLoad) {
        this.condition = condition;
        this.sortColumn = sortedColumn;
        this.ascSorted = ascSorted;
        this.firstLoad = firstLoad;
    }


    @Override
    public OtSheetData getRowData(String rowKey) {
        if (datasource != null) {
            for (OtSheetData row : datasource) {
                if (String.valueOf(row.getAutoid()).equals(rowKey))
                    return row;
            }
        }
        return null;
    }

    @Override
    public String getRowKey(OtSheetData row) {
        return String.valueOf(row.getAutoid());
    }

    @Override
    public List<OtSheetData> load(int first, int pageSize, Map<String, SortMeta> sortField, Map<String, FilterMeta> filters) {
        List<OtSheetData> data = new ArrayList<>();

        try {

            if (firstLoad) {
                first = 0;
                firstLoad = false;
            }
            first = first / pageSize;
            firstRowIndex = first;

            condition.setPageNo(first);
            condition.setLimit(pageSize);
            ResponseListData<OtSheetData> rs = OtSheetClient.getAllListOtSheetCustom(condition);
            datasource = BackendUtils.stringToArray( new Gson().toJson(rs.getData()), OtSheetData[].class);

            StringBuilder peopleIds = new StringBuilder();
            for (OtSheetData row : datasource){
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

            for (OtSheetData row : datasource) {            	
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

            	for (OtSheetDate otSheetDate: row.getOtSheetDates()){
            	    if (StringUtils.isEmpty(row.getOtSheetDateValues())){
                        row.setOtSheetDateValues(otSheetDate.getOtDate());
                    } else {
                        row.setOtSheetDateValues(row.getOtSheetDateValues() + ", " +otSheetDate.getOtDate());
                    }
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

    public List<OtSheetData> getDatasource() {
        return datasource;
    }

    public void setDatasource(List<OtSheetData> datasource) {
        this.datasource = datasource;
    }

    public OtSheetData getCondition() {
        return condition;
    }

    public void setCondition(OtSheetData condition) {
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
