package com.example.hrms.common.http.model;

import com.example.hrms.biz.user.model.User;
import com.example.hrms.common.http.criteria.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonSerialize
public class ResultPageData<T> extends Result {

	@JsonProperty("pageSize")
	private int pageSize;

	@JsonProperty("pageNo")
	private int pageNo;

	@JsonProperty("total")
	private int total;

	@JsonProperty("data")
	private T resultData;

	public ResultPageData(Object status, int total, List<User> userList) {
		super();
	}

	public ResultPageData(Page page) {
		super();
		this.pageSize = page.getPageSize();
		this.pageNo = page.getPageNo();
	}

	public ResultPageData(Page page, int total) {
		super();
		this.pageSize = page.getPageSize();
		this.pageNo = page.getPageNo();
		this.total = total;
	}

	public ResultPageData(String status) {
		super(status);
	}

	public ResultPageData(Status status, T resultData) {
		super(status);
		this.resultData = resultData;
	}

	public ResultPageData(String status, T resultData) {
		super(status);
		this.resultData = resultData;
	}

	public ResultPageData(String status, String msg, T resultData) {
		super(status, msg);
		this.resultData = resultData;
	}

	public ResultPageData(String status, String code, String msg, T resultData) {
		super(status, code, msg);
		this.resultData = resultData;
	}

	public T getResultData() {
		return resultData;
	}

	public void setResultData(T resultData) {
		this.resultData = resultData;
	}

	@Override
	public String toString() {
		return "ResultData [resultData=" + resultData + "]";
	}
}
