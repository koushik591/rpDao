package com.chrysler.rp.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

import com.chrysler.rp.model.BannerMessage;

public class BannerMessageDAO {
	private static BannerMessageDAO instance;

	static {
		instance = new BannerMessageDAO();
	}

	public static BannerMessageDAO getInstance() {
		return instance;
	}

	private BannerMessageDAO() {
	}

	public List<BannerMessage> findAll(Session session) {
		Criteria criteria = session.createCriteria(BannerMessage.class);		
		return (List<BannerMessage>) criteria.list();
	}

}
