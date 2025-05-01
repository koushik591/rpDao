package com.chrysler.rp.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.chrysler.rp.gxt.shared.UIConstants;
import com.chrysler.rp.gxt.shared.UserData;
import com.chrysler.rp.model.Report;

public class ReportDAO {
	private static ReportDAO instance;

	static {
		instance = new ReportDAO();
	}

	public static ReportDAO getInstance() {
		return instance;
	}

	private ReportDAO() {
	}

	public List<Report> findAll(String categoriesList,Session session,UserData user) {
		
		String[] categoriesLists = {""};
		
		/*Criteria criteria = session.createCriteria(Report.class);
		return (List<Report>) criteria.list();*/
		
		if(!categoriesList.contains(",")){
			categoriesLists[0] = categoriesList;
		}else {
			categoriesLists = categoriesList.split(",");
		}
		
		Criteria criteria = null;
		
		if(categoriesLists[0].equals(UIConstants.ALL)){
			criteria = session.createCriteria(Report.class);
		}else if(categoriesLists[0].equals(UIConstants.UNQ)){
			criteria = session.createCriteria(Report.class);
			criteria.setProjection( Projections.distinct( Projections.property( "category" ) ) );


			//criteria = criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		}else {
			criteria = session.createCriteria(Report.class).add(Restrictions.in("category", categoriesLists));
		}
		
		// RP-APAC --- > Check if the role of the user is not IT ADMIN, then show him the reports of only his region
		
		if(null!=user){
		if(!user.isItSuperAdmin() && user.getUSER_REGION().equalsIgnoreCase("N")){
			criteria.add(Restrictions.eq("reportRegion", user.getUSER_REGION()));
		}
		//Commented and edited by T7166PA--Starts
//		else if (!user.isItSuperAdmin()  && user.getUSER_REGION().equalsIgnoreCase("A")) {
//			criteria.add(Restrictions.eq("reportRegion", user.getUSER_REGION()));
//		}
		else if (!user.isItSuperAdmin()  && (user.getUSER_REGION().equalsIgnoreCase("A")||user.getUSER_REGION().equalsIgnoreCase("C")
				||user.getUSER_REGION().equalsIgnoreCase("I"))) {
			
			//edited by raghav for INDIA and CHINA supervisors report access issue : 19/12/16
		//	criteria.add(Restrictions.eq("reportRegion", user.getUSER_REGION()));
			criteria.add(Restrictions.eq("reportRegion", "A"));
			
		}
		//Commented and edited by T7166PA--Ends
		}
		// RP-APAC -->ends
			
		return (List<Report>) criteria.list();
		
	}
	

}
