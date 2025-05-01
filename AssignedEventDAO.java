package com.chrysler.rp.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.chrysler.rp.model.AssignedEvent;

public class AssignedEventDAO {
	private static AssignedEventDAO instance;

	static {
		instance = new AssignedEventDAO();
	}

	public static AssignedEventDAO getInstance() {
		return instance;
	}

	private AssignedEventDAO() {
	}

	public List<AssignedEvent> findAll(Session session) {
		Criteria criteria = session.createCriteria(AssignedEvent.class);		
		return (List<AssignedEvent>) criteria.list();
	}
	public List<AssignedEvent> findUserAssignedEvent(Session session, long userId) {
		Criteria criteria = session.createCriteria(AssignedEvent.class);
		criteria.add(Restrictions.eq("iUser",userId));
		return (List<AssignedEvent>) criteria.list();
	}

}