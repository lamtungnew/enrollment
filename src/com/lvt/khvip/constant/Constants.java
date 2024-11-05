package com.lvt.khvip.constant;

import com.lvt.khvip.util.ConfProperties;

public class Constants {
	public static final String IMAGE_PATH = "/home/dell/quyennb/KVCProduct/image_face/";
	public static final String URL_IMAGE = ConfProperties.getProperty("url_face_image");
	public static final String URL_FACE_CHECK_LIVELESS = "http://10.36.126.159:8080/check_liveness";
	public static final String SOURCE = "dangnt";
	public static final String EXCELL_TEMPLATE_PATH = ConfProperties.getProperty("excell_template_path");
	
	public static final class UploadExcelResponse {
		public static final String UPLOADED_FILE = "UPLOADED_FILE";
		public static final String EMPTY_FILE = "EMPTY_FILE";
		public static final String INVALID_HEADERS = "INVALID_HEADERS";
		public static final String INVALID_FILE = "INVALID_FILE";
		public static final String INVALID_FILE_TYPE = "INVALID_FILE_TYPE";

		//folder contains uploaded excel file in ftp server
		public static final String UPLOADED_EXCEL_FOLDER = "UploadedExcel";

		public static final double AVG_RECORD_PROCESSING_TIME = 0.05;
	}
	
	public static final class MastersConstants {
		public static final Integer CORE_POOL_SIZE = 4;
		public static final Integer MAX_POOL_SIZE = 20;
		public static final Integer QUEUE_CAPACITY = 500;
		public static final String THREAD_NAME_PREFIX = "masters-thread-pool";
	}
	
	public static final class ExcelTemplateCodes{
		public static final String TIMEKEEPINGSHEET_UPLOAD = "TIME_KEEPING_SHEET_UPLOAD";
		public static final String TIMEKEEPINGSHEET_EXPORT = "TIME_KEEPING_SHEET_EXPORT";
		public static final String SHIFT_MANAGER_EXPORT = "SHIFT_MANAGER_EXPORT";
		public static final String SHIFT_MANAGER_IMPORT = "SHIFT_MANAGER_IMPORT";
		public static final String EMPLOYEE_MANAGER_EXPORT = "EMPLOYEE_MANAGER_EXPORT";
		public static final String OT_SHEET_UPLOAD = "OT_SHEET_UPLOAD";
		public static final String OT_SHEET_EXPORT = "OT_SHEET_EXPORT";

		public static final Integer ERROR_COLUMN_WIDTH = 9000;
    }
	
	public static final class TimeKeepingSheetConstants{
		public static final String APPROVED_STATUS_FOR_COMPARE = "đã phê duyệt";
		public static final String NEW_STATUS_FOR_COMPARE = "chưa phê duyệt";
		public static final String DENY_STATUS_FOR_COMPARE = "Từ chối";
		public static final String NEXT_APPROVED_STATUS_FOR_COMPARE = "duyệt cấp 1";

		public static final String APPROVED_STATUS = "Đã phê duyệt";
		public static final String NEXT_APPROVED_STATUS = "Duyệt cấp 1";
		public static final String NEW_STATUS = "Chưa phê duyệt";
		public static final String DENY_STATUS = "Từ chối";
		
		public static final String STATE = "Trạng thái";
		public static final String PEOPLE_ID = "Mã nhân viên";
		public static final String FULL_NAME = "Họ và tên";
		public static final String VALID_STATE = "Đã phê duyệt; Chưa phê duyệt, Đã từ chối";

    }

    public static final class RoleConstants{
		public static final String DEP_LEADER = "DEP_LEADER";
		public static final String GROUP_LEADER = "GROUP_LEADER";
		public static final String ADMIN = "ADMIN";
		public static final String USER = "USER";
		public static final String QLNSP = "QLNSP";
	}

	
	public static final class BackendApis {
		public static final String TIMEKEEPINGSHEET_IMPORT_EXCEL = "/time-keeping-sheet/import-excel";
		public static final String TIMEKEEPINGSHEET_GET_ALL = "/time-keeping-sheets";
		public static final String OTSHEET_GET_ALL = "/ot-sheets";
		public static final String TIMEKEEPINGSHEET = "/time-keeping-sheet";
		public static final String OTSHEET = "/ot-sheet";
		public static final String GROUP = "/group";
		public static final String APPROVE = "/time-keeping-sheet/approve";
		public static final String APPROVE_OT = "/ot-sheet/approve";
		public static final String OTSHEET_IMPORT_EXCEL = "/ot-sheet/import-excel";
		public static final String APPROVAL_PEOPLE = "/approval-peoples";
		public static final String TIMEKEEPINGSHEET_CUSTOM = "/time-keeping-sheet-custom";
		public static final String OTSHEET_CUSTOM = "/ot-sheet-custom";
	}

	public static final class ApprovalFLow{
		public static final String APPROVAL = "APPROVAL";
		public static final String NEXT_APPROVAL = "NEXT_APPROVAL";
		public static final String level1 = "Duyệt cấp 1";
		public static final String level2 = "Duyệt cấp 2";
		public static final String DENY = "DENY";

		public static final String KEEPING_APPROVAL = "KEEPING_APPROVAL";
		public static final String OT_APPROVAL = "OT_APPROVAL";
	}

}
