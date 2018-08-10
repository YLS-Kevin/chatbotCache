/**
 * 
 */
package com.yls.app.entity;

/**
 * @author huangsy
 * @date 20182018年3月2日下午4:37:18
 */
public class Client {

	private String id;
	private String id_ct;
	private String cname;
	private String state;
	private String sort;
	private String remarks;
	private String create_by;
	private String create_date;
	private String update_by;
	private String update_date;
	private String del_flag;

	private String ctname;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId_ct() {
		return id_ct;
	}

	public void setId_ct(String id_ct) {
		this.id_ct = id_ct;
	}

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
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

	public String getCreate_date() {
		return create_date;
	}

	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}

	public String getUpdate_by() {
		return update_by;
	}

	public void setUpdate_by(String update_by) {
		this.update_by = update_by;
	}

	public String getUpdate_date() {
		return update_date;
	}

	public void setUpdate_date(String update_date) {
		this.update_date = update_date;
	}

	public String getDel_flag() {
		return del_flag;
	}

	public void setDel_flag(String del_flag) {
		this.del_flag = del_flag;
	}

	public String getCtname() {
		return ctname;
	}

	public void setCtname(String ctname) {
		this.ctname = ctname;
	}

}
