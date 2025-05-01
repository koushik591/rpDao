package com.chrysler.rp.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

import com.chrysler.rp.model.Document;

public class DocumentsDAO {
	private static DocumentsDAO instance;

	static {
		instance = new DocumentsDAO();
	}

	public static DocumentsDAO getInstance() {
		return instance;
	}

	private DocumentsDAO() {
	}

	public List<Document> findAll(Session session) {
		
		Criteria criteria = null;
		
		criteria = session.createCriteria(Document.class);
			return (List<Document>) criteria.list();
		
	}
}
