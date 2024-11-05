package com.lvt.khvip.model;

import com.lvt.khvip.client.ShiftClient;
import com.lvt.khvip.client.dto.ResponseListData;
import com.lvt.khvip.client.dto.ShiftListData;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.*;

import java.io.Serializable;
import java.util.*;

public class ShiftDataModel extends LazyDataModel<ShiftListData> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private boolean firstLoad = false;
    // private boolean search;
    private List<ShiftListData> datasource;
    private ShiftListData condition;
    private String sortColumn;
    private boolean ascSorted;
    private int firstRowIndex = 0;
    private int rowIndexAsc = 0;

    private String tableId;
    private List<Boolean> visibleColumns = new ArrayList<Boolean>();


    private ShiftClient shiftClient;

    public ShiftDataModel(List<ShiftListData> datasource) {
        this.datasource = datasource;
    }


    public ShiftDataModel(String tableId, ShiftListData condition, ShiftClient shiftClient) {
        this.tableId = tableId;
        this.condition = condition;
        this.shiftClient = shiftClient;
//
//		setVisibleColumns();
    }


    public void setCondition(ShiftListData condition, String sortedColumn, boolean ascSorted, boolean firstLoad) {
        this.condition = condition;
        this.sortColumn = sortedColumn;
        this.ascSorted = ascSorted;
        this.firstLoad = firstLoad;
    }


    @Override
    public ShiftListData getRowData(String rowKey) {
        if (datasource != null) {
            for (ShiftListData row : datasource) {
                if (String.valueOf(row.getAutoid()).equals(rowKey))
                    return row;
            }
        }
        return null;

    }

    @Override
    public String getRowKey(ShiftListData row) {
        return String.valueOf(row.getAutoid());
    }

    @Override
    public List<ShiftListData> load(int first, int pageSize, Map<String, SortMeta> sortField, Map<String, FilterMeta> filters) {
        List<ShiftListData> data = new ArrayList<>();

        try {


            first = first / pageSize;
            first = first + 1;

            ResponseListData<ShiftListData> rs = shiftClient.listAllShift(condition.getDepId(), condition.getPeopleId(), condition.getFullName(), condition.getGroupId(), first, pageSize);
            datasource = rs.getData();
            // filter
            for (ShiftListData row : datasource) {
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

    public List<ShiftListData> getDatasource() {
        return datasource;
    }

    public void setDatasource(List<ShiftListData> datasource) {
        this.datasource = datasource;
    }

    public ShiftListData getCondition() {
        return condition;
    }

    public void setCondition(ShiftListData condition) {
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

class ShiftDataLazySorter implements Comparator<ShiftListData> {

    private String sortField;

    private SortOrder sortOrder;

    public ShiftDataLazySorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    public int compare(ShiftListData obj1, ShiftListData obj2) {
        try {
            Object value1 = ShiftListData.class.getField(this.sortField).get(obj1);
            Object value2 = ShiftListData.class.getField(this.sortField).get(obj2);

            int value = ((Comparable) value1).compareTo(value2);

            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
