package com.lvt.khvip.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.dao.ExcelTemplateDao;
import com.lvt.khvip.entity.ExcelResponseMessage;
import com.lvt.khvip.entity.ExcelTemplate;
import com.lvt.khvip.entity.ExcelTemplateDet;

public class ExcelUtils implements Serializable {

//    @Autowired
//    private AasClient aasClient;
//    @Autowired
//    private EmailClient emailClient;
//    @Autowired
//    private MastersApplicationPropertiesConfig propertiesConfig;
//    @Autowired
//    private MessagesUtilities messagesUtilities;
//    @Autowired
//    private FtpJSchService ftpJSchService;
//    @Autowired
//    private ExcelClient excelClient;

	private final Logger log = LoggerFactory.getLogger(ExcelUtils.class);
	private final String EXCEL2003 = "xls";
	private final String EXCEL2007 = "xlsx";
	private int MILLISECOND = 1000;
	private int SECOND = 60;
	private final int MAX_ROW_ACCESS_WINDOW = 100;
	private final String EXCEL_ROW_POSITION = "excelRowPosition";
	private final String ERROR_EXCEL_FILE_NAME_INFIX = "_error_";
	private final String XLS_EXTENSION = ".xls";
	private final String XLSX_EXTENSION = ".xlsx";
	private final String ACTIVE_CELL_VALUE = "active";
	private final String INACTIVE_CELL_VALUE = "inactive";
	private Map<String, HorizontalAlignment> mapAlignment = new HashMap<>();

	public ExcelUtils() {
		mapAlignment.put("LEFT", HorizontalAlignment.LEFT);
		mapAlignment.put("RIGHT", HorizontalAlignment.RIGHT);
		mapAlignment.put("CENTER", HorizontalAlignment.CENTER);
	}

	private <T> void handleField(T t, String value, Field field) throws Exception {
		if (field == null) {
			return;
		}

		Class<?> type = field.getType();
		if (type == null || type == void.class || StringUtils.isBlank(value)) {
			return;
		}
		if (type == Object.class) {
			field.set(t, value);
		} else if ((type.getSuperclass() == null || type.getSuperclass() == Number.class)
				&& NumberUtils.isCreatable(value)) {
			if (type == int.class || type == Integer.class) {
				field.set(t, NumberUtils.toInt(value));
			} else if (type == long.class || type == Long.class) {
				field.set(t, NumberUtils.toLong(value));
			} else if (type == byte.class || type == Byte.class) {
				field.set(t, NumberUtils.toByte(value));
			} else if (type == short.class || type == Short.class) {
				field.set(t, NumberUtils.toShort(value));
			} else if (type == double.class || type == Double.class) {
				field.set(t, NumberUtils.toDouble(value));
			} else if (type == float.class || type == Float.class) {
				field.set(t, NumberUtils.toFloat(value));
			} else if (type == char.class || type == Character.class) {
				field.set(t, CharUtils.toChar(value));
			} else if (type == boolean.class) {
				field.set(t, BooleanUtils.toBoolean(value));
			} else if (type == BigDecimal.class) {
				field.set(t, new BigDecimal(value));
			}
		} else if (type == Boolean.class) {
			field.set(t, BooleanUtils.toBoolean(value));
		} else if (type == Date.class) {
			field.set(t, stringToDate(value));
		} else if (type == String.class) {
			field.set(t, value);
		} else {
			log.error("- cell type " + type + ", value " + value + ", field " + field.getName() + ", class "
					+ field.getClass().getName());
		}
	}

	private Date stringToDate(String strDate) {
		SimpleDateFormat format;
		try {
			format = new SimpleDateFormat("dd/MM/yyyy");
			return format.parse(strDate);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		try {
			format = new SimpleDateFormat("mm/dd/yyyy");
			return format.parse(strDate);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private String getCellValue(Cell cell) {
		if (cell == null) {
			return "";
		}
		if (cell.getCellType() == CellType.NUMERIC) {
			if (DateUtil.isCellDateFormatted(cell)) {
				return new SimpleDateFormat("mm/dd/yyyy").format(DateUtil.getJavaDate(cell.getNumericCellValue()));
			} else {
				return new BigDecimal(cell.getNumericCellValue()).toString();
			}
		} else if (cell.getCellType() == CellType.STRING) {
			return StringUtils.trimToEmpty(cell.getStringCellValue());
		} else if (cell.getCellType() == CellType.FORMULA) {
			return StringUtils.trimToEmpty(cell.getCellFormula());
		} else if (cell.getCellType() == CellType.BLANK) {
			return "";
		} else if (cell.getCellType() == CellType.BOOLEAN) {
			return String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == CellType.ERROR) {
			return "ERROR";
		} else {
			return cell.toString().trim();
		}
	}

	public <T> byte[] exportExcel(List<T> dataList, Class<T> cls, ExcelTemplate exportTemplate) {
		try {
			Map<String, ExcelTemplateDet> mapFieldAndColumn = new HashMap<>();

			for (ExcelTemplateDet detailColumn : exportTemplate.getListTempDetail()) {
				mapFieldAndColumn.put(detailColumn.getFieldName(), detailColumn);
			}

			List<Field> fieldList = new ArrayList<>();
			getFieldsOfAClass(cls, fieldList);
			fieldList.forEach(field -> {
				field.setAccessible(true);
			});

			SXSSFWorkbook wb = new SXSSFWorkbook(MAX_ROW_ACCESS_WINDOW);
			int rowNumber = exportTemplate.getStartRow() != null ? exportTemplate.getStartRow() : 0;
			Sheet sheet = wb.createSheet(exportTemplate.getTitle());
			Row row = sheet.createRow(rowNumber);

			// define cell style
			CellStyle columnNameStyle = getColumnNameStyle(wb);
			CellStyle rowNumStyle = getRowNumStyle(wb);
			Map<String, CellStyle> cellWithAlignmentStyles = getCellWithAlignmentStyles(wb);

			for (ExcelTemplateDet detailColumn : exportTemplate.getListTempDetail()) {
				Cell columnNameCell = row.createCell(detailColumn.getPosition());
				columnNameCell.setCellStyle(columnNameStyle);
				columnNameCell.setCellValue(detailColumn.getColumnName());
				sheet.setColumnWidth(detailColumn.getPosition(), detailColumn.getWidth());
			}

			if (CollectionUtils.isNotEmpty(dataList)) {

				for (T rowData : dataList) {
					rowNumber++;
					Row rowForWriting = sheet.createRow(rowNumber);

					// create row num field
					ExcelTemplateDet rowNumColumnDetail = mapFieldAndColumn.get("#");
					if (rowNumColumnDetail != null) {
						Cell cell = rowForWriting.createCell(rowNumColumnDetail.getPosition());
						cell.setCellStyle(rowNumStyle);
						cell.setCellValue(rowNumber);
					}

					fieldList.forEach(field -> {
						Class<?> type = field.getType();
						Object value = StringUtils.EMPTY;
						try {
							value = field.get(rowData);
						} catch (Exception e) {
							log.error(e.toString());
						}

						// get detail conlumn configuration data by field name
						ExcelTemplateDet detailColumnConfigData = mapFieldAndColumn.get(field.getName());

						// set position for a cell
						if (detailColumnConfigData != null) {
							Cell cell = rowForWriting.createCell(detailColumnConfigData.getPosition());
							CellStyle cellStyle = cellWithAlignmentStyles.get(detailColumnConfigData.getAlignment());
							cell.setCellStyle(cellStyle);

							if (value != null) {
								if (type == Date.class) {
									SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
									cell.setCellValue(simpleDateFormat.format(value));
								} else if (field.getName().equals("activeInd")) {
									cell.setCellValue(
											value.toString().equals("true") ? ACTIVE_CELL_VALUE : INACTIVE_CELL_VALUE);
								} else {
									cell.setCellValue(value.toString().replaceAll("\\|", ","));
								}
							}
						}
					});
				}
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			wb.write(bos);
			wb.close();

			byte[] attachedFileByteArr = bos.toByteArray();
			bos.close();
			return attachedFileByteArr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private CellStyle getColumnNameStyle(Workbook wb) {
		CellStyle columnNameStyle = wb.createCellStyle();
		columnNameStyle.setAlignment(HorizontalAlignment.CENTER);
		columnNameStyle.setBorderBottom(BorderStyle.THIN);
		columnNameStyle.setBorderTop(BorderStyle.THIN);
		columnNameStyle.setBorderRight(BorderStyle.THIN);
		columnNameStyle.setBorderLeft(BorderStyle.THIN);
		Font font = wb.createFont();
		font.setBold(true);
		columnNameStyle.setFont(font);
		return columnNameStyle;
	}

	private CellStyle getRowNumStyle(Workbook wb) {
		CellStyle rowNumStyle = wb.createCellStyle();
		rowNumStyle.setAlignment(HorizontalAlignment.CENTER);
		rowNumStyle.setBorderBottom(BorderStyle.THIN);
		rowNumStyle.setBorderTop(BorderStyle.THIN);
		rowNumStyle.setBorderRight(BorderStyle.THIN);
		rowNumStyle.setBorderLeft(BorderStyle.THIN);
		return rowNumStyle;
	}

	private Map<String, CellStyle> getCellWithAlignmentStyles(Workbook wb) {
		Map<String, CellStyle> result = new HashMap<>();
		mapAlignment.keySet().forEach(alignKey -> {
			CellStyle cellStyle = wb.createCellStyle();
			HorizontalAlignment horizontalAlignment = mapAlignment.get(alignKey);
			if (horizontalAlignment != null) {
				cellStyle.setAlignment(horizontalAlignment);
			}
			cellStyle.setBorderBottom(BorderStyle.THIN);
			cellStyle.setBorderTop(BorderStyle.THIN);
			cellStyle.setBorderRight(BorderStyle.THIN);
			cellStyle.setBorderLeft(BorderStyle.THIN);
			result.put(alignKey, cellStyle);
		});
		return result;
	}

	private void buildExcelDocument(String fileName, SXSSFWorkbook wb, HttpServletResponse response) {
		try {
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
			response.flushBuffer();
			wb.write(response.getOutputStream());
			wb.dispose();
		} catch (IOException e) {
			log.error(e.toString());
		}
	}

	public void getFieldsOfAClass(Class cls, List<Field> fieldList) {
		if (cls.getDeclaredFields().length > 0) {
			Field[] fields = cls.getDeclaredFields();
			fieldList.addAll(Arrays.asList(fields));
		}

		if (cls.getSuperclass() != null && !cls.getSuperclass().equals(Object.class)) {
			getFieldsOfAClass(cls.getSuperclass(), fieldList);
		}
	}

	public <T> List<T> parseExcelToDto(Class<T> cls, Sheet sheet, String fileName, ExcelTemplate uploadExcelTemplate,
			Map<Integer, StringBuilder> mapExcelErrors, Set<String> errorSummarySet)

	{
		List<T> dataList = new ArrayList<>();
		Workbook workbook = null;
		try {
			if (sheet == null) {
				errorSummarySet.add(MessageProvider.getValue("upload.excel.email.read.file.fail", null));
			}

			Map<String, ExcelTemplateDet> columnDetailByColumnName = new HashMap<>();
			Map<Integer, ExcelTemplateDet> columnDetailByColumnPosition = new HashMap<>();
			Map<Integer, Field> fieldByColumnPosition = new HashMap<>();
			Map<String, Field> fieldByFieldName = new HashMap<>();
			StringBuilder fieldHeaderError = new StringBuilder();

			for (ExcelTemplateDet detailColumn : uploadExcelTemplate.getListTempDetail()) {
				if (!StringUtils.isEmpty(detailColumn.getColumnName())) {
					columnDetailByColumnName.put(detailColumn.getColumnName().trim().toLowerCase(), detailColumn);
				}
			}

			List<Field> fieldList = new ArrayList<>();
			getFieldsOfAClass(cls, fieldList);

			for (Field field : fieldList) {
				field.setAccessible(true);
				fieldByFieldName.put(field.getName(), field);
			}

			Integer validatedDataIndex = 0;

			// init field by column position
			Row headerRow = sheet.getRow(sheet.getFirstRowNum());
			Iterator<Cell> cellIterator = headerRow.cellIterator();
			while (cellIterator.hasNext()) {
				Cell headerCell = cellIterator.next();
				String columnName = getCellValue(headerCell);
				initFieldByColumnPosition(fieldByColumnPosition, fieldByFieldName, columnDetailByColumnName, columnName,
						headerCell, columnDetailByColumnPosition);
			}

			// handling columnName
			handlingColumnHeader(headerRow, errorSummarySet, fieldByColumnPosition, fieldByFieldName,
					columnDetailByColumnName, columnDetailByColumnPosition, uploadExcelTemplate.getListTempDetail(),
					fieldHeaderError, mapExcelErrors, new ExcelResponseMessage());
			int totalCell = sheet.getRow(0).getLastCellNum();
			for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);

				if (checkIfRowIsEmpty(row)) {
					continue;
				}

				T validatedItem = cls.getDeclaredConstructor().newInstance();

				for (Field field : fieldList) {
					if (field.getName().equalsIgnoreCase(EXCEL_ROW_POSITION)) {
						field.setInt(validatedItem, rowIndex + 1);
					}
				}

				for (int cellIndex = 0; cellIndex < totalCell; cellIndex++) {
					Cell cell = row.getCell(cellIndex);
					if (cell == null) {
						cell = row.createCell(cellIndex);
					}

					ExcelTemplateDet detailColumn = columnDetailByColumnPosition.get(cell.getColumnIndex());
					if (detailColumn != null) {
						Field field = fieldByColumnPosition.get(cell.getColumnIndex());
						String cellValue = getCellValueAndCatchErrors(cell, mapExcelErrors, detailColumn,
								validatedDataIndex, errorSummarySet).replaceAll(",", "\\|");
						handleField(validatedItem, cellValue, field);
					}
				}

				dataList.add(validatedItem);
				validatedDataIndex++;

			}

			// append fieldHeaderError to mapExcelErrors: if there is no error before,
			// create error
			for (int itemIndex = 0; itemIndex < dataList.size(); itemIndex++) {
				if (mapExcelErrors.containsKey(itemIndex)) {
					StringBuilder cellErrors = mapExcelErrors.get(itemIndex);
					buildCellErrors(cellErrors, fieldHeaderError.toString());
				} else {
					StringBuilder cellErrors = new StringBuilder();
					if (!StringUtils.isEmpty(fieldHeaderError)) {
						buildCellErrors(cellErrors, fieldHeaderError.toString());
						mapExcelErrors.put(itemIndex, cellErrors);
					}
				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			errorSummarySet.add(MessageProvider.getValue("upload.excel.email.read.file.fail", null));
		}

		finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (Exception e) {
					log.error(String.format("parse excel exception!"), e);
					errorSummarySet.add(MessageProvider.getValue("upload.excel.email.read.file.fail", null));
				}
			}
		}
		return dataList;
	}

	public ExcelResponseMessage handlingColumnHeader(Row row, Set<String> errorSummarySet,
			Map<Integer, Field> fieldByColumnPosition, Map<String, Field> fieldByFieldName,
			Map<String, ExcelTemplateDet> columnDetailByColumnName,
			Map<Integer, ExcelTemplateDet> columnDetailByColumnPosition, List<ExcelTemplateDet> columnTemplateDetail,
			StringBuilder fieldHeaderError, Map<Integer, StringBuilder> mapExcelErrors,
			ExcelResponseMessage excelResponseMessage) {
		List<String> listColumnName = new ArrayList<>();

		if (checkIfRowIsEmpty(row)) {
			errorSummarySet.add(MessageProvider.getValue("upload.excel.email.missing.header", null));
			StringBuilder missingHeaderError = new StringBuilder();
			columnTemplateDetail.stream().forEach(detailColumn -> {
				if (missingHeaderError.length() == 0) {
					missingHeaderError.append(MessageProvider.getValue("upload.excel.error.missing.header",
							new String[] { detailColumn.getColumnName() }));
				} else {
					missingHeaderError.append("\n- " + MessageProvider.getValue("upload.excel.error.missing.header",
							new String[] { detailColumn.getColumnName() }));
				}
			});

			mapExcelErrors.put(0, missingHeaderError);

			excelResponseMessage.setMessage(MessageProvider.getValue("upload.excel.response.invalid.headers", null));
			excelResponseMessage.setCode(Constants.UploadExcelResponse.INVALID_HEADERS);
			excelResponseMessage.setType(Constants.UploadExcelResponse.INVALID_FILE);
		}

		// get all cells of the selected row
		Iterator<Cell> cellIterator = row.cellIterator();
		while (cellIterator.hasNext()) {
			Cell headerCell = cellIterator.next();
			String columnName = getCellValue(headerCell);

			initFieldByColumnPosition(fieldByColumnPosition, fieldByFieldName, columnDetailByColumnName, columnName,
					headerCell, columnDetailByColumnPosition);

			if (!StringUtils.isEmpty(columnName) && !listColumnName.contains(columnName.trim().toLowerCase())) {
				listColumnName.add(columnName.trim().toLowerCase());
			} else if (!StringUtils.isEmpty(columnName) && listColumnName.contains(columnName.trim().toLowerCase())) {
				buildEmailErrorList(errorSummarySet,
						MessageProvider.getValue("upload.excel.email.duplicate.header", null));
				if (fieldHeaderError.length() == 0) {
					fieldHeaderError.append(MessageProvider.getValue("upload.excel.error.duplicate.header",
							new String[] { columnName }));
				} else {
					fieldHeaderError.append("\n- " + MessageProvider.getValue("upload.excel.error.duplicate.header",
							new String[] { columnName }));
				}
			}
		}

		for (ExcelTemplateDet detailColumn : columnTemplateDetail) {
			if (!listColumnName.contains(detailColumn.getColumnName().toLowerCase())) {
				buildEmailErrorList(errorSummarySet,
						MessageProvider.getValue("upload.excel.email.missing.header", null));
				if (fieldHeaderError.length() == 0) {
					fieldHeaderError.append(MessageProvider.getValue("upload.excel.error.missing.header",
							new String[] { detailColumn.getColumnName() }));
				} else {
					fieldHeaderError.append("\n- " + MessageProvider.getValue("upload.excel.error.missing.header",
							new String[] { detailColumn.getColumnName() }));
				}
			}
		}

		if (fieldHeaderError.length() > 0) {
			excelResponseMessage.setMessage(MessageProvider.getValue("upload.excel.response.invalid.headers", null));
			excelResponseMessage.setCode(Constants.UploadExcelResponse.INVALID_HEADERS);
			excelResponseMessage.setType(Constants.UploadExcelResponse.INVALID_FILE);
		}
		return excelResponseMessage;
	}

	public void initFieldByColumnPosition(Map<Integer, Field> mapPositionAndField, Map<String, Field> mapFieldsOfAClass,
			Map<String, ExcelTemplateDet> mapDetailColumn, String columnName, Cell headerCell,
			Map<Integer, ExcelTemplateDet> mapPositionAndColumnDetail) {
		ExcelTemplateDet columnDetail = mapDetailColumn.get(columnName.replaceAll("\n", "").trim().toLowerCase());
		if (columnDetail != null) {
			mapPositionAndColumnDetail.put(headerCell.getColumnIndex(), columnDetail);
			Field field = mapFieldsOfAClass.get(columnDetail.getFieldName());

			if (field != null) {
				mapPositionAndField.put(headerCell.getColumnIndex(), field);
			}
		}
	}

	private String getCellValueAndCatchErrors(Cell cell, Map<Integer, StringBuilder> mapExcelError,
			ExcelTemplateDet detailColumn, Integer validatedDataIndex, Set<String> errorSummarySet) {
		String cellValue = StringUtils.EMPTY;
		StringBuilder cellErrors = mapExcelError.get(validatedDataIndex);

		if (cellErrors == null) {
			cellErrors = new StringBuilder();
		}

		String columnName = detailColumn.getColumnName();
		CellType cellType = cell.getCellType();
		
		if (cellType == CellType.FORMULA) {
			cellType = cell.getCachedFormulaResultType();
		}

		// check mandatory field
		if ((cellType == CellType.BLANK || (cellType == CellType.STRING && cell.getStringCellValue().equals("")))
				&& detailColumn.getMandatoryInd() != null && detailColumn.getMandatoryInd().equals(1)) {
			buildCellErrors(cellErrors,
					MessageProvider.getValue("upload.excel.error.data.mandatory", new String[] { columnName }));
			buildEmailErrorList(errorSummarySet, MessageProvider.getValue("upload.excel.email.mandatory.header", null));
		}

		// check data type
		if (!StringUtils.isEmpty(detailColumn.getCellType())
				&& !cellType.toString().equals(detailColumn.getCellType())) {
			buildCellErrors(cellErrors, MessageProvider.getValue("upload.excel.error.data.type.incorrect",
					new String[] { columnName, detailColumn.getCellType() }));
			buildEmailErrorList(errorSummarySet,
					MessageProvider.getValue("upload.excel.email.incorrect.data.format", null));
		}

		// get cell's value
		switch (cellType) {
		case _NONE:
			cellValue = StringUtils.EMPTY;
			break;
		case NUMERIC:
			cellValue = StringUtils.EMPTY + cell.getNumericCellValue();
			break;
		case STRING:
			cellValue = cell.getStringCellValue();
			break;
		case FORMULA:
			cellValue = cell.getCellFormula();
			break;
		case BLANK:
			cellValue = StringUtils.EMPTY;
			break;
		case BOOLEAN:
			cellValue = StringUtils.EMPTY + cell.getBooleanCellValue();
			break;
		}

		// check status(active--inactive)
		if (!StringUtils.isEmpty(detailColumn.getFieldName()) && (detailColumn.getFieldName().equals("activeInd")
				|| detailColumn.getFieldName().equals("isActive"))) {
			if (cellValue.trim().equalsIgnoreCase(ACTIVE_CELL_VALUE)) {
				cellValue = String.valueOf(Boolean.TRUE);
			} else if (cellValue.trim().equalsIgnoreCase(INACTIVE_CELL_VALUE)) {
				cellValue = String.valueOf(Boolean.FALSE);
			} else {
				buildCellErrors(cellErrors, MessageProvider.getValue("upload.excel.error.data.invalid.option",
						new String[] { "Status", "Active/Inactive" }));
				buildEmailErrorList(errorSummarySet,
						MessageProvider.getValue("upload.excel.email.invalid.entries", null));
			}
		}

		// check data formats
		if (!StringUtils.isEmpty(detailColumn.getPattern()) && !cellValue.matches(detailColumn.getPattern())) {
			buildCellErrors(cellErrors,
					MessageProvider.getValue("upload.excel.error.invalid.data.format", new String[] { columnName }));
			buildEmailErrorList(errorSummarySet,
					MessageProvider.getValue("upload.excel.email.incorrect.data.format", null));
		}

		// check max length
		if (detailColumn.getMaxLength() != null && (cellValue.length() > detailColumn.getMaxLength())) {
			buildCellErrors(cellErrors, MessageProvider.getValue("upload.excel.error.above.max.length",
					new String[] { columnName, detailColumn.getMaxLength().toString() }));
			buildEmailErrorList(errorSummarySet,
					MessageProvider.getValue("upload.excel.email.invalid.max.length", null));
		}

		if (!StringUtils.isEmpty(cellErrors.toString())) {
			mapExcelError.put(validatedDataIndex, cellErrors);
		}

		return cellValue;
	}

	public byte[] createAttachedFile(Workbook workbook, Sheet sheet, String fileName,
			Map<Integer, StringBuilder> mapExcelErrors, ExcelTemplate exportTemplate) {
		Integer startRow = exportTemplate.getStartRow();

		List<ExcelTemplateDet> listTempDetail = exportTemplate.getListTempDetail();
		Map<String, ExcelTemplateDet> detailByColumnName = listTempDetail.stream()
				.collect(Collectors.toMap(ExcelTemplateDet::getColumnFullName, Function.identity()));

		try {
			if (workbook != null) {
				Map<Integer, String> headerInputValueMap = getRowHeaderDataInputExcelFile(sheet, exportTemplate);
				Map<Integer, String> redundantFieldMap = new HashMap<>();
				List<String> listHeaderTemplate = new ArrayList<>();

				exportTemplate.getListTempDetail().forEach(uploadTemplateDetIdDto -> {
					listHeaderTemplate.add(uploadTemplateDetIdDto.getColumnName());
				});
				headerInputValueMap.forEach((key, val) -> {
					if (!listHeaderTemplate.contains(val)) {
						redundantFieldMap.put(key, val);
					}
				});

				int lastCell = 0;
				CellStyle cellStyle = getAttachCellStyle(workbook);

				// build map detailByUniqueColumnPosition
				Map<Integer, ExcelTemplateDet> detailByUniqueColumnPosition = new HashMap();

				Row columnHeaderRow = sheet.getRow(sheet.getFirstRowNum());
				Iterator<Cell> cellIterator = columnHeaderRow.cellIterator();
				while (cellIterator.hasNext()) {
					Cell headerCell = cellIterator.next();
					String columnName = getCellValue(headerCell);

					ExcelTemplateDet templateDetail = detailByColumnName.get(columnName);

					if (templateDetail != null && templateDetail.getNoneDuplicated() != null
							&& templateDetail.getNoneDuplicated().equals(1)) {
						detailByUniqueColumnPosition.put(headerCell.getColumnIndex(), templateDetail);
					}
				}

				for (Integer rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
					Row row = sheet.getRow(rowIndex);
					if (checkIfRowIsEmpty(row))
						continue;

					// put unique columns to the 1st column
					detailByUniqueColumnPosition.keySet().forEach(uniqueColumnPosition -> {
						Integer toCellPosition = detailByUniqueColumnPosition.get(uniqueColumnPosition).getPosition();
						changeColumnPosition(row, uniqueColumnPosition, toCellPosition, cellStyle);
					});

					int lastCellNum = row.getLastCellNum();
					if (lastCell < lastCellNum) {
						lastCell = lastCellNum;
						// set column errors' width
						sheet.setColumnWidth(lastCell, Constants.ExcelTemplateCodes.ERROR_COLUMN_WIDTH);
					}
				}

				Boolean emptySheet = checkEmptySheet(sheet);
				int rowDataIndex = 0;
				if (emptySheet) {
					buildErrorForEmptyFile(sheet, workbook, mapExcelErrors, startRow);
				} else if (mapExcelErrors.size() > 0) {
					int firstDataRow = sheet.getFirstRowNum();
					Font font = workbook.createFont();
					font.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
					CellStyle errorHeaderStyle = getAttachErrorHeaderStyle(workbook, font);
					CellStyle errorStyle = getAttachErrorStyle(workbook, font);
					for (int rowIndex = firstDataRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
						Row row = sheet.getRow(rowIndex);
						if (checkIfRowIsEmpty(row)) {
							continue;
						}

						if (row.getRowNum() == firstDataRow) {
							Cell errorColumnName = row.createCell(lastCell);
							errorColumnName.setCellStyle(errorHeaderStyle);
							errorColumnName.setCellValue("Errors");

						} else {
							// writing errors at the last cell of every row;
							StringBuilder errors = mapExcelErrors.get(rowDataIndex);
							if (errors != null) {
								Cell cellToPutErrors = row.createCell(lastCell);
								cellToPutErrors.setCellStyle(errorStyle);
								cellToPutErrors.setCellValue(errors.toString().replace(";", "\n"));
							}
							rowDataIndex++;
						}
					}
				}
				// approximate processing time
				// String processingTime = calculateProcessingTime(timeStartReadingFile);
				// putProcessingTime(mapExcelErrors, sheet, workbook, lastCell, processingTime);

				// sortMapByValue
				Map<Integer, String> result = redundantFieldMap.entrySet().stream()
						.sorted((c1, c2) -> c2.getKey().compareTo(c1.getKey()))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
								(oldValue, newValue) -> oldValue, LinkedHashMap::new));
				// remove unnecessary columns of the inputted file
				if (!emptySheet) {
					result.forEach((position, redundantField) -> {
						deleteColumn(sheet, position);
					});
				}
			}

//            String tempFileName = "excelError" + "_" + new Date().getTime();
//            File templeFile = File.createTempFile(tempFileName, ".xlsx");
//            FileOutputStream outputStream = new FileOutputStream(templeFile);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			// workbook.write(outputStream);
			workbook.close();

			byte[] attachedFileByteArr = bos.toByteArray();

			// bos.close();
			return attachedFileByteArr;
		} catch (Exception e) {
			log.error(e.toString());
		}
		return null;
	}

	public String calculateProcessingTime(Long timeStartReadingFile) {
		Long timeEndReadingFile = new Date().getTime();
		Long processingTime = timeEndReadingFile - timeStartReadingFile;
		int second = processingTime < 1000 ? 1 : ((int) (processingTime / MILLISECOND)) % SECOND;
		int minute = (int) (processingTime / (MILLISECOND * SECOND));

		return MessageProvider.getValue("upload.excel.processing.time",
				new String[] { String.valueOf(minute), String.valueOf(second) });
	}

	public void putProcessingTime(Map<Integer, StringBuilder> mapExcelErrors, Sheet sheet, Workbook workbook,
			int lastCell, String processingTime) {
		Integer firstDataRow = sheet.getFirstRowNum();
		Row headerRow = sheet.getRow(firstDataRow);

		CellStyle errorHeaderStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
		errorHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
		errorHeaderStyle.setFont(font);
		errorHeaderStyle.setWrapText(true);

		Integer processingCellPosition = mapExcelErrors.size() == 0 ? lastCell : lastCell + 1;
		Cell processingTimeCell = headerRow.createCell(processingCellPosition);
		processingTimeCell.setCellStyle(errorHeaderStyle);
		processingTimeCell.setCellValue(processingTime);
		sheet.setColumnWidth(processingCellPosition, Constants.ExcelTemplateCodes.ERROR_COLUMN_WIDTH);
	}

	private CellStyle getAttachCellStyle(Workbook workbook) {
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		return cellStyle;
	}

	private CellStyle getAttachErrorHeaderStyle(Workbook workbook, Font font) {
		CellStyle errorHeaderStyle = workbook.createCellStyle();
		errorHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
		errorHeaderStyle.setFont(font);
		errorHeaderStyle.setWrapText(true);
		return errorHeaderStyle;
	}

	private CellStyle getAttachErrorStyle(Workbook workbook, Font font) {
		CellStyle errorStyle = workbook.createCellStyle();
		errorStyle.setVerticalAlignment(VerticalAlignment.TOP);
		errorStyle.setAlignment(HorizontalAlignment.LEFT);
		errorStyle.setFont(font);
		errorStyle.setWrapText(true);
		return errorStyle;
	}

	public void buildErrorForEmptyFile(Sheet sheet, Workbook workbook, Map<Integer, StringBuilder> mapExcelErrors,
			Integer startRow) {
		Row errorHeader = sheet.createRow(0);

		CellStyle errorHeaderStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
		errorHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
		errorHeaderStyle.setFont(font);
		errorHeaderStyle.setWrapText(true);

		CellStyle errorStyle = workbook.createCellStyle();
		errorStyle.setVerticalAlignment(VerticalAlignment.TOP);
		errorStyle.setAlignment(HorizontalAlignment.LEFT);
		errorStyle.setFont(font);
		errorStyle.setWrapText(true);

		Cell errorColumnName = errorHeader.createCell(0);
		errorColumnName.setCellStyle(errorHeaderStyle);
		errorColumnName.setCellValue("Errors");

		// writing errors at the last cell of every row
		List<StringBuilder> listError = new ArrayList<>(mapExcelErrors.values());
		StringBuilder errors = listError.size() > 0 ? listError.get(0) : null;
		if (errors != null) {
			Row errorRow = sheet.createRow(1);
			Cell cellToPutErrors = errorRow.createCell(0);
			cellToPutErrors.setCellStyle(errorStyle);
			cellToPutErrors.setCellValue(errors.toString());
		}
	}

	public boolean checkEmptySheet(Sheet sheet) {
		for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (!checkIfRowIsEmpty(row)) {
				return false;
			}
		}

		return true;
	}

	public Map<Integer, String> getRowHeaderDataInputExcelFile(Sheet sheet, ExcelTemplate exportTemplate) {
		Map<Integer, String> headerMap = new HashMap<>();
		try {
			if (sheet == null) {
				return headerMap;
			}

			Row row = sheet.getRow(sheet.getFirstRowNum());

			int maxLengthOfCell = 0;
			for (Integer rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row1 = sheet.getRow(rowIndex);
				if (row1 == null)
					continue;
				int lastRow = row1.getLastCellNum();
				if (maxLengthOfCell < lastRow) {
					maxLengthOfCell = lastRow;
				}
			}

			for (int columnIndex = 0; columnIndex < maxLengthOfCell; columnIndex++) {
				Cell headerCell = row.getCell(columnIndex);
				String columnName = getCellValue(headerCell);
				headerMap.put(columnIndex, columnName);
			}
		} catch (Exception e) {
			log.error(e.toString());
		}
		return headerMap;
	}

	public void deleteColumn(Sheet sheet, int columnToDelete) {
		for (int rId = 0; rId <= sheet.getLastRowNum(); rId++) {
			Row row = sheet.getRow(rId);

			if (row == null) {
				continue;
			}

			for (int cID = columnToDelete; cID < row.getLastCellNum(); cID++) {
				Cell cOld = row.getCell(cID);
				if (cOld != null) {
					row.removeCell(cOld);
				}
				Cell cNext = row.getCell(cID + 1);
				if (cNext != null) {
					Cell cNew = row.createCell(cID, cNext.getCellType());
					cloneCell(cNew, cNext);
					if (rId == 0) {
						sheet.setColumnWidth(cID, sheet.getColumnWidth(cID + 1));

					}
				}
			}
		}
	}

	public void changeColumnPosition(Row row, Integer fromCellPosition, Integer toCellPosition, CellStyle cellStyle) {
		if (fromCellPosition.equals(toCellPosition)) {
			return;
		}

		// get data of fromCell
		Cell fromCell = row.getCell(fromCellPosition);
		String fromCellValue = getCellValue(fromCell);

		// get data of toCell
		Cell toCell = row.getCell(toCellPosition);
		String toCellValue = getCellValue(toCell);

		// create new fromCell and toCell
		fromCell.setCellValue(toCellValue);
		toCell.setCellValue(fromCellValue);
	}

	public void cloneCell(Cell cNew, Cell cOld) {

		cNew.setCellComment(cOld.getCellComment());
		cNew.setCellStyle(cOld.getCellStyle());
		if (CellType.BOOLEAN == cNew.getCellType()) {
			cNew.setCellValue(cOld.getBooleanCellValue());
		} else if (CellType.NUMERIC == cNew.getCellType()) {
			cNew.setCellValue(cOld.getNumericCellValue());
		} else if (CellType.STRING == cNew.getCellType()) {
			cNew.setCellValue(cOld.getStringCellValue());
		} else if (CellType.ERROR == cNew.getCellType()) {
			cNew.setCellValue(cOld.getErrorCellValue());
		} else if (CellType.FORMULA == cNew.getCellType()) {
			cNew.setCellValue(cOld.getCellFormula());
		}
	}

	public void buildCellErrors(StringBuilder originalCellErrors, String newError) {
		if (StringUtils.isEmpty(newError)) {
			return;
		}

		if (StringUtils.isEmpty(originalCellErrors.toString())) {
			originalCellErrors.append("- ").append(newError);
		} else {
			originalCellErrors.append("\n- ").append(newError);
		}
	}

	public void buildEmailErrorList(Set<String> errorSummarySet, String newError) {
		errorSummarySet.add(newError);
	}

	private boolean checkIfRowIsEmpty(Row row) {
		if (row == null) {
			return true;
		}
		if (row.getLastCellNum() <= 0) {
			return true;
		}

		for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
			Cell cell = row.getCell(cellNum);
			if (cell != null && cell.getCellType() != CellType.BLANK && StringUtils.isNotBlank(cell.toString())) {
				return false;
			}
		}
		return true;
	}

	public boolean validateExcelHeader(Class cls, Workbook workbook, Sheet sheet, String fileName,
			ExcelTemplate uploadTemplateHdrIdDto, Map<Integer, StringBuilder> mapExcelErrors,
			Set<String> errorSummarySet, ExcelResponseMessage excelResponseMessage) {
		calculateTimeForPopup(sheet, excelResponseMessage);

		List<ExcelTemplateDet> columnTemplateDetail = uploadTemplateHdrIdDto.getListTempDetail();
		try {
			Map<String, ExcelTemplateDet> columnDetailByColumnName = new HashMap<>();
			Map<Integer, ExcelTemplateDet> columnDetailByColumnPosition = new HashMap<>();
			Map<Integer, Field> fieldByColumnPosition = new HashMap<>();
			Map<String, Field> fieldByFieldName = new HashMap<>();
			StringBuilder fieldHeaderError = new StringBuilder();

			for (ExcelTemplateDet detailColumn : uploadTemplateHdrIdDto.getListTempDetail()) {
				if (!StringUtils.isEmpty(detailColumn.getColumnName())) {
					columnDetailByColumnName.put(detailColumn.getColumnName().trim().toLowerCase(), detailColumn);
				}
			}

			List<Field> fieldList = new ArrayList<>();
			getFieldsOfAClass(cls, fieldList);

			for (Field field : fieldList) {
				field.setAccessible(true);
				fieldByFieldName.put(field.getName(), field);
			}

			// handling columnName
			Row headerRow = sheet.getRow(sheet.getFirstRowNum());
			excelResponseMessage = handlingColumnHeader(headerRow, errorSummarySet, fieldByColumnPosition,
					fieldByFieldName, columnDetailByColumnName, columnDetailByColumnPosition, columnTemplateDetail,
					fieldHeaderError, mapExcelErrors, excelResponseMessage);

			if (!StringUtils.isEmpty(excelResponseMessage.getType())
					&& excelResponseMessage.getCode().equals(Constants.UploadExcelResponse.INVALID_HEADERS)) {
				return false;
			}

		} catch (Exception e) {
			log.error(e.toString());
			excelResponseMessage.setMessage(MessageProvider.getValue("upload.excel.response.invalid.headers", null));
			excelResponseMessage.setCode(Constants.UploadExcelResponse.INVALID_HEADERS);
			excelResponseMessage.setType(Constants.UploadExcelResponse.INVALID_FILE);
			return false;
		}

		return true;
	}

	public boolean checkEmptyFile(Sheet sheet, Integer headerRowNumber, ExcelResponseMessage excelResponseMessage,
			Set<String> errorSet) {
		boolean noRecord = true;
		for (int rowIndex = headerRowNumber + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (!checkIfRowIsEmpty(row)) {
				noRecord = false;
				break;
			}
		}

		if (!noRecord) {
			return false;
		}

		excelResponseMessage.setMessage(MessageProvider.getValue("upload.excel.response.empty.file", null));
		excelResponseMessage.setCode(Constants.UploadExcelResponse.EMPTY_FILE);
		excelResponseMessage.setType(Constants.UploadExcelResponse.INVALID_FILE);

		errorSet.add(MessageProvider.getValue("upload.excel.data.empty", null));
		return true;
	}

	public ExcelTemplate getExcelTemplate(String excelTemplateCode) {
		List<ExcelTemplate> listTemplate = ExcelTemplateDao.getExcelTemplate(excelTemplateCode);

		if (listTemplate.isEmpty()) {
			// throw new TransactionException("exception.excel.template.not.exist");
		}

		ExcelTemplate uploadTemplateHdrIdDto = listTemplate.get(0);
		return uploadTemplateHdrIdDto;
	}

//    public byte[] getByteArrOfUploadedFile(MultipartFile file){
//        byte[] byteArr = null;
//        try{
//            byteArr = file.getBytes();
//        } catch (IOException e) {
//            //throw new TransactionException("exception.excel.read.excel.file.fail");
//        }
//        return byteArr;
//    }

	public Workbook initWorkbook(byte[] byteArr, String fileName) {
		InputStream inputStream = new ByteArrayInputStream(byteArr);
		Workbook workbook = null;
		try {
			if (fileName.endsWith("xlsx")) {
				workbook = new XSSFWorkbook(inputStream);
			} else {
				workbook = new HSSFWorkbook(inputStream);
			}
		} catch (IOException e) {
			// throw new TransactionException("exception.excel.read.excel.file.fail");
		}
		return workbook;
	}

	public boolean validFileType(String fileName, ExcelResponseMessage excelResponseMessage,
			Set<String> errorSummarySet) {
		if (fileName.endsWith("xlsx") || fileName.endsWith("xls")) {
			return true;
		} else {
			errorSummarySet.add(MessageProvider.getValue("upload.excel.email.invalid.file", null));
			excelResponseMessage.setMessage(MessageProvider.getValue("upload.excel.response.invalid.file.type", null));
			excelResponseMessage.setCode(Constants.UploadExcelResponse.INVALID_FILE_TYPE);
			excelResponseMessage.setType(Constants.UploadExcelResponse.INVALID_FILE);
			return false;
		}
	}

	public boolean validSheetNumber(Workbook workbook, int dataSheetIndex, ExcelResponseMessage excelResponseMessage,
			Set<String> errorSummarySet) {
		if (dataSheetIndex < workbook.getNumberOfSheets()) {
			return true;
		} else {
			errorSummarySet.add(MessageProvider.getValue("upload.excel.email.wrong.sheet", null));
			excelResponseMessage.setMessage(MessageProvider.getValue("upload.excel.response.invalid.sheets", null));
			excelResponseMessage.setCode(Constants.UploadExcelResponse.INVALID_HEADERS);
			excelResponseMessage.setType(Constants.UploadExcelResponse.INVALID_FILE);
			return false;
		}
	}

	public void calculateTimeForPopup(Sheet sheet, ExcelResponseMessage excelResponseMessage) {
		int numberOfRecords = 0;
		for (Integer rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (!checkIfRowIsEmpty(row)) {
				numberOfRecords++;
			}
		}
		// avg time to upload a recode: second, estimateProcessingTime: minute
		double estimateProcessingTime = (Constants.UploadExcelResponse.AVG_RECORD_PROCESSING_TIME * numberOfRecords * 2)
				/ 60;
		int roundedProcessingTime = (int) Math.ceil(estimateProcessingTime);

		if (roundedProcessingTime < 1)
			roundedProcessingTime = 1;

		excelResponseMessage.setType(Constants.UploadExcelResponse.UPLOADED_FILE);
		excelResponseMessage.setMessage(MessageProvider.getValue("upload.excel.response.uploaded.file",
				new String[] { String.valueOf(roundedProcessingTime) }));
		excelResponseMessage.setCode(Constants.UploadExcelResponse.UPLOADED_FILE);
	}
}