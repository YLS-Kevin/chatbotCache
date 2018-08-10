/**
 * 
 */
package com.yls.app.entity;

import java.util.Date;

/**
 * @author huangsy
 * @date 2018年3月5日下午5:55:56
 */
public class Account {

	private String id;
	private String aname;
	private int atype;
	private int isauth;
	private Date authtime;
	private int state;
	private String remarks;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAname() {
		return aname;
	}

	public void setAname(String aname) {
		this.aname = aname;
	}

	public int getAtype() {
		return atype;
	}

	public void setAtype(int atype) {
		this.atype = atype;
	}

	public int getIsauth() {
		return isauth;
	}

	public void setIsauth(int isauth) {
		this.isauth = isauth;
	}

	public Date getAuthtime() {
		return authtime;
	}

	public void setAuthtime(Date authtime) {
		this.authtime = authtime;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}
