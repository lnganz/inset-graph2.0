package ganz.lennon.gtdgraph.io;

import ganz.lennon.gtdgraph.PropertyEdge;
import ganz.lennon.gtdgraph.PropertyVertex;

import java.io.*;
import java.util.*;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.*;
import org.apache.poi.xssf.usermodel.*;
import org.jgrapht.*;

public class GraphImporterExcel {

	// List<PropertyVertex> addedAttributes = new ArrayList<PropertyVertex>(50);
	// Map<String, Object> addedVertices = new HashMap<String, Object>();
	HashMap<String, HashMap<String, PropertyVertex>> addedCorps = new HashMap<String, HashMap<String, PropertyVertex>>(1000);
	HashMap<String, PropertyVertex> addedGroups = new HashMap<String, PropertyVertex>(1000);
	HashMap<String, PropertyVertex> addedTargets = new HashMap<String, PropertyVertex>(1000);
	HashMap<Object, HashSet<PropertyVertex>> indexedByCountryCode = new HashMap<Object, HashSet<PropertyVertex>>(10000);
	HashMap<String, HashMap<String, PropertyVertex>> indexedByCorpNameAndNat = new HashMap<String, HashMap<String, PropertyVertex>>(1000);
	HashMap<Object, HashSet<PropertyVertex>> indexedByGroupName = new HashMap<Object, HashSet<PropertyVertex>>(1000);
	HashMap<Object, HashSet<PropertyVertex>> indexedByYear = new HashMap<Object, HashSet<PropertyVertex>>(1000);
	HashMap<Long, PropertyVertex> indexedByID = new HashMap<Long, PropertyVertex>(1000);
	static int numVerticesAdded = 0;

	
	public boolean importFromExcel(String filename, DirectedGraph<PropertyVertex, PropertyEdge> graph) {

		try {

			int mb = 1024 * 1024; 
	        Runtime instance = Runtime.getRuntime();
	        System.out.println("***** Heap utilization statistics [MB] *****\n");
	        System.out.println("Total Memory: " + instance.totalMemory() / mb);
	        System.out.println("Free Memory: " + instance.freeMemory() / mb);
	        System.out.println("Used Memory: "
	                + (instance.totalMemory() - instance.freeMemory()) / mb);
	        System.out.println("Max Memory: " + instance.maxMemory() / mb);

			
			FileInputStream file = new FileInputStream(new File(filename));

			XSSFWorkbook wb = new XSSFWorkbook(file);
			
			Sheet sheet = wb.getSheetAt(0);
			
			 
	        instance = Runtime.getRuntime();
	        System.out.println("***** Heap utilization statistics [MB] *****\n");
	        System.out.println("Total Memory: " + instance.totalMemory() / mb);
	        System.out.println("Free Memory: " + instance.freeMemory() / mb);
	        System.out.println("Used Memory: "
	                + (instance.totalMemory() - instance.freeMemory()) / mb);
	        System.out.println("Max Memory: " + instance.maxMemory() / mb);

	        
			int rowStart = sheet.getFirstRowNum() + 1;
			int rowEnd = sheet.getLastRowNum();
			int lastColumn;
			int added = 0;
			int cn;

			Cell cell;

			PropertyVertex v1, v2 = null, v3 = null, vCorp, vTarget,
					vg1, vg2, vg3;	//Temporary vertices
			PropertyEdge e;	//Temporary edge
			String curGroup, curTarget = "", curCorp = "", tempStr;//Temporary strings
			boolean corpAdded, unknown;
			int tempNum;
			long vID;
			ArrayList<PropertyVertex> tempAL;
			HashSet<PropertyVertex> tempSet;
			
			for (int rowNum = rowStart; rowNum <= rowEnd; rowNum++) { // while there are more rows
				
				if (rowNum % 200 == 0)
					System.out.println(rowNum + " rows added");
				Row r = sheet.getRow(rowNum); // start of next row

				lastColumn = r.getLastCellNum();

				Iterator<Cell> cellIterator = r.cellIterator();
				while (cellIterator.hasNext()) { // while there are more columns

					vg1 = null;
					vg2 = null; 
					vg3 = null;
					corpAdded = false;
					cn = 0;
					cell = r.getCell(cn); // go to first column

					vID = (long) cell.getNumericCellValue();
					v1 = new PropertyVertex(vID); // new vertex with event ID
					v1.addLabel("INCIDENT");
					graph.addVertex(v1); // Add new vertex to graph
					indexedByID.put(vID, v1);

					cell = r.getCell(++cn);
					
					if (cell != null){
						tempNum = (int) cell.getNumericCellValue();
						v1.addProperty("YEAR", tempNum); // Year
						if (!indexedByYear.containsKey(tempNum)){
							tempSet = new HashSet<PropertyVertex>();
							tempSet.add(v1);
							indexedByYear.put(tempNum, tempSet);
						} else {
							indexedByYear.get(tempNum).add(v1);
						}
						
					}
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("MONTH", (int) cell.getNumericCellValue()); // Month
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("DAY", (int) cell.getNumericCellValue()); // Day
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Approximate Date

					if (cell != null)
						v1.addProperty("EXTENDED_INCIDENT", (cell.getNumericCellValue() == 1)); // Extended Incident?
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Resolution

					if (cell != null){
						tempNum = (int) cell.getNumericCellValue();
						v1.addProperty("COUNTRY_CODE", tempNum); // Country Code
						if (indexedByCountryCode.containsKey(tempNum)){//If the country code is already indexed
							indexedByCountryCode.get(tempNum).add(v1);//add this vertex to that code's arraylist
						}
						else{
							tempSet = new HashSet<PropertyVertex>(10);
							tempSet.add(v1);
							indexedByCountryCode.put((int)tempNum, tempSet);
						}
							
					}
					cell = r.getCell(++cn);

					// if (cell != null)
					// v1.addProperty("COUNTRY_NAME", cell.getStringCellValue()); // Country Name
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("REGION", (int) cell.getNumericCellValue()); // Region Code
					cell = r.getCell(++cn);

					// if (cell != null)
					// v1.addProperty("REGION_NAME", cell.getStringCellValue()); // Region Name
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("PROVIDENCE/STATE", cell.getStringCellValue()); // Providence/State Name
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("CITY", cell.getStringCellValue()); // City Name
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("LATITUDE", cell.getStringCellValue()); // Latitude
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("LONGITUDE", cell.getStringCellValue()); // Longitude
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("SPECIFICITY", (int) cell.getNumericCellValue()); // Specificity
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("VICINITY", (int) cell.getNumericCellValue()); // Vicinity
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("LOCATION", cell.getStringCellValue()); // Location
					cell = r.getCell(++cn);

					// if (cell != null)
					// v1.addProperty("SUMMARY", cell.getStringCellValue()); // String summary of incident
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("CRITERIA_1", (int) cell.getNumericCellValue()); // Political, Economic, Religious...
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("CRITERIA_2", (int) cell.getNumericCellValue()); // Intention to coerce, intimidate...
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("CRITERIA_3", (int) cell.getNumericCellValue()); // Outside International Humanitarian Law
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("DOUBT_TERRORISM_PROPER", (int) cell.getNumericCellValue()); // Uncertain whether an act of
																					// terrorism?
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Alternative Designation

					cell = r.getCell(++cn); // Alternative Text

					if (cell != null)
						v1.addProperty("MULTIPLE_INCIDENT", (int) cell.getNumericCellValue()); // Part of Multiple Incident
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("SUCCESS", (int) cell.getNumericCellValue()); // Successful attack?
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("SUICIDE", (int) cell.getNumericCellValue()); // Suicide attack?
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Attacktype1 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("ATTACK_TYPE_1", cell.getStringCellValue()); // Attacktype1 string
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Attacktype2 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("ATTACK_TYPE_2", cell.getStringCellValue()); // Attacktype2 string
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Attacktype3 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("ATTACK_TYPE_3", cell.getStringCellValue()); // Attacktype3 string
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Targettype1 code

					if (cell != null)
						v1.addProperty("TARGET_TYPE_1", cell.getStringCellValue()); // Targettype1 string
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Targetsubtype1 code

					if (cell != null)
						v1.addProperty("TARGET_SUBTYPE_1", cell.getStringCellValue()); // Targetsubtype1 string
					cell = r.getCell(++cn);

					if (cell != null) {
						curCorp = cell.getStringCellValue();
					}
					cell = r.getCell(++cn);

					if (cell != null) {
						curTarget = cell.getStringCellValue();
						
//						System.out.println("Target: " + curTarget);

					}
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Nationality code

					if (cell != null){ //NATIONALITY STRING, SET UP CORP&TARGET
						if (!cell.getStringCellValue().equals(".")){
							tempStr = cell.getStringCellValue(); //Nationality
							
							if (!curCorp.equals("")){
								unknown = curCorp.equals("Unknown");
								if (!unknown && addedCorps.containsKey(curCorp)) {
									if (addedCorps.get(curCorp).containsKey(tempStr))
										vCorp = addedCorps.get(curCorp).get(tempStr);
									else{
										vCorp = new PropertyVertex(++numVerticesAdded);
										indexedByID.put((long) numVerticesAdded, vCorp);
										graph.addVertex(vCorp);
										vCorp.addProperty("CORPORATION_NAME", curCorp);
										vCorp.addProperty("NATIONALITY", tempStr);
										vCorp.addLabel("CORPORATION");
										addedCorps.get(curCorp).put(tempStr, vCorp);
										indexedByCorpNameAndNat.get(curCorp).put(tempStr, vCorp);
									}
								} else {
									vCorp = new PropertyVertex(++numVerticesAdded);
									graph.addVertex(vCorp);
									indexedByID.put((long) numVerticesAdded, vCorp);
									if (!unknown){
										HashMap<String, PropertyVertex> tempMap = new HashMap<String, PropertyVertex>();
										tempMap.put(tempStr, vCorp);
										addedCorps.put(curCorp, tempMap);
										indexedByCorpNameAndNat.put(curCorp, tempMap);
									}
									vCorp.addProperty("CORPORATION_NAME", curCorp);
									vCorp.addLabel("CORPORATION");
									vCorp.addProperty("NATIONALITY", tempStr);
								}
								v3 = vCorp;
								e = graph.addEdge(v1, vCorp);
								e.addLabel("TARGET_CORPORATION");
								corpAdded = true;
//								System.out.println("CORP: " + curCorp);
								}else
									v3 = null;
							
							unknown = curTarget.equals("Unknown");
							if (!unknown && addedTargets.containsKey(curTarget)) {
								vTarget = addedTargets.get(curTarget);
							} else {
								vTarget = new PropertyVertex(++numVerticesAdded);
								graph.addVertex(vTarget);
								indexedByID.put((long) numVerticesAdded, vTarget);
								if (!unknown)
									addedTargets.put(curTarget, vTarget);
								vTarget.addProperty("TARGET_NAME", curTarget);
								vTarget.addLabel("TARGET");
							}
							e = graph.addEdge(v1, vTarget);
							e.addLabel("TARGET");
							if (v3 != null && !graph.containsEdge(vTarget, v3)){
							e = graph.addEdge(vTarget, v3);
							e.addLabel("SUBTARGET_OF");
							}
						}
					}
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // TargetType2 code
					
					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("TARGET_TYPE_2", cell.getStringCellValue()); // Targettype2 string
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Targetsubtype2 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("TARGET_SUBTYPE_2", cell.getStringCellValue()); // Targetsubtype2 string
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("CORPORATION_2", cell.getStringCellValue()); // Corporation2
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("TARGET_2", cell.getStringCellValue()); // Target2 string
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Nationality code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("TARGET_2_NATIONALITY", cell.getStringCellValue()); // Nationality2 string
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Targettype3 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("TARGET_TYPE_3", cell.getStringCellValue()); // Targettype3 string
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Targetsubtype3 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("TARGET_SUBTYPE_3", cell.getStringCellValue()); // Targetsubtype3 string
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("CORPORATION_3", cell.getStringCellValue()); // Corporation3
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("TARGET_3", cell.getStringCellValue()); // Target3 string
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // Nationality3 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("TARGET_3_NATIONALITY", cell.getStringCellValue()); // Nationality3 string
					cell = r.getCell(++cn);

					curGroup = null;
					if (cell != null) { // PERPETRATOR Group NAME
						curGroup = cell.getStringCellValue();
						if (!curGroup.equals("")){
							if (addedGroups.containsKey(curGroup) && !(curGroup.equals("Unknown"))) {
								v2 = addedGroups.get(curGroup);
							} else {
								v2 = new PropertyVertex(++numVerticesAdded);
								graph.addVertex(v2);
								indexedByID.put((long) numVerticesAdded, v2);
								addedGroups.put(curGroup, v2);
								v2.addProperty("GROUP_NAME", curGroup);
								v2.addLabel("TGROUP");
							}
							e = graph.addEdge(v1, v2);
							e.addLabel("PERPETRATED_BY"); //Edge relating incident to group
							e = graph.addEdge(v2, v1);
							e.addLabel("PERPETRATED");	//Edge relating group to incident
							vg1 = v2;
//							System.out.println("Group: " + curGroup);
							if (indexedByGroupName.containsKey(curGroup)){//If the corp name is already indexed
								indexedByGroupName.get(curGroup).add(v2);//add this vertex to that corp's set
							}
							else{
								tempSet = new HashSet<PropertyVertex>(10);
								tempSet.add(v2);
								indexedByGroupName.put(curGroup, tempSet);
							}
						}
					}

					cell = r.getCell(++cn);

					if ((cell != null) && (vg1 != null) && (!vg1.hasProperty("GROUP_SUBNAME"))) // PERPETRATOR Group
						if (!cell.getStringCellValue().equals(""))								// subname
						v2.addProperty("GROUP_SUBNAME", cell.getStringCellValue());
					cell = r.getCell(++cn);
					
					curGroup = null;
					if (cell != null) { // PERPETRATOR Group NAME 2222222
						curGroup = cell.getStringCellValue();
						if (!curGroup.equals("")){
							if (addedGroups.containsKey(curGroup) && !(curGroup.equals("Unknown"))) {
								v2 = addedGroups.get(curGroup);
							} else {
								v2 = new PropertyVertex(++numVerticesAdded);
								graph.addVertex(v2);
								indexedByID.put((long) numVerticesAdded, v2);
								addedGroups.put(curGroup, v2);
								v2.addProperty("GROUP_NAME", curGroup);
								v2.addLabel("TGROUP");
							}
							if (!graph.containsEdge(v1, v2)){
							e = graph.addEdge(v1, v2);
							e.addLabel("PERPETRATED_BY"); //Edge relating incident to group
							e = graph.addEdge(v2, v1);
							e.addLabel("PERPETRATED");	//Edge relating group to incident
							}
							if (!v2.equals(vg1)){
							if (!graph.containsEdge(v2, vg1)){
								e = graph.addEdge(v2, vg1);
								e.addLabel("COLLAB_WITH");
								e.addList("INCIDENTS", new ArrayList<PropertyVertex>()).add(v1);
								if (!graph.containsEdge(vg1, v2)){
									e = graph.addEdge(vg1, v2);
									e.addLabel("COLLAB_WITH");
									e.addList("INCIDENTS", new ArrayList<PropertyVertex>()).add(v1);
								}
							}
							else {
								graph.getEdge(v2, vg1).getList("INCIDENTS").add(v1);
								graph.getEdge(vg1, v2).getList("INCIDENTS").add(v1);
							}
							}
							if (indexedByGroupName.containsKey(curGroup)){//If the corp name is already indexed
								indexedByGroupName.get(curGroup).add(v2);//add this vertex to that corp's set
							}
							else{
								tempSet = new HashSet<PropertyVertex>(10);
								tempSet.add(v2);
								indexedByGroupName.put(curGroup, tempSet);
							}
							vg2 = v2;
	//						System.out.println("Group: " + curGroup);
						}
					}
					cell = r.getCell(++cn);

					if ((cell != null) && (vg2 != null) && (!vg2.hasProperty("GROUP_SUBNAME"))) // PERPETRATOR Group
						// subname
						if (!cell.getStringCellValue().equals(""))
						v2.addProperty("GROUP_SUBNAME", cell.getStringCellValue());
					cell = r.getCell(++cn); // gsubname2

					curGroup = null;
					if (cell != null) { // PERPETRATOR Group NAME 3333333
						curGroup = cell.getStringCellValue();
						if (!curGroup.equals("")){
						if (addedGroups.containsKey(curGroup) && !(curGroup.equals("Unknown"))) {
							v2 = addedGroups.get(curGroup);
						} else {
							v2 = new PropertyVertex(++numVerticesAdded);
							graph.addVertex(v2);
							indexedByID.put((long) numVerticesAdded, v2);
							addedGroups.put(curGroup, v2);
							v2.addProperty("GROUP_NAME", curGroup);
							v2.addLabel("TGROUP");
						}
						e = graph.addEdge(v1, v2);
						e.addLabel("PERPETRATED_BY"); //Edge relating incident to group
						e = graph.addEdge(v2, v1);
						e.addLabel("PERPETRATED");	//Edge relating group to incident
						if (!v2.equals(vg1)){
						if (!graph.containsEdge(v2, vg1)){
							e = graph.addEdge(v2, vg1);
							e.addList("INCIDENTS", new ArrayList<PropertyVertex>()).add(v1);
							e.addLabel("COLLAB_WITH");
							e = graph.addEdge(vg1, v2);
							e.addList("INCIDENTS", new ArrayList<PropertyVertex>()).add(v1);
							e.addLabel("COLLAB_WITH");
						}
						else{
							graph.getEdge(v2, vg1).getList("INCIDENTS").add(v1);
							graph.getEdge(vg1, v2).getList("INCIDENTS").add(v1);
						}
						}
						if (!graph.containsEdge(v2, vg2)){
							e = graph.addEdge(v2, vg2);
							e.addLabel("COLLAB_WITH");
							e.addList("INCIDENTS", new ArrayList<PropertyVertex>()).add(v1);
							e = graph.addEdge(vg2, v2);
							e.addLabel("COLLAB_WITH");
							e.addList("INCIDENTS", new ArrayList<PropertyVertex>()).add(v1);
							vg3 = v2;
						}
						else{
							graph.getEdge(v2, vg2).getList("INCIDENTS").add(v1);
							graph.getEdge(vg2, v2).getList("INCIDENTS").add(v1);
						}
						if (indexedByGroupName.containsKey(curGroup)){//If the corp name is already indexed
							indexedByGroupName.get(curGroup).add(v2);//add this vertex to that corp's set
						}
						else{
							tempSet = new HashSet<PropertyVertex>(10);
							tempSet.add(v2);
							indexedByGroupName.put(curGroup, tempSet);
						}
//						System.out.println("Group: " + curGroup);
					}
					}
					cell = r.getCell(++cn);

					if ((cell != null) && (vg3 != null) && (!vg3.hasProperty("GROUP_SUBNAME"))) // PERPETRATOR Group
						// subname
						if (!cell.getStringCellValue().equals(""))
						v2.addProperty("GROUP_SUBNAME", cell.getStringCellValue());
					cell = r.getCell(++cn); // gsubname3

					if (cell != null)
						v1.addProperty("MOTIVE", cell.getStringCellValue()); // String description of motive
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("GROUP_UNCERTAINTY", cell.getNumericCellValue()); // Uncertainty of group's
																							// involvment
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // g uncertainty 2

					cell = r.getCell(++cn); // g uncertainty 3

					if (cell != null)
						v1.addProperty("#_PERPETRATORS", (int) cell.getNumericCellValue()); // # perpetrators
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_PERPETRATORS_CAPTURED", (int) cell.getNumericCellValue()); // # perps captured
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("GROUP_1_CLAIM_RESPONSIBILITY", cell.getNumericCellValue());
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // claimmode code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("GROUP_1_CLAIM_MODE", cell.getStringCellValue()); // claimmode string
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // claim2
					cell = r.getCell(++cn); // claimmode2 code
					cell = r.getCell(++cn); // claimmode2 string
					cell = r.getCell(++cn); // claimmode3
					cell = r.getCell(++cn); // claimmode3 code
					cell = r.getCell(++cn); // claimmode3 string
					cell = r.getCell(++cn); // competing claims?

					cell = r.getCell(++cn); // weapon type 1 code

					// if (cell != null)
					// v2 = addPropertyEdgeAndVertex(g, v1, "WEAPON_TYPE", cell.getStringCellValue(), "WEAPON_USED");
					// cell = r.getCell(++cn);

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("WEAPON_TYPE_1", cell.getStringCellValue());
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // weapon subtype code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("WEAPON_SUBTYPE_1", cell.getStringCellValue());
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // weapon type 2 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("WEAPON_TYPE_2", cell.getStringCellValue()); // weapon type 2
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // subtype 2 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("WEAPON_SUBTYPE_2", cell.getStringCellValue()); // weapon subtype 2
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // weapon type 3 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("WEAPON_TYPE_3", cell.getStringCellValue()); // weapon type 3
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // subtype 3 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("WEAPON_SUBTYPE_3", cell.getStringCellValue()); // weapon subtype 3
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // weapon type 4 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("WEAPON_TYPE_4", cell.getStringCellValue()); // weapon type 4
					cell = r.getCell(++cn);

					cell = r.getCell(++cn); // subtype 4 code

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("WEAPON_SUBTYPE_4", cell.getStringCellValue()); // weapon subtype 4
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("WEAPON_DETAIL", cell.getStringCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_KILLED", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_KILLED_US", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_KILLED_PERPETRATORS", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_WOUNDED", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_WOUNDED_US", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_WOUNDED_PERPETRATORS", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("PROPERTY_DAMAGE", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("PROPERTY_EXTENT", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("PROPERTY_EXTENT_SUMMARY", cell.getStringCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("PROPERTY_VALUE", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("PROPERTY_COMMENT", cell.getStringCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("HOSTAGE/KIDNAPPING", cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_HOSTAGES/KIDNAPPED", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_HOSTAGES/KIDNAPPED_US", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_HOURS", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_DAYS", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("DIVERT_COUNTRY", cell.getStringCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("RESOLUTION_COUNTRY", cell.getStringCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("RANSOM", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("RANSOM_AMOUNT", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("RANSOM_AMOUNT_US", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("RANSOM_PAID", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("RANSOM_PAID_US", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("RANSOM_NOTE", cell.getStringCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("HOSTKID_OUTCOME", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						if (!cell.getStringCellValue().equals("."))
						v1.addProperty("HOSTKID_OUTCOME_TXT", cell.getStringCellValue());
					cell = r.getCell(++cn);

					if (cell != null)
						v1.addProperty("#_HOSTKID_RELEASED", (int) cell.getNumericCellValue());
					cell = r.getCell(++cn);	

					break;

				}
				
				file.close();
			}
			 // get Runtime instance
	        instance = Runtime.getRuntime();
	 
	        System.out.println("***** Heap utilization statistics [MB] *****\n");
	 
	        // available memory
	        System.out.println("Total Memory: " + instance.totalMemory() / mb);
	 
	        // free memory
	        System.out.println("Free Memory: " + instance.freeMemory() / mb);
	 
	        // used memory
	        System.out.println("Used Memory: "
	                + (instance.totalMemory() - instance.freeMemory()) / mb);
	 
	        // Maximum available memory
	        System.out.println("Max Memory: " + instance.maxMemory() / mb);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public HashMap<Object, HashSet<PropertyVertex>> getIndexCountryCode(){
		return indexedByCountryCode;
	}
	
	public HashMap<String, HashMap<String, PropertyVertex>> getIndexCorpName(){
		return indexedByCorpNameAndNat;
	}
	
	public HashMap<Object, HashSet<PropertyVertex>> getIndexGroupName(){
		return indexedByGroupName;
	}
	
	public HashMap<Long, PropertyVertex> getIndexID(){
		return indexedByID;
	}
	
	public boolean writeIndexToFile(Map<Object, HashSet<PropertyVertex>> index, String name){
		try {
			PrintWriter writer = new PrintWriter("indexed\\" + name + ".txt");
			writer.print(index.toString());
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}
