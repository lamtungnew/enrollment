package com.lvt.khvip.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.primefaces.event.ToggleEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.Visibility;

import com.lvt.khvip.client.EmployeeClient;
import com.lvt.khvip.client.dto.Employee;
import com.lvt.khvip.client.dto.ResponseListData;
import com.lvt.khvip.util.StringUtils;

public class EmployeeDataModel extends LazyDataModel<Employee> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private boolean firstLoad = false;
    // private boolean search;
    private List<Employee> datasource;
    private Employee condition;
    private String sortColumn;
    private boolean ascSorted;
    private int firstRowIndex = 0;
    private int rowIndexAsc = 0;

    private String tableId;
    private List<Boolean> visibleColumns = new ArrayList<Boolean>();


    private EmployeeClient employeeClient;

    public EmployeeDataModel(List<Employee> datasource) {
        this.datasource = datasource;
    }


    public EmployeeDataModel(String tableId, Employee condition, EmployeeClient shiftClient) {
        this.tableId = tableId;
        this.condition = condition;
        this.employeeClient = shiftClient;
//
//		setVisibleColumns();
    }


    public void setCondition(Employee condition, String sortedColumn, boolean ascSorted, boolean firstLoad) {
        this.condition = condition;
        this.sortColumn = sortedColumn;
        this.ascSorted = ascSorted;
        this.firstLoad = firstLoad;
    }


    @Override
    public Employee getRowData(String rowKey) {
        if (datasource != null) {
            for (Employee row : datasource) {
                if (String.valueOf(row.getId()).equals(rowKey))
                    return row;
            }
        }
        return null;

    }

    @Override
    public String getRowKey(Employee row) {
        return String.valueOf(row.getId());
    }

    @Override
    public List<Employee> load(int first, int pageSize, Map<String, SortMeta> sortField, Map<String, FilterMeta> filters) {
        List<Employee> data = new ArrayList<>();

        try {


            first = first / pageSize;
            first = first + 1;

            ResponseListData<Employee> rs = employeeClient.listPeople(condition.getPeopleId(), condition.getFullName(), condition.getMobilePhone(), StringUtils.isEmpty(condition.getImagePath()) ? null : condition.getImagePath(), first, pageSize);
            datasource = rs.getData();
            // filter
            for (Employee row : datasource) {
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

            // sort
//            if (sortField != null) {
//                Collections.sort(data, new LazySorter(sortField, sortOrder));
//            }

            long count = rs.getTotalCount();
            this.setRowCount((int) count);
//			reSetVisibleColumns();
            return data;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return data;
    }

    public boolean isFirstLoad() {
        return firstLoad;
    }

    public void setFirstLoad(boolean firstLoad) {
        this.firstLoad = firstLoad;
    }

    public List<Employee> getDatasource() {
        return datasource;
    }

    public void setDatasource(List<Employee> datasource) {
        this.datasource = datasource;
    }

    public Employee getCondition() {
        return condition;
    }

    public void setCondition(Employee condition) {
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

