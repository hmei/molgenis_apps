package plugins;

import java.io.File;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.pheno.Category;
import org.molgenis.pheno.Measurement;
import org.molgenis.protocol.Protocol;

import app.DatabaseFactory;

public class OpalExporter {

	public Database db = null;

	public String investigationName = null;

	public static void main(String args[]) throws Exception {

		new OpalExporter(args);
	}

	public OpalExporter(String args[]) throws Exception {

		db = DatabaseFactory.create();

		if (args.length > 0) {
			investigationName = args[0];
		} else {
			investigationName = "LifeLines";
		}

		WorkbookSettings ws = new WorkbookSettings();

		ws.setLocale(new Locale("en", "EN"));

		File tmpDir = new File(System.getProperty("java.io.tmpdir"));

		String filePath = "";

		if (args.length > 2) {
			filePath = args[2];
		} else {
			filePath = tmpDir.getAbsolutePath() + "/OpalInput.xls";
		}

		File mappingResult = new File(filePath);

		WritableWorkbook workbook = Workbook.createWorkbook(mappingResult, ws);

		WritableSheet variableSheet = workbook.createSheet("Variables", 0);

		WritableSheet categorySheet = workbook.createSheet("Categories", 1);

		String[] OpalVariableHeaders = { "table", "name", "valueType", "unit",
				"label:en", "alias" };
		String[] OpalCategoryHeaders = { "table", "variable", "name",
				"missing", "label:en" };

		for (int index = 0; index < OpalCategoryHeaders.length; index++) {
			categorySheet.addCell(new Label(index, 0,
					OpalCategoryHeaders[index]));
		}

		for (int index = 0; index < OpalVariableHeaders.length; index++) {
			variableSheet.addCell(new Label(index, 0,
					OpalVariableHeaders[index]));
		}

		int rowIndex = 1;
		int categoryRowIndex = 1;

		Query<Protocol> queryRules = db.query(Protocol.class);

		for (int i = 0; i < args.length; i++) {

			if (i == 0) {
				queryRules.addRules(new QueryRule(Protocol.INVESTIGATION_NAME,
						Operator.EQUALS, investigationName));
			} else if (i == 1) {
				queryRules.addRules(new QueryRule(Protocol.NAME,
						Operator.EQUALS, args[i]));
			} else {
				break;
			}
		}

		for (Protocol p : queryRules.find()) {

			if (p.getFeatures_Name().size() > 0) {

				List<Measurement> listOfMeasurements = db.find(
						Measurement.class, new QueryRule(Measurement.NAME,
								Operator.IN, p.getFeatures_Name()));

				for (Measurement m : listOfMeasurements) {

					variableSheet.addCell(new Label(0, rowIndex, p.getName()));

					variableSheet.addCell(new Label(1, rowIndex, m.getName()
							.toLowerCase()));

					String dataType = m.getDataType();
					if (dataType.equals("string")) {
						dataType = "text";
					} else if (dataType.equals("code")) {
						dataType = "text";
					} else if (dataType.equals("categorical")) {
						dataType = "text";
					} else if (dataType.equals("int")) {
						dataType = "integer";
					}

					variableSheet.addCell(new Label(2, rowIndex, dataType));

					if (m.getUnit_Name() != null) {
						String unit = m.getUnit_Name();
						variableSheet.addCell(new Label(3, rowIndex, unit));
					}
					if (m.getDescription() != null)
						variableSheet.addCell(new Label(4, rowIndex, m
								.getDescription()));

					rowIndex++;

					// fill out the Categories sheet.
					if (m.getCategories_Name().size() > 0) {

						List<Category> listOfCategorys = db.find(
								Category.class, new QueryRule(Category.NAME,
										Operator.IN, m.getCategories_Name()));

						for (Category c : listOfCategorys) {
							categorySheet.addCell(new Label(0,
									categoryRowIndex, p.getName()));
							categorySheet
									.addCell(new Label(1, categoryRowIndex, m
											.getName().toLowerCase()));
							categorySheet.addCell(new Label(2,
									categoryRowIndex, c.getName()));
							categorySheet.addCell(new Label(3,
									categoryRowIndex, c.getIsMissing()
											.toString()));
							if (c.getDescription() != null) {
								categorySheet.addCell(new Label(4,
										categoryRowIndex, c.getDescription()));
							}
							categoryRowIndex++;
						}
					}
				}
			}
		}

		workbook.write();
		workbook.close();
		System.out.println("finished");
	}
}
