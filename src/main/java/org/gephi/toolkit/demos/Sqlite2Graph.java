/*
 * 从sqlite3文件中读取节点、边的数据，应用Yifan Hu布局进行布局，并根据节点的某一个属性为节点赋予不同的颜色。
 * 同时将产生的网络图保存到pdf文件中，然后为pdf文件添加图例信息。
 */
package org.gephi.toolkit.demos;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.palette.Palette;
import org.gephi.appearance.plugin.palette.PaletteManager;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.database.drivers.SQLiteDriver;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
import org.gephi.io.importer.plugin.database.ImporterEdgeList;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.PartitionFunction;


public class Sqlite2Graph {
	
	public void script() {
		//Init a project - and therefore a workspace
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();
		
		//Copy example database to temp
		File temp;
		try {
			File file = new File(getClass().getResource("/org/gephi/toolkit/demos/network.sqlite3").toURI());
			temp = new File(System.getProperty("java.io.tmpdir"));
			FileUtil.copyFile(FileUtil.toFileObject(file), FileUtil.toFileObject(temp), "network");
			temp = new File(temp, "network.sqlite3");
			temp.deleteOnExit();
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		temp.deleteOnExit();
		
		//Import database
		EdgeListDatabaseImpl db = new EdgeListDatabaseImpl();
		db.setHost(temp.getAbsolutePath());
		db.setDBName("");
		db.setSQLDriver(new SQLiteDriver());
		db.setNodeQuery("select nodes.id as id, nodes.label as label, nodes.community_id as community_id from nodes");
		db.setEdgeQuery("select edges.source as source, edges.target as target, edges.weight as weight from edges");
		ImporterEdgeList edgeListImporter = new ImporterEdgeList();
		
		//load data
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);
		Container container = importController.importDatabase(db,  edgeListImporter);
		container.getLoader().setAllowAutoNode(false);//Don't create missing nodes
		container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);
		
		//Append import imported data to GraphAPI
		importController.process(container, new DefaultProcessor(), workspace);
		
		//See if graph is well imported
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		DirectedGraph graph = graphModel.getDirectedGraph();
		System.out.println("Nodes: " + graph.getNodeCount());
		System.out.println("Edges: " + graph.getEdgeCount());
		
        //Filter
//		  FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
//        DegreeRangeFilter degreeFilter = new DegreeRangeFilter();
//        degreeFilter.init(graph);
//        degreeFilter.setRange(new Range(30, Integer.MAX_VALUE));//Remove nodes with degree < 30
//        Query query = filterController.createQuery(degreeFilter);
//        GraphView view = filterController.filter(query);
//        graphModel.setVisibleView(view);
		
		//Appearance, Partition with 'community_id' column, which is in the data
		AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
		AppearanceModel appearanceModel = appearanceController.getModel();
		Column column = graphModel.getNodeTable().getColumn("community_id");
		Function func = appearanceModel.getNodeFunction(graph, column,  PartitionElementColorTransformer.class);
		Partition partition = ((PartitionFunction) func).getPartition();
		Palette palette = PaletteManager.getInstance().generatePalette(partition.size());
		partition.setColors(palette.getColors());
		appearanceController.transform(func);
		
		//Get values of partitions, print the color for each partition's value.
		System.out.println("sorted values: " + partition.getSortedValues());
		for (Object value: partition.getSortedValues()) {
			Color color = partition.getColor(value);
			System.out.println("value: " + value + ", color: " + color);
			System.out.println("Red:" + color.getRed() + "Green:" + color.getGreen() + "Blue:" + color.getBlue());
			System.out.println(color.getRGB());
		}	
		
		//Preview, label, label size
        PreviewModel previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.TRUE);
		
		//Layout, five times of Yifan Hu, each has 50 passes at most.
		for (int loop = 1; loop <= 5; loop++) {
			YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
			layout.setGraphModel(graphModel);
			layout.resetPropertiesValues();
			layout.initAlgo();
			for (int i = 1; i <= 50 && layout.canAlgo(); i++) {
				layout.goAlgo();
				System.out.println("# of loop: " + loop + ";  # of passes: " + i);
			}
			//Export to pdf after each loop
			ExportController ec = Lookup.getDefault().lookup(ExportController.class);
			try {
				ec.exportFile(new File("PDFGraph/sqlite/sqlite_" +Integer.toString(loop) + ".pdf"));
				//Add legend.
				EditPdf editPdf = new EditPdf();
				editPdf.addLegend(partition, "PDFGraph/sqlite/sqlite_" +Integer.toString(loop) + ".pdf");
			} catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
			layout.endAlgo();
		}
	} 
}
