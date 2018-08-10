/**
 * 
 */
package com.yls.app.entity;

import java.util.Date;

/**
 * @author huangsy
 * @date 20182018年2月26日上午10:52:09
 */
public class Dialog {

	private String id;
	private String id_dt;
	private int atype;
	private int state;
	private int sort;
	private String remarks;
	private String create_by;
	private Date create_date;
	private String update_by;
	private Date update_date;
	private String del_flag;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId_dt() {
		return id_dt;
	}

	public void setId_dt(String id_dt) {
		this.id_dt = id_dt;
	}

	public int getAtype() {
		return atype;
	}

	public void setAtype(int atype) {
		this.atype = atype;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getCreate_by() {
		return create_by;
	}

	public void setCreate_by(String create_by) {
		this.create_by = create_by;
	}

	public Date getCreate_date() {
		return create_date;
	}

	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}

	public String getUpdate_by() {
		return update_by;
	}

	public void setUpdate_by(String update_by) {
		this.update_by = update_by;
	}

	public Date getUpdate_date() {
		return update_date;
	}

	public void setUpdate_date(Date update_date) {
		this.update_date = update_date;
	}

	public String getDel_flag() {
		return del_flag;
	}

	public void setDel_flag(String del_flag) {
		this.del_flag = del_flag;
	}

}
