package gov.usgswim.sparrow.service.deliveryreport;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.filter.FilteredDataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.TerminalReachesRowFilter;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.SparrowResourceUtils;

import javax.xml.stream.XMLStreamReader;

public class ReportService implements HttpService<ReportRequest> {

    public XMLStreamReader getXMLStreamReader(ReportRequest req,
            boolean isNeedsCompleteFirstRow) throws Exception {
    	
    	SharedApplication sharedApp = SharedApplication.getInstance();
    	
			Integer predictionContextID = req.getContextID();
			PredictionContext context = req.getContext();
			Long modelId = null;

			if (context != null) {
				//The context was supplied w/ the request
				modelId = context.getModelID();
			} else if (predictionContextID != null) {
				//The context was passed by ID
					context = sharedApp.getPredictionContext(predictionContextID);
					modelId = context.getModelID();
			}

			PredictData predictData = sharedApp.getPredictData(modelId);
			TerminalReaches termReaches = context.getTerminalReaches();

			if (termReaches == null || termReaches.isEmpty()) {
				throw new Exception("There must be downstream reaches selected to generate the deliver report.");
			}

			DeliveryReportRequest actionRequest = new DeliveryReportRequest(context.getAdjustmentGroups(), termReaches);

			if (ReportRequest.ReportType.terminal.equals(req.getReportType())) {
				DataTable reportData = sharedApp.getTotalDeliveredLoadSummaryReport(actionRequest);
				TerminalReachesRowFilter filter = new TerminalReachesRowFilter(termReaches);
				FilteredDataTable filteredReportData = new FilteredDataTable(reportData, filter);


				String readmeText = SparrowResourceUtils.lookupModelHelp(
						context.getModelID().toString(),
						"CommonTerms.Total_Delivered_Load_Report");

				return new  ReportSerializer(
						req, filteredReportData, predictData, readmeText);
			} else {
				DataTable reportData = sharedApp.getTotalDeliveredLoadByStateSummaryReport(actionRequest);


				String readmeText = SparrowResourceUtils.lookupModelHelp(
						context.getModelID().toString(),
						"CommonTerms.Total_Delivered_Load_Report");
				
				return new  StateReportSerializer(
						req, reportData, readmeText);
			}
	

	}

	public void shutDown() {
	}
}
