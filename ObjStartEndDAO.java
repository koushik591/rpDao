package com.chrysler.rp.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import com.chrysler.rp.model.ObjStartEnd;

public class ObjStartEndDAO {
	private static ObjStartEndDAO instance;

	static {
		instance = new ObjStartEndDAO();
	}

	public static ObjStartEndDAO getInstance() {
		return instance;
	}

	private ObjStartEndDAO() {
	}

	public List<ObjStartEnd> findAll(Session session) {
		Criteria criteria = session.createCriteria(ObjStartEnd.class);
		criteria.addOrder(Order.asc("order"));
		return (List<ObjStartEnd>) criteria.list();
	}

}