/* Date:        November 15, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.3
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package plugins.breedingplugin;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.matrix.component.MatrixViewer;
import org.molgenis.matrix.component.SliceablePhenoMatrix;
import org.molgenis.matrix.component.general.MatrixQueryRule;
import org.molgenis.pheno.Individual;
import org.molgenis.pheno.Measurement;
import org.molgenis.pheno.ObservationElement;
import org.molgenis.pheno.ObservationTarget;
import org.molgenis.pheno.ObservedValue;
import org.molgenis.protocol.ProtocolApplication;
import org.molgenis.util.Entity;
import org.molgenis.util.Tuple;

import commonservice.CommonService;

public class ManageParentgroups extends PluginModel<Entity>
{
	private static final long serialVersionUID = 203412348106990472L;
	private List<Integer> selectedMotherIdList = null;
	private List<Integer> selectedFatherIdList = null;
	private CommonService ct = CommonService.getInstance();
	private SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
	private SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	private String startdate = null;
	private List<ObservationTarget> lineList;
	private int line = -1;
	private String remarks = null;
	private List<ObservationTarget> pgList;
	MatrixViewer motherMatrixViewer = null;
	MatrixViewer fatherMatrixViewer = null;
	private static String MOTHERMATRIX = "mothermatrix";
	private static String FATHERMATRIX = "fathermatrix";
	private String action = "init";
	private int userId = -1;
	
	public ManageParentgroups(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}
	
	public String getCustomHtmlHeaders() {
		return "<script type=\"text/javascript\" src=\"res/jquery-plugins/datatables/js/jquery.dataTables.js\"></script>\n" +
				"<script src=\"res/jquery-plugins/ctnotify/lib/jquery.ctNotify.js\" language=\"javascript\"></script>\n" +
				"<script src=\"res/scripts/custom/addingajax.js\" language=\"javascript\"></script>\n" +
				"<link rel=\"stylesheet\" style=\"text/css\" href=\"res/jquery-plugins/datatables/css/demo_table_jui.css\">\n" +
				"<link rel=\"stylesheet\" style=\"text/css\" href=\"res/jquery-plugins/ctnotify/lib/jquery.ctNotify.css\">" +
				"<link rel=\"stylesheet\" style=\"text/css\" href=\"res/css/animaldb.css\">";
	}

	public List<Integer> getSelectedMotherIdList() {
		return selectedMotherIdList;
	}
	public void setSelectedMotherList(List<Integer> selectedMotherIdList) {
		this.selectedMotherIdList = selectedMotherIdList;
	}

	public List<Integer> getSelectedFatherIdList() {
		return selectedFatherIdList;
	}
	public void setSelectedFatherList(List<Integer> selectedFatherIdList) {
		this.selectedFatherIdList = selectedFatherIdList;
	}
	
	public String getAnimalName(Integer id) {
		try {
			return ct.getObservationTargetLabel(id);
		} catch (Exception e) {
			return id.toString();
		}
	}
	
	public String getStartdate() {
		return startdate;
	}
	public void setStartdate(String startdate) {
		this.startdate = startdate;
	}

	public void setLine(int line) {
		this.line = line;
	}
	public int getLine() {
		return line;
	}

	public List<ObservationTarget> getLineList() {
		return lineList;
	}
	public void setLineList(List<ObservationTarget> lineList) {
		this.lineList = lineList;
	}

	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public List<ObservationTarget> getPgList() {
		return pgList;
	}
	public void setPgList(List<ObservationTarget> pgList) {
		this.pgList = pgList;
	}
	
	public String getPgStartDate(int pgId) {
		try {
			return ct.getMostRecentValueAsString(pgId, ct.getMeasurementId("StartDate"));
		} catch (Exception e) {
			e.printStackTrace();
			return "Error when retrieving start date";
		}
	}
	
	public String getPgRemarks(int pgId) throws DatabaseException {
		List<String> remarksList = ct.getRemarks(pgId);
		String returnString = "";
		for (String remark : remarksList) {
			returnString += (remark + "<br>");
		}
		if (returnString.length() > 0) {
			returnString = returnString.substring(0, returnString.length() - 4);
		}
		return returnString;
	}
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	
	private String motherMatrixViewerString;
	public String getMotherMatrixViewer()
	{
		return motherMatrixViewerString;
	}
	public void renderMotherMatrixViewer(Database db) {
		if (motherMatrixViewer != null) {
			motherMatrixViewer.setDatabase(db);
			motherMatrixViewerString =  motherMatrixViewer.render();
		} else {
			motherMatrixViewerString = "No viewer available, matrix for selecting mother(s) cannot be rendered.";
		}
	}
	
	private String fatherMatrixViewerString;
	public String getFatherMatrixViewer()
	{
		return fatherMatrixViewerString;
	}
	public void renderFatherMatrixViewer(Database db) {
		if (fatherMatrixViewer != null) {
			fatherMatrixViewer.setDatabase(db);
			fatherMatrixViewerString = fatherMatrixViewer.render();
		} else {
			fatherMatrixViewerString = "No viewer available, matrix for selecting father(s) cannot be rendered.";
		}
	}

	@Override
	public String getViewName()
	{
		return "plugins_breedingplugin_ManageParentgroups";
	}

	@Override
	public String getViewTemplate()
	{
		return "plugins/breedingplugin/ManageParentgroups.ftl";
	}
	
	private void AddParents(Database db, List<Integer> parentIdList, String protocolName, String eventName, 
			String featureName, String valueName, String valueCertainName, int parentgroupid, Date tmpDate) 
			throws DatabaseException, ParseException, IOException {
		
		int invid = ct.getOwnUserInvestigationIds(this.getLogin().getUserId()).get(0);
		int protocolId = ct.getProtocolId(protocolName);
		
		// Init lists that we can later add to the DB at once
		List<ObservedValue> valuesToAddList = new ArrayList<ObservedValue>();
		
		for (int parentId : parentIdList) {
			// Find the 'SetMother'/'SetFather' event type
			// TODO: SetMother/SetFather are now plain event types with only the Mother/Father feature
			// and no longer the Certain feature. Solve this!
			// Make the event
			ProtocolApplication app = ct.createProtocolApplication(invid, protocolId);
			db.add(app);
			int eventid = app.getId();
			// Make 'Mother'/'Father' feature-value pair and link to event
			int measurementId = ct.getMeasurementId(featureName);
			valuesToAddList.add(ct.createObservedValue(invid, eventid, tmpDate, null, measurementId, parentId, 
					null, parentgroupid));		
			// Make 'Certain' feature-value pair and link to event
			String valueString;
			if (parentIdList.size() == 1) {
				valueString = "1"; // if there's only one parent of this gender, it's certain
			} else {
				valueString = "0"; // ... otherwise, not
			}
			measurementId = ct.getMeasurementId("Certain");
			valuesToAddList.add(ct.createObservedValue(invid, eventid, tmpDate, null, measurementId, parentId, 
					valueString, 0));
		}
		// Add everything to DB
		db.add(valuesToAddList);
	}
	
	private void resetUserFields() {
		this.selectedMotherIdList.clear();
		this.selectedFatherIdList.clear();
		this.setStartdate(dateOnlyFormat.format(new Date()));
		this.setRemarks(null);
		if (lineList.size() > 0) {
			this.setLine(lineList.get(0).getId());
		} else {
			this.setLine(0);
		}
	}

	@Override
	public void handleRequest(Database db, Tuple request)
	{	
		ct.setDatabase(db);
		action = request.getString("__action");
		try {
			Date now = new Date();
			int invid = ct.getOwnUserInvestigationIds(this.getLogin().getUserId()).get(0);
			
			if (action.startsWith(motherMatrixViewer.getName())) {
				motherMatrixViewer.handleRequest(db, request);
				this.setAction("addParentgroupScreen2"); // return to mother selection screen
			}
			if (action.startsWith(fatherMatrixViewer.getName())) {
				fatherMatrixViewer.handleRequest(db, request);
				this.setAction("addParentgroupScreen3"); // return to father selection screen
			}
			
			if (action.equals("init")) {
				// do nothing here
			}
			
			if (action.equals("addParentgroupScreen1")) {
				// do nothing here
			}
			
			if (action.equals("addParentgroupScreen2")) {
				// Save line that was set in screen 1
				if (request.getInt("line") != null) {
					this.line = request.getInt("line");
				}
				this.getMessages().add(new ScreenMessage("Line successfully set", true));
				renderMotherMatrixViewer(db);
			}
			
			if (action.equals("addParentgroupScreen3")) {
				String motherNames = "";
				List<ObservationElement> rows = (List<ObservationElement>) motherMatrixViewer.getSelection(db);
				int rowCnt = 0;
				for (ObservationElement row : rows) {
					if (request.getBool(MOTHERMATRIX + "_selected_" + rowCnt) != null) {
						int motherId = row.getId();
						if (!this.selectedMotherIdList.contains(motherId)) {
							this.selectedMotherIdList.add(motherId);
							motherNames += row.getName() + " ";
						}
					}
					rowCnt++;
				}
				// Check if at least one mother selected:
				if (this.selectedMotherIdList.size() == 0) {
					throw new Exception("No mother(s) selected");
				}
				this.getMessages().add(new ScreenMessage("Mother(s) " + motherNames + "successfully added", true));
				renderFatherMatrixViewer(db);
			}
			
			if (action.equals("addParentgroupScreen4")) {
				String fatherNames = "";
				List<ObservationElement> rows = (List<ObservationElement>) fatherMatrixViewer.getSelection(db);
				int rowCnt = 0;
				for (ObservationElement row : rows) {
					if (request.getBool(FATHERMATRIX + "_selected_" + rowCnt) != null) {
						int fatherId = row.getId();
						if (!this.selectedFatherIdList.contains(fatherId)) {
							this.selectedFatherIdList.add(fatherId);
							fatherNames += row.getName() + " ";
						}
					}
					rowCnt++;
				}
				// Check if at least one father selected:
				if (this.selectedFatherIdList.size() == 0) {
					throw new Exception("No father(s) selected");
				}
				this.getMessages().add(new ScreenMessage("Father(s) " + fatherNames + "successfully added", true));
			}
			
			if (action.equals("addParentgroup")) {
				// Save start date and remarks that were set in screen 4
				if (request.getString("startdate") != null) {
					setStartdate(request.getString("startdate"));
				}
				if (request.getString("remarks") != null) {
					setRemarks(request.getString("remarks"));
				}
				Date eventDate = dateOnlyFormat.parse(startdate);
				int userId = this.getLogin().getUserId();
				// Make group
				String groupPrefix = "PG_" + ct.getObservationTargetLabel(line) + "_";
				int groupNr = ct.getHighestNumberForPrefix(groupPrefix) + 1;
				String groupNrPart = "" + groupNr;
				groupNrPart = ct.prependZeros(groupNrPart, 6);
				int groupId = ct.makePanel(invid, groupPrefix + groupNrPart, userId);
				// Make or update name prefix entry
				ct.updatePrefix(userId, "parentgroup", groupPrefix, groupNr);
				// Mark group as parent group using a special event
				int protocolId = ct.getProtocolId("SetTypeOfGroup");
				int measurementId = ct.getMeasurementId("TypeOfGroup");
				db.add(ct.createObservedValueWithProtocolApplication(invid, now, null, 
						protocolId, measurementId, groupId, "Parentgroup", 0));
				// Add parent(s)
				AddParents(db, this.selectedMotherIdList, "SetMother", "eventmother", "Mother", "valuemother", 
						"valuemothercertain", groupId, eventDate);
				AddParents(db, this.selectedFatherIdList, "SetFather", "eventfather", "Father", "valuefather", 
						"valuefathercertain", groupId, eventDate);
				// Set line
				protocolId = ct.getProtocolId("SetLine");
				measurementId = ct.getMeasurementId("Line");
				db.add(ct.createObservedValueWithProtocolApplication(invid, now, null, 
						protocolId, measurementId, groupId, null, line));
				// Set start date
				protocolId = ct.getProtocolId("SetStartDate");
				measurementId = ct.getMeasurementId("StartDate");
				db.add(ct.createObservedValueWithProtocolApplication(invid, now, null, 
						protocolId, measurementId, groupId, dbFormat.format(eventDate), 0));
				// Set remarks
				if (remarks != null) {
					protocolId = ct.getProtocolId("SetRemark");
					measurementId = ct.getMeasurementId("Remark");
					db.add(ct.createObservedValueWithProtocolApplication(invid, now, null, 
							protocolId, measurementId, groupId, remarks, 0));
				}
				
				// Success: empty selected lists and show success message
				this.setAction("init");
				this.resetUserFields();
				this.getMessages().add(new ScreenMessage("Parent group " + (groupPrefix + groupNrPart) + " successfully added", true));
			}
			
		} catch (Exception e) {
			String message = "Something went wrong";
			if (e.getMessage() != null) {
				message += ": " + e.getMessage();
			}
			this.getMessages().add(new ScreenMessage(message, false));
			e.printStackTrace();
		}
	}

	@Override
	public void reload(Database db)
	{
		ct.setDatabase(db);
		// Populate lists (do this on every reload so they keep fresh, and do it here
		// because we need the lineList in the init part that comes after)
		try {
			List<Integer> investigationIds = ct.getAllUserInvestigationIds(this.getLogin().getUserId());
			// Populate existing PG list
			pgList =  ct.getAllMarkedPanels("Parentgroup", investigationIds);
			// Populate line list
			lineList = ct.getAllMarkedPanels("Line", investigationIds);
			// Default selected is first line
			if (line == -1 && lineList.size() > 0) {
				line = lineList.get(0).getId();
			}
			if (selectedMotherIdList == null) {
				selectedMotherIdList = new ArrayList<Integer>();
			}
			if (selectedFatherIdList == null) {
				selectedFatherIdList = new ArrayList<Integer>();
			}
			
		} catch (Exception e) {
			String message = "Something went wrong while loading lists";
			if (e.getMessage() != null) {
				message += (": " + e.getMessage());
			}
			this.getMessages().add(new ScreenMessage(message, false));
			e.printStackTrace();
		}
		// Some init that only needs to be done once after login
		if (userId != this.getLogin().getUserId().intValue()) {
			userId = this.getLogin().getUserId().intValue();
			ct.setDatabase(db);
			ct.makeObservationTargetNameMap(userId, false);
			this.setStartdate(dateOnlyFormat.format(new Date()));
			
			reloadMatrixViewers(db);
		}
	}
	
	private void reloadMatrixViewers(Database db) {
		try {
			List<String> investigationNames = ct.getAllUserInvestigationNames(userId);
			
			List<String> measurementsToShow = new ArrayList<String>();
			measurementsToShow.add("Species");
			measurementsToShow.add("Sex");
			measurementsToShow.add("Active");
			measurementsToShow.add("Line");
			// Mother matrix viewer
			List<MatrixQueryRule> motherFilterRules = new ArrayList<MatrixQueryRule>();
			motherFilterRules.add(new MatrixQueryRule(MatrixQueryRule.Type.rowHeader, Individual.INVESTIGATION_NAME, 
					Operator.IN, investigationNames));
			motherFilterRules.add(new MatrixQueryRule(MatrixQueryRule.Type.colValueProperty, ct.getMeasurementId("Sex"),
					ObservedValue.RELATION_NAME, Operator.EQUALS, "Female"));
			motherFilterRules.add(new MatrixQueryRule(MatrixQueryRule.Type.colValueProperty, ct.getMeasurementId("Active"),
					ObservedValue.VALUE, Operator.EQUALS, "Alive"));
			if (line != -1) {
				motherFilterRules.add(new MatrixQueryRule(MatrixQueryRule.Type.colValueProperty, 
						ct.getMeasurementId("Line"), ObservedValue.RELATION_NAME, Operator.EQUALS,
						ct.getObservationTargetLabel(line)));
				// Setting filter on the RELATION field with value = line would be more efficient,
				// but gives a very un-userfriendly toString value when shown in the UI
			}
			
			SliceablePhenoMatrix SPMM = new SliceablePhenoMatrix(Individual.class, Measurement.class);
			SPMM.setDatabase(db);
			motherMatrixViewer = new MatrixViewer(this, MOTHERMATRIX, SPMM, true, true, motherFilterRules, 
					new MatrixQueryRule(MatrixQueryRule.Type.colHeader, Measurement.NAME, Operator.IN, measurementsToShow));
			// Father matrix viewer
			List<MatrixQueryRule> fatherFilterRules = new ArrayList<MatrixQueryRule>();
			fatherFilterRules.add(new MatrixQueryRule(MatrixQueryRule.Type.rowHeader, Individual.INVESTIGATION_NAME, 
					Operator.IN, investigationNames));
			fatherFilterRules.add(new MatrixQueryRule(MatrixQueryRule.Type.colValueProperty, ct.getMeasurementId("Sex"),
					ObservedValue.RELATION_NAME, Operator.EQUALS, "Male"));
			fatherFilterRules.add(new MatrixQueryRule(MatrixQueryRule.Type.colValueProperty, ct.getMeasurementId("Active"),
					ObservedValue.VALUE, Operator.EQUALS, "Alive"));
			if (line != -1) {
				fatherFilterRules.add(new MatrixQueryRule(MatrixQueryRule.Type.colValueProperty, 
						ct.getMeasurementId("Line"), ObservedValue.RELATION_NAME, Operator.EQUALS,
						ct.getObservationTargetLabel(line)));
				// Setting filter on the RELATION field with value = line would be more efficient,
				// but gives a very un-userfriendly toString value when shown in the UI
			}
			SliceablePhenoMatrix SPMF = new SliceablePhenoMatrix(Individual.class, Measurement.class);
			SPMF.setDatabase(db);
			fatherMatrixViewer = new MatrixViewer(this, FATHERMATRIX, SPMF, 
					true, true, fatherFilterRules, new MatrixQueryRule(MatrixQueryRule.Type.colHeader, Measurement.NAME, 
							Operator.IN, measurementsToShow));
		} catch (Exception e) {
			String message = "Something went wrong while loading matrix viewers";
			if (e.getMessage() != null) {
				message += (": " + e.getMessage());
			}
			this.getMessages().add(new ScreenMessage(message, false));
			e.printStackTrace();
		}
	}
	
}
