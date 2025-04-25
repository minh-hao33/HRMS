package com.example.hrms.common.http.model;

@SuppressWarnings("unused")
public enum Status {

	SUCCESS("success", "00", "성공"),
	FAIL("fail", "01", "실패"),
	EXCEPTION_FAIL("fail", "99", "예외발생"),
	;
	private final String status;
	private final String code;
	private final String message;

	Status(String status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}