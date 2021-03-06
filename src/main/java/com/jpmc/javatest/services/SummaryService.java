package com.jpmc.javatest.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jpmc.javatest.model.Message;
import com.jpmc.javatest.model.Product;
import com.jpmc.javatest.model.ReportingBean;
import com.jpmc.javatest.model.Sale;

public class SummaryService {

	private Map<String, List<ReportingBean>> messageSummaryMap = new HashMap<String, List<ReportingBean>>();

	public void addToSummary(Message message) {
		Product product = message.getProduct();
		int quantity = message.getQuantity();

		List<ReportingBean> reportingBeanList = messageSummaryMap.get(product.getProductType());
		if (null != reportingBeanList) {
			boolean isNew = true;
			for (ReportingBean reportingBean : reportingBeanList) {
				if (message.getProduct().getValue().equals(reportingBean.getRate())) {
					isNew = false;
					reportingBean.setValue(reportingBean.getValue().add(BigDecimal.valueOf(product.getValue().doubleValue() * quantity)));
					reportingBean.setQuantity(reportingBean.getQuantity() + message.getQuantity());
					reportingBean.setRate(product.getValue());
					break;
				}
			}
			if (isNew) {
				ReportingBean newReportingBean = new ReportingBean();
				newReportingBean.setValue(BigDecimal.valueOf(product.getValue().doubleValue() * quantity));
				newReportingBean.setProductType(product.getProductType());
				newReportingBean.setQuantity(message.getQuantity());
				newReportingBean.setRate(product.getValue());
				messageSummaryMap.get(product.getProductType()).add(newReportingBean);
			}
		} else {
			ReportingBean newReportingBean = new ReportingBean();
			newReportingBean.setValue(BigDecimal.valueOf(product.getValue().doubleValue() * quantity));
			newReportingBean.setProductType(product.getProductType());
			newReportingBean.setQuantity(message.getQuantity());
			newReportingBean.setRate(product.getValue());
			List<ReportingBean> tempList = new ArrayList<ReportingBean>();
			tempList.add(newReportingBean);
			messageSummaryMap.put(product.getProductType(), tempList);
		}
	}

	public void updateSummary(Message message) {
		Product product = message.getProduct();
		List<ReportingBean> reportingBeanList = messageSummaryMap.get(product.getProductType());
		if (null != reportingBeanList) {
			for (ReportingBean reportingBean : reportingBeanList) {
				if (message.getProduct().getValue() == reportingBean.getRate()) {
					message.getProduct().setValue(message.getAdjustedValue());
					reportingBean.setRate(message.getAdjustedValue());
					reportingBean.setValue(BigDecimal.valueOf(message.getAdjustedValue().doubleValue() * reportingBean.getQuantity()));
					break;
				}
			}
		}
	}

	public Map<String, List<ReportingBean>> getMessageSummaryMap() {
		return messageSummaryMap;
	}

	public void printSummary(Sale sale) {
		System.out.println("--------------------------------------------------------");
		System.out.println("------------------------ Summary -----------------------");
		System.out.println("Product ------ Quantity ------ Total Value (in p) ------");
		System.out.println("---------------------------------------------------------");
		Map<String, ReportingBean> displayBean = new HashMap<String, ReportingBean>();
		for (String productType : getMessageSummaryMap().keySet()) {
			List<ReportingBean> reportingBeanList = getMessageSummaryMap().get(productType);
			for (ReportingBean reportingBean : reportingBeanList) {
				ReportingBean tempBean = displayBean.get(reportingBean.getProductType());
				if (null != tempBean) {
					tempBean.setValue(tempBean.getValue().add(reportingBean.getValue()));
					tempBean.setQuantity(tempBean.getQuantity() + reportingBean.getQuantity());
				} else {
					ReportingBean rNewBean = new ReportingBean();
					rNewBean.setValue(reportingBean.getValue());
					rNewBean.setProductType(reportingBean.getProductType());
					rNewBean.setQuantity(reportingBean.getQuantity());
					displayBean.put(reportingBean.getProductType(), rNewBean);
				}
			}
		}
		for (String productType : displayBean.keySet()) {
			ReportingBean rBean = displayBean.get(productType);
			System.out.println(productType + "           " + rBean.getQuantity() + "               " + rBean.getValue());
		}
	}

	public void printFinalSummary(Sale sale) {
		System.out.println("");
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("---------------------- Final Summary ------------------------------------");
		System.out.println("Product ------ Adjustments (in p) ------ Adjusted Difference (in p)  -------");
		System.out.println("-------------------------------------------------------------------------");
		Map<String, ReportingBean> summaryMap = new HashMap<String, ReportingBean>();
		for (Message message : sale.getMessageList()) {
			Product product = message.getProduct();
			if (null != message.getAdjustedValue()) {
				BigDecimal difference = BigDecimal.ZERO;
				if (null != summaryMap.get(product.getProductType())) {
					ReportingBean reportingBean = summaryMap.get(product.getProductType());
					BigDecimal totalAdjustedValue = reportingBean.getAdjustedValue();
					BigDecimal currentAjustedValue = new BigDecimal(message.getAdjustedValue().doubleValue() * message.getQuantity());
					BigDecimal finalAdjustedValue = totalAdjustedValue.add(currentAjustedValue);
					difference = currentAjustedValue.subtract(new BigDecimal(product.getOriginalValue().doubleValue() * message.getQuantity()));
					reportingBean.setAdjustedValue(finalAdjustedValue);
					reportingBean.setValue(reportingBean.getValue().add(difference));
					summaryMap.put(product.getProductType(), reportingBean);
				} else {
					ReportingBean reportingBean = new ReportingBean();
					BigDecimal finalAdjustedValue = new BigDecimal(message.getAdjustedValue().doubleValue() * message.getQuantity());
					reportingBean.setAdjustedValue(finalAdjustedValue);
					difference = finalAdjustedValue.subtract(new BigDecimal(product.getOriginalValue().doubleValue() * message.getQuantity()));
					reportingBean.setValue(difference);
					summaryMap.put(product.getProductType(), reportingBean);
				}
			}
		}
		if (summaryMap.keySet().size() > 0) {
			for (String productType : summaryMap.keySet()) {
				ReportingBean rBean = summaryMap.get(productType);
				System.out.println(productType + "           " + rBean.getAdjustedValue() + "                       " + rBean.getValue());
			}
		} else {
			System.out.println(" **  No Adjustment Made ** ");
		}
	}
}
