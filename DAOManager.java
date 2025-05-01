package com.chrysler.rp.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.chrysler.rp.App;

public class DAOManager {

	private static SessionFactory sessionFactory;

	private static UserDAO userDAO = UserDAO.getInstance();
	private static ReportDAO reportDAO = ReportDAO.getInstance();
	private static DocumentsDAO documentsDAO = DocumentsDAO.getInstance();
	private static BannerMessageDAO bannerMessageDAO = BannerMessageDAO.getInstance();
	private static ProjectTypeDAO projectTypeDAO = ProjectTypeDAO.getInstance();
	private static AssignedEventDAO assignedEventDAO = AssignedEventDAO.getInstance();
	private static DB2Interceptor db2Interceptor = new DB2Interceptor();

	static {
		try {
			sessionFactory = new Configuration().configure(App.getProperty(App.HIBERNATE_CONFIG_FILE))
					.buildSessionFactory();
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static Session getSession() {
		return getSessionFactory().openSession(db2Interceptor);
		//return getSessionFactory().openSession();
	}

	public static void shutdown() {
		getSessionFactory().close();
	}

	public static UserDAO getUserDAO() {
		return userDAO;
	}

	public static ReportDAO getReportDAO() {
		return reportDAO;
	}

	public static DocumentsDAO getDocumentDAO() {
		return documentsDAO;
	}
	public static ProjectTypeDAO getProjectTypeDAO() {
		return projectTypeDAO;
	}
	public static AssignedEventDAO getAssignedEventDAO() {
		return assignedEventDAO;
	}

	public static BannerMessageDAO getBannerMessageDAO() {
		return bannerMessageDAO;
	}
}
